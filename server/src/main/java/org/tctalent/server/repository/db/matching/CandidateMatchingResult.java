/*
 * Copyright (c) 2026 Talent Catalog.
 */
package org.tctalent.server.repository.db.matching;

import lombok.Builder;
import lombok.Value;

/**
 * Candidate-level hybrid match. Raw scores are diagnostic only and are never directly combined.
 */
@Value
@Builder
public class CandidateMatchingResult {
    long candidateId;
    Integer lexicalRank;
    Integer semanticRank;
    Double lexicalScore;
    Double semanticScore;
    double rrfScore;
}
