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
package org.tctalent.server.service.db.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.tctalent.server.configuration.GoogleDriveConfig;
import org.tctalent.server.exception.CvGenerationException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.User;
import org.tctalent.server.service.db.FileSystemService;
import org.tctalent.server.util.filesystem.GoogleFileSystemDrive;
import org.tctalent.server.util.filesystem.GoogleFileSystemFile;
import org.tctalent.server.util.filesystem.GoogleFileSystemFolder;

@ExtendWith(MockitoExtension.class)
class GoogleDocHelperTest {

  private static final String DOCX_MIME_TYPE =
      "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

  private static final String GOOGLE_DOC_MIME_TYPE =
      "application/vnd.google-apps.document";

  @Mock
  private DocxHelper docxHelper;

  @Mock
  private FileSystemService fileSystemService;

  @Mock
  private GoogleDriveConfig googleDriveConfig;

  private GoogleDocHelper helper;

  @BeforeEach
  void setUp() {
    helper = new GoogleDocHelper(docxHelper, fileSystemService, googleDriveConfig);
  }

  @Test
  void generateGoogleDocCreatesPublishesAndReturnsGoogleDocUrlUsingCandidateFolder()
      throws Exception {
    Candidate candidate = candidateWithUser(
        42L,
        "https://drive.google.com/drive/folders/1234567890123456789012345?usp=sharing",
        "Ehsan Ehrari"
    );
    Resource docxResource = new ByteArrayResource("docx bytes".getBytes(StandardCharsets.UTF_8));
    GoogleFileSystemDrive drive = new GoogleFileSystemDrive(null);
    GoogleFileSystemFile googleDoc =
        new GoogleFileSystemFile("https://docs.google.com/document/d/google-doc-id/edit");

    when(docxHelper.generateDocx(candidate, true, false)).thenReturn(docxResource);
    when(fileSystemService.getDriveFromEntity(any(GoogleFileSystemFolder.class)))
        .thenReturn(drive);
    when(fileSystemService.uploadFileWithConversion(
        eq(drive),
        any(GoogleFileSystemFolder.class),
        eq("Ehsan Ehrari-CV"),
        eq(docxResource),
        eq(DOCX_MIME_TYPE),
        eq(GOOGLE_DOC_MIME_TYPE)
    )).thenReturn(googleDoc);

    Resource result = helper.generateGoogleDoc(candidate, true, false);

    assertEquals(
        "https://docs.google.com/document/d/google-doc-id/edit",
        new String(result.getInputStream().readAllBytes(), StandardCharsets.UTF_8)
    );

    ArgumentCaptor<GoogleFileSystemFolder> folderCaptor =
        ArgumentCaptor.forClass(GoogleFileSystemFolder.class);

    verify(fileSystemService).getDriveFromEntity(folderCaptor.capture());

    GoogleFileSystemFolder resolvedFolder = folderCaptor.getValue();
    assertEquals("1234567890123456789012345", resolvedFolder.getId());

    verify(fileSystemService).uploadFileWithConversion(
        drive,
        resolvedFolder,
        "Ehsan Ehrari-CV",
        docxResource,
        DOCX_MIME_TYPE,
        GOOGLE_DOC_MIME_TYPE
    );
    verify(fileSystemService).publishFile(googleDoc);
  }

  @Test
  void generateGoogleDocUsesRootFolderWhenCandidateFolderLinkIsBlank() throws Exception {
    Candidate candidate = candidateWithUser(43L, "   ", "Jane Smith");
    Resource docxResource = new ByteArrayResource("docx bytes".getBytes(StandardCharsets.UTF_8));
    GoogleFileSystemFolder rootFolder = new GoogleFileSystemFolder(null);
    rootFolder.setId("root-folder-id");
    GoogleFileSystemDrive drive = new GoogleFileSystemDrive(null);
    GoogleFileSystemFile googleDoc =
        new GoogleFileSystemFile("https://docs.google.com/document/d/root-doc-id/edit");

    when(docxHelper.generateDocx(candidate, false, true)).thenReturn(docxResource);
    when(googleDriveConfig.getCandidateRootFolder()).thenReturn(rootFolder);
    when(fileSystemService.getDriveFromEntity(rootFolder)).thenReturn(drive);
    when(fileSystemService.uploadFileWithConversion(
        drive,
        rootFolder,
        "Jane Smith-CV",
        docxResource,
        DOCX_MIME_TYPE,
        GOOGLE_DOC_MIME_TYPE
    )).thenReturn(googleDoc);

    Resource result = helper.generateGoogleDoc(candidate, false, true);

    assertEquals(
        "https://docs.google.com/document/d/root-doc-id/edit",
        new String(result.getInputStream().readAllBytes(), StandardCharsets.UTF_8)
    );

    verify(googleDriveConfig).getCandidateRootFolder();
    verify(fileSystemService).getDriveFromEntity(rootFolder);
    verify(fileSystemService).publishFile(googleDoc);
  }

