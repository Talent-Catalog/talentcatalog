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

package org.tctalent.server.service.db.impl;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.tctalent.server.configuration.GoogleDriveConfig;
import org.tctalent.server.service.db.FileSystemService;
import org.tctalent.server.util.filesystem.GoogleFileSystemBaseEntity;
import org.tctalent.server.util.filesystem.GoogleFileSystemDrive;
import org.tctalent.server.util.filesystem.GoogleFileSystemFile;
import org.tctalent.server.util.filesystem.GoogleFileSystemFolder;


@Service
@Slf4j
public class GoogleFileSystemServiceImpl implements FileSystemService {

    private static final String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";

    private final Drive googleDriveService;

    @Autowired
    public GoogleFileSystemServiceImpl(GoogleDriveConfig googleDriveConfig)
        throws GeneralSecurityException, IOException {
        this.googleDriveService = googleDriveConfig.getGoogleDriveService();
    }

    @Override
    public GoogleFileSystemFolder findAFolder(
        GoogleFileSystemDrive drive, GoogleFileSystemFolder parentFolder, String folderName)
        throws IOException {
        //See https://developers.google.com/drive/api/v3/search-files
        // and https://developers.google.com/drive/api/v3/enable-shareddrives
        // Search for CandidateData drive.
        FileList result = googleDriveService.files().list()
                .setSupportsAllDrives(true)
                .setIncludeItemsFromAllDrives(true)
                .setCorpora("drive")
                .setDriveId(drive.getId())

                //Escape out any quotes in the folderName
                .setQ("name='" + folderName.replace("'", "\\'") + "'" +
                      " and '" + parentFolder.getId() + "' in parents" +
                      " and mimeType='" + FOLDER_MIME_TYPE + "'")
                .setPageSize(10)
                .setFields("nextPageToken, files(id,name,webViewLink)")
                .execute();
        List<File> folders = result.getFiles();
        GoogleFileSystemFolder folder = null;
        if (folders != null && !folders.isEmpty()) {
            File file = folders.get(0);
            folder = new GoogleFileSystemFolder(file.getWebViewLink());
            folder.setId(file.getId());
            folder.setName(folderName);
        }
        return folder;
    }

    private File createFileInternal(GoogleFileSystemDrive drive,
        GoogleFileSystemFolder parentFolder, String fileName, String mimeType) throws IOException {
        //See https://developers.google.com/drive/api/v3/folder
        //and https://developers.google.com/drive/api/v3/enable-shareddrives
        File fileMetadata = new File();
        fileMetadata.setDriveId(drive.getId());
        fileMetadata.setParents(Collections.singletonList(parentFolder.getId()));
        fileMetadata.setName(fileName);
        fileMetadata.setMimeType(mimeType);
        return googleDriveService.files().create(fileMetadata)
            .setSupportsAllDrives(true)
            .setFields("id,webViewLink")
            .execute();
    }

    @Override
    public @NonNull
    GoogleFileSystemFile createFile(GoogleFileSystemDrive drive,
        GoogleFileSystemFolder parentFolder, String fileName, String mimeType) throws IOException {
        File file = createFileInternal(drive, parentFolder, fileName, mimeType);

        GoogleFileSystemFile googleFileSystemFile = new GoogleFileSystemFile(file.getWebViewLink());
        googleFileSystemFile.setId(file.getId());
        googleFileSystemFile.setName(fileName);
        return googleFileSystemFile;
    }

    @Override
    public @NonNull
    GoogleFileSystemFolder createFolder(
        GoogleFileSystemDrive drive, GoogleFileSystemFolder parentFolder, String folderName)
        throws IOException {
        File file = createFileInternal(drive, parentFolder, folderName, FOLDER_MIME_TYPE);

        GoogleFileSystemFolder folder = new GoogleFileSystemFolder(file.getWebViewLink());
        folder.setId(file.getId());
        folder.setName(folderName);
        return folder;
    }

    @Override
    public void deleteFile(GoogleFileSystemFile file) throws IOException {
        String id = file.getId();

        googleDriveService.files().delete(id)
                .setSupportsAllDrives(true)
                .execute();
    }

    @Override
    public void downloadFile(@NonNull GoogleFileSystemFile file,
                             @NonNull OutputStream out) throws IOException {
        String id = file.getId();

        //See https://developers.google.com/drive/api/v3/manage-downloads
        googleDriveService.files().get(id)
                .setSupportsAllDrives(true)
                .executeMediaAndDownloadTo(out);
    }

