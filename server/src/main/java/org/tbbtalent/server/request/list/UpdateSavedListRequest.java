/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.request.list;

import org.tbbtalent.server.model.SavedList;
import org.tbbtalent.server.request.candidate.AbstractUpdateCandidateSourceRequest;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
public class UpdateSavedListRequest extends AbstractUpdateCandidateSourceRequest {

    public void populateFromRequest(SavedList savedList) {
        super.populateFromRequest(savedList);
    }
    
}
