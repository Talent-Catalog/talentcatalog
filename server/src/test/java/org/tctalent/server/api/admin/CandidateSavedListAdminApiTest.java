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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.tctalent.server.data.SavedListTestData.getSavedLists;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.tctalent.server.request.candidate.HasSetOfSavedListsImpl;
import org.tctalent.server.request.list.SearchSavedListRequest;
import org.tctalent.server.service.db.CandidateSavedListService;
import org.tctalent.server.service.db.SavedListService;

/**
 * Unit tests for Candidate Saved List Admin Api endpoints.
 *
 * @author sadatmalik
 */
@WebMvcTest(CandidateSavedListAdminApi.class)
@AutoConfigureMockMvc
class CandidateSavedListAdminApiTest extends ApiTestBase {

  private static final String BASE_PATH = "/api/admin/candidate-saved-list";

  private static final String REPLACE_PATH = "/{id}/replace";
  private static final String SEARCH_PATH = "/{id}/search";
  private static final String LIST_PATH = "/{id}/list";
  private static final String MERGE_PATH = "/{id}/merge";
  private static final String REMOVE_PATH = "/{id}/remove";
  private static final String SEARCH_PAGED_PATH = "/{id}/search-paged";

  private static final long CANDIDATE_ID = 99L;

  @MockBean CandidateSavedListService candidateSavedListService;
  @MockBean SavedListService savedListService;

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;
  @Autowired CandidateSavedListAdminApi candidateSavedListAdminApi;

  @BeforeEach
  void setUp() {
    configureAuthentication();
  }

  @Test
  public void testWebOnlyContextLoads() {
    assertThat(candidateSavedListAdminApi).isNotNull();
  }

  @Test
  @DisplayName("search succeeds")
  void searchSucceeds() throws Exception {
    SearchSavedListRequest request = new SearchSavedListRequest();

    given(savedListService
        .search(anyLong(), any(SearchSavedListRequest.class)))
        .willReturn(getSavedLists());

    mockMvc.perform(post(BASE_PATH + SEARCH_PATH.replace("{id}", "1"))
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
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].id", is(1)))
        .andExpect(jsonPath("$.[0].description", is("Saved list description")))
        .andExpect(jsonPath("$.[0].displayedFieldsLong[0]", is("user.firstName")))
        .andExpect(jsonPath("$.[0].displayedFieldsLong[1]", is("user.lastName")))
        .andExpect(jsonPath("$.[0].exportColumns").isArray())
        .andExpect(jsonPath("$.[0].exportColumns[0].key", is("key")))
        .andExpect(jsonPath("$.[0].exportColumns[0].properties.constant", is("non default constant column value")))
        .andExpect(jsonPath("$.[0].exportColumns[0].properties.header", is("non default column header")))
        .andExpect(jsonPath("$.[0].status", is("active")))
        .andExpect(jsonPath("$.[0].name", is("Saved list name")))
        .andExpect(jsonPath("$.[0].fixed", is(true)))
        .andExpect(jsonPath("$.[0].global", is(false)))
        .andExpect(jsonPath("$.[0].savedSearchSource.id", is(123)))
        .andExpect(jsonPath("$.[0].sfJobOpp.id", is(135)))
        .andExpect(jsonPath("$.[0].sfJobOpp.sfId", is("sales-force-job-opp-id")))
        .andExpect(jsonPath("$.[0].fileJdLink", is("http://file.jd.link")))
        .andExpect(jsonPath("$.[0].fileJdName", is("JobDescriptionFileName")))
        .andExpect(jsonPath("$.[0].fileJoiLink", is("http://file.joi.link")))
        .andExpect(jsonPath("$.[0].fileJoiName", is("JoiFileName")))
        .andExpect(jsonPath("$.[0].folderlink", is("http://folder.link")))
        .andExpect(jsonPath("$.[0].folderjdlink", is("http://folder.jd.link")))
        .andExpect(jsonPath("$.[0].publishedDocLink", is("http://published.doc.link")))
        .andExpect(jsonPath("$.[0].registeredJob", is(true)))
        .andExpect(jsonPath("$.[0].tcShortName", is("Saved list Tc short name")))
        .andExpect(jsonPath("$.[0].createdBy.firstName", is("test")))
        .andExpect(jsonPath("$.[0].createdBy.lastName", is("user")))
        .andExpect(jsonPath("$.[0].createdDate", is("2023-10-30T12:30:00+02:00")))
        .andExpect(jsonPath("$.[0].updatedBy.firstName", is("test")))
        .andExpect(jsonPath("$.[0].updatedBy.lastName", is("user")))
        .andExpect(jsonPath("$.[0].updatedDate", is("2023-10-30T12:30:00+02:00")))
        .andExpect(jsonPath("$.[0].users[0].firstName", is("test")))
        .andExpect(jsonPath("$.[0].users[0].lastName", is("user")))
        .andExpect(jsonPath("$.[0].tasks[0].id", is(148)))
        .andExpect(jsonPath("$.[0].tasks[0].docLink", is("http://help.link")))
        .andExpect(jsonPath("$.[0].tasks[0].taskType", is("Simple")))
        .andExpect(jsonPath("$.[0].tasks[0].displayName", is("task display name")))
        .andExpect(jsonPath("$.[0].tasks[0].name", is("a test task")))
        .andExpect(jsonPath("$.[0].tasks[0].description", is("a test task description")))
        .andExpect(jsonPath("$.[0].tasks[0].optional", is(false)))
        .andExpect(jsonPath("$.[0].tasks[0].daysToComplete", is(7)));
  }

