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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
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
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.request.candidate.PublishListRequest;
import org.tctalent.server.request.candidate.PublishedDocImportReport;
import org.tctalent.server.request.candidate.UpdateCandidateContextNoteRequest;
import org.tctalent.server.request.candidate.UpdateDisplayedFieldPathsRequest;
import org.tctalent.server.request.candidate.source.CopySourceContentsRequest;
import org.tctalent.server.request.candidate.source.UpdateCandidateSourceDescriptionRequest;
import org.tctalent.server.request.link.UpdateShortNameRequest;
import org.tctalent.server.request.list.SearchSavedListRequest;
import org.tctalent.server.request.list.UpdateSavedListInfoRequest;
import org.tctalent.server.request.search.UpdateSharingRequest;
import org.tctalent.server.service.db.CandidateSavedListService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.SalesforceService;
import org.tctalent.server.service.db.SavedListService;

@WebMvcTest(SavedListAdminApi.class)
@AutoConfigureMockMvc
class SavedListAdminApiTest extends ApiTestBase {

    private static final long SAVED_LIST_ID = 1L;

    private static final String BASE_PATH = "/api/admin/saved-list";
    private static final String COPY_PATH = "/copy/{id}";
    private static final String CONTEXT_NOTE_PATH = "/context/{id}";
    private static final String DESCRIPTION_PATH = "/description/{id}";
    private static final String DISPLAYED_FIELDS_PATH = "/displayed-fields/{id}";
    private static final String SHORT_NAME_PATH = "/short-name";
    private static final String CREATE_FOLDER_PATH = "/{id}/create-folder";
    private static final String FEEDBACK_PATH = "/{id}/feedback";
    private static final String PUBLISH_PATH = "/{id}/publish";
    private static final String ADD_SHARED_USER_PATH = "/shared-add/{id}";
    private static final String REMOVE_SHARED_USER_PATH = "/shared-remove/{id}";
    private static final String SEARCH_PAGED_PATH = "/search-paged";
    private static final String SEARCH_PATH = "/search";

    private static final SavedList savedList = AdminApiTestUtil.getSavedList();
    private static final List<SavedList> savedLists = AdminApiTestUtil.getSavedLists();

    private final Page<SavedList> savedListPage =
        new PageImpl<>(
            savedLists,
            PageRequest.of(0,10, Sort.unsorted()),
            1
        );

    @MockBean
    SavedListService savedListService;
    @MockBean
    CandidateService candidateService;
    @MockBean
    CandidateSavedListService candidateSavedListService;
    @MockBean
    SalesforceService salesforceService;

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired SavedListAdminApi savedListAdminApi;

    @BeforeEach
    void setUp() {
        configureAuthentication();
    }

    @Test
    public void testWebOnlyContextLoads() {
        assertThat(savedListAdminApi).isNotNull();
    }

