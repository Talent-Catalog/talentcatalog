package org.tctalent.server.service.db.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobExecutionException;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.tctalent.server.service.db.BatchJobService;

/**
 * Implements BatchJobService methods for managing and running batch jobs.
 * <p>
 * Provides a method to launch a candidate anonymisation job. Only one job instance runs per day
 * by including the current date as a unique job parameter.
 *
 * @author sadatmalik
 */
@Service
public class BatchJobServiceImpl implements BatchJobService {

  private final JobOperator jobOperator;
  private final JobRepository jobRepository;

  public BatchJobServiceImpl(
      @Qualifier("asyncJobOperator") JobOperator jobOperator,
      JobRepository jobRepository) {
    this.jobOperator = jobOperator;
    this.jobRepository = jobRepository;
  }

  /**
   * Builds a plain-text summary of up to 20 recent executions per job, sorted by execution ID.
   * Each line: [executionId] jobName status startTime - endTime
   */
  @Override
  public String getJobExecutionsSummary() {
    List<JobExecution> allExecutions = new ArrayList<>();

    for (String jobName : jobRepository.getJobNames()) {
      // fetch up to 20 recent instances
      List<JobInstance> instances = jobRepository.getJobInstances(jobName, 0, 20);
      for (JobInstance instance : instances) {
        allExecutions.addAll(jobRepository.getJobExecutions(instance));
      }
    }

    // Sort by execution ID in descending order
    allExecutions.sort(Comparator.comparing(JobExecution::getId).reversed());

    // Build the summary string
    StringBuilder sb = new StringBuilder();
    for (JobExecution exec : allExecutions) {
      sb.append(formatExecution(exec));
    }

    return !sb.isEmpty() ? sb.toString() : "No job executions found.";
  }

  //The execution id based stop and restart are deprecated in Spring Batch 6 but are kept because
  //they report unknown execution ids in the same way as before (NoSuchJobExecutionException).
  @SuppressWarnings("removal")
  @Override
  public String stopJobExecution(Long executionId) throws Exception {
    boolean stopped = jobOperator.stop(executionId);
    return stopped
        ? "Job execution " + executionId + " stop initiated successfully."
        : "Job execution " + executionId + " could not be stopped (maybe already completed or not running).";
  }

  @SuppressWarnings("removal")
  @Override
  public String restartJobExecution(Long executionId) throws Exception {
    Long newExecutionId = jobOperator.restart(executionId);
    return "Job execution " + executionId + " was restarted successfully with new execution ID: " + newExecutionId;
  }

  @Override
  public String launchJob(Job job, boolean onlyOncePerDay) throws JobExecutionException {
    try {
      // Job params must be unique per job execution, if not the job will not run
      // We enforce a max of one job instance per job type per day by setting a date parameter
      JobParametersBuilder builder = new JobParametersBuilder()
          .addString("jobDate", LocalDate.now().toString()) // e.g., "2025-04-15"
          .addString("jobLabel", job.getName());

      if (!onlyOncePerDay) {
        builder.addString("jobTime", LocalDateTime.now().toString()); // Allows multiple list migrations runs per day
      }
      JobParameters params = builder.toJobParameters();


      jobOperator.start(job, params);
      return "Job '" + job.getName() + "' launched successfully";

    } catch (Exception e) {
      throw new JobExecutionException("Job launch failed: " + e.getMessage(), e);
    }
  }

  /**
   * Formats a single JobExecution as: [<executionId>] <jobName> <status> <startTime> - <endTime>
   */
  private String formatExecution(JobExecution exec) {
    LocalDateTime start = exec.getStartTime();
    LocalDateTime end   = exec.getEndTime();
    return String.format(
        "Execution ID: %d%n" +
            "Job Name    : %s%n" +
            "Status      : %s%n" +
            "Start Time  : %s%n" +
            "End Time    : %s%n%n",
        exec.getId(),
        exec.getJobInstance().getJobName(),
        exec.getStatus(),
        start == null ? "null" : start,
        end   == null ? "null" : end
    );
  }

}
