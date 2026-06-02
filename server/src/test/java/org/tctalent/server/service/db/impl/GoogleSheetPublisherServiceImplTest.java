/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.db.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.tctalent.server.service.db.impl.GoogleSheetPublisherServiceImpl.validateDataRangeCapacity;

import com.google.api.services.sheets.v4.model.GridRange;
import com.google.api.services.sheets.v4.model.Request;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.tctalent.server.configuration.GoogleDriveConfig;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.HasMultipleRows;
import org.tctalent.server.request.candidate.PublishedDocBuilderService;
import org.tctalent.server.request.candidate.PublishedDocColumnDef;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.FileSystemService;

class GoogleSheetPublisherServiceImplTest {

  @Test
  public void throwsIfTooManyRows() {
    GridRange grid = new GridRange()
        .setStartRowIndex(5)
        .setEndRowIndex(10); // 5 rows max

    assertThrows(IOException.class, () -> {
      validateDataRangeCapacity("Main!B6:W10", grid, 6); // trying to write 6 rows
    });
  }

  @Test
  public void doesNotThrowIfFitsExactly() {
    GridRange grid = new GridRange()
        .setStartRowIndex(5)
        .setEndRowIndex(10); // 5 rows

    assertDoesNotThrow(() -> {
      validateDataRangeCapacity("Main!B6:W10", grid, 5);
    });
  }

  @Test
  public void doesNotThrowIfRangeHasSpareCapacity() {
    GridRange grid = new GridRange()
        .setStartRowIndex(5)
        .setEndRowIndex(10); // 5 rows

    assertDoesNotThrow(() -> {
      validateDataRangeCapacity("Main!B6:W10", grid, 3);
    });
  }

  @Test
  public void throwsUsefulMessageIfTooManyRows() {
    GridRange grid = new GridRange()
        .setStartRowIndex(5)
        .setEndRowIndex(10); // 5 rows max

    IOException exception = assertThrows(IOException.class, () -> {
      validateDataRangeCapacity("Main!B6:W10", grid, 7);
    });

    assertEquals(
        "Attempting to publish too many candidates (6) to the sheet Main!B6:W10 "
            + "which can hold a maximum of 4 rows.",
        exception.getMessage()
    );
  }

  @Test
  public void computeNumberOfRowsWithoutExpandingColumnCountsOneRowPerCandidate() {
    PublishedDocBuilderService builderService = mock(PublishedDocBuilderService.class);
    GoogleSheetPublisherServiceImpl service = googleSheetPublisherService(builderService);

    List<Candidate> candidates = List.of(
        mock(Candidate.class),
        mock(Candidate.class),
        mock(Candidate.class)
    );

    Integer result = ReflectionTestUtils.invokeMethod(
        service,
        "computeNumberOfRows",
        candidates,
        null
    );

    assertEquals(3, result);
  }

  @Test
  public void computeNumberOfRowsWithExpandingColumnAddsExtraRows() {
    PublishedDocBuilderService builderService = mock(PublishedDocBuilderService.class);
    GoogleSheetPublisherServiceImpl service = googleSheetPublisherService(builderService);

    Candidate candidateWithExtraRows = mock(Candidate.class);
    Candidate candidateWithoutExtraRows = mock(Candidate.class);
    PublishedDocColumnDef expandingColumnDef = mock(PublishedDocColumnDef.class);
    HasMultipleRows expandingData = mock(HasMultipleRows.class);

    when(expandingData.nRows()).thenReturn(2);
    when(builderService.loadExpandingData(candidateWithExtraRows, expandingColumnDef))
        .thenReturn(expandingData);
    when(builderService.loadExpandingData(candidateWithoutExtraRows, expandingColumnDef))
        .thenReturn(null);

    Integer result = ReflectionTestUtils.invokeMethod(
        service,
        "computeNumberOfRows",
        List.of(candidateWithExtraRows, candidateWithoutExtraRows),
        expandingColumnDef
    );

    assertEquals(4, result);
  }

  @Test
  public void computeAlignmentRequestBuildsRepeatCellRequest() {
    GoogleSheetPublisherServiceImpl service =
        googleSheetPublisherService(mock(PublishedDocBuilderService.class));
    GridRange range = gridRange();

    Request request = ReflectionTestUtils.invokeMethod(
        service,
        "computeAlignmentRequest",
        range,
        "CENTER"
    );

    assertNotNull(request);
    assertNotNull(request.getRepeatCell());
    assertEquals(range, request.getRepeatCell().getRange());
    assertEquals(
        "CENTER",
        request.getRepeatCell().getCell().getUserEnteredFormat().getHorizontalAlignment()
    );
    assertEquals("userEnteredFormat(horizontalAlignment)", request.getRepeatCell().getFields());
  }

