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

import org.tbbtalent.server.model.db.task.TaskType;
import org.tbbtalent.server.util.dto.DtoBuilder;

/**
 * DTOs for Tasks
 *
 * @author John Cameron
 */
public class TaskDtoHelper {

    public static DtoBuilder getTaskAssignmentDto() {
        return new DtoBuilder()
            .add("id")
            .add("abandonedDate")
            .add("candidateNotes")
            .add("completedDate")
            .add("abandonedDate")
            .add("dueDate")
            .add("status")
            .add("candidateNotes")
            .add("task", getTaskDto())
            ;
    }

    public static DtoBuilder getTaskDto() {
        return new DtoBuilder("taskType")
            // TODO: other attributes
            .add("id")
            .add("name")
            .add("daysToComplete")
            .add("description")
            .add("optional")
            .add("helpLink")
            .add("taskType")
            .add("uploadType", TaskType.Upload)
            .add("uploadSubfolderName", TaskType.Upload)
            .add("uploadableFileTypes", TaskType.Upload)
            .add("candidateAnswerField", TaskType.Question)
            .add("createdBy", getUserDto())
            .add("createdDate")
            ;
    }

    public static DtoBuilder getUserDto() {
        return new DtoBuilder()
            .add("id")
            .add("firstName")
            .add("lastName")
            ;
    }


}
