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

package org.tctalent.server.request.list;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.tctalent.server.request.candidate.source.SearchCandidateSourceRequestPaged;

@Getter
@Setter
@ToString
public class SearchSavedListRequest extends SearchCandidateSourceRequestPaged {
    /**
     * If true will search saved lists where tbb short name (the suffix of the public external url)
     * is not null.
     */
    private Boolean shortName;

    /**
     * If true search will include lists where registeredJob is true - ie it will return lists
     * that are the registered list for a Salesforce job opportunity.
     */
    private Boolean registeredJob;

    /**
     * Only affects search for SavedLists where sfJoblink is not null - ie SavedList's which have
     * an associated job opportunity. Otherwise its value is ignored.
     * Lists which have associated job opportunities will be selected based on whether or not the
     * opportunity is closed.
     */
    private Boolean sfOppClosed;
}

