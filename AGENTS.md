# AGENTS.md

This file provides instructions for AI coding agents working on this repository.

## Project Overview

Talent Catalog is a Spring Boot backend application providing APIs for managing
candidate skills, opportunities, and partner organizations.

Key technologies:

- Java
- Spring Boot
- Spring Data JPA / Hibernate
- PostgreSQL
- Flyway (database migrations)
- Gradle
 - Redis (caching)
 - Keycloak (auth during local development)
 - Elasticsearch (search infra and index scripts under infra/elasticsearch)

The backend is a Spring Boot application in the same repository under the `server` directory.

The frontend is an Angular application in the same repository under the `ui` directory. 

### Key files & locations

- `server/build.gradle` — server-specific Gradle settings and dependencies.
- `build.gradle` (root) and `settings.gradle` — multiproject configuration.
- `server/src/main/resources/application.yml` — main runtime configuration.
- `server/src/main/resources/db/migration` — Flyway migrations.
- `docker-compose/docker-compose.yml` — local infra (Postgres, Keycloak, Redis).
- `infra/elasticsearch` — index scripts and ES utilities.
- `ui/` — Angular frontends (admin-portal, candidate-portal, public-portal).

## Build

Build the project using Gradle (use the wrapper: `./gradlew`).

See the root `build.gradle` and `server/build.gradle` files for details.

Common Gradle commands (zsh):

```bash
# build everything
./gradlew build

# build server only
./gradlew :server:build

# run server in development
./gradlew :server:bootRun

# run server unit tests
./gradlew :server:test --info
```

## Run the application

The application requires a PostgreSQL database.

Configuration is provided via `server/src/main/resources/application.yml`.

For local development you can also use the provided Docker Compose stack at `docker-compose/docker-compose.yml` which brings up Postgres, Keycloak and Redis.

Quickstart (local development)

- Start local infra (from repo root):

```bash
docker compose -f docker-compose/docker-compose.yml up -d
docker compose -f docker-compose/docker-compose.yml ps
```

- Build and run the server:

```bash
./gradlew :server:build
./gradlew :server:bootRun
```

- Run server tests:

```bash
./gradlew :server:test
```

Ports exposed by the compose stack (defaults)

- Postgres: 5432
- Keycloak (container web): 8082 -> host 8082
- Redis: 6379

## Tests

Server tests are located in the `server` directory.

Frontend tests are located in the `ui` directory.

Do not remove existing tests unless they are clearly obsolete.

Troubleshooting / common pitfalls

- Lombok & Java: the project uses Java 17 via the Gradle toolchain and pins Lombok (see `server/build.gradle`). If you see Lombok-related compile errors, ensure you're building with the Gradle wrapper or a JDK 17 toolchain.
- Gradle wrapper: always prefer `./gradlew` to use the project's Gradle version and configured toolchain.
- Flyway migrations: never modify existing migration files. Add new migrations to `server/src/main/resources/db/migration` using Flyway filename conventions (e.g. `V1_370__describe.sql`).
- Elasticsearch: deployment/index scripts live in `infra/elasticsearch`. Verify ES version compatibility before running scripts.
- Port conflicts: local infra maps Postgres (5432), Keycloak (8082) and Redis (6379) to the host — adjust or stop local services if ports conflict.

## Database


Database migrations are managed using Flyway.

Migration files are located in:

server/src/main/resources/db/migration

Guidelines:

- Never modify an existing migration.
- Add a new migration for schema changes.
- Avoid destructive schema changes.

## Coding Conventions

General rules:

- Prefer constructor injection for Spring components.
- Avoid field injection.
- Keep service classes focused on business logic.
- Keep controllers thin.

Database access:

- Prefer DTO projections or native queries for read-heavy operations.
- Avoid unnecessary entity loading.
- Be aware of N+1 query problems.

## Refactoring Guidelines

When modifying code:

- Preserve existing behaviour.
- Update or add tests where appropriate.
- Avoid large cross-cutting refactors unless specifically requested.

If uncertain about design decisions, propose changes but do not implement them automatically.

## Pull Requests

Changes should be small and easy to review.

Include:

- description of change
- list of modified components
- tests added or updated

Avoid unrelated changes in the same pull request.

## Coding preferences
- Prefer simple, explicit, readable code over clever abstractions.
- Prioritize correctness, maintainability, and clear intent.
- Add comments explaining non-obvious code and important design decisions.
- Use Lombok for Java DTOs and simple value objects where appropriate.
- Prefer JUnit Jupiter for Java tests.
- Generate tests for new service logic, especially provider adapters and mapping code.
- Keep provider-specific code isolated behind interfaces.
- Avoid leaking Cognito, Keycloak, AWS, or OAuth implementation details into Angular code.
- Do not change existing authentication flows unless explicitly requested.
## Java / Spring Boot
- Use Java 17 style unless the project clearly uses a different version.
- Prefer constructor injection.
- Prefer small Spring services with clear responsibilities.
- Avoid naming custom interfaces/classes in ways that conflict with Spring Security types.
    - For example, avoid `AuthProvider`; prefer names like `IdpAdminService`.
- Keep security design explicit:
    - Spring Security configuration should handle coarse-grained access.
    - Controller/service annotations should handle fine-grained permissions.
- For OAuth/OIDC identity:
    - Treat `issuer + subject` as the stable login identity.
    - Do not rely on email as a permanent identity.
    - Store provider admin lookup details separately where needed, for example `idpUsername`.
## Angular
- Prefer standalone Angular components for new components unless existing module structure requires otherwise.
- Use Reactive Forms only.
- Do not introduce Template Forms.
- Prefer simple, explicit RxJS.
- Clean up subscriptions using `takeUntilDestroyed` or an equivalent project-approved pattern.
- Keep UI components focused and reusable.
## Tests
- Use JUnit Jupiter for Java tests.
- For Angular tests, use Angular testing utilities.
- Add unit tests for:
    - mapping logic
    - provider adapters
    - error handling
    - custom form controls
    - codecs/parsers
- Prefer tests that document expected behaviour over tests that mirror implementation details.
## Diagrams and documentation
- Use Mermaid for diagrams.
- Output Mermaid as code only; do not render diagrams.
- Keep architectural explanations practical and implementation-oriented.
