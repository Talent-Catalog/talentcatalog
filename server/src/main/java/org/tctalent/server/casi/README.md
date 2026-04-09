# CASI (Candidate Assistance Services Interface) — Developer Guide

This document provides an overview of the CASI module. It outlines the architecture, folder layout 
and fastest path for developers to add a new CandidateAssistanceService provider.

---

## Backend

### Layout

The CASI module is organized into several key packages:

```text
casi/
├─ api/                              // inbound HTTP
│  ├─ dto/                           // API response models
│  ├─ request/                       // request DTOs
│  ├─ ServicesAdminController        // admin endpoints (/api/admin/services)
│  └─ ServicesPortalController       // candidate-portal endpoints (/api/portal/services)
│
├─ application/                      // wiring & policies
│  ├─ config/                        // Spring @Configuration (allocator beans)
│  ├─ policy/                        // TaskPolicy, EligibilityPolicy per provider
│  │  ├─ TaskPolicy                  // interface: tasks on assign/redeem/reassign/expire
│  │  ├─ TaskPolicyRegistry          // auto-discovers TaskPolicy beans
│  │  ├─ EligibilityPolicy           // interface: is a candidate eligible?
│  │  ├─ EligibilityPolicyRegistry   // auto-discovers EligibilityPolicy beans
│  │  ├─ AlwaysEligiblePolicy        // default: always eligible
│  │  └─ ReferenceEligibilityPolicy // reference impl for REFERENCE provider (status-based)
│  └─ providers/                     // concrete provider services
│     ├─ duolingo/
│     ├─ linkedin/
│     └─ reference/                  // minimal reference implementation (dev-only)
│
├─ core/                             // application services
│  ├─ allocators/                    // inventory allocation strategies
│  │  ├─ ResourceAllocator           // interface (strategy)
│  │  └─ InventoryAllocator          // picks next AVAILABLE row (SELECT FOR UPDATE SKIP LOCKED)
│  ├─ importers/
│  │  └─ FileInventoryImporter       // interface for CSV importers
│  ├─ listeners/
│  │  ├─ EmailNotificationListener   // sends emails on events
│  │  └─ ServiceTaskOrchestrator     // assigns/closes tasks on events
│  ├─ scheduler/
│  │  └─ ResourceExpiryScheduler     // cron marks expired resources
│  └─ services/
│     ├─ CandidateAssistanceService  // main interface (port)
│     ├─ AbstractCandidateAssistanceService  // base class
│     ├─ AssignmentEngine            // orchestrates assign/reassign transactions
│     ├─ CandidateServiceRegistry    // service locator by provider+serviceCode
│     └─ CandidateServicesQueryService // cross-provider queries
│
└─ domain/                           // domain objects
   ├─ events/                        // ServiceAssigned/Redeemed/Expired/ReassignedEvent
   ├─ mappers/                       // entity <--> domain --> DTO
   ├─ model/                         // enums + value objects
   │  ├─ ServiceProvider             // DUOLINGO, LINKEDIN, REFERENCE
   │  ├─ ServiceCode                 // TEST_PROCTORED, TEST_NON_PROCTORED, PREMIUM_MEMBERSHIP, VOUCHER
   │  ├─ ResourceStatus              // AVAILABLE, RESERVED, SENT, REDEEMED, EXPIRED, DISABLED
   │  └─ AssignmentStatus            // ASSIGNED, REDEEMED, EXPIRED, DISABLED, REASSIGNED
   └─ persistence/                   // JPA entities + repos
      ├─ ServiceResourceEntity       // generic inventory row
      ├─ ServiceAssignmentEntity     // append-only ledger row
      └─ *Repository                 // Spring Data JPA repos
```

### Core Concepts

The CASI module is built around several core concepts:

- **ServiceResourceEntity**: generic inventory (provider, serviceCode, resourceCode, status, 
  candidate, assignedAt, expiresAt).
