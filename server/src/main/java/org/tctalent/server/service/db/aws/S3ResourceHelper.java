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

package org.tctalent.server.service.db.aws;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.exception.FileDownloadException;
import org.tctalent.server.logging.LogBuilder;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.*;

@Slf4j
public class S3ResourceHelper {

    private final S3Client s3Client;

    @Value("${aws.s3.bucketName}")
    private String s3Bucket;

    public S3ResourceHelper(
        @Value("${aws.credentials.accessKey}") String accessKey,
        @Value("${aws.credentials.secretKey}") String secretKey,
        @Value("${aws.s3.region}") String s3Region
    ) {
        S3ClientBuilder builder = S3Client.builder()
            .region(Region.of(s3Region));

        if (StringUtils.hasText(accessKey) && StringUtils.hasText(secretKey)) {
            builder.credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)
                )
            );
        } else {
            builder.credentialsProvider(AnonymousCredentialsProvider.create());
        }

        this.s3Client = builder.build();
    }

    public String getS3Bucket() {
        return s3Bucket;
    }

    // ===================== DOWNLOAD =====================

    public File downloadFile(String key) throws FileDownloadException {
        return downloadFile(s3Bucket, key);
    }

    public File downloadFile(String bucket, String key) throws FileDownloadException {
        try {
            File file = getTmpFile();

            try (ResponseInputStream<GetObjectResponse> in = s3Client.getObject(
                GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build()
            )) {
                Files.copy(in, file.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }

            LogBuilder.builder(log)
                .action("DownloadFile")
                .message("Downloaded key " + key + " to " + file.getAbsolutePath())
                .logDebug();

            return file;

        } catch (Exception e) {
            LogBuilder.builder(log)
                .action("DownloadFile")
                .message("Error downloading file: " + key)
                .logError(e);

            throw new FileDownloadException("Error downloading file from: " + key, e);
        }
    }

    public Resource downloadResource(String key) throws FileDownloadException {
        return new FileSystemResource(downloadFile(key));
    }

    public Resource downloadResource(String key, File targetLocation) throws FileDownloadException {
        try {
            try (ResponseInputStream<GetObjectResponse> in = s3Client.getObject(
                GetObjectRequest.builder()
                    .bucket(s3Bucket)
                    .key(key)
                    .build()
            )) {
                Files.copy(in, targetLocation.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }

            return new FileSystemResource(targetLocation);

        } catch (Exception e) {
            throw new FileDownloadException("Error downloading file: " + key, e);
        }
    }

    // ===================== UPLOAD =====================

    public void uploadFile(File file, String key, String contentType) throws FileUploadException {
        uploadFile(s3Bucket, file, key, contentType);
    }

    public void uploadFile(String bucket, File file, String key, String contentType)
        throws FileUploadException {
        try {
            PutObjectRequest.Builder put = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key);

            if (contentType != null) {
                put.contentType(contentType);
            }

            s3Client.putObject(put.build(), RequestBody.fromFile(file));

            LogBuilder.builder(log)
                .action("UploadFile")
                .message("Uploaded file " + file.getAbsolutePath() + " to " + key)
                .logDebug();

        } catch (Exception e) {
            throw new FileUploadException("Error uploading file to: " + key, e);
        }
    }

    public void uploadFile(String key, MultipartFile source) throws FileUploadException {
        uploadFile(s3Bucket, key, source);
    }

    public void uploadFile(String bucket, String key, MultipartFile source)
        throws FileUploadException {
        try {
            PutObjectRequest.Builder put = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentLength(source.getSize());

            if (source.getContentType() != null) {
                put.contentType(source.getContentType());
            }

            s3Client.putObject(
                put.build(),
                RequestBody.fromInputStream(source.getInputStream(), source.getSize())
            );

        } catch (Exception e) {
            throw new FileUploadException("Error uploading file to: " + key, e);
        }
    }

    public void uploadFile(String content, String key, String contentType)
        throws FileUploadException {
        uploadFile(s3Bucket, content, key, contentType);
    }

    public void uploadFile(String bucket, String content, String key, String contentType)
        throws FileUploadException {
        try {
            byte[] bytes = content.getBytes("UTF-8");

            PutObjectRequest.Builder put = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentLength((long) bytes.length);

            if (contentType != null) {
                put.contentType(contentType);
            }

            s3Client.putObject(
                put.build(),
                RequestBody.fromBytes(bytes)
            );

        } catch (Exception e) {
            throw new FileUploadException("Error uploading string content to: " + key, e);
        }
    }

    public void uploadFile(String bucket, InputStream content, long contentLength,
        String key, String contentType)
        throws FileUploadException {
        try {
            PutObjectRequest.Builder put = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentLength(contentLength);

            if (contentType != null) {
                put.contentType(contentType);
            }

            s3Client.putObject(
                put.build(),
                RequestBody.fromInputStream(content, contentLength)
            );

        } catch (Exception e) {
            throw new FileUploadException("Error uploading content to: " + key, e);
        }
    }

    // ===================== DELETE =====================

    public void deleteFile(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
            .bucket(s3Bucket)
            .key(key)
            .build());
    }

    // ===================== COPY =====================

    public void copyObject(String sourceKey, String destinationKey) {
        copyObject(s3Bucket, sourceKey, s3Bucket, destinationKey);
    }

    public void copyObject(String sourceBucket, String sourceKey,
        String destinationBucket, String destinationKey) {

        s3Client.copyObject(CopyObjectRequest.builder()
            .sourceBucket(sourceBucket != null ? sourceBucket : s3Bucket)
            .sourceKey(sourceKey)
            .destinationBucket(destinationBucket != null ? destinationBucket : s3Bucket)
            .destinationKey(destinationKey)
            .build());
    }

    public void copyBucketContents(String sourceBucket, String sourcePrefix,
        String destinationBucket, String destinationPrefix) {

        ListObjectsV2Request request = ListObjectsV2Request.builder()
            .bucket(sourceBucket)
            .prefix(sourcePrefix)
            .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(request);

        if (response.contents().isEmpty()) {
            throw S3Exception.builder()
                .message("No files found under " + sourcePrefix)
                .build();
        }

        for (S3Object obj : response.contents()) {
            String newKey = destinationPrefix + obj.key().replace(sourcePrefix, "");

            s3Client.copyObject(CopyObjectRequest.builder()
                .sourceBucket(sourceBucket)
                .sourceKey(obj.key())
                .destinationBucket(destinationBucket)
                .destinationKey(newKey)
                .build());
        }
    }

    // ===================== LIST =====================

    public List<S3Object> getObjectSummaries() {
        List<S3Object> results = new ArrayList<>();

        String token = null;

        do {
            ListObjectsV2Response response = s3Client.listObjectsV2(
                ListObjectsV2Request.builder()
                    .bucket(s3Bucket)
                    .prefix("candidate/")
                    .continuationToken(token)
                    .build()
            );

            results.addAll(response.contents());
            token = response.nextContinuationToken();

        } while (token != null);

        return results;
    }

    public List<S3Object> listObjectSummaries(String bucket, String prefix) {
        List<S3Object> results = new ArrayList<>();

        String token = null;
        do {
            ListObjectsV2Response response = s3Client.listObjectsV2(
                ListObjectsV2Request.builder()
                    .bucket(bucket)
                    .prefix(prefix)
                    .continuationToken(token)
                    .build()
            );

            results.addAll(response.contents());
            token = response.nextContinuationToken();
        } while (token != null);

        return results;
    }

    public List<S3Object> filterMigratedObjects(List<S3Object> objects) {
        return objects.stream()
            .filter(o -> !o.key().contains("/migrated/"))
            .collect(Collectors.toList());
    }

    // ===================== UTIL =====================

    public File getTmpFile() throws IOException {
        File tmp = File.createTempFile("lola-", ".tmp");
        tmp.deleteOnExit();
        return tmp;
    }

    public boolean doesBucketExist(String bucket) {
        return s3Client.headBucket(HeadBucketRequest.builder()
            .bucket(bucket)
            .build()) != null;
    }
}
