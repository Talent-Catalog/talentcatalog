# Candidate Services — Developer Guide

This document provides an overview of the Candidate Services module in our application. It outlines 
the architecture, folder layout and fastest path for developers working with this module to add a 
new CandidateService (e.g. Udemy).

## Layout (what lives where)

The Candidate Services module is organized into several key packages:

```text
candidateservices
├─ api/                            // inbound HTTP
│  ├─ dto/                         // API response models
│  ├─ request/                     // request DTOs
│  └─ ServicesAdminController      // admin endpoints
│
├─ application/                    // wiring & policies
│  ├─ config/                      // Spring @Configuration (allocators/providers)
│  ├─ policy/                      // Task policy per provider
│  └─ providers/                   // concrete provider services
│     └─ duolingo/
│
├─ core/                           // application services
│  ├─ allocators/                  // inventory allocation strategies
│  ├─ importers/                   // CSV/API importers
│  ├─ listeners/                   // event listeners (email, tasks)
│  ├─ scheduler/                   // expiry cron
│  └─ services/                    // AssignmentEngine + base/port
│     ├─ AbstractCandidateService  // base
│     ├─ AssignmentEngine          
│     ├─ CandidateService          // interface (port)
│     └─ CandidateServiceRegistry  // service locator
|
├─ domain/                         // domain objects
│  ├─ events/                      // ServiceAssigned/Redeemed/Expired/Reassigned
│  ├─ mappers/                     // entity <--> domain --> DTO
│  ├─ model/                       // enums + value objects
│  └─ persistence/                 // JPA entities + repos (ledger + inventory)
```

## Core Concepts

The Candidate Services module is built around several core concepts:

- **ServiceResourceEntity**: generic inventory (provider, serviceCode, resourceCode, status, 
  candidate, assignedAt, expiresAt).
- **ServiceAssignmentEntity**: append-only ledger of assignments (provider, serviceCode, resourceId, 
  candidate, actor, status, assignedAt). 
- **AssignmentEngine**: orchestrates an assignment/reassignment (uses ResourceAllocator, saves 
  ledger, publishes events).
- **ResourceAllocator**: strategy for “how do I get a unit?” (e.g., InventoryAllocator; or -- todo 
  -- a simple URL allocator or API-backed allocator).
- **Events**: ServiceAssignedEvent, ServiceRedeemedEvent, ServiceExpiredEvent. Listeners do 
  tasks/emails
- **Policies**: TaskPolicy declares which tasks to assign/close for each event
- **Scheduler**: ResourceExpiryScheduler marks expired resources and emits ServiceExpiredEvent.
- **Controller**: ServicesAdminController calls a service via CandidateServiceRegistry.
- **CandidateService**: The main interface that all candidate services must implement. It defines 
  methods for assigning, redeeming, and expiring services.
- **AbstractCandidateService**: A base class that provides common functionality for all candidate 
  services. It implements the CandidateService interface and can be extended by specific service 
  implementations (e.g., DuolingoCandidateService).


## Add a new provider (step-by-step)

To add a new candidate service provider, follow these steps:

Below: example for Udemy Courses

0) Add a new enum to `ServiceProvider` in `domain/model/ServiceProvider.java`:

```java
public enum ServiceProvider {
  DUOLINGO,
  // ...
  UDEMY  // <-- new
}
```

1) Add a new enum to `ServiceCode` in `domain/model/ServiceCode.java`:

```java
public enum ServiceCode {
  DUOLINGO_TEST_PROCTORED,
  // ...
  UDEMY_COURSE  // <-- new
}
```

1) Create a new package for the provider under `application/providers/`:

```text
candidateservices
└─ application/
   └─ providers/
      └─ udemy/
         ├─ UdemyCandidateService.java
         └─ UdemyCouponImporter.java // if needed
```

2) Allocator (choose one):

Inventory-backed (recommended if you will import a number of resources upfront and assign them over 
time):

```java
// application/config/AllocatorsConfig.java
@Bean("udemyCourseAllocator")
public ResourceAllocator udemyCourseAllocator(ServiceResourceRepository repo) {
  return new InventoryAllocator(repo, ServiceProvider.UDEMY, ServiceCode.UDEMY_COURSE);
}
```

If you need a custom allocation strategy, create a new class in `core/allocators/`.

3) Importer (optional):

If you get resources from a CSV or other format, create a new parser/importer in 
`application/providers/udemy` (e.g., `UdemyCouponImporter.java`) that implements an interface in
`core/importers/`.

