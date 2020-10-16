/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.request.candidate.visa;

import org.springframework.lang.Nullable;
import org.tbbtalent.server.model.db.VisaEligibility;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CreateCandidateVisaRequest {
    @Nullable
    private Long countryId;
    @Nullable
    private VisaEligibility eligibility;
    @Nullable
    private String assessmentNotes;
}
