/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.integration.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.job.DefaultJobKeyGenerator;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.launch.support.TaskExecutorJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JdbcJobRepositoryFactoryBean;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.tctalent.server.service.db.impl.BatchJobServiceImpl;

/**
 * Tests the Spring Batch 6 database migration (V2_34) on a fresh database and on a database
 * containing Spring Batch 5 job metadata - TC-1189.
 * <p>
 * The Spring Batch 5 metadata (src/test/resources/batch/spring-batch-5-metadata.sql) was written
 * by Spring Batch 5.2.6 itself, before the upgrade. The databases are created by our Flyway
 * migrations and read and written by Spring Batch 6's JDBC job repository.
 * </p>
 */
class SpringBatchSchemaMigrationIntegrationTest {

    /** The last migration before the Spring Batch 6 migration */
    private static final String VERSION_BEFORE_BATCH_6 = "2.33";

    private static final List<String> BATCH_TABLES = List.of(
        "batch_job_instance", "batch_job_execution", "batch_job_execution_params",
        "batch_job_execution_context", "batch_step_execution", "batch_step_execution_context");

    private static final PostgreSQLContainer postgres =
        new PostgreSQLContainer("pgvector/pgvector:0.8.0-pg17");

    @BeforeAll
    static void startDatabase() {
        postgres.start();
    }

    @AfterAll
    static void stopDatabase() {
        postgres.stop();
    }

