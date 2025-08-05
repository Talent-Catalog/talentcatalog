package org.tctalent.server.service.db;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.lang.Nullable;

/**
 * Service interface for managing and running Spring Batch jobs.
 * <p>
 * Provides operations to launch, stop and restart the candidate migration jobs and to retrieve
 * a plain-text summary of recent job executions.
 * </p>
 */
public interface BatchJobService {

  /**
   * Builds a plain-text summary of job executions.
   */
  String getJobExecutionsSummary();

  /**
   * Launches the given job.
   * Returns a confirmation message or throws on failure.
   * @param job Job to be launched
   * @param label Name assigned to job
   * @param listId //TODO JC This needs to be another parameter for job
   * @return Confirmation message
   * @throws JobExecutionException if the job launch fails
   */
  String launchJob(
      Job job, String label, @Nullable Long listId) throws JobExecutionException;

  /**
   * Attempts to stop a running job execution by ID.
   */
  String stopJobExecution(Long executionId) throws Exception;

  /**
   * Attempts to restart a job execution by ID.
   */
  String restartJobExecution(Long executionId) throws Exception;

}
