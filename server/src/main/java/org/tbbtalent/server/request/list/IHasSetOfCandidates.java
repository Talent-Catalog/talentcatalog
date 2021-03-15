/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License 
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
