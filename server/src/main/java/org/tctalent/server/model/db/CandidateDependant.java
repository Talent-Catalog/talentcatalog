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
import org.springframework.lang.NonNull;
import org.tctalent.server.request.candidate.CandidateIntakeDataUpdate;

import jakarta.persistence.*;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "candidate_dependant")
@SequenceGenerator(name = "seq_gen", sequenceName = "candidate_dependant_id_seq", allocationSize = 1)
@NoArgsConstructor
public class CandidateDependant extends AbstractDomainObject<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @Enumerated(EnumType.STRING)
    private DependantRelations relation;

    private String relationOther;

    private LocalDate dob;

    private Gender gender;

    private String name;

    @Enumerated(EnumType.STRING)
    private Registration registered;

    private String registeredNumber;

    private String registeredNotes;

    @Enumerated(EnumType.STRING)
    private YesNo healthConcern;

    private String healthNotes;

    public void populateIntakeData(
            @NonNull Candidate candidate,
            CandidateIntakeDataUpdate data) {
        setCandidate(candidate);
        if (data.getDependantRelation() != null) {
            setRelation(data.getDependantRelation());
        }
        if (data.getDependantRelationOther() != null) {
            setRelationOther(data.getDependantRelationOther());
        }
        if (data.getDependantDob() != null) {
            setDob(data.getDependantDob());
        }
        if (data.getDependantGender() != null) {
            setGender(data.getDependantGender());
        }
        if (data.getDependantName() != null) {
            setName(data.getDependantName());
        }
        if (data.getDependantRegistered() != null) {
            setRegistered(data.getDependantRegistered());
        }
        if (data.getDependantRegisteredNumber() != null) {
            setRegisteredNumber(data.getDependantRegisteredNumber());
        }
        if (data.getDependantRegisteredNotes() != null) {
            setRegisteredNotes(data.getDependantRegisteredNotes());
        }
        if (data.getDependantHealthConcerns() != null) {
            setHealthConcern(data.getDependantHealthConcerns());
        }
        if (data.getDependantHealthNotes() != null) {
            setHealthNotes(data.getDependantHealthNotes());
        }
    }

}
