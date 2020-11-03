/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.request.list;

import org.springframework.lang.Nullable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Specifies the list used for saving a selection of candidates 
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString
public class TargetListSelection {
    /**
     * List id - 0 if new list requested
     */
    Long savedListId;

    /**
     * Name of new list to be created (if any - only used if savedListId = 0)
     */
    @Nullable
    String newListName;

    /**
     * If true any existing contents of target list should be replaced, otherwise
     * contents are added (merged). 
     */
    boolean replace;

    /**
     * Link to associated Salesforce job opportunity, if any, to be associated
     * with list 
     */
    @Nullable
    String sfJoblink;
}
