/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.db.aws;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.tctalent.server.exception.FileDownloadException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CopyObjectResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

class S3ResourceHelperTest {

    private static final String BUCKET = "test-bucket";

    private S3Client s3Client;
    private S3ResourceHelper s3ResourceHelper;

    @BeforeEach
    void setUp() {
        s3Client = mock(S3Client.class);
        s3ResourceHelper = new S3ResourceHelper("", "", "us-east-1");

        ReflectionTestUtils.setField(s3ResourceHelper, "s3Client", s3Client);
        ReflectionTestUtils.setField(s3ResourceHelper, "s3Bucket", BUCKET);
    }

    @Test
    void constructorSupportsExplicitCredentials() {
        assertNotNull(new S3ResourceHelper("access-key", "secret-key", "us-east-1"));
    }

    @Test
    void constructorSupportsAnonymousCredentials() {
        assertNotNull(new S3ResourceHelper("", "", "us-east-1"));
    }

    @Test
    void getS3BucketReturnsConfiguredBucket() {
        assertEquals(BUCKET, s3ResourceHelper.getS3Bucket());
    }

    @Test
    void downloadFileUsesConfiguredBucketAndCopiesContent() {
        when(s3Client.getObject(any(GetObjectRequest.class)))
            .thenAnswer(invocation -> responseInputStream("downloaded content"));

        File downloaded = s3ResourceHelper.downloadFile("candidate/file.txt");

        assertTrue(downloaded.exists());
        assertEquals("downloaded content", read(downloaded));

        ArgumentCaptor<GetObjectRequest> captor = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(s3Client).getObject(captor.capture());
        assertEquals(BUCKET, captor.getValue().bucket());
        assertEquals("candidate/file.txt", captor.getValue().key());
    }

    @Test
    void downloadFileWithBucketUsesProvidedBucket() {
        when(s3Client.getObject(any(GetObjectRequest.class)))
            .thenAnswer(invocation -> responseInputStream("bucket content"));

        File downloaded = s3ResourceHelper.downloadFile("other-bucket", "path/file.txt");

        assertEquals("bucket content", read(downloaded));

        ArgumentCaptor<GetObjectRequest> captor = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(s3Client).getObject(captor.capture());
        assertEquals("other-bucket", captor.getValue().bucket());
        assertEquals("path/file.txt", captor.getValue().key());
    }

    @Test
    void downloadFileWrapsS3Failure() {
        when(s3Client.getObject(any(GetObjectRequest.class)))
            .thenThrow(new RuntimeException("S3 failed"));

        assertThrows(FileDownloadException.class,
            () -> s3ResourceHelper.downloadFile("candidate/missing.txt"));
    }

    @Test
    void downloadResourceReturnsFileSystemResource() throws Exception {
        when(s3Client.getObject(any(GetObjectRequest.class)))
            .thenAnswer(invocation -> responseInputStream("resource content"));

        Resource resource = s3ResourceHelper.downloadResource("candidate/file.txt");

        assertTrue(resource.exists());
        assertEquals("resource content", Files.readString(resource.getFile().toPath()));
    }

    @Test
    void downloadResourceToTargetLocationCopiesContent() throws Exception {
        when(s3Client.getObject(any(GetObjectRequest.class)))
            .thenAnswer(invocation -> responseInputStream("target content"));

        File target = File.createTempFile("s3-target-", ".txt");
        Resource resource = s3ResourceHelper.downloadResource("candidate/file.txt", target);

        assertEquals(target.getAbsolutePath(), resource.getFile().getAbsolutePath());
        assertEquals("target content", Files.readString(target.toPath()));
    }

    @Test
    void downloadResourceToTargetLocationWrapsFailure() throws Exception {
        when(s3Client.getObject(any(GetObjectRequest.class)))
            .thenThrow(new RuntimeException("S3 failed"));

        File target = File.createTempFile("s3-target-", ".txt");

        assertThrows(FileDownloadException.class,
            () -> s3ResourceHelper.downloadResource("candidate/missing.txt", target));
    }

    @Test
    void uploadFileUsesConfiguredBucketAndContentType() throws Exception {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenReturn(PutObjectResponse.builder().build());

        File file = File.createTempFile("s3-upload-", ".txt");
        Files.writeString(file.toPath(), "upload content");

        s3ResourceHelper.uploadFile(file, "candidate/file.txt", "text/plain");

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        assertEquals(BUCKET, captor.getValue().bucket());
        assertEquals("candidate/file.txt", captor.getValue().key());
        assertEquals("text/plain", captor.getValue().contentType());
    }

