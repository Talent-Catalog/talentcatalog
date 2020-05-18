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
 * Request for create or modifying SavedList's
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString(callSuper = true)
public class UpdateSavedListRequest extends AbstractUpdateCandidateSourceRequest {
    
    private Set<Long> candidateIds;

    public void populateFromRequest(SavedList savedList) {
        super.populateFromRequest(savedList);
    }
}
