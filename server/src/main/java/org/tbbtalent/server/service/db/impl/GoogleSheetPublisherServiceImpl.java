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

import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.configuration.GoogleDriveConfig;
import org.tbbtalent.server.service.db.DocPublisherService;
import org.tbbtalent.server.service.db.FileSystemService;
import org.tbbtalent.server.util.filesystem.GoogleFileSystemDrive;
import org.tbbtalent.server.util.filesystem.GoogleFileSystemFile;
import org.tbbtalent.server.util.filesystem.GoogleFileSystemFolder;

/**
 * Publishes Google Sheets on a Google Drive
 *
 * @author John Cameron
 */
@Service
public class GoogleSheetPublisherServiceImpl implements DocPublisherService {
  private static final String SHEET_MIME_TYPE = "application/vnd.google-apps.spreadsheet";
  private static final Logger log = LoggerFactory.getLogger(GoogleSheetPublisherServiceImpl.class);

  private final GoogleDriveConfig googleDriveConfig;
  private final FileSystemService fileSystemService;

  public GoogleSheetPublisherServiceImpl(
      GoogleDriveConfig googleDriveConfig,
      FileSystemService fileSystemService) {
    this.googleDriveConfig = googleDriveConfig;
    this.fileSystemService = fileSystemService;
  }

  @Override
  public String createPublishedDoc(GoogleFileSystemDrive drive, GoogleFileSystemFolder folder, 
      String name, List<List<Object>> data)
      throws GeneralSecurityException, IOException {

    GoogleFileSystemFile file = fileSystemService.createFile(drive, folder, name, SHEET_MIME_TYPE);

    ValueRange body = new ValueRange().setValues(data);
    UpdateValuesResponse result =
        googleDriveConfig.getGoogleSheetsService().spreadsheets().values()
            .update(file.getId(), "1:1000", body)
            .setValueInputOption("USER_ENTERED")
            .execute();

    log.info("Created " + result.getUpdatedCells() + " cells in spreadsheet with link: " + file.getUrl());

    return file.getUrl();
  }
}
