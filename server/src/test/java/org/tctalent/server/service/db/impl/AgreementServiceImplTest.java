/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.db.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.model.db.Agreement;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.Counterparty;
import org.tctalent.server.model.db.CounterpartyType;
import org.tctalent.server.model.db.TermsInfo;
import org.tctalent.server.model.db.TermsType;
import org.tctalent.server.repository.db.AgreementRepository;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.TermsInfoService;

@ExtendWith(MockitoExtension.class)
class AgreementServiceImplTest {

    @Mock
    private AgreementRepository agreementRepository;

    @Mock
    private TermsInfoService termsInfoService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AgreementServiceImpl agreementService;

    private Candidate candidate;
    private Counterparty databaseProvider;

    @BeforeEach
    void setUp() {
        candidate = new Candidate();
        candidate.setId(5L);

        databaseProvider = new Counterparty();
        databaseProvider.setId(9L);
        databaseProvider.setType(CounterpartyType.DATABASE_PROVIDER);
        databaseProvider.setName("OPC");
    }

    @Test
    @DisplayName("recordAgreement closes active agreement and creates a new one")
    void recordAgreement_closesActiveAgreementAndCreatesNew() {
        Agreement activeAgreement = new Agreement();
        activeAgreement.setId(20L);
        activeAgreement.setCandidate(candidate);
        activeAgreement.setCounterparty(databaseProvider);
        activeAgreement.setTermsInfoId("OldTermsV1");
        activeAgreement.setTermsType(TermsType.GRN_CANDIDATE_PRIVACY_POLICY);
        activeAgreement.setStart(OffsetDateTime.now().minusDays(7));

        given(termsInfoService.get("NewTermsV2")).willReturn(new TermsInfo(
            "NewTermsV2",
            "/terms/ReferenceServiceTermsV1.html",
            TermsType.GRN_CANDIDATE_PRIVACY_POLICY,
            LocalDate.now()));
        given(agreementRepository
            .findFirstByCandidateIdAndCounterpartyIdAndTermsTypeAndEndIsNullOrderByStartDesc(
                candidate.getId(), databaseProvider.getId(), TermsType.GRN_CANDIDATE_PRIVACY_POLICY))
            .willReturn(Optional.of(activeAgreement));
        given(agreementRepository.save(any(Agreement.class))).willAnswer(invocation -> invocation.getArgument(0));

        agreementService.recordAgreement(candidate, databaseProvider, "NewTermsV2");

        ArgumentCaptor<Agreement> agreementCaptor = ArgumentCaptor.forClass(Agreement.class);
        verify(agreementRepository, times(2)).save(agreementCaptor.capture());

        List<Agreement> savedAgreements = agreementCaptor.getAllValues();
        Agreement updatedActiveAgreement = savedAgreements.get(0);
        Agreement createdAgreement = savedAgreements.get(1);

        assertTrue(updatedActiveAgreement.getId().equals(activeAgreement.getId()));
        assertTrue(updatedActiveAgreement.getEnd() != null);
        assertTrue("NewTermsV2".equals(createdAgreement.getTermsInfoId()));
        assertTrue(TermsType.GRN_CANDIDATE_PRIVACY_POLICY == createdAgreement.getTermsType());
        assertTrue(createdAgreement.getEnd() == null);
    }

    @Test
    @DisplayName("recordAgreement is idempotent when same active terms already recorded")
    void recordAgreement_isIdempotentForSameActiveTerms() {
        Agreement activeAgreement = new Agreement();
        activeAgreement.setId(30L);
        activeAgreement.setCandidate(candidate);
        activeAgreement.setCounterparty(databaseProvider);
        activeAgreement.setTermsInfoId("SameTermsV1");
        activeAgreement.setTermsType(TermsType.GRN_CANDIDATE_PRIVACY_POLICY);
        activeAgreement.setStart(OffsetDateTime.now().minusDays(1));

        given(termsInfoService.get("SameTermsV1")).willReturn(new TermsInfo(
            "SameTermsV1",
            "/terms/ReferenceServiceTermsV1.html",
            TermsType.GRN_CANDIDATE_PRIVACY_POLICY,
            LocalDate.now()));
        given(agreementRepository
            .findFirstByCandidateIdAndCounterpartyIdAndTermsTypeAndEndIsNullOrderByStartDesc(
                candidate.getId(), databaseProvider.getId(), TermsType.GRN_CANDIDATE_PRIVACY_POLICY))
            .willReturn(Optional.of(activeAgreement));

        agreementService.recordAgreement(candidate, databaseProvider, "SameTermsV1");

        verify(agreementRepository, never()).save(any(Agreement.class));
    }

