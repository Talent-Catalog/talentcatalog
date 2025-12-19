package org.tctalent.server.service.db.impl;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import org.tctalent.server.configuration.GoogleDriveConfig;
import org.tctalent.server.util.filesystem.GoogleFileSystemDrive;
import org.tctalent.server.util.filesystem.GoogleFileSystemFile;
import org.tctalent.server.util.filesystem.GoogleFileSystemFolder;

import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GoogleFileSystemServiceImplTest {

  @Mock
  private GoogleDriveConfig googleDriveConfig;

  @Mock
  private Drive googleDriveService;

  @Mock
  private Drive.Files driveFiles;

  @Mock
  private Drive.Files.List filesList;

  @Mock
  private Drive.Files.Create filesCreate;

  @Mock
  private Drive.Files.Get filesGet;

  @Mock
  private Drive.Files.Delete filesDelete;

  @Mock
  private Drive.Files.Update filesUpdate;

  @Mock
  private Drive.Files.Copy filesCopy;

  @Mock
  private Drive.Permissions permissions;

  @Mock
  private Drive.Permissions.Create permissionsCreate;

  @InjectMocks
  private GoogleFileSystemServiceImpl service;

  private GoogleFileSystemDrive drive;
  private GoogleFileSystemFolder parentFolder;
  private GoogleFileSystemFile file;

  @BeforeEach
  void setUp() throws GeneralSecurityException, IOException {
    when(googleDriveConfig.getGoogleDriveService()).thenReturn(googleDriveService);
    when(googleDriveService.files()).thenReturn(driveFiles);

    ReflectionTestUtils.setField(service, "googleDriveService", googleDriveService);

    drive = new GoogleFileSystemDrive(null);
    drive.setId("driveId");

    parentFolder = new GoogleFileSystemFolder("parentUrl");
    parentFolder.setId("parentFolderId");

    file = new GoogleFileSystemFile("fileUrl");
    file.setId("fileId");
    file.setName("testFile");
  }

  @Test
  void testFindAFolder_found() throws IOException {
    FileList fileList = new FileList();
    File folder = new File();
    folder.setId("folderId");
    folder.setWebViewLink("folderUrl");
    fileList.setFiles(Collections.singletonList(folder));

    when(driveFiles.list()).thenReturn(filesList);
    when(filesList.setSupportsAllDrives(true)).thenReturn(filesList);
    when(filesList.setIncludeItemsFromAllDrives(true)).thenReturn(filesList);
    when(filesList.setCorpora("drive")).thenReturn(filesList);
    when(filesList.setDriveId("driveId")).thenReturn(filesList);
    when(filesList.setQ(anyString())).thenReturn(filesList);
    when(filesList.setPageSize(10)).thenReturn(filesList);
    when(filesList.setFields("nextPageToken, files(id,name,webViewLink)")).thenReturn(filesList);
    when(filesList.execute()).thenReturn(fileList);

    GoogleFileSystemFolder result = service.findAFolder(drive, parentFolder, "testFolder");

    assertNotNull(result);
    assertEquals("folderId", result.getId());
    assertEquals("testFolder", result.getName());
    assertEquals("folderUrl", result.getUrl());
  }

  @Test
  void testFindAFolder_notFound() throws IOException {
    FileList fileList = new FileList();
    fileList.setFiles(Collections.emptyList());

    when(driveFiles.list()).thenReturn(filesList);
    when(filesList.setSupportsAllDrives(true)).thenReturn(filesList);
    when(filesList.setIncludeItemsFromAllDrives(true)).thenReturn(filesList);
    when(filesList.setCorpora("drive")).thenReturn(filesList);
    when(filesList.setDriveId("driveId")).thenReturn(filesList);
    when(filesList.setQ(anyString())).thenReturn(filesList);
    when(filesList.setPageSize(10)).thenReturn(filesList);
    when(filesList.setFields("nextPageToken, files(id,name,webViewLink)")).thenReturn(filesList);
    when(filesList.execute()).thenReturn(fileList);

    GoogleFileSystemFolder result = service.findAFolder(drive, parentFolder, "testFolder");

    assertNull(result);
  }

  @Test
  void testCreateFile() throws IOException {
    File createdFile = new File();
    createdFile.setId("newFileId");
    createdFile.setWebViewLink("newFileUrl");

    when(driveFiles.create(any(File.class))).thenReturn(filesCreate);
    when(filesCreate.setSupportsAllDrives(true)).thenReturn(filesCreate);
    when(filesCreate.setFields("id,webViewLink")).thenReturn(filesCreate);
    when(filesCreate.execute()).thenReturn(createdFile);

    GoogleFileSystemFile result = service.createFile(drive, parentFolder, "testFile", "text/plain");

    assertNotNull(result);
    assertEquals("newFileId", result.getId());
    assertEquals("testFile", result.getName());
    assertEquals("newFileUrl", result.getUrl());
  }

  @Test
  void testCreateFolder() throws IOException {
    File createdFolder = new File();
    createdFolder.setId("newFolderId");
    createdFolder.setWebViewLink("newFolderUrl");

    when(driveFiles.create(any(File.class))).thenReturn(filesCreate);
    when(filesCreate.setSupportsAllDrives(true)).thenReturn(filesCreate);
    when(filesCreate.setFields("id,webViewLink")).thenReturn(filesCreate);
    when(filesCreate.execute()).thenReturn(createdFolder);

    GoogleFileSystemFolder result = service.createFolder(drive, parentFolder, "testFolder");

    assertNotNull(result);
    assertEquals("newFolderId", result.getId());
    assertEquals("testFolder", result.getName());
    assertEquals("newFolderUrl", result.getUrl());
  }

  @Test
  void testDeleteFile() throws IOException {
    when(driveFiles.delete("fileId")).thenReturn(filesDelete);
    when(filesDelete.setSupportsAllDrives(true)).thenReturn(filesDelete);
    doNothing().when(filesDelete).execute();

    service.deleteFile(file);

    verify(filesDelete).execute();
  }

  @Test
  void testDownloadFile() throws IOException {
    when(driveFiles.get("fileId")).thenReturn(filesGet);
    when(filesGet.setSupportsAllDrives(true)).thenReturn(filesGet);
    doNothing().when(filesGet).executeMediaAndDownloadTo(any(OutputStream.class));

    OutputStream out = mock(OutputStream.class);
    service.downloadFile(file, out);

    verify(filesGet).executeMediaAndDownloadTo(out);
  }

  @Test
  void testGetDriveFromEntity() throws IOException {
    File fileInfo = new File();
    fileInfo.setDriveId("driveId");

    when(driveFiles.get("fileId")).thenReturn(filesGet);
    when(filesGet.setSupportsAllDrives(true)).thenReturn(filesGet);
    when(filesGet.setFields("driveId")).thenReturn(filesGet);
    when(filesGet.execute()).thenReturn(fileInfo);

    GoogleFileSystemDrive result = service.getDriveFromEntity(file);

    assertNotNull(result);
    assertEquals("driveId", result.getId());
  }

  @Test
  void testMoveEntityToFolder() throws IOException {
    File fileInfo = new File();
    fileInfo.setParents(Collections.singletonList("oldParentId"));

    when(driveFiles.get("fileId")).thenReturn(filesGet);
    when(filesGet.setSupportsAllDrives(true)).thenReturn(filesGet);
    when(filesGet.setFields("parents")).thenReturn(filesGet);
    when(filesGet.execute()).thenReturn(fileInfo);

    when(driveFiles.update(eq("fileId"), isNull())).thenReturn(filesUpdate);
    when(filesUpdate.setSupportsAllDrives(true)).thenReturn(filesUpdate);
    when(filesUpdate.setAddParents("parentFolderId")).thenReturn(filesUpdate);
    when(filesUpdate.setRemoveParents("oldParentId,")).thenReturn(filesUpdate);
    when(filesUpdate.setFields("id, parents")).thenReturn(filesUpdate);
    when(filesUpdate.execute()).thenReturn(new File());

    service.moveEntityToFolder(file, parentFolder);

    verify(filesUpdate).execute();
  }

  @Test
  void testPublishFile() throws IOException {
    when(googleDriveService.permissions()).thenReturn(permissions);
    when(permissions.create(eq("fileId"), any(Permission.class))).thenReturn(permissionsCreate);
    when(permissionsCreate.setSupportsAllDrives(true)).thenReturn(permissionsCreate);
    when(permissionsCreate.execute()).thenReturn(new Permission());

    service.publishFile(file);

    verify(permissionsCreate).execute();
  }

  @Test
  void testPublishFolder() throws IOException {
    when(googleDriveService.permissions()).thenReturn(permissions);
    when(permissions.create(eq("parentFolderId"), any(Permission.class))).thenReturn(permissionsCreate);
    when(permissionsCreate.setSupportsAllDrives(true)).thenReturn(permissionsCreate);
    when(permissionsCreate.execute()).thenReturn(new Permission());

    service.publishFolder(parentFolder);

    verify(permissionsCreate).execute();
  }

  @Test
  void testRenameFile() throws IOException {
    when(driveFiles.update(eq("fileId"), any(File.class))).thenReturn(filesUpdate);
    when(filesUpdate.setSupportsAllDrives(true)).thenReturn(filesUpdate);
    when(filesUpdate.setFields("id,name")).thenReturn(filesUpdate);
    when(filesUpdate.execute()).thenReturn(new File());

    service.renameFile(file);

    verify(filesUpdate).execute();
  }

  @Test
  void testUploadFile() throws IOException {
    File uploadedFile = new File();
    uploadedFile.setId("uploadedFileId");
    uploadedFile.setWebViewLink("uploadedFileUrl");

    when(driveFiles.create(any(File.class), any(FileContent.class))).thenReturn(filesCreate);
    when(filesCreate.setSupportsAllDrives(true)).thenReturn(filesCreate);
    when(filesCreate.setFields("id,webViewLink")).thenReturn(filesCreate);
    when(filesCreate.execute()).thenReturn(uploadedFile);

    java.io.File localFile = mock(java.io.File.class);
    GoogleFileSystemFile result = service.uploadFile(drive, parentFolder, "testFile", localFile);

    assertNotNull(result);
    assertEquals("uploadedFileId", result.getId());
    assertEquals("testFile", result.getName());
    assertEquals("uploadedFileUrl", result.getUrl());
  }

  @Test
  void testCopyFile() throws IOException {
    File copiedFile = new File();
    copiedFile.setId("copiedFileId");
    copiedFile.setWebViewLink("copiedFileUrl");

    when(driveFiles.copy(eq("fileId"), any(File.class))).thenReturn(filesCopy);
    when(filesCopy.setSupportsAllDrives(true)).thenReturn(filesCopy);
    when(filesCopy.setFields("id,webViewLink")).thenReturn(filesCopy);
    when(filesCopy.execute()).thenReturn(copiedFile);

    GoogleFileSystemFile result = service.copyFile(parentFolder, "copiedFile", file);

    assertNotNull(result);
    assertEquals("copiedFileId", result.getId());
    assertEquals("copiedFile", result.getName());
    assertEquals("copiedFileUrl", result.getUrl());
  }
}