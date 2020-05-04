/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.request.candidate;

import javax.validation.constraints.NotBlank;

import org.tbbtalent.server.model.AbstractCandidateSource;

import lombok.Data;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
public abstract @Data class AbstractUpdateCandidateSourceRequest {
    @NotBlank
    private String name;
    private Boolean fixed;

    protected void populateFromRequest(AbstractCandidateSource candidateSource) {
        candidateSource.setName(name);
        candidateSource.setFixed(fixed);
    }
}
