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
import org.tctalent.server.model.db.Industry;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.request.industry.CreateIndustryRequest;
import org.tctalent.server.request.industry.SearchIndustryRequest;
import org.tctalent.server.request.industry.UpdateIndustryRequest;
import org.tctalent.server.service.db.IndustryService;

/**
 * Unit tests for Industry Admin Api endpoints.
 *
 * @author sadatmalik
 */
@WebMvcTest(IndustryAdminApi.class)
@AutoConfigureMockMvc
class IndustryAdminApiTest extends ApiTestBase {
  private static final long INDUSTRY_ID = 111L;

  private static final String BASE_PATH = "/api/admin/industry";
  private static final String SEARCH_PAGED_PATH = "/search";

  private final List<Industry> industries = AdminApiTestUtil.getIndustries();

  private final Page<Industry> industryPage =
      new PageImpl<>(
          industries,
          PageRequest.of(0,10, Sort.unsorted()),
          1
      );

  @MockBean IndustryService industryService;

  @Autowired MockMvc mockMvc;

  @Autowired ObjectMapper objectMapper;
  @Autowired IndustryAdminApi industryAdminApi;

  @BeforeEach
  void setUp() {
    configureAuthentication();
  }

  @Test
  public void testWebOnlyContextLoads() {
    assertThat(industryAdminApi).isNotNull();
  }

  @Test
  @DisplayName("get all industries succeeds")
  void getAllIndustriesSucceeds() throws Exception {
    given(industryService
        .listIndustries())
        .willReturn(industries);

    mockMvc.perform(get(BASE_PATH)
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$", hasSize(3)))
        .andExpect(jsonPath("$[0].name", is("Tech")))
        .andExpect(jsonPath("$[0].status", is("active")))
        .andExpect(jsonPath("$[1].name", is("Finance")))
        .andExpect(jsonPath("$[1].status", is("active")))
        .andExpect(jsonPath("$[2].name", is("Health")))
        .andExpect(jsonPath("$[2].status", is("active")));

    verify(industryService).listIndustries();
  }

  @Test
  @DisplayName("search paged industries succeeds")
  void searchPagedIndustriesSucceeds() throws Exception {
    SearchIndustryRequest request = new SearchIndustryRequest();

    given(industryService
        .searchIndustries(any(SearchIndustryRequest.class)))
        .willReturn(industryPage);

    mockMvc.perform(post(BASE_PATH + SEARCH_PAGED_PATH)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.totalElements", is(3)))
        .andExpect(jsonPath("$.totalPages", is(1)))
        .andExpect(jsonPath("$.number", is(0)))
        .andExpect(jsonPath("$.hasNext", is(false)))
        .andExpect(jsonPath("$.hasPrevious", is(false)))
        .andExpect(jsonPath("$.content", notNullValue()))
        .andExpect(jsonPath("$.content[0].name", is("Tech")))
        .andExpect(jsonPath("$.content[0].status", is("active")))
        .andExpect(jsonPath("$.content[1].name", is("Finance")))
        .andExpect(jsonPath("$.content[1].status", is("active")))
        .andExpect(jsonPath("$.content[2].name", is("Health")))
        .andExpect(jsonPath("$.content[2].status", is("active")));

    verify(industryService).searchIndustries(any(SearchIndustryRequest.class));
  }

  @Test
  @DisplayName("get industry by id succeeds")
  void getEducationLevelByIdSucceeds() throws Exception {

    given(industryService
        .getIndustry(INDUSTRY_ID))
        .willReturn(new Industry("AI", Status.active));

    mockMvc.perform(get(BASE_PATH + "/" + INDUSTRY_ID)
            .header("Authorization", "Bearer " + "jwt-token")
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$.name", is("AI")))
        .andExpect(jsonPath("$.status", is("active")));

    verify(industryService).getIndustry(INDUSTRY_ID);
  }

  @Test
  @DisplayName("create industry succeeds")
  void createIndustryLevelSucceeds() throws Exception {
    CreateIndustryRequest request = new CreateIndustryRequest();
    request.setName("AI");
    request.setStatus(Status.active);

    given(industryService
        .createIndustry(any(CreateIndustryRequest.class)))
        .willReturn(new Industry("AI", Status.active));

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
        .andExpect(jsonPath("$.name", is("AI")))
        .andExpect(jsonPath("$.status", is("active")));

    verify(industryService).createIndustry(any(CreateIndustryRequest.class));
  }

  @Test
  @DisplayName("update industry succeeds")
  void updateIndustrySucceeds() throws Exception {
    UpdateIndustryRequest request = new UpdateIndustryRequest();
    request.setName("AI");
    request.setStatus(Status.active);

    given(industryService
        .updateIndustry(anyLong(), any(UpdateIndustryRequest.class)))
        .willReturn(new Industry("AI", Status.active));

    mockMvc.perform(put(BASE_PATH + "/" + INDUSTRY_ID)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$.name", is("AI")))
        .andExpect(jsonPath("$.status", is("active")));

    verify(industryService).updateIndustry(anyLong(), any(UpdateIndustryRequest.class));
  }

  @Test
  @DisplayName("delete industry by id succeeds")
  void deleteEIndustryByIdSucceeds() throws Exception {

    given(industryService
        .deleteIndustry(INDUSTRY_ID))
        .willReturn(true);

    mockMvc.perform(delete(BASE_PATH + "/" + INDUSTRY_ID)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$", is(true)));

    verify(industryService).deleteIndustry(INDUSTRY_ID);
  }
}