    @Test
    void uploadFileWithBucketAllowsNullContentType() throws Exception {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenReturn(PutObjectResponse.builder().build());

        File file = File.createTempFile("s3-upload-", ".txt");
        Files.writeString(file.toPath(), "upload content");

        s3ResourceHelper.uploadFile("other-bucket", file, "file.txt", null);

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        assertEquals("other-bucket", captor.getValue().bucket());
        assertEquals("file.txt", captor.getValue().key());
        assertNull(captor.getValue().contentType());
    }

    @Test
    void uploadFileWrapsFailure() throws Exception {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenThrow(new RuntimeException("S3 failed"));

        File file = File.createTempFile("s3-upload-", ".txt");

        assertThrows(FileUploadException.class,
            () -> s3ResourceHelper.uploadFile(file, "candidate/file.txt", "text/plain"));
    }

    @Test
    void uploadMultipartFileUsesConfiguredBucketSizeAndContentType() throws Exception {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenReturn(PutObjectResponse.builder().build());

        MockMultipartFile file = new MockMultipartFile(
            "file", "test.txt", "text/plain", "multipart content".getBytes());

        s3ResourceHelper.uploadFile("candidate/file.txt", file);

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        assertEquals(BUCKET, captor.getValue().bucket());
        assertEquals("candidate/file.txt", captor.getValue().key());
        assertEquals("text/plain", captor.getValue().contentType());
        assertEquals(file.getSize(), captor.getValue().contentLength());
    }

    @Test
    void uploadMultipartFileWithBucketAllowsNullContentType() throws Exception {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenReturn(PutObjectResponse.builder().build());

        MockMultipartFile file = new MockMultipartFile(
            "file", "test.txt", null, "multipart content".getBytes());

        s3ResourceHelper.uploadFile("other-bucket", "candidate/file.txt", file);

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        assertEquals("other-bucket", captor.getValue().bucket());
        assertEquals("candidate/file.txt", captor.getValue().key());
        assertNull(captor.getValue().contentType());
    }

    @Test
    void uploadMultipartFileWrapsFailure() throws Exception {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenThrow(new RuntimeException("S3 failed"));

        MockMultipartFile file = new MockMultipartFile(
            "file", "test.txt", "text/plain", "multipart content".getBytes());

        assertThrows(FileUploadException.class,
            () -> s3ResourceHelper.uploadFile("candidate/file.txt", file));
    }

    @Test
    void uploadStringContentUsesConfiguredBucketAndContentType() throws Exception {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenReturn(PutObjectResponse.builder().build());

        s3ResourceHelper.uploadFile("hello", "candidate/file.txt", "text/plain");

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        assertEquals(BUCKET, captor.getValue().bucket());
        assertEquals("candidate/file.txt", captor.getValue().key());
        assertEquals("text/plain", captor.getValue().contentType());
        assertEquals(5L, captor.getValue().contentLength());
    }

    @Test
    void uploadStringContentWithBucketAllowsNullContentType() throws Exception {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenReturn(PutObjectResponse.builder().build());

        s3ResourceHelper.uploadFile("other-bucket", "hello", "file.txt", null);

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        assertEquals("other-bucket", captor.getValue().bucket());
        assertEquals("file.txt", captor.getValue().key());
        assertNull(captor.getValue().contentType());
        assertEquals(5L, captor.getValue().contentLength());
    }

    @Test
    void uploadStringContentWrapsFailure() throws Exception {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenThrow(new RuntimeException("S3 failed"));

        assertThrows(FileUploadException.class,
            () -> s3ResourceHelper.uploadFile("hello", "candidate/file.txt", "text/plain"));
    }

    @Test
    void uploadInputStreamUsesProvidedBucketLengthAndContentType() throws Exception {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenReturn(PutObjectResponse.builder().build());

        InputStream inputStream = new ByteArrayInputStream("stream content".getBytes());

        s3ResourceHelper.uploadFile("other-bucket", inputStream, 14L, "file.txt", "text/plain");

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        assertEquals("other-bucket", captor.getValue().bucket());
        assertEquals("file.txt", captor.getValue().key());
        assertEquals(14L, captor.getValue().contentLength());
        assertEquals("text/plain", captor.getValue().contentType());
    }

