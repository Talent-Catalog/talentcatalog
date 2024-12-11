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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.tctalent.server.api.admin.StatsApiTestUtil.getBirthYearStats;
import static org.tctalent.server.api.admin.StatsApiTestUtil.getGenderStats;
import static org.tctalent.server.api.admin.StatsApiTestUtil.getLanguageStats;
import static org.tctalent.server.api.admin.StatsApiTestUtil.getLinkedInByRegistrationDateStats;
import static org.tctalent.server.api.admin.StatsApiTestUtil.getLinkedInExistsStats;
import static org.tctalent.server.api.admin.StatsApiTestUtil.getMaxEducationStats;
import static org.tctalent.server.api.admin.StatsApiTestUtil.getNationalityStats;
import static org.tctalent.server.api.admin.StatsApiTestUtil.getOccupationStats;
import static org.tctalent.server.api.admin.StatsApiTestUtil.getReferrerStats;
import static org.tctalent.server.api.admin.StatsApiTestUtil.getRegistrationByOccupationStats;
import static org.tctalent.server.api.admin.StatsApiTestUtil.getRegistrationStats;
import static org.tctalent.server.api.admin.StatsApiTestUtil.getSourceCountryStats;
import static org.tctalent.server.api.admin.StatsApiTestUtil.getSpokenLanguageLevelStats;
import static org.tctalent.server.api.admin.StatsApiTestUtil.getStatusStats;
import static org.tctalent.server.api.admin.StatsApiTestUtil.getSurveyStats;
import static org.tctalent.server.api.admin.StatsApiTestUtil.getUnhcrRegistrationStats;
import static org.tctalent.server.api.admin.StatsApiTestUtil.getUnhcrStatusStats;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.Set;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.repository.db.CountryRepository;
import org.tctalent.server.request.candidate.stat.CandidateStatsRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.CandidateStatsService;
import org.tctalent.server.service.db.SavedListService;
import org.tctalent.server.service.db.SavedSearchService;

/**
 * Unit tests for Candidate Stats Admin Api endpoints.
 *
 * @author sadatmalik
 */
@WebMvcTest(CandidateStatAdminApi.class)
@AutoConfigureMockMvc
class CandidateStatAdminApiTest extends ApiTestBase {

  private static final String BASE_PATH = "/api/admin/candidate/stat";

  private static final String ALL_STATS_PATH = "/all";

  @MockBean CandidateService candidateService;
  @MockBean CandidateStatsService candidateStatsService;
  @MockBean CountryRepository countryRepository;
  @MockBean SavedListService savedListService;
  @MockBean SavedSearchService savedSearchService;
  @MockBean AuthService authService;
  @MockBean EntityManager entityManager;

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;
  @Autowired CandidateStatAdminApi candidateStatAdminApi;

  private CandidateStatsRequest request = new CandidateStatsRequest();

  @BeforeEach
  void setUp() {
    request = new CandidateStatsRequest();
    request.setRunOldStats(true);

    configureAuthentication();

    user.setSourceCountries(Set.of(
        new Country("Jordan", Status.active),
        new Country("Lebanon", Status.active),
        new Country("Pakistan", Status.active)
    ));

    given(authService
        .getLoggedInUser())
        .willReturn(Optional.of(user));
  }

  @Test
  public void testWebOnlyContextLoads() {
    assertThat(candidateStatAdminApi).isNotNull();
  }

