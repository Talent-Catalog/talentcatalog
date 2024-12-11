/*
 * Copyright (c) 2024 Talent Catalog.
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

package org.tctalent.server.request.candidate.source;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.tctalent.server.request.PagedSearchRequest;

/**
 * Used for searching candidate sources (eg SavedLists and SavedSearches) by
 * their attributes.
 * <p/>
 * Flags are Boolean - meaning that they have three values: null (ie undefined, therefore ignored),
 * true or false
 * <p/>
 * The logic for searching depends on whether watched is specified.
 *
 * If watched is specified and is true, all watched sources (by the user) are returned.
 *
 * Otherwise, sources are returned which match the following logic:
 *
 * keyword matches
 * AND
 * fixed (if defined)
 * AND
 * (global OR shared OR owned) - for those values that are defined
 */
@Getter
@Setter
@ToString
public class SearchCandidateSourceRequestPaged extends PagedSearchRequest {
    /**
     * Used to match sources whose names are like this keyword
     */
    private String keyword;

    /**
     * If true the details of the source definition cannot be changed (eg name, or search parameters
     * for searches)
     */
    private Boolean fixed;

    /**
     * Everyone sees global sources. They cannot be removed (ie deleted from view).
     */
    private Boolean global;

    /**
     * Owned by me
     */
    private Boolean owned;

    /**
     * Shared with me
     */
    private Boolean shared;

    /**
     * Watched by me
     */
    private Boolean watched;

}

