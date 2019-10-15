package org.tbbtalent.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.*;
import org.tbbtalent.server.repository.CandidateShortlistRepository;
import org.tbbtalent.server.repository.CandidateRepository;
import org.tbbtalent.server.repository.SavedSearchRepository;
import org.tbbtalent.server.request.shortlist.CreateCandidateShortlistRequest;
import org.tbbtalent.server.request.shortlist.UpdateCandidateShortlistRequest;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.CandidateShortlistService;

@Service
public class CandidateShortlistServiceImpl implements CandidateShortlistService {

    private final CandidateRepository candidateRepository;
    private final CandidateShortlistRepository candidateShortlistRepository;
    private final SavedSearchRepository savedSearchRepository;
    private final UserContext userContext;

    @Autowired
    public CandidateShortlistServiceImpl(CandidateRepository candidateRepository, CandidateShortlistRepository candidateShortlistRepository, SavedSearchRepository savedSearchRepository, UserContext userContext) {
        this.candidateRepository = candidateRepository;
        this.candidateShortlistRepository = candidateShortlistRepository;
        this.savedSearchRepository = savedSearchRepository;
        this.userContext = userContext;
    }

    @Override
    public CandidateShortlistItem getCandidateShortlistItem(long id) {
        CandidateShortlistItem candidateShortlistItem = candidateShortlistRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(CandidateShortlistItem.class, id));
        return candidateShortlistItem;
    }

    @Override
    public CandidateShortlistItem createCandidateShortlist(CreateCandidateShortlistRequest request) {
        User user = userContext.getLoggedInUser();

        Candidate candidate = candidateRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new NoSuchObjectException(Candidate.class, request.getCandidateId()));

        SavedSearch savedSearch = savedSearchRepository.findById(request.getSavedSearchId())
                .orElseThrow(() -> new NoSuchObjectException(SavedSearch.class, request.getSavedSearchId()));
        CandidateShortlistItem candidateShortlistItem = new CandidateShortlistItem();
        candidateShortlistItem.setCandidate(candidate);
        candidateShortlistItem.setSavedSearch(savedSearch);
        candidateShortlistItem.setComment(request.getComment());
        candidateShortlistItem.setShortlistStatus(request.getShortlistStatus());
        candidateShortlistItem.setAuditFields(user);
        return candidateShortlistRepository.save(candidateShortlistItem);
    }

    @Override
    public CandidateShortlistItem updateCandidateShortlist(long id, UpdateCandidateShortlistRequest request) {
        CandidateShortlistItem candidateShortlistItem = this.candidateShortlistRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(CandidateEducation.class, id));

        User user = userContext.getLoggedInUser();

        candidateShortlistItem.setShortlistStatus(request.getShortlistStatus());
        candidateShortlistItem.setComment(request.getComment());
        candidateShortlistItem.setAuditFields(user);

        return candidateShortlistRepository.save(candidateShortlistItem);

    }

}
