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
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.request.verify.VerifyPlusScanRequest;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.verify.VerifyPlusIngestResult;
import org.tctalent.server.service.db.verify.VerifyPlusPayload;
import org.tctalent.server.service.db.verify.VerifyPlusPayloadParser;
import org.tctalent.server.service.db.verify.VerifyPlusService;

// TODO doco
// Flow: logged-in candidate -> parse raw payload -> duplicate check across active-like statuses->
// overwrite candidate.unhcrNumber -> return {unhcrNumber, duplicate}.
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

    @Override
    @Transactional
    public VerifyPlusIngestResult ingestScan(VerifyPlusScanRequest request) {
        // TODO - SM - ok for testing via Casi service - but for registration flow confirm if the
        //  candidate will be logged in if the scan is handled as a distinct "step 2"
        Candidate candidate = candidateService.getLoggedInCandidate()
            .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        VerifyPlusPayload payload = payloadParser.parse(request.getRawPayload());
        String unhcrId = payload.getUnhcrId();

        boolean duplicate = !candidateRepository.findOthersByUnhcrNumber(
            ACTIVE_STATUSES,
            unhcrId,
            candidate.getId()
        ).isEmpty();

        candidate.setUnhcrNumber(unhcrId);
        candidateService.save(candidate);

        return new VerifyPlusIngestResult(unhcrId, duplicate);
    }
}
