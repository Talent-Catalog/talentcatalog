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

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

/** Request to generate an on-demand LLM explanation of a candidate/opportunity match. */
@Getter
@Setter
public class CandidateMatchExplanationRequest {

    /**
     * ID of the Talent Catalog job associated with this explanation request.
     * <p>
     * This is optional contextual information. A null value means that the explanation
     * request is not associated with a specific Talent Catalog job.
     * <p>
     * The job ID is not used to obtain the opportunity description. The explanation is
     * generated using the {@link #opportunityDescription} supplied in this request.
     */
    @Nullable
    private Long jobId;

    /**
     * Description of the job or other opportunity against which the candidate's
     * experience should be compared.
     * <p>
     * This is the text supplied to the explanation service and is required regardless
     * of whether {@link #jobId} is supplied.
     */
    @NotBlank
    private String opportunityDescription;
}
