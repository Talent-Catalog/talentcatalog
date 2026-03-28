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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tctalent.server.model.db.CandidateAttachment;
import software.amazon.awssdk.services.cloudfront.CloudFrontUtilities;
import software.amazon.awssdk.services.cloudfront.model.CannedSignerRequest;
import software.amazon.awssdk.services.cloudfront.url.SignedUrl;

@Service
@RequiredArgsConstructor
public class DefaultFileUrlService implements FileUrlService {

    private final FileUrlProperties properties;

    private final CloudFrontUtilities cloudFrontUtilities = CloudFrontUtilities.create();

    @Override
    public String createApplicationUrl(CandidateAttachment attachment) {
        Long attachmentId = requireAttachmentId(attachment);
        String filename = sanitizeFilename(requireFilename(attachment));

        return joinUrl(properties.getPublicBaseUrl(),
            "files/" + attachmentId + "/" + filename);
    }

    @Override
    public String createObjectUrl(CandidateAttachment attachment) {
        String storageKey = requireStorageKey(attachment);
        return joinUrl(properties.getCloudFrontBaseUrl(), storageKey);
    }

    @Override
    public String createSignedObjectUrl(CandidateAttachment attachment, Duration duration)
        throws Exception {
        String objectUrl = createObjectUrl(attachment);
        Instant expiresAt = Instant.now().plus(duration);

        CannedSignerRequest request = CannedSignerRequest.builder()
            .resourceUrl(objectUrl)
            .privateKey(Paths.get(properties.getPrivateKeyPemPath()))
            .keyPairId(properties.getKeyPairId())
            .expirationDate(expiresAt)
            .build();

        SignedUrl signedUrl = cloudFrontUtilities.getSignedUrlWithCannedPolicy(request);
        return signedUrl.url();
    }

    @Override
    public FileAccessUrl createAccessUrl(CandidateAttachment attachment) throws Exception {
        requireStorageKey(attachment);

        if (!attachment.getUploadType().isSignedAccess()) {
            return FileAccessUrl.builder()
                .url(createObjectUrl(attachment))
                .signed(false)
                .expiresAt(null)
                .build();
        }

        Duration duration = Duration.ofMinutes(properties.getSignedUrlMinutes());
        Instant expiresAt = Instant.now().plus(duration);

        return FileAccessUrl.builder()
            .url(createSignedObjectUrl(attachment, duration))
            .signed(true)
            .expiresAt(expiresAt)
            .build();
    }

    private Long requireAttachmentId(CandidateAttachment attachment) {
        if (attachment.getId() == null) {
            throw new IllegalStateException("Attachment has no id");
        }
        return attachment.getId();
    }

    private String requireFilename(CandidateAttachment attachment) {
        String filename = attachment.getName();
        if (filename == null || filename.isBlank()) {
            throw new IllegalStateException("Attachment " + attachment.getId() + " has no filename");
        }
        return filename;
    }

    private String requireStorageKey(CandidateAttachment attachment) {
        String storageKey = attachment.getStorageKey();
        if (storageKey == null || storageKey.isBlank()) {
            throw new IllegalStateException("Attachment " + attachment.getId() + " has no storageKey");
        }
        return stripLeadingSlash(storageKey);
    }

    private String sanitizeFilename(String filename) {
        return filename
            .trim()
            .replace("\\", "_")
            .replace("/", "_");
    }

    private String joinUrl(String baseUrl, String path) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("Base URL is not configured");
        }

        String normalizedBase = stripTrailingSlash(baseUrl);
        String normalizedPath = stripLeadingSlash(path);

        return normalizedBase + "/" + normalizedPath;
    }

    private String stripLeadingSlash(String value) {
        return value.startsWith("/") ? value.substring(1) : value;
    }

    private String stripTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
