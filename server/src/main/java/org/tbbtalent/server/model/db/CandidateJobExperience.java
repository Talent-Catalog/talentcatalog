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

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

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
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean fullTime;
    private Boolean paid;
    private String description;

    public CandidateJobExperience() {
    }

    public CandidateJobExperience(Candidate candidate, Country country, CandidateOccupation candidateOccupation, String companyName, String role, LocalDate startDate, LocalDate endDate, String description) {

        this.candidate = candidate;
        this.country = country;
        this.candidateOccupation = candidateOccupation;
        this.companyName = companyName;
        this.role = role;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
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
