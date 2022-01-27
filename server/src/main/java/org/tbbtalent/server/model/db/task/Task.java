/*
 * Copyright (c) 2022 Talent Beyond Boundaries.
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

package org.tbbtalent.server.model.db.task;

import java.util.Set;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

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
     * Link which refers to help for a candidate on what they need to do to complete this task.
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
     * Name describing the task. For Question tasks, this is the question to be answered.
     * @return Name of task (question for Question tasks).
     */
    @NonNull
    String getName();

    /**
     * Optional tasks do not trigger alerts if they are overdue.
     * @return True if optional
     */
    boolean isOptional();

    //todo This is not fully implemented yet. Maybe we should not do it this way.
    //Alternative is to have a separate "TaskList" object. The advantage of this way is the
    //nesting ability. One issue with that is that when passing object up to front end only
    //Task attributes are sent - so this needs to be in base class even though it does not
    //make sense for, for example, an UploadTask to also be a task with subtasks.
    //It needs to be it's own task type - eg ParentTask - but the subTasks atribute has to be
    //part of Task to get serialized up to Angular. Then just need convention that subtasks should
    //be null (or ignored) if TaskType is not Parent.
    //Anyway, we don't need it for day one - it is just a convenience for grouping tasks together,
    //so come back to this later.
    /**
     * Subtasks - this task involves carrying out these sub tasks.
     * @return May be null - in which case this is a simple task, rather than a list of sub tasks.
     */
    @Nullable
    Set<? extends Task> getSubtasks();
}
