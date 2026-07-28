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

package org.tctalent.server.files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static software.amazon.awssdk.utils.http.SdkHttpUtils.urlEncode;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPairGenerator;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.tctalent.server.configuration.properties.CandidateFileUrlsProperties;
import org.tctalent.server.model.db.CandidateAttachment;
import software.amazon.awssdk.services.cloudfront.CloudFrontUtilities;
import software.amazon.awssdk.services.cloudfront.model.CannedSignerRequest;
import software.amazon.awssdk.services.cloudfront.url.SignedUrl;

@ExtendWith(MockitoExtension.class)
class DefaultFileUrlServiceTest {

  private static final String PUBLIC_BASE_URL = "https://app.example.com/";
  private static final String ORIGIN_BASE_URL = "https://cdn.example.com/";
  private static final String KEY_PAIR_ID = "cloudfront-key-id";
  @TempDir
  Path tempDir;
  @Mock
  private FileShareTokenService fileShareTokenService;

  @Mock
  private CloudFrontUtilities cloudFrontUtilities;

  private CandidateFileUrlsProperties properties;
  private DefaultFileUrlService service;

  @BeforeEach
  void setUp() throws Exception {
    properties = new CandidateFileUrlsProperties();
    properties.setPublicBaseUrl(PUBLIC_BASE_URL);
    properties.setOriginBaseUrl(ORIGIN_BASE_URL);
    properties.setCloudfrontPrivateKeyPemPath(createTemporaryPrivateKey().toString());
    properties.setCloudfrontKeyPairId(KEY_PAIR_ID);
    properties.setOriginExpiryMinutes(15);

    service = new DefaultFileUrlService(fileShareTokenService, properties);

    ReflectionTestUtils.setField(service, "cloudFrontUtilities", cloudFrontUtilities);
  }

  @Test
  void createApplicationUrlNormalizesBaseUrlAndFilename() {
    CandidateAttachment attachment = attachment(UploadType.cv, "public-123",
        " folder\\candidate/cv.pdf ", "/candidate/file.pdf");

    String result = service.createApplicationUrl(attachment);

    assertEquals("https://app.example.com/files/public-123/folder_candidate_cv.pdf", result);
  }

  @Test
  void createExpiringApplicationUrlAddsExpiryAndToken() {
    CandidateAttachment attachment = attachment(UploadType.cv, "public-123", "candidate.pdf",
        "/candidate/file.pdf");

    Duration duration = Duration.ofMinutes(10);
    long earliestExpectedExpiry = Instant.now().plus(duration).minusSeconds(1).getEpochSecond();

    when(fileShareTokenService.createToken(eq("public-123"), eq("candidate.pdf"),
        anyLong())).thenReturn("share-token");

    String result = service.createExpiringApplicationUrl(attachment, duration);

    ArgumentCaptor<Long> expiryCaptor = ArgumentCaptor.forClass(Long.class);

    verify(fileShareTokenService).createToken(eq("public-123"), eq("candidate.pdf"),
        expiryCaptor.capture());

    long actualExpiry = expiryCaptor.getValue();
    long latestExpectedExpiry = Instant.now().plus(duration).plusSeconds(1).getEpochSecond();

    assertTrue(actualExpiry >= earliestExpectedExpiry);
    assertTrue(actualExpiry <= latestExpectedExpiry);
    assertEquals("https://app.example.com/files/public-123/candidate.pdf" + "?e=" + actualExpiry
        + "&t=share-token", result);
  }

  @Test
  void createObjectUrlCreatesInlineContentDisposition() {
    CandidateAttachment attachment = attachment(UploadType.cv, "public-123",
        "folder/candidate cv.pdf", "/candidate/file.pdf");

    String disposition = "inline; filename=\"folder_candidate cv.pdf\"";

    String result = service.createObjectUrl(attachment);

    assertEquals(
        "https://cdn.example.com/candidate/file.pdf" + "?response-content-disposition=" + urlEncode(
            disposition), result);

    verifyNoInteractions(fileShareTokenService);
  }

  @Test
  void createObjectUrlSupportsAttachmentContentDisposition() {
    CandidateAttachment attachment = attachment(UploadType.cv, "public-123", "candidate.pdf",
        "candidate/file.pdf");

    String result = ReflectionTestUtils.invokeMethod(service, "createObjectUrl", attachment, false);

    String disposition = "attachment; filename=\"candidate.pdf\"";

    assertEquals(
        "https://cdn.example.com/candidate/file.pdf" + "?response-content-disposition=" + urlEncode(
            disposition), result);
  }