- **ServiceAssignmentEntity**: append-only ledger of assignments (provider, serviceCode, resourceId, 
  candidate, actor, status, assignedAt). 
- **AssignmentEngine**: orchestrates an assignment/reassignment (uses ResourceAllocator, saves 
  ledger, publishes events).
- **ResourceAllocator**: strategy for "how do I get a unit?" (e.g. InventoryAllocator).
- **Events**: ServiceAssignedEvent, ServiceRedeemedEvent, ServiceExpiredEvent, 
  ServiceReassignedEvent. Listeners handle tasks/emails.
- **Policies**: TaskPolicy declares which tasks to assign/close for each event.
  EligibilityPolicy determines whether a candidate can access a provider's service.
- **Scheduler**: ResourceExpiryScheduler marks expired resources and emits ServiceExpiredEvent.
- **Controllers**: ServicesAdminController (admin operations) and ServicesPortalController 
  (candidate-facing operations) both route requests via CandidateServiceRegistry. No new 
  controller code is needed for new providers.
- **CandidateAssistanceService**: the main interface that all candidate assistance services must 
  implement.
- **AbstractCandidateAssistanceService**: base class providing common functionality. Extend it and 
  override `provider()`, `serviceCode()`, `allocator()`, and optionally `importer()`.

### Add a new backend provider (step-by-step)

Below: example for Udemy Courses.

#### 1) Add enum values

Add a new `ServiceProvider` in `domain/model/ServiceProvider.java` (if the provider is new):

```java
public enum ServiceProvider {
  DUOLINGO,
  LINKEDIN,
  REFERENCE,
  UDEMY      // <-- new
}
```

Add a new `ServiceCode` in `domain/model/ServiceCode.java` (if the service type is new):

```java
public enum ServiceCode {
  TEST_PROCTORED,
  TEST_NON_PROCTORED,
  PREMIUM_MEMBERSHIP,
  VOUCHER,
  COURSE     // <-- new
}
```

A new assistance service could be for an existing provider with a new service code (e.g. a second
Duolingo service) — in that case only the ServiceCode enum needs updating.

#### 2) Create a provider package

```text
application/providers/udemy/
├── UdemyService.java
└── UdemyCourseImporter.java   // if inventory-backed
```

#### 3) Register an allocator bean

Inventory-backed (recommended if you will import a number of resources upfront and assign them over
time):

In `application/config/AllocatorsConfig.java`:

```java
@Bean("udemyCourseAllocator")
public ResourceAllocator udemyCourseAllocator(ServiceResourceRepository repo) {
  return new InventoryAllocator(repo, ServiceProvider.UDEMY, ServiceCode.COURSE);
}
```

If you need a custom allocation strategy, create a new class implementing `ResourceAllocator` in 
`core/allocators/`.

#### 4) Create an importer (optional)

If resources come from a CSV file, create an importer in your provider package that implements 
`FileInventoryImporter`. See `ReferenceVoucherImporter` or `DuolingoCouponImporter` for examples.

#### 5) Create the concrete service

Create `UdemyService.java` in `application/providers/udemy/` that extends `AbstractCandidateAssistanceService`
and override provider(), serviceCode(), allocator(), and optionally importer().

```java
@Service
public class UdemyService extends AbstractCandidateAssistanceService {
  private final ResourceAllocator udemyAllocator;
  private final FileInventoryImporter udemyImporter;

  public UdemyService(ServiceAssignmentRepository aRepo,
                      ServiceResourceRepository rRepo,
                      AssignmentEngine engine,
                      SavedListService lists,
                      @Qualifier("udemyCourseImporter") FileInventoryImporter importer,
                      @Qualifier("udemyCourseAllocator") ResourceAllocator allocator) {
    super(aRepo, rRepo, engine, lists);
    this.udemyImporter = importer;
    this.udemyAllocator = allocator;
  }

  @Override protected ServiceProvider provider()          { return ServiceProvider.UDEMY; }
  @Override protected ServiceCode serviceCode()           { return ServiceCode.COURSE; }
  @Override protected ResourceAllocator allocator()       { return udemyAllocator; }
  @Override protected FileInventoryImporter importer()    { return udemyImporter; }
}
```