    @Test
    void uploadInputStreamAllowsNullContentType() throws Exception {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenReturn(PutObjectResponse.builder().build());

        InputStream inputStream = new ByteArrayInputStream("stream content".getBytes());

        s3ResourceHelper.uploadFile("other-bucket", inputStream, 14L, "file.txt", null);

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        assertNull(captor.getValue().contentType());
    }

    @Test
    void uploadInputStreamWrapsFailure() throws Exception {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenThrow(new RuntimeException("S3 failed"));

        InputStream inputStream = new ByteArrayInputStream("stream content".getBytes());

        assertThrows(FileUploadException.class,
            () -> s3ResourceHelper.uploadFile(
                "other-bucket", inputStream, 14L, "file.txt", "text/plain"));
    }

    @Test
    void deleteFileDeletesFromConfiguredBucket() {
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
            .thenReturn(DeleteObjectResponse.builder().build());

        s3ResourceHelper.deleteFile("candidate/file.txt");

        ArgumentCaptor<DeleteObjectRequest> captor =
            ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(captor.capture());
        assertEquals(BUCKET, captor.getValue().bucket());
        assertEquals("candidate/file.txt", captor.getValue().key());
    }

    @Test
    void copyObjectUsesConfiguredBucketForSourceAndDestination() {
        when(s3Client.copyObject(any(CopyObjectRequest.class)))
            .thenReturn(CopyObjectResponse.builder().build());

        s3ResourceHelper.copyObject("source.txt", "destination.txt");

        ArgumentCaptor<CopyObjectRequest> captor =
            ArgumentCaptor.forClass(CopyObjectRequest.class);
        verify(s3Client).copyObject(captor.capture());
        assertEquals(BUCKET, captor.getValue().sourceBucket());
        assertEquals("source.txt", captor.getValue().sourceKey());
        assertEquals(BUCKET, captor.getValue().destinationBucket());
        assertEquals("destination.txt", captor.getValue().destinationKey());
    }

    @Test
    void copyObjectWithNullBucketsFallsBackToConfiguredBucket() {
        when(s3Client.copyObject(any(CopyObjectRequest.class)))
            .thenReturn(CopyObjectResponse.builder().build());

        s3ResourceHelper.copyObject(null, "source.txt", null, "destination.txt");

        ArgumentCaptor<CopyObjectRequest> captor =
            ArgumentCaptor.forClass(CopyObjectRequest.class);
        verify(s3Client).copyObject(captor.capture());
        assertEquals(BUCKET, captor.getValue().sourceBucket());
        assertEquals(BUCKET, captor.getValue().destinationBucket());
    }

    @Test
    void copyObjectWithExplicitBucketsUsesExplicitBuckets() {
        when(s3Client.copyObject(any(CopyObjectRequest.class)))
            .thenReturn(CopyObjectResponse.builder().build());

        s3ResourceHelper.copyObject(
            "source-bucket", "source.txt", "destination-bucket", "destination.txt");

        ArgumentCaptor<CopyObjectRequest> captor =
            ArgumentCaptor.forClass(CopyObjectRequest.class);
        verify(s3Client).copyObject(captor.capture());
        assertEquals("source-bucket", captor.getValue().sourceBucket());
        assertEquals("destination-bucket", captor.getValue().destinationBucket());
    }

    @Test
    void copyBucketContentsCopiesEachObjectToDestinationPrefix() {
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
            .thenReturn(ListObjectsV2Response.builder()
                .contents(
                    S3Object.builder().key("source/a.txt").build(),
                    S3Object.builder().key("source/nested/b.txt").build()
                )
                .build());
        when(s3Client.copyObject(any(CopyObjectRequest.class)))
            .thenReturn(CopyObjectResponse.builder().build());

        s3ResourceHelper.copyBucketContents(
            "source-bucket", "source/", "destination-bucket", "destination/");

        ArgumentCaptor<CopyObjectRequest> captor =
            ArgumentCaptor.forClass(CopyObjectRequest.class);
        verify(s3Client, org.mockito.Mockito.times(2)).copyObject(captor.capture());

        assertEquals("destination/a.txt", captor.getAllValues().get(0).destinationKey());
        assertEquals("destination/nested/b.txt", captor.getAllValues().get(1).destinationKey());
    }

    @Test
    void copyBucketContentsThrowsWhenNoFilesAreFound() {
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
            .thenReturn(ListObjectsV2Response.builder().contents(List.of()).build());

        assertThrows(S3Exception.class,
            () -> s3ResourceHelper.copyBucketContents(
                "source-bucket", "source/", "destination-bucket", "destination/"));
    }

