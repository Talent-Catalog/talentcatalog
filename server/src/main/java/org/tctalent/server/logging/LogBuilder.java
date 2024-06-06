/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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
import java.util.TreeMap;
import org.slf4j.Logger;
import org.tctalent.server.model.db.User;

public class LogBuilder {
  private final Logger logger;
  private final Map<LogField, String> logFields;

  private LogBuilder(Logger logger) {
    this.logger = logger;
    this.logFields = new EnumMap<>(LogField.class);
  }

  public static LogBuilder builder(Logger logger) {
    return new LogBuilder(logger);
  }

  public LogBuilder user(Optional<User> loggedInUser) {
    loggedInUser
        .map(User::getId)
        .map(Object::toString)
        .ifPresent(userId -> addField(LogField.USER_ID, userId));
    return this;
  }

  public LogBuilder listId(Long listId) {
    if (listId == null) {
      return this;
    }
    addField(LogField.LIST_ID, listId.toString());
    return this;
  }

  public LogBuilder searchId(Long searchId) {
    if (searchId == null) {
      return this;
    }
    addField(LogField.SEARCH_ID, searchId.toString());
    return this;
  }

  public LogBuilder jobId(Long jobId) {
    if (jobId == null) {
      return this;
    }
    addField(LogField.JOB_ID, jobId.toString());
    return this;
  }

  public LogBuilder action(String action) {
    if (action == null) {
      return this;
    }
    addField(LogField.ACTION, action);
    return this;
  }

  public LogBuilder message(String message) {
    if (message == null) {
      return this;
    }
    addField(LogField.MESSAGE, message);
    return this;
  }

  private void addField(LogField field, String value) {
    logFields.put(field, value);
  }

  public void logInfo() {
    logger.info(buildLogMessage());
  }

  public void logError() {
    logger.error(buildLogMessage());
  }

  public void logDebug() {
    logger.debug(buildLogMessage());
  }

  private String buildLogMessage() {
    StringBuilder sb = new StringBuilder();

    // Sort log fields based on sortOrder
    TreeMap<LogField, String> sortedLogFields =
        new TreeMap<>(Comparator.comparingInt(LogField::getSortOrder));

    sortedLogFields.putAll(logFields);

    sortedLogFields.forEach((field, value) -> {
      if (value != null) {
        sb.append(field.getLabel()).append(": ").append(value).append(" | ");
      }
    });

    // Remove the trailing " | " if present
    if (!sb.isEmpty()) {
      sb.setLength(sb.length() - 3);
    }

    return sb.toString();
  }

}
