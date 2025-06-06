package org.tctalent.server.service.db.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.time.Month;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.TermsInfo;
import org.tctalent.server.model.db.TermsType;

@ExtendWith(MockitoExtension.class)
class TermsInfoServiceImplTest {

    @Spy
    @InjectMocks
    TermsInfoServiceImpl service;

    private TermsInfo validTermsInfo;
    private TermsInfo missingTermsInfo;

    @BeforeEach
    void setUp() {
        validTermsInfo = new TermsInfo(
            "TestPolicy",
            "terms/fred.html",
            TermsType.CANDIDATE_PRIVACY_POLICY,
            LocalDate.of(2025, Month.JUNE, 1)
        );

        missingTermsInfo = new TermsInfo(
            "MissingPolicy",
            "terms/missing.html",
            TermsType.CANDIDATE_PRIVACY_POLICY,
            LocalDate.of(2025, Month.JUNE, 1)
        );
    }

    @Test
    void shouldLoadTermsContentFromResourceSuccessfully() throws NoSuchObjectException {
        // Given
        given(service.getContentFromResource("terms/fred.html"))
            .willReturn("<p>Mocked Terms Content</p>");

        TermsInfo[] termsData = new TermsInfo[] { validTermsInfo };

        // When
        service.initialize(termsData);
        TermsInfo result = service.get("TestPolicy");

        // Then
        assertNotNull(result);
        assertEquals("<p>Mocked Terms Content</p>", result.getContent());
    }

    @Test
    void shouldThrowIfContentMissing() {
        // Given
        given(service.getContentFromResource("terms/missing.html"))
            .willReturn(null);

        TermsInfo[] brokenData = new TermsInfo[] { missingTermsInfo };

        // When & Then
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.initialize(brokenData));

        assertTrue(ex.getMessage().contains("No content found"));
    }

    @Test
    void shouldThrowIfDuplicateIdIsAdded() {
        // Given
        given(service.getContentFromResource("terms/fred.html"))
            .willReturn("<p>Mocked Terms Content</p>");

        TermsInfo first = new TermsInfo(
            "DuplicatePolicy",
            "terms/fred.html",
            TermsType.CANDIDATE_PRIVACY_POLICY,
            LocalDate.of(2025, Month.JUNE, 1)
        );

        TermsInfo duplicate = new TermsInfo(
            "DuplicatePolicy", // same ID as first
            "terms/fred.html",
            TermsType.CANDIDATE_PRIVACY_POLICY,
            LocalDate.of(2025, Month.JUNE, 2)
        );

        TermsInfo[] terms = new TermsInfo[] { first, duplicate };

        // When & Then
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> service.initialize(terms));

        assertTrue(ex.getMessage().contains("Duplicate terms info id: DuplicatePolicy"));
    }
}
