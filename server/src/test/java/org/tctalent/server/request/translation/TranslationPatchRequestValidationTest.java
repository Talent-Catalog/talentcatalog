package org.tctalent.server.request.translation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TranslationPatchRequestValidationTest {

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
    @DisplayName("should accept a request with valid entries")
    void shouldAccept_whenEntriesValid() {
        assertTrue(validator.validate(request(entry("HOME.TITLE"))).isEmpty());
    }

    @Test
    @DisplayName("should reject a request whose entry is invalid")
    void shouldReject_whenEntryInvalid() {
        Set<ConstraintViolation<TranslationPatchRequest>> violations =
            validator.validate(request(entry("")));

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("entries[0].key")),
            "Expected a violation at entries[0].key but got " + violations);
    }

    private static TranslationPatchEntry entry(String key) {
        TranslationPatchEntry entry = new TranslationPatchEntry();
        entry.setKey(key);
        entry.setValues(Map.of("en", "Home"));
        return entry;
    }

    private static TranslationPatchRequest request(TranslationPatchEntry entry) {
        TranslationPatchRequest request = new TranslationPatchRequest();
        request.setVersion(1);
        request.setEntries(List.of(entry));
        return request;
    }
}
