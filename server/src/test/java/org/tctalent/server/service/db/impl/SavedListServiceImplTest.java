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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.tctalent.server.configuration.GoogleDriveConfig;
import org.tctalent.server.configuration.SystemAdminConfiguration;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.exception.RegisteredListException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateSavedList;
import org.tctalent.server.model.db.ExportColumn;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.TaskAssignmentImpl;
import org.tctalent.server.model.db.TaskImpl;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.CandidateSavedListRepository;
import org.tctalent.server.repository.db.SavedListRepository;
import org.tctalent.server.repository.db.UserRepository;
import org.tctalent.server.repository.db.read.dto.CandidateReadDto;
import org.tctalent.server.request.IdsRequest;
import org.tctalent.server.request.candidate.PublishListRequest;
import org.tctalent.server.request.candidate.PublishedDocColumnType;
import org.tctalent.server.request.candidate.PublishedDocImportReport;
import org.tctalent.server.request.candidate.SavedListGetRequest;
import org.tctalent.server.request.candidate.UpdateCandidateListOppsRequest;
import org.tctalent.server.request.candidate.UpdateDisplayedFieldPathsRequest;
import org.tctalent.server.request.candidate.source.UpdateCandidateSourceDescriptionRequest;
import org.tctalent.server.request.link.UpdateShortNameRequest;
import org.tctalent.server.request.list.ContentUpdateType;
import org.tctalent.server.request.list.SearchSavedListRequest;
import org.tctalent.server.request.list.UpdateExplicitSavedListContentsRequest;
import org.tctalent.server.request.list.UpdateSavedListContentsRequest;
import org.tctalent.server.request.list.UpdateSavedListInfoRequest;
import org.tctalent.server.request.search.UpdateSharingRequest;
import org.tctalent.server.service.db.CandidateDtoFetchService;
import org.tctalent.server.service.db.CandidateOpportunityService;
import org.tctalent.server.service.db.DocPublisherService;
import org.tctalent.server.service.db.ExportColumnsService;
import org.tctalent.server.service.db.FileSystemService;
import org.tctalent.server.service.db.PublicIDService;
import org.tctalent.server.service.db.SalesforceJobOppService;
import org.tctalent.server.service.db.SalesforceService;
import org.tctalent.server.service.db.TaskAssignmentService;
import org.tctalent.server.service.db.UserService;
import org.tctalent.server.util.filesystem.GoogleFileSystemDrive;
import org.tctalent.server.util.filesystem.GoogleFileSystemFile;
import org.tctalent.server.util.filesystem.GoogleFileSystemFolder;

@ExtendWith(MockitoExtension.class)
class SavedListServiceImplTest {

  @Mock private CandidateRepository candidateRepository;
  @Mock private CandidateDtoFetchService candidateDtoFetchService;
  @Mock private CandidateSavedListRepository candidateSavedListRepository;
  @Mock private CandidateOpportunityService candidateOpportunityService;
  @Mock private ExportColumnsService exportColumnsService;
  @Mock private SavedListRepository savedListRepository;
  @Mock private DocPublisherService docPublisherService;
  @Mock private FileSystemService fileSystemService;
  @Mock private GoogleDriveConfig googleDriveConfig;
  @Mock private PublicIDService publicIDService;
  @Mock private SalesforceService salesforceService;
  @Mock private SalesforceJobOppService salesforceJobOppService;
  @Mock private TaskAssignmentService taskAssignmentService;
  @Mock private UserRepository userRepository;
  @Mock private UserService userService;

  private SavedListServiceImpl service;
  private User user;

  @BeforeEach
  void setUp() {
    service = new SavedListServiceImpl(
        candidateRepository,
        candidateDtoFetchService,
        candidateSavedListRepository,
        candidateOpportunityService,
        exportColumnsService,
        savedListRepository,
        docPublisherService,
        fileSystemService,
        googleDriveConfig,
        publicIDService,
        salesforceService,
        salesforceJobOppService,
        taskAssignmentService,
        userRepository,
        userService
    );

    user = user(10L);
  }

  @Test
  @DisplayName("addCandidateToList adds candidate, copies context, assigns missing list tasks, and creates candidate opp for submission list")
  void addCandidateToListAddsCandidateAssignsTasksAndCreatesOpp() {
    SavedList list = savedList(1L, "Submission", user);
    Candidate candidate = candidate(2L, "1002");

    TaskImpl task = task(3L);
    list.setTasks(new HashSet<>(Set.of(task)));

    SalesforceJobOpp jobOpp = jobOpp(4L, "SF-4", "Job");
    list.setRegisteredJob(true);
    list.setSfJobOpp(jobOpp);

    given(userService.getLoggedInUser()).willReturn(user);

    service.addCandidateToList(list, candidate, "context note");

    assertEquals(1, list.getCandidateSavedLists().size());
    CandidateSavedList csl = list.getCandidateSavedLists().iterator().next();
    assertSame(candidate, csl.getCandidate());
    assertSame(list, csl.getSavedList());
    assertEquals("context note", csl.getContextNote());
    assertTrue(candidate.getCandidateSavedLists().contains(csl));

    verify(taskAssignmentService)
        .assignTaskToCandidate(user, task, candidate, list, null);
    verify(candidateOpportunityService)
        .createUpdateCandidateOpportunities(List.of(candidate), jobOpp, null);
  }

  @Test
  @DisplayName("addCandidateToList skips duplicate candidate/list link")
  void addCandidateToListSkipsDuplicate() {
    SavedList list = savedList(1L, "List", user);
    Candidate candidate = candidate(2L, "1002");
    CandidateSavedList existing = new CandidateSavedList(candidate, list);

    list.getCandidateSavedLists().add(existing);
    candidate.getCandidateSavedLists().add(existing);

    service.addCandidateToList(list, candidate, "new context");

    assertEquals(1, list.getCandidateSavedLists().size());
    verify(taskAssignmentService, never())
        .assignTaskToCandidate(any(), any(), any(), any(), any());
    verify(candidateOpportunityService, never())
        .createUpdateCandidateOpportunities(anyList(), any(), any());
  }

  @Test
  @DisplayName("addCandidateToList copies context note from source list")
  void addCandidateToListCopiesContextFromSourceList() {
    SavedList source = savedList(1L, "Source", user);
    SavedList destination = savedList(2L, "Destination", user);
    Candidate candidate = candidate(3L, "1003");

    CandidateSavedList sourceCsl = new CandidateSavedList(candidate, source);
    sourceCsl.setContextNote("copied context");
    candidate.getCandidateSavedLists().add(sourceCsl);
    source.getCandidateSavedLists().add(sourceCsl);

    service.addCandidateToList(destination, candidate, source);

    CandidateSavedList destinationCsl = destination.getCandidateSavedLists().iterator().next();
    assertEquals("copied context", destinationCsl.getContextNote());
  }

  @Test
  @DisplayName("addCandidateToList rejects pending-terms candidate on submission list")
  void addCandidateToListRejectsPendingTermsCandidateOnSubmissionList() {
    SavedList list = savedList(1L, "Submission", user);
    list.setRegisteredJob(true);
    list.setSfJobOpp(jobOpp(10L, "SF-10", "Job"));

    Candidate candidate = candidate(2L, "1002");
    SavedList pendingTermsList = savedList(
        SystemAdminConfiguration.PENDING_TERMS_ACCEPTANCE_LIST_ID,
        "Pending Terms Acceptance",
        user
    );
    addCandidateToSavedList(pendingTermsList, candidate);

    assertThrows(
        InvalidRequestException.class,
        () -> service.addCandidateToList(list, candidate, "")
    );
  }

