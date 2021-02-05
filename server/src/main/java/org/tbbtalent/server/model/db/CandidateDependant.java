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

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.tbbtalent.server.request.candidate.CandidateIntakeDataUpdate;

import javax.persistence.*;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "candidate_dependant")
@SequenceGenerator(name = "seq_gen", sequenceName = "candidate_dependant_id_seq", allocationSize = 1)
public class CandidateDependant extends AbstractDomainObject<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @Enumerated(EnumType.STRING)
    private DependantRelations relation;

    private LocalDate dob;

    private String name;

    @Enumerated(EnumType.STRING)
    private YesNo healthConcern;

    private String notes;

    public CandidateDependant() {
    }

    public void populateIntakeData(
            @NonNull Candidate candidate,
            CandidateIntakeDataUpdate data) {
        setCandidate(candidate);
        if (data.getDependantRelation() != null) {
            setRelation(data.getDependantRelation());
        }
        if (data.getDependantDob() != null) {
            setDob(data.getDependantDob());
        }
        if (data.getDependantName() != null) {
            setName(data.getDependantName());
        }
        if (data.getDependantHealthConcerns() != null) {
            setHealthConcern(data.getDependantHealthConcerns());
        }
        if (data.getDependantNotes() != null) {
            setNotes(data.getDependantNotes());
        }
    }

}
