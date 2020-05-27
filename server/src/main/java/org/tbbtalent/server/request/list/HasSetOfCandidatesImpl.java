/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.request.list;

import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Request for modifying the contents of a SavedList
 */
@Getter
@Setter
@ToString(callSuper = true)
public class HasSetOfCandidatesImpl implements IHasSetOfCandidates {
    private Set<Long> candidateIds;
}
