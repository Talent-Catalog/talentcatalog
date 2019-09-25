package org.tbbtalent.server.request.profession;

public class CreateCandidateOccupationRequest {

    private Long occupationId;
    private Long yearsExperience;

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
}
