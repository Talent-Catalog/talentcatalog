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

import com.google.api.services.drive.model.File;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.configuration.GoogleDriveConfig;
import org.tbbtalent.server.service.db.DocPublisherService;

/**
 * Publishes Google Sheets on a Google Drive
 *
 * @author John Cameron
 */
@Service
public class GoogleSheetPublisherServiceImpl implements DocPublisherService {
  private final GoogleDriveConfig googleDriveConfig;

  public GoogleSheetPublisherServiceImpl(
      GoogleDriveConfig googleDriveConfig) {
    this.googleDriveConfig = googleDriveConfig;
  }

  @Override
  public String createPublishedDoc(String name, List<List<Object>> data)
      throws GeneralSecurityException, IOException {
    File fileMetadata = new File();
    fileMetadata.setDriveId("0AJpRzZk9D_kLUk9PVA");
    fileMetadata.setParents(Collections.singletonList("1BkMPOr392ubCriQdhBAmFKxlume-c7d2"));
    fileMetadata.setName(name);
    fileMetadata.setMimeType("application/vnd.google-apps.spreadsheet");
    File file = googleDriveConfig.getGoogleDriveService().files().create(fileMetadata)
        .setSupportsAllDrives(true)
        .setFields("id,webViewLink")
        .execute();

    System.out.println("Created empty spreadsheet link: " + file.getWebViewLink());

    ValueRange body = new ValueRange().setValues(data);
    UpdateValuesResponse result =
        googleDriveConfig.getGoogleSheetsService().spreadsheets().values()
            .update(file.getId(), "1:1000", body)
            .setValueInputOption("USER_ENTERED")
            .execute();
    System.out.printf("%d cells updated.", result.getUpdatedCells());

    return file.getWebViewLink();
  }
}
