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
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import org.tctalent.server.data.CandidateTestData;
import org.tctalent.server.model.db.CandidateJobExperience;
import org.tctalent.server.request.work.experience.CreateJobExperienceRequest;
import org.tctalent.server.request.work.experience.SearchJobExperienceRequest;
import org.tctalent.server.request.work.experience.UpdateJobExperienceRequest;
import org.tctalent.server.service.db.CandidateJobExperienceService;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.service.db.OccupationService;

/**
 * Unit tests for Candidate Job Experience Admin Api endpoints.
 *
 * @author sadatmalik
 */
@WebMvcTest(CandidateJobExperienceAdminApi.class)
@AutoConfigureMockMvc
class CandidateJobExperienceAdminApiTest extends ApiTestBase {

    private static final long CANDIDATE_ID = 99L;
    private static final String BASE_PATH = "/api/admin/candidate-job-experience";
    private static final String SEARCH_PATH = "/search";

    private static final CandidateJobExperience candidateJobExperience =
        CandidateTestData.getCandidateJobExperience();

    private final Page<CandidateJobExperience> candidateJobExperiencePage =
            new PageImpl<>(
                    List.of(candidateJobExperience),
                    PageRequest.of(0,10, Sort.unsorted()),
                    1
            );

    @MockBean CandidateJobExperienceService candidateJobExperienceService;
    @MockBean CountryService countryService;
    @MockBean OccupationService occupationService;

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired CandidateJobExperienceAdminApi candidateJobExperienceAdminApi;

    @BeforeEach
    void setUp() {
        configureAuthentication();
    }

    @Test
    public void testWebOnlyContextLoads() {
        assertThat(candidateJobExperienceAdminApi).isNotNull();
    }

    @Test
    @DisplayName("search candidate job experience succeeds")
    void searchJobExperienceSucceeds() throws Exception {
        SearchJobExperienceRequest request = new SearchJobExperienceRequest();

        given(candidateJobExperienceService
                .searchCandidateJobExperience(any(SearchJobExperienceRequest.class)))
                .willReturn(candidateJobExperiencePage);

        mockMvc.perform(post(BASE_PATH + SEARCH_PATH)
                        .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.number", is(0)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)))
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.content.[0].country.name", is("Syria")))
                .andExpect(jsonPath("$.content.[0].country.status", is("active")))
                .andExpect(jsonPath("$.content.[0].role", is("Software Engineer")))
                .andExpect(jsonPath("$.content.[0].candidateOccupation.occupation.name", is("Software Engineer")))
                .andExpect(jsonPath("$.content.[0].candidateOccupation.yearsExperience", is(10)))
                .andExpect(jsonPath("$.content.[0].startDate", is("1998-01-01")))
                .andExpect(jsonPath("$.content.[0].endDate", is("2008-01-01")))
                .andExpect(jsonPath("$.content.[0].companyName", is("Microsoft")))
                .andExpect(jsonPath("$.content.[0].paid", is(true)))
                .andExpect(jsonPath("$.content.[0].description", is("Some job experience description")))
                .andExpect(jsonPath("$.content.[0].fullTime", is(true)));

        verify(candidateJobExperienceService).searchCandidateJobExperience(any(SearchJobExperienceRequest.class));
    }

    @Test
    @DisplayName("create candidate job experience by candidate id succeeds")
    void createCandidateJobExperienceByCandidateIdSucceeds() throws Exception {
        CreateJobExperienceRequest request = new CreateJobExperienceRequest();
        request.setCompanyName("Microsoft");
        request.setCountry(1L);
        request.setCandidateOccupationId(2L);
        request.setRole("Software Engineer");
        request.setDescription("Some job experience description");

        given(candidateJobExperienceService
                .createCandidateJobExperience(any(CreateJobExperienceRequest.class)))
                .willReturn(candidateJobExperience);

        mockMvc.perform(post(BASE_PATH + "/" + CANDIDATE_ID)
                        .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.country.name", is("Syria")))
                .andExpect(jsonPath("$.country.status", is("active")))
                .andExpect(jsonPath("$.role", is("Software Engineer")))
                .andExpect(jsonPath("$.candidateOccupation.occupation.name", is("Software Engineer")))
                .andExpect(jsonPath("$.candidateOccupation.yearsExperience", is(10)))
                .andExpect(jsonPath("$.startDate", is("1998-01-01")))
                .andExpect(jsonPath("$.endDate", is("2008-01-01")))
                .andExpect(jsonPath("$.companyName", is("Microsoft")))
                .andExpect(jsonPath("$.paid", is(true)))
                .andExpect(jsonPath("$.description", is("Some job experience description")))
                .andExpect(jsonPath("$.fullTime", is(true)));

        verify(candidateJobExperienceService).createCandidateJobExperience(any(CreateJobExperienceRequest.class));
    }

    @Test
    @DisplayName("update candidate job experience by id succeeds")
    void updateCandidateJobExperienceByIdSucceeds() throws Exception {
        UpdateJobExperienceRequest request = new UpdateJobExperienceRequest();
        request.setCompanyName("Microsoft");
        request.setCountryId(1L);
        request.setRole("Software Engineer");
        request.setDescription("Some job experience description");

        given(candidateJobExperienceService
                .updateCandidateJobExperience(anyLong(), any(UpdateJobExperienceRequest.class)))
                .willReturn(candidateJobExperience);

        mockMvc.perform(put(BASE_PATH + "/" + CANDIDATE_ID)
                        .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.country.name", is("Syria")))
                .andExpect(jsonPath("$.country.status", is("active")))
                .andExpect(jsonPath("$.role", is("Software Engineer")))
                .andExpect(jsonPath("$.candidateOccupation.occupation.name", is("Software Engineer")))
                .andExpect(jsonPath("$.candidateOccupation.yearsExperience", is(10)))
                .andExpect(jsonPath("$.startDate", is("1998-01-01")))
                .andExpect(jsonPath("$.endDate", is("2008-01-01")))
                .andExpect(jsonPath("$.companyName", is("Microsoft")))
                .andExpect(jsonPath("$.paid", is(true)))
                .andExpect(jsonPath("$.description", is("Some job experience description")))
                .andExpect(jsonPath("$.fullTime", is(true)));

        verify(candidateJobExperienceService).updateCandidateJobExperience(anyLong(), any(UpdateJobExperienceRequest.class));
    }

    @Test
    @DisplayName("delete candidate job experience by id succeeds")
    void deleteCandidateJobExperienceByIdSucceeds() throws Exception {
        mockMvc.perform(delete(BASE_PATH + "/" + CANDIDATE_ID)
                        .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token"))

                .andExpect(status().isOk());

        verify(candidateJobExperienceService).deleteCandidateJobExperience(anyLong());
    }
}
