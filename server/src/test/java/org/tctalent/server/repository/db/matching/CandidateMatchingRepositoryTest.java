package org.tctalent.server.repository.db.matching;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.tctalent.server.configuration.properties.VectorEmbeddingModelProperties;
import org.tctalent.server.model.db.embedding.EmbeddingModel;
import org.tctalent.server.repository.db.EmbeddingModelRepository;
import org.tctalent.server.request.candidate.matching.CandidateMatchingRequest;

class CandidateMatchingRepositoryTest {

    private NamedParameterJdbcTemplate jdbc;
    private VectorEmbeddingModelProperties properties;
    private EmbeddingModelRepository modelRepository;
    private CandidateMatchingRepository repository;

    @BeforeEach
    void setUp() {
        jdbc = mock(NamedParameterJdbcTemplate.class);
        properties = new VectorEmbeddingModelProperties();
        properties.setEmbeddingModelKey("MINILM_L6_SPACY_V3");
        properties.setAlternateEmbeddingModelKey("MINILM_L6_SPACY_V3");
        properties.setAlternateEmbeddingTable(
            CandidateMatchingRepository.PRIMARY_EMBEDDING_TABLE);
        modelRepository = mock(EmbeddingModelRepository.class);
        EmbeddingModel model = new EmbeddingModel();
        model.setDimensions(3);
        when(modelRepository.findByModelKey("MINILM_L6_SPACY_V3")).thenReturn(model);
        repository = new CandidateMatchingRepository(jdbc, properties, modelRepository);
    }

    @Test
    void sqlCollapsesExperiencesToCandidatesUsingBestScore() {
        String sql = repository.buildSql(
            CandidateMatchingRepository.PRIMARY_EMBEDDING_TABLE, 3,
            "", "");

        assertThat(sql)
            .contains("MAX(1.0 - sp.distance)")
            .contains("FULL OUTER JOIN semantic_candidates")
            .contains("ORDER BY rrf_score DESC, candidate_id");
    }

    @Test
    void semanticPoolPreservesHnswFriendlyShapeBeforeFiltering() {
        String sql = repository.buildSql(
            CandidateMatchingRepository.PRIMARY_EMBEDDING_TABLE, 3,
            "", "");
        String pool = sql.substring(sql.indexOf("semantic_pool AS"),
            sql.indexOf("semantic_candidate_scores AS"));

        assertThat(pool)
            .contains("FROM job_experience_embedding_minilm_l6_spacy_v3")
            .contains("ORDER BY embedding <=>")
            .contains("LIMIT :semanticPoolSize")
            .doesNotContain("occupation_id")
            .doesNotContain("JOIN candidate_job_experience")
            .doesNotContain("JOIN candidate_occupation");
    }

    @Test
    void bothRanksReceiveBothRrfContributions() {
        assertThat(rrf(1, 2, 2.0, 3.0, 60))
            .isEqualTo(2.0 / 61 + 3.0 / 62);
    }

    @Test
    void lexicalOnlyCandidateReceivesOnlyLexicalContribution() {
        assertThat(rrf(2, null, 2.0, 20.0, 60)).isEqualTo(2.0 / 62);
    }

    @Test
    void semanticOnlyCandidateReceivesOnlySemanticContribution() {
        assertThat(rrf(null, 3, 20.0, 3.0, 60)).isEqualTo(3.0 / 63);
    }

    @Test
    void weightsCanChangeFinalRanking() {
        double lexicalFavouredA = rrf(1, null, 10, 1, 60);
        double lexicalFavouredB = rrf(null, 1, 10, 1, 60);
        double semanticFavouredA = rrf(1, null, 1, 10, 60);
        double semanticFavouredB = rrf(null, 1, 1, 10, 60);

        assertThat(lexicalFavouredA).isGreaterThan(lexicalFavouredB);
        assertThat(semanticFavouredB).isGreaterThan(semanticFavouredA);
    }

    @Test
    void invalidEmbeddingTableNameIsRejected() {
        assertThatThrownBy(() -> repository.buildSql(
            "embedding; drop table candidate", 3, "", ""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid embedding table name");
    }

    @Test
    void wrongEmbeddingDimensionsAreRejected() {
        assertThatThrownBy(() -> repository.match(
            request(List.of(0.1, 0.2)), "", ""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("expected 3");
    }

    @Test
    @SuppressWarnings("unchecked")
    void nullableRanksAndScoresMapCorrectly() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getLong("candidate_id")).thenReturn(42L);
        when(rs.getObject("lexical_rank")).thenReturn(null);
        when(rs.getObject("semantic_rank")).thenReturn(2L);
        when(rs.getObject("lexical_score")).thenReturn(null);
        when(rs.getObject("semantic_score")).thenReturn(0.75);
        when(rs.getDouble("rrf_score")).thenReturn(0.02);
        when(jdbc.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
            .thenAnswer(invocation -> {
                RowMapper<CandidateMatchingResult> mapper = invocation.getArgument(2);
                return List.of(mapper.mapRow(rs, 0));
            });

        CandidateMatchingResult result = repository.match(
            request(List.of(0.1, 0.2, 0.3)), "", "").get(0);

        assertThat(result.getLexicalRank()).isNull();
        assertThat(result.getLexicalScore()).isNull();
        assertThat(result.getSemanticRank()).isEqualTo(2);
        assertThat(result.getSemanticScore()).isEqualTo(0.75);
    }

    @Test
    void finalOrderingIsDeterministicForTiedScores() {
        List<CandidateMatchingResult> results = new ArrayList<>(List.of(
            result(9, 0.1), result(2, 0.1), result(5, 0.2)));

        results.sort(Comparator.comparingDouble(CandidateMatchingResult::getRrfScore)
            .reversed().thenComparingLong(CandidateMatchingResult::getCandidateId));

        assertThat(results).extracting(CandidateMatchingResult::getCandidateId)
            .containsExactly(5L, 2L, 9L);
    }

    private CandidateMatchingRequest request(List<Double> embedding) {
        return CandidateMatchingRequest.builder()
            .queryText("java engineer")
            .queryEmbedding(embedding)
            .occupationId(7L)
            .lexicalWeight(1)
            .semanticWeight(1)
            .rrfK(60)
            .lexicalExperienceLimit(100)
            .lexicalCandidateLimit(20)
            .semanticPoolSize(200)
            .semanticCandidateLimit(20)
            .finalResultLimit(10)
            .build();
    }

    private static CandidateMatchingResult result(long candidateId, double score) {
        return CandidateMatchingResult.builder()
            .candidateId(candidateId)
            .rrfScore(score)
            .build();
    }

    private static double rrf(
        Integer lexicalRank, Integer semanticRank, double lexicalWeight,
        double semanticWeight, int k) {
        return (lexicalRank == null ? 0 : lexicalWeight / (k + lexicalRank))
            + (semanticRank == null ? 0 : semanticWeight / (k + semanticRank));
    }
}
