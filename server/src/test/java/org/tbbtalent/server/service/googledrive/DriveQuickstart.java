/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.googledrive;

// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.DriveList;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class DriveQuickstart {
    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    private static final String CREDENTIALS_FILE_PATH = "/talentcatalog-5754b1d225cb.json";

    /**
     * Creates an authorized Credential object.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials() throws IOException {
        // Load credentials file
        InputStream in = DriveQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleCredential credential = GoogleCredential.fromStream(in)
                .createScoped(Collections.singleton(DriveScopes.DRIVE))
                .createDelegated("jcameron@talentbeyondboundaries.org");
        return credential;
    }

    public static void main(String... args) throws IOException, GeneralSecurityException {
       boolean createFolder = true;
       boolean createFile = true;
       
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials())
                .setApplicationName(APPLICATION_NAME)
                .build();

        // Print the names and IDs for up to 10 files.
        FileList result = service.files().list()
                .setPageSize(50)
                .setCorpora("drive")
                .setDriveId("0ALMJ566d9WuVUk9PVA")
                .setIncludeItemsFromAllDrives(true)
                .setSupportsAllDrives(true)
                .setFields("nextPageToken, files(driveId,id, name)")
                .execute();
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            System.out.println("Files:");
            for (File file : files) {
                System.out.printf("%s (%s %s)\n", file.getName(), file.getId(), file.getDriveId());
            }
        }

        // Print the names and IDs for up to 10 drives.
        DriveList result2 = service.drives().list()
                .setPageSize(10)
                .setFields("nextPageToken, drives(id, name)")
                .execute();
        List<com.google.api.services.drive.model.Drive> drives = result2.getDrives();
        if (drives == null || drives.isEmpty()) {
            System.out.println("No drives found.");
        } else {
            System.out.println("Drives:");
            for (com.google.api.services.drive.model.Drive drive : drives) {
                System.out.printf("%s (%s)\n", drive.getName(), drive.getId());
            }
        }

        // Search for CandidateData drive.
        DriveList result3 = service.drives().list()
                .setUseDomainAdminAccess(true)
                .setQ("name='CandidateData'")
                .setPageSize(10)
                .setFields("nextPageToken, drives(id, name)")
                .execute();
        List<com.google.api.services.drive.model.Drive> drives2 = result3.getDrives();
        if (drives2 == null || drives2.isEmpty()) {
            System.out.println("No CandidateData drive found.");
        } else {
            System.out.println("Candidate Data Drives:");
            for (com.google.api.services.drive.model.Drive drive : drives2) {
                System.out.printf("%s (%s)\n", drive.getName(), drive.getId());
            }
        }

        if (createFolder) {
            File fileMetadata = new File();
            fileMetadata.setDriveId("0ALMJ566d9WuVUk9PVA");
            fileMetadata.setParents(Collections.singletonList("0ALMJ566d9WuVUk9PVA"));
            fileMetadata.setName("201345");
            fileMetadata.setMimeType("application/vnd.google-apps.folder");
            try {
                File file = service.files().create(fileMetadata)
                        .setSupportsAllDrives(true)
                        .setFields("id,driveId")
                        .execute();
                System.out.println("Drive ID: " + file.getDriveId());
                System.out.println("Folder ID: " + file.getId());
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }

        if (createFile) {
            File fileMetadata = new File();
            fileMetadata.setDriveId("0ALMJ566d9WuVUk9PVA");
            fileMetadata.setParents(Collections.singletonList("1itPPs_Nxs86Ozj-ET4Jyq-pTJJbdjUew"));
            fileMetadata.setName("TestIgnore");
            try {
                File file = service.files().create(fileMetadata)
                        .setFields("id,driveId")
                        .setSupportsAllDrives(true)
                        .execute();
                System.out.println("Drive ID: " + file.getDriveId());
                System.out.println("Folder ID: " + file.getId());
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
    }
}