#### 6) Create a task policy (optional)

**This step is optional.** If your provider has no task lifecycle — for example, a directory or
link service where no tasks are assigned to candidates on any event — you can skip it entirely.
The `TaskPolicyRegistry` automatically returns a no-op fallback for unregistered providers.

Only create a `TaskPolicy` `@Component` if your provider needs to trigger or close tasks in
response to assignment lifecycle events (`assigned`, `redeemed`, `reassigned`, `expired`).

If a task policy is required, create `UdemyTaskPolicy.java` in `application/policy/` that
implements `TaskPolicy` and define tasks to assign/close for each event.

```java
@Component
public class UdemyTaskPolicy implements TaskPolicy {
  @Override public ServiceProvider provider() { return ServiceProvider.UDEMY; }

  @Override
  public List<String> tasksOnAssigned(ServiceAssignedEvent e) {
    return List.of("udemyCourseAssigned");  // tasks that initiate on assignment
  }

  @Override
  public List<String> tasksOnRedeemed(ServiceRedeemedEvent e) {
    return List.of("udemyCourseInProgress"); // tasks that should be assigned on redemption
  }

  @Override
  public List<String> tasksOnReassigned(ServiceReassignedEvent e) {
    return List.of("udemyCourseAssigned", "udemyCourseInProgress"); // tasks that should be closed on reassignment
  }
  
}
```

### Service-code branching inside a TaskPolicy

Task policies are registered **per provider**, not per provider+serviceCode. If a single provider 
offers multiple service codes with different task workflows, branch on the service code inside the 
policy methods. Every event carries the full `ServiceAssignment`, so `serviceCode` is always 
available:

```java
@Component
public class DuolingoTaskPolicy implements TaskPolicy {
  @Override public ServiceProvider provider() { return ServiceProvider.DUOLINGO; }

  @Override
  public List<String> tasksOnAssigned(ServiceAssignedEvent e) {
    return switch (e.assignment().getServiceCode()) {
      case TEST_PROCTORED     -> List.of("assignProctoredCoupon");
      case TEST_NON_PROCTORED -> List.of("assignNonProctoredCoupon");
    };
  }
  
  // . . .
}
```
#### 7) Create an eligibility policy (optional)

If your provider has eligibility criteria, implement `EligibilityPolicy`:

```java
@Component
public class UdemyEligibilityPolicy implements EligibilityPolicy {
  @Override public ServiceProvider provider() { return ServiceProvider.UDEMY; }

  @Override
  public boolean isEligible(Long candidateId) {
    // custom logic here
    return true;
  }
}
```

Providers without an explicit `EligibilityPolicy` bean default to eligible for all candidates.

#### 8) Email listener (optional)

To send emails on events, add a branch for your provider in the `onAssigned` method of 
`EmailNotificationListener.java` in `core/listeners/`.

#### 9) Controllers — zero new code

No new controller code is needed. Both controllers auto-discover your provider via 
`CandidateServiceRegistry`:

**Admin endpoints** (`ServicesAdminController` — `/api/admin/services`):

| Method | Path | Description |
|--------|------|-------------|
| POST | `/{provider}/{serviceCode}/import` | Import inventory from CSV |
| POST | `/{provider}/{serviceCode}/assign/candidate/{candidateId}` | Assign to one candidate |
| POST | `/{provider}/{serviceCode}/assign/list/{listId}` | Assign to all candidates in a list |
| GET | `/{provider}/{serviceCode}/available/count` | Count available resources |
| GET | `/{provider}/{serviceCode}/available` | List available resources |
| GET | `/{provider}/{serviceCode}/resources/candidate/{candidateId}` | Resources for a candidate |
| PUT | `/{provider}/{serviceCode}/resources/status` | Update resource status |
| GET | `/assignments/candidate/{candidateId}` | All assignments for a candidate |

