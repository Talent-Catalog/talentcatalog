/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

  // replace

  // search

  // list

  @Test
  @DisplayName("merge fails - not implemented")
  void mergeFailsNotImplemented() throws Exception {
    HasSetOfSavedListsImpl request = new HasSetOfSavedListsImpl();

    mockMvc.perform(put(BASE_PATH + MERGE_PATH.replace("{id}", "1"))
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