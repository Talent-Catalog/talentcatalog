/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.db;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;
import org.tbbtalent.server.exception.NoSuchObjectException;
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

    CandidateAttachment updateCandidateAttachment(
            UpdateCandidateAttachmentRequest request) throws IOException;

    /**
     * Uploads a file attachment for the currently logged in candidate to 
     * Google Drive.
     * <p/>
     * Called from the candidate portal.
     * 
     * @param cv True if this attachment is a CV
     * @param file Uploaded file attachment
     * @return CandidateAttachment containing link to the file in 
     * {@link CandidateAttachment#getLocation()} 
     * @throws NoSuchObjectException if logged in user is not a candidate
     * @throws IOException           if there is a problem uploading the file.
     */
    @NonNull 
    CandidateAttachment uploadAttachment(Boolean cv, MultipartFile file) 
            throws IOException, NoSuchObjectException;

    /**
     * Uploads a file attachment for the given candidate to Google Drive.
     * <p/>
     * Called from the admin portal.
     * 
     * @param candidateId Id of candidate associated with attachment
     * @param cv True if this attachment is a CV
     * @param file Uploaded file attachment
     * @return CandidateAttachment containing link to the file in 
     * {@link CandidateAttachment#getLocation()} 
     * @throws NoSuchObjectException if no candidate is found with that id
     * @throws IOException           if there is a problem uploading the file.
     */
    @NonNull 
    CandidateAttachment uploadAttachment(
            @NonNull Long candidateId, Boolean cv, MultipartFile file) 
            throws IOException, NoSuchObjectException;
}
