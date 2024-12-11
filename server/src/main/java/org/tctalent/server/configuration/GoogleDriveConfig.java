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

package org.tctalent.server.configuration;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.PemReader;
import com.google.api.client.util.SecurityUtils;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.sheets.v4.Sheets;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Collection;
import java.util.Collections;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.tctalent.server.util.filesystem.GoogleFileSystemDrive;
import org.tctalent.server.util.filesystem.GoogleFileSystemFile;
import org.tctalent.server.util.filesystem.GoogleFileSystemFolder;

/**
 * Configures GoogleDrive.
 * <p/>
 * We are authenticated as a service account: see https://cloud.google.com/docs/authentication/production?authuser=2
 * <p/>
 * All access is delegated to a particular user - {@link #DELEGATED_USER}
 *
 * @author John Cameron
 */
@Getter
@Setter
@ConfigurationProperties("google.drive")
public class GoogleDriveConfig {

  private static final String APPLICATION_NAME = "TalentCatalog";
  private static final String DELEGATED_USER = "tcstorage@talentbeyondboundaries.org";
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

  private Credential credential= null;

  /**
   * OAuth 2.0 Client ID.
   * See the Google Drive API for the TalentCatalog project
   * https://console.developers.google.com/apis/api/drive.googleapis.com/
   */
  private String clientId;

  /**
   * Service account email
   * See the Google Drive API for the TalentCatalog project
   * https://console.developers.google.com/apis/api/drive.googleapis.com/
   */
  private String clientEmail;

  /**
   * Private key
   * See the Google Drive API for the TalentCatalog project
   * https://console.developers.google.com/apis/api/drive.googleapis.com/
   */
  private String privateKey;

  /**
   * Private key id
   * See the Google Drive API for the TalentCatalog project
   * https://console.developers.google.com/apis/api/drive.googleapis.com/
   */
  private String privateKeyId;

  /**
   * Project ID
   * See the Google Drive API for the TalentCatalog project
   * https://console.developers.google.com/apis/api/drive.googleapis.com/
   */
  private String projectId;

  /**
   * TokenUri
   * See the Google Drive API for the TalentCatalog project
   * https://console.developers.google.com/apis/api/drive.googleapis.com/
   */
  private String tokenUri;

  /**
   * This is lazily computed from all the above Google Drive API settings above.
   * @see #getGoogleDriveService()
   */
  private Drive googleDriveService;

  /**
   * This is lazily computed from all the above Google API settings above.
   * @see #getGoogleSheetsService()
   */
  private Sheets googleSheetsService;

  /**
   * The ID of the CandidateData Google Drive
   */
  private String candidateDataDriveId;

  /**
   * This is lazily computed from the above drive Id.
   * @see #getCandidateDataDrive()
   */
  private GoogleFileSystemDrive candidateDataDrive;

  /**
   * The ID of the root folder in the CandidateData Google Drive
   */
  private String candidateRootFolderId;

  /**
   * This is lazily computed from the above folder Id.
   * @see #getCandidateRootFolder()
   */
  private GoogleFileSystemFolder candidateRootFolder;

  /**
   * This is the id of the template doc for the Job Opportunity Intake to be copied when job created.
   */
  private String jobOppIntakeTemplateId;

  /**
   * This is lazily computed from the above template Id.
   * @see #getJobOppIntakeTemplate()
   */
  private GoogleFileSystemFile jobOppIntakeTemplate;

  /**
   * The ID of the CandidateData Google Drive
   */
  private String listFoldersDriveId;

  /**
   * This is lazily computed from the above drive Id.
   * @see #getListFoldersDrive()
   */
  private GoogleFileSystemDrive listFoldersDrive;

  /**
   * The ID of the root folder of the list folders on Google Drive
   */
  private String listFoldersRootId;

  /**
   * This is lazily computed from the above folder Id.
   * @see #getListFoldersRoot()
   */
  private GoogleFileSystemFolder listFoldersRoot;

  /**
   * This is the id of the template for published sheets to be copied when a sheet is published.
   */
  private String publishedSheetTemplateId;

  /**
   * Name of range (ie a Named range in the Google sheet) where candidate data should be written
   * (including the header line).
   */
  private String publishedSheetDataRangeName;

  /**
   * Pixel width of narrow columns in generated published sheets
   */
  private Integer publishedSheetNarrowColumn;

  /**
   * Pixel width of wide columns in generated published sheets
   */
  private Integer publishedSheetWideColumn;

  /**
   * This is lazily computed from the above template Id.
   * @see #getPublishedSheetTemplate()
   */
  private GoogleFileSystemFile publishedSheetTemplate;

  public GoogleFileSystemDrive getCandidateDataDrive() {
    if (candidateDataDrive == null) {
      candidateDataDrive = new GoogleFileSystemDrive(null);
      candidateDataDrive.setId(candidateDataDriveId);
    }
    return candidateDataDrive;
  }

