/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License 
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tbbtalent.server.model.db;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

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

    //Todo Figure out why Cascade = MERGE doesn't work. Why do we need ALL. What does this mean?
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
