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

package org.tctalent.server.service.db.impl;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.AddNamedRangeRequest;
import com.google.api.services.sheets.v4.model.AddProtectedRangeRequest;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.configuration.GoogleDriveConfig;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.request.candidate.PublishListRequest;
import org.tctalent.server.request.candidate.PublishedDocBuilderService;
import org.tctalent.server.request.candidate.PublishedDocColumnDef;
import org.tctalent.server.request.candidate.PublishedDocColumnSetUp;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.DocPublisherService;
import org.tctalent.server.service.db.FileSystemService;
import org.tctalent.server.util.filesystem.GoogleFileSystemFile;
import org.tctalent.server.util.filesystem.GoogleFileSystemFolder;

/**
 * Publishes Google Sheets on a Google Drive
 *
 * @author John Cameron
 */
@Service
@Slf4j
public class GoogleSheetPublisherServiceImpl implements DocPublisherService {

    private final CandidateService candidateService;
    private final GoogleDriveConfig googleDriveConfig;
    private final FileSystemService fileSystemService;
    private final PublishedDocBuilderService publishedDocBuilderService;

    public GoogleSheetPublisherServiceImpl(
        CandidateService candidateService,
        GoogleDriveConfig googleDriveConfig,
        FileSystemService fileSystemService,
        PublishedDocBuilderService publishedDocBuilderService) {
        this.candidateService = candidateService;
        this.googleDriveConfig = googleDriveConfig;
        this.fileSystemService = fileSystemService;
        this.publishedDocBuilderService = publishedDocBuilderService;
    }

    /**
     * Designed to be run asynchronously - hence candidate ids are passed, forcing candidate
     * entities to be reloaded from database.
     * <p/>
     * The @Transactional notation kicks off a new persistence context which allows for
     * lazy attributes to be automatically loaded as needed.
     */
    @Transactional
    @Async
    public void populatePublishedDoc(
        String publishedDocLink, long savedListId,
        List<Long> candidateIds, PublishListRequest request,
        String publishedSheetDataRangeName)
        throws GeneralSecurityException, IOException {

        //Load candidates from database into persistence context
        LogBuilder.builder(log)
            .action("populatePublishedDoc")
            .message("Loading " + candidateIds.size() +" candidates from database into persistence context")
            .logInfo();

        List<Candidate> candidates = new ArrayList<>();
        for (Long candidateId : candidateIds) {
            final Candidate candidate = candidateService.getCandidate(candidateId);

            //Set list context on candidate entities so that Candidate field contextNote can be accessed.
            candidate.setContextSavedListId(savedListId);
            candidates.add(candidate);
        }

        //Create all candidate folders (and subfolders) as needed.
        int count = 0;
        for (Candidate candidate : candidates) {
            candidateService.createCandidateFolder(candidate.getId());
            count++;

            if (count % 50 == 0 || count == candidates.size()) {
                LogBuilder.builder(log)
                    .action("populatePublishedDoc")
                    .message("Created folders for " + count + " out of " + candidates.size() + " candidates")
                    .logInfo();
            }
        }

        //This is what will be used to create the published doc
        List<List<Object>> publishedData = new ArrayList<>();

        final List<PublishedDocColumnDef> columnInfos = request.getConfiguredColumns();

        //Title row
        List<Object> title = publishedDocBuilderService.buildTitle(columnInfos);
        publishedData.add(title);

        //Sort candidates by candidate id (ie oldest first) - note that sorting by candidateNumber
        //gives alpha sort - eg 100 before 20
        candidates.sort(Comparator.comparing(Candidate::getId));

        final PublishedDocColumnDef expandingColumnDef = request.getExpandingColumnDef();
        //Add row for each candidate
        for (Candidate candidate : candidates) {
            //Could be more than one row per candidate if, for example, dependants are being displayed
            int nRows = publishedDocBuilderService.computeNumberOfRowsByCandidate(
                candidate, expandingColumnDef);
            for (int expandingCount = 0; expandingCount < nRows; expandingCount++) {
                List<Object> candidateData = publishedDocBuilderService.buildRow(
                    candidate, expandingColumnDef, expandingCount, columnInfos);
                publishedData.add(candidateData);
            }
        }
        writeCandidateDataToDoc(publishedDocLink, publishedSheetDataRangeName, publishedData);
    }

