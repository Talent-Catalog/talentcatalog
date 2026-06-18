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

package org.tctalent.server.repository.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.tctalent.server.integration.helper.TestDataFactory.createAndSaveCandidate;
import static org.tctalent.server.integration.helper.TestDataFactory.createAndSaveUser;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.integration.helper.BaseJpaIntegrationTest;
import org.tctalent.server.model.db.Agreement;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.Counterparty;
import org.tctalent.server.model.db.CounterpartyType;
import org.tctalent.server.model.db.TermsType;

@Tag("integration")
@ActiveProfiles("integration-test")
class AgreementRepositoryIntegrationTest extends BaseJpaIntegrationTest {

    @Autowired
    private AgreementRepository agreementRepository;

    @Autowired
    private CounterpartyRepository counterpartyRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private UserRepository userRepository;

    private Candidate candidate;
    private Counterparty databaseProvider;
    private Counterparty serviceProvider;

    @BeforeEach
    void setUp() {
        candidate = createAndSaveCandidate(candidateRepository, createAndSaveUser(userRepository));

        databaseProvider = new Counterparty();
        databaseProvider.setType(CounterpartyType.DATABASE_PROVIDER);
        databaseProvider.setName("OPC");
        databaseProvider = counterpartyRepository.saveAndFlush(databaseProvider);

        serviceProvider = new Counterparty();
        serviceProvider.setType(CounterpartyType.SERVICE_PROVIDER);
        serviceProvider.setServiceProvider(ServiceProvider.LINKEDIN);
        serviceProvider.setName("LinkedIn");
        serviceProvider = counterpartyRepository.saveAndFlush(serviceProvider);
    }

    @Test
    @DisplayName("findByTypeAndNameIgnoreCase finds counterparty by name case-insensitively")
    void findByTypeAndNameIgnoreCase_findsCounterparty() {
        Optional<Counterparty> result = counterpartyRepository.findByTypeAndNameIgnoreCase(
            CounterpartyType.DATABASE_PROVIDER, "opc");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(databaseProvider.getId());
    }

    @Test
    @DisplayName("findByTypeAndServiceProvider finds service provider counterparty")
    void findByTypeAndServiceProvider_findsCounterparty() {
        Optional<Counterparty> result = counterpartyRepository.findByTypeAndServiceProvider(
            CounterpartyType.SERVICE_PROVIDER, ServiceProvider.LINKEDIN);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(serviceProvider.getId());
    }

    @Test
    @DisplayName("findByCandidateIdOrderByStartDesc returns newest first")
    void findByCandidateIdOrderByStartDesc_returnsNewestFirst() {
        Agreement older = saveAgreement(
            candidate,
            databaseProvider,
            "OpcDataProcessingAgreementV1",
            TermsType.OPC_STANDARD_DATA_PROCESSING_AGREEMENT,
            OffsetDateTime.now().minusDays(7),
            OffsetDateTime.now().minusDays(2));
        Agreement newer = saveAgreement(
            candidate, databaseProvider, "OpcDataProcessingAgreementV2",
            TermsType.OPC_STANDARD_DATA_PROCESSING_AGREEMENT, OffsetDateTime.now().minusDays(1), null);

        List<Agreement> agreements = agreementRepository.findByCandidateIdOrderByStartDesc(candidate.getId());

        assertThat(agreements).hasSize(2);
        assertThat(agreements.get(0).getId()).isEqualTo(newer.getId());
        assertThat(agreements.get(1).getId()).isEqualTo(older.getId());
    }

    @Test
    @DisplayName("findFirstByCandidateIdAndCounterpartyIdAndTermsTypeAndEndIsNullOrderByStartDesc returns active record")
    void findFirstByCandidateIdAndCounterpartyIdAndTermsTypeAndEndIsNullOrderByStartDesc_returnsActive() {
        saveAgreement(
            candidate,
            databaseProvider,
            "OpcDataProcessingAgreementV1",
            TermsType.OPC_STANDARD_DATA_PROCESSING_AGREEMENT,
            OffsetDateTime.now().minusDays(10),
            OffsetDateTime.now().minusDays(5));
        Agreement active = saveAgreement(
            candidate,
            databaseProvider,
            "OpcDataProcessingAgreementV2",
            TermsType.OPC_STANDARD_DATA_PROCESSING_AGREEMENT,
            OffsetDateTime.now().minusDays(3),
            null);

        Optional<Agreement> current = agreementRepository
            .findFirstByCandidateIdAndCounterpartyIdAndTermsTypeAndEndIsNullOrderByStartDesc(
                candidate.getId(), databaseProvider.getId(), TermsType.OPC_STANDARD_DATA_PROCESSING_AGREEMENT);

        assertThat(current).isPresent();
        assertThat(current.get().getId()).isEqualTo(active.getId());
        assertThat(current.get().getEnd()).isNull();
    }

    @Test
    @DisplayName("two active agreements of different terms types are allowed for same counterparty")
    void twoActiveAgreementsOfDifferentTypesAreAllowedForSameCounterparty() {
        Agreement opcDpaAgreement = saveAgreement(
            candidate,
            serviceProvider,
            "OpcDataProcessingAgreementV1",
            TermsType.OPC_STANDARD_DATA_PROCESSING_AGREEMENT,
            OffsetDateTime.now().minusDays(2),
            null);
        Agreement referenceAgreement = saveAgreement(
            candidate,
            serviceProvider,
            "ReferenceServiceTermsV1",
            TermsType.REFERENCE_SERVICE_TERMS,
            OffsetDateTime.now().minusDays(1),
            null);

        Optional<Agreement> currentOpcDpa = agreementRepository
            .findFirstByCandidateIdAndCounterpartyIdAndTermsTypeAndEndIsNullOrderByStartDesc(
                candidate.getId(), serviceProvider.getId(), TermsType.OPC_STANDARD_DATA_PROCESSING_AGREEMENT);
        Optional<Agreement> currentReference = agreementRepository
            .findFirstByCandidateIdAndCounterpartyIdAndTermsTypeAndEndIsNullOrderByStartDesc(
                candidate.getId(), serviceProvider.getId(), TermsType.REFERENCE_SERVICE_TERMS);

        assertThat(currentOpcDpa).isPresent();
        assertThat(currentOpcDpa.get().getId()).isEqualTo(opcDpaAgreement.getId());
        assertThat(currentReference).isPresent();
        assertThat(currentReference.get().getId()).isEqualTo(referenceAgreement.getId());
    }

    private Agreement saveAgreement(
        Candidate agreementCandidate,
        Counterparty counterparty,
        String termsInfoId,
        TermsType termsType,
        OffsetDateTime start,
        OffsetDateTime end) {
        Agreement agreement = new Agreement();
        agreement.setCandidate(agreementCandidate);
        agreement.setCounterparty(counterparty);
        agreement.setTermsInfoId(termsInfoId);
        agreement.setTermsType(termsType);
        agreement.setStart(start);
        agreement.setEnd(end);
        return agreementRepository.saveAndFlush(agreement);
    }
}
