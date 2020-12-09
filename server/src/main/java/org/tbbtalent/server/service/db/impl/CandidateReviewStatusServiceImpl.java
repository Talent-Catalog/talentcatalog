package org.tbbtalent.server.service.db.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.CandidateEducation;
import org.tbbtalent.server.model.db.CandidateReviewStatusItem;
import org.tbbtalent.server.model.db.SavedSearch;
import org.tbbtalent.server.repository.db.CandidateRepository;
import org.tbbtalent.server.repository.db.CandidateReviewStatusRepository;
import org.tbbtalent.server.repository.db.SavedSearchRepository;
import org.tbbtalent.server.request.reviewstatus.CreateCandidateReviewStatusRequest;
import org.tbbtalent.server.request.reviewstatus.UpdateCandidateReviewStatusRequest;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.db.CandidateReviewStatusService;

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
        Candidate candidate = candidateRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new NoSuchObjectException(Candidate.class, request.getCandidateId()));

        SavedSearch savedSearch = savedSearchRepository.findById(request.getSavedSearchId())
                .orElseThrow(() -> new NoSuchObjectException(SavedSearch.class, request.getSavedSearchId()));
        CandidateReviewStatusItem candidateReviewStatusItem = new CandidateReviewStatusItem();
        candidateReviewStatusItem.setCandidate(candidate);
        candidateReviewStatusItem.setSavedSearch(savedSearch);
        candidateReviewStatusItem.setComment(request.getComment());
        candidateReviewStatusItem.setReviewStatus(request.getReviewStatus());
        candidateReviewStatusItem.setAuditFields(userContext.getLoggedInUser().orElse(null));
        return candidateReviewStatusRepository.save(candidateReviewStatusItem);
    }

    @Override
    public CandidateReviewStatusItem updateCandidateReviewStatusItem(long id, UpdateCandidateReviewStatusRequest request) {
        CandidateReviewStatusItem candidateReviewStatusItem = this.candidateReviewStatusRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(CandidateEducation.class, id));

        candidateReviewStatusItem.setReviewStatus(request.getReviewStatus());
        candidateReviewStatusItem.setComment(request.getComment());
        candidateReviewStatusItem.setAuditFields(userContext.getLoggedInUser().orElse(null));

        return candidateReviewStatusRepository.save(candidateReviewStatusItem);

    }

}
