package org.tctalent.server.service.explanation.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

/**
 * Verifies that {@link ExplanationsResponse} deserializes representative JSON as actually
 * produced by the Python match explanation service - in particular the structured, nested
 * {@code error} object - rather than only round-tripping Java-constructed DTOs.
 */
class ExplanationsResponseJacksonTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

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
                  "summary": "The candidate is a strong match.",
                  "experience_explanations": [
                    {
                      "experience_id": "456",
                      "explanation": "Directly relevant experience."
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
        assertEquals("The candidate is a strong match.", result.getSummary());
        assertNotNull(result.getExperienceExplanations());
        assertEquals(1, result.getExperienceExplanations().size());
        assertEquals("456", result.getExperienceExplanations().get(0).getExperienceId());
        assertEquals("Directly relevant experience.",
            result.getExperienceExplanations().get(0).getExplanation());
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
        assertNull(result.getSummary());
        assertNull(result.getExperienceExplanations());
        assertNull(result.getLimitations());
        assertNotNull(result.getError());
        assertEquals("LLM_SERVICE_UNAVAILABLE", result.getError().getCode());
        assertEquals("The LLM service is unavailable", result.getError().getMessage());
    }
}
