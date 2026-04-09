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
- Maven

The backend is a Spring Boot application in the same repository under the `server` directory.

The frontend is an Angular application in the same repository under the `ui` directory. 

## Build

Build the project using Gradle.

See the `build.gradle` file for details.

## Run the application

The application requires a PostgreSQL database.

Configuration is provided via `application.yml`.

## Tests

Server tests are located in the `server` directory.

Frontend tests are located in the `ui` directory.

Do not remove existing tests unless they are clearly obsolete.

## Database

Database migrations are managed using Flyway.

Migration files are located in:

src/main/resources/db/migration

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