**Candidate-portal endpoints** (`ServicesPortalController` — `/api/portal/services`):

| Method | Path | Description |
|--------|------|-------------|
| GET | `/{provider}/{serviceCode}/eligibility` | Check candidate eligibility |
| GET | `/{provider}/{serviceCode}/assignment` | Get current assignment |
| POST | `/{provider}/{serviceCode}/assign` | Self-assign (candidate claims) |
| PUT | `/{provider}/{serviceCode}/resources/status` | Update resource status |

The portal controller derives `candidateId` from the authenticated session — candidates can only 
access their own data.

### Assignment lifecycle

#### Assign

1. Controller calls `CandidateService.assignToCandidate(...)`.
2. `AbstractCandidateAssistanceService` guards (no duplicate active assignment) then delegates to 
   `AssignmentEngine`.
3. `AssignmentEngine` calls `ResourceAllocator.allocateFor(candidate)`:
   - For `InventoryAllocator`: `SELECT ... FOR UPDATE SKIP LOCKED` to grab next AVAILABLE.
4. Engine sets resource ASSIGNED, saves, appends `ServiceAssignmentEntity`, and publishes 
   `ServiceAssignedEvent`.
5. `ServiceTaskOrchestrator` listens and assigns tasks per `TaskPolicy`.
6. `EmailNotificationListener` (if present) sends emails.

#### Redeem

1. Candidate (or admin) calls `updateResourceStatus(resourceCode, REDEEMED)`.
2. Service validates the transition and updates the resource.
3. `ServiceRedeemedEvent` is published.
4. `ServiceTaskOrchestrator` and `EmailNotificationListener` handle the event.

#### Reassign

1. Admin calls `CandidateService.reassign(...)`.
2. `AssignmentEngine` marks old assignment REASSIGNED, old resource DISABLED, appends new 
   assignment, sets new resource ASSIGNED, saves, and publishes `ServiceReassignedEvent`.
3. `ServiceTaskOrchestrator` and `EmailNotificationListener` handle the event.

#### Expire

1. `ResourceExpiryScheduler` runs periodically, marks expired resources, and publishes 
   `ServiceExpiredEvent`.
2. `ServiceTaskOrchestrator` and `EmailNotificationListener` handle the event.

---

## Frontend

### Admin Portal

All CASI admin-portal code lives under `ui/admin-portal/src/app/components/casi-management/`.

#### Layout

```text
casi-management/
├── base-csv-import.component.ts         // abstract base class for CSV import components
├── casi-management.component.*          // tabbed page component (Duolingo, LinkedIn, etc.)
├── csv-preview/                         // reusable CSV table + pagination component
│   └── csv-preview.component.*
├── import-duolingo-coupons/             // Duolingo CSV import tab
│   └── import-duolingo-coupons.component.*
├── import-linkedin-premium-coupons/     // LinkedIn CSV import tab
│   └── import-linkedin-premium-coupons.component.*
├── import-reference-vouchers/           // Reference voucher import tab (local dev only)
│   └── import-reference-vouchers.component.*
└── offer-to-assist/                     // Offers To Assist search/list tab
    └── offer-to-assist.component.*
```

#### Routing and navigation

The CASI management page is routed at `/candidate-services` in `app-routing.module.ts`, guarded by
`RoleGuardService` with `expectedRoles: [Role.systemadmin]` and `DpaGuard`.

The menu item appears in the user dropdown in `header.component.html`, visible only to system 
admins via `*ngIf="isSystemAdminOnly()"`.

#### Generic admin service: `CasiAdminService`

`ui/admin-portal/src/app/services/casi-admin.service.ts` provides a generic HTTP service for all
admin CASI operations. It is parameterized by `provider` and `serviceCode` strings:

