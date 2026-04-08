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
package org.tctalent.server.storage;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.tctalent.server.exception.FileDownloadException;
import org.tctalent.server.exception.ServiceException;
import org.tctalent.server.service.db.aws.S3ResourceHelper;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

/**
 * One-off migration service for seeding a new GRN translations bucket from a legacy TBB S3 source.
 *
 * <p>This service bridges two distinct AWS identities:
 * <ul>
 *   <li>Source — TBB account (us-east-1), accessed via {@link S3ResourceHelper}</li>
 *   <li>Destination — GRN / OPC account (eu-west-2), accessed via {@link S3TranslationStorageService}</li>
 * </ul>
 *
 * <p>Because a server-side S3 CopyObject cannot cross account boundaries without explicit
 * bucket-policy grants, this service performs a client-side relay: download from source, upload to
 * destination.
 *
 * <p><strong>Remove this class once all target environments have been seeded.</strong>
 *
 * @author sadatmalik
 */
@Service
@RequiredArgsConstructor
public class TranslationMigrationService {

    private final S3ResourceHelper s3ResourceHelper;
    private final S3Client s3Client;

    public int migrateBucketContents(String sourceBucket, String sourcePrefix,
        String destinationBucket, String destinationPrefix) {

        String normalizedSourcePrefix = normalizePrefix(sourcePrefix);
        String normalizedDestinationPrefix = normalizePrefix(destinationPrefix);

        List<S3Object> objects = s3ResourceHelper.listObjectSummaries(sourceBucket,
            normalizedSourcePrefix);
        if (objects.isEmpty()) {
            throw new ServiceException("migration_empty",
                "No files found under " + normalizedSourcePrefix + " in " + sourceBucket);
        }

        int copiedCount = 0;
        for (S3Object obj : objects) {
            String sourceKey = obj.key();
            String relativePath = sourceKey.startsWith(normalizedSourcePrefix)
                ? sourceKey.substring(normalizedSourcePrefix.length())
                : sourceKey;
            String destinationKey = normalizedDestinationPrefix + relativePath;

            File downloaded = null;
            try {
                downloaded = s3ResourceHelper.downloadFile(sourceBucket, sourceKey);
                s3Client.putObject(
                    PutObjectRequest.builder()
                        .bucket(destinationBucket)
                        .key(destinationKey)
                        .contentLength(downloaded.length())
                        .build(),
                    RequestBody.fromFile(downloaded)
                );
                copiedCount++;
            } catch (FileDownloadException e) {
                throw e;
            } catch (Exception e) {
                throw new ServiceException("migration_failed",
                    "Failed to migrate " + sourceKey + " to " + destinationKey, e);
            } finally {
                if (downloaded != null) {
                    try {
                        Files.deleteIfExists(downloaded.toPath());
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        return copiedCount;
    }

    private String normalizePrefix(String prefix) {
        if (!StringUtils.hasText(prefix)) {
            return "";
        }
        return prefix.endsWith("/") ? prefix : prefix + "/";
    }
}