    @Test
    @DisplayName("needsAcceptance returns false when current terms have no content")
    void needsAcceptance_falseWhenCurrentTermsHaveNoContent() {
        TermsInfo currentTerms = new TermsInfo(
            "OpcDataProcessingAgreementV2",
            "/terms/OpcDataProcessingAgreement-20260701.html",
            TermsType.OPC_STANDARD_DATA_PROCESSING_AGREEMENT,
            LocalDate.now());
        currentTerms.setContent("");
        given(termsInfoService.getCurrentByType(TermsType.OPC_STANDARD_DATA_PROCESSING_AGREEMENT))
            .willReturn(currentTerms);

        assertFalse(agreementService.needsAcceptance(
            candidate, databaseProvider, TermsType.OPC_STANDARD_DATA_PROCESSING_AGREEMENT));
    }

    @Test
    @DisplayName("needsAcceptance returns true when candidate has no active agreement")
    void needsAcceptance_trueWhenNoActiveAgreement() {
        TermsInfo currentTerms = new TermsInfo(
            "OpcDataProcessingAgreementV2",
            "/terms/OpcDataProcessingAgreement-20260701.html",
            TermsType.OPC_STANDARD_DATA_PROCESSING_AGREEMENT,
            LocalDate.now());
        currentTerms.setContent("<p>Terms</p>");
        given(termsInfoService.getCurrentByType(TermsType.OPC_STANDARD_DATA_PROCESSING_AGREEMENT))
            .willReturn(currentTerms);
        given(agreementRepository
            .findFirstByCandidateIdAndCounterpartyIdAndTermsTypeAndEndIsNullOrderByStartDesc(
                candidate.getId(), databaseProvider.getId(),
                TermsType.OPC_STANDARD_DATA_PROCESSING_AGREEMENT))
            .willReturn(Optional.empty());

        assertTrue(agreementService.needsAcceptance(
            candidate, databaseProvider, TermsType.OPC_STANDARD_DATA_PROCESSING_AGREEMENT));
    }

    @Test
    @DisplayName("recordAgreement allows distinct terms types for same counterparty")
    void recordAgreement_allowsTwoDistinctTermsTypesForSameCounterparty() {
        given(termsInfoService.get("ReferenceServiceTermsV1")).willReturn(new TermsInfo(
            "ReferenceServiceTermsV1",
            "/terms/ReferenceServiceTermsV1.html",
            TermsType.REFERENCE_SERVICE_TERMS,
            LocalDate.now()));
        given(agreementRepository
            .findFirstByCandidateIdAndCounterpartyIdAndTermsTypeAndEndIsNullOrderByStartDesc(
                candidate.getId(), databaseProvider.getId(), TermsType.REFERENCE_SERVICE_TERMS))
            .willReturn(Optional.empty());
        given(agreementRepository.save(any(Agreement.class))).willAnswer(invocation -> invocation.getArgument(0));

        agreementService.recordAgreement(candidate, databaseProvider, "ReferenceServiceTermsV1");

        ArgumentCaptor<Agreement> agreementCaptor = ArgumentCaptor.forClass(Agreement.class);
        verify(agreementRepository, times(1)).save(agreementCaptor.capture());
        Agreement createdAgreement = agreementCaptor.getValue();
        assertTrue(TermsType.REFERENCE_SERVICE_TERMS == createdAgreement.getTermsType());
        assertTrue(createdAgreement.getEnd() == null);
    }

    @Test
    @DisplayName("listMyAgreements returns agreements for logged-in candidate")
    void listMyAgreements_returnsAgreementsForLoggedInCandidate() {
        Agreement activeAgreement = new Agreement();
        activeAgreement.setId(101L);
        activeAgreement.setCandidate(candidate);
        activeAgreement.setCounterparty(databaseProvider);
        activeAgreement.setTermsInfoId("OpcDataProcessingAgreementV1");
        activeAgreement.setStart(OffsetDateTime.now().minusDays(2));

        given(authService.getLoggedInCandidateId()).willReturn(candidate.getId());
        given(agreementRepository.findWithCounterpartyByCandidateIdOrderByStartDesc(candidate.getId()))
            .willReturn(List.of(activeAgreement));

        List<Agreement> result = agreementService.listMyAgreements();

        assertTrue(result.size() == 1);
        assertTrue(result.get(0).getId().equals(101L));
        verify(agreementRepository).findWithCounterpartyByCandidateIdOrderByStartDesc(candidate.getId());
    }
}
