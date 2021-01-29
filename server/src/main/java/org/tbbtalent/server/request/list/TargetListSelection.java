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