4) Concrete service:

Create `UdemyCandidateService.java` in `application/providers/udemy/` that extends `AbstractCandidateService` 
and override provider(), serviceCode(), allocator(), and optionally importer().

```java
// application/providers/udemy/UdemyService.java
@Service
public class UdemyService extends AbstractCandidateService {
  private final ResourceAllocator udemyAllocator;
  private final FileInventoryImporter udemyImporter; // if needed

  public UdemyService(ServiceAssignmentRepository aRepo,
                      ServiceResourceRepository rRepo,
                      AssignmentEngine engine,
                      SavedListService lists,
                      @Qualifier("udemyCouponImporter") FileInventoryImporter importer,
                      @Qualifier("udemyCourseAllocator") ResourceAllocator allocator) {
    super(aRepo, rRepo, engine, lists);
    this.udemyImporter = importer;
    this.udemyAllocator = allocator;
  }

  @Override protected String provider() { return "UDEMY"; }
  @Override protected ServiceCode serviceCode() { return ServiceCode.UDEMY_COURSE; }
  @Override protected ResourceAllocator allocator() { return udemyAllocator; }
  @Override protected FileInventoryImporter importer() { return udemyImporter; } // if needed
}
```

4) Task Policy:

Create `UdemyTaskPolicy.java` in `application/policy/` that implements `TaskPolicy` and define
tasks to assign/close for each event.

```java
@Component
public class UdemyTaskPolicy implements TaskPolicy {
  @Override public String provider() { return "UDEMY"; }

  @Override
  public List<String> tasksOnAssigned(ServiceAssignedEvent e) {
    return List.of("udemyVoucherClaim"); // tasks that initiate on assignment
  }
  
  @Override
  public List<String> tasksOnRedeemed(ServiceRedeemedEvent e) {
    return List.of("udemyCourseInProgress"); // tasks that should be assigned on redemption
  }

  @Override
  public List<String> tasksOnReassigned(ServiceReassignedEvent e) {
    return List.of("udemyCourseInProgress"); // tasks that should be closed on reassignment
  }

}
```

5) Email listener (optional):

If you want to send emails on events, update the onAssigned method on `EmailNotificationListener.java` 
in `core/listeners/`.

6) Controller - zero new code:

No new code is needed here. The existing `ServicesAdminController` can route requests to the new
service via the `CandidateServiceRegistry`.

The generic admin controller already supports your provider/service endpoints:

- POST /api/admin/services/UDEMY/UDEMY_COURSE/import 
- POST /api/admin/services/UDEMY/UDEMY_COURSE/assign/candidate/{candidateId} 
- POST /api/admin/services/UDEMY/UDEMY_COURSE/assign/list/{listId} 
- GET /api/admin/services/UDEMY/UDEMY_COURSE/available/count 
- etc.

## Assignment life cycle (what happens on assign/redeem/reassign/expire):

### 1) Assign:

1. Controller calls CandidateService.assignToCandidate(...). 
2. AbstractCandidateService guards (no duplicate active assignment) then delegates to 
   AssignmentEngine. 
3. AssignmentEngine calls ResourceAllocator.allocateFor(candidate):
   - For inventory allocators: SELECT ... FOR UPDATE SKIP LOCKED to grab next AVAILABLE.
4. Engine sets resource ASSIGNED, saves, appends ServiceAssignmentEntity, and publishes 
   ServiceAssignedEvent. 
5. ServiceTaskOrchestrator listens and assigns tasks per TaskPolicy. 
6. EmailNotificationListener (if present) sends emails.

### 2) Redeem: 

// todo

### 3) Reassign:
1. SystemAdminApi calls CandidateService.reassign(...). // todo: add admin endpoint
2. AbstractCandidateService delegates to AssignmentEngine.
3. AssignmentEngine marks old assignment REASSIGNED, old resource DISABLED, appends new assignment, 
   sets new resource ASSIGNED, saves, and publishes ServiceReassignedEvent.
4. ServiceTaskOrchestrator listens and updates/assigns tasks per TaskPolicy.
5. EmailNotificationListener (if present) sends emails.

### 4) Expire:

1. ResourceExpiryScheduler runs periodically, marks EXPIRED resources, and publishes 
   ServiceExpiredEvent.
2. ServiceTaskOrchestrator listens and handles expiration per TaskPolicy.
3. EmailNotificationListener (if present) sends emails.
