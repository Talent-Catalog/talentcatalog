/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
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
        if (data.getDependantHealthConcerns() != null) {
            setHealthConcern(data.getDependantHealthConcerns());
        }
        if (data.getDependantNotes() != null) {
            setNotes(data.getDependantNotes());
        }
    }

}
