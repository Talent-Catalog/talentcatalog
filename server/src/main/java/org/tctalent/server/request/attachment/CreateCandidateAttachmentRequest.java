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

package org.tctalent.server.request.attachment;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;
import org.tctalent.server.model.db.AttachmentType;
import org.tctalent.server.model.db.task.UploadType;

@Getter
@Setter
public class CreateCandidateAttachmentRequest {

    private Long candidateId;

    @Enumerated(EnumType.STRING)
    private AttachmentType type;

    private String name;

    /**
     * Currently this is the file suffix - eg pdf, docx, jpg, etc
     */
    private String fileType;

    /**
     * For links {@link AttachmentType#link} and
     * Google docs {@link AttachmentType#googlefile}, the associated url.
     * For S3 files {@link AttachmentType#file}, it is the unique filename
     * generated on S3.
     */
    private String location;

    private Boolean cv;

    private UploadType uploadType;

    /**
     * Only used by attachments stored on S3
     */
    private String folder;

    /**
     * Used for Google files where the text extraction is done earlier.
     */
    private String textExtract;



}

