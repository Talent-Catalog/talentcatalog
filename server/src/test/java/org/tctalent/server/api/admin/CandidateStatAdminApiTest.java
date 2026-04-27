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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
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
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
import org.tctalent.server.model.db.Gender;
import org.tctalent.server.model.db.Stat;
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

    request.setSelectedStats(List.of(Stat.gender));

    given(candidateStatsService
        .computeGenderStats(any(), any(), any(), any(), any()))
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
        .andExpect(jsonPath("$", hasSize(1)))

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
    verify(candidateStatsService).computeGenderStats(any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - registration report succeeds")
  void getAllStatsRegistrationReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.registrations));

    given(candidateStatsService
        .computeRegistrationStats(any(), any(), any(), any(), any()))
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
        .andExpect(jsonPath("$", hasSize(1)))

        // Registrations
        .andExpect(jsonPath("$[0].name", is("Registrations")))
        .andExpect(jsonPath("$[0].chartType", is("bar")))
        .andExpect(jsonPath("$[0].rows", notNullValue()))
        .andExpect(jsonPath("$[0].rows", hasSize(3)))
        .andExpect(jsonPath("$[0].rows[0].label", is("2016-06-04")))
        .andExpect(jsonPath("$[0].rows[0].value", is(3)))
        .andExpect(jsonPath("$[0].rows[1].label", is("2016-06-10")))
        .andExpect(jsonPath("$[0].rows[1].value", is(2)))
        .andExpect(jsonPath("$[0].rows[2].label", is("2016-06-14")))
        .andExpect(jsonPath("$[0].rows[2].value", is(1)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeRegistrationStats(any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - registration by occupation report succeeds")
  void getAllStatsRegistrationByOccupationReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.registrationsOccupations));

    given(candidateStatsService
        .computeRegistrationOccupationStats(any(), any(), any(), any(), any()))
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
        .andExpect(jsonPath("$", hasSize(1)))

        // Registrations by Occupation
        .andExpect(jsonPath("$[0].name", is("Registrations (by Occupation)")))
        .andExpect(jsonPath("$[0].chartType", is("doughnut")))
        .andExpect(jsonPath("$[0].rows", notNullValue()))
        .andExpect(jsonPath("$[0].rows", hasSize(3)))
        .andExpect(jsonPath("$[0].rows[0].label", is("undefined")))
        .andExpect(jsonPath("$[0].rows[0].value", is(11414)))
        .andExpect(jsonPath("$[0].rows[1].label", is("Unknown")))
        .andExpect(jsonPath("$[0].rows[1].value", is(1652)))
        .andExpect(jsonPath("$[0].rows[2].label", is("Teacher")))
        .andExpect(jsonPath("$[0].rows[2].value", is(777)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeRegistrationOccupationStats(any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get stats - birth year report succeeds")
  void getAllStatsBirthYearReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.birthYears));

    given(candidateStatsService
        .computeBirthYearStats(any(), any(), any(), any(), any(), any()))
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
        .andExpect(jsonPath("$", hasSize(1)))

        // Birth year
        .andExpect(jsonPath("$[0].name", is("Birth Year")))
        .andExpect(jsonPath("$[0].chartType", is("bar")))
        .andExpect(jsonPath("$[0].rows", notNullValue()))
        .andExpect(jsonPath("$[0].rows", hasSize(3)))
        .andExpect(jsonPath("$[0].rows[0].label", is("1948")))
        .andExpect(jsonPath("$[0].rows[0].value", is(3)))
        .andExpect(jsonPath("$[0].rows[1].label", is("1950")))
        .andExpect(jsonPath("$[0].rows[1].value", is(2)))
        .andExpect(jsonPath("$[0].rows[2].label", is("1951")))
        .andExpect(jsonPath("$[0].rows[2].value", is(1)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeBirthYearStats(any(), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get stats - birth year male report succeeds")
  void getAllStatsBirthYearMaleReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.birthYearsMale));

    given(candidateStatsService
            .computeBirthYearStats(eq(Gender.male), any(), any(), any(), any(), any()))
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
            .andExpect(jsonPath("$", hasSize(1)))

            // Birth year
            .andExpect(jsonPath("$[0].name", is("Birth Year (Male)")))
            .andExpect(jsonPath("$[0].chartType", is("bar")))
            .andExpect(jsonPath("$[0].rows", notNullValue()))
            .andExpect(jsonPath("$[0].rows", hasSize(3)))
            .andExpect(jsonPath("$[0].rows[0].label", is("1948")))
            .andExpect(jsonPath("$[0].rows[0].value", is(3)))
            .andExpect(jsonPath("$[0].rows[1].label", is("1950")))
            .andExpect(jsonPath("$[0].rows[1].value", is(2)))
            .andExpect(jsonPath("$[0].rows[2].label", is("1951")))
            .andExpect(jsonPath("$[0].rows[2].value", is(1)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeBirthYearStats(eq(Gender.male), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get stats - birth year female report succeeds")
  void getAllStatsBirthYearFemaleReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.birthYearsFemale));

    given(candidateStatsService
            .computeBirthYearStats(eq(Gender.female), any(), any(), any(), any(), any()))
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
            .andExpect(jsonPath("$", hasSize(1)))

            // Birth year
            .andExpect(jsonPath("$[0].name", is("Birth Year (Female)")))
            .andExpect(jsonPath("$[0].chartType", is("bar")))
            .andExpect(jsonPath("$[0].rows", notNullValue()))
            .andExpect(jsonPath("$[0].rows", hasSize(3)))
            .andExpect(jsonPath("$[0].rows[0].label", is("1948")))
            .andExpect(jsonPath("$[0].rows[0].value", is(3)))
            .andExpect(jsonPath("$[0].rows[1].label", is("1950")))
            .andExpect(jsonPath("$[0].rows[1].value", is(2)))
            .andExpect(jsonPath("$[0].rows[2].label", is("1951")))
            .andExpect(jsonPath("$[0].rows[2].value", is(1)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeBirthYearStats(eq(Gender.female), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - linked in links report succeeds")
  void getAllStatsLinkedInLinksReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.linkedin));

    given(candidateStatsService
        .computeLinkedInExistsStats(any(), any(), any(), any(), any()))
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
        .andExpect(jsonPath("$", hasSize(1)))

        // Linkedin links
        .andExpect(jsonPath("$[0].name", is("LinkedIn Links")))
        .andExpect(jsonPath("$[0].chartType", is("bar")))
        .andExpect(jsonPath("$[0].rows", notNullValue()))
        .andExpect(jsonPath("$[0].rows", hasSize(2)))
        .andExpect(jsonPath("$[0].rows[0].label", is("No link")))
        .andExpect(jsonPath("$[0].rows[0].value", is(2)))
        .andExpect(jsonPath("$[0].rows[1].label", is("Has link")))
        .andExpect(jsonPath("$[0].rows[1].value", is(10)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeLinkedInExistsStats(any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - linked in links by registration date report succeeds")
  void getAllStatsLinkedInLinksByRegistrationDateReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.linkedinRegistration));

    given(candidateStatsService
        .computeLinkedInStats(any(), any(), any(), any(), any()))
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
        .andExpect(jsonPath("$", hasSize(1)))

        // Linkedin links by registration date
        .andExpect(jsonPath("$[0].name", is("LinkedIn Links by Candidate Registration Date")))
        .andExpect(jsonPath("$[0].chartType", is("bar")))
        .andExpect(jsonPath("$[0].rows", notNullValue()))
        .andExpect(jsonPath("$[0].rows", hasSize(3)))
        .andExpect(jsonPath("$[0].rows[0].label", is("2016-06-04")))
        .andExpect(jsonPath("$[0].rows[0].value", is(3)))
        .andExpect(jsonPath("$[0].rows[1].label", is("2016-06-10")))
        .andExpect(jsonPath("$[0].rows[1].value", is(2)))
        .andExpect(jsonPath("$[0].rows[2].label", is("2016-06-14")))
        .andExpect(jsonPath("$[0].rows[2].value", is(1)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeLinkedInStats(any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get stats - Unhcr registration date report succeeds")
  void getAllStatsUnhcrRegistrationReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.unhcrRegistered));

    given(candidateStatsService
        .computeUnhcrRegisteredStats(any(), any(), any(), any(), any()))
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
        .andExpect(jsonPath("$", hasSize(1)))

        // Unhcr registration date
        .andExpect(jsonPath("$[0].name", is("UNHCR Registered")))
        .andExpect(jsonPath("$[0].chartType", is("bar")))
        .andExpect(jsonPath("$[0].rows", notNullValue()))
        .andExpect(jsonPath("$[0].rows", hasSize(3)))
        .andExpect(jsonPath("$[0].rows[0].label", is("NoResponse")))
        .andExpect(jsonPath("$[0].rows[0].value", is(3)))
        .andExpect(jsonPath("$[0].rows[1].label", is("Yes")))
        .andExpect(jsonPath("$[0].rows[1].value", is(2)))
        .andExpect(jsonPath("$[0].rows[2].label", is("No")))
        .andExpect(jsonPath("$[0].rows[2].value", is(1)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeUnhcrRegisteredStats(any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - Unhcr status report succeeds")
  void getAllStatsUnhcrStatusReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.unhcrStatus));

    given(candidateStatsService
        .computeUnhcrStatusStats(any(), any(), any(), any(), any()))
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
        .andExpect(jsonPath("$", hasSize(1)))

        // Unhcr status
        .andExpect(jsonPath("$[0].name", is("UNHCR Status")))
        .andExpect(jsonPath("$[0].chartType", is("bar")))
        .andExpect(jsonPath("$[0].rows", notNullValue()))
        .andExpect(jsonPath("$[0].rows", hasSize(4)))
        .andExpect(jsonPath("$[0].rows[0].label", is("NoResponse")))
        .andExpect(jsonPath("$[0].rows[0].value", is(4)))
        .andExpect(jsonPath("$[0].rows[1].label", is("RegisteredAsylum")))
        .andExpect(jsonPath("$[0].rows[1].value", is(3)))
        .andExpect(jsonPath("$[0].rows[2].label", is("NotRegistered")))
        .andExpect(jsonPath("$[0].rows[2].value", is(2)))
        .andExpect(jsonPath("$[0].rows[3].label", is("RegisteredStatusUnknown")))
        .andExpect(jsonPath("$[0].rows[3].value", is(1)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeUnhcrStatusStats(any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - nationalities report succeeds")
  void getAllStatsNationalityReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.nationalities));

    given(candidateStatsService
        .computeNationalityStats(any(), any(), any(), any(), any(), any(), any()))
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
        .andExpect(jsonPath("$", hasSize(1)))

        // Nationality
        .andExpect(jsonPath("$[0].name", is("Nationalities")))
        .andExpect(jsonPath("$[0].chartType", is("doughnut")))
        .andExpect(jsonPath("$[0].rows", notNullValue()))
        .andExpect(jsonPath("$[0].rows", hasSize(2)))
        .andExpect(jsonPath("$[0].rows[0].label", is("Palestinian Territories")))
        .andExpect(jsonPath("$[0].rows[0].value", is(1073)))
        .andExpect(jsonPath("$[0].rows[1].label", is("Syria")))
        .andExpect(jsonPath("$[0].rows[1].value", is(14852)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeNationalityStats(any(), any(), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - nationalities male report succeeds")
  void getAllStatsNationalityMaleReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.nationalitiesMale));

    given(candidateStatsService
            .computeNationalityStats(eq(Gender.male), any(), any(), any(), any(), any(), any()))
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
            .andExpect(jsonPath("$", hasSize(1)))

            // Nationality
            .andExpect(jsonPath("$[0].name", is("Nationalities (Male)")))
            .andExpect(jsonPath("$[0].chartType", is("doughnut")))
            .andExpect(jsonPath("$[0].rows", notNullValue()))
            .andExpect(jsonPath("$[0].rows", hasSize(2)))
            .andExpect(jsonPath("$[0].rows[0].label", is("Palestinian Territories")))
            .andExpect(jsonPath("$[0].rows[0].value", is(1073)))
            .andExpect(jsonPath("$[0].rows[1].label", is("Syria")))
            .andExpect(jsonPath("$[0].rows[1].value", is(14852)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeNationalityStats(eq(Gender.male), any(), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - nationalities female report succeeds")
  void getAllStatsNationalityFemaleReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.nationalitiesFemale));

    given(candidateStatsService
            .computeNationalityStats(eq(Gender.female), any(), any(), any(), any(), any(), any()))
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
            .andExpect(jsonPath("$", hasSize(1)))

            // Nationality
            .andExpect(jsonPath("$[0].name", is("Nationalities (Female)")))
            .andExpect(jsonPath("$[0].chartType", is("doughnut")))
            .andExpect(jsonPath("$[0].rows", notNullValue()))
            .andExpect(jsonPath("$[0].rows", hasSize(2)))
            .andExpect(jsonPath("$[0].rows[0].label", is("Palestinian Territories")))
            .andExpect(jsonPath("$[0].rows[0].value", is(1073)))
            .andExpect(jsonPath("$[0].rows[1].label", is("Syria")))
            .andExpect(jsonPath("$[0].rows[1].value", is(14852)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeNationalityStats(eq(Gender.female), any(), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - nationalities jordan report succeeds")
  void getAllStatsNationalityJordanReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.nationalitiesJordan));

    given(candidateStatsService
            .computeNationalityStats(any(), eq("jordan"), any(), any(), any(), any(), any()))
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
            .andExpect(jsonPath("$", hasSize(1)))

            // Nationality
            .andExpect(jsonPath("$[0].name", is("Nationalities (Jordan)")))
            .andExpect(jsonPath("$[0].chartType", is("doughnut")))
            .andExpect(jsonPath("$[0].rows", notNullValue()))
            .andExpect(jsonPath("$[0].rows", hasSize(2)))
            .andExpect(jsonPath("$[0].rows[0].label", is("Palestinian Territories")))
            .andExpect(jsonPath("$[0].rows[0].value", is(1073)))
            .andExpect(jsonPath("$[0].rows[1].label", is("Syria")))
            .andExpect(jsonPath("$[0].rows[1].value", is(14852)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeNationalityStats(any(), eq("jordan"), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - nationalities lebanon report succeeds")
  void getAllStatsNationalityLebanonReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.nationalitiesLebanon));

    given(candidateStatsService
            .computeNationalityStats(any(), eq("lebanon"), any(), any(), any(), any(), any()))
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
            .andExpect(jsonPath("$", hasSize(1)))

            // Nationality
            .andExpect(jsonPath("$[0].name", is("Nationalities (Lebanon)")))
            .andExpect(jsonPath("$[0].chartType", is("doughnut")))
            .andExpect(jsonPath("$[0].rows", notNullValue()))
            .andExpect(jsonPath("$[0].rows", hasSize(2)))
            .andExpect(jsonPath("$[0].rows[0].label", is("Palestinian Territories")))
            .andExpect(jsonPath("$[0].rows[0].value", is(1073)))
            .andExpect(jsonPath("$[0].rows[1].label", is("Syria")))
            .andExpect(jsonPath("$[0].rows[1].value", is(14852)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeNationalityStats(any(), eq("lebanon"), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - source countries report succeeds")
  void getAllStatsSourceCountryReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.sourceCountries));

    given(candidateStatsService
        .computeSourceCountryStats(any(), any(), any(), any(), any(), any()))
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
        .andExpect(jsonPath("$", hasSize(1)))

        // Source Country
        .andExpect(jsonPath("$[0].name", is("Source Countries")))
        .andExpect(jsonPath("$[0].chartType", is("doughnut")))
        .andExpect(jsonPath("$[0].rows", notNullValue()))
        .andExpect(jsonPath("$[0].rows", hasSize(2)))
        .andExpect(jsonPath("$[0].rows[0].label", is("Lebanon")))
        .andExpect(jsonPath("$[0].rows[0].value", is(7231)))
        .andExpect(jsonPath("$[0].rows[1].label", is("Jordan")))
        .andExpect(jsonPath("$[0].rows[1].value", is(4396)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeSourceCountryStats(any(), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - source countries male report succeeds")
  void getAllStatsSourceCountryMaleReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.sourceCountriesMale));

    given(candidateStatsService
            .computeSourceCountryStats(eq(Gender.male), any(), any(), any(), any(), any()))
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
            .andExpect(jsonPath("$", hasSize(1)))

            // Source Country
            .andExpect(jsonPath("$[0].name", is("Source Countries (Male)")))
            .andExpect(jsonPath("$[0].chartType", is("doughnut")))
            .andExpect(jsonPath("$[0].rows", notNullValue()))
            .andExpect(jsonPath("$[0].rows", hasSize(2)))
            .andExpect(jsonPath("$[0].rows[0].label", is("Lebanon")))
            .andExpect(jsonPath("$[0].rows[0].value", is(7231)))
            .andExpect(jsonPath("$[0].rows[1].label", is("Jordan")))
            .andExpect(jsonPath("$[0].rows[1].value", is(4396)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeSourceCountryStats(eq(Gender.male), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - source countries female report succeeds")
  void getAllStatsSourceCountryFemaleReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.sourceCountriesFemale));

    given(candidateStatsService
            .computeSourceCountryStats(eq(Gender.female), any(), any(), any(), any(), any()))
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
            .andExpect(jsonPath("$", hasSize(1)))

            // Source Country
            .andExpect(jsonPath("$[0].name", is("Source Countries (Female)")))
            .andExpect(jsonPath("$[0].chartType", is("doughnut")))
            .andExpect(jsonPath("$[0].rows", notNullValue()))
            .andExpect(jsonPath("$[0].rows", hasSize(2)))
            .andExpect(jsonPath("$[0].rows[0].label", is("Lebanon")))
            .andExpect(jsonPath("$[0].rows[0].value", is(7231)))
            .andExpect(jsonPath("$[0].rows[1].label", is("Jordan")))
            .andExpect(jsonPath("$[0].rows[1].value", is(4396)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeSourceCountryStats(eq(Gender.female), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get stats - status report succeeds")
  void getAllStatsStatusReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.statuses));

    given(candidateStatsService
        .computeStatusStats(any(), any(), any(), any(), any(), any(), any()))
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
        .andExpect(jsonPath("$", hasSize(1)))

        // Statuses
        .andExpect(jsonPath("$[0].name", is("Statuses")))
        .andExpect(jsonPath("$[0].chartType", is("doughnut")))
        .andExpect(jsonPath("$[0].rows", notNullValue()))
        .andExpect(jsonPath("$[0].rows", hasSize(5)))
        .andExpect(jsonPath("$[0].rows[0].label", is("pending")))
        .andExpect(jsonPath("$[0].rows[0].value", is(1000)))
        .andExpect(jsonPath("$[0].rows[1].label", is("incomplete")))
        .andExpect(jsonPath("$[0].rows[1].value", is(2000)))
        .andExpect(jsonPath("$[0].rows[2].label", is("active")))
        .andExpect(jsonPath("$[0].rows[2].value", is(3000)))
        .andExpect(jsonPath("$[0].rows[3].label", is("employed")))
        .andExpect(jsonPath("$[0].rows[3].value", is(4000)))
        .andExpect(jsonPath("$[0].rows[4].label", is("autonomousEmployment")))
        .andExpect(jsonPath("$[0].rows[4].value", is(5000)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeStatusStats(any(), any(), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get stats - status male report succeeds")
  void getAllStatsStatusMaleReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.statusesMale));

    given(candidateStatsService
            .computeStatusStats(eq(Gender.male), any(), any(), any(), any(), any(), any()))
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
            .andExpect(jsonPath("$", hasSize(1)))

            // Statuses
            .andExpect(jsonPath("$[0].name", is("Statuses (Male)")))
            .andExpect(jsonPath("$[0].chartType", is("doughnut")))
            .andExpect(jsonPath("$[0].rows", notNullValue()))
            .andExpect(jsonPath("$[0].rows", hasSize(5)))
            .andExpect(jsonPath("$[0].rows[0].label", is("pending")))
            .andExpect(jsonPath("$[0].rows[0].value", is(1000)))
            .andExpect(jsonPath("$[0].rows[1].label", is("incomplete")))
            .andExpect(jsonPath("$[0].rows[1].value", is(2000)))
            .andExpect(jsonPath("$[0].rows[2].label", is("active")))
            .andExpect(jsonPath("$[0].rows[2].value", is(3000)))
            .andExpect(jsonPath("$[0].rows[3].label", is("employed")))
            .andExpect(jsonPath("$[0].rows[3].value", is(4000)))
            .andExpect(jsonPath("$[0].rows[4].label", is("autonomousEmployment")))
            .andExpect(jsonPath("$[0].rows[4].value", is(5000)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeStatusStats(eq(Gender.male), any(), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get stats - status female report succeeds")
  void getAllStatsStatusFemaleReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.statusesFemale));

    given(candidateStatsService
            .computeStatusStats(eq(Gender.female), any(), any(), any(), any(), any(), any()))
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
            .andExpect(jsonPath("$", hasSize(1)))

            // Statuses
            .andExpect(jsonPath("$[0].name", is("Statuses (Female)")))
            .andExpect(jsonPath("$[0].chartType", is("doughnut")))
            .andExpect(jsonPath("$[0].rows", notNullValue()))
            .andExpect(jsonPath("$[0].rows", hasSize(5)))
            .andExpect(jsonPath("$[0].rows[0].label", is("pending")))
            .andExpect(jsonPath("$[0].rows[0].value", is(1000)))
            .andExpect(jsonPath("$[0].rows[1].label", is("incomplete")))
            .andExpect(jsonPath("$[0].rows[1].value", is(2000)))
            .andExpect(jsonPath("$[0].rows[2].label", is("active")))
            .andExpect(jsonPath("$[0].rows[2].value", is(3000)))
            .andExpect(jsonPath("$[0].rows[3].label", is("employed")))
            .andExpect(jsonPath("$[0].rows[3].value", is(4000)))
            .andExpect(jsonPath("$[0].rows[4].label", is("autonomousEmployment")))
            .andExpect(jsonPath("$[0].rows[4].value", is(5000)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeStatusStats(eq(Gender.female), any(), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get stats - status jordan report succeeds")
  void getAllStatsStatusJordanReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.statusesJordan));

    given(candidateStatsService
            .computeStatusStats(any(), eq("jordan"), any(), any(), any(), any(), any()))
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
            .andExpect(jsonPath("$", hasSize(1)))

            // Statuses
            .andExpect(jsonPath("$[0].name", is("Statuses (Jordan)")))
            .andExpect(jsonPath("$[0].chartType", is("doughnut")))
            .andExpect(jsonPath("$[0].rows", notNullValue()))
            .andExpect(jsonPath("$[0].rows", hasSize(5)))
            .andExpect(jsonPath("$[0].rows[0].label", is("pending")))
            .andExpect(jsonPath("$[0].rows[0].value", is(1000)))
            .andExpect(jsonPath("$[0].rows[1].label", is("incomplete")))
            .andExpect(jsonPath("$[0].rows[1].value", is(2000)))
            .andExpect(jsonPath("$[0].rows[2].label", is("active")))
            .andExpect(jsonPath("$[0].rows[2].value", is(3000)))
            .andExpect(jsonPath("$[0].rows[3].label", is("employed")))
            .andExpect(jsonPath("$[0].rows[3].value", is(4000)))
            .andExpect(jsonPath("$[0].rows[4].label", is("autonomousEmployment")))
            .andExpect(jsonPath("$[0].rows[4].value", is(5000)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeStatusStats(any(), eq("jordan"), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get stats - status lebanon report succeeds")
  void getAllStatsStatusLebanonReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.statusesLebanon));

    given(candidateStatsService
            .computeStatusStats(any(), eq("lebanon"), any(), any(), any(), any(), any()))
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
            .andExpect(jsonPath("$", hasSize(1)))

            // Statuses
            .andExpect(jsonPath("$[0].name", is("Statuses (Lebanon)")))
            .andExpect(jsonPath("$[0].chartType", is("doughnut")))
            .andExpect(jsonPath("$[0].rows", notNullValue()))
            .andExpect(jsonPath("$[0].rows", hasSize(5)))
            .andExpect(jsonPath("$[0].rows[0].label", is("pending")))
            .andExpect(jsonPath("$[0].rows[0].value", is(1000)))
            .andExpect(jsonPath("$[0].rows[1].label", is("incomplete")))
            .andExpect(jsonPath("$[0].rows[1].value", is(2000)))
            .andExpect(jsonPath("$[0].rows[2].label", is("active")))
            .andExpect(jsonPath("$[0].rows[2].value", is(3000)))
            .andExpect(jsonPath("$[0].rows[3].label", is("employed")))
            .andExpect(jsonPath("$[0].rows[3].value", is(4000)))
            .andExpect(jsonPath("$[0].rows[4].label", is("autonomousEmployment")))
            .andExpect(jsonPath("$[0].rows[4].value", is(5000)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeStatusStats(any(), eq("lebanon"), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get stats - occupations report succeeds")
  void getAllStatsOccupationsReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.occupations));

    given(candidateStatsService
        .computeOccupationStats(any(), any(), any(), any(), any(), any()))
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
        .andExpect(jsonPath("$", hasSize(1)))

        // Occupations
        .andExpect(jsonPath("$[0].name", is("Occupations")))
        .andExpect(jsonPath("$[0].chartType", is("doughnut")))
        .andExpect(jsonPath("$[0].rows", notNullValue()))
        .andExpect(jsonPath("$[0].rows", hasSize(3)))
        .andExpect(jsonPath("$[0].rows[0].label", is("undefined")))
        .andExpect(jsonPath("$[0].rows[0].value", is(1000)))
        .andExpect(jsonPath("$[0].rows[1].label", is("Teacher")))
        .andExpect(jsonPath("$[0].rows[1].value", is(2000)))
        .andExpect(jsonPath("$[0].rows[2].label", is("Accountant")))
        .andExpect(jsonPath("$[0].rows[2].value", is(3000)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeOccupationStats(any(), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get stats - occupations male report succeeds")
  void getAllStatsOccupationsMaleReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.occupationsMale));

    given(candidateStatsService
            .computeOccupationStats(eq(Gender.male), any(), any(), any(), any(), any()))
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
            .andExpect(jsonPath("$", hasSize(1)))

            // Occupations
            .andExpect(jsonPath("$[0].name", is("Occupations (Male)")))
            .andExpect(jsonPath("$[0].chartType", is("doughnut")))
            .andExpect(jsonPath("$[0].rows", notNullValue()))
            .andExpect(jsonPath("$[0].rows", hasSize(3)))
            .andExpect(jsonPath("$[0].rows[0].label", is("undefined")))
            .andExpect(jsonPath("$[0].rows[0].value", is(1000)))
            .andExpect(jsonPath("$[0].rows[1].label", is("Teacher")))
            .andExpect(jsonPath("$[0].rows[1].value", is(2000)))
            .andExpect(jsonPath("$[0].rows[2].label", is("Accountant")))
            .andExpect(jsonPath("$[0].rows[2].value", is(3000)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeOccupationStats(eq(Gender.male), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get stats - occupations female report succeeds")
  void getAllStatsOccupationsFemaleReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.occupationsFemale));

    given(candidateStatsService
            .computeOccupationStats(eq(Gender.female), any(), any(), any(), any(), any()))
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
            .andExpect(jsonPath("$", hasSize(1)))

            // Occupations
            .andExpect(jsonPath("$[0].name", is("Occupations (Female)")))
            .andExpect(jsonPath("$[0].chartType", is("doughnut")))
            .andExpect(jsonPath("$[0].rows", notNullValue()))
            .andExpect(jsonPath("$[0].rows", hasSize(3)))
            .andExpect(jsonPath("$[0].rows[0].label", is("undefined")))
            .andExpect(jsonPath("$[0].rows[0].value", is(1000)))
            .andExpect(jsonPath("$[0].rows[1].label", is("Teacher")))
            .andExpect(jsonPath("$[0].rows[1].value", is(2000)))
            .andExpect(jsonPath("$[0].rows[2].label", is("Accountant")))
            .andExpect(jsonPath("$[0].rows[2].value", is(3000)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeOccupationStats(eq(Gender.female), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - most common occupations report succeeds")
  void getAllStatsMostCommonOccupationsReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.occupationsCommon));

    given(candidateStatsService
        .computeMostCommonOccupationStats(any(), any(), any(), any(), any(), any()))
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
        .andExpect(jsonPath("$", hasSize(1)))

        // most common occupation
        .andExpect(jsonPath("$[0].name", is("Most Common Occupations")))
        .andExpect(jsonPath("$[0].chartType", is("doughnut")))
        .andExpect(jsonPath("$[0].rows", notNullValue()))
        .andExpect(jsonPath("$[0].rows", hasSize(3)))
        .andExpect(jsonPath("$[0].rows[0].label", is("undefined")))
        .andExpect(jsonPath("$[0].rows[0].value", is(1000)))
        .andExpect(jsonPath("$[0].rows[1].label", is("Teacher")))
        .andExpect(jsonPath("$[0].rows[1].value", is(2000)))
        .andExpect(jsonPath("$[0].rows[2].label", is("Accountant")))
        .andExpect(jsonPath("$[0].rows[2].value", is(3000)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeMostCommonOccupationStats(any(), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - most common occupations male report succeeds")
  void getAllStatsMostCommonOccupationsMaleReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.occupationsCommonMale));

    given(candidateStatsService
            .computeMostCommonOccupationStats(eq(Gender.male), any(), any(), any(), any(), any()))
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
            .andExpect(jsonPath("$", hasSize(1)))

            // most common occupation
            .andExpect(jsonPath("$[0].name", is("Most Common Occupations (Male)")))
            .andExpect(jsonPath("$[0].chartType", is("doughnut")))
            .andExpect(jsonPath("$[0].rows", notNullValue()))
            .andExpect(jsonPath("$[0].rows", hasSize(3)))
            .andExpect(jsonPath("$[0].rows[0].label", is("undefined")))
            .andExpect(jsonPath("$[0].rows[0].value", is(1000)))
            .andExpect(jsonPath("$[0].rows[1].label", is("Teacher")))
            .andExpect(jsonPath("$[0].rows[1].value", is(2000)))
            .andExpect(jsonPath("$[0].rows[2].label", is("Accountant")))
            .andExpect(jsonPath("$[0].rows[2].value", is(3000)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeMostCommonOccupationStats(eq(Gender.male), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - most common occupations female report succeeds")
  void getAllStatsMostCommonOccupationsFemaleReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.occupationsCommonFemale));

    given(candidateStatsService
            .computeMostCommonOccupationStats(eq(Gender.female), any(), any(), any(), any(), any()))
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
            .andExpect(jsonPath("$", hasSize(1)))

            // most common occupation
            .andExpect(jsonPath("$[0].name", is("Most Common Occupations (Female)")))
            .andExpect(jsonPath("$[0].chartType", is("doughnut")))
            .andExpect(jsonPath("$[0].rows", notNullValue()))
            .andExpect(jsonPath("$[0].rows", hasSize(3)))
            .andExpect(jsonPath("$[0].rows[0].label", is("undefined")))
            .andExpect(jsonPath("$[0].rows[0].value", is(1000)))
            .andExpect(jsonPath("$[0].rows[1].label", is("Teacher")))
            .andExpect(jsonPath("$[0].rows[1].value", is(2000)))
            .andExpect(jsonPath("$[0].rows[2].label", is("Accountant")))
            .andExpect(jsonPath("$[0].rows[2].value", is(3000)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeMostCommonOccupationStats(eq(Gender.female), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get stats - most max education level report succeeds")
  void getAllStatsMaxEducationLevelReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.maxEducation));

    given(candidateStatsService
        .computeMaxEducationStats(any(), any(), any(), any(), any(), any()))
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
        .andExpect(jsonPath("$", hasSize(1)))

        // max education level
        .andExpect(jsonPath("$[0].name", is("Max Education Level")))
        .andExpect(jsonPath("$[0].chartType", is("doughnut")))
        .andExpect(jsonPath("$[0].rows", notNullValue()))
        .andExpect(jsonPath("$[0].rows", hasSize(3)))
        .andExpect(jsonPath("$[0].rows[0].label", is("Bachelor's Degree")))
        .andExpect(jsonPath("$[0].rows[0].value", is(1000)))
        .andExpect(jsonPath("$[0].rows[1].label", is("Primary School")))
        .andExpect(jsonPath("$[0].rows[1].value", is(2000)))
        .andExpect(jsonPath("$[0].rows[2].label", is("Doctoral Degree")))
        .andExpect(jsonPath("$[0].rows[2].value", is(3000)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeMaxEducationStats(any(), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get stats - most max education level male report succeeds")
  void getAllStatsMaxEducationLevelMaleReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.maxEducationMale));

    given(candidateStatsService
            .computeMaxEducationStats(eq(Gender.male), any(), any(), any(), any(), any()))
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
            .andExpect(jsonPath("$", hasSize(1)))

            // max education level
            .andExpect(jsonPath("$[0].name", is("Max Education Level (Male)")))
            .andExpect(jsonPath("$[0].chartType", is("doughnut")))
            .andExpect(jsonPath("$[0].rows", notNullValue()))
            .andExpect(jsonPath("$[0].rows", hasSize(3)))
            .andExpect(jsonPath("$[0].rows[0].label", is("Bachelor's Degree")))
            .andExpect(jsonPath("$[0].rows[0].value", is(1000)))
            .andExpect(jsonPath("$[0].rows[1].label", is("Primary School")))
            .andExpect(jsonPath("$[0].rows[1].value", is(2000)))
            .andExpect(jsonPath("$[0].rows[2].label", is("Doctoral Degree")))
            .andExpect(jsonPath("$[0].rows[2].value", is(3000)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeMaxEducationStats(eq(Gender.male), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get stats - most max education level female report succeeds")
  void getAllStatsMaxEducationLevelFemaleReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.maxEducationFemale));

    given(candidateStatsService
            .computeMaxEducationStats(eq(Gender.female), any(), any(), any(), any(), any()))
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
            .andExpect(jsonPath("$", hasSize(1)))

            // max education level
            .andExpect(jsonPath("$[0].name", is("Max Education Level (Female)")))
            .andExpect(jsonPath("$[0].chartType", is("doughnut")))
            .andExpect(jsonPath("$[0].rows", notNullValue()))
            .andExpect(jsonPath("$[0].rows", hasSize(3)))
            .andExpect(jsonPath("$[0].rows[0].label", is("Bachelor's Degree")))
            .andExpect(jsonPath("$[0].rows[0].value", is(1000)))
            .andExpect(jsonPath("$[0].rows[1].label", is("Primary School")))
            .andExpect(jsonPath("$[0].rows[1].value", is(2000)))
            .andExpect(jsonPath("$[0].rows[2].label", is("Doctoral Degree")))
            .andExpect(jsonPath("$[0].rows[2].value", is(3000)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeMaxEducationStats(eq(Gender.female), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get stats - languages report succeeds")
  void getAllStatsLanguagesReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.languages));

    given(candidateStatsService
        .computeLanguageStats(any(), any(), any(), any(), any(), any()))
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
        .andExpect(jsonPath("$", hasSize(1)))

        // languages
        .andExpect(jsonPath("$[0].name", is("Languages")))
        .andExpect(jsonPath("$[0].chartType", is("doughnut")))
        .andExpect(jsonPath("$[0].rows", notNullValue()))
        .andExpect(jsonPath("$[0].rows", hasSize(3)))
        .andExpect(jsonPath("$[0].rows[0].label", is("English")))
        .andExpect(jsonPath("$[0].rows[0].value", is(1000)))
        .andExpect(jsonPath("$[0].rows[1].label", is("Arabic")))
        .andExpect(jsonPath("$[0].rows[1].value", is(2000)))
        .andExpect(jsonPath("$[0].rows[2].label", is("French")))
        .andExpect(jsonPath("$[0].rows[2].value", is(3000)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeLanguageStats(any(), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get stats - languages male report succeeds")
  void getAllStatsLanguagesMaleReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.languagesMale));

    given(candidateStatsService
            .computeLanguageStats(eq(Gender.male), any(), any(), any(), any(), any()))
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
            .andExpect(jsonPath("$", hasSize(1)))

            // languages
            .andExpect(jsonPath("$[0].name", is("Languages (Male)")))
            .andExpect(jsonPath("$[0].chartType", is("doughnut")))
            .andExpect(jsonPath("$[0].rows", notNullValue()))
            .andExpect(jsonPath("$[0].rows", hasSize(3)))
            .andExpect(jsonPath("$[0].rows[0].label", is("English")))
            .andExpect(jsonPath("$[0].rows[0].value", is(1000)))
            .andExpect(jsonPath("$[0].rows[1].label", is("Arabic")))
            .andExpect(jsonPath("$[0].rows[1].value", is(2000)))
            .andExpect(jsonPath("$[0].rows[2].label", is("French")))
            .andExpect(jsonPath("$[0].rows[2].value", is(3000)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeLanguageStats(eq(Gender.male), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get stats - languages female report succeeds")
  void getAllStatsLanguagesFemaleReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.languagesFemale));

    given(candidateStatsService
            .computeLanguageStats(eq(Gender.female), any(), any(), any(), any(), any()))
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
            .andExpect(jsonPath("$", hasSize(1)))

            // languages
            .andExpect(jsonPath("$[0].name", is("Languages (Female)")))
            .andExpect(jsonPath("$[0].chartType", is("doughnut")))
            .andExpect(jsonPath("$[0].rows", notNullValue()))
            .andExpect(jsonPath("$[0].rows", hasSize(3)))
            .andExpect(jsonPath("$[0].rows[0].label", is("English")))
            .andExpect(jsonPath("$[0].rows[0].value", is(1000)))
            .andExpect(jsonPath("$[0].rows[1].label", is("Arabic")))
            .andExpect(jsonPath("$[0].rows[1].value", is(2000)))
            .andExpect(jsonPath("$[0].rows[2].label", is("French")))
            .andExpect(jsonPath("$[0].rows[2].value", is(3000)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeLanguageStats(eq(Gender.female), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - referrers report succeeds")
  void getAllStatsReferrersReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.referrers));

    given(candidateStatsService
        .computeReferrerStats(any(), any(), any(), any(), any(), any(), any()))
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
        .andExpect(jsonPath("$", hasSize(1)))

        // referrers
        .andExpect(jsonPath("$[0].name", is("Referrers")))
        .andExpect(jsonPath("$[0].chartType", is("bar")))
        .andExpect(jsonPath("$[0].rows", notNullValue()))
        .andExpect(jsonPath("$[0].rows", hasSize(2)))
        .andExpect(jsonPath("$[0].rows[0].label", is("auntie rene")))
        .andExpect(jsonPath("$[0].rows[0].value", is(1000)))
        .andExpect(jsonPath("$[0].rows[1].label", is("uncle fred")))
        .andExpect(jsonPath("$[0].rows[1].value", is(2000)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeReferrerStats(any(), any(), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - referrers male report succeeds")
  void getAllStatsReferrersMaleReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.referrersMale));

    given(candidateStatsService
            .computeReferrerStats(eq(Gender.male), any(), any(), any(), any(), any(), any()))
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
            .andExpect(jsonPath("$", hasSize(1)))

            // referrers
            .andExpect(jsonPath("$[0].name", is("Referrers (Male)")))
            .andExpect(jsonPath("$[0].chartType", is("bar")))
            .andExpect(jsonPath("$[0].rows", notNullValue()))
            .andExpect(jsonPath("$[0].rows", hasSize(2)))
            .andExpect(jsonPath("$[0].rows[0].label", is("auntie rene")))
            .andExpect(jsonPath("$[0].rows[0].value", is(1000)))
            .andExpect(jsonPath("$[0].rows[1].label", is("uncle fred")))
            .andExpect(jsonPath("$[0].rows[1].value", is(2000)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeReferrerStats(eq(Gender.male), any(), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - referrers female report succeeds")
  void getAllStatsReferrersFemaleReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.referrersFemale));

    given(candidateStatsService
            .computeReferrerStats(eq(Gender.female), any(), any(), any(), any(), any(), any()))
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
            .andExpect(jsonPath("$", hasSize(1)))

            // referrers
            .andExpect(jsonPath("$[0].name", is("Referrers (Female)")))
            .andExpect(jsonPath("$[0].chartType", is("bar")))
            .andExpect(jsonPath("$[0].rows", notNullValue()))
            .andExpect(jsonPath("$[0].rows", hasSize(2)))
            .andExpect(jsonPath("$[0].rows[0].label", is("auntie rene")))
            .andExpect(jsonPath("$[0].rows[0].value", is(1000)))
            .andExpect(jsonPath("$[0].rows[1].label", is("uncle fred")))
            .andExpect(jsonPath("$[0].rows[1].value", is(2000)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeReferrerStats(eq(Gender.female), any(), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - survey report succeeds")
  void getAllStatsSurveyReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.survey));

    given(candidateStatsService
        .computeSurveyStats(any(), any(), any(), any(), any(), any(), any()))
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
        .andExpect(jsonPath("$", hasSize(1)))

        // survey
        .andExpect(jsonPath("$[0].name", is("Survey")))
        .andExpect(jsonPath("$[0].chartType", is("doughnut")))
        .andExpect(jsonPath("$[0].rows", notNullValue()))
        .andExpect(jsonPath("$[0].rows", hasSize(3)))
        .andExpect(jsonPath("$[0].rows[0].label", is("Facebook")))
        .andExpect(jsonPath("$[0].rows[0].value", is(1000)))
        .andExpect(jsonPath("$[0].rows[1].label", is("From a friend")))
        .andExpect(jsonPath("$[0].rows[1].value", is(2000)))
        .andExpect(jsonPath("$[0].rows[2].label", is("NGO")))
        .andExpect(jsonPath("$[0].rows[2].value", is(3000)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeSurveyStats(any(), any(), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get stats - survey jordan report succeeds")
  void getAllStatsSurveyJordanReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.surveyJordan));

    given(candidateStatsService
            .computeSurveyStats(any(), eq("jordan"), any(), any(), any(), any(), any()))
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
            .andExpect(jsonPath("$", hasSize(1)))

            // survey
            .andExpect(jsonPath("$[0].name", is("Survey (Jordan)")))
            .andExpect(jsonPath("$[0].chartType", is("doughnut")))
            .andExpect(jsonPath("$[0].rows", notNullValue()))
            .andExpect(jsonPath("$[0].rows", hasSize(3)))
            .andExpect(jsonPath("$[0].rows[0].label", is("Facebook")))
            .andExpect(jsonPath("$[0].rows[0].value", is(1000)))
            .andExpect(jsonPath("$[0].rows[1].label", is("From a friend")))
            .andExpect(jsonPath("$[0].rows[1].value", is(2000)))
            .andExpect(jsonPath("$[0].rows[2].label", is("NGO")))
            .andExpect(jsonPath("$[0].rows[2].value", is(3000)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeSurveyStats(any(), eq("jordan"), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get stats - survey lebanon report succeeds")
  void getAllStatsSurveyLebanonReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.surveyLebanon));

    given(candidateStatsService
            .computeSurveyStats(any(), eq("lebanon"), any(), any(), any(), any(), any()))
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
            .andExpect(jsonPath("$", hasSize(1)))

            // survey
            .andExpect(jsonPath("$[0].name", is("Survey (Lebanon)")))
            .andExpect(jsonPath("$[0].chartType", is("doughnut")))
            .andExpect(jsonPath("$[0].rows", notNullValue()))
            .andExpect(jsonPath("$[0].rows", hasSize(3)))
            .andExpect(jsonPath("$[0].rows[0].label", is("Facebook")))
            .andExpect(jsonPath("$[0].rows[0].value", is(1000)))
            .andExpect(jsonPath("$[0].rows[1].label", is("From a friend")))
            .andExpect(jsonPath("$[0].rows[1].value", is(2000)))
            .andExpect(jsonPath("$[0].rows[2].label", is("NGO")))
            .andExpect(jsonPath("$[0].rows[2].value", is(3000)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeSurveyStats(any(), eq("lebanon"), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get stats - survey male report succeeds")
  void getAllStatsSurveyMaleReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.surveyMale));

    given(candidateStatsService
            .computeSurveyStats(eq(Gender.male), any(), any(), any(), any(), any(), any()))
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
            .andExpect(jsonPath("$", hasSize(1)))

            // survey
            .andExpect(jsonPath("$[0].name", is("Survey (Male)")))
            .andExpect(jsonPath("$[0].chartType", is("doughnut")))
            .andExpect(jsonPath("$[0].rows", notNullValue()))
            .andExpect(jsonPath("$[0].rows", hasSize(3)))
            .andExpect(jsonPath("$[0].rows[0].label", is("Facebook")))
            .andExpect(jsonPath("$[0].rows[0].value", is(1000)))
            .andExpect(jsonPath("$[0].rows[1].label", is("From a friend")))
            .andExpect(jsonPath("$[0].rows[1].value", is(2000)))
            .andExpect(jsonPath("$[0].rows[2].label", is("NGO")))
            .andExpect(jsonPath("$[0].rows[2].value", is(3000)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeSurveyStats(eq(Gender.male), any(), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get stats - survey female report succeeds")
  void getAllStatsSurveyFemaleReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.surveyFemale));

    given(candidateStatsService
            .computeSurveyStats(eq(Gender.female), any(), any(), any(), any(), any(), any()))
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
            .andExpect(jsonPath("$", hasSize(1)))

            // survey
            .andExpect(jsonPath("$[0].name", is("Survey (Female)")))
            .andExpect(jsonPath("$[0].chartType", is("doughnut")))
            .andExpect(jsonPath("$[0].rows", notNullValue()))
            .andExpect(jsonPath("$[0].rows", hasSize(3)))
            .andExpect(jsonPath("$[0].rows[0].label", is("Facebook")))
            .andExpect(jsonPath("$[0].rows[0].value", is(1000)))
            .andExpect(jsonPath("$[0].rows[1].label", is("From a friend")))
            .andExpect(jsonPath("$[0].rows[1].value", is(2000)))
            .andExpect(jsonPath("$[0].rows[2].label", is("NGO")))
            .andExpect(jsonPath("$[0].rows[2].value", is(3000)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeSurveyStats(eq(Gender.female), any(), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get stats - spoken english report succeeds")
  void getAllStatsSpokenEnglishReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.spokenEnglish));

    given(candidateStatsService
        .computeSpokenLanguageLevelStats(any(), eq("English"), any(), any(), any(), any(), any()))
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
        .andExpect(jsonPath("$", hasSize(1)))

        // spoken language
        .andExpect(jsonPath("$[0].name", is("Spoken English Language Level")))
        .andExpect(jsonPath("$[0].chartType", is("doughnut")))
        .andExpect(jsonPath("$[0].rows", notNullValue()))
        .andExpect(jsonPath("$[0].rows", hasSize(3)))
        .andExpect(jsonPath("$[0].rows[0].label", is("Intermediate Proficiency")))
        .andExpect(jsonPath("$[0].rows[0].value", is(1000)))
        .andExpect(jsonPath("$[0].rows[1].label", is("Full Professional Proficiency")))
        .andExpect(jsonPath("$[0].rows[1].value", is(2000)))
        .andExpect(jsonPath("$[0].rows[2].label", is("Elementary Proficiency")))
        .andExpect(jsonPath("$[0].rows[2].value", is(3000)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService)
            .computeSpokenLanguageLevelStats(any(), eq("English"), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get stats - spoken english male report succeeds")
  void getAllStatsSpokenEnglishMaleReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.spokenEnglishMale));

    given(candidateStatsService
            .computeSpokenLanguageLevelStats(eq(Gender.male), eq("English"), any(), any(), any(), any(), any()))
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
            .andExpect(jsonPath("$", hasSize(1)))

            // spoken language
            .andExpect(jsonPath("$[0].name", is("Spoken English Language Level (Male)")))
            .andExpect(jsonPath("$[0].chartType", is("doughnut")))
            .andExpect(jsonPath("$[0].rows", notNullValue()))
            .andExpect(jsonPath("$[0].rows", hasSize(3)))
            .andExpect(jsonPath("$[0].rows[0].label", is("Intermediate Proficiency")))
            .andExpect(jsonPath("$[0].rows[0].value", is(1000)))
            .andExpect(jsonPath("$[0].rows[1].label", is("Full Professional Proficiency")))
            .andExpect(jsonPath("$[0].rows[1].value", is(2000)))
            .andExpect(jsonPath("$[0].rows[2].label", is("Elementary Proficiency")))
            .andExpect(jsonPath("$[0].rows[2].value", is(3000)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService)
            .computeSpokenLanguageLevelStats(eq(Gender.male), eq("English"), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get stats - spoken english female report succeeds")
  void getAllStatsSpokenEnglishFemaleReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.spokenEnglishFemale));

    given(candidateStatsService
            .computeSpokenLanguageLevelStats(eq(Gender.female), eq("English"), any(), any(), any(), any(), any()))
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
            .andExpect(jsonPath("$", hasSize(1)))

            // spoken language
            .andExpect(jsonPath("$[0].name", is("Spoken English Language Level (Female)")))
            .andExpect(jsonPath("$[0].chartType", is("doughnut")))
            .andExpect(jsonPath("$[0].rows", notNullValue()))
            .andExpect(jsonPath("$[0].rows", hasSize(3)))
            .andExpect(jsonPath("$[0].rows[0].label", is("Intermediate Proficiency")))
            .andExpect(jsonPath("$[0].rows[0].value", is(1000)))
            .andExpect(jsonPath("$[0].rows[1].label", is("Full Professional Proficiency")))
            .andExpect(jsonPath("$[0].rows[1].value", is(2000)))
            .andExpect(jsonPath("$[0].rows[2].label", is("Elementary Proficiency")))
            .andExpect(jsonPath("$[0].rows[2].value", is(3000)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService)
            .computeSpokenLanguageLevelStats(eq(Gender.female), eq("English"), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get stats - spoken french report succeeds")
  void getAllStatsSpokenFrenchReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.spokenFrench));

    given(candidateStatsService
            .computeSpokenLanguageLevelStats(any(), eq("French"), any(), any(), any(), any(), any()))
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
            .andExpect(jsonPath("$", hasSize(1)))

            // spoken language
            .andExpect(jsonPath("$[0].name", is("Spoken French Language Level")))
            .andExpect(jsonPath("$[0].chartType", is("doughnut")))
            .andExpect(jsonPath("$[0].rows", notNullValue()))
            .andExpect(jsonPath("$[0].rows", hasSize(3)))
            .andExpect(jsonPath("$[0].rows[0].label", is("Intermediate Proficiency")))
            .andExpect(jsonPath("$[0].rows[0].value", is(1000)))
            .andExpect(jsonPath("$[0].rows[1].label", is("Full Professional Proficiency")))
            .andExpect(jsonPath("$[0].rows[1].value", is(2000)))
            .andExpect(jsonPath("$[0].rows[2].label", is("Elementary Proficiency")))
            .andExpect(jsonPath("$[0].rows[2].value", is(3000)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService).computeSpokenLanguageLevelStats(any(), eq("French"), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get stats - spoken french male report succeeds")
  void getAllStatsSpokenFrenchMaleReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.spokenFrenchMale));

    given(candidateStatsService
            .computeSpokenLanguageLevelStats(eq(Gender.male), eq("French"), any(), any(), any(), any(), any()))
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
            .andExpect(jsonPath("$", hasSize(1)))

            // spoken language
            .andExpect(jsonPath("$[0].name", is("Spoken French Language Level (Male)")))
            .andExpect(jsonPath("$[0].chartType", is("doughnut")))
            .andExpect(jsonPath("$[0].rows", notNullValue()))
            .andExpect(jsonPath("$[0].rows", hasSize(3)))
            .andExpect(jsonPath("$[0].rows[0].label", is("Intermediate Proficiency")))
            .andExpect(jsonPath("$[0].rows[0].value", is(1000)))
            .andExpect(jsonPath("$[0].rows[1].label", is("Full Professional Proficiency")))
            .andExpect(jsonPath("$[0].rows[1].value", is(2000)))
            .andExpect(jsonPath("$[0].rows[2].label", is("Elementary Proficiency")))
            .andExpect(jsonPath("$[0].rows[2].value", is(3000)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService)
            .computeSpokenLanguageLevelStats(eq(Gender.male), eq("French"), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("get stats - spoken french female report succeeds")
  void getAllStatsSpokenFrenchFemaleReportSucceeds() throws Exception {

    request.setSelectedStats(List.of(Stat.spokenFrenchFemale));

    given(candidateStatsService
            .computeSpokenLanguageLevelStats(eq(Gender.female), eq("French"), any(), any(), any(), any(), any()))
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
            .andExpect(jsonPath("$", hasSize(1)))

            // spoken language
            .andExpect(jsonPath("$[0].name", is("Spoken French Language Level (Female)")))
            .andExpect(jsonPath("$[0].chartType", is("doughnut")))
            .andExpect(jsonPath("$[0].rows", notNullValue()))
            .andExpect(jsonPath("$[0].rows", hasSize(3)))
            .andExpect(jsonPath("$[0].rows[0].label", is("Intermediate Proficiency")))
            .andExpect(jsonPath("$[0].rows[0].value", is(1000)))
            .andExpect(jsonPath("$[0].rows[1].label", is("Full Professional Proficiency")))
            .andExpect(jsonPath("$[0].rows[1].value", is(2000)))
            .andExpect(jsonPath("$[0].rows[2].label", is("Elementary Proficiency")))
            .andExpect(jsonPath("$[0].rows[2].value", is(3000)));

    verify(authService, atLeastOnce()).getLoggedInUser();
    verify(candidateStatsService)
            .computeSpokenLanguageLevelStats(eq(Gender.female), eq("French"), any(), any(), any(), any(), any());
  }

}
