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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.tctalent.server.data.CandidateTestData.getCandidateVisaCheck;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.tctalent.server.model.db.CandidateVisaCheck;
import org.tctalent.server.request.candidate.visa.CandidateVisaCheckData;
import org.tctalent.server.request.candidate.visa.CreateCandidateVisaCheckRequest;
import org.tctalent.server.service.db.CandidateVisaService;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.service.db.OccupationService;

/**
 *  Unit tests for Candidate Visa Check Admin Api endpoints.
 *
 * @author Caroline Cameron
 */
@WebMvcTest(CandidateVisaCheckAdminApi.class)
@AutoConfigureMockMvc
public class CandidateVisaCheckAdminApiTest extends ApiTestBase {
    private static final long CANDIDATE_ID = 99L;
    private static final String BASE_PATH = "/api/admin/candidate-visa-check";
    private static final String LIST_PATH = "/{id}/list";
    private static final String UPDATE_INTAKE_PATH = "/{id}/intake";

    private static final CandidateVisaCheck candidateVisaCheck = getCandidateVisaCheck(false);
    private static final CandidateVisaCheck candidateVisaCheckComplete = getCandidateVisaCheck(true);

    @MockBean
    CandidateVisaService candidateVisaService;
    @MockBean
    CountryService countryService;
    @MockBean
    OccupationService occupationService;

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired CandidateVisaCheckAdminApi candidateVisaCheckAdminApi;

    @BeforeEach
    void setUp() {
        configureAuthentication();
    }

    @Test
    public void testWebOnlyContextLoads() {
        assertThat(candidateVisaCheckAdminApi).isNotNull();
    }

    @Test
    @DisplayName("get by id succeeds")
    void getByIdSucceeds() throws Exception {
        given(candidateVisaService
                .getVisaCheck(anyLong()))
                .willReturn(candidateVisaCheckComplete);

        mockMvc.perform(get(BASE_PATH + "/" + CANDIDATE_ID)
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.candidateVisaJobChecks").isArray())
                .andExpect(jsonPath("$.country.name", is("Australia")))
                .andExpect(jsonPath("$.protection", is("Yes")))
                .andExpect(jsonPath("$.protectionGrounds", is("These are some protection grounds.")))
                .andExpect(jsonPath("$.englishThreshold", is("No")))
                .andExpect(jsonPath("$.englishThresholdNotes", is("These are some english threshold notes.")))
                .andExpect(jsonPath("$.healthAssessment", is("Yes")))
                .andExpect(jsonPath("$.healthAssessmentNotes", is("These are some health assessment notes.")))
                .andExpect(jsonPath("$.characterAssessment", is("No")))
                .andExpect(jsonPath("$.characterAssessmentNotes", is("These are some character assessment notes.")))
                .andExpect(jsonPath("$.securityRisk", is("Yes")))
                .andExpect(jsonPath("$.securityRiskNotes", is("These are some security risk notes.")))
                .andExpect(jsonPath("$.overallRisk", is("Medium")))
                .andExpect(jsonPath("$.overallRiskNotes", is("These are some overall risk notes.")))
                .andExpect(jsonPath("$.validTravelDocs", is("Valid")))
                .andExpect(jsonPath("$.validTravelDocsNotes", is("These are some travel docs notes.")))
                .andExpect(jsonPath("$.pathwayAssessment", is("No")))
                .andExpect(jsonPath("$.pathwayAssessmentNotes", is("These are some pathway assessment notes.")))
                .andExpect(jsonPath("$.destinationFamily", is("Cousin")))
                .andExpect(jsonPath("$.destinationFamilyLocation", is("New York")));

        verify(candidateVisaService).getVisaCheck(anyLong());
    }

    @Test
    @DisplayName("list by candidate id succeeds")
    void listByCandidateIdSucceeds() throws Exception {
        given(candidateVisaService
                .listCandidateVisaChecks(CANDIDATE_ID))
                .willReturn(List.of(candidateVisaCheckComplete));

        mockMvc.perform(get(BASE_PATH + LIST_PATH.replace("{id}", Long.toString(CANDIDATE_ID)))
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.[0].id", notNullValue()))
                .andExpect(jsonPath("$.[0].candidateVisaJobChecks").isArray())
                .andExpect(jsonPath("$.[0].country.name", is("Australia")))
                .andExpect(jsonPath("$.[0].protection", is("Yes")))
                .andExpect(jsonPath("$.[0].protectionGrounds", is("These are some protection grounds.")))
                .andExpect(jsonPath("$.[0].englishThreshold", is("No")))
                .andExpect(jsonPath("$.[0].englishThresholdNotes", is("These are some english threshold notes.")))
                .andExpect(jsonPath("$.[0].healthAssessment", is("Yes")))
                .andExpect(jsonPath("$.[0].healthAssessmentNotes", is("These are some health assessment notes.")))
                .andExpect(jsonPath("$.[0].characterAssessment", is("No")))
                .andExpect(jsonPath("$.[0].characterAssessmentNotes", is("These are some character assessment notes.")))
                .andExpect(jsonPath("$.[0].securityRisk", is("Yes")))
                .andExpect(jsonPath("$.[0].securityRiskNotes", is("These are some security risk notes.")))
                .andExpect(jsonPath("$.[0].overallRisk", is("Medium")))
                .andExpect(jsonPath("$.[0].overallRiskNotes", is("These are some overall risk notes.")))
                .andExpect(jsonPath("$.[0].validTravelDocs", is("Valid")))
                .andExpect(jsonPath("$.[0].validTravelDocsNotes", is("These are some travel docs notes.")))
                .andExpect(jsonPath("$.[0].pathwayAssessment", is("No")))
                .andExpect(jsonPath("$.[0].pathwayAssessmentNotes", is("These are some pathway assessment notes.")))
                .andExpect(jsonPath("$.[0].destinationFamily", is("Cousin")))
                .andExpect(jsonPath("$.[0].destinationFamilyLocation", is("New York")));

        verify(candidateVisaService).listCandidateVisaChecks(CANDIDATE_ID);
    }

    @Test
    @DisplayName("update intake data succeeds")
    void updateIntakeDataSucceeds() throws Exception {
        CandidateVisaCheckData request = new CandidateVisaCheckData();
        String visaId = "99";

        mockMvc.perform(put(BASE_PATH + UPDATE_INTAKE_PATH.replace("{id}", visaId))
                        .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk());

        verify(candidateVisaService).updateIntakeData(anyLong(), any(CandidateVisaCheckData.class));
    }

    @Test
    @DisplayName("create visa check succeeds")
    void createVisaCheckSucceeds() throws Exception {
        CreateCandidateVisaCheckRequest request = new CreateCandidateVisaCheckRequest();
        String visaId = "99";

        given(candidateVisaService
                .createVisaCheck(anyLong(), any(CreateCandidateVisaCheckRequest.class)))
                .willReturn(candidateVisaCheck);

        mockMvc.perform(post(BASE_PATH + "/" + visaId)
                        .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.country.name", is("Australia")))
                .andExpect(jsonPath("$.candidateVisaJobChecks").isArray());

        verify(candidateVisaService).createVisaCheck(anyLong(), any(CreateCandidateVisaCheckRequest.class));
    }

    @Test
    @DisplayName("delete visa check by id succeeds")
    void deleteByIdSucceeds() throws Exception {
        String visaId = "99";
        mockMvc.perform(delete(BASE_PATH + "/" + visaId)
                        .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token"))

                .andDo(print())
                .andExpect(status().isOk());

        verify(candidateVisaService).deleteVisaCheck(anyLong());
    }
}
