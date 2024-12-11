/*
 * Copyright (c) 2024 Talent Catalog.
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateEducation;
import org.tctalent.server.model.db.CandidateReviewStatusItem;
import org.tctalent.server.model.db.SavedSearch;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.CandidateReviewStatusRepository;
import org.tctalent.server.repository.db.SavedSearchRepository;
import org.tctalent.server.request.reviewstatus.CreateCandidateReviewStatusRequest;
import org.tctalent.server.request.reviewstatus.UpdateCandidateReviewStatusRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateReviewStatusService;

@Service
public class CandidateReviewStatusServiceImpl implements CandidateReviewStatusService {

    private final CandidateRepository candidateRepository;
    private final CandidateReviewStatusRepository candidateReviewStatusRepository;
    private final SavedSearchRepository savedSearchRepository;
    private final AuthService authService;

    @Autowired
    public CandidateReviewStatusServiceImpl(CandidateRepository candidateRepository, CandidateReviewStatusRepository candidateReviewStatusRepository, SavedSearchRepository savedSearchRepository, AuthService authService) {
        this.candidateRepository = candidateRepository;
        this.candidateReviewStatusRepository = candidateReviewStatusRepository;
        this.savedSearchRepository = savedSearchRepository;
        this.authService = authService;
    }

    @Override
    public CandidateReviewStatusItem getCandidateReviewStatusItem(long id) {
        CandidateReviewStatusItem candidateReviewStatusItem = candidateReviewStatusRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(CandidateReviewStatusItem.class, id));
        return candidateReviewStatusItem;
    }

    @Override
    public CandidateReviewStatusItem createCandidateReviewStatusItem(CreateCandidateReviewStatusRequest request) {
        Candidate candidate = candidateRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new NoSuchObjectException(Candidate.class, request.getCandidateId()));

        SavedSearch savedSearch = savedSearchRepository.findById(request.getSavedSearchId())
                .orElseThrow(() -> new NoSuchObjectException(SavedSearch.class, request.getSavedSearchId()));
        CandidateReviewStatusItem candidateReviewStatusItem = new CandidateReviewStatusItem();
        candidateReviewStatusItem.setCandidate(candidate);
        candidateReviewStatusItem.setSavedSearch(savedSearch);
        candidateReviewStatusItem.setComment(request.getComment());
        candidateReviewStatusItem.setReviewStatus(request.getReviewStatus());
        candidateReviewStatusItem.setAuditFields(authService.getLoggedInUser().orElse(null));
        return candidateReviewStatusRepository.save(candidateReviewStatusItem);
    }

    @Override
    public CandidateReviewStatusItem updateCandidateReviewStatusItem(long id, UpdateCandidateReviewStatusRequest request) {
        CandidateReviewStatusItem candidateReviewStatusItem = this.candidateReviewStatusRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(CandidateEducation.class, id));

        candidateReviewStatusItem.setReviewStatus(request.getReviewStatus());
        candidateReviewStatusItem.setComment(request.getComment());
        candidateReviewStatusItem.setAuditFields(authService.getLoggedInUser().orElse(null));

        return candidateReviewStatusRepository.save(candidateReviewStatusItem);

    }

}
