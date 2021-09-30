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
import com.google.api.services.sheets.v4.model.AddNamedRangeRequest;
import com.google.api.services.sheets.v4.model.AddProtectedRangeRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetResponse;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesResponse;
import com.google.api.services.sheets.v4.model.BooleanCondition;
import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.CellFormat;
import com.google.api.services.sheets.v4.model.ConditionValue;
import com.google.api.services.sheets.v4.model.DataValidationRule;
import com.google.api.services.sheets.v4.model.DimensionProperties;
import com.google.api.services.sheets.v4.model.DimensionRange;
import com.google.api.services.sheets.v4.model.GridRange;
import com.google.api.services.sheets.v4.model.NamedRange;
import com.google.api.services.sheets.v4.model.ProtectedRange;
import com.google.api.services.sheets.v4.model.RepeatCellRequest;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.SetDataValidationRequest;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.UpdateDimensionPropertiesRequest;
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
import org.tbbtalent.server.request.candidate.PublishedDocColumnSetUp;
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
      String name, String dataRangeName, List<List<Object>> mainData, Map<String, Object> props,
      Map<Integer, PublishedDocColumnSetUp> columnSetUpMap)
      throws GeneralSecurityException, IOException {

    //Create copy of sheet from template
    GoogleFileSystemFile file = fileSystemService.copyFile(
        folder, name, googleDriveConfig.getPublishedSheetTemplate());
    final String spreadsheetId = file.getId();
    int nDataColumns = mainData.get(0).size();
    
    //Now write to sheet - see https://developers.google.com/sheets/api/guides/values#writing 
    final Sheets service = googleDriveConfig.getGoogleSheetsService();
    List<ValueRange> data = new ArrayList<>();

    //Extract row index and column index from dataRangeName named range.
    //This is useful for calculating the actual column indexes that the mainData gets written to.
    //Basically you just need to add the start column index as an offset.
    GridRange dataRange = null;
    List<NamedRange> namedRanges = getNamedRanges(service, spreadsheetId);
    for (NamedRange namedRange : namedRanges) {
      if (namedRange.getName().equals(dataRangeName)) {
        dataRange = namedRange.getRange();
        break;
      }
    }
    if (dataRange == null) {
      throw new IOException("Sheet is missing named data range called " + dataRangeName);
    }

    //Fetch properties of different sheets (tabs)
    List<SheetProperties> sheetProperties = getSheetProperties(service, spreadsheetId);

    //Find main sheet id
    Integer mainSheetId = null;
    for (SheetProperties sheetProperty : sheetProperties) {
      if ("Main".equals(sheetProperty.getTitle())) {
        mainSheetId = sheetProperty.getSheetId();
        break;
      }
    }
    
    DataInSheet dataInSheet = new DataInSheet(mainSheetId, dataRange, mainData);

    
    //   NOW START POPULATING SHEET
    
    //Add main data - the rows for each candidate, plus the column headers in the first row.
    data.add(new ValueRange().setRange(dataRangeName).setValues(mainData));

    //Add in extra properties. These go into the named cells whose names are given by the map keys.
    //This is the data that ends up in the sheet's Data tab.
    for (Entry<String, Object> prop : props.entrySet()) {
      List<List<Object>> cell = Arrays.asList(Arrays.asList(prop.getValue()));
      data.add(new ValueRange().setRange(prop.getKey()).setValues(cell));
    }
    BatchUpdateValuesRequest body = new BatchUpdateValuesRequest()
        .setValueInputOption("USER_ENTERED")
        .setData(data);
    BatchUpdateValuesResponse res = service.spreadsheets().values().batchUpdate(spreadsheetId, body).execute();
    log.info("Created " + res.getTotalUpdatedCells() + " cells in spreadsheet with link: " + file.getUrl());
    
    //Now batch various other update requests which involve configuring drop down data entry and
    //protecting parts of the sheet.
    List<Request> requests = new ArrayList<>();
    Request req;
    
    //Add column formatting
    for (Entry<Integer, PublishedDocColumnSetUp> entry : columnSetUpMap.entrySet()) {
      GridRange range = dataInSheet.getColumnRange(entry.getKey());
      PublishedDocColumnSetUp setup = entry.getValue();
      if (setup.getAlignment() != null) {
        req = computeAlignmentRequest(range, setup.getAlignment());
        requests.add(req);
      }
      if (setup.getColumnSize() != null) {
        req = computeColumnWidthRequest(range, setup.getColumnSize());
        requests.add(req);
      }
      if (setup.getDropDowns() != null) {
        req = computeDropDownsRequest(range, setup.getDropDowns());
        requests.add(req);
      }
      if (setup.getRangeName() != null) {
        req = computeAddNamedRangeRequest(range, setup.getRangeName());
        requests.add(req);
      }
    }
    
    //Now protect the sheets other than the Main one (ie the Data and Feedback sheets)
    //Users should normally only be able to change the main sheet - not the other tabs
    //See https://developers.google.com/sheets/api/samples/ranges
    for (SheetProperties sheetProperty : sheetProperties) {
      if (!"Main".equals(sheetProperty.getTitle())) {
        req = new Request();
        req.setAddProtectedRange(new AddProtectedRangeRequest().setProtectedRange(
            new ProtectedRange()
                .setRange(new GridRange().setSheetId(sheetProperty.getSheetId()))
                .setWarningOnly(true)
        ));
        requests.add(req);
      }
    }
    
    BatchUpdateSpreadsheetRequest content = 
        new BatchUpdateSpreadsheetRequest().setRequests(requests);
    BatchUpdateSpreadsheetResponse res2 = 
        service.spreadsheets().batchUpdate(spreadsheetId, content).execute();

    log.info(res2.getReplies().size() + " batch update responses received");

    return file.getUrl();
  }

  /**
   * Represents a region of data located at a certain position in a sheet.
   * <p/>
   * Used to convert a column of the data into the corresponding GridRange within the sheet. 
   */
  private static class DataInSheet {

    private final GridRange dataRange;
    private final Integer sheetId;
    private final List<List<Object>> data;

    /**
     * Places the given data at the given location in the given sheet.
     * <p/>
     * The first row of the data is assumed to be column headers
     * @param sheetId Sheet id (ie the tab id)
     * @param dataRange Location within which the data is located in the sheet
     * @param data The array of data values.
     */
    public DataInSheet(Integer sheetId, GridRange dataRange, List<List<Object>> data) {
      this.dataRange = dataRange;
      this.sheetId = sheetId;
      this.data = data;
    }

    /**
     * Returns the sheet range of the given column of the data (excluding the header)
     * @param columnInData Column in data (index 0)
     * @return A sheet range describing the location of that column of data within the sheet 
     */
    GridRange getColumnRange(int columnInData) {
      //Skip header row
      int startRow = dataRange.getStartRowIndex()+1;
      final int startColumn = dataRange.getStartColumnIndex() + columnInData;

      return new GridRange().setSheetId(sheetId)
          .setStartRowIndex(startRow)
          .setStartColumnIndex(startColumn)
          //Don't count header row
          .setEndRowIndex(startRow + data.size()-1)
          .setEndColumnIndex(startColumn+1);       
    }
  }

  private Request computeAddNamedRangeRequest(GridRange range, String rangeName) {
    //Add named range
    //See https://developers.google.com/sheets/api/samples/ranges
    Request req = new Request().setAddNamedRange(new AddNamedRangeRequest()
        .setNamedRange(new NamedRange()
            .setName(rangeName)
            .setRange(range)
        )
    );
    return req;
  }

  private Request computeAlignmentRequest(GridRange range, String alignment) {
    //Add alignment
    //See https://developers.google.com/sheets/api/samples/formatting
    Request req = new Request().setRepeatCell(new RepeatCellRequest()
        .setRange(range)
        .setCell(new CellData()
            .setUserEnteredFormat(new CellFormat()
                .setHorizontalAlignment(alignment))
        )
        .setFields("userEnteredFormat(horizontalAlignment)")
    );
    return req;
  }

  private Request computeColumnWidthRequest(GridRange range, int pixelSize) {
    //Set column width
    //See https://developers.google.com/sheets/api/samples/rowcolumn
    Request req = new Request().setUpdateDimensionProperties(new UpdateDimensionPropertiesRequest()
        .setRange(new DimensionRange()
            .setSheetId(range.getSheetId())
            .setDimension("COLUMNS")
            .setStartIndex(range.getStartColumnIndex())
            .setEndIndex(range.getEndColumnIndex()))
        .setProperties(new DimensionProperties()
            .setPixelSize(pixelSize))
        .setFields("pixelSize")
    );
    return req;
  }

  private Request computeDropDownsRequest(GridRange range, List<String> options) {
    //Add data validation drop downs
    //See https://developers.google.com/sheets/api/samples/data
    List<ConditionValue> optionValues = new ArrayList<>();
    for (String option : options) {
      optionValues.add(new ConditionValue().setUserEnteredValue(option));
    }
    Request req = new Request().setSetDataValidation(new SetDataValidationRequest()
        .setRange(range)
        .setRule(new DataValidationRule()
            .setCondition(new BooleanCondition()
                .setType("ONE_OF_LIST")
                .setValues(optionValues))
            .setStrict(true)
            
            //This causes the drop down to display
            .setShowCustomUi(true)
        )
    );
    return req;
  }
  
  /**
   * Returns all the named ranges.
   */
  private List<NamedRange> getNamedRanges(Sheets service, String spreadsheetId)
      throws IOException {
    // See https://developers.google.com/sheets/api/reference/rest/v4/spreadsheets#NamedRange
    Sheets.Spreadsheets.Get request = service.spreadsheets().get(spreadsheetId);
    return request.execute().getNamedRanges();
  }
  
  /**
   * Returns the properties of all sheets (tabs).
   */
  private List<SheetProperties> getSheetProperties(Sheets service, String spreadsheetId)
      throws IOException {
    List<SheetProperties> sheetProperties = new ArrayList<>();
    Sheets.Spreadsheets.Get request = service.spreadsheets().get(spreadsheetId);
    List<Sheet> sheets = request.execute().getSheets();
    for (Sheet sheet : sheets) {
      sheetProperties.add(sheet.getProperties());
    }
    return sheetProperties;
  }
}
