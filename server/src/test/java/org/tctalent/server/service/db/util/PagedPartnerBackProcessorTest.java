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
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.request.partner.SearchPartnerRequest;
import org.tctalent.server.service.db.PartnerService;
import org.tctalent.server.util.background.PageContext;

@ExtendWith(MockitoExtension.class)
class PagedPartnerBackProcessorTest {

  private static final String ACTION = "TestAction";

  @Mock
  private PartnerService partnerService;

  private SearchPartnerRequest searchPartnerRequest;
  private TestPagedPartnerBackProcessor processor;

  @BeforeEach
  void setUp() {
    searchPartnerRequest = new SearchPartnerRequest();

    processor = new TestPagedPartnerBackProcessor(
        ACTION,
        searchPartnerRequest,
        partnerService
    );
  }

  @Test
  void process_whenNoPageHasBeenProcessed_processesFirstPageAndReturnsFalseWhenMorePagesExist() {
    PageContext ctx = new PageContext(null);
    List<PartnerImpl> partners = List.of(mock(PartnerImpl.class), mock(PartnerImpl.class));

    Page<PartnerImpl> pageOfPartners =
        new PageImpl<>(partners, PageRequest.of(0, 2), 5);

    when(partnerService.searchPaged(same(searchPartnerRequest)))
        .thenReturn(pageOfPartners);

    boolean finished = processor.process(ctx);

    assertFalse(finished);
    assertEquals(0, searchPartnerRequest.getPageNumber());
    assertEquals(0, ctx.getLastProcessedPage());

    assertEquals(1, processor.processPartnersCallCount);
    assertSame(partnerService, processor.receivedPartnerService);
    assertIterableEquals(partners, processor.receivedPartners);

    verify(partnerService).searchPaged(same(searchPartnerRequest));
  }

  @Test
  void process_whenPageHasAlreadyBeenProcessed_processesNextPageAndReturnsTrueOnLastPage() {
    PageContext ctx = new PageContext(0);
    List<PartnerImpl> partners = List.of(mock(PartnerImpl.class));

    Page<PartnerImpl> pageOfPartners =
        new PageImpl<>(partners, PageRequest.of(1, 2), 3);

    when(partnerService.searchPaged(same(searchPartnerRequest)))
        .thenReturn(pageOfPartners);

    boolean finished = processor.process(ctx);

    assertTrue(finished);
    assertEquals(1, searchPartnerRequest.getPageNumber());
    assertEquals(1, ctx.getLastProcessedPage());

    assertEquals(1, processor.processPartnersCallCount);
    assertSame(partnerService, processor.receivedPartnerService);
    assertIterableEquals(partners, processor.receivedPartners);

    verify(partnerService).searchPaged(same(searchPartnerRequest));
  }

  @Test
  void process_whenSearchPagedThrowsException_marksPageProcessedAndReturnsTrue() {
    PageContext ctx = new PageContext(null);

    when(partnerService.searchPaged(same(searchPartnerRequest)))
        .thenThrow(new RuntimeException("Search failed"));

    boolean finished = processor.process(ctx);

    assertTrue(finished);
    assertEquals(0, searchPartnerRequest.getPageNumber());
    assertEquals(0, ctx.getLastProcessedPage());

    assertEquals(0, processor.processPartnersCallCount);

    verify(partnerService).searchPaged(same(searchPartnerRequest));
  }

  @Test
  void process_whenProcessPartnersThrowsException_catchesExceptionAndUsesPageHasNextForReturnValue() {
    PageContext ctx = new PageContext(null);
    List<PartnerImpl> partners = List.of(mock(PartnerImpl.class), mock(PartnerImpl.class));

    Page<PartnerImpl> pageOfPartners =
        new PageImpl<>(partners, PageRequest.of(0, 2), 5);

    when(partnerService.searchPaged(same(searchPartnerRequest)))
        .thenReturn(pageOfPartners);

    processor.exceptionToThrow = new RuntimeException("Processing failed");

    boolean finished = processor.process(ctx);

    assertFalse(finished);
    assertEquals(0, searchPartnerRequest.getPageNumber());
    assertEquals(0, ctx.getLastProcessedPage());

    assertEquals(1, processor.processPartnersCallCount);
    assertSame(partnerService, processor.receivedPartnerService);
    assertIterableEquals(partners, processor.receivedPartners);

    verify(partnerService).searchPaged(same(searchPartnerRequest));
  }

  private static class TestPagedPartnerBackProcessor extends PagedPartnerBackProcessor {

    private int processPartnersCallCount;
    private PartnerService receivedPartnerService;
    private List<PartnerImpl> receivedPartners;
    private RuntimeException exceptionToThrow;

    private TestPagedPartnerBackProcessor(
        String action,
        SearchPartnerRequest searchPartnerRequest,
        PartnerService partnerService
    ) {
      super(action, searchPartnerRequest, partnerService);
    }

    @Override
    protected void processPartners(
        PartnerService partnerService,
        List<PartnerImpl> partners
    ) {
      processPartnersCallCount++;
      receivedPartnerService = partnerService;
      receivedPartners = partners;

      if (exceptionToThrow != null) {
        throw exceptionToThrow;
      }
    }
  }
}