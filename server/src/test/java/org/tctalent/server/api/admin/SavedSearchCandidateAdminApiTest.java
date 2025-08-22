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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.tctalent.server.data.CandidateTestData.getListOfCandidates;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.PrintWriter;
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
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.request.candidate.SavedSearchGetRequest;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.SavedSearchService;
import org.tctalent.server.util.dto.DtoBuilder;

/**
 * Unit tests for Saved Search Candidate Admin Api endpoints
 *
 * @author samschlicht
 */

@WebMvcTest(SavedSearchCandidateAdminApi.class)
@AutoConfigureMockMvc
class SavedSearchCandidateAdminApiTest extends ApiTestBase {
  private static final String BASE_PATH = "/api/admin/saved-search-candidate";
  private static final String IS_EMPTY_PATH = "/{id}/is-empty";
  private static final String EXPORT_CSV_PATH = "/{id}/export/csv";
  private static final String SEARCH_PAGED_PATH = "/{id}/search-paged";
  private static final Long SAVED_SEARCH_ID = 123L;

  private final Page<Candidate> candidatePage =
      new PageImpl<>(
          getListOfCandidates(),
          PageRequest.of(0, 10, Sort.unsorted()),
          1
      );

  @MockBean
  SavedSearchService savedSearchService;
  @MockBean
  CandidateService candidateService;
  @MockBean
  CandidateBuilderSelector candidateBuilderSelector;

  @Autowired
  MockMvc mockMvc;
  @Autowired
  ObjectMapper objectMapper;
  @Autowired
  SavedSearchCandidateAdminApi savedSearchCandidateAdminApi;

  @BeforeEach
  void setUp() {
    configureAuthentication();

    // Minimal builder for Candidate
    DtoBuilder candidateDto = new DtoBuilder()
        .add("id")
        .add("selected")
        .add("status");

    given(candidateBuilderSelector.selectBuilder(any())).willReturn(candidateDto);
  }

  @Test
  @DisplayName("saved search candidate search paged succeeds")
  void savedSearchCandidateSearchPagedSucceeds() throws Exception {
    SavedSearchGetRequest request = new SavedSearchGetRequest();

    given(savedSearchService
        .searchCandidates(anyLong(), any(SavedSearchGetRequest.class)))
        .willReturn(candidatePage);

    mockMvc.perform(post(BASE_PATH + SEARCH_PAGED_PATH.replace("{id}", Long.toString(SAVED_SEARCH_ID)))
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.number", is(0)))
        .andExpect(jsonPath("$.numberOfElements", is(3)))
        .andExpect(jsonPath("$.totalPages", is(1)))
        .andExpect(jsonPath("$.hasPrevious", is(false)))
        .andExpect(jsonPath("$.hasNext", is(false)))
        .andExpect(jsonPath("$.content", notNullValue()))
        .andExpect(jsonPath("$.content[0].selected", is(false)))
        .andExpect(jsonPath("$.content[0].status", is("draft")));

  verify(savedSearchService).searchCandidates(anyLong(), any(SavedSearchGetRequest.class));
  verify(savedSearchService).setCandidateContext(anyLong(), any(Page.class));
  }

  @Test
  @DisplayName("is empty check succeeds")
  void isEmptyCheckSucceeds() throws Exception {
    given(savedSearchService
        .isEmpty(anyLong()))
        .willReturn(anyBoolean());

    mockMvc.perform(get(BASE_PATH + IS_EMPTY_PATH.replace("{id}", Long.toString(SAVED_SEARCH_ID)))
            .header("Authorization", "Bearer " + "jwt-token"))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", is(false)));
  }

  @Test
  @DisplayName("export csv succeeds")
  void exportCsvSucceeds() throws Exception {
    SavedSearchGetRequest request = new SavedSearchGetRequest();

    mockMvc.perform(post(BASE_PATH + EXPORT_CSV_PATH.replace("{id}", Long.toString(SAVED_SEARCH_ID)))
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))

        .andDo(print())
        .andExpect(status().isOk());

  verify(savedSearchService).exportToCsv(
      anyLong(),
      any(SavedSearchGetRequest.class),
      any(PrintWriter.class));
  }

}
