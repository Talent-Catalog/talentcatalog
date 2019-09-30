package org.tbbtalent.server.model;

import javax.persistence.*;

@Entity
@Table(name = "candidate_job_experience")
@SequenceGenerator(name = "seq_gen", sequenceName = "candidate_job_experience_id_seq", allocationSize = 1)
public class CandidateJobExperience extends AbstractDomainObject<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_occupation_id")
    private CandidateOccupation candidateOccupation;

    private String companyName;
    private String role;
    private String startDate;
    private String endDate;
    private Boolean fullTime;
    private Boolean paid;
    private String description;

    public CandidateJobExperience() {
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

    public String getCompanyName() { return companyName; }

    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public Country getCountry() { return country; }

    public void setCountry(Country country) { this.country = country; }

    public CandidateOccupation getCandidateOccupation() {
        return candidateOccupation;
    }

    public void setCandidateOccupation(CandidateOccupation candidateOccupation) {
        this.candidateOccupation = candidateOccupation;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStartDate() { return startDate; }

    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }

    public void setEndDate(String endDate) { this.endDate = endDate; }

    public Boolean getFullTime() { return fullTime; }

    public void setFullTime(Boolean fullTime) { this.fullTime = fullTime; }

    public Boolean getPaid() { return paid; }

    public void setPaid(Boolean paid) { this.paid = paid; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }
}