  @Test
  @DisplayName("get all stats - gender report succeeds")
  void getAllStatsGenderReportSucceeds() throws Exception {

    given(candidateService
        .computeGenderStats(any(), any(), any()))
        .willReturn(getGenderStats());

    mockMvc.perform(post(BASE_PATH + ALL_STATS_PATH)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$", hasSize(49)))

        // Gender
        .andExpect(jsonPath("$[0].name", is("Gender")))
        .andExpect(jsonPath("$[0].chartType", is("bar")))
        .andExpect(jsonPath("$[0].rows", notNullValue()))
        .andExpect(jsonPath("$[0].rows", hasSize(3)))
        .andExpect(jsonPath("$[0].rows[0].label", is("male")))
        .andExpect(jsonPath("$[0].rows[0].value", is(15111)))
        .andExpect(jsonPath("$[0].rows[1].label", is("undefined")))
        .andExpect(jsonPath("$[0].rows[1].value", is(3772)))
        .andExpect(jsonPath("$[0].rows[2].label", is("female")))
        .andExpect(jsonPath("$[0].rows[2].value", is(2588)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateService).computeGenderStats(any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - registration report succeeds")
  void getAllStatsRegistrationReportSucceeds() throws Exception {

    given(candidateService
        .computeRegistrationStats(any(), any(), any()))
        .willReturn(getRegistrationStats());

    mockMvc.perform(post(BASE_PATH + ALL_STATS_PATH)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$", hasSize(49)))

        // Registrations
        .andExpect(jsonPath("$[1].name", is("Registrations")))
        .andExpect(jsonPath("$[1].chartType", is("bar")))
        .andExpect(jsonPath("$[1].rows", notNullValue()))
        .andExpect(jsonPath("$[1].rows", hasSize(3)))
        .andExpect(jsonPath("$[1].rows[0].label", is("2016-06-04")))
        .andExpect(jsonPath("$[1].rows[0].value", is(3)))
        .andExpect(jsonPath("$[1].rows[1].label", is("2016-06-10")))
        .andExpect(jsonPath("$[1].rows[1].value", is(2)))
        .andExpect(jsonPath("$[1].rows[2].label", is("2016-06-14")))
        .andExpect(jsonPath("$[1].rows[2].value", is(1)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateService).computeRegistrationStats(any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - registration by occupation report succeeds")
  void getAllStatsRegistrationByOccupationReportSucceeds() throws Exception {

    given(candidateService
        .computeRegistrationOccupationStats(any(), any(), any()))
        .willReturn(getRegistrationByOccupationStats());

    mockMvc.perform(post(BASE_PATH + ALL_STATS_PATH)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$", hasSize(49)))

        // Registrations by Occupation
        .andExpect(jsonPath("$[2].name", is("Registrations (by occupations)")))
        .andExpect(jsonPath("$[2].chartType", is("doughnut")))
        .andExpect(jsonPath("$[2].rows", notNullValue()))
        .andExpect(jsonPath("$[2].rows", hasSize(3)))
        .andExpect(jsonPath("$[2].rows[0].label", is("undefined")))
        .andExpect(jsonPath("$[2].rows[0].value", is(11414)))
        .andExpect(jsonPath("$[2].rows[1].label", is("Unknown")))
        .andExpect(jsonPath("$[2].rows[1].value", is(1652)))
        .andExpect(jsonPath("$[2].rows[2].label", is("Teacher")))
        .andExpect(jsonPath("$[2].rows[2].value", is(777)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateService).computeRegistrationOccupationStats(any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - birth year report succeeds")
  void getAllStatsBirthYearReportSucceeds() throws Exception {

    given(candidateService
        .computeBirthYearStats(any(), any(), any(), any()))
        .willReturn(getBirthYearStats());

    mockMvc.perform(post(BASE_PATH + ALL_STATS_PATH)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$", hasSize(49)))

        // Birth year
        .andExpect(jsonPath("$[3].name", is("Birth years")))
        .andExpect(jsonPath("$[3].chartType", is("bar")))
        .andExpect(jsonPath("$[3].rows", notNullValue()))
        .andExpect(jsonPath("$[3].rows", hasSize(3)))
        .andExpect(jsonPath("$[3].rows[0].label", is("1948")))
        .andExpect(jsonPath("$[3].rows[0].value", is(3)))
        .andExpect(jsonPath("$[3].rows[1].label", is("1950")))
        .andExpect(jsonPath("$[3].rows[1].value", is(2)))
        .andExpect(jsonPath("$[3].rows[2].label", is("1951")))
        .andExpect(jsonPath("$[3].rows[2].value", is(1)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateService, times(3)).computeBirthYearStats(any(), any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - linked in links report succeeds")
  void getAllStatsLinkedInLinksReportSucceeds() throws Exception {

    given(candidateService
        .computeLinkedInExistsStats(any(), any(), any()))
        .willReturn(getLinkedInExistsStats());

    mockMvc.perform(post(BASE_PATH + ALL_STATS_PATH)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$", hasSize(49)))

        // Linkedin links
        .andExpect(jsonPath("$[6].name", is("LinkedIn links")))
        .andExpect(jsonPath("$[6].chartType", is("bar")))
        .andExpect(jsonPath("$[6].rows", notNullValue()))
        .andExpect(jsonPath("$[6].rows", hasSize(2)))
        .andExpect(jsonPath("$[6].rows[0].label", is("No link")))
        .andExpect(jsonPath("$[6].rows[0].value", is(2)))
        .andExpect(jsonPath("$[6].rows[1].label", is("Has link")))
        .andExpect(jsonPath("$[6].rows[1].value", is(10)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateService).computeLinkedInExistsStats(any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - linked in links by registration date report succeeds")
  void getAllStatsLinkedInLinksByRegistrationDateReportSucceeds() throws Exception {

    given(candidateService
        .computeLinkedInStats(any(), any(), any()))
        .willReturn(getLinkedInByRegistrationDateStats());

    mockMvc.perform(post(BASE_PATH + ALL_STATS_PATH)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$", hasSize(49)))

        // Linkedin links by registration date
        .andExpect(jsonPath("$[7].name", is("LinkedIn links by candidate registration date")))
        .andExpect(jsonPath("$[7].chartType", is("bar")))
        .andExpect(jsonPath("$[7].rows", notNullValue()))
        .andExpect(jsonPath("$[7].rows", hasSize(3)))
        .andExpect(jsonPath("$[7].rows[0].label", is("2016-06-04")))
        .andExpect(jsonPath("$[7].rows[0].value", is(3)))
        .andExpect(jsonPath("$[7].rows[1].label", is("2016-06-10")))
        .andExpect(jsonPath("$[7].rows[1].value", is(2)))
        .andExpect(jsonPath("$[7].rows[2].label", is("2016-06-14")))
        .andExpect(jsonPath("$[7].rows[2].value", is(1)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateService).computeLinkedInStats(any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - Unhcr registration date report succeeds")
  void getAllStatsUnhcrRegistrationReportSucceeds() throws Exception {

    given(candidateService
        .computeUnhcrRegisteredStats(any(), any(), any()))
        .willReturn(getUnhcrRegistrationStats());

    mockMvc.perform(post(BASE_PATH + ALL_STATS_PATH)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$", hasSize(49)))

        // Unhcr registration date
        .andExpect(jsonPath("$[8].name", is("UNHCR Registered")))
        .andExpect(jsonPath("$[8].chartType", is("bar")))
        .andExpect(jsonPath("$[8].rows", notNullValue()))
        .andExpect(jsonPath("$[8].rows", hasSize(3)))
        .andExpect(jsonPath("$[8].rows[0].label", is("NoResponse")))
        .andExpect(jsonPath("$[8].rows[0].value", is(3)))
        .andExpect(jsonPath("$[8].rows[1].label", is("Yes")))
        .andExpect(jsonPath("$[8].rows[1].value", is(2)))
        .andExpect(jsonPath("$[8].rows[2].label", is("No")))
        .andExpect(jsonPath("$[8].rows[2].value", is(1)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateService).computeUnhcrRegisteredStats(any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - Unhcr status report succeeds")
  void getAllStatsUnhcrStatusReportSucceeds() throws Exception {

    given(candidateService
        .computeUnhcrStatusStats(any(), any(), any()))
        .willReturn(getUnhcrStatusStats());

    mockMvc.perform(post(BASE_PATH + ALL_STATS_PATH)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$", hasSize(49)))

        // Unhcr status
        .andExpect(jsonPath("$[9].name", is("UNHCR Status")))
        .andExpect(jsonPath("$[9].chartType", is("bar")))
        .andExpect(jsonPath("$[9].rows", notNullValue()))
        .andExpect(jsonPath("$[9].rows", hasSize(4)))
        .andExpect(jsonPath("$[9].rows[0].label", is("NoResponse")))
        .andExpect(jsonPath("$[9].rows[0].value", is(4)))
        .andExpect(jsonPath("$[9].rows[1].label", is("RegisteredAsylum")))
        .andExpect(jsonPath("$[9].rows[1].value", is(3)))
        .andExpect(jsonPath("$[9].rows[2].label", is("NotRegistered")))
        .andExpect(jsonPath("$[9].rows[2].value", is(2)))
        .andExpect(jsonPath("$[9].rows[3].label", is("RegisteredStatusUnknown")))
        .andExpect(jsonPath("$[9].rows[3].value", is(1)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateService).computeUnhcrStatusStats(any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - nationalities report succeeds")
  void getAllStatsNationalityReportSucceeds() throws Exception {

    given(candidateService
        .computeNationalityStats(any(), any(), any(), any(), any()))
        .willReturn(getNationalityStats());

    mockMvc.perform(post(BASE_PATH + ALL_STATS_PATH)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$", hasSize(49)))

        // Nationality
        .andExpect(jsonPath("$[10].name", is("Nationalities by Country")))
        .andExpect(jsonPath("$[10].chartType", is("doughnut")))
        .andExpect(jsonPath("$[10].rows", notNullValue()))
        .andExpect(jsonPath("$[10].rows", hasSize(2)))
        .andExpect(jsonPath("$[10].rows[0].label", is("Palestinian Territories")))
        .andExpect(jsonPath("$[10].rows[0].value", is(1073)))
        .andExpect(jsonPath("$[10].rows[1].label", is("Syria")))
        .andExpect(jsonPath("$[10].rows[1].value", is(14852)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateService, times(5)).computeNationalityStats(any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - source countries report succeeds")
  void getAllStatsSourceCountryReportSucceeds() throws Exception {

    given(candidateService
        .computeSourceCountryStats(any(), any(), any(), any()))
        .willReturn(getSourceCountryStats());

    mockMvc.perform(post(BASE_PATH + ALL_STATS_PATH)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$", hasSize(49)))

        // Source Country
        .andExpect(jsonPath("$[15].name", is("Source Countries")))
        .andExpect(jsonPath("$[15].chartType", is("doughnut")))
        .andExpect(jsonPath("$[15].rows", notNullValue()))
        .andExpect(jsonPath("$[15].rows", hasSize(2)))
        .andExpect(jsonPath("$[15].rows[0].label", is("Lebanon")))
        .andExpect(jsonPath("$[15].rows[0].value", is(7231)))
        .andExpect(jsonPath("$[15].rows[1].label", is("Jordan")))
        .andExpect(jsonPath("$[15].rows[1].value", is(4396)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateService, times(3)).computeSourceCountryStats(any(), any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - status report succeeds")
  void getAllStatsStatusReportSucceeds() throws Exception {

    given(candidateService
        .computeStatusStats(any(), any(), any(), any(), any()))
        .willReturn(getStatusStats());

    mockMvc.perform(post(BASE_PATH + ALL_STATS_PATH)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$", hasSize(49)))

        // Statuses
        .andExpect(jsonPath("$[18].name", is("Statuses")))
        .andExpect(jsonPath("$[18].chartType", is("doughnut")))
        .andExpect(jsonPath("$[18].rows", notNullValue()))
        .andExpect(jsonPath("$[18].rows", hasSize(5)))
        .andExpect(jsonPath("$[18].rows[0].label", is("pending")))
        .andExpect(jsonPath("$[18].rows[0].value", is(1000)))
        .andExpect(jsonPath("$[18].rows[1].label", is("incomplete")))
        .andExpect(jsonPath("$[18].rows[1].value", is(2000)))
        .andExpect(jsonPath("$[18].rows[2].label", is("active")))
        .andExpect(jsonPath("$[18].rows[2].value", is(3000)))
        .andExpect(jsonPath("$[18].rows[3].label", is("employed")))
        .andExpect(jsonPath("$[18].rows[3].value", is(4000)))
        .andExpect(jsonPath("$[18].rows[4].label", is("autonomousEmployment")))
        .andExpect(jsonPath("$[18].rows[4].value", is(5000)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateService, times(5)).computeStatusStats(any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - occupations report succeeds")
  void getAllStatsOccupationsReportSucceeds() throws Exception {

    given(candidateService
        .computeOccupationStats(any(), any(), any(), any()))
        .willReturn(getOccupationStats());

    mockMvc.perform(post(BASE_PATH + ALL_STATS_PATH)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$", hasSize(49)))

        // Occupations
        .andExpect(jsonPath("$[23].name", is("Occupations")))
        .andExpect(jsonPath("$[23].chartType", is("doughnut")))
        .andExpect(jsonPath("$[23].rows", notNullValue()))
        .andExpect(jsonPath("$[23].rows", hasSize(3)))
        .andExpect(jsonPath("$[23].rows[0].label", is("undefined")))
        .andExpect(jsonPath("$[23].rows[0].value", is(1000)))
        .andExpect(jsonPath("$[23].rows[1].label", is("Teacher")))
        .andExpect(jsonPath("$[23].rows[1].value", is(2000)))
        .andExpect(jsonPath("$[23].rows[2].label", is("Accountant")))
        .andExpect(jsonPath("$[23].rows[2].value", is(3000)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateService, times(3)).computeOccupationStats(any(), any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - most common occupations report succeeds")
  void getAllStatsMostCommonOccupationsReportSucceeds() throws Exception {

    given(candidateService
        .computeMostCommonOccupationStats(any(), any(), any(), any()))
        .willReturn(getOccupationStats());

    mockMvc.perform(post(BASE_PATH + ALL_STATS_PATH)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$", hasSize(49)))

        // most common occupation
        .andExpect(jsonPath("$[26].name", is("Most Common Occupations")))
        .andExpect(jsonPath("$[26].chartType", is("doughnut")))
        .andExpect(jsonPath("$[26].rows", notNullValue()))
        .andExpect(jsonPath("$[26].rows", hasSize(3)))
        .andExpect(jsonPath("$[26].rows[0].label", is("undefined")))
        .andExpect(jsonPath("$[26].rows[0].value", is(1000)))
        .andExpect(jsonPath("$[26].rows[1].label", is("Teacher")))
        .andExpect(jsonPath("$[26].rows[1].value", is(2000)))
        .andExpect(jsonPath("$[26].rows[2].label", is("Accountant")))
        .andExpect(jsonPath("$[26].rows[2].value", is(3000)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateService, times(3)).computeMostCommonOccupationStats(any(), any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - most max education level report succeeds")
  void getAllStatsMaxEducationLevelReportSucceeds() throws Exception {

    given(candidateService
        .computeMaxEducationStats(any(), any(), any(), any()))
        .willReturn(getMaxEducationStats());

    mockMvc.perform(post(BASE_PATH + ALL_STATS_PATH)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$", hasSize(49)))

        // max education level
        .andExpect(jsonPath("$[29].name", is("Max Education Level")))
        .andExpect(jsonPath("$[29].chartType", is("doughnut")))
        .andExpect(jsonPath("$[29].rows", notNullValue()))
        .andExpect(jsonPath("$[29].rows", hasSize(3)))
        .andExpect(jsonPath("$[29].rows[0].label", is("Bachelor's Degree")))
        .andExpect(jsonPath("$[29].rows[0].value", is(1000)))
        .andExpect(jsonPath("$[29].rows[1].label", is("Primary School")))
        .andExpect(jsonPath("$[29].rows[1].value", is(2000)))
        .andExpect(jsonPath("$[29].rows[2].label", is("Doctoral Degree")))
        .andExpect(jsonPath("$[29].rows[2].value", is(3000)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateService, times(3)).computeMaxEducationStats(any(), any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - languages report succeeds")
  void getAllStatsLanguagesReportSucceeds() throws Exception {

    given(candidateService
        .computeLanguageStats(any(), any(), any(), any()))
        .willReturn(getLanguageStats());

    mockMvc.perform(post(BASE_PATH + ALL_STATS_PATH)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$", hasSize(49)))

        // languages
        .andExpect(jsonPath("$[32].name", is("Languages")))
        .andExpect(jsonPath("$[32].chartType", is("doughnut")))
        .andExpect(jsonPath("$[32].rows", notNullValue()))
        .andExpect(jsonPath("$[32].rows", hasSize(3)))
        .andExpect(jsonPath("$[32].rows[0].label", is("English")))
        .andExpect(jsonPath("$[32].rows[0].value", is(1000)))
        .andExpect(jsonPath("$[32].rows[1].label", is("Arabic")))
        .andExpect(jsonPath("$[32].rows[1].value", is(2000)))
        .andExpect(jsonPath("$[32].rows[2].label", is("French")))
        .andExpect(jsonPath("$[32].rows[2].value", is(3000)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateService, times(3)).computeLanguageStats(any(), any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - referrers report succeeds")
  void getAllStatsReferrersReportSucceeds() throws Exception {

    given(candidateService
        .computeReferrerStats(any(), any(), any(), any(), any()))
        .willReturn(getReferrerStats());

    mockMvc.perform(post(BASE_PATH + ALL_STATS_PATH)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$", hasSize(49)))

        // referrers
        .andExpect(jsonPath("$[35].name", is("Referrers")))
        .andExpect(jsonPath("$[35].chartType", is("bar")))
        .andExpect(jsonPath("$[35].rows", notNullValue()))
        .andExpect(jsonPath("$[35].rows", hasSize(2)))
        .andExpect(jsonPath("$[35].rows[0].label", is("auntie rene")))
        .andExpect(jsonPath("$[35].rows[0].value", is(1000)))
        .andExpect(jsonPath("$[35].rows[1].label", is("uncle fred")))
        .andExpect(jsonPath("$[35].rows[1].value", is(2000)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateService, times(3)).computeReferrerStats(any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - survey report succeeds")
  void getAllStatsSurveyReportSucceeds() throws Exception {

    given(candidateService
        .computeSurveyStats(any(), any(), any(), any(), any()))
        .willReturn(getSurveyStats());

    mockMvc.perform(post(BASE_PATH + ALL_STATS_PATH)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$", hasSize(49)))

        // survey
        .andExpect(jsonPath("$[38].name", is("Survey")))
        .andExpect(jsonPath("$[38].chartType", is("doughnut")))
        .andExpect(jsonPath("$[38].rows", notNullValue()))
        .andExpect(jsonPath("$[38].rows", hasSize(3)))
        .andExpect(jsonPath("$[38].rows[0].label", is("Facebook")))
        .andExpect(jsonPath("$[38].rows[0].value", is(1000)))
        .andExpect(jsonPath("$[38].rows[1].label", is("From a friend")))
        .andExpect(jsonPath("$[38].rows[1].value", is(2000)))
        .andExpect(jsonPath("$[38].rows[2].label", is("NGO")))
        .andExpect(jsonPath("$[38].rows[2].value", is(3000)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateService, times(5)).computeSurveyStats(any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - spoken language report succeeds")
  void getAllStatsSpokenLanguageReportSucceeds() throws Exception {

    given(candidateService
        .computeSpokenLanguageLevelStats(any(), any(), any(), any(), any()))
        .willReturn(getSpokenLanguageLevelStats());

    mockMvc.perform(post(BASE_PATH + ALL_STATS_PATH)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$", hasSize(49)))

        // spoken language
        .andExpect(jsonPath("$[43].name", is("Spoken English Language Level")))
        .andExpect(jsonPath("$[43].chartType", is("doughnut")))
        .andExpect(jsonPath("$[43].rows", notNullValue()))
        .andExpect(jsonPath("$[43].rows", hasSize(3)))
        .andExpect(jsonPath("$[43].rows[0].label", is("Intermediate Proficiency")))
        .andExpect(jsonPath("$[43].rows[0].value", is(1000)))
        .andExpect(jsonPath("$[43].rows[1].label", is("Full Professional Proficiency")))
        .andExpect(jsonPath("$[43].rows[1].value", is(2000)))
        .andExpect(jsonPath("$[43].rows[2].label", is("Elementary Proficiency")))
        .andExpect(jsonPath("$[43].rows[2].value", is(3000)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateService, times(6)).computeSpokenLanguageLevelStats(any(), any(), any(), any(), any());
  }

}
