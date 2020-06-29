/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.request.search;

import javax.validation.constraints.NotNull;

import org.tbbtalent.server.request.list.TargetListSelection;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class SaveSelectionRequest extends TargetListSelection {
    /** 
     * User making the selections 
     */
    @NotNull
    private Long userId;
    
}
