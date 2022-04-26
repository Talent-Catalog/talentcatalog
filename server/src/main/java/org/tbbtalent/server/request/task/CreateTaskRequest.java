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

package org.tbbtalent.server.request.task;

import lombok.Getter;
import lombok.Setter;
import org.tbbtalent.server.model.db.CandidateSubfolderType;
import org.tbbtalent.server.model.db.task.TaskType;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Request coming from admin portal when creating a new task.
 * todo could I seperate this request into create QuestionTaskRequest and UploadTaskRequest?
 *
 * @author Caroline Cameron
 */
@Getter
@Setter
public class CreateTaskRequest {
    @NotNull
    private TaskType taskType;

    @NotBlank
    private String name;

    @NotBlank
    private String displayName;

    @NotBlank
    private String description;

    @NotNull
    private Integer daysToComplete;

    private String helpLink;

    @NotNull
    private boolean optional;

    private CandidateSubfolderType uploadSubfolderName;

    private String uploadableFileTypes;

    private String candidateAnswerField;

    private String allowedAnswers;
}
