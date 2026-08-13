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
package org.tctalent.server.repository.db.matching;

import lombok.Builder;
import lombok.Value;

/**
 * <p>
 * Combined candidate match based on both lexical and semantic matches.
 * </p>
 * <p>
 *  Raw scores are diagnostic only and are never directly combined.
 * </p>
 */
@Value
@Builder
public class CandidateBestNMatchingResult {

    /**
     * Matched candidate ID.
     */
    long candidateId;

    /**
     * <p>
     * RRF ranking: Higher is better.
     * </p>
     * <p>
     *     This is what determines the combined ranking. The number 1 match has the highest score.
     * </p>
     * <p>
     *     This is computed from {@link #lexicalRank} and {@link #semanticRank} using the
     *     Reciprocal Rank Fusion (RRF) formula.
     * </p>
     * <p>
     *     See <a href="https://www.paradedb.com/learn/search-concepts/reciprocal-rank-fusion">this
     *     definition from ParadeDB</a>
     * </p>
     */
    double rrfScore;

    /**
     * Lexical ranking, based on {@link #lexicalScore}. 1 is best.
     */
    Integer lexicalRank;

    /**
     * Semantic ranking, based on {@link #semanticScore} 1 is best.
     */
    Integer semanticRank;

    /**
     * <p>
     * Raw lexical score, for informational purposes only.
     * </p>
     * <p>
     *     Lexical and semantic scores are computed very differently and are not comparable, so
     *     instead ranking is used rather than scores to come up with a combined ranking.
     * </p>
     */
    Double lexicalScore;

    /**
     * <p>
     * Raw semantic score, for informational purposes only.
     * </p>
     * <p>
     *     Lexical and semantic scores are computed very differently and are not comparable, so
     *     instead ranking is used rather than scores to come up with a combined ranking.
     * </p>
     */
    Double semanticScore;
}
