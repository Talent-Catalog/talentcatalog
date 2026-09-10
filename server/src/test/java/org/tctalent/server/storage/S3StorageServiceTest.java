/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.storage;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.configuration.properties.S3Properties;
import org.tctalent.server.files.StoredFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CopyObjectResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@ExtendWith(MockitoExtension.class)
class S3StorageServiceTest {

  @Mock
  private S3Client s3Client;

  @Mock
  private S3Properties s3Properties;

  @Mock
  private StorageKeyService storageKeyService;

  private S3StorageService service;

  @BeforeEach
  void setUp() {
    service = new S3StorageService(s3Client, s3Properties, storageKeyService);
  }

  @Test
  void storeGeneratesKeyUploadsFileWithContentTypeAndReturnsStoredFileInfo() throws Exception {
    byte[] content = "hello s3".getBytes(UTF_8);

    given(storageKeyService.newStorageKey()).willReturn("generated-key");
    given(s3Properties.getCandidateFilesBucket()).willReturn("candidate-bucket");
    given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
        .willReturn(PutObjectResponse.builder().build());

    StoredFileInfo result = service.store(StoragePutRequest.builder()
        .inputStream(new ByteArrayInputStream(content))
        .contentType("text/plain")
        .build());

    assertTrue(result.isActive());
    assertEquals("generated-key", result.getStorageKey());
    assertEquals("candidate-bucket", result.getBucket());
    assertEquals("text/plain", result.getFileType());
    assertEquals((long) content.length, result.getContentLength());
    assertEquals(sha256Hex(content), result.getSha256Hex());

    ArgumentCaptor<PutObjectRequest> putCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
    verify(s3Client).putObject(putCaptor.capture(), any(RequestBody.class));

    PutObjectRequest putRequest = putCaptor.getValue();
    assertEquals("candidate-bucket", putRequest.bucket());
    assertEquals("generated-key", putRequest.key());
    assertEquals("text/plain", putRequest.contentType());
    assertEquals((long) content.length, putRequest.contentLength());
  }

  @Test
  void storeUploadsFileWithoutContentTypeWhenContentTypeIsNull() {
    byte[] content = "no content type".getBytes(UTF_8);

    given(storageKeyService.newStorageKey()).willReturn("no-type-key");
    given(s3Properties.getCandidateFilesBucket()).willReturn("candidate-bucket");
    given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
        .willReturn(PutObjectResponse.builder().build());

    StoredFileInfo result = service.store(StoragePutRequest.builder()
        .inputStream(new ByteArrayInputStream(content))
        .build());

    assertEquals("no-type-key", result.getStorageKey());
    assertEquals("candidate-bucket", result.getBucket());
    assertNull(result.getFileType());
    assertEquals((long) content.length, result.getContentLength());

    ArgumentCaptor<PutObjectRequest> putCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
    verify(s3Client).putObject(putCaptor.capture(), any(RequestBody.class));

    PutObjectRequest putRequest = putCaptor.getValue();
    assertEquals("candidate-bucket", putRequest.bucket());
    assertEquals("no-type-key", putRequest.key());
    assertNull(putRequest.contentType());
    assertEquals((long) content.length, putRequest.contentLength());
  }

  @Test
  void storeWrapsUploadFailureAsStorageException() {
    RuntimeException s3Failure = new RuntimeException("s3 is down");

    given(storageKeyService.newStorageKey()).willReturn("failed-key");
    given(s3Properties.getCandidateFilesBucket()).willReturn("candidate-bucket");
    given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
        .willThrow(s3Failure);

    StorageException exception = assertThrows(
        StorageException.class,
        () -> service.store(StoragePutRequest.builder()
            .inputStream(new ByteArrayInputStream("content".getBytes(UTF_8)))
            .contentType("text/plain")
            .build())
    );

    assertEquals("storage", exception.getErrorCode());
    assertTrue(exception.getMessage().contains("Failed to upload to S3"));
    assertSame(s3Failure, exception.getCause());
  }

