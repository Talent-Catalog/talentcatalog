/*
 * Copyright (c) 2026 Talent Catalog.
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
package org.tctalent.server.files;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.CandidateAttachment;
import org.tctalent.server.service.db.CandidateAttachmentService;

@Service
@RequiredArgsConstructor
public class AttachmentAccessService {

    private final CandidateAttachmentService candidateAttachmentService;
    private final AttachmentAuthorizationService attachmentAuthorizationService;
    private final FileUrlService fileUrlService;
    private final FileShareTokenService fileShareTokenService;

    @Transactional(readOnly = true)
    public FinalFileAccessUrl resolveAccessUrl(
        String publicAttachmentId,
        String requestedFilename,
        Long expiresAtEpochSeconds,
        String token) throws Exception {

        CandidateAttachment attachment = loadActiveAttachment(publicAttachmentId);

        validateRequestedFilenameIfPresent(attachment, requestedFilename);

        // 1. Public file (signed access not required): no login, no token required
        if (!attachment.getUploadType().isSignedAccess()) {
            return fileUrlService.createAccessUrl(attachment);
        }

        // 2. Share link: no login required, but valid token required
        if (expiresAtEpochSeconds != null && token != null) {
            fileShareTokenService.validateToken(
                publicAttachmentId,
                attachment.getName(),
                expiresAtEpochSeconds,
                token);

            return fileUrlService.createAccessUrl(attachment);
        }

        // 3. Logged-in user access
        attachmentAuthorizationService.assertCurrentUserCanAccess(attachment);

        return fileUrlService.createAccessUrl(attachment);
    }

    private CandidateAttachment loadActiveAttachment(String publicAttachmentId) throws IOException {
        CandidateAttachment attachment = 
            candidateAttachmentService.getCandidateAttachmentByPublicId(publicAttachmentId);

        if (!attachment.isActive()) {
            throw new NoSuchObjectException(CandidateAttachment.class, publicAttachmentId);
        }

        return attachment;
    }

    private void validateRequestedFilenameIfPresent(
        CandidateAttachment attachment,
        String requestedFilename) {

        if (requestedFilename == null || requestedFilename.isBlank()) {
            return;
        }

        String expectedFilename = attachment.getName();
        if (expectedFilename != null && !expectedFilename.equals(requestedFilename)) {
            throw new NoSuchObjectException(CandidateAttachment.class, attachment.getId());
        }
    }
}
