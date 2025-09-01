package org.tctalent.server.service.db.impl;

import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

/**
 * Context object used when completing a task assignment.
 * <p>
 * This class encapsulates all relevant data needed for task completion,
 * including uploaded files, metadata field answers, and the candidate ID.
 * It provides a fluent API for setting these properties.
 * </p>
 *
 * <p><strong>Usage example:</strong></p>
 * <pre>
 * TaskCompletionContext context = new TaskCompletionContext()
 *     .setCandidateId(123L)
 *     .addFiles(files)
 *     .addFieldAnswers(metadataMap);
 * </pre>
 *
 * <ul>
 *   <li>{@code files} – the files uploaded as part of task completion</li>
 *   <li>{@code fieldAnswers} – a map of field names to values, representing metadata answers</li>
 *   <li>{@code candidateId} – the ID of the candidate completing the task</li>
 * </ul>
 */
public class TaskCompletionContext {

  /** Array of files uploaded for this task. */
  MultipartFile[] files;

  /** Metadata field answers for this task, keyed by field name. */
  Map<String, String> fieldAnswers;

  /** The candidate ID associated with this task completion. */
  Long candidateId;

  /**
   * Adds a single file to the context.
   *
   * @param file the file to add
   * @return this context instance for fluent API usage
   */
  public TaskCompletionContext addFile(MultipartFile file) {
    this.files = new MultipartFile[]{file};
    return this;
  }

  /**
   * Adds multiple files to the context.
   *
   * @param files the array of files to add
   * @return this context instance for fluent API usage
   */
  public TaskCompletionContext addFiles(MultipartFile[] files) {
    this.files = files;
    return this;
  }

  /**
   * Sets metadata field answers for this task.
   *
   * @param fieldAnswers a map of field names to their corresponding answers
   * @return this context instance for fluent API usage
   */
  public TaskCompletionContext addFieldAnswers(Map<String, String> fieldAnswers) {
    this.fieldAnswers = fieldAnswers;
    return this;
  }

  /**
   * Sets the candidate ID for this task completion context.
   *
   * @param candidateId the candidate ID
   * @return this context instance for fluent API usage
   */
  public TaskCompletionContext setCandidateId(Long candidateId) {
    this.candidateId = candidateId;
    return this;
  }
}
