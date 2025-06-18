package org.tctalent.server.service.db.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    private TermsInfo v1Info;
    private TermsInfo v2Info;

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

        v1Info = new TermsInfo(
            "PolicyV1",
            "terms/fred.html",
            TermsType.CANDIDATE_PRIVACY_POLICY,
            LocalDate.of(2025, Month.JUNE, 1)
        );

        v2Info = new TermsInfo(
            "PolicyV2", // More recent version of policy - see date below
            "terms/fred.html",
            TermsType.CANDIDATE_PRIVACY_POLICY,
            LocalDate.of(2025, Month.JUNE, 2) //Later version
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

    @Test
    void shouldThrowIfNoMatchingId() {
        TermsInfo[] terms = new TermsInfo[]{};

        // When
        service.initialize(terms);

        //Then
        RuntimeException ex = assertThrows(NoSuchObjectException.class,
            () -> service.get("MissingId"));
        assertTrue(ex.getMessage().contains("Missing"));
    }

    @Test
    void shouldSelectMatchingId() {
        // Given
        given(service.getContentFromResource("terms/fred.html"))
            .willReturn("<p>Mocked Terms Content</p>");

        TermsInfo[] terms = new TermsInfo[]{v1Info, v2Info};

        // When
        service.initialize(terms);
        TermsInfo termsInfo = service.get("PolicyV1");

        //Then
        assertNotNull(termsInfo);
        assertEquals("PolicyV1", termsInfo.getId());

        // When
        termsInfo = service.get("PolicyV2");

        //Then
        assertNotNull(termsInfo);
        assertEquals("PolicyV2", termsInfo.getId());

    }

    @Test
    void shouldThrowIfNoMatchingTermsType() {
        TermsInfo[] terms = new TermsInfo[]{};

        // When
        service.initialize(terms);

        //Then
        RuntimeException ex = assertThrows(NoSuchObjectException.class,
            () -> service.getCurrentByType(TermsType.CANDIDATE_PRIVACY_POLICY));
        assertTrue(ex.getMessage().contains("Missing"));
    }

    @Test
    void shouldSelectMostRecentOfTermsType() {
        // Given
        given(service.getContentFromResource("terms/fred.html"))
            .willReturn("<p>Mocked Terms Content</p>");

        TermsInfo[] terms = new TermsInfo[]{v1Info, v2Info};

        // When
        service.initialize(terms);
        final TermsInfo termsInfo = service.getCurrentByType(TermsType.CANDIDATE_PRIVACY_POLICY);

        //Then
        assertNotNull(termsInfo);
        assertEquals("PolicyV2", termsInfo.getId());
    }
}
