/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.db;

import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateAttachment;
import org.tctalent.server.model.db.task.UploadType;
import org.tctalent.server.request.PagedSearchRequest;
import org.tctalent.server.request.attachment.CreateCandidateAttachmentRequest;
import org.tctalent.server.request.attachment.ListByUploadTypeRequest;
import org.tctalent.server.request.attachment.SearchCandidateAttachmentsRequest;
import org.tctalent.server.request.attachment.UpdateCandidateAttachmentRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface CandidateAttachmentService {

    Page<CandidateAttachment> searchCandidateAttachments(SearchCandidateAttachmentsRequest request);

    Page<CandidateAttachment> searchCandidateAttachmentsForLoggedInCandidate(
        PagedSearchRequest request);

    List<CandidateAttachment> listCandidateAttachmentsByType(ListByUploadTypeRequest request);

    List<CandidateAttachment> listCandidateAttachmentsForLoggedInCandidate();

    List<CandidateAttachment> listCandidateCvs(Long candidateId);

    List<CandidateAttachment> listCandidateAttachments(Long candidateId);

    CandidateAttachment createCandidateAttachment(CreateCandidateAttachmentRequest request);

    void deleteCandidateAttachment(Long id);

    /**
     * Downloads the given attachment, returning an InputStream from which the
     * attachment contents can be read.
     * <p/>
     * Note: Currently only works for Google attachments
     * @param id ID of requested attachment
     * @param out OutputStream which will receive attachment contents
     * @throws IOException if there is a problem retrieving the attachment.
     * @throws NoSuchObjectException if no attachment with that id exists
     */
    void downloadCandidateAttachment(Long id, OutputStream out)
            throws IOException, NoSuchObjectException;

    /**
     * Downloads the given attachment, returning an InputStream from which the
     * attachment contents can be read.
     * <p/>
     * Note: Currently only works for Google attachments
     * @param attachment CandidateAttachment providing details of requested
     *                   attachment
     * @param out OutputStream which will receive attachment contents
     * @throws IOException if there is a problem retrieving the attachment.
     */
    void downloadCandidateAttachment(
            CandidateAttachment attachment, OutputStream out) throws IOException;

    /**
     * Retrieves details on the given attachment.
     * @param id ID of requested attachment
     * @return CandidateAttachment containing link to the file in
     * {@link CandidateAttachment#getLocation()}
     * @throws NoSuchObjectException if no attachment with that id exists
     * @throws IOException if there is a problem retrieving the attachment.
     */
    CandidateAttachment getCandidateAttachment(Long id)
            throws IOException, NoSuchObjectException;

    CandidateAttachment updateCandidateAttachment(Long id,
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
     * @throws InvalidSessionException if logged in user is not a candidate
     * @throws IOException           if there is a problem uploading the file.
     */
    @NonNull
    CandidateAttachment uploadAttachment(Boolean cv, MultipartFile file)
            throws IOException, InvalidSessionException;

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

    /**
     * Uploads a file attachment for the given candidate to Google Drive.
     *
     * @param candidate Candidate associated with attachment
     * @param uploadedFileName Name that file will be uploaded as
     * @param subfolderName Optional subfolder to store attachment. Null if none.
     * @param file Uploaded file attachment
     * @param uploadType Type of attachment - eg CV
     * @return CandidateAttachment containing link to the file in
     * {@link CandidateAttachment#getLocation()}
     * @throws NoSuchObjectException if no candidate is found with that id
     * @throws IOException           if there is a problem uploading the file.
     */
    CandidateAttachment uploadAttachment(@NonNull Candidate candidate,
        String uploadedFileName, @Nullable String subfolderName, MultipartFile file,
        UploadType uploadType) throws IOException, NoSuchObjectException;

}
