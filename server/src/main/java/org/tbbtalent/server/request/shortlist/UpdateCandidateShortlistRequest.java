package org.tbbtalent.server.request.shortlist;

import org.tbbtalent.server.model.ShortlistStatus;

import javax.validation.constraints.NotNull;

public class UpdateCandidateShortlistRequest {

    @NotNull
    private Long candidateShortlistId;
    @NotNull
    private ShortlistStatus shortlistStatus;

    private String comment;

    public Long getCandidateShortlistId() {
        return candidateShortlistId;
    }

    public void setCandidateShortlistId(Long candidateShortlistId) {
        this.candidateShortlistId = candidateShortlistId;
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