  @Test
  @DisplayName("removeCandidateFromList deletes link from repository and deactivates incomplete related tasks")
  void removeCandidateFromListDeletesAndDeactivatesTasks() {
    SavedList list = savedList(1L, "List", user);
    Candidate candidate = candidate(2L, "1002");
    TaskImpl task = task(3L);
    TaskAssignmentImpl assignment = activeAssignment(44L, task, list);

    list.setTasks(new HashSet<>(Set.of(task)));
    candidate.setTaskAssignments(new ArrayList<>(List.of(assignment)));

    CandidateSavedList csl = new CandidateSavedList(candidate, list);
    list.getCandidateSavedLists().add(csl);
    candidate.getCandidateSavedLists().add(csl);

    given(userService.getLoggedInUser()).willReturn(user);

    service.removeCandidateFromList(candidate, list);

    verify(candidateSavedListRepository).delete(new CandidateSavedList(candidate, list));
    verify(taskAssignmentService).deactivateTaskAssignment(user, 44L);
    assertFalse(list.getCandidateSavedLists().contains(csl));
    assertFalse(candidate.getCandidateSavedLists().contains(csl));
  }

  @Test
  @DisplayName("removeCandidateFromList swallows repository delete exception")
  void removeCandidateFromListSwallowsDeleteException() {
    SavedList list = savedList(1L, "List", user);
    Candidate candidate = candidate(2L, "1002");

    doThrow(new RuntimeException("delete failed"))
        .when(candidateSavedListRepository).delete(any(CandidateSavedList.class));

    assertDoesNotThrow(() -> service.removeCandidateFromList(candidate, list));
  }

  @Test
  @DisplayName("removeCandidateFromList by id throws when saved list missing")
  void removeCandidateFromListByIdThrowsWhenListMissing() {
    UpdateExplicitSavedListContentsRequest request = new UpdateExplicitSavedListContentsRequest();
    request.setCandidateIds(Set.of(1L));

    given(savedListRepository.findByIdLoadCandidates(99L)).willReturn(Optional.empty());

    assertThrows(
        NoSuchObjectException.class,
        () -> service.removeCandidateFromList(99L, request)
    );
  }

  @Test
  @DisplayName("removeCandidateFromList by id removes requested candidates")
  void removeCandidateFromListByIdRemovesCandidates() {
    SavedList list = savedList(1L, "List", user);
    Candidate candidate = candidate(2L, "1002");
    CandidateSavedList csl = new CandidateSavedList(candidate, list);
    list.getCandidateSavedLists().add(csl);
    candidate.getCandidateSavedLists().add(csl);

    UpdateExplicitSavedListContentsRequest request = new UpdateExplicitSavedListContentsRequest();
    request.setCandidateIds(Set.of(2L));

    given(savedListRepository.findByIdLoadCandidates(1L)).willReturn(Optional.of(list));
    given(candidateRepository.findById(2L)).willReturn(Optional.of(candidate));

    service.removeCandidateFromList(1L, request);

    verify(candidateSavedListRepository).delete(new CandidateSavedList(candidate, list));
  }

  @Test
  @DisplayName("associateTaskWithList adds task and assigns it to candidates missing active assignment")
  void associateTaskWithListAddsTaskAndAssignsToCandidates() {
    SavedList list = savedList(1L, "List", user);
    Candidate candidate = candidate(2L, "1002");
    addCandidateToSavedList(list, candidate);

    TaskImpl task = task(3L);

    service.associateTaskWithList(user, task, list);

    assertTrue(list.getTasks().contains(task));
    verify(savedListRepository).save(list);
    verify(taskAssignmentService).assignTaskToCandidate(user, task, candidate, list, null);
  }

  @Test
  @DisplayName("associateTaskWithList does not assign duplicate active task")
  void associateTaskWithListDoesNotAssignAlreadyActiveTask() {
    SavedList list = savedList(1L, "List", user);
    Candidate candidate = candidate(2L, "1002");
    TaskImpl task = task(3L);
    candidate.setTaskAssignments(new ArrayList<>(List.of(activeAssignment(4L, task, list))));
    addCandidateToSavedList(list, candidate);

    service.associateTaskWithList(user, task, list);

    verify(taskAssignmentService, never())
        .assignTaskToCandidate(any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("deassociateTaskFromList removes task and deactivates active incomplete related assignments")
  void deassociateTaskFromListRemovesTaskAndDeactivatesRelatedAssignments() {
    SavedList list = savedList(1L, "List", user);
    Candidate candidate = candidate(2L, "1002");
    TaskImpl task = task(3L);
    TaskAssignmentImpl assignment = activeAssignment(4L, task, list);

    list.setTasks(new HashSet<>(Set.of(task)));
    candidate.setTaskAssignments(new ArrayList<>(List.of(assignment)));
    addCandidateToSavedList(list, candidate);

    service.deassociateTaskFromList(user, task, list);

    assertFalse(list.getTasks().contains(task));
    verify(savedListRepository).save(list);
    verify(taskAssignmentService).deactivateTaskAssignment(user, 4L);
  }

  @Test
  @DisplayName("createSavedList creates non-registered list with public id")
  void createSavedListCreatesNonRegisteredList() {
    UpdateSavedListInfoRequest request = savedListInfoRequest("List", false, null, null);

    given(savedListRepository.findByNameIgnoreCase("List", user.getId()))
        .willReturn(Optional.empty());
    given(publicIDService.generatePublicID()).willReturn("public-id");
    given(savedListRepository.save(any(SavedList.class))).willAnswer(invocation -> invocation.getArgument(0));

    SavedList result = service.createSavedList(user, request);

    assertEquals("List", result.getName());
    assertEquals("public-id", result.getPublicId());
    assertFalse(result.getRegisteredJob());
    assertSame(user, result.getCreatedBy());
  }

  @Test
  @DisplayName("createSavedList throws duplicate for same active name and user")
  void createSavedListThrowsDuplicateForSameName() {
    UpdateSavedListInfoRequest request = savedListInfoRequest("List", false, null, null);
    SavedList existing = savedList(99L, "List", user);

    given(savedListRepository.findByNameIgnoreCase("List", user.getId()))
        .willReturn(Optional.of(existing));

    assertThrows(EntityExistsException.class, () -> service.createSavedList(user, request));
  }

  @Test
  @DisplayName("createSavedList ignores deleted duplicate")
  void createSavedListIgnoresDeletedDuplicate() {
    UpdateSavedListInfoRequest request = savedListInfoRequest("List", false, null, null);
    SavedList existing = savedList(99L, "List", user);
    existing.setStatus(Status.deleted);

    given(savedListRepository.findByNameIgnoreCase("List", user.getId()))
        .willReturn(Optional.of(existing));
    given(publicIDService.generatePublicID()).willReturn("public-id");
    given(savedListRepository.save(any(SavedList.class))).willAnswer(invocation -> invocation.getArgument(0));

    SavedList result = service.createSavedList(user, request);

    assertEquals("List", result.getName());
  }

  @Test
  @DisplayName("createSavedList throws when registered list has no Salesforce job")
  void createSavedListThrowsWhenRegisteredMissingSalesforceJob() {
    UpdateSavedListInfoRequest request = savedListInfoRequest("Registered", true, null, null);

    assertThrows(
        RegisteredListException.class,
        () -> service.createSavedList(user, request)
    );
  }

  @Test
  @DisplayName("createSavedList returns existing registered list for same Salesforce job")
  void createSavedListReturnsExistingRegisteredList() {
    SalesforceJobOpp job = jobOpp(1L, "SF-1", "Job");
    UpdateSavedListInfoRequest request = savedListInfoRequest(null, true, null, job);
    SavedList existing = savedList(2L, "Job*", user);

    given(savedListRepository.findRegisteredJobList("SF-1")).willReturn(Optional.of(existing));

    SavedList result = service.createSavedList(user, request);

    assertSame(existing, result);
    verify(savedListRepository, never()).save(any(SavedList.class));
  }

  @Test
  @DisplayName("createSavedList creates registered list and appends registered suffix")
  void createSavedListCreatesRegisteredList() {
    SalesforceJobOpp job = jobOpp(1L, "SF-1", "Job Name");
    UpdateSavedListInfoRequest request = savedListInfoRequest(null, true, null, job);

    given(savedListRepository.findRegisteredJobList("SF-1")).willReturn(Optional.empty());
    given(publicIDService.generatePublicID()).willReturn("public-id");
    given(savedListRepository.save(any(SavedList.class))).willAnswer(invocation -> invocation.getArgument(0));

    SavedList result = service.createSavedList(user, request);

    assertEquals("Job Name*", result.getName());
    assertTrue(result.getRegisteredJob());
    assertSame(job, result.getSfJobOpp());
  }

  @Test
  @DisplayName("createSavedList overload uses logged in user")
  void createSavedListOverloadUsesLoggedInUser() {
    UpdateSavedListInfoRequest request = savedListInfoRequest("List", false, null, null);

    given(userService.getLoggedInUser()).willReturn(user);
    given(savedListRepository.findByNameIgnoreCase("List", user.getId()))
        .willReturn(Optional.empty());
    given(publicIDService.generatePublicID()).willReturn("public-id");
    given(savedListRepository.save(any(SavedList.class))).willAnswer(invocation -> invocation.getArgument(0));

    SavedList result = service.createSavedList(request);

    assertEquals("List", result.getName());
  }

  @Test
  @DisplayName("createUpdateSalesforce delegates candidate opportunities for list candidates")
  void createUpdateSalesforceDelegates() {
    SavedList list = savedList(1L, "List", user);
    Candidate candidate = candidate(2L, "1002");
    SalesforceJobOpp job = jobOpp(3L, "SF-3", "Job");
    list.setSfJobOpp(job);
    addCandidateToSavedList(list, candidate);

    UpdateCandidateListOppsRequest request = mock(UpdateCandidateListOppsRequest.class);
    given(request.getSavedListId()).willReturn(1L);
    given(savedListRepository.findById(1L)).willReturn(Optional.of(list));

    service.createUpdateSalesforce(request);

    verify(candidateOpportunityService)
        .createUpdateCandidateOpportunities(list.getCandidates(), job, request.getCandidateOppParams());
  }

  @Test
  @DisplayName("get returns saved list")
  void getReturnsSavedList() {
    SavedList list = savedList(1L, "List", user);

    given(savedListRepository.findById(1L)).willReturn(Optional.of(list));

    assertSame(list, service.get(1L));
  }

  @Test
  @DisplayName("get throws when saved list missing")
  void getThrowsWhenMissing() {
    given(savedListRepository.findById(404L)).willReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class, () -> service.get(404L));
  }

  @Test
  @DisplayName("getByPublicId returns saved list")
  void getByPublicIdReturnsSavedList() {
    SavedList list = savedList(1L, "List", user);

    given(savedListRepository.findByPublicId("public-id")).willReturn(Optional.of(list));

    assertSame(list, service.getByPublicId("public-id"));
  }

  @Test
  @DisplayName("getByPublicId throws when missing")
  void getByPublicIdThrowsWhenMissing() {
    given(savedListRepository.findByPublicId("missing")).willReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class, () -> service.getByPublicId("missing"));
  }

