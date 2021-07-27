/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

package org.tbbtalent.server.service.db.impl;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.configuration.GoogleDriveConfig;
import org.tbbtalent.server.service.db.FileSystemService;
import org.tbbtalent.server.util.filesystem.GoogleFileSystemDrive;
import org.tbbtalent.server.util.filesystem.GoogleFileSystemFile;
import org.tbbtalent.server.util.filesystem.GoogleFileSystemFolder;


@Service
public class GoogleFileSystemServiceImpl implements FileSystemService {
    private static final Logger log = LoggerFactory.getLogger(GoogleFileSystemServiceImpl.class);
    
    private static final String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";
    
    private final Drive googleDriveService;
    private final GoogleDriveConfig googleDriveConfig;

    @Autowired
    public GoogleFileSystemServiceImpl(GoogleDriveConfig googleDriveConfig)
        throws GeneralSecurityException, IOException {
        this.googleDriveConfig = googleDriveConfig;
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
                .setQ("name='" + folderName + "'" +
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

    @Override
    public @NonNull
    GoogleFileSystemFolder createFolder(
        GoogleFileSystemDrive drive, GoogleFileSystemFolder parentFolder, String folderName) 
        throws IOException {
        //See https://developers.google.com/drive/api/v3/folder
        //and https://developers.google.com/drive/api/v3/enable-shareddrives 
        File fileMetadata = new File();
        fileMetadata.setDriveId(drive.getId());
        fileMetadata.setParents(Collections.singletonList(parentFolder.getId()));
        fileMetadata.setName(folderName);
        fileMetadata.setMimeType(FOLDER_MIME_TYPE);
        File file = googleDriveService.files().create(fileMetadata)
                .setSupportsAllDrives(true)
                .setFields("id,webViewLink")
                .execute();
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
    
}
