/*
 * Copyright (c) 2025 Talent Catalog.
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

package org.tctalent.server.service.db.impl;

import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;
import org.tctalent.server.model.db.TaskAssignmentImpl;
import org.tctalent.server.model.db.task.TaskType;
import org.tctalent.server.service.db.TaskProcessor;

// Simple Task Processor
@Service
public class SimpleTaskProcessor implements TaskProcessor {
  @Override
  public TaskType getTaskType() {
    return TaskType.Simple;
  }

  @Override
  public TaskAssignmentImpl createTaskAssignment() {
    return new TaskAssignmentImpl();
  }

  @Override
  public TaskAssignmentImpl completeTask(TaskAssignmentImpl assignment, TaskCompletionContext context) {
    assignment.setCompletedDate(OffsetDateTime.now());
    return assignment;
  }

  @Override
  public void handleCompletion(TaskAssignmentImpl assignment) {
  }
}