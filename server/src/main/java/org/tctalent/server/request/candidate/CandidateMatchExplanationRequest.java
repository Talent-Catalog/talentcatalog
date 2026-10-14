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

package org.tctalent.server.request.candidate;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Request for the LLM explanation of a candidate's match against the description of a
 * {@link org.tctalent.server.model.db.Matching}. The candidate is identified by the request path.
 * <p>
 * The description is always obtained by the server from the Matching - it is not supplied by
 * the request.
 */
@Getter
@Setter
public class CandidateMatchExplanationRequest {

    /**
     * ID of the Matching whose description the candidate is compared against.
     */
    @NotNull
    private Long matchingId;

    /**
     * True to always generate a new explanation, replacing any cached one - eg because the
     * candidate's job experience has changed, which does not make a cached explanation out of
     * date. False (the default) to return the cached explanation if it is current, only
     * generating a new one if needed.
     */
    private boolean regenerate;
}
