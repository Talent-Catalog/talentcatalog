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
import org.tctalent.server.files.UploadType;
import org.tctalent.server.model.db.AttachmentType;
import org.tctalent.server.repository.db.read.annotation.JsonOneToOne;
import org.tctalent.server.repository.db.read.annotation.SqlColumn;
import org.tctalent.server.repository.db.read.annotation.SqlDefaults;
import org.tctalent.server.repository.db.read.annotation.SqlTable;

@Getter
@Setter
@SqlTable(name="candidate_attachment", alias = "cat")
@SqlDefaults(mapUnannotatedColumns = true)
public class CandidateAttachmentReadDto {
    private OffsetDateTime createdDate;
    @JsonOneToOne(joinColumn = "created_by")
    private UserReadDto createdBy;
    private boolean cv;
    private String fileType;
    private Long id;

    //Name on database is still "location".
    @SqlColumn(name = "location")
    private String url;
    private boolean migrated;
    private String name;
    private AttachmentType type;
    private UploadType uploadType;
    private String folder;
}
