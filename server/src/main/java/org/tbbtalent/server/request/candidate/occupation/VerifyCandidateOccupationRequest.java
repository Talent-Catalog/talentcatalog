package org.tbbtalent.server.request.candidate.occupation;

public class VerifyCandidateOccupationRequest {

    private Long id;
    private boolean verified;
    private Long occupationId;
    private String comment;
    private Long yearsExperience;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public Long getOccupationId() { return occupationId; }

    public void setOccupationId(Long occupationId) { this.occupationId = occupationId; }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Long getYearsExperience() { return yearsExperience; }

    public void setYearsExperience(Long yearsExperience) { this.yearsExperience = yearsExperience; }
}
