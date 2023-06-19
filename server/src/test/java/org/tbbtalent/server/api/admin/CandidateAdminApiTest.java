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

package org.tbbtalent.server.api.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.tbbtalent.server.model.db.*;
import org.tbbtalent.server.request.candidate.*;
import org.tbbtalent.server.security.*;
import org.tbbtalent.server.service.db.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.tbbtalent.server.api.admin.AdminApiTestUtil.getCandidate;
import static org.tbbtalent.server.api.admin.AdminApiTestUtil.listOfCandidates;

/**
 * @author sadatmalik
 */
@WebMvcTest(CandidateAdminApi.class)
@AutoConfigureMockMvc
class CandidateAdminApiTest extends ApiTestBase {
    private static final String BASE_PATH = "/api/admin/candidate";
    private static final String SEARCH_PATH = "/search";
    private static final String FIND_BY_EMAIL_PATH = "/findbyemail";
    private static final String FIND_BY_EMAIL_OR_PHONE_PATH = "/findbyemailorphone";
    private static final String FIND_BY_NUMBER_OR_NAME_PATH = "/findbynumberorname";
    private static final String FIND_BY_EXTERNAL_ID_PATH = "/findbyexternalid";
    private static final String GET_BY_NUMBER_PATH = "/number/{number}";
    private static final String GET_BY_ID_PATH = "/{id}";
    private static final String GET_INTAKE_DATA_BY_ID_PATH = "/{id}/intake";
    private static final String UPDATE_LINKS_BY_ID_PATH = "/{id}/links";
    private static final String UPDATE_STATUS_PATH = "/status";
    private static final String UPDATE_CONTACT_DETAILS_BY_ID_PATH = "/{id}";
    private static final String UPDATE_ADDITIONAL_INFO_BY_ID_PATH = "/{id}/info";
    private static final String UPDATE_SHAREABLE_NOTES_BY_ID_PATH = "/{id}/shareable-notes";
    private static final String UPDATE_SHAREABLE_DOCS_BY_ID_PATH = "/{id}/shareable-docs";
    private static final String UPDATE_SURVEY_BY_ID_PATH = "/{id}/survey";
    private static final String UPDATE_MEDIA_BY_ID_PATH = "/{id}/media";
    private static final String UPDATE_REGISTRATION_BY_ID_PATH = "/{id}/registration";
    private static final String EXPORT_CSV_PATH = "/export/csv";
    private static final String DOWNLOAD_CV_PDF_BY_ID_PATH = "/{id}/cv.pdf";
    private static final String CREATE_CANDIDATE_FOLDER_BY_ID_PATH = "/{id}/create-folder";
    private static final String UPDATE_SALESFORCE_BY_ID_PATH = "/{id}/update-sf";
    private static final String UPDATE_SALESFORCE_PATH = "/update-sf";
    private static final String UPDATE_SALESFORCE_BY_LIST_PATH = "/update-sf-by-list";
    private static final String UPDATE_INTAKE_DATA_BY_ID_PATH = "/{id}/intake";
    private static final String RESOLVE_TASKS_PATH = "/resolve-tasks";
    private static final String GENERATE_TOKEN_PATH = "/token/{cn}";

    private final Page<Candidate> candidates =
            new PageImpl<>(
                    listOfCandidates(),
                    PageRequest.of(0,10, Sort.unsorted()),
                    listOfCandidates().size()
            );

    private final Candidate candidate = getCandidate();

    @MockBean CandidateService candidateService;
    @MockBean CandidateSavedListService candidateSavedListService;
    @MockBean SavedListService savedListService;
    @MockBean SavedSearchService savedSearchService;
    @MockBean UserService userService;
    @MockBean CandidateTokenProvider candidateTokenProvider;

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired CandidateAdminApi candidateAdminApi;

    @BeforeEach
    void setUp() {
        configureAuthentication();
    }

    @Test
    public void testWebOnlyContextLoads() {
        assertThat(candidateAdminApi).isNotNull();
    }

    @Test
    @DisplayName("search succeeds")
    void searchSucceeds() throws Exception {
        SearchCandidateRequest request = new SearchCandidateRequest();

        given(savedSearchService
                .searchCandidates(any(SearchCandidateRequest.class)))
                .willReturn(candidates);

        postSearchRequestAndVerifyResponse(SEARCH_PATH, objectMapper.writeValueAsString(request));
        verify(savedSearchService).searchCandidates(any(SearchCandidateRequest.class));
    }

    @Test
    @DisplayName("find by email succeeds")
    void findByEmailSucceeds() throws Exception {
        CandidateEmailSearchRequest request = new CandidateEmailSearchRequest();

        given(candidateService
                .searchCandidates(any(CandidateEmailSearchRequest.class)))
                .willReturn(candidates);

        postSearchRequestAndVerifyResponse(FIND_BY_EMAIL_PATH, objectMapper.writeValueAsString(request));
        verify(candidateService).searchCandidates(any(CandidateEmailSearchRequest.class));
    }

    @Test
    @DisplayName("find by email or phone succeeds")
    void findByEmailOrPhoneSucceeds() throws Exception {
        CandidateEmailOrPhoneSearchRequest request = new CandidateEmailOrPhoneSearchRequest();

        given(candidateService
                .searchCandidates(any(CandidateEmailOrPhoneSearchRequest.class)))
                .willReturn(candidates);

        postSearchRequestAndVerifyResponse(FIND_BY_EMAIL_OR_PHONE_PATH, objectMapper.writeValueAsString(request));
        verify(candidateService).searchCandidates(any(CandidateEmailOrPhoneSearchRequest.class));
    }

