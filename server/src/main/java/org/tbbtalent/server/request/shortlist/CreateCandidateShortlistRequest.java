package org.tbbtalent.server.request.shortlist;

import org.tbbtalent.server.model.ShortlistStatus;

import javax.validation.constraints.NotNull;

public class CreateCandidateShortlistRequest {

    @NotNull
    Long candidateId;

    @NotNull
    Long savedSearchId;

    @NotNull
    private ShortlistStatus shortlistStatus;

    String comment;

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }

    public Long getSavedSearchId() {
        return savedSearchId;
    }

    public void setSavedSearchId(Long savedSearchId) {
        this.savedSearchId = savedSearchId;
    }

    public ShortlistStatus getShortlistStatus() {
        return shortlistStatus;
    }

    public void setShortlistStatus(ShortlistStatus shortlistStatus) {
        this.shortlistStatus = shortlistStatus;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
