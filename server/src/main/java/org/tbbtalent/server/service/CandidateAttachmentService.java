package org.tbbtalent.server.service;

import org.springframework.data.domain.Page;
import org.tbbtalent.server.model.CandidateAttachment;
import org.tbbtalent.server.request.SearchRequest;
import org.tbbtalent.server.request.attachment.SearchCandidateAttachmentsRequest;

public interface CandidateAttachmentService {

    Page<CandidateAttachment> searchCandidateAttachments(SearchCandidateAttachmentsRequest request);

    Page<CandidateAttachment> searchCandidateAttachmentsForLoggedInCandidate(SearchRequest request);

}
