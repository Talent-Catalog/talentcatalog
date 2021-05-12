/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

package org.tbbtalent.server.service.db.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.CandidateSavedList;
import org.tbbtalent.server.model.db.CandidateSavedListKey;
import org.tbbtalent.server.model.db.SavedList;
import org.tbbtalent.server.repository.db.CandidateSavedListRepository;
import org.tbbtalent.server.request.candidate.UpdateCandidateContextNoteRequest;
import org.tbbtalent.server.service.db.CandidateSavedListService;

import java.util.Set;

@Service
public class CandidateSavedListServiceImpl implements CandidateSavedListService {
    private final CandidateSavedListRepository candidateSavedListRepository;
    private static final Logger log = LoggerFactory.getLogger(CandidateSavedListServiceImpl.class);

    public CandidateSavedListServiceImpl(CandidateSavedListRepository candidateSavedListRepository) {
        this.candidateSavedListRepository = candidateSavedListRepository;
    }

    @Override
    public void clearCandidateSavedLists(Candidate candidate) {
        Set<SavedList> savedLists = candidate.getSavedLists();
        for (SavedList savedList : savedLists) {
            removeFromSavedList(candidate, savedList);
        }
    }

    @Override
    public void clearSavedListCandidates(SavedList savedList) {
        Set<Candidate> candidates = savedList.getCandidates();
        for (Candidate candidate : candidates) {
           removeFromSavedList(candidate, savedList);
        }
    }

    @Override
    public void removeFromSavedList(Candidate candidate, SavedList savedList) {
        final CandidateSavedList csl = new CandidateSavedList(candidate, savedList);
        try {
            candidateSavedListRepository.delete(csl);
            csl.getCandidate().getCandidateSavedLists().remove(csl);
            csl.getSavedList().getCandidateSavedLists().remove(csl);
        } catch (Exception ex) {
            log.warn("Could not delete candidate saved list " + csl.getId(), ex);
        }
    }

    @Override
    public void updateCandidateContextNote(
            long savedListId, UpdateCandidateContextNoteRequest request) {
        CandidateSavedListKey key = 
                new CandidateSavedListKey(request.getCandidateId(), savedListId);
        CandidateSavedList csl = candidateSavedListRepository.findById(key)
                .orElse(null);
        if (csl != null) {
            csl.setContextNote(request.getContextNote());
            candidateSavedListRepository.save(csl);
        }
    }
}
