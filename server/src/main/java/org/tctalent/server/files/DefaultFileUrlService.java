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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tctalent.server.model.db.CandidateAttachment;
import org.tctalent.server.model.db.UrlAccessType;

@Service
@RequiredArgsConstructor
public class DefaultFileUrlService implements FileUrlService {

    private final PublicFileUrlService publicFileUrlService;
    private final CloudFrontSignedUrlService signedUrlService;
    private final AttachmentAuthorizationService authorizationService;
    private final FileUrlProperties properties;

    @Override
    public FileAccessUrl createAccessUrl(CandidateAttachment attachment) {

        if (!attachment.isActive()) {
            throw new AttachmentNotFoundException(attachment.getId());
        }

        // PUBLIC case (no auth required beyond published flag)
        if (attachment.getUrlAccessType() == UrlAccessType.PUBLIC
            && attachment.isPubliclyReachable()) {

            FileAccessUrl result = new FileAccessUrl();
            result.setUrl(publicFileUrlService.toPublicUrl(attachment));
            result.setSigned(false);
            result.setExpiresAt(null);

            return result;
        }

        // Everything else requires authorization
        authorizationService.assertCurrentUserCanAccess(attachment);

        // SIGNED case
        if (attachment.getUrlAccessType() == UrlAccessType.SIGNED) {
            return signedUrlService.createSignedUrl(
                attachment,
                Duration.ofMinutes(properties.getSignedUrlMinutes())
            );
        }

        // APP_ONLY fallback (optional)
        throw new UnsupportedOperationException(
            "APP_ONLY access type not implemented for direct URL"
        );
    }

    @Override
    public String createPublicUrl(CandidateAttachment attachment) {
        return publicFileUrlService.toPublicUrl(attachment);
    }

    @Override
    public String createSignedUrl(CandidateAttachment attachment, Duration duration) {
        return signedUrlService.createSignedUrl(attachment, duration).getUrl();
    }
}
