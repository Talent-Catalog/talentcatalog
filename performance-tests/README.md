# Performance Tests Documentation

## 1. Overview

The `performance-tests` module is a Gatling-based performance test suite for Talent Catalog workflows. It now covers **both HTTP-based** and **DB-based** performance testing, with a clear Java package structure for configuration, requests, scenarios, simulations, payloads, and database helpers.

At a high level, the suite is intended to:

- protect critical workflows from regressions
- compare old vs new implementations where A/B coverage exists
- separate end-to-end HTTP behavior from raw database/query behavior
- support local execution, Docker execution, and CI orchestration
- generate both human-readable and machine-readable performance artifacts

This updated documentation supersedes the older strategy doc that focused mainly on Candidate Search HTTP coverage. The current structure shows the suite has expanded to include:

- **HTTP candidate search** workloads
- **HTTP saved-list paged search** workloads
- **DB candidate search** workloads
- **health-check simulations**
- **CI utilities** for sharding, summarization, and threshold gating

---

## 2. Technology Stack

The current module is built around:

- **Java 17**
- **Gatling 3.10.5** with the Gradle plugin `io.gatling.gradle`
- **Typesafe Config** for runtime configuration
- **PostgreSQL JDBC driver** for DB-based test flows
- **HikariCP** for database connection pooling
- **Python 3** helper scripts for report summarization and threshold checks
- **Docker** for reproducible CI/local execution

The Gradle build defines a dedicated Gatling source set at:

- `src/gatling/java`
- `src/gatling/resources`

and disables old Scala Gatling compilation as a safety net.

---

## 3. Current Repository Layout

The current repository structure is best understood as follows:

```text
performance-tests/
├── ci/
│   ├── run-sim-list.sh
│   ├── summarize_gatling.py
│   └── performance_threshold_check.py
├── simulations/
│   └── (simulation list / orchestration inputs used by CI or local wrappers)
├── src/
│   └── gatling/
│       ├── java/
│       │   └── org/talentcatalog/perf/
│       │       ├── chains/
│       │       │   └── AuthChains
│       │       ├── config/
│       │       │   ├── HttpProtocolFactory
│       │       │   ├── PerfConfig
│       │       │   └── PerfSettings
│       │       ├── db/
│       │       │   ├── repositories/
│       │       │   │   └── CandidateSearchRepository
│       │       │   ├── sql/
│       │       │   │   └── Sql
│       │       │   ├── Db
│       │       │   └── JdbcRequest
│       │       ├── payloads/
│       │       │   ├── CandidateSearchPayloads
│       │       │   └── SavedListSearchPagedPayloads
│       │       ├── requests/
│       │       │   ├── db/
│       │       │   │   └── CandidateSearchRequests
│       │       │   └── http/
│       │       │       ├── auth/
│       │       │       │   └── AuthRequests
│       │       │       ├── candidatesearch/
│       │       │       │   └── CandidateSearchRequests
│       │       │       └── savedlist/
│       │       │           └── SavedListCandidateSearchPagedRequests
│       │       ├── scenarios/
│       │       │   ├── db/
│       │       │   │   └── CandidateSearchScenario
│       │       │   └── http/
│       │       │       ├── candidatesearch/
│       │       │       │   ├── NewEndpointLoopScenario
│       │       │       │   ├── OldEndpointLoopScenario
│       │       │       │   ├── RandomABFiniteScenario
│       │       │       │   └── SequentialABScenario
│       │       │       └── savedlist/
│       │       │           ├── NewSearchPagedLoopScenario
│       │       │           ├── OldSearchPagedLoopScenario
│       │       │           ├── RandomABFiniteSearchPagedScenario
│       │       │           └── SequentialABSearchPagedScenario
│       │       ├── simulations/
│       │       │   ├── db/
│       │       │   │   └── candidatesearch/
│       │       │   │       └── CandidateSearchDbSimulation
│       │       │   └── http/
│       │       │       ├── candidatesearch/
│       │       │       │   ├── CandidateSearchBaseSimulation
│       │       │       │   ├── CandidateSearchParallelClosedSimulation
│       │       │       │   ├── CandidateSearchParallelOpenSimulation
│       │       │       │   ├── CandidateSearchRandomABFiniteSimulation
│       │       │       │   └── CandidateSearchSequentialABSimulation
│       │       │       ├── health/
│       │       │       │   └── HealthSimulation
│       │       │       └── savedlist/
│       │       │           ├── SavedListSearchPagedBaseSimulation
│       │       │           ├── SavedListSearchPagedParallelClosedSimulation
│       │       │           ├── SavedListSearchPagedParallelOpenSimulation
│       │       │           ├── SavedListSearchPagedRandomABFiniteSimulation
│       │       │           └── SavedListSearchPagedSequentialABSimulation
│       │       └── GatlingRunner
│       ├── resources/
│       │   ├── payloads/
│       │   │   ├── candidate_search_heavy.json
│       │   │   ├── candidate_search_light.json
│       │   │   └── saved_list_search_paged_light.json
│       │   ├── sql/
│       │   │   └── candidate_search_paged.sql
│       │   └── application.conf
│       └── scala/
│           └── (legacy / transitional code, if still present)
├── build.gradle.kts
├── Dockerfile
├── perf-strategy.md
```

