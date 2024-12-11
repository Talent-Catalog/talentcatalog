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
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
import org.tctalent.server.model.db.BrandingInfo;
import org.tctalent.server.service.db.BrandingService;
import org.tctalent.server.service.db.RootRequestService;

/**
 * Unit tests for Root Route Admin Api endpoints.
 *
 * @author sadatmalik
 */
@WebMvcTest(RootRouteAdminApi.class)
@AutoConfigureMockMvc
class RootRouteAdminApiTest extends ApiTestBase {
  private static final String BASE_PATH = "/";

  private BrandingInfo brandingInfo;

  @MockBean BrandingService brandingService;
  @MockBean RootRequestService rootRequestService;

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;
  @Autowired RootRouteAdminApi rootRouteAdminApi;

  @BeforeEach
  void setUp() {
    configureAuthentication();
    brandingInfo = new BrandingInfo();
  }

  @Test
  public void testWebOnlyContextLoads() {
    assertThat(rootRouteAdminApi).isNotNull();
  }

  @Test
  @DisplayName("redirect sub-domain url to plain url succeeds")
  void redirectSubDomainUrlToPlainUrlSucceeds() throws Exception {

    mockMvc.perform(get(BASE_PATH)
            .header("Authorization", "Bearer " + "jwt-token")
            .header("Host", "crs.tctalent.org")
            .queryParam("h", "true")
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isFound())
        .andExpect(header().string("Location", containsString(("tctalent.org?p=crs"))));
  }

  @Test
  @DisplayName("go to landing page succeeds")
  void goToLandingPageSucceeds() throws Exception {
    brandingInfo.setLandingPage("landing-page-url");

    given(brandingService
        .getBrandingInfo(anyString()))
        .willReturn(brandingInfo);

    mockMvc.perform(get(BASE_PATH)
            .header("Authorization", "Bearer " + "jwt-token")
            .queryParam("p", "partner")
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isFound())
        .andExpect(header().string("Location", containsString(("landing-page-url"))));
  }

  @Test
  @DisplayName("go to candidate=portal if no landing page found succeeds")
  void goToCandidatePortalIfNoLandingPageSucceeds() throws Exception {

    given(brandingService
        .getBrandingInfo(anyString()))
        .willReturn(brandingInfo);

    mockMvc.perform(get(BASE_PATH)
            .header("Authorization", "Bearer " + "jwt-token")
            .queryParam("p", "partner")
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isFound())
        .andExpect(header().string("Location", containsString(("/candidate-portal/login"))));
  }

  @Test
  @DisplayName("redirect contains query string succeeds")
  void redirectContainsQueryStringSucceeds() throws Exception {

    given(brandingService
        .getBrandingInfo(anyString()))
        .willReturn(brandingInfo);

    mockMvc.perform(get(BASE_PATH)
            .header("Authorization", "Bearer " + "jwt-token")
            .header("Host", "tctalent.org")
            .queryParam("p", "partner")
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isFound())
        .andExpect(header().string("Location", containsString(("?p=partner"))));
  }

}
