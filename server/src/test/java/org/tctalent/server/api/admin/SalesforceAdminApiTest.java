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
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.tctalent.server.data.OpportunityTestData.getOpportunity;

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
import org.tctalent.server.configuration.SalesforceRecordTypeConfig;
import org.tctalent.server.exception.SalesforceException;
import org.tctalent.server.model.sf.Opportunity;
import org.tctalent.server.request.opportunity.UpdateEmployerOpportunityRequest;
import org.tctalent.server.service.db.SalesforceService;

/**
 * Unit tests for Salesforce Api endpoints.
 *
 * @author sadatmalik
 */
@WebMvcTest(SalesforceAdminApi.class)
@AutoConfigureMockMvc
class SalesforceAdminApiTest extends ApiTestBase {
  private static final String SF_OPPORTUNITY_URL
      = "https://talentbeyondboundaries.lightning.force.com/lightning/r/Opportunity/";
  private static final String SF_OPPORTUNITY_ID = "001ABCDEF012345678";
  private static final String BASE_PATH = "/api/admin/sf";
  private static final String SF_OPPORTUNITY_PATH = "/opportunity";
  private static final String UPDATE_EMPLOYER_OPPORTUNITY_PATH = "/update-emp-opp";

  private final Opportunity opportunity = getOpportunity();

  @MockBean SalesforceService salesforceService;
  @MockBean SalesforceRecordTypeConfig salesforceRecordTypeConfig;

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;
  @Autowired SalesforceAdminApi salesforceAdminApi;

  @BeforeEach
  void setUp() {
    configureAuthentication();
  }

  @Test
  public void testWebOnlyContextLoads() {
    assertThat(salesforceAdminApi).isNotNull();
  }

  @Test
  @DisplayName("get opportunity succeeds")
  void getOpportunitySucceeds() throws Exception {

    opportunity.setRecordTypeId("employer-record-id");

    given(salesforceService
        .fetchJobOpportunity(anyString()))
        .willReturn(opportunity);

    given(salesforceRecordTypeConfig.getEmployerJob()).willReturn("employer-record-id");

    mockMvc.perform(get(BASE_PATH + SF_OPPORTUNITY_PATH)
            .header("Authorization", "Bearer " + "jwt-token")
            .queryParam("url", SF_OPPORTUNITY_URL + SF_OPPORTUNITY_ID)
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name", is("Opportunity")));

    verify(salesforceService).fetchJobOpportunity(anyString());
  }

  @Test
  @DisplayName("get opportunity fails with salesforce exception")
  void getOpportunityFailsWithSalesforceException() throws Exception {

    given(salesforceService
        .fetchJobOpportunity(anyString()))
        .willThrow(new SalesforceException("SalesforceException message"));

    mockMvc.perform(get(BASE_PATH + SF_OPPORTUNITY_PATH)
            .header("Authorization", "Bearer " + "jwt-token")
            .queryParam("url", SF_OPPORTUNITY_URL + SF_OPPORTUNITY_ID)
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code", is("salesforce")))
        .andExpect(jsonPath("$.message", is("SalesforceException message")));

    verify(salesforceService).fetchJobOpportunity(anyString());
  }

  @Test
  @DisplayName("update employer opportunity succeeds")
  void updateEmployerOpportunitySucceeds() throws Exception {
    UpdateEmployerOpportunityRequest request = new UpdateEmployerOpportunityRequest();

    mockMvc.perform(put(BASE_PATH + UPDATE_EMPLOYER_OPPORTUNITY_PATH)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk());

    verify(salesforceService).updateEmployerOpportunity(any(UpdateEmployerOpportunityRequest.class));
  }

  @Test
  @DisplayName("update employer opportunity fails with salesforce exception")
  void updateEmployerOpportunityFailsWithSalesforceException() throws Exception {
    UpdateEmployerOpportunityRequest request = new UpdateEmployerOpportunityRequest();

    doThrow(new SalesforceException("SalesforceException message"))
        .when(salesforceService)
            .updateEmployerOpportunity(any(UpdateEmployerOpportunityRequest.class));

    mockMvc.perform(put(BASE_PATH + UPDATE_EMPLOYER_OPPORTUNITY_PATH)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code", is("salesforce")))
        .andExpect(jsonPath("$.message", is("SalesforceException message")));

    verify(salesforceService).updateEmployerOpportunity(any(UpdateEmployerOpportunityRequest.class));
  }

}
