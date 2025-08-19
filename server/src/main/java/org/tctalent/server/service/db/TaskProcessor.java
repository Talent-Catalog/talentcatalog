/*
 * Copyright (c) 2025 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.db;

import org.tctalent.server.model.db.TaskAssignmentImpl;
import org.tctalent.server.model.db.task.TaskAssignment;
import org.tctalent.server.model.db.task.TaskType;
import org.tctalent.server.service.db.impl.TaskCompletionContext;

/**
 * Defines the contract for processing different types of {@link TaskAssignment}s.
 * <p>
 * Implementations of this interface encapsulate the logic for creating,
 * completing, and post-processing assignments of a specific {@link TaskType}.
 * This allows the system to support multiple task types with their own
 * lifecycle handling in a consistent way.
 */
public interface TaskProcessor {

  /**
   * Returns the {@link TaskType} handled by this processor.
   *
   * @return the task type this processor is responsible for
   */
  TaskType getTaskType();

  /**
   * Creates a new {@link TaskAssignmentImpl} instance for this processor's task type.
   * <p>
   * Typically used when a new task assignment needs to be generated
   * and persisted for a user or workflow.
   *
   * @return a newly created task assignment
   */
  TaskAssignmentImpl createTaskAssignment();

  /**
   * Completes the given {@link TaskAssignmentImpl} based on the provided context.
   * <p>
   * Implementations should update the assignment state, validate inputs,
   * and apply any side effects related to task completion.
   *
   * @param assignment the task assignment to complete
   * @param context    additional contextual information for task completion
   * @return the updated task assignment after completion
   */
  TaskAssignmentImpl completeTask(TaskAssignmentImpl assignment, TaskCompletionContext context);

  /**
   * Performs any post-processing logic once a {@link TaskAssignmentImpl} has been completed.
   *
   * @param assignment the completed task assignment
   */
  void handleCompletion(TaskAssignmentImpl assignment);
}
