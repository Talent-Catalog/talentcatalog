# Karma/Jasmine to Vitest migration

**Ticket:** TC-1610  
**Implementation:** [PR #3999](https://github.com/Talent-Catalog/talentcatalog/pull/3999)  
**Merged to staging:** 19 November 2026  
**Scope:** Angular unit tests in `admin-portal`, `candidate-portal` and `public-portal`  
**Developer instructions:** [Testing wiki](https://github.com/Talent-Catalog/talentcatalog/wiki/Testing)

## 1. Background and motivation

The three Angular portals previously used Karma as the test runner and Jasmine as the testing and mocking framework. Karma is deprecated, and maintaining its browser-launcher and reporter dependencies adds maintenance overhead.

The Angular upgrade in [TC-1579](https://linear.app/open-pathway-collective/issue/TC-1579/upgrade-the-angular-front-ends-from-17-to-a-supported-version) deliberately retained Karma/Jasmine to reduce the risk of the Angular upgrade. Its [final upgrade report](../../ui/docs/TC-1579-final-angular-upgrade-report.md) identified a high-severity scanner finding in the Karma/Chokidar/Braces dependency chain. The finding related to development tooling rather than the deployed application. The assessed severity should follow [ADR 007](../decisions/007-security-issue-tracking.md) and the Operations Security Policy, with the rationale recorded on the corresponding Dependabot alert and security issue (see [TC-1248](https://linear.app/open-pathway-collective/issue/TC-1248/resolve-critical-and-high-dependabot-vulnerabilities-in-talentcatalog)).

TC-1610 replaces the legacy runner with Angular's supported Vitest integration, with the following objectives:

- Retain the existing unit-test behaviour and statement coverage across all three portals.
- Keep Angular TestBed, fixtures and existing dependency-injection test patterns.
- Remove obsolete Karma/Jasmine configuration and dependencies.
- Preserve the CI test and Codecov reporting workflows.
- Avoid unrelated changes to application functionality.

## 2. What changed

| Area | Before | After |
| --- | --- | --- |
| Angular test builder | `@angular/build:karma` | `@angular/build:unit-test` |
| Runner and test APIs | Karma 6.4.4 and Jasmine Core 4.6.1 | Vitest 5.1.1 at migration |
| Test environment | Chrome/ChromeHeadless launched by Karma | Node.js with jsdom for DOM-based unit tests |
| Angular test setup | Legacy `src/test.ts` and Karma configuration | Angular-managed test bootstrap and portal-specific setup where needed |
| Mocking | Jasmine spies and spy objects | `vi.fn()`, `vi.spyOn()` and Vitest mock types |
| Coverage | Karma coverage plugin | Vitest with Istanbul instrumentation |
| CI interface | Karma-oriented test execution | `npm run test:coverage` using the Angular CLI |
| Report integration | Codecov | Codecov retained, including portal-specific flags |

The migration was delivered together in PR #3999. The PR lists 642 modified `.spec.ts` files and 673 changed files overall. The three portals retain separate Angular workspaces and package lockfiles. No production component implementations or HTML templates were changed in this PR. Backend tests, Playwright end-to-end tests and performance tests are outside the scope of the runner migration.

The versions above describe the migration snapshot, not a requirement to pin future upgrades to those versions. Consult each portal's `package.json` and `package-lock.json` for the active versions. At the reviewed staging snapshot, Angular is 22.2.3, Vitest and `@vitest/coverage-istanbul` are 5.1.1 in the lockfiles, and the repository's `.nvmrc` pins Node 24.22.0. The package manifests include a Vite override of 8.3.5. Review version compatibility when upgrading Angular, Vitest, Vite, jsdom or Zone.js together.

## 3. Migration approach

The following sequence summarizes the migration work and provides a reproducible reference for future runner migrations. It is not a command sequence that developers need to rerun on the migrated branch.

1. Establish a working Karma baseline for each portal, recording the passing, skipped and failing tests and the statement coverage.
2. Use Angular's Karma-to-Vitest migration tooling to convert the test target and supported Jasmine APIs. The Angular schematic can be invoked from the relevant portal workspace on an unmigrated branch, for example:

   ```bash
   npx ng generate @schematics/angular:refactor-jasmine-vitest --project=admin-portal
   ```

   Use the equivalent project name for the other portals. Review the generated changes rather than treating the schematic as a complete migration.
3. Configure the `@angular/build:unit-test` builder, the portal's `build:testing` target, test TypeScript configuration and required setup files.
4. Review every changed specification for mock return values, spy behaviour, async completion, event handling, Angular change detection and meaningful assertions. Preserve existing behaviour rather than mechanically changing APIs until tests pass.
5. Resolve compatibility issues arising from jsdom's incomplete browser APIs and the existing Zone.js-based Angular tests. Keep these shims and stubs in test-only code.
6. Remove obsolete `karma.conf.js`, `src/test.ts`, legacy `src/tsconfig.spec.json`, Karma/Jasmine packages and their old TypeScript references. Regenerate the package lockfiles as part of the migration.
7. Run the full unit-test suite and coverage for each portal. Check for unhandled errors as well as failed assertions.
8. Update the three CI workflows, confirm LCOV generation and verify successful Codecov upload steps.
9. Compare results against the pre-migration baseline, review the dependency tree and confirm the migration on staging.

The [PR files](https://github.com/Talent-Catalog/talentcatalog/pull/3999/files) contain the authoritative implementation changes.

## 4. Configuration and portal-specific differences

All three portals use `angular.json`, `tsconfig.spec.json`, `package.json` and `package-lock.json` in their respective `ui/<portal>` directory. The `test` target delegates to the Angular unit-test builder and uses the portal's `build:testing` configuration. Tests remain under `src/` as `*.spec.ts` files. The test TypeScript configurations include `vitest/globals` and Node types.

| Portal | Test setup | Coverage defaults/reporters |
| --- | --- | --- |
| Admin | `src/test-setup.ts` and `src/test-global-custom-elements.ts` | Coverage requested explicitly by the coverage script; `text-summary`, `html`, `lcov` |
| Candidate | `src/test-setup.ts` | Coverage enabled in the test target; `html`, `lcovonly`, `text-summary` |
| Public | No custom `setupFiles` in the test target; `src/polyfills.ts` in the spec TypeScript configuration | Coverage enabled in the test target; `html`, `lcovonly`, `text-summary` |

All three testing build configurations load `zone.js/testing` and `zone.js/plugins/vitest-patch` to retain support for existing Zone-based testing patterns. Candidate and Public use testing-specific build settings including `aot: false` and source maps. The Admin testing build uses its own polyfill configuration. These are test build settings, not production build changes.

### Admin Portal

- [`src/test-setup.ts`](../../ui/admin-portal/src/test-setup.ts) imports the shared custom-element stubs, restores spied methods after tests and provides test-compatible storage, `document.execCommand` and object URL behaviour.
- [`src/test-global-custom-elements.ts`](../../ui/admin-portal/src/test-global-custom-elements.ts) centralizes shared `tc-*` component stubs, preserves Zone-based change detection and prevents duplicate stub module registration within one TestBed lifecycle. Tests exercising real components must not accidentally match both the real and stub selectors.
- [`router-link-stub.directive.ts`](../../ui/admin-portal/src/test-helpers/router-link-stub.directive.ts) provides a reusable router-link stub.

### Candidate Portal

- [`src/test-setup.ts`](../../ui/candidate-portal/src/test-setup.ts) provides shared Angular TestBed setup for routing, translation and HTTP testing, restores spies and installs test-only compatibility support for `localStorage`, `matchMedia`, `scrollIntoView` and `ImageData`.
- The `ImageData` fallback supports pixel data but is not a canvas renderer or image decoder. Tests requiring real media playback, image decoding or browser rendering need appropriate scoped mocks or real-browser tests.
- The existing Candidate Portal Playwright scripts, configuration and tests remain separate from Vitest.

### Public Portal

- The public portal uses Angular's built-in test bootstrap and its testing build configuration without an additional `setupFiles` entry.
- Keep shared setup minimal; introduce a custom setup file only when a demonstrated test requirement justifies it.

## 5. Jasmine to Vitest API changes

Most test structure and Angular testing APIs remain unchanged: `describe`, `it`, `beforeEach`, `afterEach`, `expect`, `TestBed.configureTestingModule`, `ComponentFixture`, and `fixture.detectChanges()` are still familiar.

| Jasmine | Vitest equivalent / approach |
| --- | --- |
| `jasmine.createSpy('name')` | `vi.fn().mockName('name')` |
| `jasmine.createSpyObj('Service', ['load'])` | `{load: vi.fn().mockName('Service.load')}` |
| `jasmine.SpyObj<Service>` | `MockedObject<Service>` or a narrowly typed mock |
| `spyOn(object, 'method')` | `vi.spyOn(object, 'method')` |
| `.and.returnValue(value)` | `.mockReturnValue(value)` |
| `.and.callFake(fn)` | `.mockImplementation(fn)` |
| `.and.throwError(error)` | `.mockImplementation(() => { throw error; })` |
| `.and.callThrough()` | A new `vi.spyOn()` calls the original implementation by default; restore any existing mock before relying on that behaviour |
| `.calls.count()` | `.mock.calls.length` or `expect(mock).toHaveBeenCalledTimes(n)` |
| `.calls.mostRecent().args` | `.mock.lastCall` |
| `.calls.reset()` | `.mockClear()` |
| `jasmine.any(Type)` | `expect.any(Type)` |
| `jasmine.objectContaining(value)` | `expect.objectContaining(value)` |
| `jasmine.arrayContaining(value)` | `expect.arrayContaining(value)` |

A simple example based on the migrated [ChangePasswordComponent spec](../../ui/admin-portal/src/app/components/account/change-password/change-password.component.spec.ts):

Before:

```typescript
const userServiceSpy = jasmine.createSpyObj('UserService', ['updatePassword']);
userServiceSpy.updatePassword.and.returnValue(of(true));
```

After:

```typescript
import {vi} from 'vitest';
import {of} from 'rxjs';

const userServiceSpy = {
    updatePassword: vi.fn().mockName('UserService.updatePassword')
};
userServiceSpy.updatePassword.mockReturnValue(of(true));
```

When using `TestBed.inject`, the existing tests use the `MockedObject<T>` type where appropriate. Prefer narrowly typed mocks when only a few service methods are needed. Retain the existing licence header and import style.

### Important behavioural differences

- A Jasmine `spyOn()` stubs by default; a new Vitest `vi.spyOn()` calls the original method by default. Stub return values explicitly when the test must not execute production side effects.
- `vi.restoreAllMocks()` restores methods spied on with `vi.spyOn()`; it is not a substitute for resetting standalone `vi.fn()` mocks or global state. Recreate mocks per test or clear/reset them deliberately.
- Await asynchronous work or return its Promise. Tests should not pass while unhandled rejections occur after the assertion. For RxJS, provide deterministic values/errors and verify the expected effect.
- The existing Zone.js test integration allows supported Angular `fakeAsync`/`tick` patterns to remain. Do not mix Angular fakeAsync timers and Vitest fake timers in a single test without a demonstrated need and compatibility check. If using `vi.useFakeTimers()`, restore real timers in cleanup.
- Mock browser APIs only to the extent needed to test the unit. jsdom is not a real rendering engine; use Playwright for browser behaviour, layout, browser-specific regressions, or full end-to-end workflows.

## 6. CI and coverage reporting

The three portal workflows are:

- [Admin Portal Tests](../../.github/workflows/tc-admin-portal-test.yml)
- [Candidate Portal Tests](../../.github/workflows/tc-candidate-portal-test.yml)
- [Public Portal Tests](../../.github/workflows/tc-public-portal-test.yml)

They run on `ubuntu-latest` for relevant pull requests targeting `staging` or `master` and pushes to `staging`. Each workflow checks out the repository, sets up Node from `.nvmrc`, caches npm downloads using the portal lockfile, runs `npm ci`, executes `npm run test:coverage` with `CI=true`, and uploads its LCOV report to Codecov. Coverage and test-result directories are collected on failure when present.

The workflow scripts point to `coverage/<portal>/lcov.info` relative to the portal workspace. All three use Istanbul coverage with a terminal statement summary, HTML and LCOV output. The existing [`codecov.yml`](../../codecov.yml) remains the central configuration and retains the `admin-portal`, `candidate-portal`, `public-portal` and `server` flags. Project and patch statuses are informational; this is not a new hard coverage threshold. Codecov upload failures are configured to fail the portal workflow.

Daily testing commands and troubleshooting are maintained in the [Testing wiki](https://github.com/Talent-Catalog/talentcatalog/wiki/Testing).

## 7. Validation and results

The pre-migration figures below are from the documented TC-1579 Angular upgrade baseline, not a new rerun of the immediate parent commit. The post-migration figures are from the Istanbul summaries and test counts in the 19 November 2026 staging push workflows for merge commit [`ab889b6`](https://github.com/Talent-Catalog/talentcatalog/commit/ab889b6bb13d70863ad8dabaa766682396917523).

| Portal | Karma passing | Karma skipped | Vitest passing | Vitest test files | Karma statements | Vitest statements | Difference (pp) |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| Admin | 3,114 | 0 | 3,118 | 497 | ~83.55% | 85.94% | +2.39 |
| Candidate | 1,532 | 1 | 1,533 | 166 | ~81.44% | 84.96% | +3.52 |
| Public | 17 | 0 | 17 | 5 | 100% | 100% | 0.00 |
| **Total** | **4,663** | **1** | **4,668** | **668** | - | - | - |

Vitest reported all of the tests in those runs passing. The baseline and post-migration snapshots differ in test counts and may differ in code and instrumentation; the table demonstrates that the reported statement coverage did not decline, but should not be interpreted as a controlled measurement of improvement attributable only to Vitest. The original baseline notes small timing-dependent coverage variation in the Candidate Portal. No run-time speedup percentage is claimed without matched baseline runs.

| Staging workflow | Unit tests | Codecov upload step |
| --- | --- | --- |
| [Admin](https://github.com/Talent-Catalog/talentcatalog/actions/runs/44505444077) | Passed | Passed |
| [Candidate](https://github.com/Talent-Catalog/talentcatalog/actions/runs/44505444061) | Passed | Passed |
| [Public](https://github.com/Talent-Catalog/talentcatalog/actions/runs/44505443911) | Passed | Passed |

The job logs report that the coverage uploads were queued for processing. To confirm final dashboard processing, inspect the corresponding commit on [Codecov](https://codecov.io/gh/Talent-Catalog/talentcatalog). The staging build/deploy workflow also completed successfully ([run](https://github.com/Talent-Catalog/talentcatalog/actions/runs/44505444140)); that status is not a substitute for the three unit-test workflows.

## 8. Dependency and security validation

The package lockfiles at the PR's base commit contained Karma 6.4.4, Jasmine Core 4.6.1, the Karma-specific Chokidar 3.6.0 dependency and Braces 3.0.3. The lockfiles at the merge commit no longer contain those Karma/Jasmine packages or Braces. Chokidar 5.0.0 remains for other tooling, and should not be removed simply because the Karma-specific chain has been eliminated.

To recheck the installed dependency tree, run these commands separately from each portal directory after `npm ci`:

```bash
npm ls karma jasmine-core braces --all
npm explain chokidar
```

An empty tree for the removed packages (which can return a nonzero exit code) is expected for the migration snapshot. A later dependency update may introduce a package through a different chain, so inspect the output instead of requiring all packages with those names to be absent forever.

The lockfile evidence confirms removal of the old dependency path. It does not establish the status of every Dependabot alert, prove that every remaining package is free of vulnerabilities, or confirm that a security issue has been closed. Check the relevant alert on the repository's default branch, record the removal and the assessed development-tooling impact, and update the associated Linear ticket in accordance with ADR 007.

## 9. Maintenance guidance

- Run unit tests and coverage for the affected portal before opening a pull request. Run all three for shared tooling changes.
- Preserve TestBed and mocking semantics during future upgrades; do not remove assertions or skip tests to obtain a green build.
- Add new shared shims only for common demonstrated test requirements. Keep them test-only and avoid masking genuine application failures.
- Check unhandled rejections, unexpected console errors and coverage changes, not just the number of passing tests.
- Maintain browser-specific checks in the Playwright suite where jsdom cannot reproduce the relevant behaviour.
- Update the Testing wiki when the normal test commands, runner configuration or coverage process changes. This document remains the historical record of TC-1610.

