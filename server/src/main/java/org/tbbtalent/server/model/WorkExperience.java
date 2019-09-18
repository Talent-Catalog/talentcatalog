package org.tbbtalent.server.model;

import javax.persistence.*;

@Entity
@Table(name = "work_experience")
public class WorkExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "work_experience_gen")
    @SequenceGenerator(name = "work_experience_gen", sequenceName = "work_experience_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    private String companyName;
    private String country;
    private String role;
    private String startDate;
    private String endDate;
    private String fullTime;
    private String paid;
    private String description;

    public WorkExperience() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

    public String getCompanyName() { return companyName; }

    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCountry() { return country; }

    public void setCountry(String country) { this.country = country; }

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
