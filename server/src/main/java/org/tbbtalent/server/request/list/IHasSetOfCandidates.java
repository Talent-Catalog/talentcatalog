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

    /**
     * Candidates 
     * @return Candidate ids - may be null, indicating no candidates
     */
    @Nullable
    Set<Long> getCandidateIds();

    /**
     * If present, indicates the list that the candidates came from
     * (allowing their context to be copied across).
     * @return Id of list, null if there was no source list, or it is ignored.
     */
    @Nullable
    Long getSourceListId();

    /**
     * Candidates 
     * @param ids Candidate ids - null is allowed representing no candidates
     */
    void setCandidateIds(@Nullable Set<Long> ids);

    /**
     * If present, indicates the list that the candidates came from
     * (allowing their context to be copied across).
     * @param sourceListId Id of list, null if there was no source list, or 
     *                     it is ignored.
     */
    void setSourceListId(@Nullable Long sourceListId);
}
