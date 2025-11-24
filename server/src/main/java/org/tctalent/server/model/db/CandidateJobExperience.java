/*
 * Copyright (c) 2024 Talent Catalog.
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

package org.tctalent.server.model.db;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "candidate_job_experience")
@SequenceGenerator(name = "seq_gen", sequenceName = "candidate_job_experience_id_seq", allocationSize = 1)
@NoArgsConstructor
public class CandidateJobExperience extends AbstractDomainObject<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    /**
     * Synchronizes the candidate field with the candidate obtained from the associated
     * CandidateOccupation.
     * <p>
     * This method is automatically invoked before the entity is persisted or updated so that the
     * candidate_id column in the candidate_job_experience table is correctly populated.
     * </p>
     */
    @PrePersist
    @PreUpdate
    private void syncCandidate() {
        if (this.candidateOccupation != null) {
            this.candidate = this.candidateOccupation.getCandidate();
        }
    }

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

    public CandidateJobExperience(Candidate candidate, Country country, CandidateOccupation candidateOccupation,
                                  String companyName, String role, LocalDate startDate, LocalDate endDate,
                                  String description) {

        this.candidate = candidate;
        this.country = country;
        this.candidateOccupation = candidateOccupation;
        this.companyName = companyName;
        this.role = role;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
    }

}