  @Test
  void createSignedObjectUrlReturnsCloudFrontSignedUrl() throws Exception {
    CandidateAttachment attachment = attachment(UploadType.passport, "public-123", "passport.pdf",
        "/candidate/passport.pdf");

    SignedUrl signedUrl = mock(SignedUrl.class);
    when(signedUrl.url()).thenReturn("https://cdn.example.com/signed-passport-url");
    when(cloudFrontUtilities.getSignedUrlWithCannedPolicy(
        any(CannedSignerRequest.class))).thenReturn(signedUrl);

    Instant before = Instant.now().plus(Duration.ofMinutes(15)).minusSeconds(1);

    String result = service.createSignedObjectUrl(attachment, Duration.ofMinutes(15));

    Instant after = Instant.now().plus(Duration.ofMinutes(15)).plusSeconds(1);

    ArgumentCaptor<CannedSignerRequest> requestCaptor = ArgumentCaptor.forClass(
        CannedSignerRequest.class);

    verify(cloudFrontUtilities).getSignedUrlWithCannedPolicy(requestCaptor.capture());

    CannedSignerRequest request = requestCaptor.getValue();

    assertEquals("https://cdn.example.com/signed-passport-url", result);
    assertTrue(request.resourceUrl().startsWith("https://cdn.example.com/candidate/passport.pdf"));
    assertEquals(KEY_PAIR_ID, request.keyPairId());
    assertFalse(request.expirationDate().isBefore(before));
    assertFalse(request.expirationDate().isAfter(after));
  }

  @Test
  void createAccessUrlReturnsUnsignedUrlForPublicAttachment() throws Exception {

    CandidateAttachment attachment = attachment(UploadType.cv, "public-123", "candidate.pdf",
        "/candidate/file.pdf");

    FinalFileAccessUrl result = service.createAccessUrl(attachment);

    assertEquals(
        "https://cdn.example.com/candidate/file.pdf" + "?response-content-disposition=" + urlEncode(
            "inline; filename=\"candidate.pdf\""), result.getUrl());
    assertFalse(result.isSigned());
    assertNull(result.getExpiresAt());

    verifyNoInteractions(fileShareTokenService, cloudFrontUtilities);
  }

  @Test
  void createAccessUrlReturnsSignedUrlForSensitiveAttachment() throws Exception {

    CandidateAttachment attachment = attachment(UploadType.passport, "public-123", "passport.pdf",
        "/candidate/passport.pdf");

    SignedUrl signedUrl = mock(SignedUrl.class);
    when(signedUrl.url()).thenReturn("https://cdn.example.com/signed-passport-url");
    when(cloudFrontUtilities.getSignedUrlWithCannedPolicy(
        any(CannedSignerRequest.class))).thenReturn(signedUrl);
    Instant before = Instant.now().plusSeconds(899);

    FinalFileAccessUrl result = service.createAccessUrl(attachment);

    Instant after = Instant.now().plusSeconds(901);

    assertEquals("https://cdn.example.com/signed-passport-url", result.getUrl());
    assertTrue(result.isSigned());
    assertNotNull(result.getExpiresAt());
    assertFalse(result.getExpiresAt().isBefore(before));
    assertFalse(result.getExpiresAt().isAfter(after));
  }

  @Test
  void createApplicationUrlThrowsWhenPublicIdIsNull() {
    CandidateAttachment attachment = attachment(UploadType.cv, null, "candidate.pdf",
        "/candidate/file.pdf");

    IllegalStateException exception = assertThrows(IllegalStateException.class,
        () -> service.createApplicationUrl(attachment));

    assertEquals("Attachment has no public id", exception.getMessage());
  }

  @Test
  void createApplicationUrlThrowsWhenFilenameIsNull() {
    CandidateAttachment attachment = attachment(UploadType.cv, "public-123", null,
        "/candidate/file.pdf");
    attachment.setId(25L);

    IllegalStateException exception = assertThrows(IllegalStateException.class,
        () -> service.createApplicationUrl(attachment));

    assertEquals("Attachment 25 has no filename", exception.getMessage());
  }

