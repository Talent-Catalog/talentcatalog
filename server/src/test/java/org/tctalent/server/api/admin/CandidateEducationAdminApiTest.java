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
import org.tctalent.server.model.db.CandidateEducation;
import org.tctalent.server.request.candidate.education.CreateCandidateEducationRequest;
import org.tctalent.server.request.candidate.education.UpdateCandidateEducationRequest;
import org.tctalent.server.service.db.CandidateEducationService;

import java.util.List;

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

/**
 * Unit tests for Candidate Education Admin Api endpoints.
 *
 * @author sadatmalik
 */
@WebMvcTest(CandidateEducationAdminApi.class)
@AutoConfigureMockMvc
class CandidateEducationAdminApiTest extends ApiTestBase {
    private static final String BASE_PATH = "/api/admin/candidate-education";
    private static final String GET_EDUCATION_LIST_BY_ID_PATH = "/{id}/list";
    private static final long CANDIDATE_ID = 99L;

    private final CandidateEducation candidateEducation = AdminApiTestUtil.getCandidateEducation();
    private final List<CandidateEducation> candidateEducationList = AdminApiTestUtil.getListOfCandidateEducations();

    @MockBean CandidateEducationService candidateEducationService;

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired CandidateEducationAdminApi candidateEducationAdminApi;

    @BeforeEach
    void setUp() {
        configureAuthentication();
    }

    @Test
    public void testWebOnlyContextLoads() {
        assertThat(candidateEducationAdminApi).isNotNull();
    }

    @Test
    @DisplayName("get list of educations by id succeeds")
    void getEducationsListByIdSucceeds() throws Exception {

        given(candidateEducationService
                .list(anyLong()))
                .willReturn(candidateEducationList);

        mockMvc.perform(get(BASE_PATH + GET_EDUCATION_LIST_BY_ID_PATH.replace("{id}", String.valueOf(CANDIDATE_ID)))
                        .header("Authorization", "Bearer " + "jwt-token")
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].institution", is("Cambridge")))
                .andExpect(jsonPath("$.[0].courseName", is("Computer Science")))
                .andExpect(jsonPath("$.[0].educationMajor.name", is("MA")))
                .andExpect(jsonPath("$.[0].educationMajor.status", is("active")))
                .andExpect(jsonPath("$.[0].educationType", is("Masters")))
                .andExpect(jsonPath("$.[0].lengthOfCourseYears", is(4)))
                .andExpect(jsonPath("$.[0].yearCompleted", is(1998)))
                .andExpect(jsonPath("$.[0].country.name", is("UK")))
                .andExpect(jsonPath("$.[0].country.status", is("active")))
                .andExpect(jsonPath("$.[0].incomplete", is(false)));

        verify(candidateEducationService).list(CANDIDATE_ID);
    }

    @Test
    @DisplayName("create candidate education succeeds")
    void createEducationSucceeds() throws Exception {
        CreateCandidateEducationRequest request = new CreateCandidateEducationRequest();

        given(candidateEducationService
                .createCandidateEducation(any(CreateCandidateEducationRequest.class)))
                .willReturn(candidateEducation);

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
                .andExpect(jsonPath("$.institution", is("Cambridge")))
                .andExpect(jsonPath("$.courseName", is("Computer Science")))
                .andExpect(jsonPath("$.educationMajor.name", is("MA")))
                .andExpect(jsonPath("$.educationMajor.status", is("active")))
                .andExpect(jsonPath("$.educationType", is("Masters")))
                .andExpect(jsonPath("$.lengthOfCourseYears", is(4)))
                .andExpect(jsonPath("$.yearCompleted", is(1998)))
                .andExpect(jsonPath("$.country.name", is("UK")))
                .andExpect(jsonPath("$.country.status", is("active")))
                .andExpect(jsonPath("$.incomplete", is(false)));

        verify(candidateEducationService).createCandidateEducation(any(CreateCandidateEducationRequest.class));
    }

    @Test
    @DisplayName("update candidate education succeeds")
    void updateEducationSucceeds() throws Exception {
        UpdateCandidateEducationRequest request = new UpdateCandidateEducationRequest();

        given(candidateEducationService
                .updateCandidateEducation(any(UpdateCandidateEducationRequest.class)))
                .willReturn(candidateEducation);

        mockMvc.perform(put(BASE_PATH)
                        .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.institution", is("Cambridge")))
                .andExpect(jsonPath("$.courseName", is("Computer Science")))
                .andExpect(jsonPath("$.educationMajor.name", is("MA")))
                .andExpect(jsonPath("$.educationMajor.status", is("active")))
                .andExpect(jsonPath("$.educationType", is("Masters")))
                .andExpect(jsonPath("$.lengthOfCourseYears", is(4)))
                .andExpect(jsonPath("$.yearCompleted", is(1998)))
                .andExpect(jsonPath("$.country.name", is("UK")))
                .andExpect(jsonPath("$.country.status", is("active")))
                .andExpect(jsonPath("$.incomplete", is(false)));

        verify(candidateEducationService).updateCandidateEducation(any(UpdateCandidateEducationRequest.class));
    }

    @Test
    @DisplayName("delete candidate education by id succeeds")
    void deleteEducationByIdSucceeds() throws Exception {
        mockMvc.perform(delete(BASE_PATH + "/" + CANDIDATE_ID)
                        .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token"))

                .andDo(print())
                .andExpect(status().isOk());

        verify(candidateEducationService).deleteCandidateEducation(CANDIDATE_ID);
    }

}
