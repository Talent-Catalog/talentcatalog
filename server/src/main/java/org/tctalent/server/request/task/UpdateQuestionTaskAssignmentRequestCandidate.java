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

/**
 * Request to update a Question TaskAssignment received through the candidate portal
 *
 * @author John Cameron
 */
@Getter
@Setter
public class UpdateQuestionTaskAssignmentRequestCandidate {

    /**
     * Answer supplied to question. If not empty and abandoned has not been set, this implies that
     * the task assignment is complete.
     */
    String answer;

    /**
     * If task is set as abandoned
     */
    boolean abandoned;

    /**
     * If task has some notes provided by the candidate
     */
    @Nullable
    String candidateNotes;
}
