package org.tbbtalent.server.request.candidate;

import org.tbbtalent.server.model.CandidateStatus;
import org.tbbtalent.server.model.Gender;

import javax.validation.constraints.NotNull;

public class UpdateCandidateStatusRequest {

    @NotNull
    private Long candidateId;
    @NotNull
    private CandidateStatus status;

    private String comment;

    public UpdateCandidateStatusRequest() {
    }

    public UpdateCandidateStatusRequest(@NotNull CandidateStatus status, String comment) {
        this.candidateId = candidateId;
        this.status = status;
        this.comment = comment;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }

    public CandidateStatus getStatus() {
        return status;
    }

    public void setStatus(CandidateStatus status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
