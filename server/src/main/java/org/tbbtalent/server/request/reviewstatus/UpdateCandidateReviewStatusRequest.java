package org.tbbtalent.server.request.reviewstatus;

import javax.validation.constraints.NotNull;

import org.tbbtalent.server.model.db.ReviewStatus;

public class UpdateCandidateReviewStatusRequest {

    @NotNull
    private Long candidateReviewStatusId;
    @NotNull
    private ReviewStatus reviewStatus;

    private String comment;

    public Long getCandidateReviewStatusId() {
        return candidateReviewStatusId;
    }

    public void setCandidateReviewStatusId(Long candidateReviewStatusId) {
        this.candidateReviewStatusId = candidateReviewStatusId;
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