    @Test
    @DisplayName("find by number or name succeeds")
    void findByNumberOrNameSucceeds() throws Exception {
        CandidateNumberOrNameSearchRequest request = new CandidateNumberOrNameSearchRequest();

        given(candidateService
                .searchCandidates(any(CandidateNumberOrNameSearchRequest.class)))
                .willReturn(candidates);

        postSearchRequestAndVerifyResponse(FIND_BY_NUMBER_OR_NAME_PATH, objectMapper.writeValueAsString(request));
        verify(candidateService).searchCandidates(any(CandidateNumberOrNameSearchRequest.class));
    }

    @Test
    @DisplayName("find by external id succeeds")
    void findByExternalIdSucceeds() throws Exception {
        CandidateExternalIdSearchRequest request = new CandidateExternalIdSearchRequest();

        given(candidateService
                .searchCandidates(any(CandidateExternalIdSearchRequest.class)))
                .willReturn(candidates);

        postSearchRequestAndVerifyResponse(FIND_BY_EXTERNAL_ID_PATH, objectMapper.writeValueAsString(request));
        verify(candidateService).searchCandidates(any(CandidateExternalIdSearchRequest.class));
    }

    @Test
    @DisplayName("get by number succeeds")
    void getByNumberSucceeds() throws Exception {
        String number = "99";

        given(candidateService
                .findByCandidateNumberRestricted(anyString()))
                .willReturn(candidate);

        getCandidateAndVerifyResponse(GET_BY_NUMBER_PATH.replace("{number}", number));

        verify(candidateService).findByCandidateNumberRestricted(anyString());
    }

    @Test
    @DisplayName("get by id succeeds")
    void getByIdSucceeds() throws Exception {
        long id = 99L;

        given(candidateService
                .getCandidate(anyLong()))
                .willReturn(candidate);

        getCandidateAndVerifyResponse(GET_BY_ID_PATH.replace("{id}", Long.toString(id)));

        verify(candidateService).getCandidate(anyLong());
    }

    @Test
    @DisplayName("get intake data by id succeeds")
    void getIntakeDataByIdSucceeds() throws Exception {
        long id = 99L;

        given(candidateService
                .getCandidate(anyLong()))
                .willReturn(candidate);

        given(candidateService
                .addMissingDestinations(any(Candidate.class)))
                .willReturn(addMissingDestinations(candidate));

        getIntakeDataAndVerifyResponse(GET_INTAKE_DATA_BY_ID_PATH.replace("{id}", Long.toString(id)));

        verify(candidateService).getCandidate(anyLong());
    }

    @Test
    @DisplayName("update links by id succeeds")
    void updateLinksByIdSucceeds() throws Exception {
        long id = 99L;
        UpdateCandidateLinksRequest request = new UpdateCandidateLinksRequest();

        given(candidateService
                .updateCandidateLinks(anyLong(), any(UpdateCandidateLinksRequest.class)))
                .willReturn(candidate);

        updateCandidateAndVerifyResponse(
                UPDATE_LINKS_BY_ID_PATH.replace("{id}", Long.toString(id)),
                objectMapper.writeValueAsString(request));

        verify(candidateService).updateCandidateLinks(anyLong(), any(UpdateCandidateLinksRequest.class));
    }

    private void postSearchRequestAndVerifyResponse(String path, String body) throws Exception {
        mockMvc.perform(post(BASE_PATH + path)
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements", is(listOfCandidates().size())))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.number", is(0)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)))
                .andExpect(jsonPath("$.content", notNullValue()));
    }

    private void getCandidateAndVerifyResponse(String path) throws Exception {
        mockMvc.perform(get(BASE_PATH + path)
                        .header("Authorization", "Bearer " + "jwt-token")
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(99)))
                .andExpect(jsonPath("$.nationality.name", is("Pakistan")));
    }

    private Candidate addMissingDestinations(Candidate candidate) {
        CandidateDestination uk = new CandidateDestination();
        uk.setCountry(new Country("UK", Status.active));

        CandidateDestination canada = new CandidateDestination();
        canada.setCountry(new Country("Canada", Status.active));

        candidate.setCandidateDestinations(new ArrayList<>(List.of(uk, canada)));
        candidate.setCandidateVisaChecks(new ArrayList<>(List.of(new CandidateVisaCheck())));

        return candidate;
    }

    private void getIntakeDataAndVerifyResponse(String path) throws Exception {
        mockMvc.perform(get(BASE_PATH + path)
                        .header("Authorization", "Bearer " + "jwt-token")
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.candidateDestinations").isArray())
                .andExpect(jsonPath("$.candidateDestinations", hasSize(2)))
                .andExpect(jsonPath("$.candidateDestinations[0].country.name", is("Canada")))
                .andExpect(jsonPath("$.candidateDestinations[1].country.name", is("UK")));
    }

    private void updateCandidateAndVerifyResponse(String path, String body) throws Exception {
        mockMvc.perform(put(BASE_PATH + path)
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(99)))
                .andExpect(jsonPath("$.nationality.name", is("Pakistan")));
    }

}
