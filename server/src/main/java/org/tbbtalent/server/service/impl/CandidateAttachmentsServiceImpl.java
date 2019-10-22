package org.tbbtalent.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.*;
import org.tbbtalent.server.repository.CandidateAttachmentRepository;
import org.tbbtalent.server.repository.CandidateNoteRepository;
import org.tbbtalent.server.repository.CandidateRepository;
import org.tbbtalent.server.request.attachment.SearchCandidateAttachmentsRequest;
import org.tbbtalent.server.request.note.CreateCandidateNoteRequest;
import org.tbbtalent.server.request.note.SearchCandidateNotesRequest;
import org.tbbtalent.server.request.note.UpdateCandidateNoteRequest;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.CandidateAttachmentService;
import org.tbbtalent.server.service.CandidateNoteService;

@Service
public class CandidateAttachmentsServiceImpl implements CandidateAttachmentService {

    private final CandidateRepository candidateRepository;
    private final CandidateAttachmentRepository candidateAttachmentRepository;
    private final UserContext userContext;

    @Autowired
    public CandidateAttachmentsServiceImpl(CandidateRepository candidateRepository, CandidateAttachmentRepository candidateAttachmentRepository,
                                           UserContext userContext) {
        this.candidateRepository = candidateRepository;
        this.candidateAttachmentRepository = candidateAttachmentRepository;
        this.userContext = userContext;
    }

    @Override
    public Page<CandidateAttachment> searchCandidateAttachments(SearchCandidateAttachmentsRequest request) {
        return candidateAttachmentRepository.findByCandidateId(request.getCandidateId(), request.getPageRequest());
    }



}
