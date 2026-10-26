# TC-1189 — Spring Boot 4 Migration Report

Branch: `john/tc-1189-upgrade-to-spring-boot-4` (base: `b1c367b9f` TC-1584)

| Stage | Status |
|---|---|
| 1. Assess and plan | Complete. Decisions D1–D10 approved. |
| Pre-migration baseline tests (requirements 1 and 2) | Complete on Boot 3.5.16: **3,577 tests, 0 failures** |
| 2. Build configuration | Complete. Decisions D11–D15 approved. |
| 3. Source fixes | **Complete.** D16–D20 approved and implemented. Main and test code compile. 3,591 tests, of which only the 8 `BatchJobServiceIntegrationTest` tests fail; they await the Stage 4 migration (§3.4). Checkpoint committed. (The 8 Batch failures were resolved in Stage 4.) |
| 4. Database migration and infrastructure | **Complete and approved.** V2_34 added. Fresh and existing (Spring Batch 5) databases are tested. **Full suite: 3,593 tests, 0 failures** (§3.5). D21 and D22 approved. |
| 5. Broader regression and application compatibility | **Complete.** One production bug found and fixed (candidate search, D26), plus an error-response format fix (D23). 15 new regression tests, most compared against Boot 3. Local runs of the application on both Boot 3 and Boot 4. **Full suite: 3,608 tests, 0 failures, 0 skipped** (run twice). New decisions D23–D27 (§4). **Awaiting review before Stage 6.** |
| 6 | Not started |

> **Deployment constraint:** the Spring Boot 4 application and the V2_34 migration **must be deployed together**.
> - Without V2_34, Spring Batch 6 cannot launch any job.
> - Once V2_34 has run, a Spring Boot 3 build cannot launch batch jobs either.
> - Rolling back to Spring Boot 3 therefore requires reversing the sequence rename by hand, outside Flyway, before or together with the rollback: `ALTER SEQUENCE batch_job_instance_seq RENAME TO batch_job_seq;`
>
> Details are in §4, under "Outstanding blockers".

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
- **Correction to Stage 1 (found in Stage 3):** 13 DTOs, not 25, use the Jackson 2 `@JsonNaming`. They are all Python-service DTOs (`service/embedding/dto`, `service/explanation/dto`). The Anthropic, Duolingo, Preset, reCAPTCHA and Salesforce classes use `@JsonProperty`, which Jackson 2 and 3 share.
- **Salesforce field naming (found in Stage 3):** Jackson 3 names properties differently from Jackson 2 when a getter starts with capitals (`getCVs_Folder__c()`: Jackson 2 `cvs_Folder__c`, Jackson 3 `CVs_Folder__c`), and by default it links such a getter to the matching public field. This removes the D14 duplicate fields. Only the `SalesforceServiceImpl` request classes have fields named like this. Decision D17.
- **jsonb (found in Stage 3):** Jackson 3 writes the same values as Jackson 2, but in alphabetical property order. That makes no difference to a `jsonb` column, which doesn't keep key order. Reading differs in two ways: a stored non-UTC offset is converted to UTC (same instant), and unknown properties are ignored (Jackson 2 failed). Neither jsonb column uses polymorphic types. Decision D18.
- **`JsonNode` text (found in Stage 3):** Jackson 3's `asString()` returns `""` for a JSON null (Jackson 2's `asText()` returned `"null"`) and throws for objects and arrays (Jackson 2 returned `""`). `JsonRows` (published Google Sheets) now reproduces the Jackson 2 text explicitly, with a test.
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
- **Found in Stage 3:** Batch 6 no longer defines a `JobRegistry` bean, so `BatchConfig` now defines a `MapJobRegistry`. As in Batch 5, it registers any `Job` beans; we have none.
- **Found in Stage 3:** the service's stop and restart methods are **not called from any endpoint** (no System Admin endpoint uses them). So D13 has no production effect. On Batch 6 the failing restart throws `IllegalArgumentException` ("The Job must not be null.") instead of `NoSuchJobException`, because `MapJobRegistry.getJob` now returns null.
- **Schema:** `migration/6.0/migration-postgresql.sql` in the Batch 6.0.6 jar contains only `ALTER SEQUENCE BATCH_JOB_SEQ RENAME TO BATCH_JOB_INSTANCE_SEQ;`. Our schema already has the Batch 5 table layout. **The next Flyway version is V2_34**: V2_33 is the latest both on this branch and on `origin/staging`. Re-checked in Stage 4 (§3.5): still free. The migration is `V2_34__rename_spring_batch_job_sequence.sql`.

### 2.5 Spring Security 7
- Main code compiled with no Security errors, including `BeanIds.AUTHENTICATION_MANAGER`, the TC-1584 `DaoAuthenticationProvider` changes and the TC-1583 WebSocket configuration.
- Unchanged from Stage 1: CSRF stays disabled for HTTP, and no `@EnableWebSocketSecurity` is used.
- **`"/status**"`:** I confirmed that Spring 6.2 and 7.0 `PathPattern` matching give the same results for this pattern. `StatusRequestMatcherTest` now pins it.
- **Found in Stage 3:** `AbstractValidatingPasswordEncoder.matches` is now `final`, so `TcPasswordEncoder` can no longer override it. Security 7's BCrypt encoder already returns false for a null (or empty) encoded password, so the override was removed and the class kept. One edge case changes: a **null raw password** now returns false, where Security 6 threw `IllegalArgumentException`. No production code path can pass a null raw password (`DaoAuthenticationProvider` rejects null credentials first, the partner API key is a path variable, and `PasswordHelper.isValidPassword` has no callers). Decision D16.

