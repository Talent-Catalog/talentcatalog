/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.db.impl;

import java.util.Set;

import org.springframework.stereotype.Service;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.CandidateSavedList;
import org.tbbtalent.server.model.db.CandidateSavedListKey;
import org.tbbtalent.server.model.db.SavedList;
import org.tbbtalent.server.repository.db.CandidateSavedListRepository;
import org.tbbtalent.server.request.candidate.UpdateCandidateContextNoteRequest;
import org.tbbtalent.server.service.db.CandidateSavedListService;

@Service
public class CandidateSavedListServiceImpl implements CandidateSavedListService {
    private final CandidateSavedListRepository candidateSavedListRepository;

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
        candidateSavedListRepository.delete(csl);
        csl.getCandidate().getCandidateSavedLists().remove(csl);
        csl.getSavedList().getCandidateSavedLists().remove(csl);
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
