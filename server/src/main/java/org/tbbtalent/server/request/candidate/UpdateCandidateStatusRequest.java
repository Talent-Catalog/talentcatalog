package org.tbbtalent.server.request.candidate;

import javax.validation.constraints.NotNull;

import org.tbbtalent.server.model.db.CandidateStatus;

public class UpdateCandidateStatusRequest {

    @NotNull
    private Long candidateId;
    @NotNull
    private CandidateStatus status;

    private String comment;
    private String candidateMessage;

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

    public String getCandidateMessage() {
        return candidateMessage;
    }

    public void setCandidateMessage(String candidateMessage) {
        this.candidateMessage = candidateMessage;
    }
}
