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

import org.tbbtalent.server.util.dto.DtoBuilder;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
public class TaskDtoHelper {

    public static DtoBuilder getTaskAssignmentDto() {
        return new DtoBuilder()
            // TODO: other attributes
            //todo If we are going to be mapping everything, do we need a dto?
            .add("id")
            .add("abandonedDate")
            .add("candidateNotes")
            .add("completedDate")
            .add("dueDate")
            .add("task", getTaskDto())
            ;
    }

    public static DtoBuilder getTaskDto() {
        return new DtoBuilder()
            // TODO: other attributes
            .add("id")
            .add("name")
            .add("daysToComplete")
            .add("description")
            .add("optional")
            .add("taskType")
            ;
    }


}