### What this structure means

The suite is now split more clearly into two execution paths:

1. **HTTP path** for end-to-end API performance testing
2. **DB path** for direct database/query-oriented performance testing

That separation is one of the most important improvements over the older documentation.

---

## 4. Architecture and Test Flow

### 4.1 HTTP-based flow

The HTTP path follows the general shape:

```text
Simulation
  -> Scenario
    -> Auth chain / auth request
      -> HTTP request definition
        -> Shared HTTP protocol
          -> Target API
```

This path is appropriate when you want to measure:

- end-to-end API latency
- authentication overhead
- routing differences between old and new endpoints
- payload serialization/deserialization effects
- realistic user workflow behavior under load

### 4.2 DB-based flow

The DB path follows the general shape:

```text
Simulation
  -> Scenario
    -> DB request / repository
      -> JdbcRequest / Db / Sql helpers
        -> PostgreSQL
```

This path is appropriate when you want to isolate:

- SQL/query execution cost
- repository-level database access behavior
- pool configuration effects
- database bottlenecks without HTTP/auth overhead

### 4.3 Why both modes matter

Using both DB and HTTP performance tests gives you two views of the same system:

- **HTTP simulations** tell you how the workflow behaves for users and services.
- **DB simulations** tell you how the underlying data access behaves in isolation.

That makes troubleshooting much faster. For example:

- if HTTP performance degrades but DB performance does not, the regression is likely in the API/service layer, auth, or serialization
- if both degrade, the bottleneck may be in the query, schema, indexes, or database capacity

---

## 5. Package and Class Responsibilities

This section documents the current role of each visible package/class group.

### 5.1 `config/`

#### `PerfConfig`
Central configuration access point for the test suite. It is expected to load and resolve settings from `application.conf`, environment variables, and/or JVM properties.

#### `PerfSettings`
Typed settings holder for runtime values used across simulations and requests, such as base URL, user agent, and likely other shared execution settings.

#### `HttpProtocolFactory`
Factory for the shared Gatling HTTP protocol configuration. This is the right place for:

- base URL
- default headers
- content type / accept headers
- user-agent
- authentication header wiring from session state

All HTTP simulations should reuse the protocol produced here instead of building ad hoc protocol definitions.

### 5.2 `chains/`

#### `AuthChains`
Encapsulates authentication behavior as a reusable Gatling chain. This allows protected HTTP scenarios to authenticate once and then reuse session state across the rest of the scenario.

### 5.3 `db/`

#### `Db`
Shared database connectivity utility. This is the logical place for DataSource creation, pool lifecycle handling, and reusable DB execution support.

#### `JdbcRequest`
Wrapper or utility for timing/executing JDBC operations in a consistent way so DB scenarios can measure database behavior with the same conventions.

#### `sql/Sql`
SQL resource helper responsible for loading SQL from resource files and making it available to DB test components.

#### `repositories/CandidateSearchRepository`
Repository abstraction for candidate-search-related DB operations. This provides a cleaner boundary between scenario code and low-level JDBC execution.

### 5.4 `payloads/`

#### `CandidateSearchPayloads`
Provides JSON payload loading/resolution for candidate-search HTTP requests. The visible resource files show at least:

