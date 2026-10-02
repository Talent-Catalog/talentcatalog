package org.tctalent.server.model.db.explanation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hypersistence.utils.hibernate.type.util.ObjectMapperWrapper;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Verifies the jsonb (de)serialization of {@link CandidateJobMatchExplanationData} using the same
 * ObjectMapper that {@code JsonBinaryType} uses to read and write the {@code explanation} column.
 */
class CandidateJobMatchExplanationDataJacksonTest {

    private final ObjectMapper objectMapper = ObjectMapperWrapper.INSTANCE.getObjectMapper();

    @Test
    @DisplayName("should round-trip generatedAt, modelName and jobTitle, storing generatedAt as an ISO-8601 string")
    void shouldRoundTripMetadata() throws Exception {
        CandidateJobMatchExplanationData data = CandidateJobMatchExplanationData.builder()
            .generatedAt(OffsetDateTime.parse("2026-10-02T03:30:00Z"))
            .modelName("qwen.qwen3-235b-a22b-2507-v1:0")
            .summary("Strong match")
            .experienceExplanations(List.of(
                ExperienceMatchExplanationData.builder()
                    .experienceId(456L)
                    .jobTitle("Senior Software Engineer")
                    .explanation("Relevant experience")
                    .build()
            ))
            .limitations(List.of())
            .build();

        String json = objectMapper.writeValueAsString(data);

        assertTrue(json.contains("\"generatedAt\":\"2026-10-02T03:30:00Z\""), json);
        assertTrue(json.contains("\"modelName\":\"qwen.qwen3-235b-a22b-2507-v1:0\""), json);
        assertTrue(json.contains("\"jobTitle\":\"Senior Software Engineer\""), json);

        CandidateJobMatchExplanationData read =
            objectMapper.readValue(json, CandidateJobMatchExplanationData.class);
        assertEquals(data, read);
    }

    @Test
    @DisplayName("should deserialize legacy persisted json without generatedAt, modelName or jobTitle")
    void shouldDeserializeLegacyJson() throws Exception {
        String legacyJson = """
            {
              "summary": "Strong match",
              "experienceExplanations": [
                { "experienceId": 456, "explanation": "Relevant experience" }
              ],
              "limitations": ["Limited detail"]
            }
            """;

        CandidateJobMatchExplanationData data =
            objectMapper.readValue(legacyJson, CandidateJobMatchExplanationData.class);

        assertNull(data.getGeneratedAt());
        assertNull(data.getModelName());
        assertEquals("Strong match", data.getSummary());
        assertEquals(1, data.getExperienceExplanations().size());
        assertEquals(456L, data.getExperienceExplanations().get(0).getExperienceId());
        assertNull(data.getExperienceExplanations().get(0).getJobTitle());
        assertEquals("Relevant experience", data.getExperienceExplanations().get(0).getExplanation());
        assertEquals(List.of("Limited detail"), data.getLimitations());
    }
}
