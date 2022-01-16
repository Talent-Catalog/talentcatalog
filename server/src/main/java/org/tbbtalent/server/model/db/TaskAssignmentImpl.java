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

package org.tbbtalent.server.model.db;

import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;
import org.tbbtalent.server.model.db.task.Task;
import org.tbbtalent.server.model.db.task.TaskAssignment;

@Getter
@Setter
/**
 * Default implementation of {@link TaskAssignment}
 *
 * @author John Cameron
 */
public class TaskAssignmentImpl implements TaskAssignment {
    User activatedBy;
    OffsetDateTime activatedDate;
    Candidate candidate;
    String candidateNotes;
    String candidateResponse;
    OffsetDateTime completedDate;
    User deactivatedBy;
    OffsetDateTime deactivatedDate;
    OffsetDateTime dueDate;
    SavedList relatedList;
    Status status;
    Task task;

}
