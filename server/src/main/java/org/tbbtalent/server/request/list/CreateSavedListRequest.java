/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.request.list;

import java.util.Set;

import org.tbbtalent.server.model.SavedList;
import org.tbbtalent.server.request.candidate.AbstractUpdateCandidateSourceRequest;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Request for creating and optionally initializing the contents of 
 * a SavedList
 *
 */
@Getter
@Setter
@ToString(callSuper = true)
public class CreateSavedListRequest extends AbstractUpdateCandidateSourceRequest 
        implements IHasSetOfCandidates {
    
    private Set<Long> candidateIds;

    public void populateFromRequest(SavedList savedList) {
        super.populateFromRequest(savedList);
    }
}
