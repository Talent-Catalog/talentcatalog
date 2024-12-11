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

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.tctalent.server.request.candidate.CandidateIntakeDataUpdate;

@Getter
@Setter
@Entity
@Table(name = "candidate_destination")
@SequenceGenerator(name = "seq_gen", sequenceName = "candidate_destination_id_seq", allocationSize = 1)
@NoArgsConstructor
public class CandidateDestination extends AbstractDomainObject<Long>
        implements Comparable<CandidateDestination> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;

    @Enumerated(EnumType.STRING)
    private YesNoUnsure interest;

    private String notes;

    @Override
    public int compareTo(CandidateDestination o) {
        if (country == null) {
            return o.country == null ? 0 : -1;
        }
        return country.compareTo(o.country);
    }

    public void populateIntakeData(
            @NonNull Candidate candidate, @NonNull Country country,
            CandidateIntakeDataUpdate data) {
        setCandidate(candidate);
        setCountry(country);
        if (data.getDestinationInterest() != null) {
            setInterest(data.getDestinationInterest());
        }
        if (data.getDestinationNotes() != null) {
            setNotes(data.getDestinationNotes());
        }
    }
}
