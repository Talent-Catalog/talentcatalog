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
import org.tctalent.server.model.db.SavedSearch;
import org.tctalent.server.request.search.SearchSavedSearchRequest;
import org.tctalent.server.service.db.SavedSearchService;
import org.tctalent.server.util.background.PageContext;

@ExtendWith(MockitoExtension.class)
class PagedSavedSearchBackProcessorTest {

  private static final String ACTION = "TestAction";

  @Mock
  private SavedSearchService savedSearchService;

  private SearchSavedSearchRequest searchSavedSearchRequest;
  private TestPagedSavedSearchBackProcessor processor;

  @BeforeEach
  void setUp() {
    searchSavedSearchRequest = new SearchSavedSearchRequest();

    processor = new TestPagedSavedSearchBackProcessor(
        ACTION,
        searchSavedSearchRequest,
        savedSearchService
    );
  }

  @Test
  void process_whenNoPageHasBeenProcessed_processesFirstPageAndReturnsFalseWhenMorePagesExist() {
    PageContext ctx = new PageContext(null);
    List<SavedSearch> savedSearches = List.of(mock(SavedSearch.class), mock(SavedSearch.class));

    Page<SavedSearch> pageOfSavedSearches =
        new PageImpl<>(savedSearches, PageRequest.of(0, 2), 5);

    when(savedSearchService.searchPaged(same(searchSavedSearchRequest)))
        .thenReturn(pageOfSavedSearches);

    boolean finished = processor.process(ctx);

    assertFalse(finished);
    assertEquals(0, searchSavedSearchRequest.getPageNumber());
    assertEquals(0, ctx.getLastProcessedPage());

    assertEquals(1, processor.processSavedSearchesCallCount);
    assertSame(savedSearchService, processor.receivedSavedSearchService);
    assertIterableEquals(savedSearches, processor.receivedSavedSearches);

    verify(savedSearchService).searchPaged(same(searchSavedSearchRequest));
  }

  @Test
  void process_whenPageHasAlreadyBeenProcessed_processesNextPageAndReturnsTrueOnLastPage() {
    PageContext ctx = new PageContext(0);
    List<SavedSearch> savedSearches = List.of(mock(SavedSearch.class));

    Page<SavedSearch> pageOfSavedSearches =
        new PageImpl<>(savedSearches, PageRequest.of(1, 2), 3);

    when(savedSearchService.searchPaged(same(searchSavedSearchRequest)))
        .thenReturn(pageOfSavedSearches);

    boolean finished = processor.process(ctx);

    assertTrue(finished);
    assertEquals(1, searchSavedSearchRequest.getPageNumber());
    assertEquals(1, ctx.getLastProcessedPage());

    assertEquals(1, processor.processSavedSearchesCallCount);
    assertSame(savedSearchService, processor.receivedSavedSearchService);
    assertIterableEquals(savedSearches, processor.receivedSavedSearches);

    verify(savedSearchService).searchPaged(same(searchSavedSearchRequest));
  }

  @Test
  void process_whenSearchPagedThrowsException_marksPageProcessedAndReturnsTrue() {
    PageContext ctx = new PageContext(null);

    when(savedSearchService.searchPaged(same(searchSavedSearchRequest)))
        .thenThrow(new RuntimeException("Search failed"));

    boolean finished = processor.process(ctx);

    assertTrue(finished);
    assertEquals(0, searchSavedSearchRequest.getPageNumber());
    assertEquals(0, ctx.getLastProcessedPage());

    assertEquals(0, processor.processSavedSearchesCallCount);

    verify(savedSearchService).searchPaged(same(searchSavedSearchRequest));
  }

  @Test
  void process_whenProcessSavedSearchesThrowsException_catchesExceptionAndUsesPageHasNextForReturnValue() {
    PageContext ctx = new PageContext(null);
    List<SavedSearch> savedSearches = List.of(mock(SavedSearch.class), mock(SavedSearch.class));

    Page<SavedSearch> pageOfSavedSearches =
        new PageImpl<>(savedSearches, PageRequest.of(0, 2), 5);

    when(savedSearchService.searchPaged(same(searchSavedSearchRequest)))
        .thenReturn(pageOfSavedSearches);

    processor.exceptionToThrow = new RuntimeException("Processing failed");

    boolean finished = processor.process(ctx);

    assertFalse(finished);
    assertEquals(0, searchSavedSearchRequest.getPageNumber());
    assertEquals(0, ctx.getLastProcessedPage());

    assertEquals(1, processor.processSavedSearchesCallCount);
    assertSame(savedSearchService, processor.receivedSavedSearchService);
    assertIterableEquals(savedSearches, processor.receivedSavedSearches);

    verify(savedSearchService).searchPaged(same(searchSavedSearchRequest));
  }

  private static class TestPagedSavedSearchBackProcessor extends PagedSavedSearchBackProcessor {

    private int processSavedSearchesCallCount;
    private SavedSearchService receivedSavedSearchService;
    private List<SavedSearch> receivedSavedSearches;
    private RuntimeException exceptionToThrow;

    private TestPagedSavedSearchBackProcessor(
        String action,
        SearchSavedSearchRequest searchSavedSearchRequest,
        SavedSearchService savedSearchService
    ) {
      super(action, searchSavedSearchRequest, savedSearchService);
    }

    @Override
    protected void processSavedSearches(
        SavedSearchService savedSearchService,
        List<SavedSearch> savedSearches
    ) {
      processSavedSearchesCallCount++;
      receivedSavedSearchService = savedSearchService;
      receivedSavedSearches = savedSearches;

      if (exceptionToThrow != null) {
        throw exceptionToThrow;
      }
    }
  }
}