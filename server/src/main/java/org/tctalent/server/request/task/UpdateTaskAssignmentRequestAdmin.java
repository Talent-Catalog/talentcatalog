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

package org.tctalent.server.request.task;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

import java.time.LocalDate;

/**
 * Request to update a TaskAssignment received through the admin portal
 *
 * @author John Cameron
 */
@Getter
@Setter
public class UpdateTaskAssignmentRequestAdmin {

    /**
     * Custom due date if supplied (otherwise the due date will be set from the task days to complete)
     */
    @Nullable
    LocalDate dueDate;

    /**
     * If task is set as completed or not
     */
    boolean completed;

    /**
     * If task is set as abandoned or not
     */
    boolean abandoned;

    /**
     * If task has some notes provided by the candidate
     */
    @Nullable
    String candidateNotes;
}
