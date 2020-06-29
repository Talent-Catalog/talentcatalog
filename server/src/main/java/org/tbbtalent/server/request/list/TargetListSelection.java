/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.request.list;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Specifies 
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString
public class TargetListSelection {
    //List id - 0 if new list requested
    Long savedListId;

    //Name of new list to be created (if any - only used if savedListId = 0)
    String newListName;

    //If true any existing contents of target list should be replaced, otherwise
    //contents are added (merged).
    boolean replace;
}