- `candidate_search_light.json`
- `candidate_search_heavy.json`

#### `SavedListSearchPagedPayloads`
Provides payload handling for saved-list paged search requests. The visible resources currently show:

- `saved_list_search_paged_light.json`

If a heavy saved-list payload exists in source but was not visible in the provided materials, it should be documented alongside the light payload.

### 5.5 `requests/`

#### `requests/http/auth/AuthRequests`
HTTP request definitions related to authentication.

#### `requests/http/candidatesearch/CandidateSearchRequests`
HTTP request definitions for candidate search. This is where the new/old endpoint request definitions are expected to live.

#### `requests/http/savedlist/SavedListCandidateSearchPagedRequests`
HTTP request definitions for saved-list paged candidate search.

#### `requests/db/CandidateSearchRequests`
DB-oriented candidate search request definitions. This package name matters, because there is also an HTTP class with the same simple class name.

### 5.6 `scenarios/`

Scenarios represent reusable user or system behavior patterns.

#### `scenarios/db/CandidateSearchScenario`
Scenario for DB-based candidate search execution. This should orchestrate request iteration and any DB-specific timing or grouping.

#### `scenarios/http/candidatesearch/`
- `NewEndpointLoopScenario` — repeatedly exercises the new HTTP candidate search path
- `OldEndpointLoopScenario` — repeatedly exercises the old/legacy HTTP candidate search path
- `RandomABFiniteScenario` — alternates between old and new search paths using a randomized A/B pattern
- `SequentialABScenario` — executes old/new or new/old in sequence for direct side-by-side comparison under the same simulated user workflow

#### `scenarios/http/savedlist/`
- `NewSearchPagedLoopScenario` — repeated loop for the new saved-list paged path
- `OldSearchPagedLoopScenario` — repeated loop for the old/legacy saved-list paged path
- `RandomABFiniteSearchPagedScenario` — random A/B traffic mix for saved-list paged search
- `SequentialABSearchPagedScenario` — deterministic sequential A/B comparison for saved-list paged search

### 5.7 `simulations/`

Simulations define workload models and are the main execution entry points for Gatling.

#### DB simulations
- `CandidateSearchDbSimulation` — DB-only candidate search performance test

#### HTTP candidate search simulations
- `CandidateSearchBaseSimulation` — shared base setup for candidate search HTTP simulations
- `CandidateSearchParallelClosedSimulation` — closed workload model with fixed concurrency
- `CandidateSearchParallelOpenSimulation` — open workload model with arrival-rate style injection
- `CandidateSearchRandomABFiniteSimulation` — finite-user randomized A/B workload
- `CandidateSearchSequentialABSimulation` — sequential A/B comparison workload

#### HTTP saved-list paged simulations
- `SavedListSearchPagedBaseSimulation` — shared base setup for saved-list paged simulations
- `SavedListSearchPagedParallelClosedSimulation` — closed concurrency model
- `SavedListSearchPagedParallelOpenSimulation` — open arrival-rate model
- `SavedListSearchPagedRandomABFiniteSimulation` — randomized A/B finite workload
- `SavedListSearchPagedSequentialABSimulation` — sequential A/B comparison workload

#### HTTP health simulation
- `HealthSimulation` — lightweight health or smoke-style HTTP simulation, useful for validating baseline availability and environment readiness before heavier runs

### 5.8 `GatlingRunner`

A dedicated JVM/IDE entry point used to launch simulations without relying exclusively on Gradle task configuration. This is useful for IntelliJ-driven development and quick local runs.

---

## 6. Configuration

The suite currently has two main configuration groups: **database** and **performance/http**.

### 6.1 Database configuration

Current documented config:

```hocon
db {
  url = "jdbc:postgresql://localhost:5432/tctalent"
  username = "tctalent"
  password = "tctalent"
  maximumPoolSize = 10
  connectionTimeoutMs = 5000
  statementTimeoutMs = 30000

  url = ${?DB_URL}
  username = ${?DB_USER}
  password = ${?DB_PASSWORD}
}
```

#### Meaning

- `url` — JDBC connection string
- `username` / `password` — database credentials
- `maximumPoolSize` — Hikari connection pool size
- `connectionTimeoutMs` — max wait for a connection from the pool
- `statementTimeoutMs` — max query/statement execution time before timeout

