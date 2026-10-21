# Legacy Admin Portal E2E inventory and Playwright migration plan

**Related cleanup PR:** [Talent-Catalog/talentcatalog #3927](https://github.com/Talent-Catalog/talentcatalog/pull/3927).  
**Legacy source root:** `ui/admin-portal/e2e/src/`  
**Target:** Eventually replace legacy browser coverage with Playwright without reproducing stale or destructive assumptions.  
**Scope of PR #3927:** Remove Protractor from `admin-portal`, `candidate-portal`, and `public-portal`; document legacy scenarios, but **do not represent documentation as working Playwright tests**. The PR deletes the old spec files; the Playwright implementations below are a future-work inventory, not new tests in that PR.

## Key findings and migration policy

- **27 Admin Portal E2E spec files were identified**: 26 contain test cases and one (`view-job-preparation-items.e2e.spec.ts`) is a placeholder with no cases. A single legacy file can contain multiple cases; the table below is a file-by-file inventory, not a count of individual test assertions.
- The Candidate Portal already has maintained Playwright tests under `ui/candidate-portal/playwright-tests/` and `playwright.config.ts`. Preserve those files, scripts, and the existing `tc-candidate-portal-e2e.yml` workflow.
- The Public and Candidate Portal legacy scaffold specs only assert generated welcome text. Do not treat them as valuable present-day product coverage. The Public Portal needs a new Playwright smoke suite in a later PR; the Candidate Portal already has Verify+ Playwright coverage.
- Treat the legacy test titles below as *intent*, not proof of current UI behavior or test success. Before migration, compare each case with the current UI and acceptance criteria. Do not copy old CSS selectors, sleeps, or any credential constants blindly.

## All 27 Admin Portal files mapped to Playwright

Paths under **Legacy spec** are relative to `ui/admin-portal/e2e/src/`. Paths under **Proposed Playwright spec** are relative to a future `ui/admin-portal/playwright-tests/` directory. Priority: **P0** essential regression protection, **P1** follow-on coverage, **P2** optional/needs product validation, **Archive** no executable case to migrate.

| # | Legacy spec | Legacy scenarios to preserve or reassess | Proposed Playwright spec | Priority / fixture guidance |
|---|---|---|---|---|
| 01 | `app.e2e-spec.ts` | Authenticated app header is visible | `smoke/authenticated-header.spec.ts` | P1; authenticated isolated account |
| 02 | `components/account/change-password/change-password.e2e.spec.ts` | Invalid-field validation; successful password change; cancel modal without updating password | `account/change-password.spec.ts` | P0; disposable user, restore known password; never change a shared account |
| 03 | `components/account/login/login.e2e.spec.ts` | Invalid-credential error; successful login; advertised logout (existing code has logout step commented out) | `auth/login.spec.ts` | P0; login success and **real** logout as separate Playwright assertions; credential and MFA secrets from environment |
| 04 | `components/account/reset-password/reset-password.e2e.spec.ts` | Invalid email validation; request reset email for valid email | `auth/reset-password.spec.ts` | P0; test mailbox or stubbed mail delivery; do not send to real users |
| 05 | `components/jobs/job-home/job-home.e2e.spec.ts` | Live jobs; starred jobs; organization-created jobs; candidate cases; TBB source cases; new-job entry point | `jobs/dashboard-tabs.spec.ts` | P1; known fixtures for role, tenant, job and case types |
| 06 | `components/jobs/job/jobs.e2e.spec.ts` | Job list, loading/empty states, search form, headers, records, detail navigation, pagination, totals, filters by stage/country, sort by name/stage/created/due | `jobs/job-list.spec.ts` | P0; deterministic seeded jobs, assert real ordering and filter results; replace fixed `browser.sleep` |
| 07 | `components/jobs/jobs-with-details/jobs-with-details.e2e.spec.ts` | Live jobs list; initial render; search-driven list; selected job detail side-panel | `jobs/job-list-details.spec.ts` | P1; seeded job with stable details, avoid brittle class selectors |
| 08 | `components/jobs/new-job/new-job.e2e.spec.ts` | New-job tab renders; invalid link validation | `jobs/create-job.spec.ts` | P0; add a successful isolated creation scenario after requirements review |
| 09 | `components/jobs/view/info/edit-job-info/edit-job-info.e2e.spec.ts` | Edit job info modal opens; changes save and modal closes | `jobs/edit-job-info.spec.ts` | P0; pre-created test job and cleanup; verify persisted data after reload |
| 10 | `components/jobs/view/info/view-job-info/view-job-info.e2e.spec.ts` | Job information renders accurately; edit button enters edit mode | `jobs/job-info.spec.ts` | P1; compare known job fixture values |
| 11 | `components/jobs/view/source-contacts/job-source-contacts-with-chats/job-source-contacts-with-chats.e2e.spec.ts` | Select source partner chat; mark chat as read | `jobs/source-contact-chat.spec.ts` | P1; isolated chat fixture with unread state, restore state |
| 12 | `components/jobs/view/source-contacts/view-job-source-contacts/view-job-source-contacts.e2e.spec.ts` | Selecting source partner triggers relevant UI update/event | `jobs/source-contact-selection.spec.ts` | P1; assert visible functional result rather than Angular internal event |
| 13 | `components/jobs/view/tab/job-general-tab/job-general-tab.e2e.spec.ts` | General tab visible; clicking it displays general information | `jobs/tabs/general.spec.ts` | P1; seeded job and tab navigation |
| 14 | `components/jobs/view/tab/job-group-chats-tab/job-group-chats-tab.e2e.spec.ts` | Group chats tab visible; chat posts render | `jobs/tabs/group-chats.spec.ts` | P1; seeded conversation fixture |
| 15 | `components/jobs/view/tab/job-intake-tab/job-intake-tab.e2e.spec.ts` | Intake tab; Job Opportunity Intake heading; employer panels | `jobs/tabs/intake.spec.ts` | P1; job with JOI and employer test records |
| 16 | `components/jobs/view/tab/job-source-contacts-tab/job-source-contacts-tab.e2e.spec.ts` | Source contacts tab and list | `jobs/tabs/source-contacts.spec.ts` | P1; seeded source contact records |
| 17 | `components/jobs/view/tab/job-suggested-searches-tab/job-suggested-searches-tab.e2e.spec.ts` | Suggested searches tab and details | `jobs/tabs/suggested-searches.spec.ts` | P1; seeded search records |
| 18 | `components/jobs/view/tab/job-upload-tab/job-upload-tab.e2e.spec.ts` | Uploads tab; job-related uploads listed | `jobs/tabs/uploads.spec.ts` | P1; safe test attachment, never download or overwrite real documents |
| 19 | `components/jobs/view/view-job-from-url/view-job-from-url.e2e.spec.ts` | Deep link opens job page and loads its details | `jobs/job-deep-link.spec.ts` | P0; valid/invalid job IDs, role-access check |
| 20 | `components/jobs/view/view-job-preparation-items/view-job-preparation-items.e2e.spec.ts` | **No tests**: placeholder file only | No migration required | Archive; create a fresh feature test only if behavior still exists |
| 21 | `components/jobs/view/view-job-submission-list/view-job-submission-list.e2e.spec.ts` | TBB job cases and submission list render | `jobs/job-submissions.spec.ts` | P1; TBB-specific fixture; do not assume OPC/GRN data parity |
| 22 | `components/jobs/view/view-job-suggested-searches/view-job-suggested-searches.e2e.spec.ts` | Starred-job context; add suggested search; delete last created job | `jobs/suggested-search-crud.spec.ts` | P1; destructive deletion **only** on a job created by this test, with teardown |
| 23 | `components/jobs/view/view-job-summary/view-job-summary.e2e.spec.ts` | Job summary input; save changes; cancel without changes | `jobs/job-summary.spec.ts` | P1; verify saved state survives reload and cancel does not persist |
| 24 | `components/jobs/view/view-job-uploads/view-job-uploads.e2e.spec.ts` | Uploads display; available upload-related actions | `jobs/job-uploads.spec.ts` | P1; fixture files and cleanup; no production S3 writes |
| 25 | `components/jobs/view/view-job/view-job.e2e.spec.ts` | Job page; toggle starred status | `jobs/job-starred.spec.ts` | P1; assert persisted starred state and restore original |
| 26 | `components/settings/users/cleanup.e2e.spec.ts` | Delete test user | `admin/users-cleanup.spec.ts` | P0 safety guard; deletion only of identities created in that run, never arbitrary database users |
| 27 | `components/settings/users/create-update-user.e2e.spec.ts` | Create user; update user fields | `admin/user-crud.spec.ts` | P0; unique generated test user, scoped admin permissions, and teardown |