  @Test
  @DisplayName("get by user and list name returns null when list name is null")
  void getByUserAndNullNameReturnsNull() {
    assertNull(service.get(user, null));
  }

  @Test
  @DisplayName("get by user and list name delegates to repository")
  void getByUserAndListNameDelegates() {
    SavedList list = savedList(1L, "List", user);

    given(savedListRepository.findByNameIgnoreCase("List", user.getId()))
        .willReturn(Optional.of(list));

    assertSame(list, service.get(user, "List"));
  }

  @Test
  @DisplayName("getCandidateIds delegates to repository union query")
  void getCandidateIdsDelegates() {
    given(savedListRepository.findUnionOfCandidates(List.of(1L)))
        .willReturn(Set.of(10L, 11L));

    assertEquals(Set.of(10L, 11L), service.getCandidateIds(1L));
  }

  @Test
  @DisplayName("getSavedListCandidateDtos builds fetch and count SQL")
  void getSavedListCandidateDtosBuildsSql() {
    SavedList list = savedList(1L, "List", user);
    list.setSfJobOpp(jobOpp(20L, "SF-20", "Job"));

    SavedListGetRequest request = new SavedListGetRequest();
    request.setKeyword("Ali");
    request.setShowClosedOpps(false);
    request.setPageNumber(0);
    request.setPageSize(10);

    Page<CandidateReadDto> page = new PageImpl<>(List.of(mock(CandidateReadDto.class)));
    given(candidateDtoFetchService.fetchPage(anyString(), anyString(), eq(request.getPageRequest())))
        .willReturn(page);

    Page<CandidateReadDto> result = service.getSavedListCandidateDtos(list, request);

    assertSame(page, result);

    ArgumentCaptor<String> fetchSql = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> countSql = ArgumentCaptor.forClass(String.class);
    verify(candidateDtoFetchService).fetchPage(
        fetchSql.capture(),
        countSql.capture(),
        eq(request.getPageRequest())
    );

    assertTrue(fetchSql.getValue().startsWith("select distinct candidate.id"));
    assertTrue(fetchSql.getValue().contains("saved_list_id = 1"));
    assertTrue(fetchSql.getValue().contains("lower(candidate_number) like '%ali%'"));
    assertTrue(fetchSql.getValue().contains("job_opp_id =20"));
    assertTrue(countSql.getValue().startsWith("select count(distinct candidate.id)"));
  }

  @Test
  @DisplayName("isEmpty returns true when list has no candidates")
  void isEmptyReturnsTrue() {
    SavedList list = savedList(1L, "List", user);

    given(savedListRepository.findById(1L)).willReturn(Optional.of(list));

    assertTrue(service.isEmpty(1L));
  }

  @Test
  @DisplayName("isEmpty returns false when list has candidates")
  void isEmptyReturnsFalse() {
    SavedList list = savedList(1L, "List", user);
    addCandidateToSavedList(list, candidate(2L, "1002"));

    given(savedListRepository.findById(1L)).willReturn(Optional.of(list));

    assertFalse(service.isEmpty(1L));
  }

