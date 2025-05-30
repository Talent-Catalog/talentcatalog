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
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import static org.tctalent.server.data.PartnerImplTestData.getDestinationPartner;
import static org.tctalent.server.data.PartnerImplTestData.getListOfDestinationPartners;
import static org.tctalent.server.data.SalesforceJobOppTestData.getSalesforceJobOppMinimal;
import static org.tctalent.server.data.UserTestData.getAuditUser;

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
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.User;
import org.tctalent.server.request.partner.SearchPartnerRequest;
import org.tctalent.server.request.partner.UpdatePartnerJobContactRequest;
import org.tctalent.server.request.partner.UpdatePartnerRequest;
import org.tctalent.server.service.db.EmployerService;
import org.tctalent.server.service.db.JobService;
import org.tctalent.server.service.db.PartnerService;
import org.tctalent.server.service.db.UserService;

/**
 * Unit tests for Partner Admin Api endpoints.
 *
 * @author sadatmalik
 */
@WebMvcTest(PartnerAdminApi.class)
@AutoConfigureMockMvc
class PartnerAdminApiTest extends ApiTestBase {
  private static final long PARTNER_ID = 1L;

  private static final String BASE_PATH = "/api/admin/partner";
  private static final String SEARCH_PAGED_PATH = "/search-paged";
  private static final String UPDATE_JOB_CONTACT_PATH = "/update-job-contact";

  private static final List<PartnerImpl> partnerList = getListOfDestinationPartners();
  private static final PartnerImpl partner = getDestinationPartner();

  private final Page<PartnerImpl> partnerPage =
      new PageImpl<>(
          partnerList,
          PageRequest.of(0,10, Sort.unsorted()),
          1
      );

  @MockBean EmployerService employerService;
  @MockBean PartnerService partnerService;
  @MockBean JobService jobService;
  @MockBean UserService userService;

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;
  @Autowired PartnerAdminApi partnerAdminApi;

  @BeforeEach
  void setUp() {
    configureAuthentication();
  }

  @Test
  public void testWebOnlyContextLoads() {
    assertThat(partnerAdminApi).isNotNull();
  }