| Method | Backend endpoint |
|--------|-----------------|
| `importInventory(provider, serviceCode, file)` | `POST /{provider}/{serviceCode}/import` |
| `countAvailable(provider, serviceCode)` | `GET /{provider}/{serviceCode}/available/count` |
| `assignToCandidate(provider, serviceCode, candidateId)` | `POST /{provider}/{serviceCode}/assign/candidate/{id}` |
| `assignToList(provider, serviceCode, listId)` | `POST /{provider}/{serviceCode}/assign/list/{id}` |

New providers should use `CasiAdminService` directly rather than creating provider-specific HTTP 
services.

#### Shared CSV import infrastructure

CSV import components share logic via `BaseCsvImportComponent` (abstract class) and 
`CsvPreviewComponent` (reusable table/pagination component).

**`BaseCsvImportComponent`** provides:
- Shared state: `error`, `working`, `csvHeaders`, `csvData`, `paginatedData`, `selectedFile`, 
  `csvImported`, `currentPage`, `pageSize`.
- Shared methods: `onFileChange()`, `parseCSV()` (with required-column validation), `importCSV()` 
  (with no-file guard), `onPageChange()`, `updatePaginatedData()`.
- Override hooks:
  - `requiredColumns: string[]` — columns the CSV must contain.
  - `doImport(): void` — call your service to upload the file.
  - `loadAvailableCount(): void` — fetch the current available count.
  - `filterColumns(headers, data)` — optionally remove columns from preview (Duolingo uses this).

**`CsvPreviewComponent`** accepts `csvHeaders`, `csvData`, `paginatedData`, `working`, 
`currentPage`, `pageSize` as `@Input()` and emits `@Output() pageChange`. Use it in your template:

```html
<app-csv-preview
  [csvHeaders]="csvHeaders"
  [csvData]="csvData"
  [paginatedData]="paginatedData"
  [working]="working"
  [currentPage]="currentPage"
  [pageSize]="pageSize"
  (pageChange)="onPageChange($event)"
></app-csv-preview>
```

#### Add a new admin import tab (step-by-step)

1. Create a component directory under `casi-management/`:
   ```text
   casi-management/import-udemy-courses/
   ├── import-udemy-courses.component.ts
   ├── import-udemy-courses.component.html
   ├── import-udemy-courses.component.scss
   └── import-udemy-courses.component.spec.ts
   ```

2. Extend `BaseCsvImportComponent`:
   ```typescript
   import {Component, Input} from '@angular/core';
   import {User} from "../../../model/user";
   import {CasiAdminService} from "../../../services/casi-admin.service";
   import {BaseCsvImportComponent} from "../base-csv-import.component";

   @Component({
     selector: 'app-import-udemy-courses',
     templateUrl: './import-udemy-courses.component.html',
     styleUrls: ['./import-udemy-courses.component.scss'],
   })
   export class ImportUdemyCoursesComponent extends BaseCsvImportComponent {
     availableCount = 0;
     requiredColumns = ['course_code', 'expires_at'];
     @Input() loggedInUser!: User;

     private readonly provider = 'UDEMY';
     private readonly serviceCode = 'COURSE';

     constructor(private casiAdminService: CasiAdminService) { super(); }

     protected doImport(): void {
       if (!this.selectedFile) { this.error = 'Please select a file.'; this.working = false; return; }
       this.casiAdminService.importInventory(this.provider, this.serviceCode, this.selectedFile).subscribe({
         next: () => { this.working = false; this.loadAvailableCount(); this.csvImported = true; },
         error: (err) => { this.working = false; this.error = 'Import failed.'; },
       });
     }

     protected loadAvailableCount(): void {
       this.casiAdminService.countAvailable(this.provider, this.serviceCode).subscribe(
         (r) => this.availableCount = r.count
       );
     }
   }
   ```

3. Template: use `<app-csv-preview>` for the table. See existing components for the pattern.

4. Declare the component in `app.module.ts`.

5. Add a `<tc-tab>` in `casi-management.component.html`:
   ```html
   <tc-tab id="udemy-courses">
     <tc-tab-header>Udemy Courses</tc-tab-header>
     <tc-tab-content>
       <app-import-udemy-courses [loggedInUser]="loggedInUser"></app-import-udemy-courses>
     </tc-tab-content>
   </tc-tab>
   ```

