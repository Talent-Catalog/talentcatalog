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

package org.tbbtalent.server.request.candidate.source;

import org.tbbtalent.server.request.PagedSearchRequest;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Used for searching candidate sources (eg SavedLists and SavedSearches) by
 * their attributes.
 */
@Getter
@Setter
@ToString
public class SearchCandidateSourceRequestPaged extends PagedSearchRequest {
    /**
     * Used to match sources whose names are like this keyword
     */
    private String keyword;
    
    private Boolean fixed;
    private Boolean global;
    private Boolean owned;
    private Boolean shared;
    private Boolean watched;
}