  public GoogleFileSystemFolder getCandidateRootFolder() {
    if (candidateRootFolder == null) {
      candidateRootFolder = new GoogleFileSystemFolder(null);
      candidateRootFolder.setId(candidateRootFolderId);
    }
    return candidateRootFolder;
  }

  public GoogleFileSystemFile getJobOppIntakeTemplate() {
    if (jobOppIntakeTemplate == null) {
      jobOppIntakeTemplate = new GoogleFileSystemFile(null);
      jobOppIntakeTemplate.setId(jobOppIntakeTemplateId);
    }
    return jobOppIntakeTemplate;
  }

  public GoogleFileSystemDrive getListFoldersDrive() {
    if (listFoldersDrive == null) {
      listFoldersDrive = new GoogleFileSystemDrive(null);
      listFoldersDrive.setId(listFoldersDriveId);
    }
    return listFoldersDrive;
  }

  public GoogleFileSystemFolder getListFoldersRoot() {
    if (listFoldersRoot == null) {
      listFoldersRoot = new GoogleFileSystemFolder(null);
      listFoldersRoot.setId(listFoldersRootId);
    }
    return listFoldersRoot;
  }

  public GoogleFileSystemFile getPublishedSheetTemplate() {
    if (publishedSheetTemplate == null) {
      publishedSheetTemplate = new GoogleFileSystemFile(null);
      publishedSheetTemplate.setId(publishedSheetTemplateId);
    }
    return publishedSheetTemplate;
  }

  private void configureGoogle() throws IOException, GeneralSecurityException {
    if (credential == null) {
      /*
       * See https://developers.google.com/drive/api/v3/quickstart/java
       */
      final NetHttpTransport HTTP_TRANSPORT =
          GoogleNetHttpTransport.newTrustedTransport();
      credential = computeCredential(HTTP_TRANSPORT)
          .createScoped(Collections.singleton(DriveScopes.DRIVE))
          .createDelegated(DELEGATED_USER);

      googleDriveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
          .setApplicationName(APPLICATION_NAME)
          .build();

      googleSheetsService = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
          .setApplicationName(APPLICATION_NAME)
          .build();
    }
  }

  public Drive getGoogleDriveService() throws IOException, GeneralSecurityException {
    configureGoogle();
    return googleDriveService;
  }

  public Sheets getGoogleSheetsService() throws IOException, GeneralSecurityException {
    configureGoogle();
    return googleSheetsService;
  }

  private GoogleCredential computeCredential(NetHttpTransport HTTP_TRANSPORT) throws IOException {
    if (privateKey == null || privateKey.trim().length() == 0) {
      throw new IOException("Missing critical configuration data. "
          + "If you are running in development, have you requested and installed the secrets file?"
          + " See the README.");
    }

    //Convert to proper newlines.
    // See https://stackoverflow.com/questions/18865393/java-replaceall-not-working-for-n-characters
    privateKey = privateKey.replaceAll("\\\\n", "\n");
    PrivateKey privateKeyFromPkcs8 = privateKeyFromPkcs8(privateKey);

    Collection<String> emptyScopes = Collections.emptyList();

    /*
     * This code follows the service Account flow described in the doc for {@link GoogleCredential}
     */
    GoogleCredential.Builder credentialBuilder = new GoogleCredential.Builder()
        .setTransport(HTTP_TRANSPORT)
        .setJsonFactory(JSON_FACTORY)
        .setServiceAccountId(clientEmail)
        .setServiceAccountScopes(emptyScopes)
        .setServiceAccountPrivateKey(privateKeyFromPkcs8)
        .setServiceAccountPrivateKeyId(privateKeyId);

    if (tokenUri != null) {
      credentialBuilder.setTokenServerEncodedUrl(tokenUri);
    }
    if (projectId != null) {
      credentialBuilder.setServiceAccountProjectId(projectId);
    }

    // Don't do a refresh at this point, as it will always fail before the scopes are added.
    return credentialBuilder.build();
  }

  private static PrivateKey privateKeyFromPkcs8(String privateKeyPem) throws IOException {
    Reader reader = new StringReader(privateKeyPem);
    PemReader.Section section = PemReader.readFirstSectionAndClose(reader, "PRIVATE KEY");
    if (section == null) {
      throw new IOException("Invalid PKCS8 data.");
    }
    byte[] bytes = section.getBase64DecodedBytes();
    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bytes);
    Exception unexpectedException;
    try {
      KeyFactory keyFactory = SecurityUtils.getRsaKeyFactory();
      PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
      return privateKey;
    } catch (GeneralSecurityException exception) {
      unexpectedException = exception;
    }
    throw new IOException("Unexpected exception reading PKCS data", unexpectedException);
  }

}
