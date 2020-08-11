/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.db.impl;

import java.io.IOException;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.service.db.GoogleFileSystemService;
import org.tbbtalent.server.util.filesystem.Folder;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

@Service
public class GoogleFileSystemServiceImpl implements GoogleFileSystemService {
    private static final Logger log = LoggerFactory.getLogger(GoogleFileSystemServiceImpl.class);

    private final Drive googleDriveService;

    @Autowired
    public GoogleFileSystemServiceImpl(Drive googleDriveService) {
        this.googleDriveService = googleDriveService;
    }

    @Override
    public Folder findAFolder(String folderName) {
        //TODO JC Implement this
        return null;
    }

    @Override
    public @NonNull
    Folder createFolder(String folderName) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setDriveId("0ALMJ566d9WuVUk9PVA");
        fileMetadata.setParents(Collections.singletonList("0ALMJ566d9WuVUk9PVA"));
        fileMetadata.setName(folderName);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");
        File file = googleDriveService.files().create(fileMetadata)
                .setSupportsAllDrives(true)
                .setFields("id,driveId,webViewLink")
                .execute();
        log.info("Drive ID: " + file.getDriveId());
        log.info("Folder ID: " + file.getId());
        Folder folder = new Folder();
        folder.setId(file.getId());
        folder.setName(folderName);
        folder.setUrl(file.getWebViewLink());
        return folder;
    }
}