  @Test
  public void computeColumnWidthRequestBuildsUpdateDimensionPropertiesRequest() {
    GoogleSheetPublisherServiceImpl service =
        googleSheetPublisherService(mock(PublishedDocBuilderService.class));
    GridRange range = gridRange();

    Request request = ReflectionTestUtils.invokeMethod(
        service,
        "computeColumnWidthRequest",
        range,
        240
    );

    assertNotNull(request);
    assertNotNull(request.getUpdateDimensionProperties());
    assertEquals(
        range.getSheetId(),
        request.getUpdateDimensionProperties().getRange().getSheetId()
    );
    assertEquals(
        "COLUMNS",
        request.getUpdateDimensionProperties().getRange().getDimension()
    );
    assertEquals(
        range.getStartColumnIndex(),
        request.getUpdateDimensionProperties().getRange().getStartIndex()
    );
    assertEquals(
        range.getEndColumnIndex(),
        request.getUpdateDimensionProperties().getRange().getEndIndex()
    );
    assertEquals(
        240,
        request.getUpdateDimensionProperties().getProperties().getPixelSize()
    );
    assertEquals("pixelSize", request.getUpdateDimensionProperties().getFields());
  }

  @Test
  public void computeDropDownsRequestBuildsStrictOneOfListValidation() {
    GoogleSheetPublisherServiceImpl service =
        googleSheetPublisherService(mock(PublishedDocBuilderService.class));
    GridRange range = gridRange();

    Request request = ReflectionTestUtils.invokeMethod(
        service,
        "computeDropDownsRequest",
        range,
        List.of("Yes", "No", "Maybe")
    );

    assertNotNull(request);
    assertNotNull(request.getSetDataValidation());
    assertEquals(range, request.getSetDataValidation().getRange());
    assertTrue(request.getSetDataValidation().getRule().getStrict());
    assertTrue(request.getSetDataValidation().getRule().getShowCustomUi());
    assertEquals(
        "ONE_OF_LIST",
        request.getSetDataValidation().getRule().getCondition().getType()
    );
    assertEquals(
        3,
        request.getSetDataValidation().getRule().getCondition().getValues().size()
    );
    assertEquals(
        "Yes",
        request.getSetDataValidation().getRule().getCondition().getValues().get(0)
            .getUserEnteredValue()
    );
    assertEquals(
        "No",
        request.getSetDataValidation().getRule().getCondition().getValues().get(1)
            .getUserEnteredValue()
    );
    assertEquals(
        "Maybe",
        request.getSetDataValidation().getRule().getCondition().getValues().get(2)
            .getUserEnteredValue()
    );
  }

  @Test
  public void computeAddNamedRangeRequestBuildsNamedRangeRequest() {
    GoogleSheetPublisherServiceImpl service =
        googleSheetPublisherService(mock(PublishedDocBuilderService.class));
    GridRange range = gridRange();

    Request request = ReflectionTestUtils.invokeMethod(
        service,
        "computeAddNamedRangeRequest",
        range,
        "candidateFeedback"
    );

    assertNotNull(request);
    assertNotNull(request.getAddNamedRange());
    assertEquals(
        "candidateFeedback",
        request.getAddNamedRange().getNamedRange().getName()
    );
    assertEquals(range, request.getAddNamedRange().getNamedRange().getRange());
  }

  @Test
  public void dataInSheetConvertsDataColumnToSheetRangeSkippingHeaderRow() throws Exception {
    GridRange dataRange = new GridRange()
        .setSheetId(123)
        .setStartRowIndex(5)
        .setEndRowIndex(20)
        .setStartColumnIndex(1)
        .setEndColumnIndex(10);

    Class<?> dataInSheetClass = Class.forName(
        "org.tctalent.server.service.db.impl.GoogleSheetPublisherServiceImpl$DataInSheet"
    );
    Constructor<?> constructor = dataInSheetClass.getDeclaredConstructor(
        Integer.class,
        GridRange.class,
        int.class
    );
    constructor.setAccessible(true);

    Object dataInSheet = constructor.newInstance(123, dataRange, 6);

    Method getColumnRange = dataInSheetClass.getDeclaredMethod("getColumnRange", int.class);
    getColumnRange.setAccessible(true);

    GridRange columnRange = (GridRange) getColumnRange.invoke(dataInSheet, 3);

    assertEquals(123, columnRange.getSheetId());
    assertEquals(6, columnRange.getStartRowIndex());
    assertEquals(11, columnRange.getEndRowIndex());
    assertEquals(4, columnRange.getStartColumnIndex());
    assertEquals(5, columnRange.getEndColumnIndex());
  }

  private GoogleSheetPublisherServiceImpl googleSheetPublisherService(
      PublishedDocBuilderService builderService) {
    return new GoogleSheetPublisherServiceImpl(
        mock(CandidateService.class),
        mock(GoogleDriveConfig.class),
        mock(FileSystemService.class),
        builderService
    );
  }

  private GridRange gridRange() {
    return new GridRange()
        .setSheetId(123)
        .setStartRowIndex(5)
        .setEndRowIndex(10)
        .setStartColumnIndex(2)
        .setEndColumnIndex(3);
  }

}
