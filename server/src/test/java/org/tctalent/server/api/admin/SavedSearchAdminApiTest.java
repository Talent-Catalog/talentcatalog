/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
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
import org.tctalent.server.model.db.SavedSearch;
import org.tctalent.server.request.candidate.SearchCandidateRequest;
import org.tctalent.server.request.search.UpdateSavedSearchRequest;
import org.tctalent.server.service.db.CandidateSavedListService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.SavedListService;
import org.tctalent.server.service.db.SavedSearchService;

/**
 * Unit tests for Saved Search Admin Api endpoints
 *
 * @author samschlicht
 */

@WebMvcTest(SavedSearchAdminApi.class)
@AutoConfigureMockMvc
class SavedSearchAdminApiTest extends ApiTestBase {
  private static final String BASE_PATH = "/api/admin/saved-search";
  private static final SavedSearch savedSearch = AdminApiTestUtil.getSavedSearch();

  private final Page<SavedSearch> savedSearchPage =
      new PageImpl<>(
          List.of(savedSearch),
          PageRequest.of(0,10, Sort.unsorted()),
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
  void setUp() { configureAuthentication(); }

  @Test
  public void testWebOnlyContextLoads() { assertThat(savedSearchAdminApi).isNotNull(); }

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

    mockMvc.perform(post(BASE_PATH)
                    .header("Authorization", "Bearer " + "jwt-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
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

    verify(savedSearchService).createSavedSearch(any(UpdateSavedSearchRequest.class));
  }

  @Test
  @DisplayName("delete by id returns true")
  void deleteByIdReturnsTrue() throws Exception {
    long id = 123L;

    given(savedSearchService
            .deleteSavedSearch(anyLong()))
            .willReturn(true);

    mockMvc.perform(delete(BASE_PATH + "/" + id)
                    .header("Authorization", "Bearer " + "jwt-token"))

            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", is(true)));

    verify(savedSearchService).deleteSavedSearch(anyLong());
  }
}
