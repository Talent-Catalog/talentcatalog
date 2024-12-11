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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.service.db.SavedListService;

/**
 * Unit tests for Published Admin Api endpoints.
 *
 * @author sadatmalik
 */
@WebMvcTest(PublishedLinkAdminApi.class)
@AutoConfigureMockMvc
class PublishedLinkAdminApiTest extends ApiTestBase {
  private static final String SHORT_NAME = "short-name";

  private static final String BASE_PATH = "/published";

  private final SavedList savedList = new SavedList();

  @MockBean SavedListService savedListService;

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;
  @Autowired PublishedLinkAdminApi publishedLinkAdminApi;

  @BeforeEach
  void setUp() {
    configureAuthentication();
  }

  @Test
  public void testWebOnlyContextLoads() {
    assertThat(publishedLinkAdminApi).isNotNull();
  }

  @Test
  @DisplayName("short name redirect published doc link found")
  void shortNameRedirectFound() throws Exception {
    savedList.setPublishedDocLink("published-doc-link");

    given(savedListService
        .findByShortName(anyString()))
        .willReturn(savedList);

    mockMvc.perform(get(BASE_PATH + "/" + SHORT_NAME)
            .header("Authorization", "Bearer " + "jwt-token")
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isFound());

    verify(savedListService).findByShortName(anyString());
  }

  @Test
  @DisplayName("short name redirect published doc link not found")
  void shortNameRedirectNotFound() throws Exception {

    given(savedListService
        .findByShortName(anyString()))
        .willReturn(savedList);

    mockMvc.perform(get(BASE_PATH + "/" + SHORT_NAME)
            .header("Authorization", "Bearer " + "jwt-token")
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isNotFound());

    verify(savedListService).findByShortName(anyString());
  }

}
