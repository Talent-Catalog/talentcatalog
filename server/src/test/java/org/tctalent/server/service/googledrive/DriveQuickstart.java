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

package org.tctalent.server.service.googledrive;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.DriveList;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                .createDelegated("tcstorage@talentbeyondboundaries.org");
        return credential;
    }

    public static void main(String... args) throws IOException, GeneralSecurityException {
       boolean createFolder = true;
       boolean createFile = true;
       File fileMetadata;
       File file;

        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials())
                .setApplicationName(APPLICATION_NAME)
                .build();

        // Print the names and IDs for up to 10 files.
        FileList result = service.files().list()
                .setPageSize(50)
                .setCorpora("drive")
                .setDriveId("0AHvd4Bs-dSp4Uk9PVA")
                .setIncludeItemsFromAllDrives(true)
                .setSupportsAllDrives(true)
                .setFields("nextPageToken, files(driveId,id, name,webViewLink)")
                .execute();
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            System.out.println("Files:");
            for (File f : files) {
                System.out.printf("%s (%s %s)\n", f.getName(), f.getId(), f.getDriveId());
                System.out.println(getIdFromUrl(f.getWebViewLink()));
            }
        }

        // Upload file.
        fileMetadata = new File();
        fileMetadata.setDriveId("0AHvd4Bs-dSp4Uk9PVA");
        fileMetadata.setParents(Collections.singletonList("0AHvd4Bs-dSp4Uk9PVA"));
        fileMetadata.setName("EnglishTxt.txt");

        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        java.io.File filePath = new java.io.File(classLoader.getResource("text/EnglishTxt.txt").getFile());
        FileContent mediaContent = new FileContent("text/plain", filePath);
        file = service.files().create(fileMetadata, mediaContent)
                .setSupportsAllDrives(true)
                .setFields("id")
                .execute();
        String uploadedFileId = file.getId();
        System.out.println("Uploaded file ID: " + uploadedFileId);

        // Download file.
        String fileId = uploadedFileId;
        OutputStream outputStream = new ByteArrayOutputStream();
        service.files().get(fileId)
                .setSupportsAllDrives(true)
                .executeMediaAndDownloadTo(outputStream);

        System.out.println("Created outputstream: " + outputStream.toString());

        //Rename file
        File targetFile = new File();
        targetFile.setName("Peanuts.txt");
        File updatedFile = service.files().update(uploadedFileId, targetFile)
                .setSupportsAllDrives(true)
                .setFields("id,name")
                .execute();


        //Delete file.
        service.files().delete(file.getId()).setSupportsAllDrives(true).execute();
        System.out.println("File ID: " + file.getId());


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
            fileMetadata = new File();
            fileMetadata.setDriveId("0AHvd4Bs-dSp4Uk9PVA");
            fileMetadata.setParents(Collections.singletonList("0AHvd4Bs-dSp4Uk9PVA"));
            fileMetadata.setName("201345");
            fileMetadata.setMimeType("application/vnd.google-apps.folder");
            try {
                file = service.files().create(fileMetadata)
                        .setSupportsAllDrives(true)
                        .setFields("id,driveId,webViewLink")
                        .execute();
                System.out.println("Drive ID: " + file.getDriveId());
                System.out.println("Folder ID: " + file.getId());
                System.out.println(getIdFromUrl(file.getWebViewLink()));
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }

        if (createFile) {
            fileMetadata = new File();
            fileMetadata.setDriveId("0AHvd4Bs-dSp4Uk9PVA");
            fileMetadata.setParents(Collections.singletonList("1itPPs_Nxs86Ozj-ET4Jyq-pTJJbdjUew"));
            fileMetadata.setName("TestIgnore");
            try {
                file = service.files().create(fileMetadata)
                        .setFields("id,driveId,webViewLink")
                        .setSupportsAllDrives(true)
                        .execute();
                System.out.println("Drive ID: " + file.getDriveId());
                System.out.println("Folder ID: " + file.getId());
                System.out.println(getIdFromUrl(file.getWebViewLink()));
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
    }

    public static String getIdFromUrl(String url) {

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