### Candidate Portal

Candidate-facing CASI components live under 
`ui/candidate-portal/src/app/components/profile/view/tab/services/`.

#### Layout

```text
services/
├── services.component.*             // service card grid + tab router
├── duolingo/                        // Duolingo coupon claim/redeem
│   └── duolingo-coupon/             // sub-component for individual coupon display
├── linkedin/                        // LinkedIn premium claim/redeem
│   └── linkedin-redeemed/           // sub-component for redeemed state
└── reference/                       // Reference voucher (dev-only)
```

#### Generic portal service: `CasiPortalService`

`ui/candidate-portal/src/app/services/casi-portal.service.ts` provides a generic HTTP service for 
all candidate-facing CASI operations:

| Method | Backend endpoint |
|--------|-----------------|
| `checkEligibility(provider, serviceCode)` | `GET /{provider}/{serviceCode}/eligibility` |
| `getAssignment(provider, serviceCode)` | `GET /{provider}/{serviceCode}/assignment` |
| `assign(provider, serviceCode)` | `POST /{provider}/{serviceCode}/assign` |
| `updateResourceStatus(provider, serviceCode, request)` | `PUT /{provider}/{serviceCode}/resources/status` |

New providers should use `CasiPortalService` directly.

#### Frontend model: `services.ts`

`ui/candidate-portal/src/app/model/services.ts` contains the TypeScript enums and interfaces that 
mirror the backend domain model:

- `ServiceProvider` (DUOLINGO, LINKEDIN, REFERENCE)
- `ServiceCode` (TEST_PROCTORED, TEST_NON_PROCTORED, PREMIUM_MEMBERSHIP, VOUCHER)
- `ResourceStatus`, `AssignmentStatus`
- `ServiceAssignment`, `ServiceResource` interfaces

When adding a new provider, add the enum values here to match the backend.

#### Add a new candidate-portal service component (step-by-step)

1. Add your `ServiceProvider` and `ServiceCode` values to `model/services.ts`.

2. Create a component under `services/`:
   ```text
   services/udemy/
   ├── udemy.component.ts
   ├── udemy.component.html
   ├── udemy.component.scss
   └── udemy.component.spec.ts
   ```

3. Use `CasiPortalService` for all backend calls (eligibility, assignment, status updates).

4. Wire the component into `services.component.html`:
   - Add an `<app-udemy>` entry in the component rendering section.
   - Add a service card in the card grid.

5. In the parent `view-candidate.component.ts`, check eligibility using `CasiPortalService` and
   pass the result as an observable to `ServicesComponent`.

6. Declare the component in the candidate portal `app.module.ts`.

---

## Reference Provider

The `REFERENCE::VOUCHER` provider is a minimal end-to-end test harness for the CASI framework. It 
demonstrates the full lifecycle (import, assign, reassign, ledger/events, task/email hooks) with 
zero external dependencies.

The REFERENCE provider uses an `EligibilityPolicy` bean (`ReferenceEligibilityPolicy` in 
`application/policy/`) as the reference implementation for service eligibility verification.

- **Backend**: `application/providers/reference/` (ReferenceService + ReferenceVoucherImporter)
- **Admin UI**: `casi-management/import-reference-vouchers/` (CSV import tab, local-dev-only)
- **Candidate UI**: `services/reference/` (voucher claim/redeem)

The Reference Vouchers admin tab is only visible when `environment.environmentName === 'local'` 
(i.e. localhost dev builds). It is hidden in staging and production.

An example CSV for testing is at:
`ui/admin-portal/src/app/components/casi-management/import-reference-vouchers/reference-vouchers-example.csv`

```csv
voucher_code,expires_at
REF-TEST-0001,2026-12-31
REF-TEST-0002,31/01/2027
REF-TEST-0003,02/15/2027
```
