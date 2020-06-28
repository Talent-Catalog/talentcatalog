/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.request.search;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SaveSelectionRequest {
    /** 
     * User making the selections 
     */
    @NotNull
    private Long userId;

    /**
     * List to save to - 0 if new list
     */
    private long savedListId;

    /**
     * Name of new list to be created (if any - only used if savedListId = 0
     */
    private String newListName;

    /**
     * If true any existing contents of list are replaced, otherwise contents 
     * are added (merged).
     */
    private boolean replace;
    
}
