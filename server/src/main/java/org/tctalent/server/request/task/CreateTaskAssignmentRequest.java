/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

package org.tctalent.server.request.task;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

import java.time.LocalDate;

/**
 * Request to create a TaskAssignment by assigning a task to a candidate
 *
 * @author John Cameron
 */
@Getter
@Setter
public class CreateTaskAssignmentRequest {

    /**
     * Candidate to which task is being assigned
     */
    long candidateId;

    /**
     * Task to assign to candidate
     */
    long taskId;

    /**
     * Custom due date if supplied (otherwise the due date will be set from the task days to complete)
     */
    @Nullable
    LocalDate dueDate;
}
