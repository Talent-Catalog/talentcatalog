/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

package org.tctalent.server.request.candidate;

import java.util.Arrays;
import java.util.Collection;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Request to update the status of one or more candidates.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class UpdateCandidateStatusRequest {

    /**
     * Candidate(s) whose status should be updated
     */
    @NotNull
    private Collection<Long> candidateIds;

    /**
     * New status and associated other information
     */
    @NotNull
    private UpdateCandidateStatusInfo info;

    public UpdateCandidateStatusRequest(Long... candidateIds) {
        this.candidateIds = Arrays.asList(candidateIds);
    }

    public UpdateCandidateStatusRequest(Collection<Long> candidateIds) {
        this.candidateIds = candidateIds;
    }

    public UpdateCandidateStatusInfo getInfo() {
        if (info == null) {
            info = new UpdateCandidateStatusInfo();
        }
        return info;
    }
}
