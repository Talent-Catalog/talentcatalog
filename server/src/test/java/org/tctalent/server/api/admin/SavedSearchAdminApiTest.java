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

package org.tctalent.server.api.admin;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.tctalent.server.data.SavedListTestData.getSavedList;
import static org.tctalent.server.data.SavedSearchTestData.getListOfSavedSearches;
import static org.tctalent.server.data.SavedSearchTestData.getSavedSearch;
import static org.tctalent.server.model.db.SavedSearchType.other;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.tctalent.server.model.db.CandidateStatus;
import org.tctalent.server.model.db.Gender;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.SavedSearch;
import org.tctalent.server.request.candidate.SearchCandidateRequest;
import org.tctalent.server.request.candidate.UpdateCandidateContextNoteRequest;
import org.tctalent.server.request.candidate.UpdateCandidateStatusInfo;
import org.tctalent.server.request.candidate.UpdateCandidateStatusRequest;
import org.tctalent.server.request.candidate.UpdateDisplayedFieldPathsRequest;
import org.tctalent.server.request.candidate.source.CopySourceContentsRequest;
import org.tctalent.server.request.candidate.source.UpdateCandidateSourceDescriptionRequest;
import org.tctalent.server.request.list.UpdateExplicitSavedListContentsRequest;
import org.tctalent.server.request.search.ClearSelectionRequest;
import org.tctalent.server.request.search.CreateFromDefaultSavedSearchRequest;
import org.tctalent.server.request.search.SearchSavedSearchRequest;
import org.tctalent.server.request.search.SelectCandidateInSearchRequest;
import org.tctalent.server.request.search.UpdateSavedSearchRequest;
import org.tctalent.server.request.search.UpdateSharingRequest;
import org.tctalent.server.request.search.UpdateWatchingRequest;
import org.tctalent.server.service.db.CandidateSavedListService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.SavedListService;
import org.tctalent.server.service.db.SavedSearchService;
import org.tctalent.server.service.db.UserService;

/**
 * Unit tests for Saved Search Admin Api endpoints
 *
 * @author samschlicht
 */

@WebMvcTest(SavedSearchAdminApi.class)
@AutoConfigureMockMvc
class SavedSearchAdminApiTest extends ApiTestBase {

  private static final long SAVED_SEARCH_ID = 123L;
  private static final long USER_ID = 11L;
  private static final long CANDIDATE_ID = 11L;
  private static final String BASE_PATH = "/api/admin/saved-search";
  private static final String SEARCH_PATH = "/search";
  private static final String SEARCH_PAGED_PATH = "/search-paged";
  private static final SavedSearch savedSearch = getSavedSearch();
  private static final SavedList savedList = getSavedList();
  private static final List<SavedSearch> savedSearchList = getListOfSavedSearches();
  private static final String CLEAR_SELECTION_PATH = "/clear-selection/";
  private static final String CREATE_FROM_DEFAULT_PATH = "/create-from-default";
  private static final String SAVE_SELECTION_PATH = "/save-selection/";
  private static final String UPDATE_SELECTED_STATUSES_PATH = "/update-selected-statuses/";
  private static final String SELECT_CANDIDATE_PATH = "/select-candidate/";
  private static final String GET_SELECTION_COUNT_PATH = "/get-selection-count/";
  private static final String GET_DEFAULT_PATH = "/default";
  private static final String LOAD_PATH = "/load";
  private static final String ADD_SHARED_USER_PATH = "/shared-add/";
  private static final String REMOVE_SHARED_USER_PATH = "/shared-remove/";
  private static final String ADD_WATCHER_PATH = "/watcher-add/";
  private static final String REMOVE_WATCHER_PATH = "/watcher-remove/";
  private static final String UPDATE_CONTEXT_PATH = "/context/";
  private static final String UPDATE_FIELDS_PATH = "/displayed-fields/";
  private static final String UPDATE_DESCRIPTION_PATH = "/description/";

  private final Page<SavedSearch> savedSearchPage =
      new PageImpl<>(
          savedSearchList,
          PageRequest.of(0, 10, Sort.unsorted()),
          1
      );

