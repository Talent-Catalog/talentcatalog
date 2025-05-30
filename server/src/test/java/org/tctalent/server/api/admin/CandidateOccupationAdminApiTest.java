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
import static org.tctalent.server.data.CandidateTestData.getCandidateOccupation;
import static org.tctalent.server.data.CandidateTestData.getListOfCandidateOccupations;
import static org.tctalent.server.data.CandidateTestData.getListOfOccupations;

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
import org.tctalent.server.model.db.CandidateOccupation;
import org.tctalent.server.model.db.Occupation;
import org.tctalent.server.request.candidate.occupation.CreateCandidateOccupationRequest;
import org.tctalent.server.request.candidate.occupation.UpdateCandidateOccupationRequest;
import org.tctalent.server.service.db.CandidateOccupationService;
import org.tctalent.server.service.db.OccupationService;
import org.tctalent.server.util.dto.DtoBuilder;

/**
 * Unit tests for Candidate Occupation Admin Api endpoints.
 *
 * @author sadatmalik
 */
@WebMvcTest(CandidateOccupationAdminApi.class)
@AutoConfigureMockMvc
class CandidateOccupationAdminApiTest extends ApiTestBase {

    private static final long CANDIDATE_ID = 99L;

    private static final String BASE_PATH = "/api/admin/candidate-occupation";
    private static final String GET_ALL_OCCUPATIONS_PATH = "/occupation";
    private static final String GET_CANDIDATE_OCCUPATIONS_BY_ID_PATH = "/{id}/list";

    private static final List<Occupation> occupationsList = getListOfOccupations();
    private static final CandidateOccupation candidateOccupation = getCandidateOccupation();
    private static final List<CandidateOccupation> candidateOccupationsList = getListOfCandidateOccupations();

    @MockBean OccupationService occupationService;
    @MockBean CandidateOccupationService candidateOccupationService;

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired CandidateOccupationAdminApi candidateOccupationAdminApi;

    @BeforeEach
    void setUp() {
        configureAuthentication();
    }

    @Test
    public void testWebOnlyContextLoads() {
        assertThat(candidateOccupationAdminApi).isNotNull();
    }

    @Test
    @DisplayName("get all occupations succeeds")
    void getAllOccupationsSucceeds() throws Exception {

        given(candidateOccupationService
                .listOccupations())
                .willReturn(occupationsList);
        given(occupationService
                .selectBuilder())
                .willReturn(new DtoBuilder().add("name"));

        mockMvc.perform(get(BASE_PATH + GET_ALL_OCCUPATIONS_PATH)
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$.[0].name", is("Builder")))
                .andExpect(jsonPath("$.[1].name", is("Baker")));

        verify(candidateOccupationService).listOccupations();
    }

    @Test
    @DisplayName("get candidate occupations by id succeeds")
    void getCandidateOccupationsByIdSucceeds() throws Exception {

        given(candidateOccupationService
                .listCandidateOccupations(anyLong()))
                .willReturn(candidateOccupationsList);

        mockMvc.perform(get(BASE_PATH + GET_CANDIDATE_OCCUPATIONS_BY_ID_PATH.replace("{id}", String.valueOf(CANDIDATE_ID)))
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].occupation.name", is("Software Engineer")))
                .andExpect(jsonPath("$.[0].yearsExperience", is(10)));

        verify(candidateOccupationService).listCandidateOccupations(anyLong());
    }

    @Test
    @DisplayName("create candidate occupation by candidate id succeeds")
    void createCandidateOccupationByCandidateIdSucceeds() throws Exception {
        CreateCandidateOccupationRequest request = new CreateCandidateOccupationRequest();
        request.setOccupationId(1L);
        request.setYearsExperience(10L);

        given(candidateOccupationService
                .createCandidateOccupation(any(CreateCandidateOccupationRequest.class)))
                .willReturn(candidateOccupation);

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
                .andExpect(jsonPath("$.occupation.name", is("Software Engineer")))
                .andExpect(jsonPath("$.yearsExperience", is(10)));

        verify(candidateOccupationService).createCandidateOccupation(any(CreateCandidateOccupationRequest.class));
    }

    @Test
    @DisplayName("update candidate occupation by id succeeds")
    void updateCandidateOccupationByIdSucceeds() throws Exception {
        UpdateCandidateOccupationRequest request = new UpdateCandidateOccupationRequest();

        given(candidateOccupationService
                .updateCandidateOccupation(any(UpdateCandidateOccupationRequest.class)))
                .willReturn(candidateOccupation);

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
                .andExpect(jsonPath("$.occupation.name", is("Software Engineer")))
                .andExpect(jsonPath("$.yearsExperience", is(10)));

        verify(candidateOccupationService).updateCandidateOccupation(any(UpdateCandidateOccupationRequest.class));
    }

    @Test
    @DisplayName("delete candidate occupation by id succeeds")
    void deleteCandidateOccupationByIdSucceeds() throws Exception {
        mockMvc.perform(delete(BASE_PATH + "/" + CANDIDATE_ID)
                        .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token"))

                .andExpect(status().isOk());

        verify(candidateOccupationService).deleteCandidateOccupation(CANDIDATE_ID);
    }
}
