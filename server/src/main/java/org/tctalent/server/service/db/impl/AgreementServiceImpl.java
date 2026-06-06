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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.model.db.Agreement;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.Counterparty;
import org.tctalent.server.model.db.TermsInfo;
import org.tctalent.server.model.db.TermsType;
import org.tctalent.server.repository.db.AgreementRepository;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.AgreementService;
import org.tctalent.server.service.db.TermsInfoService;

/**
 * Implementation of {@link AgreementService}
 *
 * @author sadatmalik
 */
@Service
@RequiredArgsConstructor
public class AgreementServiceImpl implements AgreementService {

    private final AgreementRepository agreementRepository;
    private final AuthService authService;
    private final TermsInfoService termsInfoService;

    @Override
    @NonNull
    @Transactional
    public Agreement recordAgreement(
        @NonNull Candidate candidate,
        @NonNull Counterparty counterparty,
        @NonNull String termsInfoId) {

        Optional<Agreement> activeAgreementOpt =
            agreementRepository.findFirstByCandidateIdAndCounterpartyTypeAndEndIsNullOrderByStartDesc(
                candidate.getId(), counterparty.getType());

        if (activeAgreementOpt.isPresent()) {
            Agreement activeAgreement = activeAgreementOpt.get();
            if (Objects.equals(activeAgreement.getTermsInfoId(), termsInfoId)) {
                return activeAgreement;
            }
            activeAgreement.setEnd(OffsetDateTime.now());
            agreementRepository.save(activeAgreement);
        }

        Agreement agreement = new Agreement();
        agreement.setCandidate(candidate);
        agreement.setCounterparty(counterparty);
        agreement.setTermsInfoId(termsInfoId);
        agreement.setStart(OffsetDateTime.now());
        return agreementRepository.save(agreement);
    }

    @Override
    @NonNull
    @Transactional(readOnly = true)
    public List<Agreement> listMyAgreements() {
        Long candidateId = authService.getLoggedInCandidateId();
        return agreementRepository.findWithCounterpartyByCandidateIdOrderByStartDesc(candidateId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean needsAcceptance(
        @NonNull Candidate candidate,
        @NonNull Counterparty counterparty,
        @NonNull TermsType termsType) {

        TermsInfo currentTerms = termsInfoService.getCurrentByType(termsType);
        if (currentTerms.getContent() == null || currentTerms.getContent().isBlank()) {
            return false;
        }

        Optional<Agreement> activeAgreementOpt =
            agreementRepository.findFirstByCandidateIdAndCounterpartyTypeAndEndIsNullOrderByStartDesc(
                candidate.getId(), counterparty.getType());

        if (activeAgreementOpt.isEmpty()) {
            return true;
        }

        Agreement activeAgreement = activeAgreementOpt.get();
        return !Objects.equals(activeAgreement.getTermsInfoId(), currentTerms.getId());
    }
}