  @Test
  void generateGoogleDocUsesRootFolderWhenCandidateFolderLinkHasNoGoogleId() throws Exception {
    Candidate candidate = candidateWithUser(44L, "https://example.com/not-google-folder", null);
    Resource docxResource = new ByteArrayResource("docx bytes".getBytes(StandardCharsets.UTF_8));
    GoogleFileSystemFolder rootFolder = new GoogleFileSystemFolder(null);
    rootFolder.setId("root-folder-id");
    GoogleFileSystemDrive drive = new GoogleFileSystemDrive(null);
    GoogleFileSystemFile googleDoc =
        new GoogleFileSystemFile("https://docs.google.com/document/d/root-doc-id/edit");

    when(docxHelper.generateDocx(candidate, true, true)).thenReturn(docxResource);
    when(googleDriveConfig.getCandidateRootFolder()).thenReturn(rootFolder);
    when(fileSystemService.getDriveFromEntity(rootFolder)).thenReturn(drive);
    when(fileSystemService.uploadFileWithConversion(
        drive,
        rootFolder,
        "Candidate-44-CV",
        docxResource,
        DOCX_MIME_TYPE,
        GOOGLE_DOC_MIME_TYPE
    )).thenReturn(googleDoc);

    Resource result = helper.generateGoogleDoc(candidate, true, true);

    assertEquals(
        "https://docs.google.com/document/d/root-doc-id/edit",
        new String(result.getInputStream().readAllBytes(), StandardCharsets.UTF_8)
    );

    verify(googleDriveConfig).getCandidateRootFolder();
    verify(fileSystemService).uploadFileWithConversion(
        drive,
        rootFolder,
        "Candidate-44-CV",
        docxResource,
        DOCX_MIME_TYPE,
        GOOGLE_DOC_MIME_TYPE
    );
    verify(fileSystemService).publishFile(googleDoc);
  }

  @Test
  void generateGoogleDocWrapsFailuresInCvGenerationException() {
    Candidate candidate = candidateWithUser(45L, null, "Error Candidate");

    when(docxHelper.generateDocx(candidate, true, true))
        .thenThrow(new RuntimeException("docx failed"));

    CvGenerationException exception = assertThrows(
        CvGenerationException.class,
        () -> helper.generateGoogleDoc(candidate, true, true)
    );

    assertEquals("docx failed", exception.getMessage());
  }

  @Test
  void resolveGoogleDocExportFolderReturnsCandidateFolderWhenLinkContainsGoogleId()
      throws Exception {
    Candidate candidate = candidateWithUser(
        46L,
        "https://drive.google.com/drive/folders/abcdefghijklmnopqrstuvwxyz123?usp=sharing",
        "Candidate"
    );

    GoogleFileSystemFolder result = invokeFolder(
        new Class<?>[] {Candidate.class},
        candidate
    );

    assertEquals("abcdefghijklmnopqrstuvwxyz123", result.getId());
  }

  @Test
  void resolveGoogleDocExportFolderReturnsRootFolderWhenFolderLinkIsBlankOrInvalid()
      throws Exception {
    GoogleFileSystemFolder rootFolder = new GoogleFileSystemFolder(null);
    rootFolder.setId("root-folder-id");

    when(googleDriveConfig.getCandidateRootFolder()).thenReturn(rootFolder);

    Candidate blankFolderCandidate = candidateWithUser(47L, " ", "Blank Folder");
    Candidate invalidFolderCandidate =
        candidateWithUser(48L, "https://example.com/not-google-folder", "Invalid Folder");

    assertSame(
        rootFolder,
        invokeFolder(
            new Class<?>[] {Candidate.class},
            blankFolderCandidate
        )
    );
    assertSame(
        rootFolder,
        invokeFolder(
            new Class<?>[] {Candidate.class},
            invalidFolderCandidate
        )
    );
  }

  @Test
  void buildGoogleDocNameUsesDisplayNameAndReplacesUnsafeFileNameCharacters() throws Exception {
    Candidate candidate = candidateWithUser(
        49L,
        null,
        "Ehsan / Ehrari: CV*Name? \"Bad\" <Test>|"
    );

    assertEquals(
        "Ehsan - Ehrari- CV-Name- -Bad- -Test---CV",
        invokeString(new Class<?>[] {Candidate.class}, candidate)
    );
  }

  @Test
  void buildGoogleDocNameFallsBackToCandidateIdWhenUserIsNullOrDisplayNameIsBlank()
      throws Exception {
    Candidate noUserCandidate = candidateWithUser(50L, null, null);
    Candidate blankDisplayNameCandidate = candidateWithUser(51L, null, "   ");

    assertEquals(
        "Candidate-50-CV",
        invokeString(new Class<?>[] {Candidate.class}, noUserCandidate)
    );
    assertEquals(
        "Candidate-51-CV",
        invokeString(new Class<?>[] {Candidate.class}, blankDisplayNameCandidate)
    );
  }

  private Candidate candidateWithUser(Long id, String folderLink, String displayName) {
    Candidate candidate = new Candidate();
    candidate.setId(id);
    candidate.setFolderlink(folderLink);

    if (displayName != null) {
      User user = new User();
      user.setFirstName(displayName);
      candidate.setUser(user);
    }

    return candidate;
  }

  private GoogleFileSystemFolder invokeFolder(
      Class<?>[] parameterTypes,
      Object... args
  ) throws Exception {
    return (GoogleFileSystemFolder) invoke("resolveGoogleDocExportFolder", parameterTypes, args);
  }

  private String invokeString(Class<?>[] parameterTypes, Object... args)
      throws Exception {
    Object result = invoke("buildGoogleDocName", parameterTypes, args);
    return result == null ? null : (String) result;
  }

  private Object invoke(String methodName, Class<?>[] parameterTypes, Object... args)
      throws Exception {
    Method method = GoogleDocHelper.class.getDeclaredMethod(methodName, parameterTypes);
    method.setAccessible(true);

    try {
      return method.invoke(helper, args);
    } catch (InvocationTargetException e) {
      if (e.getCause() instanceof RuntimeException runtimeException) {
        throw runtimeException;
      }
      throw e;
    }
  }
}