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
