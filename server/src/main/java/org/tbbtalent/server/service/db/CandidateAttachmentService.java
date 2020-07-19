/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.db;

import java.util.List;

import org.springframework.data.domain.Page;
import org.tbbtalent.server.model.db.CandidateAttachment;
import org.tbbtalent.server.request.PagedSearchRequest;
import org.tbbtalent.server.request.attachment.CreateCandidateAttachmentRequest;
import org.tbbtalent.server.request.attachment.SearchCandidateAttachmentsRequest;
import org.tbbtalent.server.request.attachment.UpdateCandidateAttachmentRequest;

public interface CandidateAttachmentService {

    Page<CandidateAttachment> searchCandidateAttachments(SearchCandidateAttachmentsRequest request);

    Page<CandidateAttachment> searchCandidateAttachmentsForLoggedInCandidate(PagedSearchRequest request);

    List<CandidateAttachment> listCandidateAttachmentsForLoggedInCandidate();

    CandidateAttachment createCandidateAttachment(CreateCandidateAttachmentRequest request, Boolean adminOnly);

    void deleteCandidateAttachment(Long id);

    CandidateAttachment updateCandidateAttachment(UpdateCandidateAttachmentRequest request);
}
