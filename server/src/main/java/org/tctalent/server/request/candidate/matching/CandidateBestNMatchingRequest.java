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
package org.tctalent.server.request.candidate.matching;

import java.util.List;
import lombok.Builder;
import lombok.Value;
import org.tctalent.server.request.candidate.SearchCandidateRequest;

/**
 * <p>
 * This defines the criteria against which we want to return the best matching candidates.
 * </p>
 * <p>
 *     The criteria are extracted from a description of the candidate experience we want to
 *     match. For example, a job description.
 * </p>
 * <p>
 *     This is similar to {@link org.tctalent.server.request.candidate.SearchCandidateRequest} as
 *     used by the {@link org.tctalent.server.service.db.SavedSearchService}.
 *     See the doc for {@link org.tctalent.server.service.db.CandidateBestNMatchingService}.
 * </p>
 */
@Value
@Builder
public class CandidateBestNMatchingRequest {

    /**
     * <p>
     * Candidate-related text is matched against this query.
     * </p>
     * <p>
     *     Typically, this query will be based on the known skills extracted from the description
     *     of the candidate's experience we want to match.
     * </p>
     * <p>
     *     This is the "lexical" match. See {@link #getLexicalWeight()}.
     * </p>
     * @see SearchCandidateRequest#getSimpleQueryString()
     */
    String simpleQueryString;

    /**
     * <p>
     * Candidate experience vectors are matched against this vector.
     * </p>
     * <p>
     *   This vector is the embedding computed from a description of the candidate experience we
     *   want to match.
     * </p>
     * <p>
     *     This is the "semantic" match.
     * </p>
     */
    List<Double> queryEmbedding;

    /**
     * <p>
     * A fraction between 0 and 1. Zero means that lexical matches are ignored.
     * </p>
     * <p>
     * The semanticWeight is 1 - the lexical weight.
     * </p>
     * <p>
     * In other words, lexicalWeight and semanticWeight should sum to 1.
     * So a weight of 0.5 indicates that lexical and semantic matches have equal weight in computing
     * the final ranking.
     * </p>
     */
    double lexicalWeight;

    /**
     * <p>
     * The maximum number of candidates to return from the lexical match and from the semantic match.
     * </p>
     * <p>
     * Note that this number should be greater than {@link #getResultLimit()} because depending
     * on weightings of lexical and semantic matches, a candidate's combined ranking may
     * be within the resultLimit, based on a contribution from below the resultLimit on one of
     * the matchings.
     * </p>
     */
    int candidateLimit;

    /**
     * The maximum number of candidates in the final ranking. This is "N" in the BestNMatching.
     */
    int resultLimit;

    /**
     * <p>
     *     The maximum number of candidate experiences to consider in the semantic matching.
     * </p>
     * <p>
     *     Candidates may have multiple experiences, so we need to consider a larger number of
     *     experiences to end up with {@link #candidateLimit} candidates.
     * </p>
     */
    int semanticPoolSize;
}
