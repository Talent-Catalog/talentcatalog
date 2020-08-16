/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.configuration;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Collection;
import java.util.Collections;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbbtalent.server.service.db.GoogleFileSystemService;

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

/**
 * Configures the googleDriveService bean which is used in the implementation 
 * of {@link GoogleFileSystemService}
 *
 * @author John Cameron
 */
@Configuration
public class GoogleDriveConfiguration {

    private static final String APPLICATION_NAME = "TalentCatalog";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    @Value("${google.drive.clientId}")
    private String clientId;
    @Value("${google.drive.clientEmail}")
    private String clientEmail;
    @Value("${google.drive.privateKey}")
    private String privateKey;
    @Value("${google.drive.privateKeyId}")
    private String privateKeyId;
    @Value("${google.drive.projectId}")
    private String projectId;
    @Value("${google.drive.tokenUri}")
    private String tokenUri;
    
    @Bean
    public Drive googleDriveService() {
        try {
            final NetHttpTransport HTTP_TRANSPORT = 
                    GoogleNetHttpTransport.newTrustedTransport();
            Credential credential = computeCredential(HTTP_TRANSPORT)
                    .createScoped(Collections.singleton(DriveScopes.DRIVE))
                    .createDelegated("candidates@talentbeyondboundaries.org");
            
            Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            return service;
        } catch (Exception e) {
            throw new BeanCreationException(
                    "googleDriveService", "Failed to create a Drive", e);
        }
    }

    private GoogleCredential computeCredential(NetHttpTransport HTTP_TRANSPORT) throws IOException {
        //Convert to proper newlines. 
        // See https://stackoverflow.com/questions/18865393/java-replaceall-not-working-for-n-characters
        privateKey = privateKey.replaceAll("\\\\n", "\n");
        PrivateKey privateKeyFromPkcs8 = privateKeyFromPkcs8(privateKey);

        Collection<String> emptyScopes = Collections.emptyList();

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
