/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.request.list;

import java.util.Set;

/**
 * Interface for requests containing candidate ids.
 *
 * @author John Cameron
 */
public interface IHasSetOfCandidates {
    Set<Long> getCandidateIds();
    void setCandidateIds(Set<Long> ids);
}
