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
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.tctalent.server.configuration.properties.S3Properties;
import org.tctalent.server.exception.ServiceException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

@ExtendWith(MockitoExtension.class)
class S3TranslationStorageServiceTest {

  @Mock
  private S3Client s3Client;

  @Mock
  private S3Properties s3Properties;

  private S3TranslationStorageService service;

  @BeforeEach
  void setUp() {
    service = new S3TranslationStorageService(s3Client, s3Properties);
    ReflectionTestUtils.setField(service, "instanceType", "TBB");
  }

  @Test
  void getTranslationFileUsesConfiguredBucketAndConfiguredFolderWithTrailingSlash() {
    given(s3Properties.getTranslationsBucket()).willReturn("configured-bucket");
    given(s3Properties.getTranslationsFolder()).willReturn("custom/translations/");

    given(s3Client.getObject(any(GetObjectRequest.class)))
        .willReturn(responseInputStream("""
            {"hello":"world","count":2}
            """));

    Map<String, Object> result = service.getTranslationFile("fr");

    assertEquals("world", result.get("hello"));
    assertEquals(2, result.get("count"));

    ArgumentCaptor<GetObjectRequest> requestCaptor =
        ArgumentCaptor.forClass(GetObjectRequest.class);
    verify(s3Client).getObject(requestCaptor.capture());

    GetObjectRequest request = requestCaptor.getValue();
    assertEquals("configured-bucket", request.bucket());
    assertEquals("custom/translations/fr.json", request.key());
  }

  @Test
  void getTranslationFileUsesGrnFallbackBucketAndDefaultTranslationsFolder() {
    ReflectionTestUtils.setField(service, "instanceType", "GRN");

    given(s3Properties.getTranslationsBucket()).willReturn("");
    given(s3Properties.getGrnTranslationsBucket()).willReturn("grn-translations-bucket");
    given(s3Properties.getTranslationsFolder()).willReturn("");

    given(s3Client.getObject(any(GetObjectRequest.class)))
        .willReturn(responseInputStream("""
            {"language":"en"}
            """));

    Map<String, Object> result = service.getTranslationFile("en");

    assertEquals("en", result.get("language"));

    ArgumentCaptor<GetObjectRequest> requestCaptor =
        ArgumentCaptor.forClass(GetObjectRequest.class);
    verify(s3Client).getObject(requestCaptor.capture());

    GetObjectRequest request = requestCaptor.getValue();
    assertEquals("grn-translations-bucket", request.bucket());
    assertEquals("translations/en.json", request.key());
  }

  @Test
  void getTranslationFileWrapsIoExceptionAsServiceException() {
    given(s3Properties.getTranslationsBucket()).willReturn("bucket");
    given(s3Properties.getTranslationsFolder()).willReturn("translations");

    given(s3Client.getObject(any(GetObjectRequest.class)))
        .willReturn(responseInputStreamThatFailsOnRead());

    ServiceException exception = assertThrows(
        ServiceException.class,
        () -> service.getTranslationFile("en")
    );

    assertEquals("json_error", exception.getErrorCode());
    assertEquals("Error reading JSON file from s3", exception.getMessage());
    assertInstanceOf(IOException.class, exception.getCause());
  }

  @Test
  void updateTranslationFileArchivesCurrentFileAndUploadsNewJsonUsingTbbFallbackBucket()
      throws IOException {
    ReflectionTestUtils.setField(service, "instanceType", "TBB");

    given(s3Properties.getTranslationsBucket()).willReturn(" ");
    given(s3Properties.getTbbTranslationsBucket()).willReturn("tbb-translations-bucket");
    given(s3Properties.getTranslationsFolder()).willReturn("i18n");

    Map<String, Object> translations = new LinkedHashMap<>();
    translations.put("hello", "world");
    translations.put("nested", Map.of("yes", true));

    service.updateTranslationFile("es", translations);

    ArgumentCaptor<CopyObjectRequest> copyCaptor =
        ArgumentCaptor.forClass(CopyObjectRequest.class);
    verify(s3Client).copyObject(copyCaptor.capture());

    CopyObjectRequest copyRequest = copyCaptor.getValue();
    assertEquals("tbb-translations-bucket", copyRequest.sourceBucket());
    assertEquals("i18n/es.json", copyRequest.sourceKey());
    assertEquals("tbb-translations-bucket", copyRequest.destinationBucket());
    assertTrue(copyRequest.destinationKey()
        .matches("i18n/old-versions/es\\.json\\.\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}"));

    ArgumentCaptor<PutObjectRequest> putCaptor =
        ArgumentCaptor.forClass(PutObjectRequest.class);
    ArgumentCaptor<RequestBody> bodyCaptor =
        ArgumentCaptor.forClass(RequestBody.class);

    verify(s3Client).putObject(putCaptor.capture(), bodyCaptor.capture());

    byte[] expectedJson = """
        {"hello":"world","nested":{"yes":true}}\
        """.getBytes(UTF_8);

    PutObjectRequest putRequest = putCaptor.getValue();
    assertEquals("tbb-translations-bucket", putRequest.bucket());
    assertEquals("i18n/es.json", putRequest.key());
    assertEquals("text/json", putRequest.contentType());
    assertEquals((long) expectedJson.length, putRequest.contentLength());

    try (InputStream bodyStream = bodyCaptor.getValue().contentStreamProvider().newStream()) {
      assertArrayEquals(expectedJson, bodyStream.readAllBytes());
    }
  }