  @Test
  void storeWrapsTempFileCreationFailureAsStorageException() {
    IOException tempFileFailure = new IOException("temp unavailable");

    try (MockedStatic<File> mockedFile = mockStatic(File.class)) {
      mockedFile.when(() -> File.createTempFile("s3-upload-", ".tmp"))
          .thenThrow(tempFileFailure);

      StorageException exception = assertThrows(
          StorageException.class,
          () -> service.store(StoragePutRequest.builder()
              .inputStream(new ByteArrayInputStream("content".getBytes(UTF_8)))
              .contentType("text/plain")
              .build())
      );

      assertEquals("storage", exception.getErrorCode());
      assertTrue(exception.getMessage().contains("Failed to upload to S3"));
      assertSame(tempFileFailure, exception.getCause());
      verifyNoInteractions(s3Client);
    }
  }

  @Test
  void storeCoversCleanupBranchWhenTempFileNoLongerExists() throws IOException {
    Path realTempPath = Files.createTempFile("s3-upload-real-", ".tmp");
    File tempFileThatReportsMissing = new File(realTempPath.toString()) {
      @Override
      public boolean exists() {
        return false;
      }
    };

    try (MockedStatic<File> mockedFile = mockStatic(File.class)) {
      mockedFile.when(() -> File.createTempFile("s3-upload-", ".tmp"))
          .thenReturn(tempFileThatReportsMissing);

      given(storageKeyService.newStorageKey()).willReturn("cleanup-branch-key");
      given(s3Properties.getCandidateFilesBucket()).willReturn("candidate-bucket");
      given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
          .willReturn(PutObjectResponse.builder().build());

      StoredFileInfo result = service.store(StoragePutRequest.builder()
          .inputStream(new ByteArrayInputStream("cleanup branch".getBytes(UTF_8)))
          .build());

      assertEquals("cleanup-branch-key", result.getStorageKey());
      assertEquals("candidate-bucket", result.getBucket());
    } finally {
      Files.deleteIfExists(realTempPath);
    }
  }

  @Test
  void openStreamReturnsObjectStreamFromS3() throws IOException {
    ResponseInputStream<GetObjectResponse> s3Object = responseInputStream();

    given(s3Properties.getCandidateFilesBucket()).willReturn("candidate-bucket");
    given(s3Client.getObject(any(GetObjectRequest.class))).willReturn(s3Object);

    InputStream result = service.openStream("stored-key");

    assertSame(s3Object, result);
    assertEquals("downloaded file", new String(result.readAllBytes(), UTF_8));

    ArgumentCaptor<GetObjectRequest> requestCaptor = ArgumentCaptor.forClass(GetObjectRequest.class);
    verify(s3Client).getObject(requestCaptor.capture());

    GetObjectRequest request = requestCaptor.getValue();
    assertEquals("candidate-bucket", request.bucket());
    assertEquals("stored-key", request.key());
  }

  @Test
  void openStreamWrapsNoSuchKeyExceptionAsStorageException() {
    NoSuchKeyException notFound = NoSuchKeyException.builder()
        .message("not found")
        .build();

    given(s3Properties.getCandidateFilesBucket()).willReturn("candidate-bucket");
    given(s3Client.getObject(any(GetObjectRequest.class))).willThrow(notFound);

    StorageException exception = assertThrows(
        StorageException.class,
        () -> service.openStream("missing-key")
    );

    assertEquals("storage", exception.getErrorCode());
    assertEquals("Object not found: missing-key", exception.getMessage());
    assertSame(notFound, exception.getCause());
  }

  @Test
  void deleteBuildsDeleteObjectRequest() {
    given(s3Properties.getCandidateFilesBucket()).willReturn("candidate-bucket");

    service.delete("delete-key");

    ArgumentCaptor<DeleteObjectRequest> requestCaptor =
        ArgumentCaptor.forClass(DeleteObjectRequest.class);
    verify(s3Client).deleteObject(requestCaptor.capture());

    DeleteObjectRequest request = requestCaptor.getValue();
    assertEquals("candidate-bucket", request.bucket());
    assertEquals("delete-key", request.key());
  }

