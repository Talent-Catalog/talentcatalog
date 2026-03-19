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

import java.time.Duration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.model.db.CandidateAttachment;
import org.tctalent.server.model.db.UrlAccessType;
import org.tctalent.server.repository.db.CandidateAttachmentRepository;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
@Service
public class AttachmentAccessService {

    private final CandidateAttachmentRepository attachmentRepository;
    private final AttachmentAuthorizationService authorizationService;
    private final PublicFileUrlService publicFileUrlService;
    private final CloudFrontSignedUrlService signedUrlService;

    public AttachmentAccessService(
        CandidateAttachmentRepository attachmentRepository,
        AttachmentAuthorizationService authorizationService,
        PublicFileUrlService publicFileUrlService,
        CloudFrontSignedUrlService signedUrlService
    ) {
        this.attachmentRepository = attachmentRepository;
        this.authorizationService = authorizationService;
        this.publicFileUrlService = publicFileUrlService;
        this.signedUrlService = signedUrlService;
    }

    @Transactional(readOnly = true)
    public FileAccessUrl getAccessUrlForCurrentUser(Long attachmentId) {
        CandidateAttachment attachment = attachmentRepository.findById(attachmentId)
            .orElseThrow(() -> new AttachmentNotFoundException(attachmentId));

        if (!attachment.isActive()) {
            throw new AttachmentNotFoundException(attachmentId);
        }

        if (attachment.getUrlAccessType() == UrlAccessType.PUBLIC && attachment.isPubliclyReachable()) {
            return new FileAccessUrl(
                publicFileUrlService.toPublicUrl(attachment),
                false,
                null
            );
        }

        authorizationService.assertCurrentUserCanAccess(attachment);

        if (attachment.getUrlAccessType() == UrlAccessType.SIGNED) {
            return signedUrlService.createSignedUrl(attachment);
        }

        throw new UnsupportedOperationException("APP_ONLY access not implemented here");
    }

    @Transactional(readOnly = true)
    public FileAccessUrl getAccessUrlForCurrentUser(Long attachmentId, Duration signedDuration) {
        CandidateAttachment attachment = attachmentRepository.findById(attachmentId)
            .orElseThrow(() -> new AttachmentNotFoundException(attachmentId));

        if (!attachment.isActive()) {
            throw new AttachmentNotFoundException(attachmentId);
        }

        if (attachment.getUrlAccessType() == UrlAccessType.PUBLIC && attachment.isPubliclyReachable()) {
            return new FileAccessUrl(
                publicFileUrlService.toPublicUrl(attachment),
                false,
                null
            );
        }

        authorizationService.assertCurrentUserCanAccess(attachment);

        if (attachment.getUrlAccessType() == UrlAccessType.SIGNED) {
            return signedUrlService.createSignedUrl(attachment, signedDuration);
        }

        throw new UnsupportedOperationException("APP_ONLY access not implemented here");
    }
}