  @Test
  void createApplicationUrlThrowsWhenFilenameIsBlank() {
    CandidateAttachment attachment = attachment(UploadType.cv, "public-123", "   ",
        "/candidate/file.pdf");
    attachment.setId(25L);

    IllegalStateException exception = assertThrows(IllegalStateException.class,
        () -> service.createApplicationUrl(attachment));

    assertEquals("Attachment 25 has no filename", exception.getMessage());
  }

  @Test
  void createObjectUrlThrowsWhenStorageKeyIsNull() {
    CandidateAttachment attachment = attachment(UploadType.cv, "public-123", "candidate.pdf", null);
    attachment.setId(25L);

    IllegalStateException exception = assertThrows(IllegalStateException.class,
        () -> service.createObjectUrl(attachment));

    assertEquals("Attachment 25 has no storageKey", exception.getMessage());
  }

  @Test
  void createObjectUrlThrowsWhenStorageKeyIsBlank() {
    CandidateAttachment attachment = attachment(UploadType.cv, "public-123", "candidate.pdf",
        "   ");
    attachment.setId(25L);

    IllegalStateException exception = assertThrows(IllegalStateException.class,
        () -> service.createObjectUrl(attachment));

    assertEquals("Attachment 25 has no storageKey", exception.getMessage());
  }

  @Test
  void createApplicationUrlThrowsWhenPublicBaseUrlIsNull() {
    properties.setPublicBaseUrl(null);

    CandidateAttachment attachment = attachment(UploadType.cv, "public-123", "candidate.pdf",
        "/candidate/file.pdf");

    IllegalStateException exception = assertThrows(IllegalStateException.class,
        () -> service.createApplicationUrl(attachment));

    assertEquals("Base URL is not configured", exception.getMessage());
  }

  @Test
  void createApplicationUrlThrowsWhenPublicBaseUrlIsBlank() {
    properties.setPublicBaseUrl("   ");

    CandidateAttachment attachment = attachment(UploadType.cv, "public-123", "candidate.pdf",
        "/candidate/file.pdf");

    IllegalStateException exception = assertThrows(IllegalStateException.class,
        () -> service.createApplicationUrl(attachment));

    assertEquals("Base URL is not configured", exception.getMessage());
  }

  @Test
  void createObjectUrlThrowsWhenOriginBaseUrlIsMissing() {
    properties.setOriginBaseUrl(null);

    CandidateAttachment attachment = attachment(UploadType.cv, "public-123", "candidate.pdf",
        "/candidate/file.pdf");

    IllegalStateException exception = assertThrows(IllegalStateException.class,
        () -> service.createObjectUrl(attachment));

    assertEquals("Base URL is not configured", exception.getMessage());
  }

  @Test
  void createObjectUrlHandlesBaseUrlWithoutTrailingSlash() {
    properties.setOriginBaseUrl("https://cdn.example.com");

    CandidateAttachment attachment = attachment(UploadType.cv, "public-123", "candidate.pdf",
        "candidate/file.pdf");

    String result = service.createObjectUrl(attachment);

    assertTrue(result.startsWith("https://cdn.example.com/candidate/file.pdf?"));
  }

  private CandidateAttachment attachment(UploadType uploadType, String publicId, String filename,
      String storageKey) {

    CandidateAttachment attachment = new CandidateAttachment();
    attachment.setUploadType(uploadType);
    attachment.setPublicId(publicId);
    attachment.setName(filename);
    attachment.setStorageKey(storageKey);

    return attachment;
  }

  private Path createTemporaryPrivateKey() throws Exception {
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");

    // The AWS builder only needs to parse the key because CloudFrontUtilities
    // itself is mocked.
    keyPairGenerator.initialize(1024);

    byte[] privateKeyBytes = keyPairGenerator.generateKeyPair().getPrivate().getEncoded();

    String encodedPrivateKey = Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.UTF_8))
        .encodeToString(privateKeyBytes);

    String pem = """
        -----BEGIN PRIVATE KEY-----
        %s
        -----END PRIVATE KEY-----
        """.formatted(encodedPrivateKey);

    Path privateKeyPath = tempDir.resolve("cloudfront-private-key.pem");

    Files.writeString(privateKeyPath, pem, StandardCharsets.UTF_8);

    return privateKeyPath;
  }
}