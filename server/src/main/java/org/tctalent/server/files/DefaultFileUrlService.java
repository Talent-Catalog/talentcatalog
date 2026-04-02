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

import static software.amazon.awssdk.utils.http.SdkHttpUtils.urlEncode;

import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tctalent.server.configuration.properties.CandidateFileUrlsProperties;
import org.tctalent.server.model.db.CandidateAttachment;
import software.amazon.awssdk.services.cloudfront.CloudFrontUtilities;
import software.amazon.awssdk.services.cloudfront.model.CannedSignerRequest;
import software.amazon.awssdk.services.cloudfront.url.SignedUrl;

@Service
@RequiredArgsConstructor
public class DefaultFileUrlService implements FileUrlService {

    private final CloudFrontUtilities cloudFrontUtilities = CloudFrontUtilities.create();
    private final FileShareTokenService fileShareTokenService;
    private final CandidateFileUrlsProperties properties;

    @Override
    public String createApplicationUrl(CandidateAttachment attachment) {
        String publicId = requirePublicId(attachment);
        String filename = sanitizeFilename(requireFilename(attachment));

        return joinUrl(properties.getPublicBaseUrl(),
            "files/" + publicId + "/" + filename);
    }

    @Override
    public String createExpiringApplicationUrl(CandidateAttachment attachment, Duration duration) {
        String publicId = requirePublicId(attachment);
        String filename = sanitizeFilename(requireFilename(attachment));
        long expiresAt = Instant.now().plus(duration).getEpochSecond();
        String token = fileShareTokenService.createToken(publicId, filename, expiresAt);

        String baseUrl = createApplicationUrl(attachment);
        return baseUrl + "?e=" + expiresAt + "&t=" + token;
    }

    @Override
    public String createObjectUrl(CandidateAttachment attachment) {
        //Default to inline disposition type
        return createObjectUrl(attachment, true);
    }

    /**
     * Creates a URL with Content-Disposition header override by using the 
     * response-content-disposition query parameter.
     * <p>
     * See <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_GetObject.html">
     *   AWS S3 Doc on fetching objects</a>
     * <p>
     * Content-Disposition can also be used to associate the attachment file name with the 
     * attachment content.
     * <p>
     * See <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Disposition">
     *     Content-Disposition</a>.
     * @param attachment Attachment whose url is being generated
     * @param inlineDisposition true = inline, false = attachment
     * @return Generated url
     */
    private String createObjectUrl(CandidateAttachment attachment, boolean inlineDisposition) {

        String baseUrl = createObjectBaseUrl(attachment);
        
        String filename = sanitizeFilename(requireFilename(attachment));
        String dispositionType = inlineDisposition ? "inline" : "attachment";

        //Set the name of the file. This is useful if a browser user does a Save As.
        //Note that this supports file renaming without modifying the stored object.
        String dispositionValue = dispositionType + "; filename=\"" + filename + "\"";

        return baseUrl
            + "?response-content-disposition="
            + urlEncode(dispositionValue);
    }

    private String createObjectBaseUrl(CandidateAttachment attachment) {
        String storageKey = requireStorageKey(attachment);
        return joinUrl(properties.getOriginBaseUrl(), storageKey);
    }

    @Override
    public String createSignedObjectUrl(CandidateAttachment attachment, Duration duration)
        throws Exception {
        String objectUrl = createObjectUrl(attachment);
        Instant expiresAt = Instant.now().plus(duration);

        CannedSignerRequest request = CannedSignerRequest.builder()
            .resourceUrl(objectUrl)
            .privateKey(Paths.get(properties.getCloudfrontPrivateKeyPemPath()))
            .keyPairId(properties.getCloudfrontKeyPairId())
            .expirationDate(expiresAt)
            .build();

        SignedUrl signedUrl = cloudFrontUtilities.getSignedUrlWithCannedPolicy(request);
        return signedUrl.url();
    }

    @Override
    public FinalFileAccessUrl createAccessUrl(CandidateAttachment attachment) throws Exception {
        requireStorageKey(attachment);

        //TODO JC Remove this. ALL access urls should be signed so that they expire and also so
        //that query parameters are passed through to S3.
        if (!attachment.getUploadType().isSignedAccess()) {
            return FinalFileAccessUrl.builder()
                .url(createObjectUrl(attachment))
                .signed(false)
                .expiresAt(null)
                .build();
        }

        Duration duration = Duration.ofMinutes(properties.getOriginExpiryMinutes());
        Instant expiresAt = Instant.now().plus(duration);

        return FinalFileAccessUrl.builder()
            .url(createSignedObjectUrl(attachment, duration))
            .signed(true)
            .expiresAt(expiresAt)
            .build();
    }

    private String requirePublicId(CandidateAttachment attachment) {
        if (attachment.getPublicId() == null) {
            throw new IllegalStateException("Attachment has no public id");
        }
        return attachment.getPublicId();
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

    /**
     * Remove any folder structure with underscores
     */
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
