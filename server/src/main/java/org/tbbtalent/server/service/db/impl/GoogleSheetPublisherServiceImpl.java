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
import com.google.api.services.sheets.v4.model.AddProtectedRangeRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetResponse;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesResponse;
import com.google.api.services.sheets.v4.model.GridRange;
import com.google.api.services.sheets.v4.model.ProtectedRange;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.ValueRange;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
      String name, List<List<Object>> mainData, Map<String, Object> props)
      throws GeneralSecurityException, IOException {

    //Create copy of sheet from template
    GoogleFileSystemFile file = fileSystemService.copyFile(
        folder, name, googleDriveConfig.getPublishedSheetTemplate());

    //Now write to sheet - see https://developers.google.com/sheets/api/guides/values#writing 
    final Sheets service = googleDriveConfig.getGoogleSheetsService();
    List<ValueRange> data = new ArrayList<>();

    //Add main data
    data.add(new ValueRange().setRange("B7").setValues(mainData));

    //Add in extra properties
    for (Entry<String, Object> prop : props.entrySet()) {
      List<List<Object>> cell = Arrays.asList(Arrays.asList(prop.getValue()));
      data.add(new ValueRange().setRange(prop.getKey()).setValues(cell));
    }

    BatchUpdateValuesRequest body = new BatchUpdateValuesRequest()
        .setValueInputOption("USER_ENTERED")
        .setData(data);

    final String spreadsheetId = file.getId();
    BatchUpdateValuesResponse res =
        service.spreadsheets().values().batchUpdate(spreadsheetId, body).execute();
    
    log.info("Created " + res.getTotalUpdatedCells() + " cells in spreadsheet with link: " + file.getUrl());


    List<Request> requests = new ArrayList<>();

    List<Integer> sheetIdsToProtect = getSheetsToProtect(service, spreadsheetId);
    for (Integer sheetId : sheetIdsToProtect) {
      Request req = new Request();
      req.setAddProtectedRange(new AddProtectedRangeRequest().setProtectedRange(
          new ProtectedRange()
              .setRange(new GridRange().setSheetId(sheetId))
              .setWarningOnly(true)
      ));
      requests.add(req);
    }
    
    BatchUpdateSpreadsheetRequest content = 
        new BatchUpdateSpreadsheetRequest().setRequests(requests);
    BatchUpdateSpreadsheetResponse res2 = 
        service.spreadsheets().batchUpdate(spreadsheetId, content).execute();

    log.info(res2.getReplies().size() + " protection responses received");

    return file.getUrl();
  }

  private List<Integer> getSheetsToProtect(Sheets service, String spreadsheetId)
      throws IOException {
    List<Integer> ids = new ArrayList<>();
    Sheets.Spreadsheets.Get request = service.spreadsheets().get(spreadsheetId);
    List<Sheet> sheets = request.execute().getSheets();
    for (Sheet sheet : sheets) {
      SheetProperties p = sheet.getProperties();
      if (!"Main".equals(p.getTitle())) {
        //Protect this sheet
        ids.add(p.getSheetId());
      }
    }
    return ids;
  }
}
