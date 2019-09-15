package org.tbbtalent.server.model;

import javax.persistence.*;

@Entity
@Table(name = "profession")
public class Profession {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "profession_gen")
    @SequenceGenerator(name = "profession_gen", sequenceName = "profession_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "industry_id")
    private Industry industry;

    private Long yearsExperience;

    public Profession() {
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

    public Industry getIndustry() {
        return industry;
    }

    public void setIndustry(Industry industry) {
        this.industry = industry;
    }

    public Long getYearsExperience() {
        return yearsExperience;
    }

    public void setYearsExperience(Long yearsExperience) {
        this.yearsExperience = yearsExperience;
    }
}
