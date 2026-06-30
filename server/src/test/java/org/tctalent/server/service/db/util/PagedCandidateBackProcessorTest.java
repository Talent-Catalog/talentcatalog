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
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.request.candidate.SearchCandidateRequest;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.SavedSearchService;
import org.tctalent.server.util.background.PageContext;

@ExtendWith(MockitoExtension.class)
class PagedCandidateBackProcessorTest {

  private static final String ACTION = "TestAction";

  @Mock
  private CandidateService candidateService;

  @Mock
  private SavedSearchService savedSearchService;

  private SearchCandidateRequest searchCandidateRequest;
  private TestPagedCandidateBackProcessor processor;

  @BeforeEach
  void setUp() {
    searchCandidateRequest = new SearchCandidateRequest();

    processor = new TestPagedCandidateBackProcessor(
        ACTION,
        searchCandidateRequest,
        candidateService,
        savedSearchService
    );
  }

  @Test
  void process_whenNoPageHasBeenProcessed_processesFirstPageAndReturnsFalseWhenMorePagesExist() {
    PageContext ctx = new PageContext(null);
    List<Candidate> candidates = List.of(mock(Candidate.class), mock(Candidate.class));

    Page<Candidate> pageOfCandidates =
        new PageImpl<>(candidates, PageRequest.of(0, 2), 5);

    when(savedSearchService.searchCandidates(same(searchCandidateRequest)))
        .thenReturn(pageOfCandidates);

    boolean finished = processor.process(ctx);

    assertFalse(finished);
    assertEquals(0, searchCandidateRequest.getPageNumber());
    assertEquals(0, ctx.getLastProcessedPage());

    assertEquals(1, processor.processCandidatesCallCount);
    assertSame(candidateService, processor.receivedCandidateService);
    assertIterableEquals(candidates, processor.receivedCandidates);

    verify(savedSearchService).searchCandidates(same(searchCandidateRequest));
  }

  @Test
  void process_whenPageHasAlreadyBeenProcessed_processesNextPageAndReturnsTrueOnLastPage() {
    PageContext ctx = new PageContext(0);
    List<Candidate> candidates = List.of(mock(Candidate.class));

    Page<Candidate> pageOfCandidates =
        new PageImpl<>(candidates, PageRequest.of(1, 2), 3);

    when(savedSearchService.searchCandidates(same(searchCandidateRequest)))
        .thenReturn(pageOfCandidates);

    boolean finished = processor.process(ctx);

    assertTrue(finished);
    assertEquals(1, searchCandidateRequest.getPageNumber());
    assertEquals(1, ctx.getLastProcessedPage());

    assertEquals(1, processor.processCandidatesCallCount);
    assertSame(candidateService, processor.receivedCandidateService);
    assertIterableEquals(candidates, processor.receivedCandidates);

    verify(savedSearchService).searchCandidates(same(searchCandidateRequest));
  }

  @Test
  void process_whenSearchThrowsException_marksPageProcessedAndReturnsTrue() {
    PageContext ctx = new PageContext(null);

    when(savedSearchService.searchCandidates(same(searchCandidateRequest)))
        .thenThrow(new RuntimeException("Search failed"));

    boolean finished = processor.process(ctx);

    assertTrue(finished);
    assertEquals(0, searchCandidateRequest.getPageNumber());
    assertEquals(0, ctx.getLastProcessedPage());

    assertEquals(0, processor.processCandidatesCallCount);

    verify(savedSearchService).searchCandidates(same(searchCandidateRequest));
  }

  @Test
  void process_whenProcessCandidatesThrowsException_catchesExceptionAndUsesPageHasNextForReturnValue() {
    PageContext ctx = new PageContext(null);
    List<Candidate> candidates = List.of(mock(Candidate.class), mock(Candidate.class));

    Page<Candidate> pageOfCandidates =
        new PageImpl<>(candidates, PageRequest.of(0, 2), 5);

    when(savedSearchService.searchCandidates(same(searchCandidateRequest)))
        .thenReturn(pageOfCandidates);

    processor.exceptionToThrow = new RuntimeException("Processing failed");

    boolean finished = processor.process(ctx);

    assertFalse(finished);
    assertEquals(0, searchCandidateRequest.getPageNumber());
    assertEquals(0, ctx.getLastProcessedPage());

    assertEquals(1, processor.processCandidatesCallCount);
    assertSame(candidateService, processor.receivedCandidateService);
    assertIterableEquals(candidates, processor.receivedCandidates);

    verify(savedSearchService).searchCandidates(same(searchCandidateRequest));
  }

  private static class TestPagedCandidateBackProcessor extends PagedCandidateBackProcessor {

    private int processCandidatesCallCount;
    private CandidateService receivedCandidateService;
    private List<Candidate> receivedCandidates;
    private RuntimeException exceptionToThrow;

    private TestPagedCandidateBackProcessor(
        String action,
        SearchCandidateRequest searchCandidateRequest,
        CandidateService candidateService,
        SavedSearchService savedSearchService
    ) {
      super(action, searchCandidateRequest, candidateService, savedSearchService);
    }

    @Override
    protected void processCandidates(
        CandidateService candidateService,
        List<Candidate> candidates
    ) {
      processCandidatesCallCount++;
      receivedCandidateService = candidateService;
      receivedCandidates = candidates;

      if (exceptionToThrow != null) {
        throw exceptionToThrow;
      }
    }
  }
}