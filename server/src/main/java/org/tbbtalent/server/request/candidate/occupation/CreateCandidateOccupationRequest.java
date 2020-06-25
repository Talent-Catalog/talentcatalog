package org.tbbtalent.server.request.candidate.occupation;

import javax.validation.constraints.NotNull;

public class CreateCandidateOccupationRequest {

    private Long candidateId;
    @NotNull
    private Long occupationId;
    @NotNull
    private Long yearsExperience;
    @NotNull
    private boolean verified;
    private String comment;

    public Long getCandidateId() { return candidateId; }

    public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }

    public Long getOccupationId() {
        return occupationId;
    }

    public void setOccupationId(Long occupationId) {
        this.occupationId = occupationId;
    }

    public Long getYearsExperience() {
        return yearsExperience;
    }

    public void setYearsExperience(Long yearsExperience) {
        this.yearsExperience = yearsExperience;
    }

    public boolean isVerified() { return verified; }

    public void setVerified(boolean verified) { this.verified = verified; }

    public String getComment() { return comment; }

    public void setComment(String comment) { this.comment = comment; }
}
