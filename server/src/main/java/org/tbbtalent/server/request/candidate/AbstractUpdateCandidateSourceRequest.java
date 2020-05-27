/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.request.candidate;

import javax.validation.constraints.NotBlank;

import org.tbbtalent.server.model.AbstractCandidateSource;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Base class for any Update/Create requests on candidate sources 
 * (sublasses of {@link AbstractCandidateSource}).
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString
public abstract class AbstractUpdateCandidateSourceRequest {
    @NotBlank
    private String name;
    private Boolean fixed;

    public void populateFromRequest(AbstractCandidateSource candidateSource) {
        candidateSource.setName(name);
        candidateSource.setFixed(fixed);
    }
}
