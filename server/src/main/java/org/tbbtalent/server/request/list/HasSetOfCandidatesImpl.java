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

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Request for modifying the contents of a SavedList
 */
@Getter
@Setter
@ToString
public class HasSetOfCandidatesImpl implements IHasSetOfCandidates {
    private Set<Long> candidateIds;

    private Long sourceListId;
    
    public void addCandidateId(Long id) {
        if (candidateIds == null) {
            candidateIds = new HashSet<>();
        }
        candidateIds.add(id);
    }
}
