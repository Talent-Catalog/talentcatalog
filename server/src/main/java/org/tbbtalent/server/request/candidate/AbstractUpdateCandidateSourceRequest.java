/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.request.candidate;

import javax.validation.constraints.NotBlank;

import org.tbbtalent.server.model.AbstractCandidateSource;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
public abstract class AbstractUpdateCandidateSourceRequest {
    @NotBlank
    private String name;
    private Boolean fixed;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getFixed() {
        return fixed;
    }

    public void setFixed(Boolean fixed) {
        this.fixed = fixed;
    }

    protected void populateFromRequest(AbstractCandidateSource candidateSource) {
        candidateSource.setName(name);
        candidateSource.setFixed(fixed);
    }
}
