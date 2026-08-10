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
import org.tctalent.server.model.db.embedding.EmbeddingModel;
import org.tctalent.server.request.candidate.matching.CandidateBestNMatchingRequest;
import org.tctalent.server.service.db.EmbeddingModelService;

/**
 * Executes candidate matching with JDBC because the PostgreSQL-specific CTEs, full-text operators,
 * pgvector nearest-neighbour ordering, and dynamic trusted identifier are not a good fit for JPA.
 */
@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CandidateBestNMatchingRepository {

    //This is the most comon standard for K in Reciprocal Rank Fusion.
    //We will standardize on it.
    private static final int RRF_K = 60;

    private static final Pattern SAFE_IDENTIFIER = Pattern.compile("[a-z][a-z0-9_]*");

    private final NamedParameterJdbcTemplate jdbc;

    private final EmbeddingModelService embeddingModelService;

    /**
     * This returns the best candidate matches corresponding to the given request.
     * The request contains the natural language requirements (and its vector embedding)
     * as well as the skills that have been extracted from those requirements.
     * <p>
     * The skills search uses text search for the extracted skills.
     * The SQL for that search is passed in {#lexicalCandidateScoresSql}.
     * <p>
     * The matching is also limited by SQL providing the standard search constraints of
     * a TC search screen - such as candidate gender, status, location, etc.
     * <p>
     *
     * @param request Request that contains the natural language requirements and other information
     *                controlling the matching process. See {@link CandidateBestNMatchingRequest}
     *                for details.
     * @param lexicalCandidateScoresSql This is the SQL that does the text matching
     * @param constraintJoinsAndWhereSql The embedded vector matching is also constrained by this
     *                                   SQL generated from the standard search constraints of a
     *                                   TC search screen.
     * @return Best combined matches
     */
    public List<CandidateBestNMatchingResult> match(CandidateBestNMatchingRequest request,
    String lexicalCandidateScoresSql, String constraintJoinsAndWhereSql) {

        EmbeddingModel model;
        String modelKey = request.getModelKey();
        if (modelKey == null || modelKey.isBlank()) {
            model = embeddingModelService.getDefaultModel();
        } else {
            model = embeddingModelService.findModelByKey(modelKey);
        }
        if (model == null) {
            throw new IllegalArgumentException("No embedding model found for modelKey: " + modelKey);
        }
        String tableName = embeddingModelService.getTableNameForModel(model);

        validate(request, tableName, model.getDimensions());

        MapSqlParameterSource parameters = new MapSqlParameterSource()
            .addValue("queryText", request.getSimpleQueryString())
            .addValue("queryEmbedding", toVectorLiteral(request.getQueryEmbedding()))
            .addValue("lexicalWeight", request.getLexicalWeight())
            .addValue("semanticWeight", 1-request.getLexicalWeight())
            .addValue("rrfK", RRF_K)
            .addValue("candidateLimit", request.getCandidateLimit())
            .addValue("semanticPoolSize", request.getSemanticPoolSize())
            .addValue("resultLimit", request.getResultLimit());

        final String sql = buildSql(tableName, model.getDimensions(),
            lexicalCandidateScoresSql, constraintJoinsAndWhereSql);
        return jdbc.query(sql, parameters, CandidateBestNMatchingRepository::mapRow);
    }

    String buildSql(String embeddingTable, int dimensions,
        String lexicalCandidateScoresSql, String constraintJoinsAndWhereSql) {
        validateTableName(embeddingTable);
        if (dimensions <= 0) {
            throw new IllegalArgumentException("Embedding dimensions must be positive");
        }

        // A SQL bind parameter cannot represent an identifier. The table name is interpolated
        // only after syntax and configured-model allow-list validation.
        return """
            WITH parameters AS (
                SELECT to_tsquery('english', :queryText) AS text_query
            ),
            lexical_candidate_scores AS (
            """
                +
                lexicalCandidateScoresSql
                +
                " LIMIT :candidateLimit"
                +
            """
            ),
            lexical_candidates AS (
                SELECT id AS candidate_id, score AS lexical_score,
                       ROW_NUMBER() OVER (ORDER BY score DESC, id) AS lexical_rank
                FROM lexical_candidate_scores
                ORDER BY score DESC, id
                LIMIT :candidateLimit
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
                JOIN candidate on cje.candidate_id = candidate.id
            """.formatted(dimensions, embeddingTable, dimensions)
                +
                constraintJoinsAndWhereSql
                +
            """
                GROUP BY cje.candidate_id
            ),
            semantic_candidates AS (
                SELECT candidate_id, semantic_score,
                       ROW_NUMBER() OVER (ORDER BY semantic_score DESC, candidate_id)
                           AS semantic_rank
                FROM semantic_candidate_scores
                ORDER BY semantic_score DESC, candidate_id
                LIMIT :candidateLimit
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
            LIMIT :resultLimit
            """;
    }

    private void validate(CandidateBestNMatchingRequest request, String tableName, int dimensions) {
        if (request == null) {
            throw new IllegalArgumentException("Matching request is required");
        }
        validateTableName(tableName);
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
        if (request.getCandidateLimit() <= 0 || request.getSemanticPoolSize() <= 0
            || request.getResultLimit() <= 0) {
            throw new IllegalArgumentException("Limits must be positive");
        }
        if (request.getSemanticPoolSize() < request.getCandidateLimit()) {
            throw new IllegalArgumentException(
                "Semantic pool size must be greater than result limit"
            );
        }
        if (request.getCandidateLimit() < request.getResultLimit()) {
            throw new IllegalArgumentException(
                "Candidate limit should be greater than result limit"
            );
        }
        if (!Double.isFinite(request.getLexicalWeight()) || request.getLexicalWeight() < 0
            || request.getLexicalWeight() > 1) {
            throw new IllegalArgumentException(
                "Lexical weight must be finite between 0 and 1 inclusive");
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

    private static CandidateBestNMatchingResult mapRow(ResultSet resultSet, int rowNumber)
        throws SQLException {
        Number lexicalRank = (Number) resultSet.getObject("lexical_rank");
        Number semanticRank = (Number) resultSet.getObject("semantic_rank");
        Number lexicalScore = (Number) resultSet.getObject("lexical_score");
        Number semanticScore = (Number) resultSet.getObject("semantic_score");
        return CandidateBestNMatchingResult.builder()
            .candidateId(resultSet.getLong("candidate_id"))
            .lexicalRank(lexicalRank == null ? null : lexicalRank.intValue())
            .semanticRank(semanticRank == null ? null : semanticRank.intValue())
            .lexicalScore(lexicalScore == null ? null : lexicalScore.doubleValue())
            .semanticScore(semanticScore == null ? null : semanticScore.doubleValue())
            .rrfScore(resultSet.getDouble("rrf_score"))
            .build();
    }
}