    /**
     * Writes the candidate data to the given document into the given range.
     * @param docUrl Link to document (sheet) to write to
     * @param dataRangeName Name of data range where data is to be written
     * @param mainData Array of cell data to write
     * @throws GeneralSecurityException if there are security problems accessing the document
     * @throws IOException if there are communication problems accessing the document
     */
    private void writeCandidateDataToDoc(
        String docUrl, String dataRangeName, List<List<Object>> mainData)
        throws GeneralSecurityException, IOException {

        GoogleFileSystemFile sheet = new GoogleFileSystemFile(docUrl);

        List<ValueRange> data = new ArrayList<>();

        //Add main data - the rows for each candidate, plus the column headers in the first row.
        data.add(new ValueRange().setRange(dataRangeName).setValues(mainData));

        // TODO: 8/2/22 Should work - but no need to batch
        BatchUpdateValuesRequest body = new BatchUpdateValuesRequest()
            .setValueInputOption("USER_ENTERED")
            .setData(data);

        final Sheets service = googleDriveConfig.getGoogleSheetsService();
        final String spreadsheetId = sheet.getId();

        BatchUpdateValuesResponse res = service.spreadsheets().values()
            .batchUpdate(spreadsheetId, body).execute();

        LogBuilder.builder(log)
            .action("WriteCandidateDataToDoc")
            .message("Created " + res.getTotalUpdatedCells() + " cells in spreadsheet with link: "
                + sheet.getUrl())
            .logInfo();
    }

    /**
     * If there is no expanding column, the number of rows equals the number of candidates - ie
     * one row per candidate.
     * <p>
     * However, if there is an expanding column (eg a candidate's dependants), there may be extra
     * rows.
     * @param candidates The candidates supplying the data
     * @param expandingColumnDef If not null defines a column that may lead to more than one
     *                           row being displayed for some candidates.
     * @return The number of rows of data.
     */
    private int computeNumberOfRows(
        @NonNull List<Candidate> candidates, @Nullable PublishedDocColumnDef expandingColumnDef) {
        int nRows = 0;
        for (Candidate candidate : candidates) {
            nRows += publishedDocBuilderService.computeNumberOfRowsByCandidate(
                candidate, expandingColumnDef);
        }
        return nRows;
    }

    @Override
    public String createPublishedDoc(GoogleFileSystemFolder folder,
        String name, String dataRangeName,
        List<Candidate> candidates, PublishListRequest request,
        Map<String, Object> props,
        Map<Integer, PublishedDocColumnSetUp> columnSetUpMap)
        throws GeneralSecurityException, IOException {

        //The number of data rows required plus 1 for the header
        int nRowsData = computeNumberOfRows(candidates, request.getExpandingColumnDef()) + 1;

        //Create copy of sheet from template
        GoogleFileSystemFile file = fileSystemService.copyFile(
            folder, name, googleDriveConfig.getPublishedSheetTemplate());
        final String spreadsheetId = file.getId();

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

        DataInSheet dataInSheet = new DataInSheet(mainSheetId, dataRange, nRowsData);

        //Add in extra properties. These go into the named cells whose names are given by the map keys.
        //This is the data that ends up in the sheet's Data tab.
        for (Entry<String, Object> prop : props.entrySet()) {
            List<List<Object>> cell = Arrays.asList(Arrays.asList(prop.getValue()));
            data.add(new ValueRange().setRange(prop.getKey()).setValues(cell));
        }
        BatchUpdateValuesRequest body = new BatchUpdateValuesRequest()
            .setValueInputOption("USER_ENTERED")
            .setData(data);
        BatchUpdateValuesResponse res = service.spreadsheets().values()
            .batchUpdate(spreadsheetId, body).execute();

        LogBuilder.builder(log)
            .action("CreatePublishedDoc")
            .message("Created " + res.getTotalUpdatedCells() + " cells in spreadsheet with link: "
                + file.getUrl())
            .logInfo();

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

        //Now protect the sheets other than the Main one (eg the Data sheet)
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

        LogBuilder.builder(log)
            .action("CreatePublishedDoc")
            .message(res2.getReplies().size() + " batch update responses received")
            .logInfo();

        //Validate the number of candidate rows to be written fits within the bounds of the named data range.
        //Throws an IOException which the user will see in the admin portal if the range is too small.
        validateDataRangeCapacity(dataRangeName, dataRange, nRowsData);

        //File is already public - ie viewable by anyone with the link - because of the folder where
        //it is located
        //Setting it public when it is already causes Google to throw a permissions error

        return file.getUrl();

    }

