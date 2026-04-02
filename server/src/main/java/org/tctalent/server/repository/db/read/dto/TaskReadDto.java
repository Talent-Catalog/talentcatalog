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
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.tctalent.server.files.UploadType;
import org.tctalent.server.model.db.task.TaskType;
import org.tctalent.server.repository.db.read.annotation.JsonOneToOne;
import org.tctalent.server.repository.db.read.annotation.SqlColumn;
import org.tctalent.server.repository.db.read.annotation.SqlDefaults;
import org.tctalent.server.repository.db.read.annotation.SqlIgnore;
import org.tctalent.server.repository.db.read.annotation.SqlTable;

@Getter
@Setter
@SqlTable(name="task", alias = "ctask")
@SqlDefaults(mapUnannotatedColumns = true)
public class TaskReadDto {
    @SqlIgnore  //This is manually populated (from explicitAllowedAnswers)
    private List<AllowedQuestionTaskAnswerReadDto> allowedAnswers;
    private String candidateAnswerField;

    @JsonOneToOne(joinColumn = "candidate_form_id")
    private CandidateFormReadDto candidateForm;

    @JsonOneToOne(joinColumn = "created_by")
    private UserShortReadDto createdBy;
    private OffsetDateTime createdDate;
    private Integer daysToComplete;
    private String description;
    private String displayName;
    private String docLink;

    @SqlColumn(transform = "to_jsonb(string_to_array(%s, ','))") //Convert csv string to jsonb array
    private List<String> explicitAllowedAnswers;

    private Long id;
    private String name;
    private boolean optional;

    private TaskType taskType;

    @SqlColumn(transform = "to_jsonb(string_to_array(%s, ','))") //Convert csv string to jsonb array
    private List<String> uploadableFileTypes;
    private String uploadSubfolderName;
    private UploadType uploadType;
}
