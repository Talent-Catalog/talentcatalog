/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.db.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.service.db.GoogleFileSystemService;
import org.tbbtalent.server.util.filesystem.FileSystemFile;
import org.tbbtalent.server.util.filesystem.FileSystemFolder;

import com.google.api.client.http.FileContent;
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
    public FileSystemFolder findAFolder(String folderName) throws IOException {
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
        FileSystemFolder folder = null;
        if (folders != null && !folders.isEmpty()) {
            File file = folders.get(0);
            folder = new FileSystemFolder();
            folder.setId(file.getId());
            folder.setName(folderName);
            folder.setUrl(file.getWebViewLink());
        }
        return folder;
    }

    @Override
    public @NonNull
    FileSystemFolder createFolder(String folderName) throws IOException {
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
        FileSystemFolder folder = new FileSystemFolder();
        folder.setId(file.getId());
        folder.setName(folderName);
        folder.setUrl(file.getWebViewLink());
        return folder;
    }

    @Override
    public void deleteFile(FileSystemFile file) throws IOException {
        String id = file.getId();
        if (id == null) {
            id = extractIdFromUrl(file.getUrl());
        }
        if (id == null) {
            throw new IOException("Could not find id to delete file " + file);
        }
        
        googleDriveService.files().delete(id)
                .setSupportsAllDrives(true)
                .execute();
    }

    @Override
    public void downloadFile(@NonNull FileSystemFile file, 
                             @NonNull OutputStream out) throws IOException {
        String id = file.getId();
        if (id == null) {
            id = extractIdFromUrl(file.getUrl());
        }
        if (id == null) {
            throw new IOException("Could not find id to delete file " + file);
        }

        //See https://developers.google.com/drive/api/v3/manage-downloads
        googleDriveService.files().get(id)
                .setSupportsAllDrives(true)
                .executeMediaAndDownloadTo(out);
    }

    @Override
    public void renameFile(@NonNull FileSystemFile file) 
            throws IOException {
        String id = file.getId();
        if (id == null) {
            id = extractIdFromUrl(file.getUrl());
        }
        if (id == null) {
            throw new IOException("Could not find id to delete file " + file);
        }

        File targetFile = new File();
        targetFile.setName(file.getName());
        googleDriveService.files().update(id, targetFile)
                .setSupportsAllDrives(true)
                .setFields("id,name")
                .execute();
    }

    @Override
    public @NonNull FileSystemFile uploadFile(
            @Nullable FileSystemFolder parentFolder, 
            String fileName, java.io.File file) 
            throws IOException {

        File fileMetadata = new File();

        //Set parent to given folder, or drive (root) if no folder given
        List<String> parent;
        if (parentFolder == null) {
            parent = Collections.singletonList(candidateDataDriveId);
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
        FileSystemFile fsf = new FileSystemFile();
        fsf.setId(uploadedfile.getId());
        fsf.setName(fileName);
        fsf.setUrl(uploadedfile.getWebViewLink());
        
        return fsf;
    }

    public String extractIdFromUrl(String url) {
        if (url == null) {
            return null;
        }
        
        //See https://stackoverflow.com/questions/16840038/easiest-way-to-get-file-id-from-url-on-google-apps-script 
        String pattern = ".*[^-\\w]([-\\w]{25,})[^-\\w]?.*";
        Pattern r = Pattern.compile(pattern);

        Matcher m = r.matcher(url);
        if (m.find() && m.groupCount() == 1) {
            return m.group(1);
        } else {
            return null;
        }
    }
    
}
