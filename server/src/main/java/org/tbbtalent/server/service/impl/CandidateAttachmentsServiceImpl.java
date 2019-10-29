package org.tbbtalent.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.CandidateAttachment;
import org.tbbtalent.server.repository.CandidateAttachmentRepository;
import org.tbbtalent.server.repository.CandidateRepository;
import org.tbbtalent.server.request.SearchRequest;
import org.tbbtalent.server.request.attachment.SearchCandidateAttachmentsRequest;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.CandidateAttachmentService;

@Service
public class CandidateAttachmentsServiceImpl implements CandidateAttachmentService {

    private final CandidateRepository candidateRepository;
    private final CandidateAttachmentRepository candidateAttachmentRepository;
    private final UserContext userContext;

    @Autowired
    public CandidateAttachmentsServiceImpl(CandidateRepository candidateRepository,
                                           CandidateAttachmentRepository candidateAttachmentRepository,
                                           UserContext userContext) {
        this.candidateRepository = candidateRepository;
        this.candidateAttachmentRepository = candidateAttachmentRepository;
        this.userContext = userContext;
    }

    @Override
    public Page<CandidateAttachment> searchCandidateAttachments(SearchCandidateAttachmentsRequest request) {
        return candidateAttachmentRepository.findByCandidateId(request.getCandidateId(), request.getPageRequest());
    }

    @Override
    public Page<CandidateAttachment> searchCandidateAttachmentsForLoggedInCandidate(SearchRequest request) {
        Candidate candidate = userContext.getLoggedInCandidate();
        return candidateAttachmentRepository.findByCandidateId(candidate.getId(), request.getPageRequest());
    }

}
