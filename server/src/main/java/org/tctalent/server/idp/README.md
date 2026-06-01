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

Each Talent Catalog instance, the TBB and GRN instances, has its own Cognito IDP.

Potentially, in future, a TBB user may log in to a GRN instance, and vice versa.
The globally unique public id of each user is stored in the IDP. This can be used to identify
that the same user is in fact logged on to both instances.

On developers' local machines we use Keycloak to replicate the Cognito IDP on a user's
local machine for convenient development and testing.

User authorization and user data is stored in our standard Postgres database.
The IDP provider contains the minimal information needed to authenticate users and issue tokens.
Those tokens contain enough data to allow the application to link an authenticated user to their 
full profile in our database, including their role(s) and permissions.
                                                                          
### IDP Configuration
                                                                         
TBB and GRN instances of the Talent Catalog have separate IDP providers. 

Each IDP instance is configured as follows:
- username and email are always the same. email IS the username.
- a single "user pool" (or "realm" in Keycloak) called "talentcatalog" (same for both instances).
- a client called "candidate" for candidate-portal users (refugees)
- a client called "admin" for admin-portal users.
- clients for each service provider connecting through the API.

### Client assignment
tbc - based on url

### Data held in IDP

Each user on the IDP stores the following data:
- username (email)
- password
- first name
- last name
- TC assigned globally unique public id
- issuer - identifies the IDP instance (e.g. "tbb" or "grn")
- sub (subject) - IDP assigned unique identifier for the user
- email_verified - true if the user has verified their email address
- client (see above)


### Data passed in tokens

- iss – the IDP instance issuing the token (e.g. "tbb" or "grn").
- sub – the IDP assigned unique identifier for the user.
- clientId – in Keycloak this appears in the token as "azp". In Cognito this is "client_id".

### Manual Login flow
tbc
### Manual Registration flow
tbc
### Automatic Registration flow
tbc           
### API flow
tbc
### Using IDP Admin Console Manually
#### Keycloak
Login to the local Keycloak admin console at https://localhost:8082 using the username and password
set in the tcsecrets file.

In the admin console you can manually add users. Select the talencatalog realm and click on Users.
