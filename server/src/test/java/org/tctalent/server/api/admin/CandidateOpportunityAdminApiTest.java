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
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.request.candidate.dependant.UpdateRelocatingDependantIds;
import org.tctalent.server.request.candidate.opportunity.CandidateOpportunityParams;
import org.tctalent.server.request.candidate.opportunity.SearchCandidateOpportunityRequest;
import org.tctalent.server.service.db.CandidateOpportunityService;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.service.db.SalesforceService;

/**
 * Unit tests for Candidate Opportunity Admin Api endpoints.
 *
 * @author sadatmalik
 */
@WebMvcTest(CandidateOpportunityAdminApi.class)
@AutoConfigureMockMvc
class CandidateOpportunityAdminApiTest extends ApiTestBase {

    private static final long CANDIDATE_ID = 99L;

    private static final String BASE_PATH = "/api/admin/opp";
    private static final String SEARCH_PAGED_PATH = "/search-paged";
    private static final String UPDATE_SF_CASE_PATH = "/{id}/update-sf-case-relocation-info";
    private static final String RELOCATING_DEPENDANTS_PATH = "/{id}/relocating-dependants";

    private static final CandidateOpportunity candidateOpportunity = AdminApiTestUtil.getCandidateOpportunity();
    private final Page<CandidateOpportunity> candidateOpportunityPage =
            new PageImpl<>(
                    List.of(candidateOpportunity),
                    PageRequest.of(0,10, Sort.unsorted()),
                    1
            );

    @MockBean CandidateOpportunityService candidateOpportunityService;
    @MockBean CountryService countryService;
    @MockBean SalesforceService salesforceService;

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired CandidateOpportunityAdminApi candidateOpportunityAdminApi;

    @BeforeEach
    void setUp() {
        configureAuthentication();
    }

    @Test
    public void testWebOnlyContextLoads() {
        assertThat(candidateOpportunityAdminApi).isNotNull();
    }

    @Test
    @DisplayName("get candidate opportunity by id succeeds")
    void getCandidateOpportunityByIdSucceeds() throws Exception {

        given(candidateOpportunityService
                .getCandidateOpportunity(anyLong()))
                .willReturn(candidateOpportunity);

        mockMvc.perform(get(BASE_PATH + "/" + CANDIDATE_ID)
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.candidate.user.firstName", is("test")))
                .andExpect(jsonPath("$.candidate.user.lastName", is("candidate1")))
                .andExpect(jsonPath("$.candidate.user.email", is("test.candidate1@some.thing")))
                .andExpect(jsonPath("$.candidate.user.username", is("candidate1")))
                .andExpect(jsonPath("$.stage", is("offer")))
                .andExpect(jsonPath("$.closingCommentsForCandidate", is("Some closing comments for candidate")))
                .andExpect(jsonPath("$.closed", is(false)))
                .andExpect(jsonPath("$.employerFeedback", is("Some employer feedback")));

        verify(candidateOpportunityService).getCandidateOpportunity(anyLong());
    }

    @Test
    @DisplayName("search opportunities paged succeeds")
    void searchPagedSucceeds() throws Exception {
        SearchCandidateOpportunityRequest request = new SearchCandidateOpportunityRequest();

        given(candidateOpportunityService
                .searchCandidateOpportunities(any(SearchCandidateOpportunityRequest.class)))
                .willReturn(candidateOpportunityPage);

        mockMvc.perform(post(BASE_PATH + SEARCH_PAGED_PATH)
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
                .andExpect(jsonPath("$.content.[0].candidate.user.firstName", is("test")))
                .andExpect(jsonPath("$.content.[0].candidate.user.lastName", is("candidate1")))
                .andExpect(jsonPath("$.content.[0].candidate.user.email", is("test.candidate1@some.thing")))
                .andExpect(jsonPath("$.content.[0].candidate.user.username", is("candidate1")))
                .andExpect(jsonPath("$.content.[0].stage", is("offer")))
                .andExpect(jsonPath("$.content.[0].closingCommentsForCandidate", is("Some closing comments for candidate")))
                .andExpect(jsonPath("$.content.[0].closed", is(false)))
                .andExpect(jsonPath("$.content.[0].employerFeedback", is("Some employer feedback")));

        verify(candidateOpportunityService).searchCandidateOpportunities(any(SearchCandidateOpportunityRequest.class));
    }

    @Test
    @DisplayName("update opportunity by id succeeds")
    void updateCandidateOccupationByIdSucceeds() throws Exception {
        CandidateOpportunityParams request = new CandidateOpportunityParams();

        given(candidateOpportunityService
                .updateCandidateOpportunity(anyLong(), any(CandidateOpportunityParams.class)))
                .willReturn(candidateOpportunity);

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
                .andExpect(jsonPath("$.candidate.user.firstName", is("test")))
                .andExpect(jsonPath("$.candidate.user.lastName", is("candidate1")))
                .andExpect(jsonPath("$.candidate.user.email", is("test.candidate1@some.thing")))
                .andExpect(jsonPath("$.candidate.user.username", is("candidate1")))
                .andExpect(jsonPath("$.stage", is("offer")))
                .andExpect(jsonPath("$.closingCommentsForCandidate", is("Some closing comments for candidate")))
                .andExpect(jsonPath("$.closed", is(false)))
                .andExpect(jsonPath("$.employerFeedback", is("Some employer feedback")));

        verify(candidateOpportunityService).updateCandidateOpportunity(anyLong(), any(CandidateOpportunityParams.class));
    }

    @Test
    @DisplayName("update sf case relocation info succeeds")
    void updateSfCaseRelocationInfoSucceeds() throws Exception {
        given(candidateOpportunityService
                .getCandidateOpportunity(anyLong()))
                .willReturn(any(CandidateOpportunity.class));

        mockMvc.perform(put(BASE_PATH + UPDATE_SF_CASE_PATH.replace(
                        "{id}", "3"))
                        .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token"))

                .andDo(print())
                .andExpect(status().isOk());

        verify(candidateOpportunityService).getCandidateOpportunity(anyLong());
    }

    @Test
    @DisplayName("update relocating dependants succeeds")
    void updateRelocatingDependantsSucceeds() throws Exception {
        UpdateRelocatingDependantIds request = new UpdateRelocatingDependantIds();
        request.setRelocatingDependantIds(List.of(1L, 2L));

        given(candidateOpportunityService
                .updateRelocatingDependants(anyLong(), any(UpdateRelocatingDependantIds.class)))
                .willReturn(candidateOpportunity);

        mockMvc.perform(put(BASE_PATH + RELOCATING_DEPENDANTS_PATH.replace(
                        "{id}", "3"))
                        .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk());

        verify(candidateOpportunityService).updateRelocatingDependants(anyLong(), any(UpdateRelocatingDependantIds.class));
    }
}
