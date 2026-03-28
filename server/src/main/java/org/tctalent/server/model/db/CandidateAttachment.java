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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import org.tctalent.server.files.UploadType;

@Getter
@Setter
@Entity
@Table(name = "candidate_attachment")
@SequenceGenerator(name = "seq_gen", sequenceName = "candidate_attachment_id_seq", allocationSize = 1)
public class CandidateAttachment extends AbstractAuditableDomainObject<Long> implements ICandidateAttachment {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @Enumerated(EnumType.STRING)
    private AttachmentType type;

    private String name;

    /**
     * The attachment's public url.
     * <p>
     * Historical note: This field maps to the "location" database column for backward
     * compatibility. Prior to 2026, this column stored relative S3 paths for file-type
     * attachments. Migration V1_400 converted all paths to full URLs.
     */
    @Column(name = "location") //Originally this field was called location
    private String url;

    /**
     * This is recorded just as the suffix of the attachment filename
     * (ie name attribute) - eg pdf, doc, jpg etc
     */
    private String fileType;

    private boolean migrated;

    //TODO JC This needs to map to database - make transient now so that it compiles
    @Transient
    private String storageKey;

    private String textExtract;

    //todo Eventually get rid of this cv attribute altogether - replacing it with just uploadType
    //For now they duplicate each other
    private boolean cv;

    @Enumerated(EnumType.STRING)
    private UploadType uploadType;

    public CandidateAttachment() {
    }
}
