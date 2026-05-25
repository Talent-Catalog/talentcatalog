# IDP (OAuth2 Identity Providers) — Developer Guide

(This document is modelled on the CASI README.md.)

This document provides an overview how we use OAuth2 Identity Providers. 
It documents the high-level IDP abstraction we use to access our supported providers.

---

## Providers

We use the following OAuth2 Identity Providers:

- Keycloak – only used on developer's local machines inside our standard supported docker image
- AWS Cognito – used for our AWS cloud deployments: on staging and production


### Layout

The IDP module is organized into several key packages:

```text
todo Complete this
casi/
├─ api/                              // inbound HTTP
│  ├─ dto/                           // API response models
│  ├─ request/                       // request DTOs
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

We use IDP to manage logins and registrations of all users.

In the Amazon AWS cloud we use Amazon's IDP provider, Cognito, for user authentication.

On developers' local machines we use Keycloak to replicate the Cognito IDP on a user's
local machine for convenient development and testing.

User authorization and user data is stored in our standard Postgres database.
The IDP provider contains the minimal information needed to authenticate users and issue tokens.
Those tokens contain enough data to allow the application to link an authenticated user to their 
full profile in our database, including their role(s) and permissions.
                                                                          
### Data held in IDP

tbc
Realms, groups, Terminology (Keycloak vs Cognito), etc.

### Data passed in tokens

tbc

### Manual Login flow
tbc
### Manual Registration flow
tbc
### Automatic Registration flow
tbc           
### API flow
tbc
