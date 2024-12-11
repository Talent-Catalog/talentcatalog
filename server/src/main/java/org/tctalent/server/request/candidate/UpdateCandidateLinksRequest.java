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

package org.tctalent.server.request.candidate;

import jakarta.validation.constraints.NotNull;

public class UpdateCandidateLinksRequest {

    @NotNull
    private Long candidateId;

    private String sflink;
    private String folderlink;
    private String videolink;
    private String linkedInLink;

    public UpdateCandidateLinksRequest() {
    }

    public UpdateCandidateLinksRequest(String sflink, String folderlink,
                                       String videolink) {
        this.sflink = sflink;
        this.folderlink = folderlink;
        this.videolink = videolink;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }

    public String getSflink() {
        return sflink;
    }

    public void setSflink(String sflink) {
        this.sflink = sflink;
    }

    public String getFolderlink() {
        return folderlink;
    }

    public void setFolderlink(String folderlink) {
        this.folderlink = folderlink;
    }

    public String getVideolink() {
        return videolink;
    }

    public void setVideolink(String videolink) {
        this.videolink = videolink;
    }

    public String getLinkedInLink() { return linkedInLink; }

    public void setLinkedInLink(String linkedInLink) { this.linkedInLink = linkedInLink; }
}
