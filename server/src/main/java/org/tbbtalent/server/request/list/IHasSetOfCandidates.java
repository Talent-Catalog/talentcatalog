/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.request.list;

import java.util.Set;

import org.springframework.lang.Nullable;

/**
 * Interface for requests containing candidate ids.
 *
 * @author John Cameron
 */
public interface IHasSetOfCandidates {
    @Nullable
    Set<Long> getCandidateIds();
    
    void setCandidateIds(Set<Long> ids);
}
