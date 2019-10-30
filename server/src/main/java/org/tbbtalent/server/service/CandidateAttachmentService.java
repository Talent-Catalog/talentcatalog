package org.tbbtalent.server.service;

import org.springframework.data.domain.Page;
import org.tbbtalent.server.model.CandidateAttachment;
import org.tbbtalent.server.request.SearchRequest;
import org.tbbtalent.server.request.attachment.CreateCandidateAttachmentRequest;
import org.tbbtalent.server.request.attachment.SearchCandidateAttachmentsRequest;

import java.util.List;

public interface CandidateAttachmentService {

    Page<CandidateAttachment> searchCandidateAttachments(SearchCandidateAttachmentsRequest request);

    Page<CandidateAttachment> searchCandidateAttachmentsForLoggedInCandidate(SearchRequest request);

    List<CandidateAttachment> listCandidateAttachmentsForLoggedInCandidate();

    CandidateAttachment createCandidateAttachment(CreateCandidateAttachmentRequest request, Boolean adminOnly);

    void deleteCandidateAttachment(Long id);
}
