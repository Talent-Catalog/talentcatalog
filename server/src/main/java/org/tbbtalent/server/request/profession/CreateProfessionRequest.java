package org.tbbtalent.server.request.profession;

public class CreateProfessionRequest {

    private Long industryId;
    private Long yearsExperience;

    public Long getIndustryId() {
        return industryId;
    }

    public void setIndustryId(Long industryId) {
        this.industryId = industryId;
    }

    public Long getYearsExperience() {
        return yearsExperience;
    }

    public void setYearsExperience(Long yearsExperience) {
        this.yearsExperience = yearsExperience;
    }
}
