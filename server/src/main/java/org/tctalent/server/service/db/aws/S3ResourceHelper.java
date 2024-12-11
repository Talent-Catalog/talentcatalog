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

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.internal.Mimetypes;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.TransferManagerConfiguration;
import com.amazonaws.services.s3.transfer.Upload;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.exception.FileDownloadException;
import org.tctalent.server.logging.LogBuilder;

@Slf4j
public class S3ResourceHelper {
    public static long FILE_PART_SIZE = 5 * 1024 * 1024; // 5MB

    private AmazonS3 amazonS3;

    @Value("${aws.s3.bucketName}")
    private String s3Bucket;

    private TransferManager transferManager;

    @Autowired
    public S3ResourceHelper(@Value("${aws.credentials.accessKey}") String accessKey,
                            @Value("${aws.credentials.secretKey}") String secretKey,
                            @Value("${aws.s3.region}") String s3Region) {
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        amazonS3 = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(s3Region).build();
        transferManager = TransferManagerBuilder.standard().withS3Client(amazonS3).build();
    }

    public String getS3Bucket() {
        return s3Bucket;
    }

    public File downloadFile(String key) throws FileDownloadException {
        return downloadFile(s3Bucket, key);
    }

    public File downloadFile(String bucket,
                             String key)
            throws FileDownloadException {
        File downloadFile = null;
        try {
            downloadFile = getTmpFile();
            Download download = transferManager.download(bucket, key, downloadFile);
            // blocking wait
            download.waitForCompletion();

            LogBuilder.builder(log)
                .action("DownloadFile")
                .message("Downloaded key " + key + " to " + downloadFile.getAbsolutePath())
                .logDebug();

        } catch (Exception e) {
            LogBuilder.builder(log)
                .action("DownloadFile")
                .message("Error downloading file to: " + key)
                .logError(e);

            throw new FileDownloadException("Error downloading file from: " + key, e);
        }
        return downloadFile;
    }

    public Resource downloadResource(String key) throws FileDownloadException {
        Resource resource = null;
        try {
            File downloadFile = getTmpFile();
            Download download = transferManager.download(s3Bucket, key, downloadFile);
            // blocking wait
            download.waitForCompletion();

            LogBuilder.builder(log)
                .action("DownloadResource")
                .message("Downloaded key " + key + " to " + downloadFile.getAbsolutePath())
                .logDebug();

            // convert to resource
            resource = new FileSystemResource(downloadFile);
        } catch (Exception e) {
            LogBuilder.builder(log)
                .action("DownloadResource")
                .message("Error downloading file to: " + key)
                .logError(e);

            throw new FileDownloadException("Error downloading file from: " + key, e);
        }
        return resource;
    }

    public Resource downloadResource(String key,
                                     File targetLocation)
            throws FileDownloadException {
        Resource resource = null;
        try {
            File downloadFile = targetLocation;
            Download download = transferManager.download(s3Bucket, key, downloadFile);
            // blocking wait
            download.waitForCompletion();

            LogBuilder.builder(log)
                .action("DownloadResource")
                .message("Downloaded key " + key + " to " + downloadFile.getAbsolutePath())
                .logDebug();

            // convert to resource
            resource = new FileSystemResource(downloadFile);
        } catch (Exception e) {
            LogBuilder.builder(log)
                .action("DownloadResource")
                .message("Error downloading file to: " + key)
                .logError(e);

            throw new FileDownloadException("Error downloading file from: " + key, e);
        }
        return resource;
    }

    public void uploadFile(File file,
                           String key,
                           String contentType)
            throws FileUploadException {
        uploadFile(s3Bucket, file, key, contentType);
    }

    public void uploadFile(String bucket,
                           File file,
                           String key,
                           String contentType)
            throws FileUploadException {
        try {
            Upload upload = transferManager.upload(bucket, key, file);
            // blocking wait, though we could let uploads just run in background
            upload.waitForCompletion();

            LogBuilder.builder(log)
                .action("UploadFile")
                .message("Uploaded file " + file.getAbsolutePath() + " to " + key)
                .logDebug();

        } catch (Exception e) {
            LogBuilder.builder(log)
                .action("UploadFile")
                .message("Error uploading file to: " + key)
                .logError(e);

            throw new FileUploadException("Error uploading file to: " + key, e);
        }
    }

    public void uploadFile(String key,
                           MultipartFile source)
            throws FileUploadException {
        try {
            uploadFile(s3Bucket, key, source);

        } catch (Exception e) {
            LogBuilder.builder(log)
                .action("UploadFile")
                .message("Error uploading file to: " + key)
                .logError(e);

            throw new FileUploadException("Error uploading file to: " + key, e);
        }
    }

    public void uploadFile(String bucket,
                           String key,
                           MultipartFile source)
            throws FileUploadException {
        try {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(source.getSize());
            objectMetadata.setContentType(source.getContentType());
            Upload upload = transferManager.upload(bucket, key, source.getInputStream(), objectMetadata);

            TransferManagerConfiguration configuration = transferManager.getConfiguration();
            configuration.setMultipartUploadThreshold(FILE_PART_SIZE);

            // blocking wait, though we could let uploads just run in background
            upload.waitForCompletion();

            LogBuilder.builder(log)
                .action("UploadFile")
                .message("Uploaded MultipartFile to: " + key)
                .logDebug();

        } catch (Exception e) {
            LogBuilder.builder(log)
                .action("UploadFile")
                .message("Error uploading file to: " + key)
                .logError(e);

            throw new FileUploadException("Error uploading file to: " + key, e);
        }
    }