  @Test
  @DisplayName("replace succeeds")
  void replaceSucceeds() throws Exception {
    HasSetOfSavedListsImpl request = new HasSetOfSavedListsImpl();

    mockMvc.perform(put(BASE_PATH + REPLACE_PATH.replace("{id}", String.valueOf(CANDIDATE_ID)))
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk());

    verify(candidateSavedListService).clearCandidateSavedLists(CANDIDATE_ID);
    verify(candidateSavedListService).mergeCandidateSavedLists(anyLong(), any(HasSetOfSavedListsImpl.class));
  }

  @Test
  @DisplayName("list fails - not implemented")
  void listFailsNotImplemented() throws Exception {

    mockMvc.perform(get(BASE_PATH + LIST_PATH.replace("{id}", "1"))
            .header("Authorization", "Bearer " + "jwt-token")
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$.code", is("not_implemented")))
        .andExpect(jsonPath("$.message", is("Method 'list' of CandidateSavedListAdminApi is not implemented.")));
  }

  @Test
  @DisplayName("merge fails - not implemented")
  void mergeFailsNotImplemented() throws Exception {
    HasSetOfSavedListsImpl request = new HasSetOfSavedListsImpl();

    mockMvc.perform(put(BASE_PATH + MERGE_PATH.replace("{id}", "1"))
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$.code", is("not_implemented")))
        .andExpect(jsonPath("$.message", is("Method 'merge' of CandidateSavedListAdminApi is not implemented.")));
  }

  @Test
  @DisplayName("remove fails - not implemented")
  void removeFailsNotImplemented() throws Exception {
    HasSetOfSavedListsImpl request = new HasSetOfSavedListsImpl();

    mockMvc.perform(put(BASE_PATH + REMOVE_PATH.replace("{id}", "1"))
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$.code", is("not_implemented")))
        .andExpect(jsonPath("$.message", is("Method 'remove' of CandidateSavedListAdminApi is not implemented.")));
  }

  @Test
  @DisplayName("search paged fails - not implemented")
  void searchPagedFailsNotImplemented() throws Exception {
    SearchSavedListRequest request = new SearchSavedListRequest();

    mockMvc.perform(post(BASE_PATH + SEARCH_PAGED_PATH.replace("{id}", "1"))
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$.code", is("not_implemented")))
        .andExpect(jsonPath("$.message", is("Method 'searchPaged' of CandidateSavedListAdminApi is not implemented.")));
  }

}
