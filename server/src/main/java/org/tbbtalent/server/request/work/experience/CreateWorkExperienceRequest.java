package org.tbbtalent.server.request.work.experience;

public class CreateWorkExperienceRequest {

    private String companyName;
    private String countryId;
    private String role;
    private String startDate;
    private String endDate;
    private String fullTime;
    private String paid;
    private String description;

    public String getCompanyName() { return companyName; }

    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCountryId() { return countryId; }

    public void setCountryId(String countryId) { this.countryId = countryId; }

    public String getRole() { return role; }

    public void setRole(String role) { this.role = role; }

    public String getStartDate() { return startDate; }

    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }

    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getFullTime() { return fullTime; }

    public void setFullTime(String fullTime) { this.fullTime = fullTime; }

    public String getPaid() { return paid; }

    public void setPaid(String paid) { this.paid = paid; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }
}
