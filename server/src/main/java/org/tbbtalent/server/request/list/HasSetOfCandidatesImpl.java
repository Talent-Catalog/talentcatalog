/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
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