### 2.6 Spring Data JPA / Hibernate 7
- Main code compiled with no Data JPA or Hibernate errors. Specifications, `@Query`, `@Formula`, `@Immutable`, `@Type(JsonBinaryType)` and `CamelCaseToUnderscoresNamingStrategy` (still present in 7.4.13) all compile.
- **JAXB pin, investigated in Stage 3 (requirement 3). No runtime problem found; nothing changed.**
  - The resolved JAXB artifacts are **identical on Boot 3 and Boot 4** (compared using a temporary worktree of `HEAD`):
    - `jaxb-runtime` 2.3.8 (the explicit pin wins over Hibernate's 4.0.2 on Boot 3 and 4.0.7 on Boot 4);
    - `javax.xml.bind:jaxb-api` 2.3.1;
    - `jakarta.xml.bind-api` 4.0.5;
    - `txw2` 4.0.9.
  - So there is **no Jakarta JAXB implementation** on the classpath, now or before.
  - Hibernate 7.4 only creates a `JAXBContext` lazily, in three cases. We have none of them:
    - binding `orm.xml`, `hbm.xml` or `hibernate.cfg.xml` files (there are none in the repo);
    - mapping XML-typed columns (`SqlTypes.SQLXML` or an XML `FormatMapper`);
    - transforming `hbm.xml`.
  - At startup Hibernate only checks that the `jakarta.xml.bind` **API** class loads, and creates its XML format mapper without a `JAXBContext`.
  - Every Hibernate-booting test passes on Boot 4.
  - `DocxFormatterHelperTest` exercises docx4j's JAXB path (create a package, XHTML import, save) and passes.
  - Risk: if anyone later adds an XML mapping file or an XML column type, Hibernate would fail with "Implementation of Jakarta XML Binding-API has not been found". Upgrading docx4j to 11.x (Jakarta) removes this; it stays a follow-up, not changed here.
- **Hibernate settings:** `hibernate.order_by.default_null_ordering` is still supported. `hibernate.temp.use_jdbc_metadata_defaults` was already deprecated on Boot 3 and is still honoured. All other `spring.*` keys in `application.yml` are valid Boot 4 properties.
- **Entity id sequences (found in Stage 3, would have broken production):** all entities inherit `@GeneratedValue(generator = "seq_gen")` and declare their own `@SequenceGenerator(name = "seq_gen", …)`. Two entities don't:
  - `LanguageLevel` names its generator `language_level_gen`;
  - `SavedListLink` declares none.

  I added a probe to a temporary Boot 3 worktree and to Boot 4, which lists the sequence used by each of the 73 entities:
  - Hibernate 6.6 had silently used **another entity's sequence**: `language_id_seq` for `LanguageLevel`, `saved_list_id_seq` for `SavedListLink`.
  - Hibernate 7 falls back to a sequence literally named `seq_gen`, which doesn't exist. **Creating a language level would fail.**
  - The other 71 entities resolve identically.

  The two entities now declare those same sequences explicitly, so behaviour is unchanged. The new `EntityIdGeneratorIntegrationTest` pins the sequence of all 73 entities to the Hibernate 6.6 result. Decision D19 (FYI).
- Runtime risks to cover in Stage 5: Hibernate 7's stricter HQL validation (45 `@Query` files) and merge semantics.

### 2.7 Other
- **ShedLock 7.10.1** and **Hypersistence 3.16.0**: main code compiles against both with no changes.
- **Testcontainers 2:** `PostgresTestContainer` now uses `org.testcontainers.postgresql.PostgreSQLContainer` (not generic). It accepts the `pgvector/pgvector` image.
  - **Developer note (corrected in Stage 4):** the shared test container has a fixed name, `tc-postgres-integration`. If a test run starts before the previous run's container has been removed (for example, runs from two worktrees in quick succession), it fails with "Container startup failed". Run `docker rm -f tc-postgres-integration`. This is not caused by the Testcontainers version. Container reuse is ignored in both 1.21 and 2.0, because `testcontainers.reuse.enable` is only read from the environment or `~/.testcontainers.properties`, not from the classpath file. Hence the pre-existing "Reuse was requested but the environment does not support the reuse of containers" warning.
- **Found in Stage 3:** `ResponseEntity.getStatusCodeValue()` was removed in Spring 7; 8 tests now use `getStatusCode().value()`.
- **Found in Stage 3:** Boot 4 no longer initialises Mockito `@Mock` fields in tests run with `SpringExtension` (Boot's `MockitoTestExecutionListener` was removed). One test (`NotificationServiceImplTest`) relied on this. It now uses `MockitoExtension` in lenient mode, as before.
- **Found in Stage 3:** `SimpMessagingTemplate.convertAndSend(String, Map)` is now ambiguous with a new Spring 7 overload. `ChatPostServiceImpl` casts the payload to `Object`, so the map is still sent as the payload.
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

**Behaviour recorded by the baseline tests on Boot 3 (superseded on Boot 4 by D17):**
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

### 3.4 Stage 3: source fixes (Boot 4.1.2)

**Changes, grouped by cause:**

| # | Cause | Change |
|---|---|---|
| M1 | Batch 6 package moves | Imports updated in 10 main and 5 test files. |
| M1 | Batch 6 API (D5) | `BatchConfig`: one async `TaskExecutorJobOperator` (`SimpleAsyncTaskExecutor`), registered as `asyncJobOperator`, plus a `MapJobRegistry` bean. `BatchJobServiceImpl`: launches with `jobOperator.start`, and reads status from `JobRepository` (which replaces `JobExplorer`). It keeps the execution-id based `stop`/`restart` (deprecated) because they report unknown ids the same way as before. The test registers jobs with `jobRegistry.register(job)` (`ReferenceJobFactory` was removed). |
| M2 | Boot relocated classes | `TcFlywayConfig` and `DatabaseConfiguration` imports. |
| M3 | Security 7: `matches` is final | `TcPasswordEncoder` override removed (D16). |
| M4 | Spring 7 overload ambiguity | `ChatPostServiceImpl` payload cast. |
| M5 | Hibernate 7 generator resolution | `LanguageLevel` and `SavedListLink` sequences declared (D19). |
| J1 | Jackson 3: injected mappers (D3) | `CandidateReadDao`, `CandidateReadMapper`, `PublishedDocBuilderServiceImpl`, `CandidateDtoFetchServiceImpl` and `VerifyPlusPayloadParserImpl` now use Boot's Jackson 3 mapper. `JsonProcessingException` became `JacksonException`, and `isTextual`/`textValue` became `isString`/`stringValue`. |
| J2 | Jackson 3: snake_case DTOs | The 13 Python-service DTOs use Jackson 3's `@JsonNaming`. |
| J3 | Jackson 3: `JsonNode` text | `JsonRows` reproduces the Jackson 2 `asText()` output. |
| J4 | D11 | `server/lombok.config` generates the `@Jacksonized` annotations for Jackson 2 and 3 (the 16 warnings are gone). |
| J5 | D4 | `spring.jackson.use-jackson2-defaults: true` in `application.yml`. |
| C1 | Redis exclude | `application.yml` uses `spring.data.redis.repositories.enabled: false`. `application-test.yml` uses the Boot 4 class names. |
| T1 | Test annotations | 57 files: relocated `@AutoConfigureMockMvc`, `@WebMvcTest`, `@DataJpaTest` and `@AutoConfigureTestDatabase`. |
| T3 | Test auto-configuration | Relocated auto-configuration imports in the payload tests. |
| T4 | JUnit 4 | `assertThrows`/`assertFalse` imports changed to JUnit 5 (the calls take the same arguments). |
| T5 | Jackson 3 in tests | The 52 MockMvc tests autowire Boot's Jackson 3 `ObjectMapper`. 7 unit tests that imitated the application mapper now use `util/json/ApplicationJsonMapper`, which builds it through Boot's own `JacksonAutoConfiguration`. Left on Jackson 2 deliberately: `TextPartsCodecTest` (the class under test is Jackson 2), `model/sf/ContactTest`/`OpportunityTest`, and the chatbot evaluation tests. |
| T6 | Spring 7 / Boot 4 test API | `getStatusCodeValue()` (8 tests); `NotificationServiceImplTest` mock initialisation. |
| T7 | Testcontainers 2 | `PostgresTestContainer`. |
| L1 | Test isolation (D20) | `LogBuilderConfig` clears `LogBuilder`'s static `SystemMetrics` provider when its context closes. |

**New tests (Stage 3):**
- `CandidateMatchExplanationIntegrationTest` (+2):
  - nested jsonb with nulls and special characters is written with the same values as Jackson 2;
  - Jackson 2-written jsonb is read back unchanged.
  - The expected JSON was generated with the real Hypersistence 3.7.3 and Jackson 2.21.
- `JsonRowsTest` (+1): the `JsonNode` text output, checked against Jackson 2.21.
- `StatusRequestMatcherTest` (+9): the `/status**` matching, checked against Spring 6.2 and 7.0.
- `EntityIdGeneratorIntegrationTest` (+1): every entity's id sequence, against the Hibernate 6.6 result.
- `util/json/ApplicationJsonMapper`: a test helper.

**Requirement 1 (jsonb), Stage 3 additions:** the tests now cover:
- reading Jackson 2-generated jsonb (legacy, current and nested formats);
- writing equivalent JSON under Jackson 3 (exact values, plus Postgres `jsonb` equality);
- nested structures, and nulls at the top level, inside nested objects and inside arrays;
- special characters.

There is no polymorphic data: no `@JsonTypeInfo` anywhere in main code. The format differences found are listed in §2.3 and D18; nothing was changed to hide them.

**Requirement 2 (Salesforce):** Jackson 3 changes the duplicate-field output. I stopped and did not add a workaround. Approved as D17(a): the tests now pin the de-duplicated output.

**Requirement 3 (JAXB):** no runtime problem; nothing changed (§2.6).

**Test results:**

| Run | Tests | Failures | Notes |
|---|---|---|---|
| Focused: payload, jsonb, Batch, changed classes | 83 | 12 | Only the expected groups below |
| Explicit Testcontainers run: all 19 `BaseDBIntegrationTest`/`BaseJpaIntegrationTest` classes, plus `NotificationServiceImplTest`, `StatusRequestMatcherTest` and `DocxFormatterHelperTest` | 234 | 8 | All 8 are `BatchJobServiceIntegrationTest` (Stage 4 migration) |
| Full suite (first run) | 3,589 | 19 | 12 expected, plus 7 found and fixed: `NotificationServiceImplTest` (6), and `CandidateDataAuditingIntegrationTest` (1, the D19 sequence) |
| Full suite (second run) | 3,590 | 367 | Context-load failures depending on test order: `LogBuilder` test isolation (see below), found and fixed |
| Full suite (run twice before D16–D18) | 3,590 | 12 | The 8 Batch tests + 3 Salesforce (D17) + 1 password (D16) |
| Affected tests after D16–D18 (`TcPasswordEncoderTest`, `SalesforceJsonPayloadTest`, `CandidateMatchExplanationIntegrationTest`, `ChatbotMessageRepositoryIntegrationTest`, `CandidateMatchingExplanationDataJacksonTest`) | 41 | 0 | |
| **Full suite, checkpoint** (`./gradlew -Denv=dev :server:compileJava :server:compileTestJava :server:test --rerun-tasks`) | **3,591** | **8** | All 8 are `BatchJobServiceIntegrationTest` (Stage 4 migration). 3,591 = the 3,577 baseline + 14 new tests. No compile errors. |

**Remaining failures at the checkpoint:**
- Only `BatchJobServiceIntegrationTest` (8): `relation "batch_job_instance_seq" does not exist`. This needs the V2_34 migration in Stage 4.

No test was disabled and no assertion was weakened. Apart from the approved D16–D18 expectation changes, test changes are import or API moves only (plus the `NotificationServiceImplTest` runner). The tests that imitated the application's mapper now use the real application mapper configuration.

### 3.5 Stage 4: database migration and infrastructure

**Flyway version check.** I fetched `origin` before creating the migration (2026-10-26). V2_33 is the latest migration on this branch and on `origin/staging`, and no commit on any of the 1,086 remote branches adds V2_34 or later. The query was confirmed by finding V2_33 the same way. Re-check before merging if staging moves on.

**Schema comparison:**
- Our Batch tables (created in `V2__init.sql`) match Spring Batch 6.0.6's `schema-postgresql.sql`: the same 6 tables, columns, keys and constraints.
- Spring Batch's own `migration/6.0/migration-postgresql.sql` is the single statement `ALTER SEQUENCE BATCH_JOB_SEQ RENAME TO BATCH_JOB_INSTANCE_SEQ;`.
- Execution contexts are serialized the same way in Batch 5 and Batch 6 (`DefaultExecutionContextSerializer`, in both `DefaultBatchConfiguration` 5.2.6 and `JdbcDefaultBatchConfiguration` 6.0.6).

**Migration:** `server/src/main/resources/db/migration/V2_34__rename_spring_batch_job_sequence.sql`

```sql
-- Spring Batch 6 uses batch_job_instance_seq for job instance ids instead of batch_job_seq.
-- Renaming keeps the current sequence value, so new job instance ids continue from the existing
-- ones. This is the only schema change between Spring Batch 5 and 6 - see Spring Batch's
-- org/springframework/batch/core/migration/6.0/migration-postgresql.sql (TC-1189).
ALTER SEQUENCE batch_job_seq RENAME TO batch_job_instance_seq;
```

I deliberately left out `IF EXISTS`: if the sequence were missing, the migration should fail rather than leave Batch unable to launch jobs. No other schema change was made.

**Genuine Spring Batch 5 metadata.** I didn't want to rely on hand-written rows, so I generated the test fixture `server/src/test/resources/batch/spring-batch-5-metadata.sql` with the real Batch 5 code:
- I ran real jobs on Spring Batch 5.2.6 (Boot 3.5.16, in a temporary worktree of `734b383b5`, since deleted).
- I then exported the batch tables with `pg_dump --data-only --column-inserts`.

It contains:
- 4 job instances and executions: COMPLETED (a chunk step, with typed String, Long and LocalDate parameters plus a non-identifying one), FAILED (with a stack-trace exit message), STOPPED (restartable, with progress in its step context), and one launched through `BatchJobService.launchJob`;
- their serialized job and step execution contexts;
- the sequence values.

**New test: `integration/batch/SpringBatchSchemaMigrationIntegrationTest`** (2 tests). It uses its own `pgvector` PostgreSQL container, runs Flyway with the application's settings, and uses Spring Batch 6's real JDBC job repository.

*Fresh database:*
- Flyway creates everything from an empty database, including V2_34.
- The batch sequences are exactly `batch_job_instance_seq`, `batch_job_execution_seq` and `batch_step_execution_seq`.
- Every Batch table and column equals Spring Batch 6's own `schema-postgresql.sql` (parsed from the jar).
- A job launches and completes (instance and execution id 1), and its metadata is read back.

*Existing database:*
- Migrate to V2_33, load the Batch 5 fixture, then migrate to V2_34.
- **All six Batch tables are unchanged**, compared row by row as JSON before and after.
- `batch_job_instance_seq` has exactly the old `batch_job_seq` value (`last_value = 4`, `is_called = true`), and the other two sequences are unchanged.
- Batch 6 reads every existing instance, execution, parameter (with types and the identifying flag), job execution context (`batch.version = 5.2.6`) and step context (`reader.position`, `progress`). The failure message and the STOPPED status are intact.
- `BatchJobServiceImpl.getJobExecutionsSummary()` (System Admin status reporting) lists the existing history.
- New jobs and executions get ids 5, 6 and 7, with no collision with existing rows.
- **Finding (D21):** Batch 6 computes job keys differently from Batch 5, so a job instance created before the upgrade isn't found from its parameters. The test records the consequences:
  - a once-per-day job that ran before the upgrade can run once more on the upgrade day (after that the restriction applies again);
  - restarting an execution stopped under Batch 5 starts a new instance from the beginning instead of resuming.
  - Neither is reachable in production (see D21).

**`BatchJobServiceIntegrationTest` (pre-migration characterization tests), on the migrated shared test database:**
- **All 8 pass:** asynchronous launch, status summary, stop, stopping a job that isn't running, restart of a registered stopped job (a new execution of the same instance, which completes), once-per-day blocking, and non-once-per-day allowing a rerun.
- The D13 test (restarting an unregistered job fails, and no execution is created) needed one change: Spring Batch 6 rejects the restart with `IllegalArgumentException("The Job must not be null.")` instead of `NoSuchJobException` (D22). I kept the substance of the test and added a check that the stopped execution stays STOPPED.
- The shared test database is restored from `dump.sql` (at V2.18) and migrated by Flyway through V2.34, so the whole integration suite also runs on an upgraded database.

**Infrastructure checks:**

| Component | Result |
|---|---|
| Flyway 12.4.0 | Migrates from empty (fresh test), from V2.18 (shared test DB: "Successfully applied 16 migrations … now at version v2.34") and from V2.33 (existing test). No Flyway warnings. |
| Testcontainers 2.0.5 | Shared container and a separate container both work. The "reuse not supported" warning is pre-existing (see the developer note in §2.7). |
| Spring Batch 6 JDBC | Metadata is persisted and read through `JdbcJobRepository`, on both a fresh and an upgraded database. |
| Hibernate 7 startup | All full-context tests start, so every `@Query` parses. |
| Hibernate 7 schema validation | The application doesn't enable validation (no `ddl-auto`, on Boot 3 or 4). I ran it once via an environment variable: it stops at a **pre-existing** mismatch (`audit_log.event_date` is a `date` column mapped as a timestamp), and Boot 3 / Hibernate 6.6 reports exactly the same error. For a complete comparison I had Hibernate 6.6 and 7.4 each write the DDL they expect for all entities. After normalizing formatting, **all 77 tables have identical columns and types**. Hibernate 7 only adds three DDL-only constraints (two discriminator checks and one unique constraint), which matter only for schema generation, which we don't use. |
| ShedLock 7.10.1 | No existing tests cover it. Every full-context test creates the `JdbcTemplateLockProvider` bean. A throwaway check (not kept) acquired a lock on the migrated `shedlock` table, was refused a second lock, then unlocked and re-acquired. |
| Log warnings | Only pre-existing ones remain: HHH000502 for `SalesforceJobOpp.matching` (46 times, identical on Boot 3), the explicit `hibernate.dialect`, and the deprecated `use_jdbc_metadata_defaults`. |

**Test results (Stage 4):**

| Run | Tests | Failures |
|---|---|---|
| `SpringBatchSchemaMigrationIntegrationTest` | 2 | 0 |
| `BatchJobServiceIntegrationTest` | 8 | 0 |
| Explicit Testcontainers run: all 18 `BaseDBIntegrationTest`/`BaseJpaIntegrationTest` classes plus the migration test (before the D22 test change) | 202 | 1, the D22 exception type, since fixed |
| **Full suite** (`./gradlew -Denv=dev :server:compileJava :server:compileTestJava :server:test --rerun-tasks`) | **3,593** (354 classes) | **0** (0 skipped) |

3,593 = the 3,591 at the checkpoint + the 2 migration tests. No test was disabled or skipped.

### 3.6 Stage 5: broader regression and application compatibility

**Method.** Wherever possible, a new test was **run on Boot 3.5.16 first** (in a temporary worktree of `734b383b5`, since deleted). That recorded the previous behaviour, and the same test was then run on Boot 4. So "unchanged" below means compared against real Boot 3 output, not judged by eye. Throwaway probes and worktrees were deleted, and only the regression tests listed here are kept.

#### 1. Verified by automated tests

| Area | Test (new unless stated) | What it checks | Result |
|---|---|---|---|
| Security 7: HTTP authorization | `integration/security/HttpAuthorizationMatrixIntegrationTest` | Builds requests for **every path pattern in `SecurityConfiguration`** (parsed from the file) plus extra paths, for each relevant HTTP method: **367 requests**. Each is sent through the real security filter chain as 9 personas: anonymous, an invalid JWT, and **real JWTs** for `user`, `limited`, `semilimited`, `partneradmin`, `admin`, `systemadmin` and read-only users. It records permit, 401 or 403; controllers don't run. | **All 3,303 decisions are identical to Boot 3 / Security 6.5** (expected file `security/http-authorization-matrix.txt` recorded on Boot 3). |
| Security 7: JWT and login, TOTP | `integration/api/ApiJsonCompatibilityIntegrationTest` | Real login: success, wrong password, unknown user. **MFA login** without a code, with a wrong code, and with a **valid TOTP code** from the real TOTP library. Requests with and without a JWT. | Identical to Boot 3 |
| Security 7: WebSocket/STOMP (TC-1583) | existing `WebSocketSecurityConfigTest` | JWT CONNECT, CSRF, subscribe and send rules | Passes |
| Jackson 3: REST API JSON | `ApiJsonCompatibilityIntegrationTest` | A golden master of real HTTP exchanges through the filter chain, controllers, message converters and database: login, user, a paged and sorted search, candidate (simple and extended DTOs), 404, validation error, malformed JSON, a wrongly typed value, `/api/hal` (root, collection, item, the admin portal's request, and a refused write). Volatile values are normalized, keeping their formats. | Identical to Boot 3 **after D23 and D26**, except for D24 (the HAL content type when no `Accept` header is sent). |
| Jackson 3: candidate search | `integration/api/CandidateSearchIntegrationTest` | The admin portal's flow: get the default saved search, then search. Results are read from **database-built candidate JSON** into `CandidateReadDto`. | **Failed on Boot 4 (production bug, D26); passes after the fix.** The response JSON is identical to Boot 3, including key order. |
| Jackson 3: jsonb, Python services, Salesforce, Anthropic | from Stages 2–3 | Payload and storage formats | Pass |
| Hibernate 7 / Data JPA 4: queries | `integration/repo/RepositoryQueryCompatibilityIntegrationTest` | Calls **every read-only method of all our repositories (278)**, covering derived queries, JPQL and **native SQL**, with representative arguments: real entities, enums, dates, collections and `Pageable`. Each call runs in its own rolled-back transaction. | **All 278 outcomes are identical to Hibernate 6.6**, including result types in a detailed one-off comparison. 275 succeed. 3 fail identically on both versions; these are pre-existing (§ observations). |
| Hibernate 7: native result mapping with data | `integration/api/CandidateStatsIntegrationTest` | Every candidate statistic, over all candidates and over a list. These native queries return `Object[]` rows that our code casts (`(Long) row[1]`), and no Gradle test ran them with data before. | Pass on Boot 3 and Boot 4 |
| Hibernate 7: persistence behaviour | `integration/repo/PersistenceBehaviourIntegrationTest` | Insert then update of entities with **assigned composite ids** (`CandidateProperty`, `JobChatUser`), which Spring Data does with `merge`. Also updating a detached entity, dirty checking, and lazy loading inside and outside a transaction (`LazyInitializationException`). | Pass on Boot 3 and Boot 4. No entity uses `@Version`, so Hibernate 7's stricter merge rule for versioned entities does not apply. |
| Hibernate 7: pagination and sorting | the repository sweep and API golden master | `Pageable` queries, paged and sorted user search | Identical |
| Hibernate 7: id sequences, schema | from Stages 3–4 | | Pass |
| Spring Batch 6: the real candidate-text job | `integration/batch/CandidateTextJobIntegrationTest` | `POST /api/admin/system/set_candidate_text/list-{id}-cpu-100` launches the **real job** asynchronously. It reads the list's candidates, updates their `text`, writes them, and the result appears in the status summary. | Pass on Boot 3 and Boot 4 |
| Spring Batch 6: lifecycle | `BatchJobServiceIntegrationTest` (changed), `SpringBatchSchemaMigrationIntegrationTest` | Async launch, status, stop, restart, once-per-day, D13, D21 | Pass. See D27 for the change in stop semantics. |
| Redis (Spring Data Redis 4, Lettuce 7) | `integration/cache/RedisIntegrationTest` | Against a **real Redis server** (`redis:7.2.4`). The other tests turn caching off. It checks the `users` cache read and write (the value served from Redis after the database row is deleted), the fallback to the database for an unreadable cached value, and the candidate JSON cache (put, versioned get, clear). | Pass on Boot 3 and Boot 4 |
| ShedLock 7 with scheduled jobs | `integration/cache/ShedLockSchedulerIntegrationTest` | `ResourceExpiryScheduler.markResourcesAsExpired()` takes the 23-hour lock in the `shedlock` table. A second run while locked does nothing. After release it runs and expires an overdue resource. | Pass on Boot 3 and Boot 4 |
| DOCX generation | existing `DocxFormatterHelperTest` | docx4j with JAXB | Pass |

**Test-infrastructure findings.** These were found while running the new tests; none are production changes:
- **The shared test database is re-imported from `dump.sql` before every test class.** The import resets all sequences (`setval(…, 1, false)`), including `batch_job_execution_seq`. So tests that commit Batch metadata must remove it, or later classes collide. A new `integration/helper/BatchMetadataCleaner` is used by `CandidateTextJobIntegrationTest` and `BatchJobServiceIntegrationTest`. The other new tests that commit data clean it up.
- **`CpuUsageTest`** (existing) failed twice in about ten runs: the Micrometer CPU gauge was `NaN` because the JVM had no CPU-load sample at that moment. It now waits up to 5 seconds for a sample, then applies the same 0–1 assertion.
- `ApplicationJsonMapper` (the test helper from Stage 3) now loads the real `spring.jackson` settings from `application.yml`.

#### 2. Verified by running the application locally

I ran the Boot 4 jar against **throwaway** PostgreSQL (restored from `dump.sql`) and Redis containers on separate ports. Your docker-compose development database was **not** used, because V2_34 would rename its Batch sequence. I also ran the **Boot 3** jar the same way for comparison. Everything was removed afterwards.

| Check | Boot 4 result |
|---|---|
| Startup | Starts in about 8.5 s. **Flyway 12 migrated the restored database through V2.34.** Health is UP, and `/actuator/info` shows the commit. |
| Startup warnings | **Identical to the Boot 3 jar**: the same 5 warnings, all pre-existing (Hibernate dialect and `use_jdbc_metadata_defaults` deprecations, `open-in-view`, a macOS Netty DNS notice, a duplicate JVM gauge). |
| Admin login, JWT, wrong password, no token | 200 / 401 / 401 |
| Candidate self-registration and portal login | Works. A candidate's JWT gets 403 on admin endpoints. |
| Portal endpoints (`personal`, `contact`) | Work |
| Admin user, country, candidate, statistics endpoints | Work |
| Candidate search (native SQL JSON, **real Redis** cache) | **Failed** ("Could not deserialize cached JSON") until the D26 fix. Afterwards: the first search reads from the database, the second from Redis. |
| CV download as **DOCX** and **PDF** | A valid Word document (13 parts, containing the candidate number and email) and a 1-page PDF |
| `/api/hal` as the admin portal requests it | 200 `application/json` |
| **WebSocket/STOMP chat over SockJS** | With the admin's JWT: CONNECT, SUBSCRIBE, SEND, and the post is stored and broadcast. Without a JWT: CONNECT is accepted, SUBSCRIBE is refused (TC-1583 rules). **The broadcast message JSON is identical to the Boot 3 jar** (same fields, order, date format and characters). |
| Batch: candidate-text job from System Admin | Launched, COMPLETED, read and write count 1, the candidate's text updated, using `batch_job_instance_seq` on the V2_34 database |
| Rolling deploy (Boot 3 and Boot 4 sharing Redis) | Boot 4 could not read a Boot 3-written `users` cache entry. It logged "Cache get failed … falling through to database" once and continued correctly (D25). |
| `timestamptz` read from the database | UTC on both Hibernate 6.6 and 7.4 (checked with a probe), so no change in API date offsets |

#### 3. Still requires manual testing (external services or the Angular UI)

Passing serialization tests don't prove these integrations work; each has to be checked against the real service:
- **Salesforce:** sync against the **sandbox**, including the de-duplicated request bodies (D17, mandatory before merging), authentication, upserts, opportunity sync (the `JobOppSyncFromSalesforce` scheduled job) and response handling.
- **Python explanation and embedding services:** real calls (request and response formats are covered by tests).
- **Anthropic** and **Duolingo** calls, and other external HTTP clients (reCAPTCHA, Preset, Google Drive/Docs, AWS S3/SES, Slack).
- **Angular UI:** both portals end to end, in particular:
  - login, including 2FA setup and login;
  - candidate search and saved searches;
  - lists and stats;
  - chat (WebSocket);
  - CV export;
  - candidate property definitions (`/api/hal`);
  - System Admin batch launch.
- **Deployment environment:** startup with production configuration and secrets, Redis (ElastiCache), and scheduled jobs with ShedLock across several instances.
- **Copy of production data:** candidate search and stats on realistic data, and Batch status on real Batch 5 history.

---

## 4. Issues and decisions

### Approved decisions (Stage 1)
| # | Decision | Decision |
|---|---|---|
| D1 | Boot 4.1.2 | Approved, implemented |
| D2 | Keep Gradle 8.14.4 and Java 17 | Approved, implemented |
| D3 | Jackson 3 for Spring; Jackson 2 only where needed; no `spring-boot-jackson2` | Approved. **See D12 amendment** |
| D4 | `spring.jackson.use-jackson2-defaults=true` for TC-1189 | Approved, implemented (Stage 3) |
| D5 | Single async `JobOperator` plus `JobRepository`; keep the deprecated chunk classes | Approved, implemented (Stage 3) |
| D6 | Flyway: remove the pin and use the managed 12.4.0 | Approved, implemented |
| D7 | ShedLock 7.10.1, Hypersistence `-73:3.16.0`, Testcontainers 2 artifact names | Approved, implemented |
| D8 | Keep jjwt, docx4j and TOTP | Approved, implemented |
| D9 | Explicit modular starters | Approved, implemented |
| D10 | Report location | Done |

### Decisions approved after Stage 2
| # | Decision | Recommendation |
|---|---|---|
| D11 | Lombok `@Jacksonized` variant (`server/lombok.config`) | Generate **both** the Jackson 2 and Jackson 3 builder annotations (`lombok.jacksonized.jacksonVersion += 2` and `+= 3`) for the duration of the migration. The DTOs are read by Spring and Hypersistence (Jackson 3), and the existing Jackson 2 unit tests still read them too. Switching to Jackson 3 only can be a follow-up. |
| D12 | Amend D3: Hypersistence (jsonb columns) uses Jackson 3 | Accept, since there is no Jackson 2 option for Hibernate 7.3+. The new persisted-format tests (§3.1) must keep passing, so stored jsonb stays readable and is written in the same format. The alternative, replacing Hypersistence with Hibernate's native `@JdbcTypeCode(SqlTypes.JSON)`, is a larger change and not recommended for TC-1189. |
| D13 | Pre-existing: restarting factory-built batch jobs fails (`NoSuchJobException`) | **Preserve the current behaviour in TC-1189**, with the characterization test, and fix it in a separate ticket. Fixing it changes production behaviour, which is outside a like-for-like upgrade. Batch 6's `TaskExecutorJobOperator.restart` still uses the registry, so behaviour stays the same either way. |
| D14 | Pre-existing Salesforce duplicate fields (`RecordType`, `EmployerOpportunityRequest`) | **Superseded by D17**, since Jackson 3 removes the duplicates. Originally: keep the current output in TC-1189. If Jackson 3 changes it, report it before changing anything. Clean it up in a separate ticket. |
| D15 | Starters added beyond the Stage 1 plan: `webclient`, `restclient`, `data-rest` and `security-test` (§2.1) | These are routine compatibility changes needed to keep current behaviour, so they are already applied. Listed here for visibility. |

D11–D15 were approved.

### Decisions approved after Stage 3
| # | Decision | Options | Recommendation |
|---|---|---|---|
| D16 | `TcPasswordEncoder`: a null raw password now returns false instead of throwing (§2.5) | (a) Accept Security 7's behaviour and change `TcPasswordEncoderTest.matches_withNullRawPassword_throwsIllegalArgumentException` to expect false. (b) Stop extending `BCryptPasswordEncoder` and wrap it, so we can keep throwing. | **(a).** No production code path passes a null raw password, and returning false is the safer result. I have **not** changed the test: it currently fails, pending your decision. |
| D17 | **Salesforce duplicate fields (D14): Jackson 3 cannot reproduce them reliably.** Today Jackson 3 drops every duplicate: `RecordType` is sent as `{"Name": …}` only, and `EmployerOpportunityRequest` sends each field once under its exact Salesforce name (`CVs_Folder__c`, `Job_Description_Folder__c`, `Talent_Catalog_List__c`, `TCid__c`). Values and all other fields are unchanged. Jackson 3's Jackson 2 compatibility mode restores the `RecordType` duplicate but only one of the four `EmployerOpportunityRequest` duplicates. Response decoding is unchanged (all decoding tests pass). | (a) **Accept the de-duplicated output**: update the three Salesforce payload tests to pin it, and verify against the Salesforce sandbox in Stage 6. (b) **Keep Salesforce on Jackson 2**: give the Salesforce `WebClient` Spring's `Jackson2JsonEncoder`/`Decoder`. They are deprecated for removal in Spring 7, and this contradicts D3. (c) Reproduce the duplicates with explicit annotations: a deliberate workaround, not recommended. | **(a).** The lower-case duplicates were an accident of Jackson 2's naming. Salesforce treats field names case-insensitively, and the same values are sent under the correct API names. This is the clean-up D14 had planned for later. If you want zero wire change in TC-1189, (b) is the reliable alternative. **No workaround applied: the three tests currently fail.** |
| D18 | jsonb read differences under Jackson 3 (Hypersistence, D12): a stored non-UTC `generatedAt` offset is returned as UTC (same instant); unknown properties are ignored instead of failing. Write output is identical apart from key order, which `jsonb` doesn't keep. | (a) Accept. (b) Configure a custom Hypersistence `ObjectMapperSupplier` that restores the Jackson 2 behaviour. | **(a).** `generatedAt` comes from the Python service through the RestClient, whose mapper already converted it to UTC on Boot 3, so stored values are UTC in practice. Ignoring unknown properties only makes reading more tolerant. The new tests pin all values and the instant. |
| D19 | (FYI, applied) `LanguageLevel` and `SavedListLink` id sequences (§2.6) | (a) Keep the sequences Hibernate 6.6 actually used, declared explicitly. (b) Move them to `language_level_id_seq` and `saved_list_link_id_seq`, which needs a migration to advance those sequences past the existing ids. | **(a), applied**, because it keeps production behaviour identical. (b) is a follow-up. |
| D20 | (FYI, applied) `LogBuilder` static provider | `LogBuilder` keeps a static `ObjectProvider` from the application context. After Spring's test context cache closed that context, later test contexts (in particular `@WebMvcTest` slices, which don't include `LogBuilderConfig`) failed whenever code logged through `LogBuilder`. Boot 4 now looks up beans through the closed context, which throws "has been closed already"; Boot 3 tolerated it. The failures depend on test order: 0 in one run, 355 in another. | **Applied:** `LogBuilderConfig` clears the provider (only if it is still its own) when its context closes. In production this only affects logging after shutdown has started, which then omits the CPU and memory fields. Two consecutive full runs are clean. |

D16, D17 and D18 were approved with option (a), and D19 and D20 as applied. **Salesforce sandbox verification (D17) is mandatory before merging.** Implementation:
- D16: `TcPasswordEncoderTest.matches_withNullRawPassword_returnsFalse` (renamed from `…_throwsIllegalArgumentException`) now expects false.
- D17: the three `SalesforceJsonPayloadTest` tests now pin the de-duplicated bodies: `RecordType` `{"Name": …}`, and each `EmployerOpportunityRequest` field once under its Salesforce name.
- D18: `CandidateMatchExplanationIntegrationTest` now pins the UTC conversion exactly, and a new test (`explanationJsonWithUnknownPropertiesIsRead`) pins that unknown properties are ignored.

### Decisions approved after Stage 4
| # | Decision | Options | Recommendation |
|---|---|---|---|
| D21 | **Spring Batch 6 job keys differ from Batch 5.** The key is an MD5 of the identifying parameters; Batch 5 hashed `name={value=…, type=…, identifying=true};`, Batch 6 hashes `JobParameter{name='…', value=…, …}`. So Batch 6 doesn't find job instances created before the upgrade from their parameters. Consequences: (1) a once-per-day job which already ran on the upgrade day can run once more that day; (2) restarting a pre-upgrade STOPPED/FAILED execution starts a new instance from the beginning. Existing history is still read correctly. | (a) **Accept.** (b) Keep Batch 5 keys with a custom `JobKeyGenerator`. Boot 4's batch auto-configuration doesn't expose one, so this means replacing Boot's batch configuration, a permanent divergence from Batch 6. (c) A migration that recomputes `job_key` for existing instances in SQL. | **(a).** It has **no production effect**: the only production launch (`SystemAdminApi`, candidate text update) uses `onlyOncePerDay = false`, which adds a timestamp so every run is a new instance anyway, and restart isn't exposed (D13). The migration test records this behaviour. |
| D22 | D13 test: restarting an unregistered job now fails with `IllegalArgumentException("The Job must not be null.")` instead of `NoSuchJobException` (noted in §2.4 during Stage 3) | (a) **Accept** Spring Batch 6's exception: the test now expects it and still asserts that the restart fails, no execution is created and the old execution stays STOPPED. (b) Make `BatchJobServiceImpl` look up the registry and throw `NoSuchJobException`, which adds code to an unused path. | **(a), applied** pending your confirmation. The behaviour D13 preserves (restart fails, nothing runs) is unchanged, and no endpoint calls this method. |

D21 and D22 were both approved with option (a).
- **D21 (known limitation, kept):** job instances created by Spring Batch 5 are not found again from their parameters. On the upgrade day, a once-per-day job that has already run may run once more. Restarting a stopped or failed pre-upgrade execution starts a new instance from the beginning. Existing history is still readable. Neither case affects current production workflows. `SpringBatchSchemaMigrationIntegrationTest` records this behaviour as a regression test.
- **D22:** restarting an unregistered job now fails with `IllegalArgumentException`. `BatchJobServiceIntegrationTest.restartUnregisteredJobFails` still verifies that no new execution is created and that the stopped execution stays STOPPED.

### New decisions requiring approval (Stage 5)
| # | Decision | Options | Recommendation |
|---|---|---|---|
| D23 | **Error responses from `ErrorHandler` (`ErrorDTO`) were written by Spring Data REST's own `JsonMapper`.** That mapper pretty-prints and ignores our `spring.jackson` settings, because Spring Data REST's exception resolver picks up message converter beans and Boot 4 no longer defines them. | (a) Restore Boot 3 behaviour: give that resolver the application's `JsonMapper` through Spring Data REST's own hook (`RestRepositoryConfig.configureExceptionHandlerExceptionResolver`). (b) Accept: the JSON is equivalent but pretty-printed. | **(a), applied** pending your confirmation. Error responses are now byte-for-byte as on Boot 3 (golden master). |
| D24 | **HAL content type:** with no `Accept` header, `/api/hal` now answers `application/vnd.hal+json` instead of `application/hal+json` (Spring HATEOAS 3 / Data REST 5). The admin portal's request (`Accept: application/json, text/plain, */*`) still gets `application/json` and an identical body. | (a) Accept. (b) Force the old media type in `RestRepositoryConfig`. | **(a).** No client is affected. The golden master records the new value. |
| D25 | **The Redis `users` cache is not readable across the upgrade.** It stores Java-serialized `User` entities, and Hibernate 7 changed the `serialVersionUID` of its collection classes. After deployment, and in both directions during a rolling deployment, each stale entry logs one "Cache get failed … falling through to database" error. The correct user is then loaded from the database and re-cached. The TTL is 5 minutes. | (a) Accept the transient error logs. (b) Clear the `users::*` keys in Redis as part of the deployment. | **(b)** if convenient, otherwise (a). It is harmless either way. The candidate JSON cache is unaffected (it stores plain JSON strings). |
| D26 | **Production bug, fixed: candidate search failed under Jackson 3.** `CandidateReadDto` has only Lombok's package-private `@Builder` constructor. Jackson 2 created it through that constructor (parameter names module, creator visibility `ANY`). Jackson 3 doesn't, even with Boot's Jackson 2 defaults, which turn parameter-name detection off. | (a) Restore Jackson 2's behaviour globally in `application.yml`: `spring.jackson.mapper.detect-parameter-names: true` and `spring.jackson.visibility.creator: any`. (b) Fix `CandidateReadDto` alone (eg add `@NoArgsConstructor` or `@Jacksonized`), leaving any other class that relies on the same behaviour broken. | **(a), applied** pending your confirmation. It restores Boot 3 behaviour for every class. The search response, error responses and all golden masters now match Boot 3 (apart from D24). |
| D27 | **Spring Batch 6 stop semantics:** `stop()` now records **STOPPED immediately**, while the job thread is still running (it stops at the next chunk boundary). Batch 5 recorded STOPPING until the job had actually stopped. So a restart soon after a stop could run alongside the old execution. | (a) Accept and record it. (b) Work around it in `BatchJobServiceImpl`. | **(a).** Neither stop nor restart is called by any endpoint (D13). The Stage 2 restart test now waits for the old execution's steps to stop before restarting. |

### Outstanding blockers
- None. D23–D27 need confirmation.
- **Deployment constraint: the V2_34 migration and the Boot 4 code must be deployed together.** On a database without V2_34, Batch 6 can't launch jobs. Once V2_34 has run, an older Boot 3 build could no longer launch batch jobs either, because `batch_job_seq` is gone. So a rollback to the previous release would need `ALTER SEQUENCE batch_job_instance_seq RENAME TO batch_job_seq;` (manually, outside Flyway).

### Changes that may affect production behaviour
- Jackson 3 becomes the serializer for HTTP, RestClient, WebClient **and the Hypersistence jsonb columns** (D12).
- Flyway 11 → 12.
- The Batch sequence rename migration (V2_34). It keeps all metadata and the sequence value (§3.5), but it is a one-way step for older builds (see the blockers above).
- Job instances from before the upgrade aren't matched by parameters (D21; no production effect).
- Hibernate 7 query and merge semantics.
- Batch job metadata depends on `spring-boot-starter-batch-jdbc`. With the plain batch starter, metadata would no longer be persisted.
- Salesforce request bodies no longer contain the duplicate fields (D17). This needs verifying in the Salesforce sandbox before merging.
- jsonb reads: UTC offsets and tolerance of unknown properties (D18).
- A null raw password returns false (D16; not reachable in production).
- Restored Jackson 2 creator detection (D26) and error-response serialization (D23). These keep the behaviour unchanged, so they aren't changes, but they are configuration that must stay in place.
- HAL content type without an `Accept` header (D24); Redis `users` cache entries invalidated by the upgrade (D25); STOPPED recorded immediately on stop (D27).

### Manual verification requirements (see §3.6 part 3 for the full list)
- Chat over WebSocket from the Angular clients.
- Explanation and embedding calls to the Python services.
- Salesforce sync against the sandbox (in particular the de-duplicated payloads, D14/D17).
- Creating a language level (D19).
- Running a batch job from System Admin (the candidate text update), and checking the job status summary on a copy of production data, which contains real Batch 5 history.
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
- Replace the deprecated Batch 6 chunk classes in `CandidateBatchJobFactory`/`AdaptiveDelayTasklet` with the new `ChunkOrientedStep` model.
- Switch `@Jacksonized` to Jackson 3 only once Jackson 2 readers are gone (D11).
- Migrate `org.springframework.lang.Nullable` (270 files) to JSpecify.
- Remove `spring.jackson.use-jackson2-defaults` after checking the API wire format under the Jackson 3 defaults.
- Move the remaining standalone Jackson 2 utilities to Jackson 3.
- Upgrade jjwt to 0.13.
- Upgrade docx4j to 11.x (Jakarta JAXB), which would also remove the `jaxb-runtime` 2.3.8 pin that overrides Hibernate's 4.x. Until then, adding a Hibernate XML mapping or an XML column type would fail (§2.6).
- Move `LanguageLevel` and `SavedListLink` to their own id sequences, with a migration that advances them past the existing ids (D19).
- Replace the deprecated execution-id `JobOperator.stop`/`restart` (Batch 6), or remove them, since no endpoint uses them (D13).
- Rename `hibernate.temp.use_jdbc_metadata_defaults` to `hibernate.boot.allow_jdbc_metadata_access`.
- Move `model/sf` `ContactTest`/`OpportunityTest` to Jackson 3, which is what Salesforce decoding now uses.
- Fix the pre-existing `audit_log.event_date` type mismatch (`date` column mapped as a timestamp), then consider enabling `hibernate.hbm2ddl.auto=validate`, so that future mapping and schema drift is caught at startup.
- Remove the explicit `hibernate.dialect` setting (Hibernate warns it is unnecessary).
- Consider storing the Redis `users` cache as JSON rather than Java serialization, so cache entries survive library upgrades (D25).
- `dump.sql` is restored before every integration test class and resets all sequences. Consider loading it once per test run (test infrastructure).
- Pre-existing, found by the repository sweep:
  - `SavedListLinkRepository`'s `saved_list_link` table doesn't exist (the entity is marked as possible zombie code);
  - `CandidateRepository.findByIdIn(String, Pageable)` binds a SQL fragment as a parameter, so it fails with a syntax error for any value. It is unused (both callers pass a collection, which uses the other overload), so it can be removed.
- Make `CandidateReadDto` (and similar Lombok `@Builder` DTOs) explicitly deserializable (eg `@Jacksonized`) so that it doesn't rely on D26's global settings.
- Add tests for ShedLock-protected scheduled jobs (none exist).
- Give the shared test container a unique name or enable reuse properly, to avoid "Container startup failed" when test runs overlap.
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

**Stage 3 (source):**
- Main code:
  - Batch: `BatchConfig`, `BatchJobService`, `BatchJobServiceImpl`, `SystemAdminApi`, `CandidateBatchJobFactory`, `CandidateReader`, `CandidateWriter`, `AdaptiveDelayTasklet`, `LoggingChunkListener`, `LoggingJobExecutionListener`.
  - Boot relocations: `TcFlywayConfig`, `DatabaseConfiguration`.
  - Security and messaging: `TcPasswordEncoder`, `ChatPostServiceImpl`.
  - Logging: `LogBuilder`, `LogBuilderConfig` (D20).
  - Hibernate sequences: `LanguageLevel`, `SavedListLink`.
  - Jackson 3: `CandidateReadDao`, `CandidateReadMapper`, `PublishedDocBuilderServiceImpl`, `CandidateDtoFetchServiceImpl`, `VerifyPlusPayloadParserImpl`, `JsonRows`, and the 13 DTOs in `service/embedding/dto` and `service/explanation/dto`.
- Configuration: `server/lombok.config` (new), `application.yml`, `application-test.yml`.
- Tests: about 75 files with import or API moves (§3.4), plus `PostgresTestContainer` and `NotificationServiceImplTest`.
- New tests: `StatusRequestMatcherTest`, `EntityIdGeneratorIntegrationTest`, `util/json/ApplicationJsonMapper`; additions to `CandidateMatchExplanationIntegrationTest` (3) and `JsonRowsTest` (1).
- Decision changes: `TcPasswordEncoderTest` (D16), `SalesforceJsonPayloadTest` (D17), `CandidateMatchExplanationIntegrationTest` (D18).

**Checkpoint review:** I reviewed the whole diff. There is no temporary debugging code, no disabled test and no probe file left behind. The probe used for D19 lived only in the scratchpad and in a temporary Boot 3 worktree, and both are deleted. `BatchConfig` keeps its existing Javadoc and `@RequiredArgsConstructor`, so it shows no unrelated change. The other non-mechanical changes are the ones listed in §3.4.

**Stage 5:**
- Main code:
  - `configuration/RestRepositoryConfig` (D23);
  - `application.yml` (D26: `spring.jackson.mapper.detect-parameter-names`, `spring.jackson.visibility.creator`).
- New tests:
  - `integration/security/HttpAuthorizationMatrixIntegrationTest` (+ `security/http-authorization-matrix.txt`);
  - `integration/api/ApiJsonCompatibilityIntegrationTest` (+ `api/api-json-exchanges.txt`);
  - `integration/api/CandidateSearchIntegrationTest`;
  - `integration/api/CandidateStatsIntegrationTest`;
  - `integration/repo/RepositoryQueryCompatibilityIntegrationTest` (+ `repository/repository-query-outcomes.txt`);
  - `integration/repo/PersistenceBehaviourIntegrationTest`;
  - `integration/batch/CandidateTextJobIntegrationTest`;
  - `integration/cache/RedisIntegrationTest`;
  - `integration/cache/ShedLockSchedulerIntegrationTest`;
  - `integration/helper/BatchMetadataCleaner`.
- Tests changed:
  - `BatchJobServiceIntegrationTest` (cleanup; D27 wait before restart);
  - `logging/CpuUsageTest` (wait for a CPU sample);
  - `util/json/ApplicationJsonMapper` (loads `application.yml`).

**Stage 4:**
- Migration (new): `server/src/main/resources/db/migration/V2_34__rename_spring_batch_job_sequence.sql`
- Test (new): `integration/batch/SpringBatchSchemaMigrationIntegrationTest`
- Test fixture (new): `server/src/test/resources/batch/spring-batch-5-metadata.sql`
- Test changed: `BatchJobServiceIntegrationTest` (D22: exception type, plus a stronger check).
- Temporary checks, all deleted: the Boot 3 worktree, the Batch 5 fixture generator, the ShedLock probe and the DDL comparison scripts.

**New tests (baseline, before Stage 2):**
- `util/http/RecordingHttpServer`
- `service/explanation/PythonServiceClientsJsonPayloadTest`
- `service/db/impl/SalesforceJsonPayloadTest`
- `service/ai/impl/AnthropicJsonPayloadTest`
- `service/db/impl/BatchJobServiceIntegrationTest`

**Tests added to existing classes:**
- `CandidateMatchExplanationIntegrationTest` (2)
- `ChatbotMessageRepositoryIntegrationTest` (3)
