# TC-1189 — Spring Boot 4 Migration Report

Branch: `john/tc-1189-upgrade-to-spring-boot-4` (base: `b1c367b9f` TC-1584)
Status: **Stage 1 complete — plan awaiting review. No repository files modified.**

---

## 1. Environment and versions

Target versions come from the `spring-boot-dependencies:4.1.2` BOM on Maven Central (latest GA, published 2026-10-22).

| Component | Current (3.5.16) | Target (Boot 4.1.2) | How set |
|---|---|---|---|
| Spring Boot | 3.5.16 | **4.1.2** | plugin |
| Spring Framework | 6.2.x | 7.0.10 | managed |
| Spring Security | 6.5.x | 7.1.2 | managed |
| Spring Batch | 5.2.x | 6.0.6 | managed |
| Spring Data | 2025.0.x | 2026.0.2 | managed |
| Hibernate ORM | 6.6.x | 7.4.13.Final | managed |
| Jackson | 2.19.x | 3.1.7 (`tools.jackson`) + 2.21.7 (`com.fasterxml`) still managed | managed |
| Flyway | 11.20.1 (override) | 12.4.0 | drop override |
| Testcontainers | **1.21.4 actually resolved** (Boot 3.5 management overrode the 2.0.3 BOM) | 2.0.5 | managed, drop BOM |
| JUnit Jupiter | 5.12.x | 6.0.3 | managed |
| Mockito | 5.17.x | 5.23.0 | managed |
| Tomcat | 10.1.x | 11.0.27 | managed |
| HikariCP | 6.x | 7.0.2 | managed |
| Lettuce | 6.x | 7.5.2 | managed |
| PostgreSQL JDBC | 42.7.x | 42.7.14 | managed |
| Hypersistence Utils | hibernate-63:3.7.3 | **hibernate-73:3.16.0** (covers Hibernate 7.3/7.4) | explicit |
| ShedLock | 5.10.0 | **7.10.1** | explicit |
| jjwt | 0.11.2 | 0.11.2 (unchanged; see D8) | explicit |
| TOTP starter | 1.7.1 | 1.7.1 (unchanged) | explicit |
| docx4j | 8.3.15 (javax JAXB) | 8.3.15 (unchanged) | explicit |
| dependency-management plugin | 1.1.7 | 1.1.7 | plugin |
| Gradle | 8.14.4 | 8.14.4 (Boot 4 supports 8.14+ and 9.x) | wrapper |
| Java | 17 toolchain | 17 (Boot 4 baseline is 17) | toolchain |

---

## 2. Findings by area (Stage 1 assessment)

### 2.1 Build / starters (Boot 4 modular structure)
- `spring-boot-starter-web` → `spring-boot-starter-webmvc` (old name deprecated).
- **Flyway:** `flyway-core` alone no longer triggers auto-configuration. Must add `spring-boot-starter-flyway`. Keep `flyway-database-postgresql`. Without this, **migrations would silently stop running at startup.**
- **Batch:** `spring-boot-starter-batch` now defaults to an **in-memory (resourceless) job repository**. We need `spring-boot-starter-batch-jdbc`, or job history, restart and stop would silently stop using the database.
- Tests need technology test starters: `spring-boot-starter-webmvc-test` (for `@WebMvcTest` and `@AutoConfigureMockMvc`), `spring-boot-starter-data-jpa-test` (for `@DataJpaTest` and `@AutoConfigureTestDatabase`), and probably `spring-boot-starter-security-test`.
- Remove the `testcontainers-bom:2.0.3` platform and use Boot's managed version. Rename the artifacts to `testcontainers-junit-jupiter` and `testcontainers-postgresql`.
- Remove `set('flyway.version', '11.20.1')`.
- `META-INF/spring/...AutoConfiguration.imports` registers `TotpAutoConfiguration`. The class only uses `@ConditionalOnClass`, `@ConditionalOnMissingBean` and `@EnableConfigurationProperties`, which all still exist in Boot 4, so it is low risk.

