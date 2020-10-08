/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.model.db;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.springframework.lang.NonNull;
import org.tbbtalent.server.request.candidate.CandidateIntakeDataUpdate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "candidate_citizenship")
@SequenceGenerator(name = "seq_gen", sequenceName = "candidate_citizenship_id_seq", allocationSize = 1)
public class CandidateCitizenship extends AbstractDomainObject<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @Enumerated(EnumType.STRING)
    private HasPassport hasPassport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nationality_id")
    private Nationality nationality;

    private String notes;

    public CandidateCitizenship() {
    }

    public void populateIntakeData(
            @NonNull Candidate candidate, @NonNull Nationality nationality, 
            CandidateIntakeDataUpdate data) {
        setCandidate(candidate);
        setNationality(nationality);
        if (data.getCitizenNotes() != null) {
            setNotes(data.getCitizenNotes());
        }
        if (data.getCitizenHasPassport() != null) {
            setHasPassport(data.getCitizenHasPassport());
        }
    }
    
}
