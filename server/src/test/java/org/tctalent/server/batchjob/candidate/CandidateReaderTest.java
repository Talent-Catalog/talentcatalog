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

package org.tctalent.server.batchjob.candidate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.SavedSearch;
import org.tctalent.server.request.candidate.SavedListGetRequest;
import org.tctalent.server.request.candidate.SavedSearchGetRequest;
import org.tctalent.server.request.candidate.SearchCandidateRequest;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.SavedSearchService;

@ExtendWith(MockitoExtension.class)
class CandidateReaderTest {

  @Mock
  private CandidateService candidateService;

  @Mock
  private SavedSearchService savedSearchService;

  @Mock
  private StepExecution stepExecution;

  private Candidate firstCandidate;
  private Candidate secondCandidate;
  private Candidate thirdCandidate;

  @BeforeEach
  void setUp() {
    firstCandidate = mock(Candidate.class);
    secondCandidate = mock(Candidate.class);
    thirdCandidate = mock(Candidate.class);
  }

  @Test
  void savedListReaderReadsCandidatesAcrossPagesAndReturnsNullAtEnd() throws Exception {
    SavedList savedList = new SavedList();

    given(candidateService.getSavedListCandidates(
        same(savedList),
        any(SavedListGetRequest.class)
    )).willReturn(
        new PageImpl<>(
            List.of(firstCandidate, secondCandidate),
            PageRequest.of(0, 2),
            3
        ),
        new PageImpl<>(
            List.of(thirdCandidate),
            PageRequest.of(1, 2),
            3
        )
    );

    CandidateReader reader = new CandidateReader(savedList, 2, candidateService);

    assertSame(firstCandidate, reader.read());
    assertSame(secondCandidate, reader.read());
    assertSame(thirdCandidate, reader.read());
    assertNull(reader.read());

    ArgumentCaptor<SavedListGetRequest> requestCaptor =
        ArgumentCaptor.forClass(SavedListGetRequest.class);

    verify(candidateService, times(2))
        .getSavedListCandidates(same(savedList), requestCaptor.capture());

    List<SavedListGetRequest> requests = requestCaptor.getAllValues();

    assertEquals(0, requests.get(0).getPageNumber());
    assertEquals(2, requests.get(0).getPageSize());

    assertEquals(1, requests.get(1).getPageNumber());
    assertEquals(2, requests.get(1).getPageSize());
  }

  @Test
  void savedSearchReaderUsesSavedSearchServiceWithSavedSearchGetRequest() throws Exception {
    SavedSearch savedSearch = mock(SavedSearch.class);
    given(savedSearch.getId()).willReturn(55L);

    given(savedSearchService.searchCandidates(
        eq(55L),
        any(SavedSearchGetRequest.class)
    )).willReturn(
        new PageImpl<>(
            List.of(firstCandidate),
            PageRequest.of(0, 5),
            1
        )
    );

    CandidateReader reader = new CandidateReader(savedSearch, 5, savedSearchService);

    assertSame(firstCandidate, reader.read());
    assertNull(reader.read());

    ArgumentCaptor<SavedSearchGetRequest> requestCaptor =
        ArgumentCaptor.forClass(SavedSearchGetRequest.class);

    verify(savedSearchService)
        .searchCandidates(eq(55L), requestCaptor.capture());

    SavedSearchGetRequest request = requestCaptor.getValue();

    assertEquals(0, request.getPageNumber());
    assertEquals(5, request.getPageSize());
  }

  @Test
  void searchCandidateRequestReaderUsesExistingRequestAndSetsPaging() throws Exception {
    SearchCandidateRequest request = new SearchCandidateRequest();

    given(savedSearchService.searchCandidates(same(request)))
        .willReturn(
            new PageImpl<>(
                List.of(firstCandidate),
                PageRequest.of(0, 3),
                1
            )
        );

    CandidateReader reader = new CandidateReader(request, 3, savedSearchService);

    assertSame(firstCandidate, reader.read());
    assertNull(reader.read());

    assertEquals(1, request.getPageNumber());
    assertEquals(3, request.getPageSize());

    verify(savedSearchService).searchCandidates(same(request));
  }

  @Test
  void beforeStepResetsReaderStateSoReadingStartsAgainFromFirstPage() throws Exception {
    SavedList savedList = new SavedList();

    given(candidateService.getSavedListCandidates(
        same(savedList),
        any(SavedListGetRequest.class)
    )).willReturn(
        new PageImpl<>(
            List.of(firstCandidate, secondCandidate),
            PageRequest.of(0, 2),
            2
        ),
        new PageImpl<>(
            List.of(firstCandidate, secondCandidate),
            PageRequest.of(0, 2),
            2
        )
    );

    CandidateReader reader = new CandidateReader(savedList, 2, candidateService);

    assertSame(firstCandidate, reader.read());

    reader.beforeStep(stepExecution);

    assertSame(firstCandidate, reader.read());

    verify(candidateService, times(2))
        .getSavedListCandidates(same(savedList), any(SavedListGetRequest.class));
  }

  @Test
  void afterStepReturnsCompleted() {
    CandidateReader reader = new CandidateReader(new SavedList(), 10, candidateService);

    ExitStatus result = reader.afterStep(stepExecution);

    assertEquals(ExitStatus.COMPLETED, result);
  }

  @Test
  void readWrapsCandidateServiceFailureInGenericException() {
    SavedList savedList = new SavedList();
    RuntimeException serviceFailure = new RuntimeException("database failed");

    given(candidateService.getSavedListCandidates(
        same(savedList),
        any(SavedListGetRequest.class)
    )).willThrow(serviceFailure);

    CandidateReader reader = new CandidateReader(savedList, 10, candidateService);

    Exception exception = assertThrows(Exception.class, reader::read);

    assertEquals("Failed to read Candidate", exception.getMessage());
    assertSame(serviceFailure, exception.getCause());
  }

  @Test
  void readWrapsMissingReaderSourceFailureInGenericException() {
    CandidateReader reader = new CandidateReader((SavedList) null, 10, candidateService);

    Exception exception = assertThrows(Exception.class, reader::read);

    assertEquals("Failed to read Candidate", exception.getMessage());
    assertInstanceOf(RuntimeException.class, exception.getCause());
    assertEquals(
        "No saved search or saved list specified",
        exception.getCause().getMessage()
    );
  }

  @Test
  void readWrapsDefensiveInnerMissingReaderSourceBranchInGenericException() {
    SearchCandidateRequest request = mock(SearchCandidateRequest.class);
    CandidateReader reader = new CandidateReader(request, 10, savedSearchService);

    doAnswer(invocation -> {
      ReflectionTestUtils.setField(reader, "searchCandidateRequest", null);
      return null;
    }).when(request).setPageSize(10);

    Exception exception = assertThrows(Exception.class, reader::read);

    assertEquals("Failed to read Candidate", exception.getMessage());
    assertInstanceOf(RuntimeException.class, exception.getCause());
    assertEquals(
        "No saved search or saved list specified",
        exception.getCause().getMessage()
    );
  }
}