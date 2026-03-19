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

import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.tctalent.server.model.db.CandidateAttachment;
import software.amazon.awssdk.services.cloudfront.CloudFrontUtilities;
import software.amazon.awssdk.services.cloudfront.model.CannedSignerRequest;
import software.amazon.awssdk.services.cloudfront.url.SignedUrl;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
@Service
public class CloudFrontSignedUrlService {

    private final FileUrlProperties properties;
    private final PublicFileUrlService publicFileUrlService;
    private final CloudFrontUtilities cloudFrontUtilities = CloudFrontUtilities.create();

    public CloudFrontSignedUrlService(
        FileUrlProperties properties,
        PublicFileUrlService publicFileUrlService
    ) {
        this.properties = properties;
        this.publicFileUrlService = publicFileUrlService;
    }

    public FileAccessUrl createSignedUrl(CandidateAttachment attachment) {
        return createSignedUrl(attachment, Duration.ofMinutes(properties.getSignedUrlMinutes()));
    }

    public FileAccessUrl createSignedUrl(CandidateAttachment attachment, Duration duration) {
        if (attachment.getPublicPath() == null || attachment.getPublicPath().isBlank()) {
            throw new IllegalStateException("Attachment cannot be signed without publicPath");
        }

        Instant expiresAt = Instant.now().plus(duration);
        String resourceUrl = publicFileUrlService.toCloudFrontResourceUrl(attachment);

        CannedSignerRequest request = CannedSignerRequest.builder()
            .resourceUrl(resourceUrl)
            .privateKey(Paths.get(properties.getPrivateKeyPemPath()))
            .keyPairId(properties.getKeyPairId())
            .expirationDate(expiresAt)
            .build();

        SignedUrl signedUrl = cloudFrontUtilities.getSignedUrlWithCannedPolicy(request);

        return new FileAccessUrl(
            signedUrl.url(),
            true,
            expiresAt
        );
    }
}
