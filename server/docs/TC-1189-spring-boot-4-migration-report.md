# TC-1189 — Spring Boot 4 Migration Report

Branch: `john/tc-1189-upgrade-to-spring-boot-4` (base: `b1c367b9f` TC-1584)

| Stage | Status |
|---|---|
| 1. Assess and plan | Complete. Decisions D1–D10 approved. |
| Pre-migration baseline tests (requirements 1 and 2) | Complete on Boot 3.5.16: **3,577 tests, 0 failures** |
| 2. Build configuration | Complete. Decisions D11–D15 approved. |
| 3. Source fixes | **Complete.** D16–D20 approved and implemented. Main and test code compile. 3,591 tests, of which only the 8 `BatchJobServiceIntegrationTest` tests fail; they await the Stage 4 migration (§3.4). Ready for a checkpoint commit. **The branch must not be merged or deployed until Stage 4 is done.** |
| 4–6 | Not started |

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
- **Schema:** `migration/6.0/migration-postgresql.sql` in the Batch 6.0.6 jar contains only `ALTER SEQUENCE BATCH_JOB_SEQ RENAME TO BATCH_JOB_INSTANCE_SEQ;`. Our schema already has the Batch 5 table layout. **The next Flyway version is V2_34**: V2_33 is the latest both on this branch and on `origin/staging`. I'll re-check this immediately before creating the migration in Stage 4, since other branches could claim V2_34 first.

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
  - **Developer note:** the reusable `tc-postgres-integration` container created by Testcontainers 1.x clashes with Testcontainers 2 ("Container startup failed"). Run `docker rm -f tc-postgres-integration` once after switching branches.
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

### Outstanding blockers
- **Do not merge or deploy before Stage 4.** The V2_34 Batch migration must ship with this code. Spring Batch 6 uses `BATCH_JOB_INSTANCE_SEQ`, so without the migration **every batch job launch fails** ("relation batch_job_instance_seq does not exist"). This is Stage 4.

### Changes that may affect production behaviour
- Jackson 3 becomes the serializer for HTTP, RestClient, WebClient **and the Hypersistence jsonb columns** (D12).
- Flyway 11 → 12.
- The Batch sequence rename migration (V2_34).
- Hibernate 7 query and merge semantics.
- Batch job metadata depends on `spring-boot-starter-batch-jdbc`. With the plain batch starter, metadata would no longer be persisted.
- Salesforce request bodies no longer contain the duplicate fields (D17). This needs verifying in the Salesforce sandbox before merging.
- jsonb reads: UTC offsets and tolerance of unknown properties (D18).
- A null raw password returns false (D16; not reachable in production).

### Manual verification requirements (to be refined)
- Chat over WebSocket from the Angular clients.
- Explanation and embedding calls to the Python services.
- Salesforce sync against the sandbox (in particular the de-duplicated payloads, D14/D17).
- Creating a language level (D19).
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

**New tests (baseline, before Stage 2):**
- `util/http/RecordingHttpServer`
- `service/explanation/PythonServiceClientsJsonPayloadTest`
- `service/db/impl/SalesforceJsonPayloadTest`
- `service/ai/impl/AnthropicJsonPayloadTest`
- `service/db/impl/BatchJobServiceIntegrationTest`

**Tests added to existing classes:**
- `CandidateMatchExplanationIntegrationTest` (2)
- `ChatbotMessageRepositoryIntegrationTest` (3)
