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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.tctalent.server.api.admin.StatsApiTestUtil.getBirthYearStats;
import static org.tctalent.server.api.admin.StatsApiTestUtil.getGenderStats;
import static org.tctalent.server.api.admin.StatsApiTestUtil.getLinkedInByRegistrationDateStats;
import static org.tctalent.server.api.admin.StatsApiTestUtil.getLinkedInExistsStats;
import static org.tctalent.server.api.admin.StatsApiTestUtil.getNationalityStats;
import static org.tctalent.server.api.admin.StatsApiTestUtil.getRegistrationByOccupationStats;
import static org.tctalent.server.api.admin.StatsApiTestUtil.getRegistrationStats;
import static org.tctalent.server.api.admin.StatsApiTestUtil.getUnhcrRegistrationStats;
import static org.tctalent.server.api.admin.StatsApiTestUtil.getUnhcrStatusStats;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.tctalent.server.model.db.Status;
import org.tctalent.server.repository.db.CountryRepository;
import org.tctalent.server.request.candidate.stat.CandidateStatsRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateService;
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
  @MockBean CountryRepository countryRepository;
  @MockBean SavedListService savedListService;
  @MockBean SavedSearchService savedSearchService;
  @MockBean AuthService authService;

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;
  @Autowired CandidateStatAdminApi candidateStatAdminApi;

  @BeforeEach
  void setUp() {
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
    CandidateStatsRequest request = new CandidateStatsRequest();

    given(candidateService
        .computeGenderStats(any(), any(), any()))
        .willReturn(getGenderStats());

    mockMvc.perform(post(BASE_PATH + ALL_STATS_PATH)
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$", hasSize(46)))

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

    verify(authService).getLoggedInUser();
    verify(candidateService).computeGenderStats(any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - registration report succeeds")
  void getAllStatsRegistrationReportSucceeds() throws Exception {
    CandidateStatsRequest request = new CandidateStatsRequest();

    given(candidateService
        .computeRegistrationStats(any(), any(), any()))
        .willReturn(getRegistrationStats());

    mockMvc.perform(post(BASE_PATH + ALL_STATS_PATH)
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$", hasSize(46)))

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

    verify(authService).getLoggedInUser();
    verify(candidateService).computeRegistrationStats(any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - registration by occupation report succeeds")
  void getAllStatsRegistrationByOccupationReportSucceeds() throws Exception {
    CandidateStatsRequest request = new CandidateStatsRequest();

    given(candidateService
        .computeRegistrationOccupationStats(any(), any(), any()))
        .willReturn(getRegistrationByOccupationStats());

    mockMvc.perform(post(BASE_PATH + ALL_STATS_PATH)
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$", hasSize(46)))

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

    verify(authService).getLoggedInUser();
    verify(candidateService).computeRegistrationOccupationStats(any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - birth year report succeeds")
  void getAllStatsBirthYearReportSucceeds() throws Exception {
    CandidateStatsRequest request = new CandidateStatsRequest();

    given(candidateService
        .computeBirthYearStats(any(), any(), any(), any()))
        .willReturn(getBirthYearStats());

    mockMvc.perform(post(BASE_PATH + ALL_STATS_PATH)
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$", hasSize(46)))

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

    verify(authService).getLoggedInUser();
    verify(candidateService, times(3)).computeBirthYearStats(any(), any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - linked in links report succeeds")
  void getAllStatsLinkedInLinksReportSucceeds() throws Exception {
    CandidateStatsRequest request = new CandidateStatsRequest();

    given(candidateService
        .computeLinkedInExistsStats(any(), any(), any()))
        .willReturn(getLinkedInExistsStats());

    mockMvc.perform(post(BASE_PATH + ALL_STATS_PATH)
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$", hasSize(46)))

        // Linkedin links
        .andExpect(jsonPath("$[6].name", is("LinkedIn links")))
        .andExpect(jsonPath("$[6].chartType", is("bar")))
        .andExpect(jsonPath("$[6].rows", notNullValue()))
        .andExpect(jsonPath("$[6].rows", hasSize(2)))
        .andExpect(jsonPath("$[6].rows[0].label", is("No link")))
        .andExpect(jsonPath("$[6].rows[0].value", is(2)))
        .andExpect(jsonPath("$[6].rows[1].label", is("Has link")))
        .andExpect(jsonPath("$[6].rows[1].value", is(10)));

    verify(authService).getLoggedInUser();
    verify(candidateService).computeLinkedInExistsStats(any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - linked in links by registration date report succeeds")
  void getAllStatsLinkedInLinksByRegistrationDateReportSucceeds() throws Exception {
    CandidateStatsRequest request = new CandidateStatsRequest();

    given(candidateService
        .computeLinkedInStats(any(), any(), any()))
        .willReturn(getLinkedInByRegistrationDateStats());

    mockMvc.perform(post(BASE_PATH + ALL_STATS_PATH)
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$", hasSize(46)))

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

    verify(authService).getLoggedInUser();
    verify(candidateService).computeLinkedInStats(any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - Unhcr registration date report succeeds")
  void getAllStatsUnhcrRegistrationReportSucceeds() throws Exception {
    CandidateStatsRequest request = new CandidateStatsRequest();

    given(candidateService
        .computeUnhcrRegisteredStats(any(), any(), any()))
        .willReturn(getUnhcrRegistrationStats());

    mockMvc.perform(post(BASE_PATH + ALL_STATS_PATH)
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$", hasSize(46)))

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

    verify(authService).getLoggedInUser();
    verify(candidateService).computeUnhcrRegisteredStats(any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - Unhcr status report succeeds")
  void getAllStatsUnhcrStatusReportSucceeds() throws Exception {
    CandidateStatsRequest request = new CandidateStatsRequest();

    given(candidateService
        .computeUnhcrStatusStats(any(), any(), any()))
        .willReturn(getUnhcrStatusStats());

    mockMvc.perform(post(BASE_PATH + ALL_STATS_PATH)
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$", hasSize(46)))

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

    verify(authService).getLoggedInUser();
    verify(candidateService).computeUnhcrStatusStats(any(), any(), any());
  }

  @Test
  @DisplayName("get all stats - nationalities report succeeds")
  void getAllStatsNationalityReportSucceeds() throws Exception {
    CandidateStatsRequest request = new CandidateStatsRequest();

    given(candidateService
        .computeNationalityStats(any(), any(), any(), any(), any()))
        .willReturn(getNationalityStats());

    mockMvc.perform(post(BASE_PATH + ALL_STATS_PATH)
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$", hasSize(46)))

        // Nationality
        .andExpect(jsonPath("$[10].name", is("Nationalities by Country")))
        .andExpect(jsonPath("$[10].chartType", is("doughnut")))
        .andExpect(jsonPath("$[10].rows", notNullValue()))
        .andExpect(jsonPath("$[10].rows", hasSize(2)))
        .andExpect(jsonPath("$[10].rows[0].label", is("Palestinian Territories")))
        .andExpect(jsonPath("$[10].rows[0].value", is(1073)))
        .andExpect(jsonPath("$[10].rows[1].label", is("Syria")))
        .andExpect(jsonPath("$[10].rows[1].value", is(14852)));

    verify(authService).getLoggedInUser();
    verify(candidateService, times(5)).computeNationalityStats(any(), any(), any(), any(), any());
  }
}
