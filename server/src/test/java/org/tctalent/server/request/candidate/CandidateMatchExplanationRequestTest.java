package org.tctalent.server.request.candidate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    @DisplayName("should reject blank opportunity description")
    void shouldReject_whenOpportunityDescriptionBlank() {
        CandidateMatchExplanationRequest request = new CandidateMatchExplanationRequest();
        request.setOpportunityDescription("   ");

        Set<ConstraintViolation<CandidateMatchExplanationRequest>> violations =
            validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("opportunityDescription")));
    }

    @Test
    @DisplayName("should reject null opportunity description")
    void shouldReject_whenOpportunityDescriptionNull() {
        CandidateMatchExplanationRequest request = new CandidateMatchExplanationRequest();

        Set<ConstraintViolation<CandidateMatchExplanationRequest>> violations =
            validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("should accept a nonblank opportunity description")
    void shouldAccept_whenOpportunityDescriptionNonBlank() {
        CandidateMatchExplanationRequest request = new CandidateMatchExplanationRequest();
        request.setOpportunityDescription("We are looking for an experienced Java developer.");

        Set<ConstraintViolation<CandidateMatchExplanationRequest>> violations =
            validator.validate(request);

        assertEquals(0, violations.size());
    }

    @Test
    @DisplayName("should accept a request with no jobId")
    void shouldAccept_whenJobIdAbsent() {
        CandidateMatchExplanationRequest request = new CandidateMatchExplanationRequest();
        request.setOpportunityDescription("We are looking for an experienced Java developer.");

        assertEquals(0, validator.validate(request).size());
        assertNull(request.getJobId());
    }

    @Test
    @DisplayName("should accept a request with a jobId supplied")
    void shouldAccept_whenJobIdSupplied() {
        CandidateMatchExplanationRequest request = new CandidateMatchExplanationRequest();
        request.setJobId(12345L);
        request.setOpportunityDescription("We are looking for an experienced Java developer.");

        assertEquals(0, validator.validate(request).size());
        assertEquals(12345L, request.getJobId());
    }
}
