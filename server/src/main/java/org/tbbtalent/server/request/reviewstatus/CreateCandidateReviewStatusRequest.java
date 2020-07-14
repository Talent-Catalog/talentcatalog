package org.tbbtalent.server.request.reviewstatus;

import javax.validation.constraints.NotNull;

import org.tbbtalent.server.model.ReviewStatus;

public class CreateCandidateReviewStatusRequest {

    @NotNull
    Long candidateId;

    @NotNull
    Long savedSearchId;

    @NotNull
    private ReviewStatus reviewStatus;

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

    public ReviewStatus getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(ReviewStatus reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