### 2.2 Spring Boot relocated classes (main code)
| File | Class | Boot 4 location (to confirm at compile) |
|---|---|---|
| `DatabaseConfiguration` | `DataSourceProperties` | `org.springframework.boot.jdbc.autoconfigure` |
| `DatabaseConfiguration` | `EntityManagerFactoryBuilder` | `org.springframework.boot.jpa` |
| `DatabaseConfiguration` | `SpringImplicitNamingStrategy` | `org.springframework.boot.hibernate` |
| `TcFlywayConfig` | `FlywayMigrationStrategy` | `org.springframework.boot.flyway.autoconfigure` |
| `application.yml` | `spring.autoconfigure.exclude: ...data.redis.RedisRepositoriesAutoConfiguration` | Class renamed and moved. **Boot fails startup on an invalid exclude.** Replace it with `spring.data.redis.repositories.enabled: false`, which is equivalent and does not depend on the class name. |

### 2.3 Jackson 3 — highest behavioural risk
- Boot 4 auto-configures a Jackson 3 `JsonMapper` and **no Jackson 2 `ObjectMapper` bean**. Spring MVC, WebClient codecs and Spring Data REST all use Jackson 3.
- Main code injects `com.fasterxml...ObjectMapper` in 5 classes: `CandidateReadDao`, `CandidateReadMapper`, `PublishedDocBuilderServiceImpl`, `CandidateDtoFetchServiceImpl` and `VerifyPlusPayloadParserImpl`. These **fail at startup**.
- About 60 `@WebMvcTest` classes `@Autowired ObjectMapper` (Jackson 2), so they fail too.
- **Silent wire-format risk:** 25 DTOs use `com.fasterxml.jackson.databind.annotation.@JsonNaming(SnakeCaseStrategy)` and related databind annotations, which Jackson 3 ignores. They are sent and received through WebClient: explanations and embeddings (Python services), Anthropic, Preset, Duolingo, reCAPTCHA and the Salesforce `model/sf` classes. They would compile, but field names would change from snake_case to camelCase. Annotations from `com.fasterxml.jackson.annotation` (`@JsonProperty`, `@JsonCreator`, …) are shared by Jackson 2 and 3, so they are fine.
- Jackson 3 changes some defaults, for example property ordering and strictness on nulls and trailing tokens. Boot offers `spring.jackson.use-jackson2-defaults=true` to keep Boot 3 behaviour.
- **Jackson 2 must remain** for: Hypersistence `JsonBinaryType` and `ObjectMapperWrapper` (`ChatbotMessage`, `CandidateMatchingExplanation` jsonb columns), `jjwt-jackson`, the Google and AWS libraries, and standalone `new ObjectMapper()` utilities (`QueryHelper`, `TextPartsCodec`, `S3TranslationStorageService`, `QAServiceImpl`). Boot 4 still manages the Jackson 2 BOM, so the two can coexist.
- `CandidateMatchingExplanationData` is persisted through Jackson 2 (Hypersistence). If it is also exchanged over WebClient, it needs annotations that work in both Jackson 2 and Jackson 3. Check each class.

### 2.4 Spring Batch 6
- Many package moves. For example, `org.springframework.batch.core.Job` moves to `core.job.Job`, `Step`/`StepExecution` move to `core.step.*`, and `org.springframework.batch.item.*` moves to `org.springframework.batch.infrastructure.item.*`. This affects 10 main files and 4 test files.
- `JobLauncher` and `JobExplorer` are deprecated: `JobOperator` now covers launching and `JobRepository` covers exploring. `SimpleJobOperator` is replaced by `TaskExecutorJobOperator`. `BatchConfig` and `BatchJobServiceImpl` collapse to a single async `JobOperator`, with `JobRepository` used for queries. Observable behaviour stays the same.
- `CandidateBatchJobFactory` builds its step manually from `ChunkOrientedTasklet`, `SimpleChunkProvider`, `SimpleChunkProcessor` and `RepeatTemplate`, wrapped by `AdaptiveDelayTasklet`. In Batch 6 these legacy chunk classes are **deprecated but still present**.
- **Schema:** I checked the jar. `spring-batch-core-6.0.6` ships `migration/6.0/migration-postgresql.sql`, which contains exactly one statement:
  `ALTER SEQUENCE BATCH_JOB_SEQ RENAME TO BATCH_JOB_INSTANCE_SEQ;`
  Our schema (`V2__init.sql`) is already at the Batch 5 table layout, with `parameter_name` and `parameter_type`. **One new Flyway migration is needed**, and only for the rename. The rename keeps the current sequence value, all history and restartability. Nothing is dropped.

