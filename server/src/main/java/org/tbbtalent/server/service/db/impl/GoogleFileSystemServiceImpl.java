/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.db.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.service.db.GoogleFileSystemService;
import org.tbbtalent.server.util.filesystem.Folder;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

@Service
public class GoogleFileSystemServiceImpl implements GoogleFileSystemService {
    private static final Logger log = LoggerFactory.getLogger(GoogleFileSystemServiceImpl.class);
    
    private static final String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";

    @Value("${google.drive.candidateDataDriveId}")
    private String candidateDataDriveId;

    @Value("${google.drive.candidateRootFolderId}")
    private String candidateRootFolderId;
    
    private final Drive googleDriveService;

    @Autowired
    public GoogleFileSystemServiceImpl(Drive googleDriveService) {
        this.googleDriveService = googleDriveService;
    }

    @Override
    public Folder findAFolder(String folderName) throws IOException {
        //See https://developers.google.com/drive/api/v3/search-files
        // and https://developers.google.com/drive/api/v3/enable-shareddrives
        // Search for CandidateData drive.
        FileList result = googleDriveService.files().list()
                .setSupportsAllDrives(true)
                .setIncludeItemsFromAllDrives(true)
                .setCorpora("drive")
                .setDriveId(candidateDataDriveId)
                .setQ("name='" + folderName + "'" +
                      " and '" + candidateRootFolderId + "' in parents" +        
                      " and mimeType='" + FOLDER_MIME_TYPE + "'")
                .setPageSize(10)
                .setFields("nextPageToken, files(id,name,webViewLink)")
                .execute();
        List<File> folders = result.getFiles();
        Folder folder = null;
        if (folders != null && !folders.isEmpty()) {
            File file = folders.get(0);
            folder = new Folder();
            folder.setId(file.getId());
            folder.setName(folderName);
            folder.setUrl(file.getWebViewLink());
        }
        return folder;
    }

    @Override
    public @NonNull
    Folder createFolder(String folderName) throws IOException {
        //See https://developers.google.com/drive/api/v3/folder
        //and https://developers.google.com/drive/api/v3/enable-shareddrives 
        File fileMetadata = new File();
        fileMetadata.setDriveId(candidateDataDriveId);
        fileMetadata.setParents(Collections.singletonList(candidateRootFolderId));
        fileMetadata.setName(folderName);
        fileMetadata.setMimeType(FOLDER_MIME_TYPE);
        File file = googleDriveService.files().create(fileMetadata)
                .setSupportsAllDrives(true)
                .setFields("id,webViewLink")
                .execute();
        Folder folder = new Folder();
        folder.setId(file.getId());
        folder.setName(folderName);
        folder.setUrl(file.getWebViewLink());
        return folder;
    }
}
