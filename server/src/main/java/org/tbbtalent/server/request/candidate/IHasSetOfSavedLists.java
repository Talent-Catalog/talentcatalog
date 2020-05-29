/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.request.candidate;

import java.util.Set;

/**
 * Interface for requests containing saved list ids.
 *
 * @author John Cameron
 */
public interface IHasSetOfSavedLists {
    Set<Long> getSavedListIds();
    void setSavedListIds(Set<Long> ids);
}