  @Test
  @DisplayName("mergeSavedList throws when list missing")
  void mergeSavedListThrowsWhenMissing() {
    UpdateExplicitSavedListContentsRequest request = new UpdateExplicitSavedListContentsRequest();

    given(savedListRepository.findByIdLoadCandidates(99L)).willReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class, () -> service.mergeSavedList(99L, request));
  }

  @Test
  @DisplayName("mergeSavedList adds fetched candidates")
  void mergeSavedListAddsFetchedCandidates() {
    SavedList list = savedList(1L, "Destination", user);
    Candidate candidate = candidate(2L, "1002");

    UpdateExplicitSavedListContentsRequest request = new UpdateExplicitSavedListContentsRequest();
    request.setCandidateIds(Set.of(2L));
    request.setUpdateType(ContentUpdateType.add);

    given(savedListRepository.findByIdLoadCandidates(1L)).willReturn(Optional.of(list));
    given(candidateRepository.findById(2L)).willReturn(Optional.of(candidate));
    given(userService.getLoggedInUser()).willReturn(user);
    given(savedListRepository.save(list)).willReturn(list);

    service.mergeSavedList(1L, request);

    assertEquals(Set.of(candidate), list.getCandidates());
    verify(savedListRepository).save(list);
  }

  @Test
  @DisplayName("mergeSavedListFromInputStream imports candidate numbers and public ids")
  void mergeSavedListFromInputStreamImportsCandidateNumbersAndPublicIds() throws IOException {
    SavedList list = savedList(1L, "Destination", user);
    Candidate numberCandidate = candidate(2L, "123");
    Candidate publicIdCandidate = candidate(3L, "456");
    publicIdCandidate.setPublicId("abcdefghijklmnopqrstuv");

    String csv = "Candidate Number\n123\nabcdefghijklmnopqrstuv\n";

    given(candidateRepository.findByCandidateNumber("123")).willReturn(numberCandidate);
    given(candidateRepository.findByPublicId("abcdefghijklmnopqrstuv"))
        .willReturn(Optional.of(publicIdCandidate));
    given(savedListRepository.findByIdLoadCandidates(1L)).willReturn(Optional.of(list));
    given(candidateRepository.findById(2L)).willReturn(Optional.of(numberCandidate));
    given(candidateRepository.findById(3L)).willReturn(Optional.of(publicIdCandidate));
    given(userService.getLoggedInUser()).willReturn(user);
    given(savedListRepository.save(list)).willReturn(list);

    service.mergeSavedListFromInputStream(1L, inputStream(csv));

    assertEquals(Set.of(numberCandidate, publicIdCandidate), list.getCandidates());
  }

  @Test
  @DisplayName("mergeSavedListFromInputStream throws when candidate does not exist")
  void mergeSavedListFromInputStreamThrowsWhenCandidateMissing() {
    given(candidateRepository.findByCandidateNumber("999")).willReturn(null);

    assertThrows(
        NoSuchObjectException.class,
        () -> service.mergeSavedListFromInputStream(1L, inputStream("999\n"))
    );
  }

  @Test
  @DisplayName("mergeSavedListFromInputStream wraps CSV validation exception")
  void mergeSavedListFromInputStreamWrapsCsvValidationException() throws Exception {
    try (MockedConstruction<CSVReader> ignored =
        Mockito.mockConstruction(CSVReader.class, (reader, context) ->
            given(reader.readNext()).willThrow(new CsvValidationException("bad csv")))) {

      IOException exception = assertThrows(
          IOException.class,
          () -> service.mergeSavedListFromInputStream(1L, inputStream("bad"))
      );

      assertEquals("Bad file format: bad csv", exception.getMessage());
    }
  }

  @Test
  @DisplayName("search by candidate id uses logged in user and sorted repository query")
  @SuppressWarnings({"rawtypes", "unchecked"})
  void searchByCandidateIdDelegates() {
    SearchSavedListRequest request = new SearchSavedListRequest();
    List<SavedList> lists = List.of(savedList(1L, "List", user));

    given(userService.getLoggedInUser()).willReturn(user);
    given(savedListRepository.findAll(any(Specification.class), any(org.springframework.data.domain.Sort.class)))
        .willReturn(lists);

    assertSame(lists, service.search(99L, request));
  }

  @Test
  @DisplayName("search by ids delegates to repository")
  void searchByIdsDelegates() {
    IdsRequest request = new IdsRequest();
    request.setIds(Set.of(1L, 2L));
    List<SavedList> lists = List.of(savedList(1L, "List", user));

    given(savedListRepository.findByIds(Set.of(1L, 2L))).willReturn(lists);

    assertSame(lists, service.search(request));
  }

  @Test
  @DisplayName("search delegates to repository with standard name sort")
  @SuppressWarnings({"rawtypes", "unchecked"})
  void searchDelegates() {
    SearchSavedListRequest request = new SearchSavedListRequest();
    List<SavedList> lists = List.of(savedList(1L, "List", user));

    given(userService.getLoggedInUser()).willReturn(user);
    given(savedListRepository.findAll(any(Specification.class), any(org.springframework.data.domain.Sort.class)))
        .willReturn(lists);

    assertSame(lists, service.search(request));
  }

  @Test
  @DisplayName("searchPaged defaults sort when request has no sort direction")
  @SuppressWarnings({"rawtypes", "unchecked"})
  void searchPagedDefaultsSort() {
    SearchSavedListRequest request = new SearchSavedListRequest();
    request.setPageNumber(0);
    request.setPageSize(10);

    Page<SavedList> page = new PageImpl<>(List.of(savedList(1L, "List", user)));

    given(userService.getLoggedInUser()).willReturn(user);
    given(savedListRepository.findAll(any(Specification.class), any(Pageable.class)))
        .willReturn(page);

    assertSame(page, service.searchPaged(request));
    assertEquals(org.springframework.data.domain.Sort.Direction.ASC, request.getSortDirection());
    assertEquals("name", request.getSortFields()[0]);
  }


  @Test
  @DisplayName("setPublicIds assigns missing ids and saves non-empty list")
  void setPublicIdsAssignsMissingIds() {
    SavedList missing = savedList(1L, "Missing", user);
    SavedList existing = savedList(2L, "Existing", user);
    existing.setPublicId("existing-public-id");

    given(publicIDService.generatePublicID()).willReturn("new-public-id");

    service.setPublicIds(List.of(missing, existing));

    assertEquals("new-public-id", missing.getPublicId());
    assertEquals("existing-public-id", existing.getPublicId());
    verify(savedListRepository).saveAll(List.of(missing, existing));
  }

  @Test
  @DisplayName("setPublicIds does not save empty list")
  void setPublicIdsDoesNotSaveEmptyList() {
    service.setPublicIds(List.of());

    verify(savedListRepository, never()).saveAll(anyList());
  }

  @Test
  @DisplayName("updateSavedList updates fields and attaches job")
  void updateSavedListUpdatesFieldsAndJob() {
    SavedList list = savedList(1L, "Old", user);
    SalesforceJobOpp job = jobOpp(5L, "SF-5", "Job");
    UpdateSavedListInfoRequest request = savedListInfoRequest("New", false, 5L, null);

    given(userService.getLoggedInUser()).willReturn(user);
    given(savedListRepository.findByNameIgnoreCase("New", user.getId()))
        .willReturn(Optional.empty());
    given(savedListRepository.findById(1L)).willReturn(Optional.of(list));
    given(salesforceJobOppService.getJobOpp(5L)).willReturn(job);
    given(savedListRepository.save(list)).willReturn(list);

    SavedList result = service.updateSavedList(1L, request);

    assertSame(list, result);
    assertEquals("New", list.getName());
    assertSame(job, list.getSfJobOpp());
  }

  @Test
  @DisplayName("updateSavedList clears job when request job id is negative")
  void updateSavedListClearsJobWhenJobIdNegative() {
    SavedList list = savedList(1L, "Old", user);
    list.setSfJobOpp(jobOpp(5L, "SF-5", "Job"));

    UpdateSavedListInfoRequest request = savedListInfoRequest("Old", false, -1L, null);

    given(userService.getLoggedInUser()).willReturn(user);
    given(savedListRepository.findByNameIgnoreCase("Old", user.getId()))
        .willReturn(Optional.of(list));
    given(savedListRepository.findById(1L)).willReturn(Optional.of(list));
    given(savedListRepository.save(list)).willReturn(list);

    SavedList result = service.updateSavedList(1L, request);

    assertSame(list, result);
    assertNull(result.getSfJobOpp());
  }

  @Test
  @DisplayName("updateSavedList throws duplicate when another active list has requested name")
  void updateSavedListThrowsDuplicate() {
    SavedList list = savedList(1L, "Old", user);
    SavedList duplicate = savedList(2L, "New", user);
    UpdateSavedListInfoRequest request = savedListInfoRequest("New", false, null, null);

    given(userService.getLoggedInUser()).willReturn(user);
    given(savedListRepository.findByNameIgnoreCase("New", user.getId()))
        .willReturn(Optional.of(duplicate));

    assertThrows(EntityExistsException.class, () -> service.updateSavedList(1L, request));
  }

  @Test
  @DisplayName("updateDescription saves description")
  void updateDescriptionSavesDescription() {
    SavedList list = savedList(1L, "List", user);
    UpdateCandidateSourceDescriptionRequest request = new UpdateCandidateSourceDescriptionRequest();
    request.setDescription("description");

    given(savedListRepository.findById(1L)).willReturn(Optional.of(list));
    given(savedListRepository.save(list)).willReturn(list);

    service.updateDescription(1L, request);

    assertEquals("description", list.getDescription());
    verify(savedListRepository).save(list);
  }

  @Test
  @DisplayName("updateTcShortName saves unique short name")
  void updateTcShortNameSavesUniqueShortName() {
    SavedList list = savedList(1L, "List", user);
    UpdateShortNameRequest request = new UpdateShortNameRequest();
    request.setSavedListId(1L);
    request.setTcShortName("short");

    given(savedListRepository.findById(1L)).willReturn(Optional.of(list));
    given(savedListRepository.findByShortNameIgnoreCase("short")).willReturn(Optional.empty());
    given(savedListRepository.save(list)).willReturn(list);

    SavedList result = service.updateTcShortName(request);

    assertSame(list, result);
    assertEquals("short", list.getTcShortName());
  }

  @Test
  @DisplayName("updateTcShortName allows same short name for same list")
  void updateTcShortNameAllowsSameList() {
    SavedList list = savedList(1L, "List", user);
    UpdateShortNameRequest request = new UpdateShortNameRequest();
    request.setSavedListId(1L);
    request.setTcShortName("short");

    given(savedListRepository.findById(1L)).willReturn(Optional.of(list));
    given(savedListRepository.findByShortNameIgnoreCase("short")).willReturn(Optional.of(list));
    given(savedListRepository.save(list)).willReturn(list);

    assertSame(list, service.updateTcShortName(request));
  }

  @Test
  @DisplayName("updateTcShortName throws when short name belongs to another list")
  void updateTcShortNameThrowsDuplicate() {
    SavedList list = savedList(1L, "List", user);
    SavedList duplicate = savedList(2L, "Duplicate", user);

    UpdateShortNameRequest request = new UpdateShortNameRequest();
    request.setSavedListId(1L);
    request.setTcShortName("short");

    given(savedListRepository.findById(1L)).willReturn(Optional.of(list));
    given(savedListRepository.findByShortNameIgnoreCase("short")).willReturn(Optional.of(duplicate));

    assertThrows(EntityExistsException.class, () -> service.updateTcShortName(request));
  }

  @Test
  @DisplayName("findByShortName delegates to repository")
  void findByShortNameDelegates() {
    SavedList list = savedList(1L, "List", user);

    given(savedListRepository.findByShortNameIgnoreCase("short")).willReturn(Optional.of(list));

    assertSame(list, service.findByShortName("short"));
  }

  @Test
  @DisplayName("findListsAssociatedWithJobs delegates")
  void findListsAssociatedWithJobsDelegates() {
    List<SavedList> lists = List.of(savedList(1L, "List", user));

    given(savedListRepository.findListsWithJobs()).willReturn(lists);

    assertSame(lists, service.findListsAssociatedWithJobs());
  }

  @Test
  @DisplayName("updateDisplayedFieldPaths updates non-null fields only")
  void updateDisplayedFieldPathsUpdatesNonNullFieldsOnly() {
    SavedList list = savedList(1L, "List", user);
    list.setDisplayedFieldsShort(List.of("old-short"));

    UpdateDisplayedFieldPathsRequest request = new UpdateDisplayedFieldPathsRequest();
    request.setDisplayedFieldsLong(List.of("new-long"));

    given(savedListRepository.findById(1L)).willReturn(Optional.of(list));
    given(savedListRepository.save(list)).willReturn(list);

    service.updateDisplayedFieldPaths(1L, request);

    assertEquals(List.of("new-long"), list.getDisplayedFieldsLong());
    assertEquals(List.of("old-short"), list.getDisplayedFieldsShort());
  }

  @Test
  @DisplayName("updatePendingTermsAcceptance adds candidate when flag true")
  void updatePendingTermsAcceptanceAddsCandidate() {
    SavedList pendingTermsList = savedList(1L, "Pending Terms", user);
    Candidate candidate = candidate(2L, "1002");

    given(savedListRepository.findPendingTermsAcceptanceList())
        .willReturn(Optional.of(pendingTermsList));
    given(savedListRepository.save(pendingTermsList)).willReturn(pendingTermsList);

    service.updatePendingTermsAcceptance(candidate, true);

    assertEquals(Set.of(candidate), pendingTermsList.getCandidates());
    verify(savedListRepository).save(pendingTermsList);
  }

  @Test
  @DisplayName("updatePendingTermsAcceptance removes candidate when flag false")
  void updatePendingTermsAcceptanceRemovesCandidate() {
    SavedList pendingTermsList = savedList(1L, "Pending Terms", user);
    Candidate candidate = candidate(2L, "1002");
    addCandidateToSavedList(pendingTermsList, candidate);

    given(savedListRepository.findPendingTermsAcceptanceList())
        .willReturn(Optional.of(pendingTermsList));
    given(savedListRepository.save(pendingTermsList)).willReturn(pendingTermsList);

    service.updatePendingTermsAcceptance(candidate, false);

    verify(candidateSavedListRepository).delete(new CandidateSavedList(candidate, pendingTermsList));
    verify(savedListRepository).save(pendingTermsList);
  }

  @Test
  @DisplayName("updatePendingTermsAcceptance throws when pending terms list missing")
  void updatePendingTermsAcceptanceThrowsWhenMissing() {
    given(savedListRepository.findPendingTermsAcceptanceList()).willReturn(Optional.empty());

    assertThrows(
        NoSuchObjectException.class,
        () -> service.updatePendingTermsAcceptance(candidate(1L, "1001"), true)
    );
  }

  @Test
  @DisplayName("addSharedUser adds user and saves")
  void addSharedUserAddsUser() {
    SavedList list = savedList(1L, "List", user);
    User sharedUser = user(20L);

    UpdateSharingRequest request = new UpdateSharingRequest();
    request.setUserId(20L);

    given(savedListRepository.findById(1L)).willReturn(Optional.of(list));
    given(userRepository.findById(20L)).willReturn(Optional.of(sharedUser));
    given(savedListRepository.save(list)).willReturn(list);

    SavedList result = service.addSharedUser(1L, request);

    assertSame(list, result);
    assertTrue(list.getUsers().contains(sharedUser));
    assertTrue(sharedUser.getSharedLists().contains(list));
  }

  @Test
  @DisplayName("addSharedUser throws when list missing")
  void addSharedUserThrowsWhenListMissing() {
    UpdateSharingRequest request = new UpdateSharingRequest();
    request.setUserId(20L);

    given(savedListRepository.findById(1L)).willReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class, () -> service.addSharedUser(1L, request));
  }

  @Test
  @DisplayName("addSharedUser throws when user missing")
  void addSharedUserThrowsWhenUserMissing() {
    SavedList list = savedList(1L, "List", user);
    UpdateSharingRequest request = new UpdateSharingRequest();
    request.setUserId(20L);

    given(savedListRepository.findById(1L)).willReturn(Optional.of(list));
    given(userRepository.findById(20L)).willReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class, () -> service.addSharedUser(1L, request));
  }

  @Test
  @DisplayName("removeSharedUser removes user and saves")
  void removeSharedUserRemovesUser() {
    SavedList list = savedList(1L, "List", user);
    User sharedUser = user(20L);
    list.addUser(sharedUser);

    UpdateSharingRequest request = new UpdateSharingRequest();
    request.setUserId(20L);

    given(savedListRepository.findById(1L)).willReturn(Optional.of(list));
    given(userRepository.findById(20L)).willReturn(Optional.of(sharedUser));
    given(savedListRepository.save(list)).willReturn(list);

    SavedList result = service.removeSharedUser(1L, request);

    assertSame(list, result);
    assertFalse(list.getUsers().contains(sharedUser));
    assertFalse(sharedUser.getSharedLists().contains(list));
  }

  @Test
  @DisplayName("importEmployerFeedback returns message when list has no published doc link")
  void importEmployerFeedbackNoLink() throws GeneralSecurityException, IOException {
    SavedList list = savedList(1L, "List", user);

    given(savedListRepository.findById(1L)).willReturn(Optional.of(list));

    PublishedDocImportReport report = service.importEmployerFeedback(1L);

    assertEquals("No Salesforce job opportunity associated with list", report.getMessage());
  }

  @Test
  @DisplayName("importEmployerFeedback returns message when candidate column is missing")
  void importEmployerFeedbackNoCandidateColumn() throws GeneralSecurityException, IOException {
    SavedList list = savedList(1L, "List", user);
    list.setPublishedDocLink("doc-link");

    given(savedListRepository.findById(1L)).willReturn(Optional.of(list));
    given(docPublisherService.readPublishedDocColumns(eq("doc-link"), anyList()))
        .willReturn(Map.of());

    PublishedDocImportReport report = service.importEmployerFeedback(1L);

    assertEquals(0, report.getNumCandidates());
    assertEquals("No candidate column found - nothing to import", report.getMessage());
  }

  @Test
  @DisplayName("importEmployerFeedback imports notes and updates Salesforce")
  void importEmployerFeedbackImportsNotes() throws GeneralSecurityException, IOException {
    SavedList list = savedList(1L, "List", user);
    SalesforceJobOpp job = jobOpp(2L, "SF-2", "Job");
    list.setPublishedDocLink("doc-link");
    list.setSfJobOpp(job);

    Candidate candidate = candidate(3L, "1003");

    given(savedListRepository.findById(1L)).willReturn(Optional.of(list));
    given(docPublisherService.readPublishedDocColumns(eq("doc-link"), anyList()))
        .willReturn(Map.of(
            "CandidateNumber", List.of("1003"),
            PublishedDocColumnType.EmployerCandidateNotes.toString(), List.of("good candidate")
        ));
    given(candidateRepository.findByCandidateNumber("1003")).willReturn(candidate);

    PublishedDocImportReport report = service.importEmployerFeedback(1L);

    assertEquals(1, report.getNumCandidates());
    assertEquals(1, report.getNumEmployerFeedbacks());
    assertEquals("Import complete", report.getMessage());
    verify(salesforceService).updateCandidateOpportunities(anyList(), eq(job));
  }

  @Test
  @DisplayName("importEmployerFeedback detects no feedback")
  void importEmployerFeedbackNoFeedbackDetected() throws GeneralSecurityException, IOException {
    SavedList list = savedList(1L, "List", user);
    list.setPublishedDocLink("doc-link");

    Candidate candidate = candidate(3L, "1003");

    given(savedListRepository.findById(1L)).willReturn(Optional.of(list));
    given(docPublisherService.readPublishedDocColumns(eq("doc-link"), anyList()))
        .willReturn(Map.of("CandidateNumber", List.of("1003")));
    given(candidateRepository.findByCandidateNumber("1003")).willReturn(candidate);

    PublishedDocImportReport report = service.importEmployerFeedback(1L);

    assertEquals("No feedback detected", report.getMessage());
    verify(salesforceService, never()).updateCandidateOpportunities(anyList(), any());
  }

  @Test
  @DisplayName("importEmployerFeedback throws when candidate number cell is null")
  void importEmployerFeedbackThrowsOnNullCandidateNumber() throws GeneralSecurityException, IOException {
    SavedList list = savedList(1L, "List", user);
    list.setPublishedDocLink("doc-link");

    List<Object> candidateNumbers = new ArrayList<>();
    candidateNumbers.add(null);

    given(savedListRepository.findById(1L)).willReturn(Optional.of(list));
    given(docPublisherService.readPublishedDocColumns(eq("doc-link"), anyList()))
        .willReturn(Map.of("CandidateNumber", candidateNumbers));

    assertThrows(NoSuchObjectException.class, () -> service.importEmployerFeedback(1L));
  }

  @Test
  @DisplayName("importEmployerFeedback throws when candidate not found")
  void importEmployerFeedbackThrowsWhenCandidateMissing() throws GeneralSecurityException, IOException {
    SavedList list = savedList(1L, "List", user);
    list.setPublishedDocLink("doc-link");

    given(savedListRepository.findById(1L)).willReturn(Optional.of(list));
    given(docPublisherService.readPublishedDocColumns(eq("doc-link"), anyList()))
        .willReturn(Map.of("CandidateNumber", List.of("9999")));
    given(candidateRepository.findByCandidateNumber("9999")).willReturn(null);

    assertThrows(NoSuchObjectException.class, () -> service.importEmployerFeedback(1L));
  }

  @Test
  @DisplayName("createListFolder creates id folder, named folder, JD folder, and JOI file")
  void createListFolderCreatesFolders() throws IOException {
    SavedList list = savedList(1L, "List Name", user);

    GoogleFileSystemDrive drive = mock(GoogleFileSystemDrive.class);
    GoogleFileSystemFolder root = mock(GoogleFileSystemFolder.class);
    GoogleFileSystemFolder idFolder = mock(GoogleFileSystemFolder.class);
    GoogleFileSystemFolder namedFolder = mock(GoogleFileSystemFolder.class);
    GoogleFileSystemFolder jdFolder = mock(GoogleFileSystemFolder.class);
    GoogleFileSystemFile template = mock(GoogleFileSystemFile.class);
    GoogleFileSystemFile copiedFile = mock(GoogleFileSystemFile.class);

    given(savedListRepository.findById(1L)).willReturn(Optional.of(list));
    given(googleDriveConfig.getListFoldersDrive()).willReturn(drive);
    given(googleDriveConfig.getListFoldersRoot()).willReturn(root);
    given(googleDriveConfig.getJobOppIntakeTemplate()).willReturn(template);
    given(fileSystemService.findAFolder(drive, root, "1")).willReturn(null);
    given(fileSystemService.createFolder(drive, root, "1")).willReturn(idFolder);
    given(fileSystemService.createFolder(drive, idFolder, "List Name")).willReturn(namedFolder);
    given(namedFolder.getUrl()).willReturn("named-folder-url");
    given(fileSystemService.createFolder(drive, namedFolder, "JobDescription")).willReturn(jdFolder);
    given(jdFolder.getUrl()).willReturn("jd-folder-url");
    given(fileSystemService.copyFile(eq(jdFolder), contains("JobOpportunityIntake"), eq(template)))
        .willReturn(copiedFile);
    given(copiedFile.getName()).willReturn("JOI");
    given(copiedFile.getUrl()).willReturn("joi-url");
    given(savedListRepository.save(list)).willReturn(list);

    SavedList result = service.createListFolder(1L);

    assertSame(list, result);
    assertEquals("named-folder-url", list.getFolderlink());
    assertEquals("jd-folder-url", list.getFolderjdlink());
    assertEquals("JOI", list.getFileJoiName());
    assertEquals("joi-url", list.getFileJoiLink());
    verify(fileSystemService).publishFolder(namedFolder);
    verify(savedListRepository).save(list);
  }

  @Test
  @DisplayName("publish creates folder, creates published doc, saves export columns, and populates document")
  void publishCreatesDocAndSavesLink() throws GeneralSecurityException, IOException {
    SavedList list = savedList(1L, "Publish List", user);
    Candidate candidate = candidate(2L, "1002");
    addCandidateToSavedList(list, candidate);

    GoogleFileSystemDrive drive = mock(GoogleFileSystemDrive.class);
    GoogleFileSystemFolder root = mock(GoogleFileSystemFolder.class);
    GoogleFileSystemFolder idFolder = mock(GoogleFileSystemFolder.class);
    GoogleFileSystemFolder namedFolder = mock(GoogleFileSystemFolder.class);
    GoogleFileSystemFolder jdFolder = mock(GoogleFileSystemFolder.class);
    GoogleFileSystemFile template = mock(GoogleFileSystemFile.class);
    GoogleFileSystemFile copiedFile = mock(GoogleFileSystemFile.class);

    PublishListRequest request = mock(PublishListRequest.class);
    List<ExportColumn> exportColumns = List.of(new ExportColumn());

    given(savedListRepository.findById(1L)).willReturn(Optional.of(list));
    given(googleDriveConfig.getListFoldersDrive()).willReturn(drive);
    given(googleDriveConfig.getListFoldersRoot()).willReturn(root);
    given(googleDriveConfig.getJobOppIntakeTemplate()).willReturn(template);
    given(fileSystemService.findAFolder(drive, root, "1")).willReturn(null);
    given(fileSystemService.createFolder(drive, root, "1")).willReturn(idFolder);
    given(fileSystemService.createFolder(drive, idFolder, "Publish List")).willReturn(namedFolder);
    given(namedFolder.getUrl()).willReturn("folder-url");
    given(fileSystemService.createFolder(drive, namedFolder, "JobDescription")).willReturn(jdFolder);
    given(jdFolder.getUrl()).willReturn("jd-url");
    given(fileSystemService.copyFile(eq(jdFolder), anyString(), eq(template))).willReturn(copiedFile);
    given(copiedFile.getName()).willReturn("JOI");
    given(copiedFile.getUrl()).willReturn("joi-url");

    given(request.getConfiguredColumns()).willReturn(List.of());
    given(request.getExportColumns(list)).willReturn(exportColumns);
    given(googleDriveConfig.getPublishedSheetDataRangeName()).willReturn("candidateDataRange");
    given(docPublisherService.createPublishedDoc(
        any(GoogleFileSystemFolder.class),
        eq("Publish List"),
        eq("candidateDataRange"),
        eq(List.of(candidate)),
        eq(request),
        anyMap(),
        anyMap()
    )).willReturn("published-link");
    given(userService.getLoggedInUser()).willReturn(user);
    given(savedListRepository.save(list)).willReturn(list);

    SavedList result = service.publish(1L, request);

    assertSame(list, result);
    assertEquals("published-link", list.getPublishedDocLink());
    assertEquals(exportColumns, list.getExportColumns());
    verify(docPublisherService)
        .populatePublishedDoc("published-link", 1L, List.of(2L), request, "candidateDataRange");
    verify(exportColumnsService).clearExportColumns(list);
    verify(savedListRepository, times(2)).save(list);
  }

  @Test
  @DisplayName("fetchCandidates returns candidates for ids")
  void fetchCandidatesReturnsCandidates() {
    Candidate first = candidate(1L, "1001");
    Candidate second = candidate(2L, "1002");

    UpdateExplicitSavedListContentsRequest request = new UpdateExplicitSavedListContentsRequest();
    request.setCandidateIds(Set.of(1L, 2L));

    given(candidateRepository.findById(1L)).willReturn(Optional.of(first));
    given(candidateRepository.findById(2L)).willReturn(Optional.of(second));

    assertEquals(Set.of(first, second), service.fetchCandidates(request));
  }

  @Test
  @DisplayName("fetchCandidates throws when candidate missing")
  void fetchCandidatesThrowsWhenCandidateMissing() {
    UpdateExplicitSavedListContentsRequest request = new UpdateExplicitSavedListContentsRequest();
    request.setCandidateIds(Set.of(1L));

    given(candidateRepository.findById(1L)).willReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class, () -> service.fetchCandidates(request));
  }

  @Test
  @DisplayName("fetchCandidateIds delegates")
  void fetchCandidateIdsDelegates() {
    given(savedListRepository.findUnionOfCandidates(List.of(1L))).willReturn(Set.of(10L));

    assertEquals(Set.of(10L), service.fetchCandidateIds(1L));
  }

  @Test
  @DisplayName("fetchCandidatePublicIds delegates")
  void fetchCandidatePublicIdsDelegates() {
    given(savedListRepository.findCandidatePublicIdsBySavedListPublicIds(List.of("public-list")))
        .willReturn(Set.of("candidate-public"));

    assertEquals(Set.of("candidate-public"), service.fetchCandidatePublicIds("public-list"));
  }

  @Test
  @DisplayName("fetchUnionCandidateIds returns null for null list ids")
  void fetchUnionCandidateIdsReturnsNull() {
    assertNull(service.fetchUnionCandidateIds(null));
  }

  @Test
  @DisplayName("fetchUnionCandidateIds delegates for list ids")
  void fetchUnionCandidateIdsDelegates() {
    given(savedListRepository.findUnionOfCandidates(List.of(1L, 2L))).willReturn(Set.of(10L, 11L));

    assertEquals(Set.of(10L, 11L), service.fetchUnionCandidateIds(List.of(1L, 2L)));
  }

  @Test
  @DisplayName("fetchUnionCandidatePublicIds returns null for null public list ids")
  void fetchUnionCandidatePublicIdsReturnsNull() {
    assertNull(service.fetchUnionCandidatePublicIds(null));
  }

  @Test
  @DisplayName("fetchUnionCandidatePublicIds delegates for public list ids")
  void fetchUnionCandidatePublicIdsDelegates() {
    given(savedListRepository.findCandidatePublicIdsBySavedListPublicIds(List.of("a", "b")))
        .willReturn(Set.of("candidate"));

    assertEquals(Set.of("candidate"), service.fetchUnionCandidatePublicIds(List.of("a", "b")));
  }

  @Test
  @DisplayName("fetchIntersectionCandidateIds returns null for null list ids")
  void fetchIntersectionCandidateIdsReturnsNull() {
    assertNull(service.fetchIntersectionCandidateIds(null));
  }

  @Test
  @DisplayName("fetchIntersectionCandidateIds returns empty set for empty list ids")
  void fetchIntersectionCandidateIdsReturnsEmptySet() {
    assertEquals(Set.of(), service.fetchIntersectionCandidateIds(List.of()));
  }

  @Test
  @DisplayName("fetchIntersectionCandidateIds computes intersection")
  void fetchIntersectionCandidateIdsComputesIntersection() {
    given(savedListRepository.findUnionOfCandidates(List.of(1L))).willReturn(new HashSet<>(Set.of(10L, 11L)));
    given(savedListRepository.findUnionOfCandidates(List.of(2L))).willReturn(new HashSet<>(Set.of(11L, 12L)));

    assertEquals(Set.of(11L), service.fetchIntersectionCandidateIds(List.of(1L, 2L)));
  }

  @Test
  @DisplayName("fetchIntersectionCandidatePublicIds returns null for null public list ids")
  void fetchIntersectionCandidatePublicIdsReturnsNull() {
    assertNull(service.fetchIntersectionCandidatePublicIds(null));
  }

  @Test
  @DisplayName("fetchIntersectionCandidatePublicIds returns empty set for empty public list ids")
  void fetchIntersectionCandidatePublicIdsReturnsEmptySet() {
    assertEquals(Set.of(), service.fetchIntersectionCandidatePublicIds(List.of()));
  }

  @Test
  @DisplayName("fetchIntersectionCandidatePublicIds computes intersection")
  void fetchIntersectionCandidatePublicIdsComputesIntersection() {
    given(savedListRepository.findCandidatePublicIdsBySavedListPublicIds(List.of("a")))
        .willReturn(new HashSet<>(Set.of("x", "y")));
    given(savedListRepository.findCandidatePublicIdsBySavedListPublicIds(List.of("b")))
        .willReturn(new HashSet<>(Set.of("y", "z")));

    assertEquals(Set.of("y"), service.fetchIntersectionCandidatePublicIds(List.of("a", "b")));
  }

  @Test
  @DisplayName("fetchSourceList returns null when source id is null")
  void fetchSourceListReturnsNull() {
    UpdateSavedListContentsRequest request = mock(UpdateSavedListContentsRequest.class);
    given(request.getSourceListId()).willReturn(null);

    assertNull(service.fetchSourceList(request));
  }

  @Test
  @DisplayName("fetchSourceList loads source list")
  void fetchSourceListLoadsList() {
    SavedList source = savedList(1L, "Source", user);
    UpdateSavedListContentsRequest request = mock(UpdateSavedListContentsRequest.class);

    given(request.getSourceListId()).willReturn(1L);
    given(savedListRepository.findByIdLoadCandidates(1L)).willReturn(Optional.of(source));

    assertSame(source, service.fetchSourceList(request));
  }

  @Test
  @DisplayName("fetchSourceList throws when source list missing")
  void fetchSourceListThrowsWhenMissing() {
    UpdateSavedListContentsRequest request = mock(UpdateSavedListContentsRequest.class);

    given(request.getSourceListId()).willReturn(1L);
    given(savedListRepository.findByIdLoadCandidates(1L)).willReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class, () -> service.fetchSourceList(request));
  }

  @Test
  @DisplayName("saveIt updates audit fields and saves")
  void saveItSaves() {
    SavedList list = savedList(1L, "List", user);

    given(userService.getLoggedInUser()).willReturn(user);
    given(savedListRepository.save(list)).willReturn(list);

    assertSame(list, service.saveIt(list));
    verify(savedListRepository).save(list);
  }

  @Test
  @DisplayName("updateAssociatedListsNames renames submission and exclusion lists")
  void updateAssociatedListsNamesRenamesLists() {
    SalesforceJobOpp job = jobOpp(1L, "SF-1", "New Job");
    SavedList submissionList = savedList(2L, "Old*", user);
    SavedList exclusionList = savedList(3L, "Old*Exclude", user);

    job.setSubmissionList(submissionList);
    job.setExclusionList(exclusionList);

    given(savedListRepository.save(any(SavedList.class))).willAnswer(invocation -> invocation.getArgument(0));

    service.updateAssociatedListsNames(job);

    assertEquals("New Job*", submissionList.getName());
    assertEquals("New Job*Exclude", exclusionList.getName());
    verify(savedListRepository, times(2)).save(any(SavedList.class));
  }

  private UpdateSavedListInfoRequest savedListInfoRequest(
      String initialName,
      Boolean registeredJob,
      Long jobId,
      SalesforceJobOpp sfJobOpp
  ) {
    UpdateSavedListInfoRequest request = mock(UpdateSavedListInfoRequest.class);

    final String[] name = new String[] {initialName};

    lenient().when(request.getName()).thenAnswer(invocation -> name[0]);
    lenient().when(request.getRegisteredJob()).thenReturn(registeredJob);
    lenient().when(request.getJobId()).thenReturn(jobId);
    lenient().when(request.getSfJobOpp()).thenReturn(sfJobOpp);

    lenient().doAnswer(invocation -> {
      name[0] = invocation.getArgument(0);
      return null;
    }).when(request).setName(anyString());

    lenient().doAnswer(invocation -> {
      SavedList savedList = invocation.getArgument(0);
      savedList.setName(name[0]);
      savedList.setRegisteredJob(registeredJob);
      return null;
    }).when(request).populateFromRequest(any(SavedList.class));

    return request;
  }

  private InputStream inputStream(String value) {
    return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
  }

  private User user(Long id) {
    User user = new User();
    user.setId(id);
    user.setFirstName("First");
    user.setLastName("Last");
    user.setEmail("user@example.org");
    user.setSharedLists(new HashSet<>());
    return user;
  }

  private SavedList savedList(Long id, String name, User createdBy) {
    SavedList savedList = new SavedList();
    savedList.setId(id);
    savedList.setName(name);
    savedList.setCreatedBy(createdBy);
    savedList.setStatus(Status.active);
    savedList.setRegisteredJob(false);
    savedList.setTasks(new HashSet<>());
    savedList.setCandidateSavedLists(new HashSet<>());
    savedList.setUsers(new HashSet<>());
    return savedList;
  }

  private Candidate candidate(Long id, String candidateNumber) {
    Candidate candidate = new Candidate();
    candidate.setId(id);
    candidate.setCandidateNumber(candidateNumber);
    candidate.setCreatedDate(OffsetDateTime.now());
    candidate.setCandidateSavedLists(new HashSet<>());
    candidate.setTaskAssignments(new ArrayList<>());
//    candidate.setCandidateOpportunities(new L<>());
    return candidate;
  }

  private TaskImpl task(Long id) {
    TaskImpl task = new TaskImpl();
    task.setId(id);
    task.setName("task-" + id);
    return task;
  }

  private TaskAssignmentImpl activeAssignment(Long id, TaskImpl task, SavedList relatedList) {
    TaskAssignmentImpl assignment = new TaskAssignmentImpl();
    assignment.setId(id);
    assignment.setTask(task);
    assignment.setStatus(Status.active);
    assignment.setRelatedList(relatedList);
    assignment.setCompletedDate(null);
    assignment.setAbandonedDate(null);
    return assignment;
  }

  private SalesforceJobOpp jobOpp(Long id, String sfId, String name) {
    SalesforceJobOpp jobOpp = new SalesforceJobOpp();
    jobOpp.setId(id);
    jobOpp.setSfId(sfId);
    jobOpp.setName(name);
    return jobOpp;
  }

  private void addCandidateToSavedList(SavedList savedList, Candidate candidate) {
    CandidateSavedList csl = new CandidateSavedList(candidate, savedList);
    savedList.getCandidateSavedLists().add(csl);
    candidate.getCandidateSavedLists().add(csl);
  }
}