    @Test
    void getObjectSummariesListsAllCandidateObjectsAcrossPages() {
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
            .thenReturn(
                ListObjectsV2Response.builder()
                    .contents(S3Object.builder().key("candidate/a.txt").build())
                    .nextContinuationToken("next-page")
                    .build(),
                ListObjectsV2Response.builder()
                    .contents(S3Object.builder().key("candidate/b.txt").build())
                    .build()
            );

        List<S3Object> results = s3ResourceHelper.getObjectSummaries();

        assertEquals(2, results.size());
        assertEquals("candidate/a.txt", results.get(0).key());
        assertEquals("candidate/b.txt", results.get(1).key());

        ArgumentCaptor<ListObjectsV2Request> captor =
            ArgumentCaptor.forClass(ListObjectsV2Request.class);
        verify(s3Client, org.mockito.Mockito.times(2)).listObjectsV2(captor.capture());
        assertEquals(BUCKET, captor.getAllValues().get(0).bucket());
        assertEquals("candidate/", captor.getAllValues().get(0).prefix());
        assertNull(captor.getAllValues().get(0).continuationToken());
        assertEquals("next-page", captor.getAllValues().get(1).continuationToken());
    }

    @Test
    void listObjectSummariesListsAllObjectsAcrossPages() {
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
            .thenReturn(
                ListObjectsV2Response.builder()
                    .contents(S3Object.builder().key("prefix/a.txt").build())
                    .nextContinuationToken("next-page")
                    .build(),
                ListObjectsV2Response.builder()
                    .contents(S3Object.builder().key("prefix/b.txt").build())
                    .build()
            );

        List<S3Object> results = s3ResourceHelper.listObjectSummaries("other-bucket", "prefix/");

        assertEquals(2, results.size());
        assertEquals("prefix/a.txt", results.get(0).key());
        assertEquals("prefix/b.txt", results.get(1).key());

        ArgumentCaptor<ListObjectsV2Request> captor =
            ArgumentCaptor.forClass(ListObjectsV2Request.class);
        verify(s3Client, org.mockito.Mockito.times(2)).listObjectsV2(captor.capture());
        assertEquals("other-bucket", captor.getAllValues().get(0).bucket());
        assertEquals("prefix/", captor.getAllValues().get(0).prefix());
        assertNull(captor.getAllValues().get(0).continuationToken());
        assertEquals("next-page", captor.getAllValues().get(1).continuationToken());
    }

    @Test
    void filterMigratedObjectsRemovesObjectsInMigratedFolder() {
        List<S3Object> results = s3ResourceHelper.filterMigratedObjects(List.of(
            S3Object.builder().key("candidate/one.pdf").build(),
            S3Object.builder().key("candidate/migrated/two.pdf").build(),
            S3Object.builder().key("candidate/three.pdf").build()
        ));

        assertEquals(2, results.size());
        assertEquals("candidate/one.pdf", results.get(0).key());
        assertEquals("candidate/three.pdf", results.get(1).key());
        assertFalse(results.stream().anyMatch(o -> o.key().contains("/migrated/")));
    }

    @Test
    void getTmpFileCreatesTemporaryFile() throws Exception {
        File file = s3ResourceHelper.getTmpFile();

        assertTrue(file.exists());
        assertTrue(file.getName().startsWith("lola-"));
        assertTrue(file.getName().endsWith(".tmp"));
    }

    @Test
    void doesBucketExistReturnsTrueWhenHeadBucketSucceeds() {
        when(s3Client.headBucket(any(HeadBucketRequest.class)))
            .thenReturn(HeadBucketResponse.builder().build());

        assertTrue(s3ResourceHelper.doesBucketExist("other-bucket"));

        ArgumentCaptor<HeadBucketRequest> captor =
            ArgumentCaptor.forClass(HeadBucketRequest.class);
        verify(s3Client).headBucket(captor.capture());
        assertEquals("other-bucket", captor.getValue().bucket());
    }

    private ResponseInputStream<GetObjectResponse> responseInputStream(String content) {
        return new ResponseInputStream<>(
            GetObjectResponse.builder().build(),
            AbortableInputStream.create(new ByteArrayInputStream(content.getBytes()))
        );
    }

    private String read(File file) {
        try {
            return Files.readString(file.toPath());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}