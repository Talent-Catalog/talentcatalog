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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tctalent.server.files.StoredFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CopyObjectResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@Service
@RequiredArgsConstructor
public class S3StorageService implements StorageService {

    private final S3Client s3Client;
    private final StorageProperties properties;
    private final StorageKeyService storageKeyService;

    @Override
    public StoredFileInfo store(StoragePutRequest request) {
        String key = storageKeyService.newStorageKey();
        return store(key, request);
    }

    private StoredFileInfo store(String storageKey, StoragePutRequest request) {

        File tempFile = null;

        try {
            // 1. Stream to temp file while computing SHA-256
            tempFile = File.createTempFile("s3-upload-", ".tmp");

            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            try (
                InputStream in = new BufferedInputStream(request.getInputStream());
                DigestInputStream dis = new DigestInputStream(in, digest);
                OutputStream out = new BufferedOutputStream(new FileOutputStream(tempFile))
            ) {
                dis.transferTo(out);
            }

            long contentLength = tempFile.length();

            String sha256 = HexFormat.of().formatHex(digest.digest());

            // 2. Build PutObject request
            PutObjectRequest.Builder put = PutObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(storageKey)
                .contentLength(contentLength);

            if (request.getContentType() != null) {
                put.contentType(request.getContentType());
            }

            // 3. Upload from file (streamed by SDK)
            PutObjectResponse response = s3Client.putObject(
                put.build(),
                RequestBody.fromFile(tempFile)
            );

            // 4. Build result
            return StoredFileInfo.builder()
                .active(true)
                .storageKey(storageKey)
                .bucket(properties.getBucket())
                .fileType(request.getContentType())
                .contentLength(contentLength)
                .sha256Hex(sha256)
                .build();

        } catch (Exception e) {
            throw new StorageException("Failed to upload to S3", e);
        } finally {
            if (tempFile != null && tempFile.exists()) {
                try {
                    Files.deleteIfExists(tempFile.toPath());
                } catch (IOException ignored) {
                }
            }
        }
    }

    @Override
    public InputStream openStream(String storageKey) {
        try {
            return s3Client.getObject(GetObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(storageKey)
                .build());
        } catch (NoSuchKeyException e) {
            throw new StorageException("Object not found: " + storageKey, e);
        }
    }

    @Override
    public void delete(String storageKey) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
            .bucket(properties.getBucket())
            .key(storageKey)
            .build());
    }

    @Override
    public boolean exists(String storageKey) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(storageKey)
                .build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    //TODO JC For now we could leave out copy and move

    @Override
    public StoredFile copy(String sourceKey, String targetKey) {
        CopyObjectResponse response = s3Client.copyObject(CopyObjectRequest.builder()
            .sourceBucket(properties.getBucket())
            .sourceKey(sourceKey)
            .destinationBucket(properties.getBucket())
            .destinationKey(targetKey)
            .build());

        //TODO JC Use Mapper to map response
        return StoredFileInfo.builder()
            .storageKey(targetKey)
            .bucket(properties.getBucket())
            .build();
    }

    @Override
    public StoredFile move(String sourceKey, String targetKey) {
        StoredFile result = copy(sourceKey, targetKey);
        delete(sourceKey);
        return result;
    }
}
