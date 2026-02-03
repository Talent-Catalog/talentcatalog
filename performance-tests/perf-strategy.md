# Performance Testing Strategy (Gatling)

> ⚠️ **Status: In Progress / Subject to Change**  
> This document describes the current performance testing approach under `performance-tests/`.  
> It is a living strategy document and will evolve as the test suite grows and the Scala legacy code is retired.

---

## 1. Goals

The performance test suite is designed to:

1. **Protect critical user workflows** from performance regressions  
   (starting with Candidate Search).

2. **Support safe endpoint comparisons**  
   between the new implementation and the legacy `old-fetch` endpoint.

3. **Provide repeatable performance baselines**  
   that can be executed locally and later integrated into CI.

4. **Make performance tests easy to extend**  
   through a clean separation of requests, scenarios, and simulations.

---

## 2. Guiding Principles

- **Workflow-first testing**  
  Tests should represent realistic user behavior, not just raw endpoint calls.

- **Clear separation of concerns**  
  The codebase follows:

  ```
  Requests → Chains → Scenarios → Simulations
  ```

- **Configurable execution**  
  All tests are controlled through system properties (base URL, payload size, load shape).

- **Repeatable and comparable results**  
  A/B simulations must keep all conditions constant except the endpoint under test.

- **Scala retirement**  
  Existing Scala simulations will be phased out.  
  All new work should be written using the **Gatling Java DSL**.

---

## 3. Repository Layout

Current structure:

```
performance-tests/
  src/gatling/java/org/talentcatalog/perf/
    GatlingRunner.java

    chains/
    config/
    payloads/
    requests/
    scenarios/
    simulations/

  src/gatling/resources/
    application.conf
    payloads/
    users.csv

  src/gatling/scala/
    org.talentcatalog.perf.legacy/   (legacy, to be removed)
```

---

## 4. Execution Entry Point

### 4.1 GatlingRunner

Class:

```
org.talentcatalog.perf.GatlingRunner
```

Purpose:

- Run simulations directly from IntelliJ
- Run simulations from any JVM launcher
- Avoid reliance on Gradle plugin tasks

Simulation selection priority:

1. Program argument 0  
2. `-DsimClass=...`  
3. Default simulation class

Results output directory:

- Default:

  ```
  build/reports/gatling
  ```

- Override:

  ```bash
  -Dgatling.resultsFolder=build/reports/gatling
  ```

---

## 5. Configuration Strategy

### 5.1 PerfConfig + PerfSettings

Configuration is resolved in this order:

1. JVM system properties  
2. `application.conf`

Required:

- `perf.baseUrl`

Optional:

- `perf.userAgent`

Example:

```bash
-Dperf.baseUrl=https://tctalent-test.org
-Dperf.userAgent=gatling-performance-tests
```

---

### 5.2 HttpProtocolFactory

The shared HTTP protocol builder configures:

- Base URL
- JSON headers
- User-Agent
- Authorization header derived from session token

All simulations reuse the same protocol.

---

## 6. Authentication Strategy

### 6.1 AuthChains.ensureLoggedIn()

Authentication is centralized and idempotent:

- If `accessToken` exists in session → skip login
- Otherwise:

  - POST `/api/admin/auth/login`
  - Extract `$.accessToken`
  - Store it in the session

Used by all Candidate Search scenarios before executing requests.

---

## 7. Request Layer

### 7.1 CandidateSearchRequests

Candidate Search tests compare two endpoints:

| Endpoint Type | Path |
|-------------|------|
| NEW         | `POST /api/admin/candidate/search` |
| OLD-FETCH   | `POST /api/admin/candidate/search-old-fetch` |

Both requests validate:

- HTTP 200 response

---

## 8. Payload Strategy

### 8.1 CandidateSearchPayloads

Payload files live under:

```
src/gatling/resources/payloads/
```

Available modes:

| Mode | Property | File |
|------|----------|------|
| Baseline | `-Dpayload=baseline` | `candidate_search_light.json` |
| Heavy (default) | `-Dpayload=heavy` | `candidate_search_heavy.json` |

Default behavior:

- Missing or unknown values fall back to `heavy`

---

## 9. Scenario Layer (User Behavior)

Scenarios represent virtual user workflows.

All Candidate Search scenarios:

- Feed users from `users.csv`
- Authenticate once per user
- Loop candidate search calls
- Use `group(...)` labels for report breakdown

---

### 9.1 Loop Scenarios

#### NewEndpointLoopScenario

- Login once
- Repeat NEW endpoint search requests
- Pause between iterations

#### OldEndpointLoopScenario

- Login once
- Repeat OLD-FETCH endpoint search requests
- Pause between iterations

---

### 9.2 A/B Scenarios

#### SequentialABScenario

Each iteration executes:

1. NEW endpoint
2. OLD endpoint immediately after

Useful for per-user direct comparisons under the same session.

---

#### RandomABFiniteScenario

Each iteration:

1. Pause randomly
2. Choose NEW vs OLD based on configured percentage
3. Execute chosen request