  @MockBean
  CandidateService candidateService;
  @MockBean
  CandidateSavedListService candidateSavedListService;
  @MockBean
  SavedListService savedListService;
  @MockBean
  SavedSearchService savedSearchService;
  @MockBean
  UserService userService;
  @MockBean
  SavedListBuilderSelector savedListBuilderSelector;
  @MockBean
  ExportColumnsBuilderSelector exportColumnsBuilderSelector;

  @Autowired
  MockMvc mockMvc;
  @Autowired
  ObjectMapper objectMapper;
  @Autowired
  SavedSearchAdminApi savedSearchAdminApi;

  @BeforeEach
  void setUp() {
    configureAuthentication();
  }

  @Test
  public void testWebOnlyContextLoads() {
    assertThat(savedSearchAdminApi).isNotNull();
  }

  @Test
  @DisplayName("create saved search succeeds")
  void createSavedSearchSucceeds() throws Exception {
    UpdateSavedSearchRequest request = new UpdateSavedSearchRequest();
    request.setSavedSearchType(other);
    request.setSearchCandidateRequest(new SearchCandidateRequest());
    request.setFixed(false);
    request.setGlobal(false);

    given(savedSearchService
        .createSavedSearch(any(UpdateSavedSearchRequest.class)))
        .willReturn(savedSearch);

    createSavedSearchAndVerifyResponse(BASE_PATH, objectMapper.writeValueAsString(request));

    verify(savedSearchService).createSavedSearch(any(UpdateSavedSearchRequest.class));
  }

  @Test
  @DisplayName("delete by id returns true")
  void deleteByIdReturnsTrue() throws Exception {
    given(savedSearchService
        .deleteSavedSearch(anyLong()))
        .willReturn(true);

    mockMvc.perform(delete(BASE_PATH + "/" + SAVED_SEARCH_ID)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token"))

        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", is(true)));

    verify(savedSearchService).deleteSavedSearch(anyLong());
  }

  @Test
  @DisplayName("get by id succeeds")
  void getByIdSucceeds() throws Exception {
    String path = "/" + SAVED_SEARCH_ID;

    given(savedSearchService
        .getSavedSearch(anyLong()))
        .willReturn(savedSearch);

    readSavedSearchAndVerifyResponse(path);

    verify(savedSearchService).getSavedSearch((anyLong()));
  }

  @Test
  @DisplayName("search saved searches returns list succeeds")
  void searchSavedSearchesReturnsListSucceeds() throws Exception {
    SearchSavedSearchRequest request = new SearchSavedSearchRequest();

    given(savedSearchService.
        search(any(SearchSavedSearchRequest.class)))
        .willReturn(savedSearchList);

    mockMvc.perform(post(BASE_PATH + SEARCH_PATH)
        .with(csrf())
        .header("Authorization", "Bearer " + "jwt-token")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request))
        .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.[0].name", is("My Search")))
        .andExpect(jsonPath("$.[0].description", is("This is a search about nothing.")))
        .andExpect(jsonPath("$.[0].fixed", is(false)))
        .andExpect(jsonPath("$.[0].global", is(false)))
        .andExpect(jsonPath("$.[0].savedSearchType", is("other")))
        .andExpect(jsonPath("$.[0].simpleQueryString", is("search + term")))
        .andExpect(jsonPath("$.[0].statuses", is("active,pending")))
        .andExpect(jsonPath("$.[0].gender", is("male")))
        .andExpect(jsonPath("$.[0].occupationIds", is("8577,8484")))
        .andExpect(jsonPath("$.[1].name", is("Saved Search 2")))
        .andExpect(jsonPath("$.[1].description", is("This is a search about nothing.")))
        .andExpect(jsonPath("$.[1].fixed", is(false)))
        .andExpect(jsonPath("$.[1].global", is(false)))
        .andExpect(jsonPath("$.[1].savedSearchType", is("other")))
        .andExpect(jsonPath("$.[1].simpleQueryString", is("search + term")))
        .andExpect(jsonPath("$.[1].statuses", is("active,pending")))
        .andExpect(jsonPath("$.[1].gender", is("male")))
        .andExpect(jsonPath("$.[1].occupationIds", is("8577,8484")))
        .andExpect(jsonPath("$.[2].name", is("Saved Search 3")))
        .andExpect(jsonPath("$.[2].description", is("This is a search about nothing.")))
        .andExpect(jsonPath("$.[2].fixed", is(false)))
        .andExpect(jsonPath("$.[2].global", is(false)))
        .andExpect(jsonPath("$.[2].savedSearchType", is("other")))
        .andExpect(jsonPath("$.[2].simpleQueryString", is("search + term")))
        .andExpect(jsonPath("$.[2].statuses", is("active,pending")))
        .andExpect(jsonPath("$.[2].gender", is("male")))
        .andExpect(jsonPath("$.[2].occupationIds", is("8577,8484"))
        );

    verify(savedSearchService).search(any(SearchSavedSearchRequest.class));
  }

