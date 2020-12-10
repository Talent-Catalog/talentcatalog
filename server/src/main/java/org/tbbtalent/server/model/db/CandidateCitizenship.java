/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.model.db;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.tbbtalent.server.request.candidate.CandidateIntakeDataUpdate;

import javax.persistence.*;
import java.time.LocalDate;

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

    @Nullable
    private LocalDate passportExp;

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
        if (data.getCitizenPassportExp() != null) {
            setPassportExp(data.getCitizenPassportExp());
        }
    }
    
}
