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

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
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
      String name, List<List<Object>> mainData)
      throws GeneralSecurityException, IOException {

    GoogleFileSystemFile file = fileSystemService.copyFile(
        folder, name, googleDriveConfig.getPublishedSheetTemplate());

    final Sheets service = googleDriveConfig.getGoogleSheetsService();

    //Now write to sheet - see https://developers.google.com/sheets/api/guides/values#writing 

    List<ValueRange> data = new ArrayList<>();

    data.add(new ValueRange()
        .setRange("B7")
        .setValues(mainData));

//TODO JC Pass in Properties with other values to set. 
    List<List<Object>> cell;
    cell = Collections.singletonList(Collections.singletonList("Freddy Baby"));
    data.add(new ValueRange()
        .setRange("name")
        .setValues(cell));

    cell = Collections.singletonList(Collections.singletonList("Iress"));
    data.add(new ValueRange()
        .setRange("employer")
        .setValues(cell));

    BatchUpdateValuesRequest body = new BatchUpdateValuesRequest()
        .setValueInputOption("USER_ENTERED")
        .setData(data);

    BatchUpdateValuesResponse res =
        service.spreadsheets().values().batchUpdate(file.getId(), body).execute();
    
    log.info("Created " + res.getTotalUpdatedCells() + " cells in spreadsheet with link: " + file.getUrl());

    return file.getUrl();
  }
}