  @Test
  void updateTranslationFileWrapsJsonProcessingFailureAsServiceException() {
    Map<String, Object> translations = Map.of("bad", new JsonBreakingValue());

    ServiceException exception = assertThrows(
        ServiceException.class,
        () -> service.updateTranslationFile("en", translations)
    );

    assertEquals("invalid_json", exception.getErrorCode());
    assertEquals("The translation data could not be converted to JSON", exception.getMessage());
    verifyNoInteractions(s3Client);
  }

  @Test
  void updateTranslationFileWrapsS3FailureAsServiceException() {
    given(s3Properties.getTranslationsBucket()).willReturn("bucket");
    given(s3Properties.getTranslationsFolder()).willReturn("translations");

    given(s3Client.copyObject(any(CopyObjectRequest.class)))
        .willThrow(S3Exception.builder().message("copy failed").build());

    ServiceException exception = assertThrows(
        ServiceException.class,
        () -> service.updateTranslationFile("en", Map.of("hello", "world"))
    );

    assertEquals("file_upload", exception.getErrorCode());
    assertEquals("The JSON file could not be uploaded to s3", exception.getMessage());

    verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
  }

  @Test
  void copyBucketContentsCopiesObjectsWithNormalizedPrefixesAndUnexpectedSourceKeyBranch() {
    given(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
        .willReturn(ListObjectsV2Response.builder()
            .contents(List.of(
                S3Object.builder().key("source/en.json").build(),
                S3Object.builder().key("unexpected/orphan.json").build()
            ))
            .build());

    service.copyBucketContents(
        "source-bucket",
        "source",
        "destination-bucket",
        "destination/"
    );

    ArgumentCaptor<ListObjectsV2Request> listCaptor =
        ArgumentCaptor.forClass(ListObjectsV2Request.class);
    verify(s3Client).listObjectsV2(listCaptor.capture());

    ListObjectsV2Request listRequest = listCaptor.getValue();
    assertEquals("source-bucket", listRequest.bucket());
    assertEquals("source/", listRequest.prefix());
    assertNull(listRequest.continuationToken());

    ArgumentCaptor<CopyObjectRequest> copyCaptor =
        ArgumentCaptor.forClass(CopyObjectRequest.class);
    verify(s3Client, times(2)).copyObject(copyCaptor.capture());

    List<CopyObjectRequest> copyRequests = copyCaptor.getAllValues();

    assertEquals("source-bucket", copyRequests.get(0).sourceBucket());
    assertEquals("source/en.json", copyRequests.get(0).sourceKey());
    assertEquals("destination-bucket", copyRequests.get(0).destinationBucket());
    assertEquals("destination/en.json", copyRequests.get(0).destinationKey());

    assertEquals("source-bucket", copyRequests.get(1).sourceBucket());
    assertEquals("unexpected/orphan.json", copyRequests.get(1).sourceKey());
    assertEquals("destination-bucket", copyRequests.get(1).destinationBucket());
    assertEquals("destination/unexpected/orphan.json", copyRequests.get(1).destinationKey());
  }

  @Test
  void copyBucketContentsSupportsPaginationAndEmptyPrefixes() {
    given(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
        .willReturn(
            ListObjectsV2Response.builder()
                .contents(List.of(S3Object.builder().key("first.json").build()))
                .nextContinuationToken("TOKEN-1")
                .build(),
            ListObjectsV2Response.builder()
                .contents(List.of(S3Object.builder().key("second.json").build()))
                .build()
        );

    service.copyBucketContents(
        "source-bucket",
        "",
        "destination-bucket",
        ""
    );

    ArgumentCaptor<ListObjectsV2Request> listCaptor =
        ArgumentCaptor.forClass(ListObjectsV2Request.class);
    verify(s3Client, times(2)).listObjectsV2(listCaptor.capture());

    List<ListObjectsV2Request> listRequests = listCaptor.getAllValues();

    assertEquals("", listRequests.get(0).prefix());
    assertNull(listRequests.get(0).continuationToken());

    assertEquals("", listRequests.get(1).prefix());
    assertEquals("TOKEN-1", listRequests.get(1).continuationToken());

    ArgumentCaptor<CopyObjectRequest> copyCaptor =
        ArgumentCaptor.forClass(CopyObjectRequest.class);
    verify(s3Client, times(2)).copyObject(copyCaptor.capture());

    List<CopyObjectRequest> copyRequests = copyCaptor.getAllValues();

    assertEquals("first.json", copyRequests.get(0).destinationKey());
    assertEquals("second.json", copyRequests.get(1).destinationKey());
  }

  @Test
  void copyBucketContentsThrowsS3ExceptionWhenNoFilesFound() {
    given(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
        .willReturn(ListObjectsV2Response.builder()
            .contents(List.of())
            .build());

    S3Exception exception = assertThrows(
        S3Exception.class,
        () -> service.copyBucketContents(
            "source-bucket",
            "missing",
            "destination-bucket",
            "destination"
        )
    );

    assertTrue(exception.getMessage().contains("No files found under missing/"));
  }

  private ResponseInputStream<GetObjectResponse> responseInputStream(String json) {
    return new ResponseInputStream<>(
        GetObjectResponse.builder().build(),
        AbortableInputStream.create(new ByteArrayInputStream(json.getBytes(UTF_8)))
    );
  }

  private ResponseInputStream<GetObjectResponse> responseInputStreamThatFailsOnRead() {
    InputStream failingInputStream = new InputStream() {
      @Override
      public int read() throws IOException {
        throw new IOException("read failed");
      }
    };

    return new ResponseInputStream<>(
        GetObjectResponse.builder().build(),
        AbortableInputStream.create(failingInputStream)
    );
  }

  private static class JsonBreakingValue {
    public String getValue() {
      throw new IllegalStateException("Cannot serialize this value");
    }
  }
}