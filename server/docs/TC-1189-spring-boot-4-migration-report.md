# TC-1189 — Spring Boot 4 Migration Report

Branch: `john/tc-1189-upgrade-to-spring-boot-4` (base: `b1c367b9f` TC-1584)

| Stage | Status |
|---|---|
| 1. Assess and plan | Complete. Decisions D1–D10 approved. |
| Pre-migration baseline tests (requirements 1 and 2) | Complete on Boot 3.5.16: **3,577 tests, 0 failures** |
| 2. Build configuration | Complete. Compilation fails as expected; failures are grouped in §3.3. **Awaiting review before Stage 3.** |
| 3–6 | Not started |

Nothing has been committed.

---

## 1. Environment and versions

The target versions are the versions **actually resolved** by Gradle from `spring-boot-dependencies:4.1.2`, checked with `./gradlew :server:dependencies`.

| Component | Before (Boot 3.5.16) | After Stage 2 (Boot 4.1.2) | How set |
|---|---|---|---|
| Spring Boot | 3.5.16 | **4.1.2** | plugin |
| Spring Framework | 6.2.x | 7.0.10 | managed |
| Spring Security | 6.5.x | 7.1.2 | managed |
| Spring Batch | 5.2.6 | 6.0.6 | managed |
| Spring Data JPA | 3.5.x | 4.1.2 (release train 2026.0.2) | managed |
| Spring Data REST | 4.5.x | 5.1.2 | managed (now via `spring-boot-starter-data-rest`) |
| Hibernate ORM | 6.6.x | 7.4.13.Final | managed |
| Jackson 3 (`tools.jackson`) | — | 3.1.7 | managed (Boot's JSON mapper) |
| Jackson 2 (`com.fasterxml.jackson`) | 2.19.x | 2.21.7 | managed, now declared explicitly |
| Flyway | 11.20.1 (pinned) | 12.4.0 | managed (pin removed) |
| Testcontainers | **1.21.4** in practice (Boot 3.5's management overrode the declared 2.0.3 BOM) | 2.0.5 | managed (BOM removed) |
| JUnit Jupiter | 5.13.4 | 6.0.3 | managed |
| Mockito | 5.17.x | 5.23.0 | managed |
| Tomcat | 10.1.x | 11.0.27 | managed |
| HikariCP | 6.x | 7.0.2 | managed |
| Lettuce | 6.x | 7.5.2 | managed |
| PostgreSQL JDBC | 42.7.x | 42.7.14 | managed |
| Lombok | 1.18.x | 1.18.48 | managed |
| Hypersistence Utils | hibernate-63:3.7.3 | **hibernate-73:3.16.0** (Hibernate 7.3+; **uses Jackson 3**, see §2.3) | explicit |
| ShedLock | 5.10.0 | **7.10.1** | explicit |
| jjwt | 0.11.2 | 0.11.2 (unchanged, D8) | explicit |
| TOTP starter | 1.7.1 | 1.7.1 (unchanged, D8) | explicit |
| docx4j | 8.3.15 (javax JAXB) | 8.3.15 (unchanged, D8) | explicit |
| dependency-management plugin | 1.1.7 | 1.1.7 | plugin |
| Gradle | 8.14.4 | 8.14.4 (D2) | wrapper |
| Java | 17 toolchain | 17 (D2) | toolchain |

---

## 2. Findings by area

### 2.1 Build / starters (Boot 4 modular structure)
- `spring-boot-starter-web` is renamed `spring-boot-starter-webmvc`.
- **Flyway:** `flyway-core` on its own no longer triggers auto-configuration, so `spring-boot-starter-flyway` is needed. Without it, **migrations would silently stop running at startup.**
- **Batch:** `spring-boot-starter-batch` now uses an **in-memory job repository**. `spring-boot-starter-batch-jdbc` is needed to keep job metadata in our database.
- **Found in Stage 2:**
  - `spring-boot-starter-webflux` no longer provides the auto-configured **`WebClient.Builder`** that `AnthropicConfig` and `DuolingoConfig` inject. It now comes from `spring-boot-starter-webclient`.
  - Nothing in our current starters would provide the **`RestClient.Builder`** that the Python-service clients and `TcApiServiceImpl`/`TcSkillsExtractionServiceImpl` inject. It now comes from `spring-boot-starter-restclient`.
- **Found in Stage 2:** `spring-data-rest-webmvc` on its own no longer activates Data REST auto-configuration (`/api/hal`, `RestRepositoryConfig`). It now needs `spring-boot-starter-data-rest`.
- **Found in Stage 2:** `spring-boot-security-test` is what wires Spring Security into `@AutoConfigureMockMvc` (its `AutoConfigureMockMvc.imports`). So `spring-boot-starter-security-test` is **required**. Without it, the ~56 MockMvc tests would run without the security filter integration.
- `spring-boot-flyway` registers Flyway with the data-source-initialization test imports, so `@DataJpaTest` (`BaseJpaIntegrationTest`) still runs Flyway without a Flyway test starter.
- `TotpAutoConfiguration` is registered in `META-INF/spring/...AutoConfiguration.imports`. Its conditions and annotations all still exist in Boot 4.

### 2.2 Spring Boot relocated classes
Locations confirmed against the Boot 4.1.2 jars.

| Used in | Class | Boot 4 location |
|---|---|---|
| `TcFlywayConfig` | `FlywayMigrationStrategy` | `org.springframework.boot.flyway.autoconfigure` |
| `DatabaseConfiguration` | `DataSourceProperties` | `org.springframework.boot.jdbc.autoconfigure` |
| `DatabaseConfiguration` | `EntityManagerFactoryBuilder` | `org.springframework.boot.jpa` |
| `DatabaseConfiguration` | `SpringImplicitNamingStrategy` | `org.springframework.boot.hibernate` |
| 56 tests | `@AutoConfigureMockMvc` | `org.springframework.boot.webmvc.test.autoconfigure` |
| 54 tests | `@WebMvcTest` | `org.springframework.boot.webmvc.test.autoconfigure` |
| `BaseJpaIntegrationTest` | `@DataJpaTest` | `org.springframework.boot.data.jpa.test.autoconfigure` |
| `BaseJpaIntegrationTest` | `@AutoConfigureTestDatabase` | `org.springframework.boot.jdbc.test.autoconfigure` |
| new payload tests | `JacksonAutoConfiguration` and others | `org.springframework.boot.{jackson,http.converter,restclient,http.codec,webclient,validation}.autoconfigure` |
| `application.yml` | exclude `...autoconfigure.data.redis.RedisRepositoriesAutoConfiguration` | now `org.springframework.boot.data.redis.autoconfigure.DataRedisRepositoriesAutoConfiguration`. **An invalid exclude fails startup.** Plan: use `spring.data.redis.repositories.enabled: false`. |
| `application-test.yml` | excludes `RedisAutoConfiguration` and `RedisRepositoriesAutoConfiguration` (**found in Stage 2**) | now `DataRedisAutoConfiguration` and `DataRedisRepositoriesAutoConfiguration` (same package as above) |

### 2.3 Jackson 3 (highest behavioural risk)
- Boot 4 auto-configures a Jackson 3 `JsonMapper` and **no Jackson 2 `ObjectMapper` bean**. Spring MVC, RestClient, WebClient codecs and Spring Data REST all use Jackson 3.
- Five main classes inject a Jackson 2 `ObjectMapper`: `CandidateReadDao`, `CandidateReadMapper`, `PublishedDocBuilderServiceImpl`, `CandidateDtoFetchServiceImpl` and `VerifyPlusPayloadParserImpl`. They compile, but **startup will fail**.
- About 60 `@WebMvcTest` classes `@Autowired` a Jackson 2 `ObjectMapper`. They compile, but the context will fail to load.
- **Silent wire-format risk:** the Python-service DTOs use the Jackson 2 annotation `com.fasterxml...@JsonNaming(SnakeCaseStrategy)`, which Jackson 3 ignores. These classes are now covered by regression tests (§3.1).
- **Lombok `@Jacksonized` (found in Stage 2):** 16 classes produce the compiler warning *"Ambiguous: Jackson2 and Jackson3 exist; define which variant(s) you want in lombok.config"*. Affected: all the Python-service DTOs, `CandidateMatchingExplanationData` and `ExperienceMatchExplanationData`. `@Jacksonized` generates the builder-based deserialization annotations, so without the right configuration Jackson 3 **cannot deserialize these immutable classes**. Decision D11.
- **Correction to Stage 1 (found in Stage 2):** **Hypersistence `-hibernate-73` uses Jackson 3** (`tools.jackson.*`). Every release of that artifact does, 3.15.0–3.16.0, and it is the only line that supports Hibernate 7.3+. So the jsonb columns (`ChatbotMessage.referencedFaqIds`, `CandidateMatchingExplanation.explanation`) will be written and read with Jackson 3. `ObjectMapperWrapper.INSTANCE.getObjectMapper()` now returns a Jackson 3 mapper, which breaks `CandidateMatchingExplanationDataJacksonTest` at compile time. Persisted-format regression tests were added (§3.1). Decision D12 amends D3.
- Jackson 2 is still used directly by the standalone utilities (`QueryHelper`, `TextPartsCodec`, `S3TranslationStorageService`, `QAServiceImpl`) and by `jjwt-jackson`. It is now declared explicitly as `com.fasterxml.jackson.core:jackson-databind`, with the version managed by Boot.

### 2.4 Spring Batch 6
- Package moves, confirmed against `spring-batch-core`/`-infrastructure` 6.0.6:

  | Batch 5 | Batch 6 |
  |---|---|
  | `core.Job`, `JobExecution`, `JobInstance`, `JobExecutionException` | `core.job.*` |
  | `core.JobParameters`, `JobParametersBuilder` | `core.job.parameters.*` |
  | `core.Step`, `StepExecution`, `StepContribution` | `core.step.*` |
  | `core.ChunkListener`, `JobExecutionListener`, `StepExecutionListener` | `core.listener.*` |
  | `core.explore.JobExplorer` (deprecated) | `core.repository.explore.JobExplorer` |
  | `core.repository.JobInstanceAlreadyCompleteException` | `core.launch.JobInstanceAlreadyCompleteException` |
  | `item.*` (`Chunk`, `ItemReader`, `ItemProcessor`, `ItemWriter`) | `infrastructure.item.*` |
  | `repeat.*` (`RepeatStatus`, `RepeatTemplate`, `SimpleCompletionPolicy`) | `infrastructure.repeat.*` |

- `JobLauncher` and `JobExplorer` are deprecated, and `TaskExecutorJobOperator` replaces `SimpleJobOperator`, as planned in D5.
- The legacy chunk classes (`ChunkOrientedTasklet`, `SimpleChunkProvider`, `SimpleChunkProcessor`) are still present in 6.0.6 (deprecated), as planned in D5.
- **Pre-existing issue (found while writing the baseline tests):** jobs built by `CandidateBatchJobFactory` are never registered in the `JobRegistry`. Batch 5's `SimpleJobOperator.restart(executionId)` looks the job up with `jobRegistry.getJob(jobName)`, so **restarting a factory-built job from System Admin fails today with `NoSuchJobException`**. The baseline test `restartUnregisteredJobFails` records this current behaviour. Decision D13.
- **Schema:** `migration/6.0/migration-postgresql.sql` in the Batch 6.0.6 jar contains only `ALTER SEQUENCE BATCH_JOB_SEQ RENAME TO BATCH_JOB_INSTANCE_SEQ;`. Our schema already has the Batch 5 table layout. **The next Flyway version is V2_34**: V2_33 is the latest both on this branch and on `origin/staging`. I'll re-check this immediately before creating the migration in Stage 4, since other branches could claim V2_34 first.

### 2.5 Spring Security 7
- Main code compiled with no Security errors, including `BeanIds.AUTHENTICATION_MANAGER`, the TC-1584 `DaoAuthenticationProvider` changes and the TC-1583 WebSocket configuration.
- Unchanged from Stage 1: CSRF stays disabled for HTTP, no `@EnableWebSocketSecurity` is used, and the `"/status**"` pattern needs a regression test (Stage 3).

### 2.6 Spring Data JPA / Hibernate 7
- Main code compiled with no Data JPA or Hibernate errors. Specifications, `@Query`, `@Formula`, `@Immutable`, `@Type(JsonBinaryType)` and `CamelCaseToUnderscoresNamingStrategy` (still present in 7.4.13) all compile.
- **JAXB pin (noted in Stage 2, unchanged from Boot 3):** Hibernate 7.4 requests `jaxb-runtime:4.0.7` (Jakarta), but our explicit `jaxb-runtime:2.3.8` for docx4j wins. Hibernate 6.6 under Boot 3 was in the same position, so nothing changes. Check at startup in Stage 6.
- Runtime risks to cover in Stage 5: Hibernate 7's stricter HQL validation (45 `@Query` files) and merge semantics.

### 2.7 Other
- **ShedLock 7.10.1** and **Hypersistence 3.16.0**: main code compiles against both with no changes.
- **Testcontainers 2:** `org.testcontainers.containers.PostgreSQLContainer` still exists (deprecated), so `PostgresTestContainer` still compiles. The plan is to move to `org.testcontainers.postgresql.PostgreSQLContainer`.
- **JUnit 4 leftovers (found in Stage 2):** `UserAdminApiTest` (`org.junit.Assert.assertThrows`) and `SavedSearchServiceImplTest` (`org.junit.Assert.assertFalse`). JUnit 4 was only on the classpath through Testcontainers 1.x. The fix is to use the JUnit 5 `Assertions` equivalents, which behave the same.
- `org.springframework.lang.@Nullable` is deprecated in Framework 7 (270 files). This only produces warnings, so it is left as it is.
- Test and CI command: CI runs `./gradlew clean :server:test --stacktrace --rerun-tasks`. Locally I use `./gradlew -Denv=dev :server:test --rerun-tasks`. `-Denv=dev` only skips bundling the Angular portals into the server jar and does not change which server tests run.

---

## 3. Test results

### 3.1 Pre-migration baseline tests (added on Boot 3.5.16 before any build change)
All of these tests check behaviour, not Jackson or Spring Batch APIs, so they can run unchanged on Boot 4. The exception is the relocated Boot test auto-configuration imports, which Stage 3 updates.

**Requirement 1: JSON payloads.** All of these use real HTTP to a local recording server (`util/http/RecordingHttpServer`), and request bodies are compared in strict JSONAssert mode. Strict mode means exactly the same fields, explicit nulls, values and array order.

| Test | Path covered | Tests |
|---|---|---|
| `service/explanation/PythonServiceClientsJsonPayloadTest` | Boot `RestClient.Builder` with `@HttpExchange` clients. Explanations and embeddings requests and responses, snake_case, nested objects, explicit nulls, enums, `OffsetDateTime`, unknown fields ignored. | 4 |
| `service/db/impl/SalesforceJsonPayloadTest` | Plain `WebClient.builder()` (Spring default codecs). Complete bodies for contact upsert and composite upsert (with an `attributes`-first check), job opportunity upsert, employer opportunity update, stage update and candidate opportunity composite upsert. Decoding of the bearer token, contacts, opportunity (booleans, numbers, nulls), account, opportunity history and upsert errors. | 12 |
| `service/ai/impl/AnthropicJsonPayloadTest` | Boot `WebClient.Builder` (the same path as Duolingo): request fields and response decoding. | 1 |
| `CandidateMatchExplanationIntegrationTest` (2 tests added) | jsonb written by the explanation service (exact stored JSON), and reading of rows stored in the legacy and current formats. | +2 |
| `ChatbotMessageRepositoryIntegrationTest` (3 tests added) | jsonb array written for `referencedFaqIds`, null handling, and reading of previously stored rows. | +3 |

**Requirement 2: Spring Batch.** `service/db/impl/BatchJobServiceIntegrationTest` (8 tests) runs on real PostgreSQL through Testcontainers with the Flyway-created schema. Job state is read directly from the `batch_*` tables. It covers:
- asynchronous launch: the call returns while the job is still running, and the job runs on another thread;
- the summary format;
- stopping a running job, which ends `STOPPED`;
- stopping a job that isn't running, which throws `JobExecutionNotRunningException`;
- restarting a stopped registered job: a new execution of the same job instance, which completes, while the old execution stays `STOPPED`;
- restarting an unregistered job, which throws `NoSuchJobException` (the current behaviour, D13);
- once-per-day blocking a second run;
- non-once-per-day allowing a second run.

The test that existing execution metadata survives the schema migration needs the V2_34 migration, so it is planned for **Stage 4**. It will test both a fresh database and a database containing Batch 5 metadata.

**Current behaviour recorded by the baseline tests (pre-existing, not changed):**
- Salesforce `RecordType` is sent as `{"Name": "...", "name": "..."}`, a duplicate. The cause is `RecordTypeField`'s public `Name` field plus Lombok's `getName()`.
- `EmployerOpportunityRequest` sends **every field twice**, for example `CVs_Folder__c` and `cvs_Folder__c`, and `TCid__c` and `tcid__c`. The cause is the public fields plus Lombok `@Getter`. This presumably works because Salesforce field names are case-insensitive.

Jackson 3 may serialize both of these differently. The tests pin the current output, so any change will be caught and has to be decided deliberately.

### 3.2 Test runs
| Date | Command | Tests | Failures | Notes |
|---|---|---|---|---|
| (TC-1584) | `./gradlew :server:test` | 3,547 | 0 | Boot 3.5.16 |
| 2026-10-26 | `./gradlew -Denv=dev :server:test --rerun-tasks` | 3,572 | 0 | Boot 3.5.16 with 25 new tests (Python 4, Salesforce 12, Anthropic 1, Batch 8). 351 classes, 0 skipped. |
| 2026-10-26 | `./gradlew -Denv=dev :server:test --tests '*ChatbotMessageRepositoryIntegrationTest' --tests '*CandidateMatchExplanationIntegrationTest'` | 19 | 0 | Boot 3.5.16, including the 5 new jsonb tests. **Boot 3 baseline is now 3,577.** |

**Correction to Stage 1:** the PostgreSQL/Testcontainers integration tests are **not** excluded from the Gradle run. All 17 integration classes ran in the baseline:
- `ServiceAssignmentRepositoryIntegrationTest`
- `ServiceResourceRepositoryIntegrationTest`
- `CandidateDataAuditingIntegrationTest`
- `PrivacyPolicyAgreementIntegrationTest`
- `CandidateCertificationRepositoryIntegrationTest`
- `CandidateRepositoryIntegrationTest`
- `ChatbotMessageRepositoryIntegrationTest`
- `DuolingoCouponRepositoryIntegrationTest`
- `UserRepositoryIntegrationTest`
- `AgreementRepositoryIntegrationTest`
- `MatchingRepositoryIntegrationTest`
- `SavedListSearchByCandidateIntegrationTest`
- `SavedListMatchingIntegrationTest`
- `SavedSearchMatchingIntegrationTest`
- `SavedSearchServiceImplTest` (integration-based)
- `CandidateMatchExplanationIntegrationTest`
- the new `BatchJobServiceIntegrationTest`

Per requirement 3, later stages will also run them explicitly with `--tests` filters.

### 3.3 Stage 2 compilation (Boot 4.1.2)
Command: `./gradlew -Denv=dev :server:compileJava`, with `-Xmaxerrs` raised through an init script in the scratchpad (the build itself is unchanged).

**Main code: 62 errors in 12 files, from 2 causes.** All other main code compiles, including Security, WebSocket, JPA/Hibernate, Hypersistence, ShedLock, Redis, TOTP, docx4j and Jackson 2 usage.

| # | Cause | Errors | Files |
|---|---|---|---|
| M1 | **Spring Batch 6 package moves** (§2.4), plus the deprecated `JobExplorer` move | 55 | `BatchConfig`, `BatchJobService`, `BatchJobServiceImpl`, `SystemAdminApi`, `CandidateBatchJobFactory`, `CandidateReader`, `CandidateWriter`, `AdaptiveDelayTasklet`, `LoggingChunkListener`, `LoggingJobExecutionListener` |
| M2 | **Spring Boot relocated classes** (§2.2) | 7 | `TcFlywayConfig` (`FlywayMigrationStrategy`), `DatabaseConfiguration` (`DataSourceProperties`, `EntityManagerFactoryBuilder`, `SpringImplicitNamingStrategy`) |

Main-code warnings:
- 16 Lombok `@Jacksonized` ambiguity warnings (§2.3, D11).
- 14 MapStruct "unmapped target properties" warnings, which are pre-existing.

**Test code** cannot compile until main compiles. I scanned every test import against the resolved Boot 4 test classpath; the scan doesn't count MapStruct-generated `*MapperImpl` classes, which will exist once main compiles. Results:

| # | Cause | Files |
|---|---|---|
| T1 | Relocated Boot test annotations: `@AutoConfigureMockMvc` (56), `@WebMvcTest` (54), `@DataJpaTest`/`@AutoConfigureTestDatabase` (1) | ~57 |
| T2 | Spring Batch 6 package moves | 5 (`CandidateBatchJobFactoryTest`, `CandidateReaderTest`, `CandidateWriterTest`, `SystemAdminApiTest`, `BatchJobServiceIntegrationTest`) |
| T3 | Relocated Boot auto-configuration classes used by the new payload tests | 2 (`PythonServiceClientsJsonPayloadTest`, `AnthropicJsonPayloadTest`) |
| T4 | JUnit 4 asserts no longer on the classpath | 2 (`UserAdminApiTest`, `SavedSearchServiceImplTest`) |
| T5 | Jackson type change: `ObjectMapperWrapper.getObjectMapper()` now returns a Jackson 3 mapper | 1 (`CandidateMatchingExplanationDataJacksonTest`) |

**Expected to fail at runtime rather than at compile time** (Stage 3 or Stage 5):
- the 5 injected Jackson 2 `ObjectMapper`s in main code, and about 60 in tests (§2.3);
- the Redis exclude class names in `application.yml` and `application-test.yml` (§2.2);
- the `@Jacksonized` and `@JsonNaming` behaviour under Jackson 3;
- Hibernate 7 HQL validation.

---

## 4. Issues and decisions

### Approved decisions (Stage 1)
| # | Decision | Decision |
|---|---|---|
| D1 | Boot 4.1.2 | Approved, implemented |
| D2 | Keep Gradle 8.14.4 and Java 17 | Approved, implemented |
| D3 | Jackson 3 for Spring; Jackson 2 only where needed; no `spring-boot-jackson2` | Approved. **See D12 amendment** |
| D4 | `spring.jackson.use-jackson2-defaults=true` for TC-1189 | Approved, Stage 3 |
| D5 | Single async `JobOperator` plus `JobRepository`; keep the deprecated chunk classes | Approved, Stage 3 |
| D6 | Flyway: remove the pin and use the managed 12.4.0 | Approved, implemented |
| D7 | ShedLock 7.10.1, Hypersistence `-73:3.16.0`, Testcontainers 2 artifact names | Approved, implemented |
| D8 | Keep jjwt, docx4j and TOTP | Approved, implemented |
| D9 | Explicit modular starters | Approved, implemented |
| D10 | Report location | Done |

### New decisions requiring approval (Stage 2)
| # | Decision | Recommendation |
|---|---|---|
| D11 | Lombok `@Jacksonized` variant (`server/lombok.config`) | Generate **both** the Jackson 2 and Jackson 3 builder annotations (`lombok.jacksonized.jacksonVersion += 2` and `+= 3`) for the duration of the migration. The DTOs are read by Spring and Hypersistence (Jackson 3), and the existing Jackson 2 unit tests still read them too. Switching to Jackson 3 only can be a follow-up. |
| D12 | Amend D3: Hypersistence (jsonb columns) uses Jackson 3 | Accept, since there is no Jackson 2 option for Hibernate 7.3+. The new persisted-format tests (§3.1) must keep passing, so stored jsonb stays readable and is written in the same format. The alternative, replacing Hypersistence with Hibernate's native `@JdbcTypeCode(SqlTypes.JSON)`, is a larger change and not recommended for TC-1189. |
| D13 | Pre-existing: restarting factory-built batch jobs fails (`NoSuchJobException`) | **Preserve the current behaviour in TC-1189**, with the characterization test, and fix it in a separate ticket. Fixing it changes production behaviour, which is outside a like-for-like upgrade. Batch 6's `TaskExecutorJobOperator.restart` still uses the registry, so behaviour stays the same either way. |
| D14 | Pre-existing Salesforce duplicate fields (`RecordType`, `EmployerOpportunityRequest`) | **Keep the current output in TC-1189.** If Jackson 3 changes it, report it before changing anything. Clean it up in a separate ticket. |
| D15 | Starters added beyond the Stage 1 plan: `webclient`, `restclient`, `data-rest` and `security-test` (§2.1) | These are routine compatibility changes needed to keep current behaviour, so they are already applied. Listed here for visibility. |

### Outstanding blockers
- None. Stage 3 can start once these decisions are reviewed.

### Changes that may affect production behaviour
- Jackson 3 becomes the serializer for HTTP, RestClient, WebClient **and the Hypersistence jsonb columns** (D12).
- Flyway 11 → 12.
- The Batch sequence rename migration (V2_34).
- Hibernate 7 query and merge semantics.
- Batch job metadata depends on `spring-boot-starter-batch-jdbc`. With the plain batch starter, metadata would no longer be persisted.

### Manual verification requirements (to be refined)
- Chat over WebSocket from the Angular clients.
- Explanation and embedding calls to the Python services.
- Salesforce sync (in particular the duplicate-field payloads, D14).
- Running, stopping and restarting batch jobs from System Admin.
- 2FA (TOTP) login.
- CV DOCX export.
- The Data REST `/api/hal` endpoints.

### Observations (not changed)
- `CandidateMatchExplanationServiceClientConfig` reads `tc-vector-embedding-service.apiUrl` rather than a separate explanation-service URL. It may be intentional (same Python service), but it's worth confirming.

### Carried over from TC-1584 (assess separately, not in TC-1189)
- The `TcAuthenticationProvider.supports()` type-checking behaviour.
- Possible duplicate Salesforce POSTs after connection failures and retries.
- The Salesforce `Content_Type` header.

---

## 5. Follow-up recommendations (not part of TC-1189)
- **Salesforce WebClient response handling:** TC-1584 kept the `executeWithRetry()` contract by buffering responses and returning a `ClientResponse`. After Boot 4, refactor it to return typed results and process responses inside the WebClient lifecycle. This does not block the migration.
- Fix restarting of `CandidateBatchJobFactory` jobs (D13).
- Remove the duplicate Salesforce request fields (D14).
- Replace the deprecated Batch 6 chunk classes in `CandidateBatchJobFactory`/`AdaptiveDelayTasklet` with the new `ChunkOrientedStep` model.
- Switch `@Jacksonized` to Jackson 3 only once Jackson 2 readers are gone (D11).
- Migrate `org.springframework.lang.Nullable` (270 files) to JSpecify.
- Remove `spring.jackson.use-jackson2-defaults` after checking the API wire format under the Jackson 3 defaults.
- Move the remaining standalone Jackson 2 utilities to Jackson 3.
- Upgrade jjwt to 0.13.
- Upgrade docx4j to 11.x (Jakarta JAXB), which would also remove the `jaxb-runtime` 2.3.8 pin that overrides Hibernate's 4.x.
- Remove `thymeleaf-extras-springsecurity6` if it is confirmed unused (no `sec:` in templates).
- `DatabaseConfiguration` TODO: move to the default DataSource/JPA auto-configuration.
- Upgrade to Gradle 9 and Java 21/25 separately.

---

## Appendix: files changed so far

**Build (Stage 2):** `server/build.gradle`
- Boot plugin changed to 4.1.2.
- Flyway pin removed.
- Starter changes:
  - `batch` → `batch-jdbc`
  - `web` → `webmvc`
  - added `webclient` and `restclient`
  - `spring-data-rest-webmvc` → `starter-data-rest`
  - `flyway-core` → `starter-flyway`
- Test starters: `webmvc-test`, `data-jpa-test` and `security-test` replace `starter-test` and `spring-security-test`.
- Testcontainers BOM removed, and the Testcontainers artifacts renamed.
- ShedLock changed to 7.10.1 and Hypersistence to `-73:3.16.0`.
- Explicit Jackson 2 `jackson-databind` added.

**New tests:**
- `util/http/RecordingHttpServer`
- `service/explanation/PythonServiceClientsJsonPayloadTest`
- `service/db/impl/SalesforceJsonPayloadTest`
- `service/ai/impl/AnthropicJsonPayloadTest`
- `service/db/impl/BatchJobServiceIntegrationTest`

**Tests added to existing classes:**
- `CandidateMatchExplanationIntegrationTest` (2)
- `ChatbotMessageRepositoryIntegrationTest` (3)
