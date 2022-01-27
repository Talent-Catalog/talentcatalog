/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.springframework.lang.Nullable;
import org.tbbtalent.server.model.db.task.UploadType;

@Entity
@Table(name = "candidate_attachment")
@SequenceGenerator(name = "seq_gen", sequenceName = "candidate_attachment_id_seq", allocationSize = 1)
public class CandidateAttachment extends AbstractAuditableDomainObject<Long>  {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @Enumerated(EnumType.STRING)
    private AttachmentType type;

    private String name;

    /**
     * For links {@link AttachmentType#link} and Google docs
     * {@link AttachmentType#googlefile}, the associated url.
     *
     * For S3 files {@link AttachmentType#file}, it is the unique filename
     * generated on S3.
     */
    private String location;

    /**
     * This is recorded just as the suffix of the attachment filename
     * (ie name attribute) - eg pdf, doc, jpg etc
     */
    private String fileType;

    private boolean migrated;
    private String textExtract;

    //todo Eventually get rid of this cv attribute altogether - replacing it with just uploadType
    //For now they duplicate each other
    private boolean cv;
    @Enumerated(EnumType.STRING)
    private UploadType uploadType;

    @Transient
    @Nullable
    private String url;

    public CandidateAttachment() {
    }

    public String getUrl() {
        if (type == AttachmentType.file) {
            url = "https://s3.us-east-1.amazonaws.com/files.tbbtalent.org/candidate/" + (migrated ? "migrated" : candidate.getCandidateNumber()) + '/' + location;
        } else {
            url = location;
        }
        return url;
    }


    public Candidate getCandidate() {
        return candidate;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

    public AttachmentType getType() {
        return type;
    }

    public void setType(AttachmentType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public boolean isMigrated() {
        return migrated;
    }

    public void setMigrated(boolean migrated) {
        this.migrated = migrated;
    }

    public boolean isCv() { return cv; }

    public void setCv(boolean cv) { this.cv = cv; }

    public String getTextExtract() { return textExtract; }

    public void setTextExtract(String textExtract) { this.textExtract = textExtract; }

    public UploadType getUploadType() {
        return uploadType;
    }

    public void setUploadType(UploadType uploadType) {
        this.uploadType = uploadType;
    }
}
