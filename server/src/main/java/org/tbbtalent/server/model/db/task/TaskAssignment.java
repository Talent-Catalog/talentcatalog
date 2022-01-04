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

import java.time.OffsetDateTime;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.SavedList;
import org.tbbtalent.server.model.db.Status;
import org.tbbtalent.server.model.db.User;

/**
 * Represents the assignment of a task to a candidate.
 *
 * @author John Cameron
 */
public interface TaskAssignment {

    /**
     * The person who activated this assignment. This will be a TBB admin.
     * <p/>
     * This will also be the person who is responsible for the assignment, because tasks are
     * automatically active when they are created. See {@link #getStatus()}.
     * <p/>
     * Assignment may be made by directly assigning a task to a particular candidate, or
     * indirectly by assigning a task to a list, which is then automatically assigned to all
     * candidates in that list (including candidates who may subsequently be added to the list).
     * @return Assigning user.
     */
    @NonNull
    User getActivatedBy();

    /**
     * Time at which this assignment was made (and therefore activated).
     * @return Date/time of assignment
     */
    @NonNull
    OffsetDateTime getActivatedDate();

    /**
     * Candidate associated with this task assignment.
     * <p/>
     * See also {@link #getTask()}
     * @return Candidate assigned the task
     */
    @NonNull
    Candidate getCandidate();

    /**
     * Time when task was completed - initially null.
     * @return Task completion time, null if not completed.
     */
    @Nullable
    OffsetDateTime getCompletedDate();

    /**
     * The person who deactivated this assignment. This will be a TBB admin.
     * <p/>
     * Assignment may be deactivated directly, or indirectly by removing a candidate from
     * the list which originally triggered the task assignment.
     * See {@link #getActivatedBy()}
     * @return Deactivating user. Null if task assignment has not been deactivated.
     */
    @Nullable
    User getDeactivatedBy();

    /**
     * Time at which this assignment was deactivated.
     * @return Date/time of deactivation. Null if assignment has not been deactivated.
     */
    @Nullable
    OffsetDateTime getDeactivatedDate();

    /**
     * Date by which the task should be completed.
     * <p/>
     * This defaults based on {@link Task#getDaysToComplete()}.
     * @return Due date/time. Can be null for optional tasks.
     */
    @Nullable
    OffsetDateTime getDueDate();

    /**
     * List through which the candidate was assigned the task.
     * <p/>
     * Null if the candidate was assigned this task directly, rather than automatically through
     * a list.
     */
    @Nullable
    SavedList getRelatedList();

    /**
     * Only active task assignments can trigger overdue alerts.
     * <p/>
     * Task assignments are automatically active when they are created.
     * <p/>
     * See also {@link #getActivatedBy()}, {@link #getActivatedDate()}, {@link #getDeactivatedBy()},
     * and {@link #getDeactivatedDate()} which provide details on who changed this status and when.
     * @return Status of assignment
     */
    @NonNull
    Status getStatus();

    /**
     * Task assigned to candidate.
     * <p/>
     * See also {@link #getCandidate()}
     * @return Task to be completed by candidate
     */
    @NonNull
    Task getTask();
}
