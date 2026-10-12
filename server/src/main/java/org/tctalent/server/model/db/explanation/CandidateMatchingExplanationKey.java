/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.model.db.explanation;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Primary key for {@link CandidateMatchingExplanation}.
 * See doc for that class.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Embeddable
public class CandidateMatchingExplanationKey implements Serializable {

    @Column(name = "candidate_id")
    private Long candidateId;

    @Column(name = "matching_id")
    private Long matchingId;

    public CandidateMatchingExplanationKey() {
    }

    public CandidateMatchingExplanationKey(Long candidateId, Long matchingId) {
        this.candidateId = candidateId;
        this.matchingId = matchingId;
    }
}
