package org.tbbtalent.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.CandidateEducation;
import org.tbbtalent.server.model.CandidateReviewStatusItem;
import org.tbbtalent.server.model.SavedSearch;
import org.tbbtalent.server.model.User;
import org.tbbtalent.server.repository.CandidateRepository;
import org.tbbtalent.server.repository.CandidateReviewStatusRepository;
import org.tbbtalent.server.repository.SavedSearchRepository;
import org.tbbtalent.server.request.reviewstatus.CreateCandidateReviewStatusRequest;
import org.tbbtalent.server.request.reviewstatus.UpdateCandidateReviewStatusRequest;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.CandidateReviewStatusService;

@Service
public class CandidateReviewStatusServiceImpl implements CandidateReviewStatusService {

    private final CandidateRepository candidateRepository;
    private final CandidateReviewStatusRepository candidateReviewStatusRepository;
    private final SavedSearchRepository savedSearchRepository;
    private final UserContext userContext;

    @Autowired
    public CandidateReviewStatusServiceImpl(CandidateRepository candidateRepository, CandidateReviewStatusRepository candidateReviewStatusRepository, SavedSearchRepository savedSearchRepository, UserContext userContext) {
        this.candidateRepository = candidateRepository;
        this.candidateReviewStatusRepository = candidateReviewStatusRepository;
        this.savedSearchRepository = savedSearchRepository;
        this.userContext = userContext;
    }

    @Override
    public CandidateReviewStatusItem getCandidateReviewStatusItem(long id) {
        CandidateReviewStatusItem candidateReviewStatusItem = candidateReviewStatusRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(CandidateReviewStatusItem.class, id));
        return candidateReviewStatusItem;
    }

    @Override
    public CandidateReviewStatusItem createCandidateReviewStatusItem(CreateCandidateReviewStatusRequest request) {
        User user = userContext.getLoggedInUser();

        Candidate candidate = candidateRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new NoSuchObjectException(Candidate.class, request.getCandidateId()));

        SavedSearch savedSearch = savedSearchRepository.findById(request.getSavedSearchId())
                .orElseThrow(() -> new NoSuchObjectException(SavedSearch.class, request.getSavedSearchId()));
        CandidateReviewStatusItem candidateReviewStatusItem = new CandidateReviewStatusItem();
        candidateReviewStatusItem.setCandidate(candidate);
        candidateReviewStatusItem.setSavedSearch(savedSearch);
        candidateReviewStatusItem.setComment(request.getComment());
        candidateReviewStatusItem.setReviewStatus(request.getReviewStatus());
        candidateReviewStatusItem.setAuditFields(user);
        return candidateReviewStatusRepository.save(candidateReviewStatusItem);
    }

    @Override
    public CandidateReviewStatusItem updateCandidateReviewStatusItem(long id, UpdateCandidateReviewStatusRequest request) {
        CandidateReviewStatusItem candidateReviewStatusItem = this.candidateReviewStatusRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(CandidateEducation.class, id));

        User user = userContext.getLoggedInUser();

        candidateReviewStatusItem.setReviewStatus(request.getReviewStatus());
        candidateReviewStatusItem.setComment(request.getComment());
        candidateReviewStatusItem.setAuditFields(user);

        return candidateReviewStatusRepository.save(candidateReviewStatusItem);

    }

}
