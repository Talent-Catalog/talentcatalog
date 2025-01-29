/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.logging;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.springframework.beans.factory.ObjectProvider;
import org.tctalent.server.model.db.User;

/**
 * The {@code LogBuilder} class is used to construct log messages with standardised fields
 * and log them using SLF4J. The fields are specified using the {@link LogField} enum and are
 * included in the log messages based on their sort order.
 * <p>
 * Example Usage:
 *
 * <pre>
 * {@code
 * LogBuilder.builder(logger)
 *     .user(authService.getLoggedInUser())
 *     .jobId(300L)
 *     .action("CreateJob")
 *     .message("Creating a new job entry")
 *     .logInfo();
 * }
 * </pre>
 *
 * // Output: uid: 12345 | jid: 200 | action: CreateJob | msg: Creating a new job entry
 * <p>
 * In this example, a log message is constructed with the user ID, job ID, action, and a custom
 * message, and then logged at the INFO level using SLF4J.
 *
 * <p>
 * This class also integrates with system metrics to optionally include CPU and memory utilisation
 * in log messages. These metrics are configurable using the following properties:
 * <ul>
 *   <li>{@code logbuilder.includeCpuUtilisation}: When enabled, includes CPU utilisation in the
 *   log messages.</li>
 *   <li>{@code logbuilder.includeMemoryUtilization}: When enabled, includes memory utilisation in
 *   the log messages.</li>
 * </ul>
 *
 * If {@code logCpu} or {@code logMemory} is enabled, the log message may also include system metrics:
 * <p>
 * // cpu: 50.00% | mem: 25.00% | uid: 12345 | jid: 200 | action: CreateJob | msg: Creating a new job entry
 *
 * @author sadatmalik
 */
public class LogBuilder {

  private static ObjectProvider<SystemMetricsImpl> systemMetricsProvider;
  private static boolean logCpu;
  private static boolean logMemory;

  private final Logger logger;
  private final Map<LogField, String> logFields;

  /**
   * Constructs a new {@code LogBuilder} with the specified {@link Logger}.
   *
   * @param logger the SLF4J logger to use for logging
   */
  private LogBuilder(Logger logger) {
    this.logger = logger;
    this.logFields = new EnumMap<>(LogField.class);
  }

  // Static method to set the SystemMetrics provider for all LogBuilder instances
  public static void setSystemMetricsProvider(ObjectProvider<SystemMetricsImpl> provider) {
    LogBuilder.systemMetricsProvider = provider;
  }

  // Static method to set the logCpu flag for all LogBuilder instances
  public static void setLogCpu(boolean logCpu) {
    LogBuilder.logCpu = logCpu;
  }

  // Static method to set the logMemory flag for all LogBuilder instances
  public static void setLogMemory(boolean logMemory) {
    LogBuilder.logMemory = logMemory;
  }

  /**
   * Creates a new {@code LogBuilder} instance with the specified {@link Logger}.
   *
   * @param logger the SLF4J logger to use for logging
   * @return a new instance of {@code LogBuilder}
   */
  public static LogBuilder builder(Logger logger) {
    return new LogBuilder(logger);
  }

  /**
   * Adds the user ID to the log fields if the user is present.
   *
   * @param loggedInUser an {@link Optional} containing the logged-in user
   * @return the current {@code LogBuilder} instance for chaining
   */
  public LogBuilder user(Optional<User> loggedInUser) {
    loggedInUser
        .map(User::getId)
        .map(Object::toString)
        .ifPresent(userId -> addField(LogField.USER_ID, userId));
    return this;
  }

  /**
   * Adds the candidate ID to the log fields if the candidate ID is not null.
   *
   * @param candidateId the candidate ID
   * @return the current {@code LogBuilder} instance for chaining
   */
  public LogBuilder candidateId(Long candidateId) {
    if (candidateId == null) {
      return this;
    }
    addField(LogField.CANDIDATE_ID, candidateId.toString());
    return this;
  }

  /**
   * Adds the list ID to the log fields if the list ID is not null.
   *
   * @param listId the list ID
   * @return the current {@code LogBuilder} instance for chaining
   */
  public LogBuilder listId(Long listId) {
    if (listId == null) {
      return this;
    }
    addField(LogField.LIST_ID, listId.toString());
    return this;
  }

  /**
   * Adds the search ID to the log fields if the search ID is not null.
   *
   * @param searchId the search ID
   * @return the current {@code LogBuilder} instance for chaining
   */
  public LogBuilder searchId(Long searchId) {
    if (searchId == null) {
      return this;
    }
    addField(LogField.SEARCH_ID, searchId.toString());
    return this;
  }

