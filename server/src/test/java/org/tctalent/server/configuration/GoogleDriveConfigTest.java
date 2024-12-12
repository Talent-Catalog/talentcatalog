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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.Lists;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class GoogleDriveConfigTest {
  private static final String APPLICATION_NAME = "TalentCatalog";
  private static final String DELEGATED_USER = "tcstorage@talentbeyondboundaries.org";
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

//  @Test
  void name() throws IOException, GeneralSecurityException {

    String jsonPath = "/Users/john/Downloads/talentcatalog-ee5ca91e0f79.json";
    GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(jsonPath))
        .createScoped(Lists.newArrayList(
//            "https://www.googleapis.com/auth/cloud-platform",
            SheetsScopes.DRIVE))
        .createDelegated(DELEGATED_USER);

    assertNotNull(credentials);

//    Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
//
//    System.out.println("Buckets:");
//    Page<Bucket> buckets = storage.list();
//    for (Bucket bucket : buckets.iterateAll()) {
//      System.out.println(bucket.toString());
//    }

    final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

    Drive googleDriveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
        .setApplicationName(APPLICATION_NAME)
        .build();
    File fileMetadata = new File();
    fileMetadata.setDriveId("0AJpRzZk9D_kLUk9PVA");
    fileMetadata.setParents(Collections.singletonList("1BkMPOr392ubCriQdhBAmFKxlume-c7d2"));
    fileMetadata.setName("AAAJohnWasHere");
    fileMetadata.setMimeType("application/vnd.google-apps.spreadsheet");
    File file = googleDriveService.files().create(fileMetadata)
        .setSupportsAllDrives(true)
        .setFields("id,webViewLink")
        .execute();

    System.out.println("Created empty spreadsheet link: " + file.getWebViewLink());


    Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY,
        new HttpCredentialsAdapter(credentials))
        .setApplicationName(APPLICATION_NAME)
        .build();

    List<List<Object>> values = Arrays.asList(
        Arrays.asList(
            "1", "10", "20"
        ),
        Arrays.asList(
            "=A1", "=A1+A2", "40"
        )
    );
    ValueRange body = new ValueRange()
        .setValues(values);
    UpdateValuesResponse result =
        service.spreadsheets().values().update(file.getId(), "1:1000", body)
            .setValueInputOption("USER_ENTERED")
            .execute();
    System.out.printf("%d cells updated.", result.getUpdatedCells());

  }

}
