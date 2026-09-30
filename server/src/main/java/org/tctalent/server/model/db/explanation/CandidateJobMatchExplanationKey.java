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
 * Primary key for {@link CandidateJobMatchExplanation}.
 * See doc for that class.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Embeddable
public class CandidateJobMatchExplanationKey implements Serializable {

    @Column(name = "candidate_id")
    private Long candidateId;

    @Column(name = "job_id")
    private Long jobId;

    public CandidateJobMatchExplanationKey() {
    }

    public CandidateJobMatchExplanationKey(Long candidateId, Long jobId) {
        this.candidateId = candidateId;
        this.jobId = jobId;
    }
}
