package org.tbbtalent.server.request.candidate;

import javax.validation.constraints.NotNull;

import org.tbbtalent.server.model.CandidateStatus;

public class UpdateCandidateLinksRequest {

    @NotNull
    private Long candidateId;

    private String sflink;
    private String folderlink;

    public UpdateCandidateLinksRequest() {
    }

    public UpdateCandidateLinksRequest(String sflink, String folderlink) {
        this.sflink = sflink;
        this.folderlink = folderlink;
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
}