#### Override model

The config explicitly supports environment-variable overrides for:

- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`

This is the preferred way to inject environment-specific DB configuration in CI or shared environments.

### 6.2 HTTP/performance configuration

Current documented config:

```hocon
perf {
  # defaults
  baseUrl   = "http://localhost:8080"
  userAgent = "gatling-performance-tests"

  # override if env vars exist
  baseUrl   = ${?PERF_BASE_URL}
  userAgent = ${?PERF_USER_AGENT}
}
```

#### Meaning

- `baseUrl` — target service base URL for HTTP simulations
- `userAgent` — HTTP User-Agent used by Gatling requests

#### Override model

The config explicitly supports environment-variable overrides for:

- `PERF_BASE_URL`
- `PERF_USER_AGENT`

### 6.3 JVM system property forwarding

The Gradle `gatlingTest` task forwards **all JVM system properties** into the Gatling JVM. That means system-property overrides are also available for values resolved through your configuration layer, as long as the Java config code is written to consume them.

The same task also supports a dedicated report-root override through:

```bash
-Dgatling.reportRoot=/some/path
```

### 6.4 Recommended local environment setup

For local DB + HTTP testing:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/tctalent
export DB_USER=tctalent
export DB_PASSWORD=tctalent

export PERF_BASE_URL=http://localhost:8080
export PERF_USER_AGENT=gatling-performance-tests
```

---

## 7. Build and Runtime Behavior

### 7.1 Gradle build behavior

The module uses a custom `gatlingTest` task.

Key behaviors:

- source set = `src/gatling/java` and `src/gatling/resources`
- main entrypoint = `io.gatling.app.Gatling`
- simulation class is selected through `-PsimClass=...`
- default simulation class is:

```text
org.talentcatalog.perf.simulations.http.candidatesearch.CandidateSearchSequentialABSimulation
```

- report output root defaults to:

```text
build/reports/gatling
```

- report root can be overridden via:

```bash
-Dgatling.reportRoot=/tmp/tc-gatling-reports
```

### 7.2 Scala migration status

We still have a `src/gatling/scala/` area, but the Gradle build disables `compileGatlingScala` if such a task exists. That strongly suggests the current direction is:

- keep Gatling development in Java DSL
- preserve Scala only as legacy/transitional code
- avoid reintroducing Scala as the main path for new tests

---

## 8. Running Simulations

### 8.1 Run through Gradle

Generic pattern:

```bash
./gradlew :performance-tests:gatlingTest \
  -PsimClass=<fully.qualified.SimulationClass>
```

### 8.2 Example: HTTP candidate search sequential A/B

```bash
./gradlew :performance-tests:gatlingTest \
  -PsimClass=org.talentcatalog.perf.simulations.http.candidatesearch.CandidateSearchSequentialABSimulation
```

### 8.3 Example: HTTP candidate search parallel open

```bash
./gradlew :performance-tests:gatlingTest \
  -PsimClass=org.talentcatalog.perf.simulations.http.candidatesearch.CandidateSearchParallelOpenSimulation
```

### 8.4 Example: HTTP saved-list paged sequential A/B

```bash
./gradlew :performance-tests:gatlingTest \
  -PsimClass=org.talentcatalog.perf.simulations.http.savedlist.SavedListSearchPagedSequentialABSimulation
```

### 8.5 Example: DB candidate search simulation

```bash
./gradlew :performance-tests:gatlingTest \
  -PsimClass=org.talentcatalog.perf.simulations.db.candidatesearch.CandidateSearchDbSimulation
```

### 8.6 Example: health simulation

```bash
./gradlew :performance-tests:gatlingTest \
  -PsimClass=org.talentcatalog.perf.simulations.http.health.HealthSimulation
```

### 8.7 Simulation-specific parameters

Your current class naming strongly suggests that workload-specific JVM properties still exist for things such as:

- number of users
- concurrency
- arrival rate
- repeat count
- pause durations
- A/B routing percentages

However, those exact parameter names and defaults should be confirmed against the simulation Java classes themselves before documenting them as authoritative. The older `perf-strategy.md` documented properties such as `seqUsers`, `seqRepeats`, `totalUsersPerSec`, and `totalConcurrentUsers`; retain them only where they still match the updated Java source.

