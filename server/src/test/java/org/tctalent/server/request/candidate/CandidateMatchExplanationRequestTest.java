package org.tctalent.server.request.candidate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CandidateMatchExplanationRequestTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    @DisplayName("should reject a request with no matchingId")
    void shouldReject_whenMatchingIdNull() {
        CandidateMatchExplanationRequest request = new CandidateMatchExplanationRequest();

        Set<ConstraintViolation<CandidateMatchExplanationRequest>> violations =
            validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("matchingId")));
    }

    @Test
    @DisplayName("should accept a request with a matchingId, not regenerating by default")
    void shouldAccept_whenMatchingIdSupplied() {
        CandidateMatchExplanationRequest request = new CandidateMatchExplanationRequest();
        request.setMatchingId(12345L);

        assertEquals(0, validator.validate(request).size());
        assertFalse(request.isRegenerate());
    }

    @Test
    @DisplayName("should ignore any opportunityDescription or jobId supplied by a client")
    void shouldIgnoreDescriptionAndJobId() throws Exception {
        //Same as the application's ObjectMapper, which ignores unknown properties.
        ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        CandidateMatchExplanationRequest request = objectMapper.readValue(
            "{\"matchingId\": 3, \"regenerate\": true, \"jobId\": 7,"
                + " \"opportunityDescription\": \"Text from the client\"}",
            CandidateMatchExplanationRequest.class);

        assertEquals(3L, request.getMatchingId());
        assertTrue(request.isRegenerate());
    }
}