    @Test
    @DisplayName("create saved list succeeds")
    void createSavedListSucceeds() throws Exception {
        UpdateSavedListInfoRequest request = new UpdateSavedListInfoRequest();

        given(savedListService
            .createSavedList(any(UpdateSavedListInfoRequest.class)))
            .willReturn(savedList);

        mockMvc.perform(post(BASE_PATH)
                .with(csrf())
                .header("Authorization", "Bearer " + "jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))

            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", notNullValue()))
            .andExpect(jsonPath("$.id", is(1)));

        verify(savedListService).createSavedList(any(UpdateSavedListInfoRequest.class));
    }

    @Test
    @DisplayName("delete saved list by id succeeds")
    void deleteSavedListByIdSucceeds() throws Exception {

        given(candidateSavedListService
            .deleteSavedList(SAVED_LIST_ID))
            .willReturn(true);

        mockMvc.perform(delete(BASE_PATH + "/" + SAVED_LIST_ID)
                .with(csrf())
                .header("Authorization", "Bearer " + "jwt-token")
                .accept(MediaType.APPLICATION_JSON))

            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", notNullValue()))
            .andExpect(jsonPath("$", is(true)));

        verify(candidateSavedListService).deleteSavedList(SAVED_LIST_ID);
    }

    @Test
    @DisplayName("get saved list by id succeeds")
    void getSavedListByIdSucceeds() throws Exception {

        given(savedListService
            .get(SAVED_LIST_ID))
            .willReturn(savedList);

        mockMvc.perform(get(BASE_PATH + "/" + SAVED_LIST_ID)
                .header("Authorization", "Bearer " + "jwt-token")
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
            .andExpect(jsonPath("$.tasks[0].daysToComplete", is(7)));

        verify(savedListService).get(SAVED_LIST_ID);
    }

    @Test
    @DisplayName("search saved lists succeeds")
    void searchSavedListsSucceeds() throws Exception {
        SearchSavedListRequest request = new SearchSavedListRequest();
        given(savedListService
            .search(any(SearchSavedListRequest.class)))
            .willReturn(savedLists);

        mockMvc.perform(post(BASE_PATH + SEARCH_PATH)
                .with(csrf())
                .header("Authorization", "Bearer " + "jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))

            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", notNullValue()))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(1)));

        verify(savedListService).search(any(SearchSavedListRequest.class));
    }

    @Test
    @DisplayName("search paged saved lists succeeds")
    void searchPagedSavedListsSucceeds() throws Exception {
        SearchSavedListRequest request = new SearchSavedListRequest();

        given(savedListService
            .searchPaged(any(SearchSavedListRequest.class)))
            .willReturn(savedListPage);

        mockMvc.perform(post(BASE_PATH + SEARCH_PAGED_PATH)
                .with(csrf())
                .header("Authorization", "Bearer " + "jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))

            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.totalElements", is(1)))
            .andExpect(jsonPath("$.totalPages", is(1)))
            .andExpect(jsonPath("$.number", is(0)))
            .andExpect(jsonPath("$.hasNext", is(false)))
            .andExpect(jsonPath("$.hasPrevious", is(false)))
            .andExpect(jsonPath("$.content", notNullValue()))
            .andExpect(jsonPath("$.content.[0].id", is(1)));

        verify(savedListService).searchPaged(any(SearchSavedListRequest.class));
    }

    @Test
    @DisplayName("update saved list succeeds")
    void updateSavedListSucceeds() throws Exception {
        UpdateSavedListInfoRequest request = new UpdateSavedListInfoRequest();

        given(savedListService
            .updateSavedList(anyLong(), any(UpdateSavedListInfoRequest.class)))
            .willReturn(savedList);

        mockMvc.perform(put(BASE_PATH + "/" + SAVED_LIST_ID)
                .with(csrf())
                .header("Authorization", "Bearer " + "jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))

            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", notNullValue()))
            .andExpect(jsonPath("$.id", is(1)));

        verify(savedListService).updateSavedList(anyLong(), any(UpdateSavedListInfoRequest.class));
    }

    @Test
    @DisplayName("copy saved list succeeds")
    void copySavedListSucceeds() throws Exception {
        CopySourceContentsRequest request = new CopySourceContentsRequest();

        // Set up the source list which I am copying across to my test saved list.
        Long testTargetListId = 11L;
        SavedList targetList = new SavedList();
        targetList.setId(testTargetListId);
        targetList.setName("test list");

        given(savedListService
            .get(SAVED_LIST_ID))
            .willReturn(savedList);

        given(candidateSavedListService
            .copy(any(SavedList.class), any(CopySourceContentsRequest.class)))
            .willReturn(targetList);

        mockMvc.perform(put(BASE_PATH + COPY_PATH.replace("{id}", Long.toString(SAVED_LIST_ID)))
                .with(csrf())
                .header("Authorization", "Bearer " + "jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))

            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", notNullValue()))
            .andExpect(jsonPath("$.id", is(testTargetListId.intValue())));

        verify(savedListService).get(anyLong());
        verify(candidateSavedListService).copy(any(SavedList.class), any(CopySourceContentsRequest.class));
    }

    @Test
    @DisplayName("create folder succeeds")
    void createFolderSucceeds() throws Exception {
         given(savedListService
                .createListFolder(SAVED_LIST_ID))
                .willReturn(savedList);

        mockMvc.perform(put(BASE_PATH + CREATE_FOLDER_PATH.replace("{id}", Long.toString(SAVED_LIST_ID)))
                        .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token")
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.id", is(1)));

        verify(savedListService).createListFolder(anyLong());
    }

    @Test
    @DisplayName("add shared user succeeds")
    void addSharedUserSucceeds() throws Exception {
        UpdateSharingRequest request = new UpdateSharingRequest();

        given(savedListService
                .addSharedUser(anyLong(), any(UpdateSharingRequest.class)))
                .willReturn(savedList);

        mockMvc.perform(put(BASE_PATH + ADD_SHARED_USER_PATH.replace("{id}", Long.toString(SAVED_LIST_ID)))
                        .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.id", is(1)));

        verify(savedListService).addSharedUser(anyLong(), any(UpdateSharingRequest.class));
    }

    @Test
    @DisplayName("remove shared user succeeds")
    void removeSharedUserSucceeds() throws Exception {
        UpdateSharingRequest request = new UpdateSharingRequest();

        given(savedListService
                .removeSharedUser(anyLong(), any(UpdateSharingRequest.class)))
                .willReturn(savedList);

        mockMvc.perform(put(BASE_PATH + REMOVE_SHARED_USER_PATH.replace("{id}", Long.toString(SAVED_LIST_ID)))
                        .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.id", is(1)));

        verify(savedListService).removeSharedUser(anyLong(), any(UpdateSharingRequest.class));
    }

    @Test
    @DisplayName("import employer feedback succeeds")
    void importEmployerFeedbackSucceeds() throws Exception {
        PublishedDocImportReport report = new PublishedDocImportReport();
        report.setMessage("this is a message.");

        given(savedListService
                .importEmployerFeedback(anyLong()))
                .willReturn(report);

        mockMvc.perform(put(BASE_PATH + FEEDBACK_PATH.replace("{id}", Long.toString(SAVED_LIST_ID)))
                        .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token")
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.message", is("this is a message.")));

        verify(savedListService).importEmployerFeedback(anyLong());
    }

    @Test
    @DisplayName("publish saved list succeeds")
    void publishSavedListSucceeds() throws Exception {
        PublishListRequest request = new PublishListRequest();
        request.setColumns(new ArrayList<>());

        given(savedListService
                .publish(anyLong(), any(PublishListRequest.class)))
                .willReturn(savedList);

        mockMvc.perform(put(BASE_PATH + PUBLISH_PATH.replace("{id}", Long.toString(SAVED_LIST_ID)))
                        .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.id", is(1)));

        verify(savedListService).publish(anyLong(), any(PublishListRequest.class));
    }

    @Test
    @DisplayName("update context note succeeds")
    void updateContextNoteSucceeds() throws Exception {
        UpdateCandidateContextNoteRequest request = new UpdateCandidateContextNoteRequest();

        mockMvc.perform(put(BASE_PATH + CONTEXT_NOTE_PATH.replace("{id}", Long.toString(SAVED_LIST_ID)))
                        .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk());

        verify(candidateSavedListService).updateCandidateContextNote(anyLong(), any(UpdateCandidateContextNoteRequest.class));
    }

    @Test
    @DisplayName("update description succeeds")
    void updateDescriptionSucceeds() throws Exception {
        UpdateCandidateSourceDescriptionRequest request = new UpdateCandidateSourceDescriptionRequest();

        mockMvc.perform(put(BASE_PATH + DESCRIPTION_PATH.replace("{id}", Long.toString(SAVED_LIST_ID)))
                        .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk());

        verify(savedListService).updateDescription(anyLong(), any(UpdateCandidateSourceDescriptionRequest.class));
    }

    @Test
    @DisplayName("update displayed field paths succeeds")
    void updateDisplayedFieldPathsSucceeds() throws Exception {
        UpdateDisplayedFieldPathsRequest request = new UpdateDisplayedFieldPathsRequest();

        mockMvc.perform(put(BASE_PATH + DISPLAYED_FIELDS_PATH.replace("{id}", Long.toString(SAVED_LIST_ID)))
                        .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk());

        verify(savedListService).updateDisplayedFieldPaths(anyLong(), any(UpdateDisplayedFieldPathsRequest.class));
    }

    @Test
    @DisplayName("update tbb short name succeeds")
    void updateTcShortName() throws Exception {
        UpdateShortNameRequest request = new UpdateShortNameRequest();

        mockMvc.perform(put(BASE_PATH + SHORT_NAME_PATH.replace("{id}", Long.toString(SAVED_LIST_ID)))
                        .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk());

        verify(savedListService).updateTcShortName(any(UpdateShortNameRequest.class));
    }
}