Useful for modeling mixed traffic bursts.

---

## 10. Simulation Layer (Load Models)

Simulations define:

- Load injection profile
- Duration phases
- Assertions

All Candidate Search simulations extend:

```
CandidateSearchBaseSimulation
```

Shared setup includes:

- Config loading
- Protocol building
- Payload resolution

---

## 11. Candidate Search Simulations

---

### 11.1 CandidateSearchSequentialABSimulation

Sequential per-user A/B comparison.

System properties:

| Property | Default | Description |
|---------|---------|-------------|
| `seqUsers` | 1 | Users started immediately |
| `seqRepeats` | 1 | Sequential iterations per user |

Example:

```bash
./gradlew :performance-tests:gatlingTest \
  -PsimClass=org.talentcatalog.perf.simulations.http.candidatesearch.CandidateSearchSequentialABSimulation \
  -Dperf.baseUrl=https://OUR_ENV_HOST \
  -Dpayload=baseline \
  -DseqUsers=5 \
  -DseqRepeats=50
```

---

### 11.2 CandidateSearchRandomABFiniteSimulation

Finite active users with weighted random routing.

System properties:

| Property | Default | Description |
|---------|---------|-------------|
| `randUsers` | required | Number of users |
| `randRepeats` | 100 | Iterations per user |
| `randMinPauseSeconds` | 1 | Minimum think time |
| `randMaxPauseSeconds` | 10 | Maximum think time |
| `randPctNew` | 50 | % routed to NEW |

Example:

```bash
./gradlew :performance-tests:gatlingTest \
  -PsimClass=org.talentcatalog.perf.simulations.http.candidatesearch.CandidateSearchRandomABFiniteSimulation \
  -Dperf.baseUrl=https://OUR_ENV_HOST \
  -Dpayload=heavy \
  -DrandUsers=100 \
  -DrandRepeats=100 \
  -DrandPctNew=50
```

---

### 11.3 CandidateSearchParallelOpenSimulation

Open workload model:

- Constant arrival rate (users/sec)
- Split ~50/50 between NEW and OLD

System properties:

| Property | Default | Description |
|---------|---------|-------------|
| `totalUsersPerSec` | required | Total arrival rate |
| `rampSeconds` | 60 | Ramp duration |
| `warmupSeconds` | 120 | Warmup duration |
| `measureSeconds` | 600 | Measurement duration |

Example:

```bash
./gradlew :performance-tests:gatlingTest \
  -PsimClass=org.talentcatalog.perf.simulations.http.candidatesearch.CandidateSearchParallelOpenSimulation \
  -Dperf.baseUrl=https://OUR_ENV_HOST \
  -DtotalUsersPerSec=20 \
  -DrampSeconds=60 \
  -DwarmupSeconds=120 \
  -DmeasureSeconds=600
```

---

### 11.4 CandidateSearchParallelClosedSimulation

Closed workload model:

- Fixed concurrent users split across endpoints

System properties:

| Property | Default | Description |
|---------|---------|-------------|
| `totalConcurrentUsers` | required | Total concurrency |
| `warmupSeconds` | 120 | Warmup duration |
| `measureSeconds` | 600 | Measurement duration |

Example:

```bash
./gradlew :performance-tests:gatlingTest \
  -PsimClass=org.talentcatalog.perf.simulations.http.candidatesearch.CandidateSearchParallelClosedSimulation \
  -Dperf.baseUrl=https://OUR_ENV_HOST \
  -DtotalConcurrentUsers=100 \
  -DwarmupSeconds=120 \
  -DmeasureSeconds=600
```

---

## 12. Assertions (Current Defaults)

Initial global guardrails:

- Failed requests < **1%**
- 95th percentile response time < **2000ms**

These thresholds are placeholders and will evolve as stable baselines are established.

---

## 13. Reporting

Reports are generated under:

```
build/reports/gatling/
```

Grouping labels allow clear breakdown:

- `candidate-search-new`
- `candidate-search-old-fetch`

---

## 14. Scala → Java Migration Plan

Scala simulations under:

```
src/gatling/scala/
```

are legacy and will be retired.

Planned steps:

1. Stop adding new Scala tests
2. Migrate remaining Scala simulations into Java DSL
3. Remove Scala dependencies once unused
4. Simplify Gradle build and reduce compilation overhead

---

## 15. Roadmap (Next Iterations)

Short-term:

- Document `users.csv` format and safe credential handling
- Add quick-start recommended default runs
- Standardize assertion strategy per workflow

Mid-term:

- Add CI regression baseline simulation
- Expand test coverage beyond Candidate Search

Long-term:

- Fully remove Scala legacy folder
- Build reusable performance suite for critical workflows

---

## Appendix: Candidate Search Endpoints

- NEW:

  ```
  POST /api/admin/candidate/search
  ```

- OLD-FETCH:

  ```
  POST /api/admin/candidate/search-old-fetch
  ```

Authentication:

```
POST /api/admin/auth/login
```

---