  @Test
  void existsReturnsTrueWhenHeadObjectSucceeds() {
    given(s3Properties.getCandidateFilesBucket()).willReturn("candidate-bucket");

    boolean result = service.exists("existing-key");

    assertTrue(result);

    ArgumentCaptor<HeadObjectRequest> requestCaptor =
        ArgumentCaptor.forClass(HeadObjectRequest.class);
    verify(s3Client).headObject(requestCaptor.capture());

    HeadObjectRequest request = requestCaptor.getValue();
    assertEquals("candidate-bucket", request.bucket());
    assertEquals("existing-key", request.key());
  }

  @Test
  void existsReturnsFalseWhenS3ThrowsNoSuchKeyException() {
    given(s3Properties.getCandidateFilesBucket()).willReturn("candidate-bucket");
    given(s3Client.headObject(any(HeadObjectRequest.class)))
        .willThrow(NoSuchKeyException.builder().message("missing").build());

    boolean result = service.exists("missing-key");

    assertFalse(result);
  }

  @Test
  void copyBuildsCopyObjectRequestAndReturnsTargetStoredFile() {
    given(s3Properties.getCandidateFilesBucket()).willReturn("candidate-bucket");
    given(s3Client.copyObject(any(CopyObjectRequest.class)))
        .willReturn(CopyObjectResponse.builder().build());

    StoredFile result = service.copy("source-key", "target-key");

    assertEquals("target-key", result.getStorageKey());
    assertEquals("candidate-bucket", result.getBucket());

    ArgumentCaptor<CopyObjectRequest> requestCaptor =
        ArgumentCaptor.forClass(CopyObjectRequest.class);
    verify(s3Client).copyObject(requestCaptor.capture());

    CopyObjectRequest request = requestCaptor.getValue();
    assertEquals("candidate-bucket", request.sourceBucket());
    assertEquals("source-key", request.sourceKey());
    assertEquals("candidate-bucket", request.destinationBucket());
    assertEquals("target-key", request.destinationKey());
  }

  @Test
  void moveCopiesToTargetThenDeletesSource() {
    given(s3Properties.getCandidateFilesBucket()).willReturn("candidate-bucket");
    given(s3Client.copyObject(any(CopyObjectRequest.class)))
        .willReturn(CopyObjectResponse.builder().build());

    StoredFile result = service.move("source-key", "target-key");

    assertEquals("target-key", result.getStorageKey());
    assertEquals("candidate-bucket", result.getBucket());

    InOrder inOrder = inOrder(s3Client);
    inOrder.verify(s3Client).copyObject(any(CopyObjectRequest.class));
    inOrder.verify(s3Client).deleteObject(any(DeleteObjectRequest.class));

    ArgumentCaptor<DeleteObjectRequest> deleteCaptor =
        ArgumentCaptor.forClass(DeleteObjectRequest.class);
    verify(s3Client).deleteObject(deleteCaptor.capture());

    DeleteObjectRequest deleteRequest = deleteCaptor.getValue();
    assertEquals("candidate-bucket", deleteRequest.bucket());
    assertEquals("source-key", deleteRequest.key());
  }

  @Test
  void tempFileCreationFailureDoesNotCallPutObject() {
    IOException tempFileFailure = new IOException("cannot create temp file");

    try (MockedStatic<File> mockedFile = mockStatic(File.class)) {
      mockedFile.when(() -> File.createTempFile("s3-upload-", ".tmp"))
          .thenThrow(tempFileFailure);

      assertThrows(
          StorageException.class,
          () -> service.store(StoragePutRequest.builder()
              .inputStream(new ByteArrayInputStream("content".getBytes(UTF_8)))
              .build())
      );

      verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }
  }

  private ResponseInputStream<GetObjectResponse> responseInputStream() {
    return new ResponseInputStream<>(
        GetObjectResponse.builder().build(),
        AbortableInputStream.create(new ByteArrayInputStream("downloaded file".getBytes(UTF_8)))
    );
  }

  private String sha256Hex(byte[] content) throws Exception {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    return HexFormat.of().formatHex(digest.digest(content));
  }
}