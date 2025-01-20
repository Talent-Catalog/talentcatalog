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

package org.tctalent.server.model.db;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.tctalent.server.api.admin.DtoType;
import org.tctalent.server.model.db.task.QuestionTask;
import org.tctalent.server.model.db.task.QuestionTaskAssignment;
import org.tctalent.server.model.db.task.UploadTask;
import org.tctalent.server.model.db.task.UploadTaskAssignment;
import org.tctalent.server.util.dto.DtoBuilder;
import org.tctalent.server.util.dto.DtoPropertyFilter;

/**
 * DTOs for Tasks and TaskAssignments
 *
 * @author John Cameron
 */
public class TaskDtoHelper {

    /**
     * Filters out properties in the DtoBuilder not appropriate to the task type
     */
    static private class TaskDtoPropertyFilter implements DtoPropertyFilter {

        //These properties should only be extracted for QuestionTask's
        private final Set<String> questionOnlyProperties =
            new HashSet<>(Arrays.asList("answer", "candidateAnswerField", "allowedAnswers"));

        //These properties should only be extracted for UploadTask's
        private final Set<String> uploadOnlyProperties =
            new HashSet<>(Arrays.asList("uploadType", "uploadSubfolderName", "uploadableFileTypes"));

        public boolean ignoreProperty(Object o, String property) {
            //Ignore properties which do not exist on task class
            boolean ignore =
                questionOnlyProperties.contains(property) &&
                    ! (o instanceof QuestionTask || o instanceof QuestionTaskAssignment) ||
                uploadOnlyProperties.contains(property) &&
                    ! (o instanceof UploadTask || o instanceof UploadTaskAssignment);

            return ignore;
        }
    };

    public static DtoBuilder getTaskAssignmentDto() {
        return getTaskAssignmentDto(DtoType.FULL);
    }

    public static DtoBuilder getTaskAssignmentDto(DtoType dtoType) {
        return new DtoBuilder(new TaskDtoPropertyFilter())
            .add("id")
            .add("abandonedDate")
            .add("candidateNotes")
            .add("completedDate")
            .add("dueDate")
            .add("status")
            .add("task", getTaskDto(dtoType))
            .add("answer")
            ;
    }

    public static DtoBuilder getTaskDto() {
        return getTaskDto(DtoType.FULL);
    }

    public static DtoBuilder getTaskDto(DtoType dtoType) {
        final DtoBuilder builder = new DtoBuilder(new TaskDtoPropertyFilter())
            .add("id")
            .add("name")
            .add("daysToComplete")
            .add("description")
            .add("content")
            .add("displayName")
            .add("optional")
            .add("helpLink")
            .add("taskType")
            .add("uploadType")
            .add("uploadSubfolderName")
            .add("uploadableFileTypes")
            .add("candidateAnswerField")
            .add("createdDate")
            ;

        if (!DtoType.PREVIEW.equals(dtoType)) {
            builder
                .add("allowedAnswers", getAllowedQuestionTaskAnswerDto())
                .add("createdBy", getUserDto())
            ;
        }
        return builder;
    }

    private static DtoBuilder getAllowedQuestionTaskAnswerDto() {
        return new DtoBuilder()
            .add("name")
            .add("displayName")
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
