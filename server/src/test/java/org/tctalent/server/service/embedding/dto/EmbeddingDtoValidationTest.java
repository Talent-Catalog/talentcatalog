package org.tctalent.server.service.embedding.dto;

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
 * Verifies that validation cascades into the elements of each list in the embedding DTOs,
 * via {@code List<@Valid X>} type argument annotations.
 */
class EmbeddingDtoValidationTest {

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
        assertTrue(validator.validate(request("input-1")).isEmpty());
    }

    @Test
    @DisplayName("should reject a request whose input is invalid")
    void shouldRejectRequest_whenInputInvalid() {
        assertHasViolationAt(validator.validate(request("")), "inputs[0].id");
    }

    @Test
    @DisplayName("should accept a valid response")
    void shouldAccept_validResponse() {
        EmbeddingResult result = EmbeddingResult.builder()
            .id("input-1")
            .embedding(List.of(0.1, 0.2))
            .build();

        assertTrue(validator.validate(response(result)).isEmpty());
    }

    @Test
    @DisplayName("should reject a response whose result is invalid")
    void shouldRejectResponse_whenResultInvalid() {
        // Neither an embedding nor an error: fails EmbeddingResult.isOutcomeValid()
        EmbeddingResult result = EmbeddingResult.builder()
            .id("input-1")
            .build();

        assertHasViolationAt(validator.validate(response(result)), "results[0].outcomeValid");
    }

    private static EmbeddingModelDetails model() {
        return EmbeddingModelDetails.builder()
            .modelName("all-MiniLM-L6-v2")
            .configurationVersion(EmbeddingConfigurationVersion.SBERT_RAW_V1)
            .dimensions(384)
            .build();
    }

    private static EmbeddingsRequest request(String inputId) {
        return EmbeddingsRequest.builder()
            .model(model())
            .type(EmbeddingInputType.DOCUMENT)
            .input(EmbeddingInput.builder().id(inputId).text("Some text").build())
            .build();
    }

    private static EmbeddingsResponse response(EmbeddingResult result) {
        return EmbeddingsResponse.builder()
            .model(model())
            .requested(1)
            .succeeded(result.getEmbedding() != null ? 1 : 0)
            .failed(result.getError() != null ? 1 : 0)
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
