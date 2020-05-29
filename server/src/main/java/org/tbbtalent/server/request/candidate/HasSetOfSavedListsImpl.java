/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.request.candidate;

import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Request for modifying the saved lists associated with a candidate
 */
@Getter
@Setter
@ToString
public class HasSetOfSavedListsImpl implements IHasSetOfSavedLists {
    private Set<Long> savedListIds;
}
