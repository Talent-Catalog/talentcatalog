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

package org.tctalent.server.service.db.verify.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateStatus;
import org.tctalent.server.model.db.YesNoUnsure;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.request.verify.VerifyPlusScanRequest;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.verify.VerifyPlusIngestResult;
import org.tctalent.server.service.db.verify.VerifyPlusPayload;
import org.tctalent.server.service.db.verify.VerifyPlusPayloadParser;
import org.tctalent.server.service.db.verify.VerifyPlusService;

/**
 * Ingests a Verify+ QR scan for the logged-in candidate.
 * <p>
 * Flow: resolve logged-in candidate → parse raw payload → duplicate-check UNHCR ID across
 * active-like statuses → set {@code unhcrRegistered=Yes} and overwrite {@code unhcrNumber} →
 * return {@code {unhcrNumber, duplicate}}.
 * <p>
 * Used by both the Services tab and the registration wizard scan step.
 *
 * @author sadatmalik
 */
@Service
@RequiredArgsConstructor
public class VerifyPlusServiceImpl implements VerifyPlusService {

    private static final List<CandidateStatus> ACTIVE_STATUSES =
        List.of(
            CandidateStatus.active,
            CandidateStatus.unreachable,
            CandidateStatus.incomplete,
            CandidateStatus.pending
        );

    private final CandidateService candidateService;
    private final CandidateRepository candidateRepository;
    private final VerifyPlusPayloadParser payloadParser;

    /**
     * Ingests a Verify+ scan for the currently logged-in candidate.
     * <p>
     * Both call sites require an authenticated session: the Services tab (profile) and the
     * registration wizard scan step, which sits after account creation / contact so the
     * candidate is already logged in when this method runs.
     *
     * @param request raw Verify+ QR payload from the portal
     * @return ingested UNHCR number and whether it duplicates another active-like candidate
     * @throws InvalidSessionException if no candidate is logged in
     */
    @Override
    @Transactional
    public VerifyPlusIngestResult ingestScan(VerifyPlusScanRequest request) {
        Candidate candidate = candidateService.getLoggedInCandidate()
            .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        VerifyPlusPayload payload = payloadParser.parse(request.getRawPayload());
        String unhcrId = payload.getUnhcrId();

        boolean duplicate = !candidateRepository.findOthersByUnhcrNumber(
            ACTIVE_STATUSES,
            unhcrId,
            candidate.getId()
        ).isEmpty();

        candidate.setUnhcrRegistered(YesNoUnsure.Yes);
        candidate.setUnhcrNumber(unhcrId);
        candidateService.save(candidate);

        return new VerifyPlusIngestResult(unhcrId, duplicate);
    }
}