    @Test
    @DisplayName("fresh database: Flyway creates the Spring Batch 6 schema and jobs run")
    void freshDatabase() throws Exception {
        DataSource dataSource = createDatabase("fresh");
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        flyway(dataSource, null).migrate();

        assertThat(appliedVersions(jdbc)).contains("2.34");
        assertThat(batchSequences(jdbc)).containsExactlyInAnyOrder(
            "batch_job_instance_seq", "batch_job_execution_seq", "batch_step_execution_seq");

        //Same tables and columns as Spring Batch 6's own PostgreSQL schema
        assertThat(batchColumns(jdbc)).isEqualTo(springBatchSchemaColumns());

        JobRepository jobRepository = jobRepository(dataSource);
        TaskExecutorJobOperator jobOperator = jobOperator(jobRepository, new MapJobRegistry());

        JobExecution execution = jobOperator.start(
            simpleJob("tc1189-fresh-job", jobRepository, dataSource),
            new JobParametersBuilder().addString("jobDate", "2026-10-26").toJobParameters());

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(execution.getJobInstance().getInstanceId()).isEqualTo(1L);
        assertThat(execution.getId()).isEqualTo(1L);

        //The metadata is persisted and read back
        JobExecution read = jobRepository.getJobExecution(execution.getId());
        assertThat(read.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(read.getJobParameters().getString("jobDate")).isEqualTo("2026-10-26");
        assertThat(read.getStepExecutions()).hasSize(1);
        assertThat(jdbc.queryForObject("select count(*) from batch_job_instance", Long.class))
            .isEqualTo(1L);
    }

    @Test
    @DisplayName("existing database: Spring Batch 5 metadata survives the migration and is usable")
    void existingSpringBatch5Database() throws Exception {
        DataSource dataSource = createDatabase("upgraded");
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        //A database as it was before the upgrade, containing Spring Batch 5 job metadata
        flyway(dataSource, VERSION_BEFORE_BATCH_6).migrate();
        try (var connection = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(connection,
                new ClassPathResource("batch/spring-batch-5-metadata.sql"));
        }
        Map<String, String> rowsBefore = tableContents(jdbc);
        Map<String, Object> jobSeqBefore = jdbc.queryForMap(
            "select last_value, is_called from batch_job_seq");
        Map<String, Object> executionSeqBefore = jdbc.queryForMap(
            "select last_value, is_called from batch_job_execution_seq");
        Map<String, Object> stepSeqBefore = jdbc.queryForMap(
            "select last_value, is_called from batch_step_execution_seq");
        assertThat(jobSeqBefore).containsEntry("last_value", 4L).containsEntry("is_called", true);

        flyway(dataSource, null).migrate();

        //Only the sequence is renamed, keeping its value. All job metadata is unchanged.
        assertThat(appliedVersions(jdbc)).contains("2.34");
        assertThat(batchSequences(jdbc)).containsExactlyInAnyOrder(
            "batch_job_instance_seq", "batch_job_execution_seq", "batch_step_execution_seq");
        assertThat(jdbc.queryForMap("select last_value, is_called from batch_job_instance_seq"))
            .isEqualTo(jobSeqBefore);
        assertThat(jdbc.queryForMap("select last_value, is_called from batch_job_execution_seq"))
            .isEqualTo(executionSeqBefore);
        assertThat(jdbc.queryForMap("select last_value, is_called from batch_step_execution_seq"))
            .isEqualTo(stepSeqBefore);
        assertThat(tableContents(jdbc)).isEqualTo(rowsBefore);

        JobRepository jobRepository = jobRepository(dataSource);

        //Existing instances, executions, parameters and execution contexts are read by Batch 6
        assertThat(jobRepository.getJobNames()).containsExactlyInAnyOrder(
            "tc1189-batch5-completed", "tc1189-batch5-failed", "tc1189-batch5-stopped",
            "tc1189-batch5-service");

        JobExecution completed = onlyExecution(jobRepository, "tc1189-batch5-completed");
        assertThat(completed.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        JobParameters parameters = completed.getJobParameters();
        assertThat(parameters.getString("jobDate")).isEqualTo("2025-04-15");
        assertThat(parameters.getLong("count")).isEqualTo(42L);
        assertThat(parameters.getParameter("runDate").value()).isEqualTo(LocalDate.of(2025, 4, 15));
        assertThat(parameters.getParameter("note").identifying()).isFalse();
        assertThat(completed.getExecutionContext().getString("batch.version")).isEqualTo("5.2.6");
        StepExecution chunkStep = completed.getStepExecutions().iterator().next();
        assertThat(chunkStep.getStepName()).isEqualTo("chunkStep");
        assertThat(chunkStep.getReadCount()).isEqualTo(5);
        assertThat(chunkStep.getWriteCount()).isEqualTo(5);
        assertThat(chunkStep.getExecutionContext().getInt("reader.position")).isEqualTo(5);

        JobExecution failed = onlyExecution(jobRepository, "tc1189-batch5-failed");
        assertThat(failed.getStatus()).isEqualTo(BatchStatus.FAILED);
        assertThat(failed.getExitStatus().getExitDescription()).contains("Simulated failure");

        JobExecution stopped = onlyExecution(jobRepository, "tc1189-batch5-stopped");
        assertThat(stopped.getStatus()).isEqualTo(BatchStatus.STOPPED);
        assertThat(stopped.getStepExecutions().iterator().next().getExecutionContext()
            .getInt("progress")).isEqualTo(3);

        //The status summary reported by our batch job service still lists the existing history
        MapJobRegistry jobRegistry = new MapJobRegistry();
        TaskExecutorJobOperator jobOperator = jobOperator(jobRepository, jobRegistry);
        BatchJobServiceImpl batchJobService = new BatchJobServiceImpl(jobOperator, jobRepository);
        String summary = batchJobService.getJobExecutionsSummary();
        assertThat(summary)
            .contains("Job Name    : tc1189-batch5-completed")
            .contains("Job Name    : tc1189-batch5-failed")
            .contains("Status      : STOPPED")
            .contains("Job Name    : tc1189-batch5-service");

        //D21: Batch 6 computes job keys (the hash of the identifying parameters) differently from
        //Batch 5, so job instances created before the upgrade are not found from their parameters.
        JobExecution service = onlyExecution(jobRepository, "tc1189-batch5-service");
        JobParameters sameParameters = new JobParametersBuilder()
            .addString("jobDate", service.getJobParameters().getString("jobDate"))
            .addString("jobLabel", "tc1189-batch5-service")
            .toJobParameters();
        assertThat(new DefaultJobKeyGenerator().generateKey(sameParameters))
            .isNotEqualTo(jdbc.queryForObject(
                "select job_key from batch_job_instance where job_name = 'tc1189-batch5-service'",
                String.class));

        //So a once per day job which ran under Batch 5 can run once more on the upgrade day,
        //as a new job instance...
        JobExecution sameDay = jobOperator.start(
            simpleJob("tc1189-batch5-service", jobRepository, dataSource), sameParameters);
        assertThat(sameDay.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(sameDay.getJobInstance().getInstanceId()).isEqualTo(5L);
        assertThat(sameDay.getId()).isEqualTo(5L);

        //...after which the once per day restriction applies as before.
        assertThatThrownBy(() -> jobOperator.start(
            simpleJob("tc1189-batch5-service", jobRepository, dataSource), sameParameters))
            .isInstanceOf(JobInstanceAlreadyCompleteException.class);

        //And restarting an execution stopped under Batch 5 starts a new job instance from the
        //beginning rather than resuming the old one. (Restart is not used in production - D13.)
        List<Boolean> progressFoundOnRestart = new ArrayList<>();
        Job stoppedJob = new JobBuilder("tc1189-batch5-stopped", jobRepository)
            .start(new StepBuilder("loopingStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    progressFoundOnRestart.add(chunkContext.getStepContext().getStepExecution()
                        .getExecutionContext().containsKey("progress"));
                    return RepeatStatus.FINISHED;
                }, new JdbcTransactionManager(dataSource))
                .build())
            .build();
        jobRegistry.register(stoppedJob);
        String restartMessage = batchJobService.restartJobExecution(stopped.getId());

        assertThat(restartMessage).endsWith("new execution ID: 6");
        JobExecution restarted = jobRepository.getJobExecution(6L);
        assertThat(restarted.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(restarted.getJobInstance().getInstanceId()).isEqualTo(6L);
        assertThat(progressFoundOnRestart).containsExactly(false);
        assertThat(jobRepository.getJobExecution(stopped.getId()).getStatus())
            .isEqualTo(BatchStatus.STOPPED);

        //New job instances and executions continue from the existing ids, with no collisions
        JobExecution newJob = jobOperator.start(simpleJob("tc1189-new-job", jobRepository, dataSource),
            new JobParametersBuilder().addString("jobDate", "2026-10-26").toJobParameters());

        assertThat(newJob.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(newJob.getJobInstance().getInstanceId()).isEqualTo(7L);
        assertThat(newJob.getId()).isEqualTo(7L);
        assertThat(jdbc.queryForObject("select count(*) from batch_job_instance", Long.class))
            .isEqualTo(7L);
        assertThat(jdbc.queryForObject("select count(*) from batch_job_execution", Long.class))
            .isEqualTo(7L);
    }

    private static DataSource createDatabase(String name) {
        new JdbcTemplate(new DriverManagerDataSource(
            postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword()))
            .execute("create database " + name);
        String url = "jdbc:postgresql://" + postgres.getHost() + ":"
            + postgres.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT) + "/" + name;
        return new DriverManagerDataSource(url, postgres.getUsername(), postgres.getPassword());
    }

    /**
     * Flyway configured as in application.yml, optionally migrating only up to the given version.
     */
    private static Flyway flyway(DataSource dataSource, String targetVersion) {
        var configuration = Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .baselineVersion("2")
            .validateOnMigrate(true);
        if (targetVersion != null) {
            configuration.target(targetVersion);
        }
        return configuration.load();
    }

    private static JobRepository jobRepository(DataSource dataSource) throws Exception {
        JdbcJobRepositoryFactoryBean factory = new JdbcJobRepositoryFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTransactionManager(new JdbcTransactionManager(dataSource));
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    /** Synchronous, so that each job has finished when it is started or restarted. */
    private static TaskExecutorJobOperator jobOperator(
        JobRepository jobRepository, MapJobRegistry jobRegistry) throws Exception {
        TaskExecutorJobOperator jobOperator = new TaskExecutorJobOperator();
        jobOperator.setJobRepository(jobRepository);
        jobOperator.setJobRegistry(jobRegistry);
        jobOperator.setTaskExecutor(new SyncTaskExecutor());
        jobOperator.afterPropertiesSet();
        return jobOperator;
    }

    private static Job simpleJob(String name, JobRepository jobRepository, DataSource dataSource) {
        return new JobBuilder(name, jobRepository)
            .start(new StepBuilder("simpleStep", jobRepository)
                .tasklet((contribution, chunkContext) -> RepeatStatus.FINISHED,
                    new JdbcTransactionManager(dataSource))
                .build())
            .build();
    }

    private static JobExecution onlyExecution(JobRepository jobRepository, String jobName) {
        List<JobInstance> instances = jobRepository.getJobInstances(jobName, 0, 10);
        assertThat(instances).hasSize(1);
        List<JobExecution> executions = jobRepository.getJobExecutions(instances.get(0));
        assertThat(executions).hasSize(1);
        return executions.get(0);
    }

    private static List<String> appliedVersions(JdbcTemplate jdbc) {
        return jdbc.queryForList(
            "select version from flyway_schema_history where success and version is not null",
            String.class);
    }

    private static List<String> batchSequences(JdbcTemplate jdbc) {
        return jdbc.queryForList("select sequence_name from information_schema.sequences"
            + " where sequence_name like 'batch\\_%'", String.class);
    }

    /** The full contents of each batch table, as JSON ordered by every column. */
    private static Map<String, String> tableContents(JdbcTemplate jdbc) {
        Map<String, String> contents = new LinkedHashMap<>();
        for (String table : BATCH_TABLES) {
            contents.put(table, jdbc.queryForObject(
                "select coalesce(json_agg(t order by t::text), '[]')::text from " + table + " t",
                String.class));
        }
        return contents;
    }

    private static Map<String, TreeSet<String>> batchColumns(JdbcTemplate jdbc) {
        Map<String, TreeSet<String>> columns = new TreeMap<>();
        jdbc.query("select table_name, column_name from information_schema.columns"
                + " where table_schema = 'public' and table_name like 'batch\\_%'",
            rs -> {
                columns.computeIfAbsent(rs.getString(1), t -> new TreeSet<>())
                    .add(rs.getString(2));
            });
        return columns;
    }

    /** Tables and columns from Spring Batch's own schema-postgresql.sql */
    private static Map<String, TreeSet<String>> springBatchSchemaColumns() throws IOException {
        String schema = new ClassPathResource("org/springframework/batch/core/schema-postgresql.sql")
            .getContentAsString(StandardCharsets.UTF_8);
        Map<String, TreeSet<String>> columns = new TreeMap<>();
        Matcher table = Pattern.compile("CREATE TABLE (\\w+) \\((.*?)\\) ;", Pattern.DOTALL)
            .matcher(schema);
        while (table.find()) {
            TreeSet<String> names = new TreeSet<>();
            for (String line : table.group(2).split("\n")) {
                Matcher column = Pattern.compile("^\\s*([A-Z_]+)\\s+[A-Z]").matcher(line);
                if (column.find() && !line.trim().startsWith("constraint")) {
                    names.add(column.group(1).toLowerCase());
                }
            }
            columns.put(table.group(1).toLowerCase(), names);
        }
        assertThat(columns).hasSize(6);
        return columns;
    }
}