    public void uploadFile(String content,
                           String key,
                           String contentType)
            throws FileUploadException {
        try {
            uploadFile(s3Bucket, content, key, contentType);

        } catch (Exception e) {
            LogBuilder.builder(log)
                .action("UploadFile")
                .message("Error uploading file to: " + key)
                .logError(e);

            throw new FileUploadException("Error uploading file to: " + key, e);
        }
    }

    public void uploadFile(String bucket,
                           String content,
                           String key,
                           String contentType)
            throws FileUploadException {
        try {
            byte[] bytes = content.getBytes("UTF-8");
            InputStream is = new ByteArrayInputStream(bytes);
            uploadFile(bucket, is, bytes.length, key, contentType);
        } catch (FileUploadException e) {
            throw e;
        } catch (Exception e) {
            throw new FileUploadException("Error uploading string content to: " + key, e);
        }
    }

    public void uploadFile(String bucket,
                           InputStream content,
                           long contentLength,
                           String key,
                           String contentType)
            throws FileUploadException {
        try {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(contentLength);
            objectMetadata.setContentType(contentType);
            Upload upload = transferManager.upload(bucket, key, content, objectMetadata);
            // blocking wait, though we could let uploads just run in background
            upload.waitForCompletion();

            LogBuilder.builder(log)
                .action("UploadFile")
                .message("Uploaded string content to: " + key)
                .logDebug();

        } catch (Exception e) {
            LogBuilder.builder(log)
                .action("UploadFile")
                .message("Error uploading file to: " + key)
                .logError(e);

            throw new FileUploadException("Error uploading string content to: " + key, e);
        }
    }

    public void deleteFile(String key) {
        amazonS3.deleteObject(s3Bucket, key);
    }

    public void copyObject(String sourceKey,
                           String destinationKey) {
        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(s3Bucket, sourceKey, s3Bucket, destinationKey);
        transferManager.copy(copyObjectRequest);
    }

    public void copyObject(String sourceBucket,
                           String sourceKey,
                           String destinationBucket,
                           String destinationKey) {

        sourceBucket = sourceBucket != null ? sourceBucket : s3Bucket;
        destinationBucket = destinationBucket != null ? destinationBucket : s3Bucket;
        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(sourceBucket, sourceKey, destinationBucket,
                                                                    destinationKey);
        transferManager.copy(copyObjectRequest);
    }

    public void copyBucketContents(String sourceBucket,
                                   String sourcePrefix,
                                   String destinationBucket,
                                   String destinationPrefix)
            throws AmazonS3Exception {
        ListObjectsRequest req = new ListObjectsRequest();
        req.setBucketName(sourceBucket);
        req.withPrefix(sourcePrefix);
        List<S3ObjectSummary> contents = amazonS3.listObjects(req).getObjectSummaries();
        if (contents.size() <= 0) {
            throw new AmazonS3Exception("No files found under " + sourceBucket + " and prefix " + sourcePrefix);
        }
        for (S3ObjectSummary summary : contents) {
            String newKey = destinationPrefix + summary.getKey().replace(sourcePrefix, "");
            CopyObjectRequest copyObjectRequest = new CopyObjectRequest(sourceBucket, summary.getKey(),
                                                                        destinationBucket, newKey);
            transferManager.copy(copyObjectRequest);
        }
    }

    public File getTmpFile() throws IOException {
        File tmpFile = File.createTempFile("lola-", ".tmp");
        tmpFile.deleteOnExit();
        return tmpFile;
    }

    public boolean doesBucketExist(String bucket) {
        return amazonS3.doesBucketExist(s3Bucket + "bucket");
    }

    public List<S3ObjectSummary> getObjectSummaries() {
        List<S3ObjectSummary> objectSummaries = new ArrayList<S3ObjectSummary>();
        ObjectListing objects = amazonS3.listObjects
                (s3Bucket, "candidate/");
        objectSummaries.addAll(objects.getObjectSummaries());
        while (objects.isTruncated()) {
            objects = amazonS3.listNextBatchOfObjects(objects);
            objectSummaries.addAll(objects.getObjectSummaries());
        }
        return objectSummaries;
    }

    public List<S3ObjectSummary> filterMigratedObjects(List<S3ObjectSummary> objectSummaries) {
        List<S3ObjectSummary> filteredSummaries = objectSummaries.stream().filter(s -> !s.getKey().contains("/migrated/"))
                .collect(Collectors.toList());
        return filteredSummaries;
    }

    public void addObjectMetadata(S3ObjectSummary objectSummary) {
        ObjectMetadata metadata = new ObjectMetadata();
        Mimetypes mimetypes = Mimetypes.getInstance();
        metadata.setContentType(mimetypes.getMimetype(objectSummary.getKey()));
        final CopyObjectRequest request = new CopyObjectRequest(objectSummary.getBucketName(), objectSummary.getKey(), objectSummary.getBucketName(), objectSummary.getKey())
                .withSourceBucketName(objectSummary.getBucketName())
                .withSourceKey(objectSummary.getKey())
                .withNewObjectMetadata(metadata);
        amazonS3.copyObject(request);
    }
}
