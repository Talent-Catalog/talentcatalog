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

import java.util.List;
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
     * Name describing the task.
     * @return Name of task.
     */
    @NonNull
    String getName();

    /**
     * Optional tasks do not trigger alerts if they are overdue.
     * @return True if optional
     */
    boolean isOptional();

    /**
     * Subtasks - this task involves carrying out these sub tasks.
     * @return May be null - in which case this is a simple task, rather than a list of sub tasks.
     */
    @Nullable
    List<Task> getSubtasks();

    /**
     * Type of task - replacing class hierarchy.
     * <p/>
     * The type of the task determines which attributes are used for a particular instance -
     * reflecting the various Task interfaces.
     * @return Type of this task
     */
    @NonNull
    TaskType getType();
}