### 2.5 Spring Security 7
- `SecurityConfiguration` already uses the lambda DSL and `requestMatchers(String)`. In Security 7 the patterns are matched by `PathPatternRequestMatcher`. MVC already used `PathPatternParser`, so semantics should be unchanged. `"/status**"` is the only odd pattern and needs a regression test.
- The CSRF filter is disabled for HTTP. The WebSocket config (TC-1583) builds `AuthorizationChannelInterceptor` manually without the CSRF channel interceptor, so it uses no `@EnableWebSocketSecurity`, adds no CSRF requirement for STOMP and has no API that Security 7 removed. TC-1584 already changed the `DaoAuthenticationProvider` constructor.
- `BeanIds.AUTHENTICATION_MANAGER` needs to be confirmed at compile.

### 2.6 Spring Data JPA / Hibernate 7
- There are no `Specification.where/not/allOf` calls left (TC-1583). The specs build predicates with `cb.and()`, and none return `null`.
- The code makes no direct Hibernate `Session` calls. Hibernate annotations used: `@Formula`, `@Immutable` and `@Type(JsonBinaryType)`.
- `CamelCaseToUnderscoresNamingStrategy` may be deprecated in Hibernate 7. Hibernate offers `PhysicalNamingStrategySnakeCaseImpl` instead; check whether the naming output matches before switching.
- In `application.yml`, `hibernate.temp.use_jdbc_metadata_defaults` and the explicit `PostgreSQLDialect` are probably obsolete. They will at most produce warnings and are left as they are.
- Runtime risks to cover with tests: Hibernate 7 validates HQL more strictly (45 `@Query` files) and `merge`/`save` behaves differently for detached entities with an assigned id that are not in the database. Startup and the integration suite are the check.

### 2.7 Other
- **ShedLock 5.10 → 7.x:** ShedLock 7 is the Spring 7 line. `JdbcTemplateLockProvider(DataSource)` stays the same.
- **Hypersistence:** the artifact changes to `hypersistence-utils-hibernate-73`. Imports are unchanged (`io.hypersistence.utils.hibernate.type.json.*`).
- **jjwt 0.11.2:** it uses `jjwt-jackson`, which is Jackson 2 and still available. It is not a blocker.
- **docx4j 8.3.15:** it uses javax JAXB with explicit `jaxb-api 2.3.1` and `jaxb-runtime 2.3.8` versions. The explicit versions win, as they did under Boot 3, so nothing changes.
- **Testcontainers 2:** `org.testcontainers.containers.PostgreSQLContainer<?>` becomes `org.testcontainers.postgresql.PostgreSQLContainer` (`PostgresTestContainer`).
- **Test annotations:** about 130 mechanical import changes (`@WebMvcTest`, `@AutoConfigureMockMvc`, `@DataJpaTest`, `@AutoConfigureTestDatabase`). The tests already use `@MockitoBean`.
- **Spring Framework 7:** `org.springframework.lang.@Nullable` is deprecated in favour of JSpecify and is used in 270 files. It only produces warnings and is left as it is.
- `HttpHeaders` is no longer a `MultiValueMap` in Framework 7. The code only uses the constants, so nothing is affected.
- **Actuator:** liveness and readiness probes are now enabled by default. Security only permits the exact path `/actuator/health`, so the new sub-paths are not public.
- `performance-tests` is a separate Kotlin Gradle module that does not use Spring Boot, so it is not affected.

---

## 3. Test results
| Date | Command | Tests | Failures | Notes |
|---|---|---|---|---|
| (baseline) | `./gradlew :server:test` | 3,547 | 0 | Boot 3.5.16, from the TC-1584 run |

Note: about 14 Testcontainers integration tests and other `skip-test-in-gradle-build`-tagged tests are excluded from the Gradle run. They will be run separately in Stage 4, which needs Docker.

---

## 4. Issues and decisions

### Decisions requiring approval (Stage 1)
Status: **awaiting review**. Record the decision in the last column.

