/*
 * Copyright (c) 2025 Talent Catalog.
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

package org.tctalent.server.service.db;

import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.lang.Nullable;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.request.candidate.SearchCandidateRequest;

/**
 * Support for searching candidates.
 *
 * @author John Cameron
 */
public interface CandidateSearchService {

    /**
     * Do a paged search for candidates according to the given request but excluding the given
     * candidates. Sorting and paging are supported as specified in the request.
     * @param request Specifies the details of the search
     * @param excludedCandidates If specified, indicates candidates to be excluded from the search.
     * @return Sorted page of candidates
     */
    Page<Candidate> searchCandidates(
        SearchCandidateRequest request, @Nullable Set<Candidate> excludedCandidates);
}