    static void validateDataRangeCapacity(String rangeName, GridRange range, int nRowsData)
        throws IOException {

        int availableDataRows = range.getEndRowIndex() - range.getStartRowIndex();

        if (nRowsData > availableDataRows) {
            throw new IOException("Attempting to publish too many candidates (" + (nRowsData - 1)
                + ") to the sheet " + rangeName + " which can hold a maximum of "
                + (availableDataRows - 1) + " rows.");
        }
    }

    @Override
    public Map<String, List<Object>> readPublishedDocColumns(String docUrl,
        List<String> columnNamedRanges)
        throws GeneralSecurityException, IOException {
        GoogleFileSystemFile spreadsheet = new GoogleFileSystemFile(docUrl);
        String spreadsheetId = spreadsheet.getId();
        final Sheets service = googleDriveConfig.getGoogleSheetsService();

        //Generate list of column ranges that are present in sheet
        List<String> rangeNamesInSheet = getRangeNamesInSheet(service, spreadsheetId);
        List<String> columnRangeNamesInSheet = rangeNamesInSheet.stream()
            .filter(columnNamedRanges::contains)
            .collect(Collectors.toList());

        //Fetch data for those column names
        BatchGetValuesResponse result = service.spreadsheets().values().batchGet(spreadsheetId)
            .setRanges(columnRangeNamesInSheet).execute();

        Map<String, List<Object>> feedbackColumns = new HashMap<>();

        List<ValueRange> valueRanges = result.getValueRanges();
        if (valueRanges != null) {
            int rangeIndex = 0;
            for (ValueRange valueRange : valueRanges) {
                List<List<Object>> vals = valueRange.getValues();
                List<Object> colVals = new ArrayList<>();
                feedbackColumns.put(columnRangeNamesInSheet.get(rangeIndex++), colVals);
                if (vals != null) {
                    for (List<Object> val : vals) {
                        if (val.size() > 0) {
                            colVals.add(val.get(0));
                        } else {
                            colVals.add(null);
                        }
                    }
                }
            }
        }
        return feedbackColumns;
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
        Request req = new Request().setUpdateDimensionProperties(
            new UpdateDimensionPropertiesRequest()
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
     * Returns names of all named ranges.
     */
    private List<String> getRangeNamesInSheet(Sheets service, String spreadsheetId)
        throws IOException {
        List<NamedRange> namedRanges = getNamedRanges(service, spreadsheetId);
        List<String> rangeNames = new ArrayList<>();
        for (NamedRange namedRange : namedRanges) {
            rangeNames.add(namedRange.getName());
        }
        return rangeNames;
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


    /**
     * Represents a region of data located at a certain position in a sheet.
     * <p/>
     * Used to convert a column of the data into the corresponding GridRange within the sheet.
     */
    private static class DataInSheet {

        private final GridRange dataRange;
        private final Integer sheetId;
        private final int nRowsData;

        /**
         * Places the given data at the given location in the given sheet.
         * <p/>
         * The first row of the data is assumed to be column headers
         *
         * @param sheetId   Sheet id (ie the tab id)
         * @param dataRange Location within which the data is located in the sheet
         * @param nRowsData The number of rows of data (including the header row).
         */
        public DataInSheet(Integer sheetId, GridRange dataRange, int nRowsData) {
            this.dataRange = dataRange;
            this.sheetId = sheetId;
            this.nRowsData = nRowsData;
        }

        /**
         * Returns the sheet range of the given column of the data (excluding the header)
         *
         * @param columnInData Column in data (index 0)
         * @return A sheet range describing the location of that column of data within the sheet
         */
        GridRange getColumnRange(int columnInData) {
            //Skip header row
            int startRow = dataRange.getStartRowIndex() + 1;
            final int startColumn = dataRange.getStartColumnIndex() + columnInData;

            return new GridRange().setSheetId(sheetId)
                .setStartRowIndex(startRow)
                .setStartColumnIndex(startColumn)
                //Don't count header row
                .setEndRowIndex(startRow + nRowsData - 1)
                .setEndColumnIndex(startColumn + 1);
        }
    }

}
