/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.db.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.request.list.SearchSavedListRequest;
import org.tctalent.server.service.db.SavedListService;
import org.tctalent.server.util.background.PageContext;

@ExtendWith(MockitoExtension.class)
class PagedSavedListBackProcessorTest {

  private static final String ACTION = "TestAction";

  @Mock
  private SavedListService savedListService;

  private SearchSavedListRequest searchSavedListRequest;
  private TestPagedSavedListBackProcessor processor;

  @BeforeEach
  void setUp() {
    searchSavedListRequest = new SearchSavedListRequest();

    processor = new TestPagedSavedListBackProcessor(
        ACTION,
        searchSavedListRequest,
        savedListService
    );
  }

  @Test
  void process_whenNoPageHasBeenProcessed_processesFirstPageAndReturnsFalseWhenMorePagesExist() {
    PageContext ctx = new PageContext(null);
    List<SavedList> savedLists = List.of(mock(SavedList.class), mock(SavedList.class));

    Page<SavedList> pageOfSavedLists =
        new PageImpl<>(savedLists, PageRequest.of(0, 2), 5);

    when(savedListService.searchPaged(same(searchSavedListRequest)))
        .thenReturn(pageOfSavedLists);

    boolean finished = processor.process(ctx);

    assertFalse(finished);
    assertEquals(0, searchSavedListRequest.getPageNumber());
    assertEquals(0, ctx.getLastProcessedPage());

    assertEquals(1, processor.processSavedListsCallCount);
    assertSame(savedListService, processor.receivedSavedListService);
    assertIterableEquals(savedLists, processor.receivedSavedLists);

    verify(savedListService).searchPaged(same(searchSavedListRequest));
  }

  @Test
  void process_whenPageHasAlreadyBeenProcessed_processesNextPageAndReturnsTrueOnLastPage() {
    PageContext ctx = new PageContext(0);
    List<SavedList> savedLists = List.of(mock(SavedList.class));

    Page<SavedList> pageOfSavedLists =
        new PageImpl<>(savedLists, PageRequest.of(1, 2), 3);

    when(savedListService.searchPaged(same(searchSavedListRequest)))
        .thenReturn(pageOfSavedLists);

    boolean finished = processor.process(ctx);

    assertTrue(finished);
    assertEquals(1, searchSavedListRequest.getPageNumber());
    assertEquals(1, ctx.getLastProcessedPage());

    assertEquals(1, processor.processSavedListsCallCount);
    assertSame(savedListService, processor.receivedSavedListService);
    assertIterableEquals(savedLists, processor.receivedSavedLists);

    verify(savedListService).searchPaged(same(searchSavedListRequest));
  }

  @Test
  void process_whenSearchPagedThrowsException_marksPageProcessedAndReturnsTrue() {
    PageContext ctx = new PageContext(null);

    when(savedListService.searchPaged(same(searchSavedListRequest)))
        .thenThrow(new RuntimeException("Search failed"));

    boolean finished = processor.process(ctx);

    assertTrue(finished);
    assertEquals(0, searchSavedListRequest.getPageNumber());
    assertEquals(0, ctx.getLastProcessedPage());

    assertEquals(0, processor.processSavedListsCallCount);

    verify(savedListService).searchPaged(same(searchSavedListRequest));
  }

  @Test
  void process_whenProcessSavedListsThrowsException_catchesExceptionAndUsesPageHasNextForReturnValue() {
    PageContext ctx = new PageContext(null);
    List<SavedList> savedLists = List.of(mock(SavedList.class), mock(SavedList.class));

    Page<SavedList> pageOfSavedLists =
        new PageImpl<>(savedLists, PageRequest.of(0, 2), 5);

    when(savedListService.searchPaged(same(searchSavedListRequest)))
        .thenReturn(pageOfSavedLists);

    processor.exceptionToThrow = new RuntimeException("Processing failed");

    boolean finished = processor.process(ctx);

    assertFalse(finished);
    assertEquals(0, searchSavedListRequest.getPageNumber());
    assertEquals(0, ctx.getLastProcessedPage());

    assertEquals(1, processor.processSavedListsCallCount);
    assertSame(savedListService, processor.receivedSavedListService);
    assertIterableEquals(savedLists, processor.receivedSavedLists);

    verify(savedListService).searchPaged(same(searchSavedListRequest));
  }

  private static class TestPagedSavedListBackProcessor extends PagedSavedListBackProcessor {

    private int processSavedListsCallCount;
    private SavedListService receivedSavedListService;
    private List<SavedList> receivedSavedLists;
    private RuntimeException exceptionToThrow;

    private TestPagedSavedListBackProcessor(
        String action,
        SearchSavedListRequest searchSavedListRequest,
        SavedListService savedListService
    ) {
      super(action, searchSavedListRequest, savedListService);
    }

    @Override
    protected void processSavedLists(
        SavedListService savedListService,
        List<SavedList> savedLists
    ) {
      processSavedListsCallCount++;
      receivedSavedListService = savedListService;
      receivedSavedLists = savedLists;

      if (exceptionToThrow != null) {
        throw exceptionToThrow;
      }
    }
  }
}