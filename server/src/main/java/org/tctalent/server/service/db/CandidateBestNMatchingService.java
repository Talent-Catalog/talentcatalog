/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */
package org.tctalent.server.service.db;

import java.util.List;
import org.springframework.data.domain.Page;
import org.tctalent.server.repository.db.read.dto.CandidateReadDto;
import org.tctalent.server.request.candidate.SearchCandidateRequest;

/**
 * Service for finding the best N candidates matching a candidate set of requirements
 * (e.g., a job description).
 * <p>
 *     See also {@link SavedSearchService}.
 * </p>
 * <p>
 *     The two services are similar in that they both return candidates based on input criteria.
 *     The differences are:
 * </p>
 * <ul>
 *     <li>
 *         SavedSearchService returns ALL matching candidates, which can be sorted in various
 *         ways - not necessarily by closeness of match. For example, they could be sorted by
 *         a candidate's nationality or location.
 *         This service, by contrast, returns only the best N candidates
 *         sorted by closeness of match.</li>
 *     <li>
 *         This service uses vector embeddings generated using various AI models for natural
 *         language processing (NLP).
 *     </li>
 * </ul>
 */
public interface CandidateBestNMatchingService {

    /**
     * Returns the best candidate matches corresponding to the given search request.
     * @param request Request that candidates are matched against.
     * @return Sorted results with the best match first.
     * @throws UnsupportedOperationException if the request does not contain a non empty
     * {@link SearchCandidateRequest#getRequirements()}
     */
    List<CandidateReadDto> match(SearchCandidateRequest request);

    /**
     * As for {@link #match(SearchCandidateRequest)} except that it returns a single Page of
     * results.
     */
    Page<CandidateReadDto> matchAsSinglePage(SearchCandidateRequest request);

}