  @Test
  @DisplayName("search paged saved searches succeeds")
  void searchPagedSavedSearchesSucceeds() throws Exception {
    SearchSavedSearchRequest request = new SearchSavedSearchRequest();

    given(savedSearchService.
        searchPaged(any(SearchSavedSearchRequest.class)))
        .willReturn(savedSearchPage);

    mockMvc.perform(post(BASE_PATH + SEARCH_PAGED_PATH)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.numberOfElements", is(3)))
        .andExpect(jsonPath("$.totalPages", is(1)))
        .andExpect(jsonPath("$.hasPrevious", is(false)))
        .andExpect(jsonPath("$.hasNext", is(false)))
        .andExpect(jsonPath("$.content", notNullValue()))
        .andExpect(jsonPath("$.content[0].name", is("My Search")))
        .andExpect(jsonPath("$.content[0].description", is("This is a search about nothing.")))
        .andExpect(jsonPath("$.content[0].fixed", is(false)))
        .andExpect(jsonPath("$.content[0].global", is(false)))
        .andExpect(jsonPath("$.content[0].savedSearchType", is("other")))
        .andExpect(jsonPath("$.content[0].simpleQueryString", is("search + term")))
        .andExpect(jsonPath("$.content[0].statuses", is("active,pending")))
        .andExpect(jsonPath("$.content[0].gender", is("male")))
        .andExpect(jsonPath("$.content[0].occupationIds", is("8577,8484")))
        .andExpect(jsonPath("$.content[1].name", is("Saved Search 2")))
        .andExpect(jsonPath("$.content[1].description", is("This is a search about nothing.")))
        .andExpect(jsonPath("$.content[1].fixed", is(false)))
        .andExpect(jsonPath("$.content[1].global", is(false)))
        .andExpect(jsonPath("$.content[1].savedSearchType", is("other")))
        .andExpect(jsonPath("$.content[1].simpleQueryString", is("search + term")))
        .andExpect(jsonPath("$.content[1].statuses", is("active,pending")))
        .andExpect(jsonPath("$.content[1].gender", is("male")))
        .andExpect(jsonPath("$.content[1].occupationIds", is("8577,8484")))
        .andExpect(jsonPath("$.content[2].name", is("Saved Search 3")))
        .andExpect(jsonPath("$.content[2].description", is("This is a search about nothing.")))
        .andExpect(jsonPath("$.content[2].fixed", is(false)))
        .andExpect(jsonPath("$.content[2].global", is(false)))
        .andExpect(jsonPath("$.content[2].savedSearchType", is("other")))
        .andExpect(jsonPath("$.content[2].simpleQueryString", is("search + term")))
        .andExpect(jsonPath("$.content[2].statuses", is("active,pending")))
        .andExpect(jsonPath("$.content[2].gender", is("male")))
        .andExpect(jsonPath("$.content[2].occupationIds", is("8577,8484"))
        );

    verify(savedSearchService).searchPaged(any(SearchSavedSearchRequest.class));
  }

  @Test
  @DisplayName("update saved search succeeds")
  void updateSavedSearchSucceeds() throws Exception {
    String path = "/" + SAVED_SEARCH_ID;

    UpdateSavedSearchRequest request = new UpdateSavedSearchRequest();

    given(savedSearchService
          .updateSavedSearch(anyLong(), any(UpdateSavedSearchRequest.class)))
          .willReturn(savedSearch);

    updateSavedSearchAndVerifyResponse(path, objectMapper.writeValueAsString(request));

    verify(savedSearchService).updateSavedSearch(anyLong(), any(UpdateSavedSearchRequest.class));
  }

  @Test
  @DisplayName("clears selection succeeds")
  void clearsSelectionSucceeds() throws Exception {
    ClearSelectionRequest request = new ClearSelectionRequest();
    request.setUserId(USER_ID);

    updateSavedSearch(CLEAR_SELECTION_PATH, objectMapper.writeValueAsString(request));

    verify(savedSearchService).clearSelection(anyLong(), anyLong());
  }

  @Test
  @DisplayName("create from default saved search succeeds")
  void createFromDefaultSavedSearchSucceeds() throws Exception {
    String path = BASE_PATH + CREATE_FROM_DEFAULT_PATH;

    CreateFromDefaultSavedSearchRequest request = new CreateFromDefaultSavedSearchRequest();

    given(savedSearchService
        .createFromDefaultSavedSearch(any(CreateFromDefaultSavedSearchRequest.class)))
        .willReturn(savedSearch);

    createSavedSearchAndVerifyResponse(path, objectMapper.writeValueAsString(request));

    verify(savedSearchService).createFromDefaultSavedSearch(any(CreateFromDefaultSavedSearchRequest.class));
  }

  @Test
  @DisplayName("save selection succeeds")
  void saveSelectionSucceeds() throws Exception {
    CopySourceContentsRequest request = new CopySourceContentsRequest();
    UpdateCandidateStatusInfo update = new UpdateCandidateStatusInfo();
    update.setStatus(CandidateStatus.active);
    request.setStatusUpdateInfo(update);

    given(savedSearchService
        .getSelectionListForLoggedInUser(anyLong()))
        .willReturn(savedList);

    given(candidateSavedListService
        .copy(any(SavedList.class), any(CopySourceContentsRequest.class)))
        .willReturn(savedList);

    mockMvc.perform(put(BASE_PATH + SAVE_SELECTION_PATH + SAVED_SEARCH_ID)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.description", is("Saved list description")))
        .andExpect(jsonPath("$.displayedFieldsLong[0]", is("user.firstName")))
        .andExpect(jsonPath("$.displayedFieldsLong[1]", is("user.lastName")))
        .andExpect(jsonPath("$.exportColumns").isArray())
        .andExpect(jsonPath("$.exportColumns[0].key", is("key")))
        .andExpect(jsonPath("$.exportColumns[0].properties.constant", is("non default constant column value")))
        .andExpect(jsonPath("$.exportColumns[0].properties.header", is("non default column header")))
        .andExpect(jsonPath("$.status", is("active")))
        .andExpect(jsonPath("$.name", is("Saved list name")))
        .andExpect(jsonPath("$.fixed", is(true)))
        .andExpect(jsonPath("$.global", is(false)))
        .andExpect(jsonPath("$.savedSearchSource.id", is(123)))
        .andExpect(jsonPath("$.sfJobOpp.id", is(135)))
        .andExpect(jsonPath("$.sfJobOpp.sfId", is("sales-force-job-opp-id")))
        .andExpect(jsonPath("$.fileJdLink", is("http://file.jd.link")))
        .andExpect(jsonPath("$.fileJdName", is("JobDescriptionFileName")))
        .andExpect(jsonPath("$.fileJoiLink", is("http://file.joi.link")))
        .andExpect(jsonPath("$.fileJoiName", is("JoiFileName")))
        .andExpect(jsonPath("$.folderlink", is("http://folder.link")))
        .andExpect(jsonPath("$.folderjdlink", is("http://folder.jd.link")))
        .andExpect(jsonPath("$.publishedDocLink", is("http://published.doc.link")))
        .andExpect(jsonPath("$.registeredJob", is(true)))
        .andExpect(jsonPath("$.tcShortName", is("Saved list Tc short name")))
        .andExpect(jsonPath("$.createdBy.firstName", is("test")))
        .andExpect(jsonPath("$.createdBy.lastName", is("user")))
        .andExpect(jsonPath("$.createdDate", is("2023-10-30T12:30:00+02:00")))
        .andExpect(jsonPath("$.updatedBy.firstName", is("test")))
        .andExpect(jsonPath("$.updatedBy.lastName", is("user")))
        .andExpect(jsonPath("$.updatedDate", is("2023-10-30T12:30:00+02:00")))
        .andExpect(jsonPath("$.users[0].firstName", is("test")))
        .andExpect(jsonPath("$.users[0].lastName", is("user")))
        .andExpect(jsonPath("$.tasks[0].id", is(148)))
        .andExpect(jsonPath("$.tasks[0].docLink", is("http://help.link")))
        .andExpect(jsonPath("$.tasks[0].taskType", is("Simple")))
        .andExpect(jsonPath("$.tasks[0].displayName", is("task display name")))
        .andExpect(jsonPath("$.tasks[0].name", is("a test task")))
        .andExpect(jsonPath("$.tasks[0].description", is("a test task description")))
        .andExpect(jsonPath("$.tasks[0].optional", is(false)))
        .andExpect(jsonPath("$.tasks[0].daysToComplete", is(7))
        );

    verify(savedSearchService).getSelectionListForLoggedInUser(anyLong());
    verify(candidateSavedListService).copy(any(SavedList.class), any(CopySourceContentsRequest.class));
    verify(candidateService).updateCandidateStatus(any(SavedList.class), any(UpdateCandidateStatusInfo.class));
  }

  @Test
  @DisplayName("update selected statuses succeeds")
  void updateSelectedStatusesSucceeds() throws Exception {
    UpdateCandidateStatusInfo request = new UpdateCandidateStatusInfo();
    request.setStatus(CandidateStatus.employed);

    given(savedSearchService
        .getSelectionListForLoggedInUser(anyLong()))
        .willReturn(savedList);

    updateSavedSearch(UPDATE_SELECTED_STATUSES_PATH, objectMapper.writeValueAsString(request));

    verify(savedSearchService).getSelectionListForLoggedInUser(anyLong());
    verify(candidateService).updateCandidateStatus(any(UpdateCandidateStatusRequest.class));
  }

  @Test
  @DisplayName("get selection count succeeds")
  public void getSelectionCountSucceeds() throws Exception {
    given(savedSearchService
        .getSelectionListForLoggedInUser(anyLong()))
        .willReturn(savedList);

    mockMvc.perform(get(BASE_PATH + GET_SELECTION_COUNT_PATH + SAVED_SEARCH_ID)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", is(1))
        );

    verify(savedSearchService).getSelectionListForLoggedInUser(anyLong());
  }

  @Test
  @DisplayName("select and remove candidate succeeds")
  void selectAndRemoveCandidateSucceeds() throws Exception {
    SelectCandidateInSearchRequest request = new SelectCandidateInSearchRequest();
    request.setUserId(USER_ID);
    request.setCandidateId(CANDIDATE_ID);

    given(savedSearchService
        .getSelectionList(anyLong(), anyLong()))
        .willReturn(savedList);

    updateSavedSearch(SELECT_CANDIDATE_PATH, objectMapper.writeValueAsString(request));

    verify(savedSearchService).getSelectionList(anyLong(), anyLong());
    verify(savedListService).removeCandidateFromList(anyLong(), any(UpdateExplicitSavedListContentsRequest.class));
  }

  @Test
  @DisplayName("select and add candidate by id succeeds")
  void selectAndAddCandidateByIdSucceeds() throws Exception {
    SelectCandidateInSearchRequest request = new SelectCandidateInSearchRequest();
    request.setUserId(USER_ID);
    request.setCandidateId(CANDIDATE_ID);
    request.setSelected(true);

    given(savedSearchService
        .getSelectionList(anyLong(), anyLong()))
        .willReturn(savedList);

    updateSavedSearch(SELECT_CANDIDATE_PATH, objectMapper.writeValueAsString(request));

    verify(savedSearchService).getSelectionList(anyLong(), anyLong());
    verify(savedListService).mergeSavedList(anyLong(), any(UpdateExplicitSavedListContentsRequest.class));
  }

  @Test
  @DisplayName("get default search succeeds")
  void getDefaultSearchSucceeds() throws Exception {
    given(savedSearchService
        .getDefaultSavedSearch())
        .willReturn(savedSearch);

    readSavedSearchAndVerifyResponse(GET_DEFAULT_PATH);

    verify(savedSearchService).getDefaultSavedSearch();
  }

  @Test
  @DisplayName("load by id succeeds")
  void loadByIdSucceeds() throws Exception {
    SearchCandidateRequest request = new SearchCandidateRequest();
    request.setGender(Gender.male);
    request.setSimpleQueryString("welder + mechanic");
    request.setEnglishMinWrittenLevel(1);

    given(savedSearchService
        .loadSavedSearch(anyLong()))
        .willReturn(request);

    mockMvc.perform(get(BASE_PATH + "/" + SAVED_SEARCH_ID + LOAD_PATH)
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$.gender", is("male")))
        .andExpect(jsonPath("$.simpleQueryString", is("welder + mechanic")))
        .andExpect(jsonPath("$.englishMinWrittenLevel", is(1))
        );

    verify(savedSearchService).loadSavedSearch(anyLong());
  }

  @Test
  @DisplayName("add shared user by id succeeds")
  void addSharedUserByIdSucceeds() throws Exception {
    final String path = ADD_SHARED_USER_PATH + SAVED_SEARCH_ID;

    UpdateSharingRequest request = new UpdateSharingRequest();
    request.setUserId(USER_ID);

    given(savedSearchService
        .addSharedUser(anyLong(), any(UpdateSharingRequest.class)))
        .willReturn(savedSearch);

    updateSavedSearchAndVerifyResponse(path, objectMapper.writeValueAsString(request));

    verify(savedSearchService).addSharedUser(anyLong(), any(UpdateSharingRequest.class));
  }

  @Test
  @DisplayName("remove shared user succeeds")
  void removeSharedUserSucceeds() throws Exception {
    String path = REMOVE_SHARED_USER_PATH + SAVED_SEARCH_ID;

    UpdateSharingRequest request = new UpdateSharingRequest();
    request.setUserId(USER_ID);

    given(savedSearchService
        .removeSharedUser(anyLong(), any(UpdateSharingRequest.class)))
        .willReturn(savedSearch);

    updateSavedSearchAndVerifyResponse(path, objectMapper.writeValueAsString(request));

    verify(savedSearchService).removeSharedUser(anyLong(), any(UpdateSharingRequest.class));
  }

  @Test
  @DisplayName("add watcher succeeds")
  void addWatcherSucceeds() throws Exception {
    String path = ADD_WATCHER_PATH + SAVED_SEARCH_ID;

    UpdateWatchingRequest request = new UpdateWatchingRequest();
    request.setUserId(USER_ID);

    given(savedSearchService
        .addWatcher(anyLong(), any(UpdateWatchingRequest.class)))
        .willReturn(savedSearch);

    updateSavedSearchAndVerifyResponse(path, objectMapper.writeValueAsString(request));

    verify(savedSearchService).addWatcher(anyLong(), any(UpdateWatchingRequest.class));
  }

  @Test
  @DisplayName("remove watcher succeeds")
  void removeWatcherSucceeds() throws Exception {
    String path = REMOVE_WATCHER_PATH + SAVED_SEARCH_ID;

    UpdateWatchingRequest request = new UpdateWatchingRequest();
    request.setUserId(USER_ID);

    given(savedSearchService
        .removeWatcher(anyLong(), any(UpdateWatchingRequest.class)))
        .willReturn(savedSearch);

    updateSavedSearchAndVerifyResponse(path, objectMapper.writeValueAsString(request));

    verify(savedSearchService).removeWatcher(anyLong(), any(UpdateWatchingRequest.class));
  }

  @Test
  @DisplayName("update context note succeeds")
  void addContextNoteSucceeds() throws Exception {
    UpdateCandidateContextNoteRequest request = new UpdateCandidateContextNoteRequest();
    request.setCandidateId(CANDIDATE_ID);
    request.setContextNote("Some context.");

    updateSavedSearch(UPDATE_CONTEXT_PATH, objectMapper.writeValueAsString(request));

    verify(savedSearchService).updateCandidateContextNote(anyLong(), any(UpdateCandidateContextNoteRequest.class));
  }

  @Test
  @DisplayName("update description succeeds")
  void addDescriptionSucceeds() throws Exception {
    UpdateCandidateSourceDescriptionRequest request = new UpdateCandidateSourceDescriptionRequest();
    request.setDescription("A very descriptive description.");

    updateSavedSearch(UPDATE_DESCRIPTION_PATH, objectMapper.writeValueAsString(request));

    verify(savedSearchService).updateDescription(anyLong(), any(UpdateCandidateSourceDescriptionRequest.class));
  }

  @Test
  @DisplayName("update displayed fields succeeds")
  void addDisplayedFieldSucceeds() throws Exception {
    UpdateDisplayedFieldPathsRequest request = new UpdateDisplayedFieldPathsRequest();

    updateSavedSearch(UPDATE_FIELDS_PATH, objectMapper.writeValueAsString(request));

    verify(savedSearchService).updateDisplayedFieldPaths(anyLong(), any(UpdateDisplayedFieldPathsRequest.class));
  }

  private void updateSavedSearchAndVerifyResponse(String path, String body) throws Exception {
    mockMvc.perform(put(BASE_PATH + path)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body)
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$.id", is(123)))
        .andExpect(jsonPath("$.name", is("My Search")))
        .andExpect(jsonPath("$.description", is("This is a search about nothing.")))
        .andExpect(jsonPath("$.fixed", is(false)))
        .andExpect(jsonPath("$.global", is(false)))
        .andExpect(jsonPath("$.savedSearchType", is("other")))
        .andExpect(jsonPath("$.simpleQueryString", is("search + term")))
        .andExpect(jsonPath("$.statuses", is("active,pending")))
        .andExpect(jsonPath("$.gender", is("male")))
        .andExpect(jsonPath("$.occupationIds", is("8577,8484"))
        );
  }

  private void updateSavedSearch(String path, String body) throws Exception {
    mockMvc.perform(put(BASE_PATH + path + SAVED_SEARCH_ID)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body)
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk());
  }

  private void readSavedSearchAndVerifyResponse(String path) throws Exception {
    mockMvc.perform(get(BASE_PATH + path)
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$.id", is(123)))
        .andExpect(jsonPath("$.name", is("My Search")))
        .andExpect(jsonPath("$.description", is("This is a search about nothing.")))
        .andExpect(jsonPath("$.fixed", is(false)))
        .andExpect(jsonPath("$.global", is(false)))
        .andExpect(jsonPath("$.savedSearchType", is("other")))
        .andExpect(jsonPath("$.simpleQueryString", is("search + term")))
        .andExpect(jsonPath("$.statuses", is("active,pending")))
        .andExpect(jsonPath("$.gender", is("male")))
        .andExpect(jsonPath("$.occupationIds", is("8577,8484")));
  }

  private void createSavedSearchAndVerifyResponse(String path, String body) throws Exception {
    mockMvc.perform(post(path)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body)
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$.id", is(123)))
        .andExpect(jsonPath("$.name", is("My Search")))
        .andExpect(jsonPath("$.description", is("This is a search about nothing.")))
        .andExpect(jsonPath("$.fixed", is(false)))
        .andExpect(jsonPath("$.global", is(false)))
        .andExpect(jsonPath("$.savedSearchType", is("other")))
        .andExpect(jsonPath("$.simpleQueryString", is("search + term")))
        .andExpect(jsonPath("$.statuses", is("active,pending")))
        .andExpect(jsonPath("$.gender", is("male")))
        .andExpect(jsonPath("$.occupationIds", is("8577,8484")));
  }

}
