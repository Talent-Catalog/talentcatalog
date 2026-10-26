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

package org.tctalent.server.service.db.impl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecutionException;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.tctalent.server.integration.helper.BaseDBIntegrationTest;
import org.tctalent.server.repository.db.read.cache.CandidateRedisCache;
import org.tctalent.server.service.db.BatchJobService;

/**
 * Integration tests of {@link BatchJobService} against the real Spring Batch infrastructure and
 * PostgreSQL job repository schema (created by our Flyway migrations).
 * <p>
 * Verifies the behavior relied on by the System Admin batch job endpoints: asynchronous launch,
 * status reporting, stopping, restarting and the "once per day" option.
 * </p>
 * <p>
 * Job state is verified directly from the Spring Batch metadata tables, so these tests do not
 * depend on Spring Batch's query APIs (which change in Spring Batch 6). Added before the Spring
 * Boot 4 (Spring Batch 6) migration - TC-1189.
 * </p>
 */
@SpringBootTest
class BatchJobServiceIntegrationTest extends BaseDBIntegrationTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    @Autowired private BatchJobService batchJobService;
    @Autowired private JobRepository jobRepository;
    @Autowired private JobRegistry jobRegistry;
    @Autowired private PlatformTransactionManager transactionManager;
    @Autowired private JdbcTemplate jdbcTemplate;

    //Redis is not available in tests
    @MockitoBean private CandidateRedisCache candidateRedisCache;

    /**
     * Latches and flags which hold jobs running. All are released after each test so that no
     * job is left running.
     */
    private final List<CountDownLatch> latches = new ArrayList<>();
    private final List<AtomicBoolean> keepRunningFlags = new ArrayList<>();
    private final List<String> registeredJobNames = new ArrayList<>();

    @AfterEach
    void releaseJobs() {
        latches.forEach(CountDownLatch::countDown);
        keepRunningFlags.forEach(flag -> flag.set(false));
        registeredJobNames.forEach(jobRegistry::unregister);
        waitUntil("no test job is still running", () -> jdbcTemplate.queryForObject("""
            select count(*) from batch_job_execution e
            join batch_job_instance i on i.job_instance_id = e.job_instance_id
            where i.job_name like 'tc1189-test-%' and e.status in ('STARTING', 'STARTED', 'STOPPING')
            """, Integer.class) == 0);
    }

    @Test
    @DisplayName("launchJob returns while the job is still running and the job completes in the background")
    void launchIsAsynchronous() throws Exception {
        CountDownLatch release = latch();
        AtomicReference<String> jobThread = new AtomicReference<>();
        Job job = job(jobName(), (contribution, chunkContext) -> {
            jobThread.set(Thread.currentThread().getName());
            release.await(TIMEOUT.toSeconds(), TimeUnit.SECONDS);
            return RepeatStatus.FINISHED;
        });

        String result = batchJobService.launchJob(job, false);

        //The job is held by the latch, so it cannot have completed yet
        long executionId = latestExecutionId(job.getName());
        waitUntil("job is running", () -> jobThread.get() != null);
        assertAll(
            () -> assertEquals("Job '" + job.getName() + "' launched successfully", result),
            () -> assertTrue(Set.of("STARTING", "STARTED").contains(status(executionId)),
                status(executionId)),
            () -> assertNotEquals(Thread.currentThread().getName(), jobThread.get())
        );

        release.countDown();
        waitForStatus(executionId, "COMPLETED");
        assertEquals("COMPLETED", exitCode(executionId));
    }

    @Test
    @DisplayName("getJobExecutionsSummary reports the id, name and status of executions")
    void summaryReportsExecutions() throws Exception {
        Job job = job(jobName(), (contribution, chunkContext) -> RepeatStatus.FINISHED);
        batchJobService.launchJob(job, false);
        long executionId = latestExecutionId(job.getName());
        waitForStatus(executionId, "COMPLETED");

        String summary = batchJobService.getJobExecutionsSummary();

        String expectedStart = String.format(
            "Execution ID: %d%nJob Name    : %s%nStatus      : COMPLETED%nStart Time  : ",
            executionId, job.getName());
        assertTrue(summary.contains(expectedStart), summary);
    }

    @Test
    @DisplayName("stopJobExecution stops a running job, which ends with status STOPPED")
    void stopRunningJob() throws Exception {
        Job job = job(jobName(), continuableTasklet(keepRunning()));
        batchJobService.launchJob(job, false);
        long executionId = latestExecutionId(job.getName());
        waitForStatus(executionId, "STARTED");

        String result = batchJobService.stopJobExecution(executionId);

        assertEquals("Job execution " + executionId + " stop initiated successfully.", result);
        waitForStatus(executionId, "STOPPED");
        assertEquals("STOPPED", exitCode(executionId));
    }

    @Test
    @DisplayName("stopJobExecution of an execution which is not running throws")
    void stopCompletedJobThrows() throws Exception {
        Job job = job(jobName(), (contribution, chunkContext) -> RepeatStatus.FINISHED);
        batchJobService.launchJob(job, false);
        long executionId = latestExecutionId(job.getName());
        waitForStatus(executionId, "COMPLETED");

        Exception e = assertThrows(Exception.class,
            () -> batchJobService.stopJobExecution(executionId));

        assertEquals("JobExecutionNotRunningException", e.getClass().getSimpleName());
    }

    @Test
    @DisplayName("restartJobExecution restarts a stopped registered job as a new execution of the same job instance")
    void restartStoppedRegisteredJob() throws Exception {
        AtomicBoolean keepRunning = keepRunning();
        Job job = job(jobName(), continuableTasklet(keepRunning));
        register(job);
        batchJobService.launchJob(job, false);
        long executionId = latestExecutionId(job.getName());
        waitForStatus(executionId, "STARTED");
        batchJobService.stopJobExecution(executionId);
        waitForStatus(executionId, "STOPPED");

        //Let the restarted execution complete
        keepRunning.set(false);
        String result = batchJobService.restartJobExecution(executionId);

        long restartedId = latestExecutionId(job.getName());
        assertAll(
            () -> assertNotEquals(executionId, restartedId),
            () -> assertEquals("Job execution " + executionId
                + " was restarted successfully with new execution ID: " + restartedId, result),
            () -> assertEquals(jobInstanceId(executionId), jobInstanceId(restartedId))
        );
        waitForStatus(restartedId, "COMPLETED");
        assertEquals("STOPPED", status(executionId));
    }

    /**
     * Records current behavior: jobs built by CandidateBatchJobFactory are not registered in the
     * JobRegistry, and the job operator needs the registered Job to restart it.
     * See TC-1189 migration report (pre-existing issue).
     */
    @Test
    @DisplayName("restartJobExecution of a job which is not registered fails with NoSuchJobException")
    void restartUnregisteredJobFails() throws Exception {
        Job job = job(jobName(), continuableTasklet(keepRunning()));
        batchJobService.launchJob(job, false);
        long executionId = latestExecutionId(job.getName());
        waitForStatus(executionId, "STARTED");
        batchJobService.stopJobExecution(executionId);
        waitForStatus(executionId, "STOPPED");

        assertThrows(NoSuchJobException.class,
            () -> batchJobService.restartJobExecution(executionId));
        assertEquals(1, executionCount(job.getName()));
    }

    @Test
    @DisplayName("a job launched once per day cannot be launched again the same day after completing")
    void oncePerDayJobCannotRunTwice() throws Exception {
        Job job = job(jobName(), (contribution, chunkContext) -> RepeatStatus.FINISHED);
        batchJobService.launchJob(job, true);
        waitForStatus(latestExecutionId(job.getName()), "COMPLETED");

        JobExecutionException e = assertThrows(JobExecutionException.class,
            () -> batchJobService.launchJob(job, true));

        assertAll(
            () -> assertInstanceOf(JobInstanceAlreadyCompleteException.class, e.getCause()),
            () -> assertEquals(1, executionCount(job.getName()))
        );
    }

    @Test
    @DisplayName("a job not limited to once per day can be launched again the same day")
    void jobCanRunAgainWhenNotOncePerDay() throws Exception {
        Job job = job(jobName(), (contribution, chunkContext) -> RepeatStatus.FINISHED);
        batchJobService.launchJob(job, false);
        long firstId = latestExecutionId(job.getName());
        waitForStatus(firstId, "COMPLETED");

        batchJobService.launchJob(job, false);
        long secondId = latestExecutionId(job.getName());
        waitForStatus(secondId, "COMPLETED");

        assertAll(
            () -> assertNotEquals(firstId, secondId),
            () -> assertNotEquals(jobInstanceId(firstId), jobInstanceId(secondId))
        );
    }

    // ------------------------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------------------------

    private static String jobName() {
        return "tc1189-test-" + UUID.randomUUID();
    }

    private Job job(String name, Tasklet tasklet) {
        return new JobBuilder(name, jobRepository)
            .start(new StepBuilder(name + "-step", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build())
            .build();
    }

    /**
     * Tasklet which keeps running (one short transaction per iteration, like a chunk) until the
     * flag is cleared or the job is stopped.
     */
    private static Tasklet continuableTasklet(AtomicBoolean keepRunning) {
        return (contribution, chunkContext) -> {
            Thread.sleep(20);
            return keepRunning.get() ? RepeatStatus.CONTINUABLE : RepeatStatus.FINISHED;
        };
    }

    private CountDownLatch latch() {
        CountDownLatch latch = new CountDownLatch(1);
        latches.add(latch);
        return latch;
    }

    private AtomicBoolean keepRunning() {
        AtomicBoolean flag = new AtomicBoolean(true);
        keepRunningFlags.add(flag);
        return flag;
    }

    private void register(Job job) throws Exception {
        jobRegistry.register(job);
        registeredJobNames.add(job.getName());
    }

    private long latestExecutionId(String jobName) {
        return jdbcTemplate.queryForObject("""
            select max(e.job_execution_id) from batch_job_execution e
            join batch_job_instance i on i.job_instance_id = e.job_instance_id
            where i.job_name = ?
            """, Long.class, jobName);
    }

    private int executionCount(String jobName) {
        return jdbcTemplate.queryForObject("""
            select count(*) from batch_job_execution e
            join batch_job_instance i on i.job_instance_id = e.job_instance_id
            where i.job_name = ?
            """, Integer.class, jobName);
    }

    private long jobInstanceId(long executionId) {
        return jdbcTemplate.queryForObject(
            "select job_instance_id from batch_job_execution where job_execution_id = ?",
            Long.class, executionId);
    }

    private String status(long executionId) {
        return jdbcTemplate.queryForObject(
            "select status from batch_job_execution where job_execution_id = ?",
            String.class, executionId);
    }

    private String exitCode(long executionId) {
        return jdbcTemplate.queryForObject(
            "select exit_code from batch_job_execution where job_execution_id = ?",
            String.class, executionId);
    }

    private void waitForStatus(long executionId, String expected) {
        waitUntil("execution " + executionId + " has status " + expected,
            () -> expected.equals(status(executionId)));
    }

    private static void waitUntil(String description, BooleanSupplier condition) {
        long deadline = System.nanoTime() + TIMEOUT.toNanos();
        while (!condition.getAsBoolean()) {
            if (System.nanoTime() > deadline) {
                throw new AssertionError("Timed out waiting until " + description);
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AssertionError("Interrupted waiting until " + description, e);
            }
        }
    }
}