  @Test
  @DisplayName("create partner succeeds")
  void createPartnerSucceeds() throws Exception {
    UpdatePartnerRequest request = new UpdatePartnerRequest();
    request.setName("My TC Partner");
    request.setStatus(Status.active);

    given(partnerService
        .create(any(UpdatePartnerRequest.class)))
        .willReturn(partner);

    mockMvc.perform(post(BASE_PATH)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$.name", is("TC Partner")))
        .andExpect(jsonPath("$.abbreviation", is("TCP")))
        .andExpect(jsonPath("$.jobCreator", is(true)))
        .andExpect(jsonPath("$.sourcePartner", is(true)))
        .andExpect(jsonPath("$.logo", is("logo_url")))
        .andExpect(jsonPath("$.websiteUrl", is("website_url")))
        .andExpect(jsonPath("$.registrationLandingPage", is("registration_landing_page")))
        .andExpect(jsonPath("$.notificationEmail", is("notification@email.address")))
        .andExpect(jsonPath("$.status", is("active")));

    verify(partnerService).create(any(UpdatePartnerRequest.class));
  }

  @Test
  @DisplayName("get partner by id succeeds")
  void getPartnerByIdSucceeds() throws Exception {

    given(partnerService
        .getPartner(anyLong()))
        .willReturn(partner);

    mockMvc.perform(get(BASE_PATH + "/" + PARTNER_ID)
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$.name", is("TC Partner")))
        .andExpect(jsonPath("$.abbreviation", is("TCP")))
        .andExpect(jsonPath("$.jobCreator", is(true)))
        .andExpect(jsonPath("$.sourcePartner", is(true)))
        .andExpect(jsonPath("$.logo", is("logo_url")))
        .andExpect(jsonPath("$.websiteUrl", is("website_url")))
        .andExpect(jsonPath("$.registrationLandingPage", is("registration_landing_page")))
        .andExpect(jsonPath("$.notificationEmail", is("notification@email.address")))
        .andExpect(jsonPath("$.status", is("active")));

    verify(partnerService).getPartner(anyLong());
  }

  @Test
  @DisplayName("list all partners succeeds")
  void listAllPartnersSucceeds() throws Exception {

    given(partnerService
        .listPartners())
        .willReturn(partnerList);

    mockMvc.perform(get(BASE_PATH)
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$", hasSize(3)))
        .andExpect(jsonPath("$.[0].name", is("TC Partner")))
        .andExpect(jsonPath("$.[0].abbreviation", is("TCP")))
        .andExpect(jsonPath("$.[0].jobCreator", is(true)))
        .andExpect(jsonPath("$.[0].sourcePartner", is(true)))
        .andExpect(jsonPath("$.[0].logo", is("logo_url")))
        .andExpect(jsonPath("$.[0].websiteUrl", is("website_url")))
        .andExpect(jsonPath("$.[0].registrationLandingPage", is("registration_landing_page")))
        .andExpect(jsonPath("$.[0].notificationEmail", is("notification@email.address")))
        .andExpect(jsonPath("$.[0].status", is("active")))
        .andExpect(jsonPath("$.[1].name", is("TC Partner 2")))
        .andExpect(jsonPath("$.[1].abbreviation", is("TCP")))
        .andExpect(jsonPath("$.[1].jobCreator", is(true)))
        .andExpect(jsonPath("$.[1].sourcePartner", is(true)))
        .andExpect(jsonPath("$.[1].logo", is("logo_url")))
        .andExpect(jsonPath("$.[1].websiteUrl", is("website_url")))
        .andExpect(jsonPath("$.[1].registrationLandingPage", is("registration_landing_page")))
        .andExpect(jsonPath("$.[1].notificationEmail", is("notification@email.address")))
        .andExpect(jsonPath("$.[1].status", is("active")))
        .andExpect(jsonPath("$.[2].name", is("TC Partner 3")))
        .andExpect(jsonPath("$.[2].abbreviation", is("TCP")))
        .andExpect(jsonPath("$.[2].jobCreator", is(true)))
        .andExpect(jsonPath("$.[2].sourcePartner", is(true)))
        .andExpect(jsonPath("$.[2].logo", is("logo_url")))
        .andExpect(jsonPath("$.[2].websiteUrl", is("website_url")))
        .andExpect(jsonPath("$.[2].registrationLandingPage", is("registration_landing_page")))
        .andExpect(jsonPath("$.[2].notificationEmail", is("notification@email.address")))
        .andExpect(jsonPath("$.[2].status", is("active")));

    verify(partnerService).listPartners();
  }

  @Test
  @DisplayName("search paged partners succeeds")
  void searchPagedSucceeds() throws Exception {
    SearchPartnerRequest request = new SearchPartnerRequest();

    given(partnerService
        .searchPaged(any(SearchPartnerRequest.class)))
        .willReturn(partnerPage);

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
        .andExpect(jsonPath("$.content[0].name", is("TC Partner")))
        .andExpect(jsonPath("$.content[0].abbreviation", is("TCP")))
        .andExpect(jsonPath("$.content[0].jobCreator", is(true)))
        .andExpect(jsonPath("$.content[0].sourcePartner", is(true)))
        .andExpect(jsonPath("$.content[0].logo", is("logo_url")))
        .andExpect(jsonPath("$.content[0].websiteUrl", is("website_url")))
        .andExpect(jsonPath("$.content[0].registrationLandingPage", is("registration_landing_page")))
        .andExpect(jsonPath("$.content[0].notificationEmail", is("notification@email.address")))
        .andExpect(jsonPath("$.content[0].status", is("active")))
        .andExpect(jsonPath("$.content[1].name", is("TC Partner 2")))
        .andExpect(jsonPath("$.content[1].abbreviation", is("TCP")))
        .andExpect(jsonPath("$.content[1].jobCreator", is(true)))
        .andExpect(jsonPath("$.content[1].sourcePartner", is(true)))
        .andExpect(jsonPath("$.content[1].logo", is("logo_url")))
        .andExpect(jsonPath("$.content[1].websiteUrl", is("website_url")))
        .andExpect(jsonPath("$.content[1].registrationLandingPage", is("registration_landing_page")))
        .andExpect(jsonPath("$.content[1].notificationEmail", is("notification@email.address")))
        .andExpect(jsonPath("$.content[1].status", is("active")))
        .andExpect(jsonPath("$.content[2].name", is("TC Partner 3")))
        .andExpect(jsonPath("$.content[2].abbreviation", is("TCP")))
        .andExpect(jsonPath("$.content[2].jobCreator", is(true)))
        .andExpect(jsonPath("$.content[2].sourcePartner", is(true)))
        .andExpect(jsonPath("$.content[2].logo", is("logo_url")))
        .andExpect(jsonPath("$.content[2].websiteUrl", is("website_url")))
        .andExpect(jsonPath("$.content[2].registrationLandingPage", is("registration_landing_page")))
        .andExpect(jsonPath("$.content[2].notificationEmail", is("notification@email.address")))
        .andExpect(jsonPath("$.content[2].status", is("active")));

    verify(partnerService).searchPaged(any(SearchPartnerRequest.class));
  }

  @Test
  @DisplayName("update partner by id succeeds")
  void updatePartnerByIdSucceeds() throws Exception {
    UpdatePartnerRequest request = new UpdatePartnerRequest();
    request.setDefaultContactId(11L);

    given(userService
        .getUser(11L))
        .willReturn(getAuditUser());

    given(partnerService
        .update(anyLong(), any(UpdatePartnerRequest.class)))
        .willReturn(partner);

    mockMvc.perform(put(BASE_PATH + "/" + PARTNER_ID)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$.name", is("TC Partner")))
        .andExpect(jsonPath("$.abbreviation", is("TCP")))
        .andExpect(jsonPath("$.jobCreator", is(true)))
        .andExpect(jsonPath("$.sourcePartner", is(true)))
        .andExpect(jsonPath("$.logo", is("logo_url")))
        .andExpect(jsonPath("$.websiteUrl", is("website_url")))
        .andExpect(jsonPath("$.registrationLandingPage", is("registration_landing_page")))
        .andExpect(jsonPath("$.notificationEmail", is("notification@email.address")))
        .andExpect(jsonPath("$.status", is("active")));

    verify(userService).getUser(anyLong());
    verify(partnerService).update(anyLong(), any(UpdatePartnerRequest.class));
  }

  @Test
  @DisplayName("update job contact by partner id succeeds")
  void updateJobContactByPartnerIdSucceeds() throws Exception {
    long partnerId = 7L;
    long jobId = 9L;
    long userId = 11L;

    UpdatePartnerJobContactRequest request = new UpdatePartnerJobContactRequest();
    request.setJobId(jobId);
    request.setUserId(userId);

    given(partnerService
        .getPartner(anyLong()))
        .willReturn(partner);

    given(jobService
        .getJob(jobId))
        .willReturn(getSalesforceJobOppMinimal());

    given(userService
        .getUser(userId))
        .willReturn(getAuditUser());

    mockMvc.perform(put(BASE_PATH + "/" + partnerId + UPDATE_JOB_CONTACT_PATH)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$.name", is("TC Partner")))
        .andExpect(jsonPath("$.abbreviation", is("TCP")))
        .andExpect(jsonPath("$.jobCreator", is(true)))
        .andExpect(jsonPath("$.sourcePartner", is(true)))
        .andExpect(jsonPath("$.logo", is("logo_url")))
        .andExpect(jsonPath("$.websiteUrl", is("website_url")))
        .andExpect(jsonPath("$.registrationLandingPage", is("registration_landing_page")))
        .andExpect(jsonPath("$.notificationEmail", is("notification@email.address")))
        .andExpect(jsonPath("$.status", is("active")));

    verify(partnerService).getPartner(anyLong());
    verify(jobService).getJob(anyLong());
    verify(userService).getUser(anyLong());
    verify(partnerService).updateJobContact(any(PartnerImpl.class), any(SalesforceJobOpp.class), any(User.class));

    assertEquals(jobId, partner.getContextJobId());
  }
}
