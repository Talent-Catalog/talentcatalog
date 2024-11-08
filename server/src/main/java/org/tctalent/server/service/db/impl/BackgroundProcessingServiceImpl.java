/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.db.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientException;
import org.tctalent.server.exception.SalesforceException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateStatus;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.service.db.BackgroundProcessingService;
import org.tctalent.server.service.db.CandidateService;

/**
 * Provides separation for background processing methods when desired. Particularly useful when
 * processing requires Spring's @Transactional annotation, which doesn't work when annotated method
 * is called by another in its class.
 */
@Service
@RequiredArgsConstructor
public class BackgroundProcessingServiceImpl implements BackgroundProcessingService {
    private final CandidateRepository candidateRepository;
    private final CandidateService candidateService;

    @Transactional
    @Override
    public void processSfCandidateSyncPage(
        long startPage, List<CandidateStatus> statuses
    ) throws SalesforceException, WebClientException {
      // Obtain and process new page
      Pageable newPageable =
          PageRequest.of((int) startPage, 200, Sort.by("id").ascending());
      Page<Candidate> newCandidatePage = candidateRepository
          .findByStatusesOrSfLinkIsNotNull(statuses, newPageable);
      List<Candidate> candidateList = newCandidatePage.getContent();
      candidateService.upsertCandidatesToSf(candidateList);
    }
}
