package org.tbbtalent.server.request.candidate;

import javax.validation.constraints.NotNull;

public class UpdateCandidateLinksRequest {

    @NotNull
    private Long candidateId;

    private String sflink;
    private String folderlink;
    private String videolink;

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
}
