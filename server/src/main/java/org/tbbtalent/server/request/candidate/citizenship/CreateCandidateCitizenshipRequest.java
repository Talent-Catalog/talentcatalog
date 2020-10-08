/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.request.candidate.citizenship;

import org.springframework.lang.Nullable;
import org.tbbtalent.server.model.db.HasPassport;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CreateCandidateCitizenshipRequest {
    @Nullable
    private Long nationalityId;
    @Nullable
    private HasPassport hasPassport;
    @Nullable
    private String notes;
}