| # | Decision | Recommendation | Decision |
|---|---|---|---|
| D1 | Boot version | **4.1.2**, the latest release (the 4.0.x line is at 4.0.9). | |
| D2 | Gradle and Java | Keep **Gradle 8.14.4 and Java 17**. Both meet Boot 4's minimums. Upgrade Gradle 9 and Java 21/25 in separate tickets. | |
| D3 | Jackson strategy | **Jackson 3 for Spring** (MVC, WebClient, Data REST): migrate the 5 injected `ObjectMapper` classes, the ~60 tests and the 25 snake_case WebClient classes (§2.3). **Keep Jackson 2 only where libraries require it**: Hypersistence jsonb columns, jjwt, Google/AWS libraries and the standalone utilities. Avoid the deprecated `spring-boot-jackson2` stop-gap module. | |
| D4 | Jackson 3 default settings | Set `spring.jackson.use-jackson2-defaults=true` for TC-1189 so the API wire format stays as it is now. Remove it in a follow-up. | |
| D5 | Batch job classes | Replace `JobLauncher`/`JobExplorer` with one async `JobOperator` plus `JobRepository` (same behaviour). **Keep the deprecated chunk classes** used by `AdaptiveDelayTasklet` for now and redesign them in a follow-up. | |
| D6 | Flyway | Remove the `11.20.1` pin and use Boot's managed 12.4.0. | |
| D7 | Library upgrades | ShedLock 5.10.0 → 7.10.1. Hypersistence `-hibernate-63:3.7.3` → `-hibernate-73:3.16.0`. Testcontainers 2 artifact names (`testcontainers-junit-jupiter`, `testcontainers-postgresql`). | |
| D8 | Libraries left alone | Keep jjwt 0.11.2, docx4j 8.3.15 and TOTP 1.7.1. None of them blocks the migration. | |
| D9 | Starter style | Use the **explicit modular starters** (`webmvc`, `flyway`, `batch-jdbc` and the `*-test` starters), not the `-classic` compatibility starters. | |
| D10 | Report location | `server/docs/TC-1189-spring-boot-4-migration-report.md` | Moved here |

### Proposed implementation sequence
1. **Stage 2, build configuration:** Boot plugin 4.1.2, the modular starters, library upgrades and removing the obsolete pins. Compile, then report the failures grouped by cause.
2. **Stage 3, source fixes, one framework per change:**
   1. Relocated Spring Boot classes and test annotation imports.
   2. Batch 6 package moves and `JobOperator`.
   3. Jackson 3, with tests that check the snake_case payloads.
   4. Testcontainers 2.
   5. Hibernate, Security and ShedLock leftovers.
3. **Stage 4, database:** add `V2_10__spring_batch_6_rename_job_sequence.sql`. Test it on a fresh database and on one with existing job history, including restarting a job. Run the Testcontainers integration tests.
4. **Stage 5, full test suite:** compare against the 3,547-test baseline.
5. **Stage 6, packaging and runtime checks:** build the JAR, start the app, and check the startup logs for Flyway, Security and Batch.

### Changes that may affect production behaviour
- Jackson 3 becomes the HTTP and WebClient serializer (§2.3).
- Flyway 11 → 12.
- Batch sequence rename migration.
- Hibernate 7 query and merge semantics.

### Manual verification requirements (to be refined)
- Chat over WebSocket from the Angular clients.
- Explanation and embedding calls to the Python services (snake_case payloads).
- Salesforce sync.
- Running and restarting batch jobs from the System Admin page.
- 2FA (TOTP) login.
- CV DOCX export.

### Carried over from TC-1584 (assess separately, not in TC-1189)
- The `TcAuthenticationProvider.supports()` type-checking behaviour.
- Possible duplicate Salesforce POSTs after connection failures and retries.
- The Salesforce `Content_Type` header.

---

## 5. Follow-up recommendations (not part of TC-1189)
- **Salesforce WebClient response handling:** TC-1584 kept the `executeWithRetry()` contract by buffering responses and returning a `ClientResponse`. After Boot 4, refactor it to return typed results and process responses inside the WebClient lifecycle. This does not block the migration.
- Replace the deprecated Batch 6 chunk classes in `CandidateBatchJobFactory`/`AdaptiveDelayTasklet` with the new `ChunkOrientedStep` model.
- Migrate `org.springframework.lang.Nullable` (270 files) to JSpecify.
- Drop `spring.jackson.use-jackson2-defaults` once the API wire format has been checked under the Jackson 3 defaults.
- Move the remaining standalone Jackson 2 utilities to Jackson 3 where no third-party library constrains them.
- Upgrade jjwt to 0.13 (API changes, and `SignatureAlgorithm` is deprecated).
- Upgrade docx4j to 11.x (Jakarta JAXB) and remove the javax JAXB dependencies.
- `thymeleaf-extras-springsecurity6` appears unused, since no templates use `sec:`. Consider removing it.
- `DatabaseConfiguration` TODO: move to the default DataSource/JPA auto-configuration.
- Upgrade to Gradle 9 and Java 21/25 separately.
