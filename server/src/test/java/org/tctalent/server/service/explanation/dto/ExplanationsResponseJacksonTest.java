package org.tctalent.server.service.explanation.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Verifies that {@link ExplanationsResponse} deserializes representative JSON as actually
 * produced by the Python match explanation service - in particular the structured, nested
 * {@code error} object - rather than only round-tripping Java-constructed DTOs.
 */
class ExplanationsResponseJacksonTest {

    // Configured the same way as Spring's auto-configured ObjectMapper (used by the RestClient),
    // which registers the java.time module needed for generated_at.
    private final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

    @Test
    void shouldDeserialize_successfulResult() throws Exception {
        String json = """
            {
              "requested": 1,
              "succeeded": 1,
              "failed": 0,
              "results": [
                {
                  "candidate_id": "123",
                  "generated_at": "2026-10-02T03:30:00Z",
                  "model_name": "qwen.qwen3-235b-a22b-2507-v1:0",
                  "summary": "The candidate is a strong match.",
                  "experience_explanations": [
                    {
                      "experience_id": "456",
                      "job_title": "Senior Software Engineer",
                      "explanation": "Directly relevant experience."
                    },
                    {
                      "experience_id": "789",
                      "job_title": null,
                      "explanation": "Some transferable experience."
                    }
                  ],
                  "limitations": ["Limited detail in job description."],
                  "error": null
                }
              ]
            }
            """;

        ExplanationsResponse response = objectMapper.readValue(json, ExplanationsResponse.class);

        assertEquals(1, response.getRequested());
        assertEquals(1, response.getSucceeded());
        assertEquals(0, response.getFailed());
        assertEquals(1, response.getResults().size());

        ExplanationResult result = response.getResults().get(0);
        assertEquals("123", result.getCandidateId());
        assertEquals(OffsetDateTime.parse("2026-10-02T03:30:00Z"), result.getGeneratedAt());
        assertEquals("qwen.qwen3-235b-a22b-2507-v1:0", result.getModelName());
        assertEquals("The candidate is a strong match.", result.getSummary());
        assertNotNull(result.getExperienceExplanations());
        assertEquals(2, result.getExperienceExplanations().size());
        assertEquals("456", result.getExperienceExplanations().get(0).getExperienceId());
        assertEquals("Senior Software Engineer",
            result.getExperienceExplanations().get(0).getJobTitle());
        assertEquals("Directly relevant experience.",
            result.getExperienceExplanations().get(0).getExplanation());
        assertEquals("789", result.getExperienceExplanations().get(1).getExperienceId());
        assertNull(result.getExperienceExplanations().get(1).getJobTitle());
        assertNotNull(result.getLimitations());
        assertEquals("Limited detail in job description.", result.getLimitations().get(0));
        assertNull(result.getError());
    }

    @Test
    void shouldDeserialize_itemLevelFailureWithStructuredError() throws Exception {
        String json = """
            {
              "requested": 1,
              "succeeded": 0,
              "failed": 1,
              "results": [
                {
                  "candidate_id": "123",
                  "generated_at": null,
                  "model_name": null,
                  "summary": null,
                  "experience_explanations": null,
                  "limitations": null,
                  "error": {
                    "code": "LLM_SERVICE_UNAVAILABLE",
                    "message": "The LLM service is unavailable"
                  }
                }
              ]
            }
            """;

        ExplanationsResponse response = objectMapper.readValue(json, ExplanationsResponse.class);

        ExplanationResult result = response.getResults().get(0);
        assertEquals("123", result.getCandidateId());
        assertNull(result.getGeneratedAt());
        assertNull(result.getModelName());
        assertNull(result.getSummary());
        assertNull(result.getExperienceExplanations());
        assertNull(result.getLimitations());
        assertNotNull(result.getError());
        assertEquals("LLM_SERVICE_UNAVAILABLE", result.getError().getCode());
        assertEquals("The LLM service is unavailable", result.getError().getMessage());
    }
}
