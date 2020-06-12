package org.tbbtalent.server.model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "candidate_occupation")
@SequenceGenerator(name = "seq_gen", sequenceName = "candidate_occupation_id_seq", allocationSize = 1)
public class CandidateOccupation extends AbstractAuditableDomainObject<Long> {


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "occupation_id")
    private Occupation occupation;

    private Long yearsExperience;

    private boolean verified;

    private Boolean topCandidate;

    private String migrationOccupation;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidateOccupation", cascade = CascadeType.ALL)
    private List<CandidateJobExperience> candidateJobExperiences;

    public CandidateOccupation() {
    }

    public CandidateOccupation(Candidate candidate, Occupation occupation, Long yearsExperience) {
        this.candidate = candidate;
        this.occupation = occupation;
        this.yearsExperience = yearsExperience;
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

    public Occupation getOccupation() {
        return occupation;
    }

    public void setOccupation(Occupation occupation) {
        this.occupation = occupation;
    }

    public Long getYearsExperience() {
        return yearsExperience;
    }

    public void setYearsExperience(Long yearsExperience) {
        this.yearsExperience = yearsExperience;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public Boolean getTopCandidate() {
        return topCandidate;
    }

    public void setTopCandidate(Boolean topCandidate) {
        this.topCandidate = topCandidate;
    }

    public String getMigrationOccupation() {
        return migrationOccupation;
    }

    public void setMigrationOccupation(String migrationOccupation) {
        this.migrationOccupation = migrationOccupation;
    }

    public List<CandidateJobExperience> getCandidateJobExperiences() { return candidateJobExperiences; }

    public void setCandidateJobExperiences(List<CandidateJobExperience> candidateJobExperiences) { this.candidateJobExperiences = candidateJobExperiences; }
}
