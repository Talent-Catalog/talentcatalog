package org.tbbtalent.server.request.work.experience;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

public class UpdateJobExperienceRequest {

    @NotBlank
    private String companyName;
    @NotNull
    private Long countryId;
    @NotBlank
    private String role;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean fullTime;
    private Boolean paid;
    @NotBlank
    private String description;

    public String getCompanyName() { return companyName; }

    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public Long getCountryId() {
        return countryId;
    }

    public void setCountry(Long countryId) {
        this.countryId = countryId;
    }

    public String getRole() { return role; }

    public void setRole(String role) { this.role = role; }

    public LocalDate getStartDate() { return startDate; }

    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }

    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Boolean getFullTime() { return fullTime; }

    public void setFullTime(Boolean fullTime) { this.fullTime = fullTime; }

    public Boolean getPaid() { return paid; }

    public void setPaid(Boolean paid) { this.paid = paid; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }
}
