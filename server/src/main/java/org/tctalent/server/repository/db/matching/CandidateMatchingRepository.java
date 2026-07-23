/*
 * Copyright (c) 2026 Talent Catalog.
 */
package org.tctalent.server.repository.db.matching;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.configuration.properties.VectorEmbeddingModelProperties;
import org.tctalent.server.model.db.embedding.EmbeddingModel;
import org.tctalent.server.repository.db.EmbeddingModelRepository;
import org.tctalent.server.request.candidate.matching.CandidateMatchingRequest;

/**
 * Executes candidate matching with JDBC because the PostgreSQL-specific CTEs, full-text operators,
 * pgvector nearest-neighbour ordering, and dynamic trusted identifier are not a good fit for JPA.
 */
@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CandidateMatchingRepository {

    static final String PRIMARY_EMBEDDING_TABLE =
        "job_experience_embedding_minilm_l6_spacy_v3";
    private static final Pattern SAFE_IDENTIFIER = Pattern.compile("[a-z][a-z0-9_]*");

    private final NamedParameterJdbcTemplate jdbc;
    private final VectorEmbeddingModelProperties embeddingProperties;
    private final EmbeddingModelRepository embeddingModelRepository;

    public List<CandidateMatchingResult> match(CandidateMatchingRequest request) {
        String tableName = configuredTableName();
        EmbeddingModel model = configuredModel();
        validate(request, tableName, model.getDimensions());

        MapSqlParameterSource parameters = new MapSqlParameterSource()
            .addValue("queryText", request.getQueryText())
            .addValue("queryEmbedding", toVectorLiteral(request.getQueryEmbedding()))
            .addValue("occupationId", request.getOccupationId())
            .addValue("lexicalWeight", request.getLexicalWeight())
            .addValue("semanticWeight", request.getSemanticWeight())
            .addValue("rrfK", request.getRrfK())
            .addValue("lexicalExperienceLimit", request.getLexicalExperienceLimit())
            .addValue("lexicalCandidateLimit", request.getLexicalCandidateLimit())
            .addValue("semanticPoolSize", request.getSemanticPoolSize())
            .addValue("semanticCandidateLimit", request.getSemanticCandidateLimit())
            .addValue("finalResultLimit", request.getFinalResultLimit());

        return jdbc.query(buildSql(tableName, model.getDimensions()), parameters,
            CandidateMatchingRepository::mapRow);
    }

    String buildSql(String embeddingTable, int dimensions) {
        validateTableName(embeddingTable);
        if (dimensions <= 0) {
            throw new IllegalArgumentException("Embedding dimensions must be positive");
        }

        // A SQL bind parameter cannot represent an identifier. The table name is interpolated
        // only after syntax and configured-model allow-list validation.
        return """
            WITH parameters AS (
                SELECT websearch_to_tsquery('english', :queryText) AS text_query
            ),
            lexical_experience_results AS (
                SELECT cje.id AS experience_id,
                       cje.candidate_id,
                       ts_rank_cd(cje.ts, p.text_query) AS lexical_score
                FROM candidate_job_experience cje
                JOIN candidate_occupation co ON co.id = cje.candidate_occupation_id
                CROSS JOIN parameters p
                WHERE co.occupation_id = :occupationId
                  AND cje.ts @@ p.text_query
                ORDER BY lexical_score DESC, cje.id
                LIMIT :lexicalExperienceLimit
            ),
            lexical_candidate_scores AS (
                SELECT candidate_id, MAX(lexical_score) AS lexical_score
                FROM lexical_experience_results
                GROUP BY candidate_id
            ),
            lexical_candidates AS (
                SELECT candidate_id, lexical_score,
                       ROW_NUMBER() OVER (ORDER BY lexical_score DESC, candidate_id) AS lexical_rank
                FROM lexical_candidate_scores
                ORDER BY lexical_score DESC, candidate_id
                LIMIT :lexicalCandidateLimit
            ),
            semantic_pool AS (
                -- Deliberately no join or occupation predicate here: this exact nearest-neighbour
                -- ORDER BY/LIMIT shape gives PostgreSQL the best opportunity to use the HNSW index.
                SELECT candidate_job_experience_id,
                       embedding <=> CAST(:queryEmbedding AS vector(%d)) AS distance
                FROM %s
                ORDER BY embedding <=> CAST(:queryEmbedding AS vector(%d))
                LIMIT :semanticPoolSize
            ),
            semantic_candidate_scores AS (
                SELECT cje.candidate_id, MAX(1.0 - sp.distance) AS semantic_score
                FROM semantic_pool sp
                JOIN candidate_job_experience cje
                  ON cje.id = sp.candidate_job_experience_id
                JOIN candidate_occupation co ON co.id = cje.candidate_occupation_id
                WHERE co.occupation_id = :occupationId
                GROUP BY cje.candidate_id
            ),
            semantic_candidates AS (
                SELECT candidate_id, semantic_score,
                       ROW_NUMBER() OVER (ORDER BY semantic_score DESC, candidate_id)
                           AS semantic_rank
                FROM semantic_candidate_scores
                ORDER BY semantic_score DESC, candidate_id
                LIMIT :semanticCandidateLimit
            ),
            fused_candidates AS (
                SELECT COALESCE(lc.candidate_id, sc.candidate_id) AS candidate_id,
                       lc.lexical_rank,
                       sc.semantic_rank,
                       lc.lexical_score,
                       sc.semantic_score,
                       -- Raw scores are diagnostics only. Weighted RRF combines ranks, not scores.
                       COALESCE(:lexicalWeight /
                           (:rrfK + lc.lexical_rank), 0.0)
                       + COALESCE(:semanticWeight /
                           (:rrfK + sc.semantic_rank), 0.0) AS rrf_score
                FROM lexical_candidates lc
                FULL OUTER JOIN semantic_candidates sc USING (candidate_id)
            )
            SELECT candidate_id, lexical_rank, semantic_rank,
                   lexical_score, semantic_score, rrf_score
            FROM fused_candidates
            ORDER BY rrf_score DESC, candidate_id
            LIMIT :finalResultLimit
            """.formatted(dimensions, embeddingTable, dimensions);
    }

    private String configuredTableName() {
        String alternate = embeddingProperties.getAlternateEmbeddingTable();
        return alternate == null || alternate.isBlank() ? PRIMARY_EMBEDDING_TABLE : alternate;
    }

    private EmbeddingModel configuredModel() {
        String alternateTable = embeddingProperties.getAlternateEmbeddingTable();
        String modelKey = alternateTable == null || alternateTable.isBlank()
            ? embeddingProperties.getEmbeddingModelKey()
            : embeddingProperties.getAlternateEmbeddingModelKey();
        EmbeddingModel model = embeddingModelRepository.findByModelKey(modelKey);
        if (model == null) {
            throw new IllegalStateException("Configured embedding model not found: " + modelKey);
        }
        return model;
    }

    private void validate(CandidateMatchingRequest request, String tableName, int dimensions) {
        if (request == null) {
            throw new IllegalArgumentException("Matching request is required");
        }
        validateTableName(tableName);
        // This is an explicit allow-list: matching may use only the primary entity table or the
        // alternate table supplied by application configuration, never a request value.
        if (!tableName.equals(PRIMARY_EMBEDDING_TABLE)
            && !tableName.equals(embeddingProperties.getAlternateEmbeddingTable())) {
            throw new IllegalArgumentException("Embedding table is not configured: " + tableName);
        }
        if (request.getQueryText() == null || request.getQueryText().isBlank()) {
            throw new IllegalArgumentException("Query text is required");
        }
        if (request.getOccupationId() == null) {
            throw new IllegalArgumentException("Occupation id is required");
        }
        List<Double> embedding = request.getQueryEmbedding();
        if (embedding == null || embedding.isEmpty()) {
            throw new IllegalArgumentException("Query embedding is required");
        }
        if (embedding.size() != dimensions) {
            throw new IllegalArgumentException(
                "Query embedding has " + embedding.size() + " dimensions; expected " + dimensions);
        }
        if (embedding.stream().anyMatch(value -> value == null || !Double.isFinite(value))) {
            throw new IllegalArgumentException("Query embedding values must be finite");
        }
        if (request.getRrfK() < 0 || request.getLexicalExperienceLimit() <= 0
            || request.getLexicalCandidateLimit() <= 0 || request.getSemanticPoolSize() <= 0
            || request.getSemanticCandidateLimit() <= 0 || request.getFinalResultLimit() <= 0) {
            throw new IllegalArgumentException("RRF constant must be non-negative and limits positive");
        }
        if (!Double.isFinite(request.getLexicalWeight())
            || !Double.isFinite(request.getSemanticWeight())
            || request.getLexicalWeight() < 0 || request.getSemanticWeight() < 0) {
            throw new IllegalArgumentException("Weights must be finite and non-negative");
        }
    }

    void validateTableName(String tableName) {
        if (tableName == null || !SAFE_IDENTIFIER.matcher(tableName).matches()) {
            throw new IllegalArgumentException("Invalid embedding table name: " + tableName);
        }
    }

    private static String toVectorLiteral(List<Double> embedding) {
        return embedding.stream()
            .map(value -> Double.toString(value))
            .collect(Collectors.joining(",", "[", "]"));
    }

    private static CandidateMatchingResult mapRow(ResultSet resultSet, int rowNumber)
        throws SQLException {
        Number lexicalRank = (Number) resultSet.getObject("lexical_rank");
        Number semanticRank = (Number) resultSet.getObject("semantic_rank");
        Number lexicalScore = (Number) resultSet.getObject("lexical_score");
        Number semanticScore = (Number) resultSet.getObject("semantic_score");
        return CandidateMatchingResult.builder()
            .candidateId(resultSet.getLong("candidate_id"))
            .lexicalRank(lexicalRank == null ? null : lexicalRank.intValue())
            .semanticRank(semanticRank == null ? null : semanticRank.intValue())
            .lexicalScore(lexicalScore == null ? null : lexicalScore.doubleValue())
            .semanticScore(semanticScore == null ? null : semanticScore.doubleValue())
            .rrfScore(resultSet.getDouble("rrf_score"))
            .build();
    }
}