### 8.8 Running from IntelliJ

`GatlingRunner` provides an IDE-friendly way to launch simulations directly. This is useful when:

- you want faster edit/run cycles
- you are debugging a single simulation
- you do not want to configure the Gradle task repeatedly

---

## 9. HTTP Test Coverage

### 9.1 Candidate Search

Candidate Search remains a primary workflow and now has a more explicit package structure.

Coverage includes:

- looping the **new** endpoint path
- looping the **old** endpoint path
- random A/B comparisons
- sequential A/B comparisons
- open workload modeling
- closed workload modeling

This gives you several useful comparison modes:

- **single-path regression checks** using loop scenarios
- **true side-by-side comparisons** using sequential A/B
- **mixed production-like traffic** using random A/B
- **rate-driven load** using parallel open simulations
- **concurrency-driven load** using parallel closed simulations

### 9.2 Saved List Candidate Search (Paged)

Saved-list paged search now mirrors candidate-search structure with its own request, scenario, payload, and simulation classes.

Coverage includes:

- new-path loop
- old-path loop
- sequential A/B
- random A/B
- parallel open
- parallel closed

This symmetry is valuable because it keeps execution and reporting conventions aligned across workflows.

### 9.3 Health

`HealthSimulation` should be treated as a lightweight readiness and baseline check rather than a substitute for business-workflow performance testing.

Recommended use:

- environment smoke validation
- quick network/connectivity check
- pre-flight validation before heavier load runs

---

## 10. DB Test Coverage

### 10.1 Candidate Search DB simulation

`CandidateSearchDbSimulation` provides direct DB-focused performance coverage for candidate search.

Use this simulation when the goal is to measure:

- query latency
- repository overhead
- JDBC interaction cost
- connection pool behavior
- database-only regressions

### 10.2 SQL resources

The current resources show:

```text
src/gatling/resources/sql/candidate_search_paged.sql
```

This implies that candidate-search DB testing is backed by a real SQL resource rather than embedded SQL strings.

That is a good pattern because it:

- keeps SQL visible and versionable
- makes performance-query changes easier to review
- reduces duplication between repository and scenario code

### 10.3 When to prefer DB simulations

Prefer DB simulations when you need to answer questions like:

- Did the SQL get slower after a schema or index change?
- Is the regression in the database or in the API layer?

---

## 11. Payloads and Test Data

### 11.1 Current visible payload files

```text
src/gatling/resources/payloads/
├── candidate_search_heavy.json
├── candidate_search_light.json
└── saved_list_search_paged_light.json
```

### 11.2 Suggested meaning

- **light** payloads are appropriate for smoke tests, quick checks, and smaller baselines
- **heavy** payloads are appropriate for worst-case or more realistic search complexity

---

## 12. Docker Execution

The supplied `Dockerfile` is designed to run the performance suite in a reproducible container.

### 12.1 What the Docker image provides

- Eclipse Temurin JDK 17 base image
- `bash`, `python3`, `ca-certificates`, and `git`
- non-root `runner` user
- Gradle wrapper and project files copied into `/work`
- executable permissions for Gradle and CI scripts
- warmed Gradle dependency cache

### 12.2 Why this matters

This setup makes CI and local container runs more reliable because it:

- avoids root-owned outputs
- reduces dependency download churn
- ensures Python helper scripts are available alongside Gatling
- keeps `/work` as the execution root

### 12.3 Operational notes

The image warms dependencies with:

```text
/work/gradlew :performance-tests:dependencies --no-daemon || true
```

That improves caching without making the image build fail on transient dependency-resolution problems.

---

## 13. CI Utilities and Artifact Flow

The `ci/` folder is now a major part of the performance-testing story, not just an implementation detail.

### 13.1 `run-sim-list.sh`

This script runs a list of simulations and is designed for CI-friendly orchestration.

#### Main responsibilities

- accepts a simulation list file
- supports sharding through `SHARD_INDEX` and `SHARD_TOTAL`
- splits CLI args into JVM `-D...` properties and normal Gradle args
- launches each simulation through Gradle
- writes HTML reports, raw logs, and summary artifacts
- copies the latest report directory into mounted artifact storage

#### Artifact roots used by the script

Inside the mounted workspace the script writes to:

```text
/work/build/reports/gatling
/work/perf-raw
/work/perf-summary
```

It also uses writable temp directories such as:

```text
/tmp/tc-build
/tmp/tc-gatling-reports
```

This is a good design because Gatling temp/report generation happens in writable temp space, and only final artifacts are copied into mounted workspace paths.

### 13.2 `summarize_gatling.py`

This helper converts the newest Gatling report into:

- a small Markdown summary
- a compact JSON summary

It reads:

```text
<report-root>/<run-dir>/js/stats.json
```

and normalizes Gatling numeric fields so downstream checks do not break on placeholder values such as `"-"`.

The JSON output contains, at minimum:

- request totals (`total`, `ok`, `ko`)
- response time metrics (`min`, `mean`, `p50`, `p75`, `p95`, `p99`, `max`)
- metadata such as `runId`, `simClass`, and `runDir`

### 13.3 `performance_threshold_check.py`

This script consumes one or more summary JSON files and applies two main gate types:

1. **failed-request percentage** threshold
2. **latency percentile** threshold

It also supports:

- lenient percentile selection for small sample sizes
- stricter percentile selection for larger sample sizes
- `warn-only` mode
- ignoring latency or failure gating when needed

This is a strong pattern for CI because it separates:

- simulation execution
- summary extraction
- policy enforcement

---

## 14. Reports and Outputs

### 14.1 Gatling HTML reports

By default, Gatling reports are written under:

```text
build/reports/gatling
```

In CI or custom runs, the root can be overridden with:

```bash
-Dgatling.reportRoot=/custom/path
```

### 14.2 Raw logs

The CI runner stores raw `simulation.log` files in:

```text
/work/perf-raw
```

### 14.3 Summaries

The CI runner stores human/machine summaries in:

```text
/work/perf-summary
```

These summaries are the recommended inputs for dashboards, trend tracking, and threshold gates.

---

## 15. Threshold Strategy

The helper scripts show a practical gating model that is better than checking raw Gatling output directly.

### 15.1 Correctness gate

The threshold checker can fail a run when the percentage of failed requests exceeds a configured limit.

### 15.2 Latency gate

The checker can enforce:

- a more lenient percentile for small-sample runs
- a stricter percentile once request volume is high enough

That is useful because percentile noise is higher when request counts are small.

---

## 16. Recommended Usage Patterns

### 16.1 Use HTTP simulations when

- validating user-visible endpoint performance
- comparing legacy vs new implementations
- measuring realistic request/response behavior
- checking the impact of auth and service-layer logic

### 16.2 Use DB simulations when

- isolating query cost
- validating repository/SQL changes
- investigating a suspected database bottleneck
- comparing database behavior before and after schema/index changes

### 16.3 Use sequential A/B when

- you want cleaner side-by-side comparison conditions
- you want both implementations exercised in nearly identical session context

### 16.4 Use random A/B when

- you want a mixed traffic pattern
- you want to model partial rollout or weighted routing

### 16.5 Use open vs closed wisely

- **open** simulations are better for arrival-rate questions
- **closed** simulations are better for concurrency/capacity questions

---

## 17. Extending the Suite

### 17.1 To add a new HTTP workflow

Follow the existing layering:

1. add payload helper if needed
2. add request definitions under `requests/http/...`
3. add reusable scenarios under `scenarios/http/...`
4. add a base simulation if the workflow has multiple load models
5. add concrete simulations for open/closed/A-B patterns as needed
6. add resource payloads under `src/gatling/resources/payloads/`
7. add CI list entries and threshold coverage if the simulation will run in automation

### 17.2 To add a new DB workflow

Follow the DB layering:

1. add SQL resource under `src/gatling/resources/sql/`
2. add or extend repository support under `db/repositories/`
3. add or extend DB request logic under `requests/db/`
4. add scenario under `scenarios/db/`
5. add simulation under `simulations/db/...`

### 17.3 Naming conventions

The current codebase already has a strong naming pattern:

- `*Requests` for request definitions
- `*Scenario` for reusable behavior
- `*Simulation` for Gatling entrypoints
- `*BaseSimulation` for shared setup
- `*Payloads` for resource payload resolution

Keep following that pattern. It makes the module easy to navigate.

---
