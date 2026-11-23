package org.tctalent.server.service.explanation.dto;

import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Verifies that validation cascades into the elements of each list in the explanation DTOs,
 * via {@code List<@Valid X>} type argument annotations.
 */
class ExplanationDtoValidationTest {

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
    @DisplayName("should accept a valid request")
    void shouldAccept_validRequest() {
        ExplanationsRequest request = ExplanationsRequest.builder()
            .opportunityDescription("Job description")
            .candidates(List.of(candidateInput("123", experienceInput("456"))))
            .build();

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    @DisplayName("should reject a request whose candidate is invalid")
    void shouldRejectRequest_whenCandidateInvalid() {
        ExplanationsRequest request = ExplanationsRequest.builder()
            .opportunityDescription("Job description")
            .candidates(List.of(candidateInput("", experienceInput("456"))))
            .build();

        assertHasViolationAt(validator.validate(request), "candidates[0].candidateId");
    }

    @Test
    @DisplayName("should reject a candidate input whose experience is invalid")
    void shouldRejectCandidateInput_whenExperienceInvalid() {
        ExplanationCandidateInput candidate = candidateInput("123", experienceInput(""));

        assertHasViolationAt(validator.validate(candidate), "experiences[0].experienceId");
    }

    @Test
    @DisplayName("should accept a valid response")
    void shouldAccept_validResponse() {
        ExplanationsResponse response = response(result("123", explanationItem("456")));

        assertTrue(validator.validate(response).isEmpty());
    }

    @Test
    @DisplayName("should reject a response whose result is invalid")
    void shouldRejectResponse_whenResultInvalid() {
        ExplanationsResponse response = response(result("", explanationItem("456")));

        assertHasViolationAt(validator.validate(response), "results[0].candidateId");
    }

    @Test
    @DisplayName("should reject a result whose experience explanation is invalid")
    void shouldRejectResult_whenExperienceExplanationInvalid() {
        ExplanationResult result = result("123", explanationItem(""));

        assertHasViolationAt(validator.validate(result), "experienceExplanations[0].experienceId");
    }

    private static ExplanationExperienceInput experienceInput(String experienceId) {
        return ExplanationExperienceInput.builder()
            .experienceId(experienceId)
            .jobTitle("Engineer")
            .description("Built things")
            .build();
    }

    private static ExplanationCandidateInput candidateInput(
        String candidateId, ExplanationExperienceInput experience) {
        return ExplanationCandidateInput.builder()
            .candidateId(candidateId)
            .experiences(List.of(experience))
            .build();
    }

    private static ExperienceExplanationItem explanationItem(String experienceId) {
        return ExperienceExplanationItem.builder()
            .experienceId(experienceId)
            .explanation("Relevant experience")
            .build();
    }

    private static ExplanationResult result(String candidateId, ExperienceExplanationItem item) {
        return ExplanationResult.builder()
            .candidateId(candidateId)
            .summary("Strong match")
            .experienceExplanations(List.of(item))
            .build();
    }

    private static ExplanationsResponse response(ExplanationResult result) {
        return ExplanationsResponse.builder()
            .requested(1)
            .succeeded(1)
            .failed(0)
            .results(List.of(result))
            .build();
    }

    private static <T> void assertHasViolationAt(
        Set<ConstraintViolation<T>> violations, String propertyPath) {
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals(propertyPath)),
            "Expected a violation at " + propertyPath + " but got " + violations);
    }
}
