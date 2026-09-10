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

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.CandidateAttachment;
import org.tctalent.server.service.db.CandidateAttachmentService;

@ExtendWith(MockitoExtension.class)
class AttachmentAccessServiceTest {

  private static final String PUBLIC_ID = "public-attachment-id";
  private static final String FILENAME = "passport.pdf";
  private static final long EXPIRES_AT = 1_800_000_000L;
  private static final String TOKEN = "valid-share-token";

  @Mock
  private CandidateAttachmentService candidateAttachmentService;

  @Mock
  private AttachmentAuthorizationService attachmentAuthorizationService;

  @Mock
  private FileUrlService fileUrlService;

  @Mock
  private FileShareTokenService fileShareTokenService;

  @Mock
  private CandidateAttachment attachment;

  @Mock
  private FinalFileAccessUrl accessUrl;

  private AttachmentAccessService service;

  @BeforeEach
  void setUp() {
    service = new AttachmentAccessService(
        candidateAttachmentService,
        attachmentAuthorizationService,
        fileUrlService,
        fileShareTokenService
    );
  }

  @Test
  void resolveAccessUrlReturnsPublicUrlWithoutAuthorization() throws Exception {
    mockActiveAttachment(UploadType.cv);
    when(fileUrlService.createAccessUrl(attachment)).thenReturn(accessUrl);

    FinalFileAccessUrl result =
        service.resolveAccessUrl(PUBLIC_ID, null, null, null);

    assertSame(accessUrl, result);

    verify(fileUrlService).createAccessUrl(attachment);
    verifyNoInteractions(
        attachmentAuthorizationService,
        fileShareTokenService
    );
  }

  @Test
  void resolveAccessUrlAcceptsBlankRequestedFilename() throws Exception {
    mockActiveAttachment(UploadType.cv);
    when(fileUrlService.createAccessUrl(attachment)).thenReturn(accessUrl);

    FinalFileAccessUrl result =
        service.resolveAccessUrl(PUBLIC_ID, "   ", null, null);

    assertSame(accessUrl, result);

    verify(fileUrlService).createAccessUrl(attachment);
    verify(attachment, never()).getName();
    verifyNoInteractions(
        attachmentAuthorizationService,
        fileShareTokenService
    );
  }

  @Test
  void resolveAccessUrlUsesShareTokenForSignedAttachment() throws Exception {
    mockActiveAttachment(UploadType.passport);
    when(attachment.getName()).thenReturn(FILENAME);
    when(fileUrlService.createAccessUrl(attachment)).thenReturn(accessUrl);

    FinalFileAccessUrl result = service.resolveAccessUrl(
        PUBLIC_ID,
        FILENAME,
        EXPIRES_AT,
        TOKEN
    );

    assertSame(accessUrl, result);

    verify(fileShareTokenService).validateToken(
        PUBLIC_ID,
        FILENAME,
        EXPIRES_AT,
        TOKEN
    );
    verify(fileUrlService).createAccessUrl(attachment);
    verifyNoInteractions(attachmentAuthorizationService);
  }

  @Test
  void resolveAccessUrlUsesLoggedInAuthorizationWhenExpiryIsMissing()
      throws Exception {

    mockActiveAttachment(UploadType.passport);
    when(fileUrlService.createAccessUrl(attachment)).thenReturn(accessUrl);

    FinalFileAccessUrl result =
        service.resolveAccessUrl(PUBLIC_ID, null, null, TOKEN);

    assertSame(accessUrl, result);

    verify(attachmentAuthorizationService)
        .assertCurrentUserCanAccess(attachment);
    verify(fileUrlService).createAccessUrl(attachment);
    verifyNoInteractions(fileShareTokenService);
  }

  @Test
  void resolveAccessUrlUsesLoggedInAuthorizationWhenTokenIsMissing()
      throws Exception {

    mockActiveAttachment(UploadType.passport);
    when(fileUrlService.createAccessUrl(attachment)).thenReturn(accessUrl);

    FinalFileAccessUrl result =
        service.resolveAccessUrl(PUBLIC_ID, null, EXPIRES_AT, null);

    assertSame(accessUrl, result);

    verify(attachmentAuthorizationService)
        .assertCurrentUserCanAccess(attachment);
    verify(fileUrlService).createAccessUrl(attachment);
    verifyNoInteractions(fileShareTokenService);
  }

  @Test
  void resolveAccessUrlThrowsWhenAttachmentIsInactive() throws Exception {
    when(candidateAttachmentService.getCandidateAttachmentByPublicId(PUBLIC_ID))
        .thenReturn(attachment);
    when(attachment.isActive()).thenReturn(false);

    assertThrows(
        NoSuchObjectException.class,
        () -> service.resolveAccessUrl(
            PUBLIC_ID,
            FILENAME,
            EXPIRES_AT,
            TOKEN
        )
    );

    verifyNoInteractions(
        fileUrlService,
        fileShareTokenService,
        attachmentAuthorizationService
    );
  }

  @Test
  void resolveAccessUrlThrowsWhenRequestedFilenameDoesNotMatch()
      throws Exception {

    when(candidateAttachmentService.getCandidateAttachmentByPublicId(PUBLIC_ID))
        .thenReturn(attachment);
    when(attachment.isActive()).thenReturn(true);
    when(attachment.getName()).thenReturn("actual-passport.pdf");
    when(attachment.getId()).thenReturn(25L);

    assertThrows(
        NoSuchObjectException.class,
        () -> service.resolveAccessUrl(
            PUBLIC_ID,
            "incorrect-passport.pdf",
            EXPIRES_AT,
            TOKEN
        )
    );

    verifyNoInteractions(
        fileUrlService,
        fileShareTokenService,
        attachmentAuthorizationService
    );
  }

  @Test
  void resolveAccessUrlAllowsRequestWhenStoredFilenameIsNull()
      throws Exception {

    mockActiveAttachment(UploadType.cv);
    when(attachment.getName()).thenReturn(null);
    when(fileUrlService.createAccessUrl(attachment)).thenReturn(accessUrl);

    FinalFileAccessUrl result = service.resolveAccessUrl(
        PUBLIC_ID,
        "requested-file.pdf",
        null,
        null
    );

    assertSame(accessUrl, result);

    verify(fileUrlService).createAccessUrl(attachment);
    verifyNoInteractions(
        fileShareTokenService,
        attachmentAuthorizationService
    );
  }

  private void mockActiveAttachment(UploadType uploadType) throws Exception {
    when(candidateAttachmentService.getCandidateAttachmentByPublicId(PUBLIC_ID))
        .thenReturn(attachment);
    when(attachment.isActive()).thenReturn(true);
    when(attachment.getUploadType()).thenReturn(uploadType);
  }
}