/*
 * Copyright (c) 2026 Talent Catalog.
 */
package org.tctalent.server.request.candidate.matching;

import java.util.List;
import lombok.Builder;
import lombok.Value;

/**
 * Inputs controlling both retrieval pipelines and weighted reciprocal rank fusion.
 * The embedding table is intentionally absent: it is selected from trusted application
 * configuration.
 */
@Value
@Builder
public class CandidateMatchingRequest {
    String queryText;
    List<Double> queryEmbedding;
    Long occupationId;
    double lexicalWeight;
    double semanticWeight;
    int rrfK;
    int lexicalExperienceLimit;
    int lexicalCandidateLimit;
    int semanticPoolSize;
    int semanticCandidateLimit;
    int finalResultLimit;
}
