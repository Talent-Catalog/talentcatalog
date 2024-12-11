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

package org.tctalent.server.model.db.task;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.tctalent.server.model.db.Auditable;

/**
 * Describes a task which can be assigned to candidates.
 *
 * @author John Cameron
 */
public interface Task extends Auditable {

    /**
     * True if this task is expected to be carried out by TBB staff - rather, or as well as,
     * the candidate. For example, a candidate intake.
     * @return True if an admin task, false if the task is completely the responsibility of the
     * candidate.
     */
    boolean isAdmin();

    /**
     * Number of days estimated needed to complete this task.
     * Used to default the due date for associated {@link TaskAssignment}.
     * @return Number of days. May be 0 for optional tasks.
     */
    @Nullable
    Integer getDaysToComplete();

    /**
     * Optional description of task
     * @return description of task
     */
    @Nullable
    String getDescription();

    /**
     * Displayed name describing the task. For Question tasks, this is the question to be answered.
     * @return Displayed name of task (question for Question tasks).
     */
    @NonNull
    String getDisplayName();

    /**
     * Link which refers to help for a candidate on what they need to do to complete this task.
     * If a help link exists it will be linked to in a help button in the candidate task view.
     * If linking to a document, this can be embedded into the page in simple task's (e.g. display document in page to allow checkbox agreement).
     * @return Typically a link to a document or web page.
     */
    @Nullable
    String getHelpLink();

    /**
     * Unique id identifying this task
     * @return Task id
     */
    Long getId();

    /**
     * Name of task. For Question tasks, this name is used to store the answer as a property
     * (if needed). The name must be unique.
     * @return Name of task.
     */
    @NonNull
    String getName();

    /**
     * Optional tasks do not trigger alerts if they are overdue.
     * Default to false (ie not optional) if it has not explicitly been set.
     * @return True if optional
     */
    boolean isOptional();

    /**
     * Type of task - this encodes the class type - so {@link TaskType#Simple} for a simple task,
     * {@link TaskType#Upload} for an UploadTask etc.
     * This allows the class type information of any task to be passed to Angular through JSON
     * serialization. Otherwise, we lose that type information when tasks objects are returned
     * through our REST Api to Angular.
     * <p/>
     * This method should be overridden by extending interfaces, to provide their related type.
     */
    default TaskType getTaskType() {
        return TaskType.Simple;
    }

}
