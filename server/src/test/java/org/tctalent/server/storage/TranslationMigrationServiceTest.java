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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.exception.FileDownloadException;
import org.tctalent.server.exception.ServiceException;
import org.tctalent.server.service.db.aws.S3ResourceHelper;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

@ExtendWith(MockitoExtension.class)
class TranslationMigrationServiceTest {

  @Mock
  private S3ResourceHelper s3ResourceHelper;

  @Mock
  private S3Client s3Client;

  private TranslationMigrationService service;

  @BeforeEach
  void setUp() {
    service = new TranslationMigrationService(s3ResourceHelper, s3Client);
  }

  @Test
  void migrateBucketContentsCopiesFilesWithNormalizedPrefixesAndReturnsCopiedCount()
      throws IOException {
    File enFile = createTempFile("en");
    File orphanFile = createTempFile("orphan");

    given(s3ResourceHelper.listObjectSummaries("source-bucket", "legacy/"))
        .willReturn(List.of(
            S3Object.builder().key("legacy/en.json").build(),
            S3Object.builder().key("unexpected/orphan.json").build()
        ));

    given(s3ResourceHelper.downloadFile("source-bucket", "legacy/en.json"))
        .willReturn(enFile);
    given(s3ResourceHelper.downloadFile("source-bucket", "unexpected/orphan.json"))
        .willReturn(orphanFile);

    int copiedCount = service.migrateBucketContents(
        "source-bucket",
        "legacy",
        "destination-bucket",
        "translations/"
    );

    assertEquals(2, copiedCount);
    assertFalse(enFile.exists());
    assertFalse(orphanFile.exists());

    ArgumentCaptor<PutObjectRequest> putCaptor =
        ArgumentCaptor.forClass(PutObjectRequest.class);

    verify(s3Client, times(2)).putObject(putCaptor.capture(), any(RequestBody.class));

    List<PutObjectRequest> requests = putCaptor.getAllValues();

    assertEquals("destination-bucket", requests.get(0).bucket());
    assertEquals("translations/en.json", requests.get(0).key());
    assertEquals(2L, requests.get(0).contentLength());

    assertEquals("destination-bucket", requests.get(1).bucket());
    assertEquals("translations/unexpected/orphan.json", requests.get(1).key());
    assertEquals(6L, requests.get(1).contentLength());
  }

  @Test
  void migrateBucketContentsSupportsBlankSourceAndDestinationPrefixes() throws IOException {
    File file = createTempFile("hello");

    given(s3ResourceHelper.listObjectSummaries("source-bucket", ""))
        .willReturn(List.of(S3Object.builder().key("en.json").build()));

    given(s3ResourceHelper.downloadFile("source-bucket", "en.json"))
        .willReturn(file);

    int copiedCount = service.migrateBucketContents(
        "source-bucket",
        " ",
        "destination-bucket",
        null
    );

    assertEquals(1, copiedCount);
    assertFalse(file.exists());

    ArgumentCaptor<PutObjectRequest> putCaptor =
        ArgumentCaptor.forClass(PutObjectRequest.class);

    verify(s3Client).putObject(putCaptor.capture(), any(RequestBody.class));

    PutObjectRequest request = putCaptor.getValue();
    assertEquals("destination-bucket", request.bucket());
    assertEquals("en.json", request.key());
    assertEquals(5L, request.contentLength());
  }

  @Test
  void migrateBucketContentsThrowsServiceExceptionWhenNoFilesFound() {
    given(s3ResourceHelper.listObjectSummaries("source-bucket", "missing/"))
        .willReturn(List.of());

    ServiceException exception = assertThrows(
        ServiceException.class,
        () -> service.migrateBucketContents(
            "source-bucket",
            "missing",
            "destination-bucket",
            "translations"
        )
    );

    assertEquals("migration_empty", exception.getErrorCode());
    assertEquals(
        "No files found under missing/ in source-bucket",
        exception.getMessage()
    );

    verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
  }

  @Test
  void migrateBucketContentsRethrowsFileDownloadException() throws FileDownloadException {
    FileDownloadException downloadException =
        new FileDownloadException("download failed", new RuntimeException("source failed"));

    given(s3ResourceHelper.listObjectSummaries("source-bucket", "legacy/"))
        .willReturn(List.of(S3Object.builder().key("legacy/en.json").build()));

    given(s3ResourceHelper.downloadFile("source-bucket", "legacy/en.json"))
        .willThrow(downloadException);

    FileDownloadException exception = assertThrows(
        FileDownloadException.class,
        () -> service.migrateBucketContents(
            "source-bucket",
            "legacy",
            "destination-bucket",
            "translations"
        )
    );

    assertSame(downloadException, exception);

    verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
  }

  @Test
  void migrateBucketContentsWrapsUploadFailureAsServiceExceptionAndDeletesDownloadedFile()
      throws IOException {
    RuntimeException uploadFailure = new RuntimeException("upload failed");
    File file = createTempFile("broken");

    given(s3ResourceHelper.listObjectSummaries("source-bucket", "legacy/"))
        .willReturn(List.of(S3Object.builder().key("legacy/en.json").build()));

    given(s3ResourceHelper.downloadFile("source-bucket", "legacy/en.json"))
        .willReturn(file);

    given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
        .willThrow(uploadFailure);

    ServiceException exception = assertThrows(
        ServiceException.class,
        () -> service.migrateBucketContents(
            "source-bucket",
            "legacy",
            "destination-bucket",
            "translations"
        )
    );

    assertEquals("migration_failed", exception.getErrorCode());
    assertEquals(
        "Failed to migrate legacy/en.json to translations/en.json",
        exception.getMessage()
    );
    assertSame(uploadFailure, exception.getCause());
    assertFalse(file.exists());
  }

  @Test
  void migrateBucketContentsIgnoresCleanupFailureAndStillReturnsCopiedCount()
      throws IOException {
    File file = createTempFile("cleanup");

    given(s3ResourceHelper.listObjectSummaries("source-bucket", "legacy/"))
        .willReturn(List.of(S3Object.builder().key("legacy/en.json").build()));

    given(s3ResourceHelper.downloadFile("source-bucket", "legacy/en.json"))
        .willReturn(file);

    try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
      mockedFiles.when(() -> Files.deleteIfExists(file.toPath()))
          .thenThrow(new IOException("delete failed"));

      int copiedCount = service.migrateBucketContents(
          "source-bucket",
          "legacy",
          "destination-bucket",
          "translations"
      );

      assertEquals(1, copiedCount);
    } finally {
      Files.deleteIfExists(file.toPath());
    }
  }

  private File createTempFile(String content) throws IOException {
    File file = File.createTempFile("translation-migration-", ".json");
    Files.writeString(file.toPath(), content, UTF_8);
    return file;
  }
}