  /**
   * Adds the job ID to the log fields if the job ID is not null.
   *
   * @param jobId the job ID
   * @return the current {@code LogBuilder} instance for chaining
   */
  public LogBuilder jobId(Long jobId) {
    if (jobId == null) {
      return this;
    }
    addField(LogField.JOB_ID, jobId.toString());
    return this;
  }

  /**
   * Adds the job opportunity ID to the log fields if the job opportunity ID is not null.
   *
   * @param jobOppId the job opportunity ID
   * @return the current {@code LogBuilder} instance for chaining
   */
  public LogBuilder jobOppId(Long jobOppId) {
    if (jobOppId == null) {
      return this;
    }
    addField(LogField.JOB_OPP_ID, jobOppId.toString());
    return this;
  }

  /**
   * Adds the candidate opportunity ID (case ID) to the log fields if the candidate opportunity ID
   * is not null.
   *
   * @param caseId the candidate opportunity ID
   * @return the current {@code LogBuilder} instance for chaining
   */
  public LogBuilder caseId(Long caseId) {
    if (caseId == null) {
      return this;
    }
    addField(LogField.CASE_ID, caseId.toString());
    return this;
  }

  /**
   * Adds the action to the log fields if the action is not null.
   *
   * @param action the action
   * @return the current {@code LogBuilder} instance for chaining
   */
  public LogBuilder action(String action) {
    if (action == null) {
      return this;
    }
    addField(LogField.ACTION, action);
    return this;
  }

  /**
   * Adds the message to the log fields if the message is not null.
   *
   * @param message the message
   * @return the current {@code LogBuilder} instance for chaining
   */
  public LogBuilder message(String message) {
    if (message == null) {
      return this;
    }
    addField(LogField.MESSAGE, message);
    return this;
  }

  /**
   * Logs the constructed message at the INFO level.
   */
  public void logInfo() {
    logger.info(buildLogMessage());
  }

  /**
   * Logs the constructed message at the WARN level.
   */
  public void logWarn() {
    logger.warn(buildLogMessage());
  }

  /**
   * Logs the constructed message with exception at the WARN level.
   */
  public void logWarn(Exception e) {
    logger.warn(buildLogMessage(), e);
  }

  /**
   * Logs the constructed message at the ERROR level.
   */
  public void logError() {
    logger.error(buildLogMessage());
  }

  /**
   * Logs the constructed message with exception at the ERROR level.
   */
  public void logError(Exception e) {
    logger.error(buildLogMessage(), e);
  }

  /**
   * Logs the constructed message at the DEBUG level.
   */
  public void logDebug() {
    logger.debug(buildLogMessage());
  }

  /**
   * Constructs the log message by populating the log fields with CPU and memory utilization
   * metrics (if enabled) and sorting them based on their sort order. The log fields are then
   * concatenated into a single string with each key-value pair separated by a delimiter.
   * <p>
   * If the {@code systemMetricsProvider} is available, the method retrieves the system metrics
   * (CPU and memory utilization) and includes them in the log message if the corresponding flags
   * {@code logCpu} or {@code logMemory} are enabled.
   * <p>
   * Log fields with null values are excluded from the final log message.
   *
   * @return the constructed log message
   */
  private String buildLogMessage() {

    if (systemMetricsProvider != null) {
      SystemMetricsImpl systemMetrics = systemMetricsProvider.getIfAvailable();
      if (systemMetrics != null) {
        if (logCpu) {
          String cpuUsage = systemMetrics.getCpuUtilization();
          logFields.put(LogField.CPU_UTILIZATION, cpuUsage);
        }
        if (logMemory) {
          String memoryUsage = systemMetrics.getMemoryUtilization();
          logFields.put(LogField.MEMORY_UTILIZATION, memoryUsage);
        }
      }
    }

    return logFields.entrySet().stream()
        .filter(entry -> entry.getValue() != null)
        .sorted(Map.Entry.comparingByKey(Comparator.comparingInt(LogField::getSortOrder)))
        .map(entry -> entry.getKey().getLabel() + ": " + entry.getValue())
        .collect(Collectors.joining(" | "));
  }

  /**
   * Adds a field to the log fields map.
   *
   * @param field the log field
   * @param value the value of the log field
   */
  private void addField(LogField field, String value) {
    logFields.put(field, value);
  }

}
