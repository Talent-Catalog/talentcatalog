# [TC-1579](https://linear.app/open-pathway-collective/issue/TC-1579/upgrade-the-angular-front-ends-from-17-to-a-supported-version) — Final Angular Upgrade Review

**Recommendation: Approve for merge into** `staging`**.**

The upgrade of all three Talent Catalog Angular applications from Angular 17.3 to Angular 22.2.3 is complete. The PR build has succeeded, all 4,663 unit tests pass, and local application testing has not identified functional regressions.

The migration also eliminates the Angular framework and build-toolchain vulnerabilities identified during the initial assessment.

The remaining security findings, warnings and technical debt are documented below. None is considered a blocker for [TC-1579](https://linear.app/open-pathway-collective/issue/TC-1579/upgrade-the-angular-front-ends-from-17-to-a-supported-version), although several warrant separate follow-up issues.

## 1. Executive summary

| Area | Result |
| -- | -- |
| Public portal | Angular 17.3 → 22.2.3 |
| Candidate portal | Angular 17.3 → 22.2.3 |
| Admin portal | Angular 17.3 → 22.2.3 |
| Node.js | 24.22.0 |
| TypeScript | 6.0.3 |
| RxJS | 7.8.2 |
| Unit tests | **4,663 passing, 1 existing skip** |
| Production, staging and development builds | All pass |
| Gradle UI JAR packaging | All pass |
| PR build | Successful |
| npm audit findings | **251 → 24** |
| Angular/toolchain security advisories | None remaining in local audit |
| Local application testing | No apparent regressions |
| Candidate Playwright | Outstanding, non-blocking |
| Amplify PR preview | Failed; non-blocking because Amplify is being phased out |

The work was performed incrementally, upgrading each portal separately and running builds and tests after every Angular major-version step.

No significant loss of test coverage was observed.

## 2. Dependency upgrades

All three applications now use consistent core versions.

| Dependency | Previous | New |
| -- | -- | -- |
| Angular framework / CLI / build | 17.3.x | 22.2.3 |
| TypeScript | 5.2.2 | 6.0.3 |
| RxJS | 6.x | 7.8.2 |
| zone.js | 0.14.x | 0.16.3 |
| ng-bootstrap | 16 | 21.0.0 |
| ng-select | 12 | 23.11.0 |
| ngx-wig | 17 | 22.0.0 |
| ngx-infinite-scroll (admin) | 17 | 22.0.0 |
| ng2-dragula (admin) | 5.1 | 9.0 |

The ng-select dependency is constrained to `~23.11.0` in candidate-portal and admin-portal to avoid known compatibility problems in earlier 23.x releases.

The admin portal's `ng2-charts` dependency is pinned to 4.0.0, preserving the previously installed version and avoiding an unnecessary Angular CDK dependency.

Several other libraries remain on their existing versions, including Quill, ngx-quill, Chart.js, emoji-mart, ngx-translate and the Preset SDK. These were retained to avoid introducing unrelated functional changes.

Obsolete tooling removed includes Protractor, TSLint, Codelyzer and unused build/test dependencies.

## 3. Why the PR changes more than 1,000 files

Most of the changed files contain mechanical Angular compatibility migrations rather than functional changes.

### 3.1 Explicit `standalone: false`

Angular 19 changed the default interpretation of components, directives and pipes to standalone.

The Talent Catalog applications use an NgModule-based architecture. Previously, these declarations were implicitly non-standalone.

The migration therefore adds:

```typescript
standalone: false
```

This explicitly preserves the existing NgModule architecture.

Converting the entire application to standalone components was intentionally excluded from [TC-1579](https://linear.app/open-pathway-collective/issue/TC-1579/upgrade-the-angular-front-ends-from-17-to-a-supported-version) because it would introduce substantial additional work and regression risk without being necessary for the security upgrade.

### 3.2 Explicit `ChangeDetectionStrategy.Eager`

Angular 22 introduced changes to change-detection defaults and terminology.

The migration adds:

```typescript
changeDetection: ChangeDetectionStrategy.Eager
```

to components that previously relied on the historical default change-detection behaviour.

This preserves existing behaviour rather than introducing a more selective change-detection strategy as part of the upgrade.

### 3.3 Other mechanical migrations

The upgrade also required:

* Replacing deprecated `HttpClientModule` and testing-module configuration with `provideHttpClient(...)` and `provideHttpClientTesting()`.
* Explicitly configuring zone-based change detection.
* Preserving the existing HTTP XHR backend.
* Applying Angular's safe-navigation template compatibility migration.
* Updating TypeScript configuration and imports for TypeScript 6.
* Updating Karma/Jasmine configuration for the new builder.
* Making small compatibility changes in existing components and tests.

The Angular control-flow migration (`*ngIf`/`*ngFor` to `@if`/`@for`) was intentionally deferred.

**The large diff is therefore primarily the result of preserving existing behaviour across several Angular major-version changes. It does not represent a comparable amount of new application functionality.**

## 4. Application builder and Spring Boot compatibility

All three portals were migrated from the legacy webpack-based Angular builder to the newer application builder.

The Spring Boot distribution layout was preserved:

* `ui-bundle/<portal>/index.html`
* Root-level JavaScript and CSS bundles
* Existing `assets/` directories
* Correct portal-specific base URLs
* SPA deep-link routing

### Media-serving issue discovered and resolved

Final verification identified a deployment regression that earlier build tests had not exposed.

The new builder initially placed CSS-referenced fonts and images under `media/`. The existing Spring Boot resource handlers did not serve this directory, causing requests for fonts to return `index.html`.

This would have broken Font Awesome icons across all three portals and a CSS background image in candidate-portal.

The issue was resolved by configuring each portal with:

```json
"outputPath": {
  "base": "dist/<portal>",
  "browser": "",
  "media": "."
}
```

The fix was verified using the repository's actual Spring `WebConfiguration` in a minimal Spring Boot 3.5.13 application containing the three packaged UI JARs.

Verification confirmed:

* Font Awesome WOFF2 and TTF files return HTTP 200 with correct MIME types.
* JavaScript, CSS, images and SVG files are served correctly.
* Deep links continue to return the SPA entry point.
* Headless Chrome successfully loads the fonts.
* No unexpected `media/` references remain.

The full backend was not started for this specific test because its database, Redis and Elasticsearch dependencies were unavailable. The static-resource behavior was nevertheless exercised through the real Spring resource configuration.

## 5. Testing and verification

| Portal | Unit tests | Statement coverage |
| -- | -- | -- |
| Public | 17/17 | 100% |
| Candidate | 1,532 pass, 1 skipped | Approximately 81.44% |
| Admin | 3,114/3,114 | Approximately 83.55% |
| **Total** | **4,663 pass, 1 skipped** |  |

All production, staging and development builds pass.

Gradle packaging succeeds for all three portals.

The candidate portal's statement coverage varies slightly between identical runs because of one timing-dependent execution path. There is no material decline from the baseline.

Additional verification included application initialization, routing, assets, authentication interceptors, deep links and HTTP request behavior.

The admin portal's authentication request sequence was compared with the Angular 17 application and found to be identical.

The PR code built successfully in CI, and the upgraded application was also run locally without apparent problems.

### Outstanding non-blocking verification

**Candidate Playwright:** The E2E suite could not be executed during the initial local migration because it requires backend services and credentials. Playwright is relatively new to this project, so completion of this suite is not being treated as a condition for accepting [TC-1579](https://linear.app/open-pathway-collective/issue/TC-1579/upgrade-the-angular-front-ends-from-17-to-a-supported-version).

**Amplify previews:** The Amplify PR preview failed. Amplify is being phased out, so this is not considered a merge blocker. The failure was not established to be an Angular application regression.

**Manual functional testing:** The local application test was satisfactory, but it does not constitute exhaustive regression testing of every feature. Areas worth monitoring after deployment include candidate search, matching explanations, profile forms, date pickers, ng-select controls, chat, drag-and-drop, charts and PDF exports.

## 6. Security improvements

The upgrade substantially reduces the number of reported npm vulnerabilities.

| Portal | Before | After | Remaining production findings |
| -- | -- | -- | -- |
| Public | 80 | 6 | 0 |
| Candidate | 83 | 8 | 2 |
| Admin | 88 | 10 | 4 |
| **Total** | **251** | **24** | **6** |

These are npm audit finding counts, not necessarily distinct vulnerabilities.

The final local audit found no outstanding advisories targeting Angular, Angular CLI, the new Angular builder, esbuild, Vite, RxJS, zone.js or TypeScript.

Dependabot alert closure on the default `staging` branch should be confirmed after merge.

### Remaining security findings

**S1 — jsPDF 2.5.2 (admin): Critical**

The remaining jsPDF advisories include path traversal and HTML-injection issues.

The application uses jsPDF primarily for generating PDF files from canvas images, rather than the affected HTML-rendering functionality. This limits practical exposure but does not eliminate the need for remediation.

**Recommendation:** Create a high-priority issue to upgrade jsPDF to a patched 4.x release, verifying PDF export compatibility.

**S2 — DOMPurify 2.5.9 (admin): Moderate**

DOMPurify is included through jsPDF. The vulnerable functionality is not believed to be exercised by the application's current PDF export paths.

**Recommendation:** Address together with the jsPDF upgrade.

**S3 — Quill 1.3.7 (candidate and admin): Moderate XSS**

Both portals retain Quill 1 and ngx-quill 16.

Angular sanitization of displayed HTML mitigates some risk, but the vulnerable editor dependency remains.

**Recommendation:** Create a separate issue to upgrade to Quill 2 and a compatible ngx-quill release. This requires careful regression testing of chat, custom toolbar behavior, link validation and editor extensions.

**S4 — braces 3.0.3 via Karma/chokidar (all portals): High, development only**

This is a regular-expression denial-of-service advisory affecting test tooling. It is not included in the deployed application.

No patched version is available through the current Karma dependency chain.

**Recommendation:** Document or dismiss the alert with justification, and consider migrating from Karma to Vitest in a separate project.

These vulnerabilities pre-date the Angular upgrade and are not reasons to expand [TC-1579](https://linear.app/open-pathway-collective/issue/TC-1579/upgrade-the-angular-front-ends-from-17-to-a-supported-version) further.

## 7. Warnings and technical debt

### 7.1 Sass deprecation warnings

The newer Sass compiler reports deprecated `@import` usage and global Sass functions.

Reported warning counts per build:

| Portal | Warnings |
| -- | -- |
| Public | 4 |
| Candidate | 142 |
| Admin | 172 |

The warnings do not currently prevent compilation or change the resulting styles.

**Recommendation:** Migrate to Sass modules (`@use`/`@forward`) and module-specific functions before removal of the deprecated features in a future Dart Sass release.

### 7.2 Initial bundle sizes

| Portal | Before | After |
| -- | -- | -- |
| Candidate | 3.77 MB | 4.29 MB |
| Admin | 4.71 MB | 4.84 MB |

The admin portal has approximately **160 kB of headroom** before reaching its configured 5 MB build-error threshold.

Analysis identified several large dependencies:

* moment-timezone: approximately 915 kB
* Emoji picker: approximately 500 kB
* jsPDF: approximately 434 kB
* html2canvas: approximately 239 kB

Potential improvements include replacing moment-timezone where only browser timezone detection is required, lazy-loading PDF export libraries, and lazy-loading the emoji picker.

These could materially reduce the initial bundle without changing the Angular upgrade itself.

**Recommendation:** Create a separate bundle optimization issue rather than simply raising the budget.

### 7.3 Angular forms warning NG01354

Admin unit tests report 42 occurrences of `NG01354`.

The warnings originate from reusable date-picker and language-score components that use internal `ngModel` controls while explicitly synchronizing with parent reactive-form controls.

The investigation found no new functional risk. The controls were not intended to register directly with the parent `FormGroupDirective`.

**Recommendation:** Add `[ngModelOptions]="{standalone: true}"` to the relevant internal inputs in a future cleanup.

### 7.4 CommonJS warnings

Existing CommonJS warnings remain for dependencies including Quill, html2canvas, canvg, angular2-uuid and the Preset SDK.

One new DOMPurify CommonJS warning is associated with the existing jsPDF dependency.

These are build optimization warnings, not evidence of functional regressions.

### 7.5 Other build and test warnings

* Font Awesome font 404 messages occur in Karma test output; tests pass and deployed Spring resource serving has been separately verified.
* npm 11 reports restrictions relating to dependency installation scripts; local builds succeed.
* Some obsolete TypeScript compilation warnings disappeared with the builder migration.
* Jasmine 4.6.1 is retained for compatibility with Zone-based Karma tests.

## 8. Pre-existing defects identified

The upgrade review also uncovered issues unrelated to Angular 22.

### D1 — Recursive logout requests

In admin-portal and candidate-portal, an HTTP 401 response from the logout endpoint can cause the authentication interceptor to invoke logout repeatedly.

The investigation reproduced approximately 32,700 requests in 10 seconds under the simulated failure condition.

The same behavior exists in Angular 17.

The current backend normally permits and successfully handles logout requests, so the immediate likelihood is low. However, a proxy, gateway or future authentication change could expose the problem.

**Recommendation:** Create a separate issue to prevent authentication-expiry handling from recursively processing logout requests and make logout idempotent.

### D2 — Legacy `.doc` CV preview

The admin CV preview condition originally contained:

```typescript
cv.fileType != ('doc' && 'docx')
```

JavaScript evaluates the parenthesized expression to `'docx'`, meaning `.doc` files are not excluded as intended.

The migration made the existing behavior explicit without fixing it.

**Recommendation:** Correct the condition and add tests for both `.doc` and `.docx`.

### D3 — Minor type and test issues

Additional observations include getters returning an empty string despite a `string[]` type and a harmless test-provider configuration typo.

These should be addressed through ordinary maintenance rather than [TC-1579](https://linear.app/open-pathway-collective/issue/TC-1579/upgrade-the-angular-front-ends-from-17-to-a-supported-version).

## 9. Deferred Angular modernization

Several optional migrations were deliberately excluded to keep the upgrade focused.

These include:

* Converting NgModule-based components to standalone components.
* Replacing `*ngIf`, `*ngFor` and `*ngSwitch` with Angular's newer control-flow syntax.
* Adopting `OnPush` more widely or moving to zoneless change detection.
* Removing compatibility wrappers introduced by Angular's safe-navigation migration.
* Replacing deprecated dynamic bootstrap and animation APIs.
* Migrating from Karma to Vitest.
* Upgrading other third-party libraries beyond the versions necessary for Angular 22.

The admin portal also contained 30 old Protractor E2E specifications. Protractor was no longer supported or used in CI, so the obsolete runner and specifications were removed. The scenarios remain recoverable from Git history and could inform future Playwright coverage.

None of these modernization tasks is required to achieve the principal objective of [TC-1579](https://linear.app/open-pathway-collective/issue/TC-1579/upgrade-the-angular-front-ends-from-17-to-a-supported-version).

## 10. Recommended follow-up issues

| Priority | Follow-up | Reason |
| -- | -- | -- |
| **High** | Upgrade jsPDF and DOMPurify | Remaining critical production dependency advisory |
| **High** | Upgrade Quill 1 → Quill 2 | Remaining editor XSS advisory in two portals |
| **Medium** | Reduce admin initial bundle size | Only 160 kB below build-error threshold |
| **Medium** | Prevent recursive logout requests | Latent request-storm defect |
| **Medium** | Migrate deprecated Sass usage | Future compiler compatibility |
| **Low** | Fix `.doc` CV preview handling | Existing UX defect |
| **Low** | Explicitly mark internal `ngModel` controls standalone | Remove NG01354 warnings |
| **Low** | Review Karma → Vitest migration | Retire deprecated test tooling and dev-only vulnerability |
| **Optional** | Extend Playwright coverage | Improve future regression detection |
| **Optional** | Adopt newer Angular component/control-flow patterns | Long-term maintainability |
| **Optional** | Review unused dependencies and CommonJS packages | Dependency hygiene and bundle optimization |

The security remediation issues should be handled first. Optional architectural modernization can be scheduled independently.

## 11. Final assessment

[TC-1579](https://linear.app/open-pathway-collective/issue/TC-1579/upgrade-the-angular-front-ends-from-17-to-a-supported-version) **has achieved its primary objective.**

All three Talent Catalog front ends have moved from Angular 17 to Angular 22.2.3, with compatible dependencies and a supported build toolchain.

The upgrade preserves the existing NgModule architecture, change-detection behavior, routing, HTTP integration and Spring Boot deployment structure.

All 4,663 unit tests pass, with one pre-existing skipped test. Production builds and Gradle packaging succeed, the PR build succeeded, and local application testing has not revealed significant regressions.

The review also identified and resolved a genuine Spring Boot static-resource regression before merge.

The npm audit finding count fell from 251 to 24, and the local audit reports no remaining Angular framework or build-toolchain advisories.

The remaining vulnerabilities and technical debt have been identified and can be addressed through separate, focused issues.

**Final recommendation: Approve** [TC-1579](https://linear.app/open-pathway-collective/issue/TC-1579/upgrade-the-angular-front-ends-from-17-to-a-supported-version) **for merge into** `staging`**.** Confirm Dependabot alert closure after merge and track the remaining security issues separately.
