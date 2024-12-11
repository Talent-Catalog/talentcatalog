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
import org.springframework.lang.Nullable;
import org.tctalent.server.request.candidate.CandidateIntakeDataUpdate;

import jakarta.persistence.*;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "candidate_citizenship")
@SequenceGenerator(name = "seq_gen", sequenceName = "candidate_citizenship_id_seq", allocationSize = 1)
@NoArgsConstructor
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
    private Country nationality;

    private String notes;

    public void populateIntakeData(
            @NonNull Candidate candidate, @NonNull Country nationality,
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