    @Override
    public GoogleFileSystemDrive getDriveFromEntity(GoogleFileSystemBaseEntity fileOrFolder) throws IOException {
        String id = fileOrFolder.getId();

        //https://developers.google.com/drive/api/guides/fields-parameter
        File fileInfo = googleDriveService.files().get(id)
            .setSupportsAllDrives(true)
            .setFields("driveId")
            .execute();

        //Return an object representing the drive associated with the file
        GoogleFileSystemDrive drive = new GoogleFileSystemDrive(null);
        drive.setId(fileInfo.getDriveId());

        return drive;
    }

    /*
     * See https://developers.google.com/drive/api/guides/folder#move_files_between_folders
     */
    @Override
    public void moveEntityToFolder(GoogleFileSystemBaseEntity fileOrFolder,
        GoogleFileSystemFolder parentFolder) throws IOException {
        String entityId = fileOrFolder.getId();
        String folderId = parentFolder.getId();

        // Retrieve the existing parents to remove
        File file = googleDriveService.files().get(entityId)
            .setSupportsAllDrives(true)
            .setFields("parents")
            .execute();
        StringBuilder previousParents = new StringBuilder();
        for (String parent : file.getParents()) {
            previousParents.append(parent);
            previousParents.append(',');
        }

        // Move the entity to the new folder
        googleDriveService.files().update(entityId, null)
            .setSupportsAllDrives(true)
            .setAddParents(folderId)
            .setRemoveParents(previousParents.toString())
            .setFields("id, parents")
            .execute();
    }

    private void publishFileOrFolder(@NonNull GoogleFileSystemBaseEntity fileOrFolder)
        throws IOException {
        String id = fileOrFolder.getId();

        Permission anyoneReadPermission = new Permission()
            .setType("anyone")
            .setRole("reader");

        googleDriveService.permissions().create(id, anyoneReadPermission)
            .setSupportsAllDrives(true)
            .execute();
    }

    @Override
    public void publishFile(@NonNull GoogleFileSystemFile file) throws IOException {
        publishFileOrFolder(file);
    }

    @Override
    public void publishFolder(@NonNull GoogleFileSystemFolder folder) throws IOException {
        publishFileOrFolder(folder);
    }

    @Override
    public void renameFile(@NonNull GoogleFileSystemFile file)
            throws IOException {
        String id = file.getId();

        File targetFile = new File();
        targetFile.setName(file.getName());
        googleDriveService.files().update(id, targetFile)
                .setSupportsAllDrives(true)
                .setFields("id,name")
                .execute();
    }

    @Override
    public @NonNull
    GoogleFileSystemFile uploadFile(GoogleFileSystemDrive drive,
            @Nullable GoogleFileSystemFolder parentFolder,
            String fileName, java.io.File file)
            throws IOException {

        File fileMetadata = new File();

        //Set parent to given folder, or drive (root) if no folder given
        List<String> parent;
        if (parentFolder == null) {
            parent = Collections.singletonList(drive.getId());
        } else {
            parent = Collections.singletonList(parentFolder.getId());
        }
        fileMetadata.setParents(parent);

        fileMetadata.setName(fileName);

        FileContent mediaContent = new FileContent(null, file);

        //Upload file to Google.
        File uploadedfile = googleDriveService.files()
                .create(fileMetadata, mediaContent)
                .setSupportsAllDrives(true)
                .setFields("id,webViewLink")
                .execute();

        //Return an object representing the uploaded file
        GoogleFileSystemFile fsf = new GoogleFileSystemFile(uploadedfile.getWebViewLink());
        fsf.setId(uploadedfile.getId());
        fsf.setName(fileName);

        return fsf;
    }

    @Override
    public GoogleFileSystemFile copyFile(
        GoogleFileSystemFolder parentFolder, String name, GoogleFileSystemFile sourceFile)
        throws IOException {
        List<String> parent = Collections.singletonList(parentFolder.getId());
        File copyMetadata = new File();
        copyMetadata.setName(name);
        copyMetadata.setParents(parent);

        File copyFile = googleDriveService.files()
                .copy(sourceFile.getId(), copyMetadata)
                .setSupportsAllDrives(true)
                .setFields("id,webViewLink")
                .execute();

        GoogleFileSystemFile fsf = new GoogleFileSystemFile(copyFile.getWebViewLink());
        fsf.setId(copyFile.getId());
        fsf.setName(name);
        return fsf;
    }

}
