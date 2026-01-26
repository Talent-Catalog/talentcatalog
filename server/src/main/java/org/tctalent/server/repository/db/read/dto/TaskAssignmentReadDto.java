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

package org.tctalent.server.repository.db.read.dto;

import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.task.TaskType;
import org.tctalent.server.repository.db.read.annotation.JsonOneToOne;
import org.tctalent.server.repository.db.read.annotation.SqlDefaults;
import org.tctalent.server.repository.db.read.annotation.SqlIgnore;
import org.tctalent.server.repository.db.read.annotation.SqlTable;

@Getter
@Setter
@SqlTable(name="task_assignment", alias = "ctaska")
@SqlDefaults(mapUnannotatedColumns = true)
public class TaskAssignmentReadDto {
    private OffsetDateTime abandonedDate;

    @SqlIgnore //Could be manually populated - but for now it is not being populated
    private String answer;
    private String candidateNotes;
    private OffsetDateTime completedDate;
    private OffsetDateTime dueDate;
    private Long id;
    private Status status;

    @JsonOneToOne(joinColumn = "task_id")
    private TaskReadDto task;

    private TaskType taskType;
}
