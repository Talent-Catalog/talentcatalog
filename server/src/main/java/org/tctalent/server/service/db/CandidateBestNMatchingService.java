/*
 * Copyright (c) 2026 Talent Catalog.
 */
package org.tctalent.server.service.db;

import java.util.List;
import org.springframework.data.domain.Page;
import org.tctalent.server.repository.db.matching.CandidateBestNMatchingResult;
import org.tctalent.server.repository.db.read.dto.CandidateReadDto;
import org.tctalent.server.request.candidate.SearchCandidateRequest;
import org.tctalent.server.request.candidate.matching.CandidateBestNMatchingRequest;

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
     * Returns the best candidate matches corresponding to the given request.
     * @param request Request that candidates are matched against.
     * @return Sorted results with the best match first.
     */
    List<CandidateBestNMatchingResult> match(CandidateBestNMatchingRequest request);

    Page<CandidateReadDto> match(SearchCandidateRequest request);

}
