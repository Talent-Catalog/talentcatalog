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
import org.tctalent.server.data.CandidateTestData;
import org.tctalent.server.model.db.CandidateCertification;
import org.tctalent.server.request.candidate.certification.CreateCandidateCertificationRequest;
import org.tctalent.server.request.candidate.certification.UpdateCandidateCertificationRequest;
import org.tctalent.server.service.db.CandidateCertificationService;

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
 * Unit tests for Candidate Certification Admin Api endpoints.
 *
 * @author sadatmalik
 */
@WebMvcTest(CandidateCertificationAdminApi.class)
@AutoConfigureMockMvc
class CandidateCertificationAdminApiTest extends ApiTestBase {
    private static final String BASE_PATH = "/api/admin/candidate-certification";
    private static final String GET_CERTIFICATION_LIST_BY_ID_PATH = "/{id}/list";
    private static final long CANDIDATE_ID = 99L;

    private final CandidateCertification candidateCertification =
        CandidateTestData.getCandidateCertification();
    private final List<CandidateCertification> candidateCertificationList =
        CandidateTestData.getListOfCandidateCertifications();

    @MockBean CandidateCertificationService candidateCertificationService;

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired CandidateCertificationAdminApi candidateCertificationAdminApi;

    @BeforeEach
    void setUp() {
        configureAuthentication();
    }

    @Test
    public void testWebOnlyContextLoads() {
        assertThat(candidateCertificationAdminApi).isNotNull();
    }

    @Test
    @DisplayName("get list of certifications by id succeeds")
    void getCertificationListByIdSucceeds() throws Exception {

        given(candidateCertificationService
                .list(anyLong()))
                .willReturn(candidateCertificationList);

        mockMvc.perform(get(BASE_PATH + GET_CERTIFICATION_LIST_BY_ID_PATH.replace("{id}", String.valueOf(CANDIDATE_ID)))
                        .header("Authorization", "Bearer " + "jwt-token")
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].institution", is("Cambridge")))
                .andExpect(jsonPath("$.[0].dateCompleted", is("1998-05-01")))
                .andExpect(jsonPath("$.[0].name", is("BA")));

        verify(candidateCertificationService).list(CANDIDATE_ID);
    }

    @Test
    @DisplayName("create candidate certification succeeds")
    void createCertificationSucceeds() throws Exception {
        CreateCandidateCertificationRequest request = new CreateCandidateCertificationRequest();

        given(candidateCertificationService
                .createCandidateCertification(any(CreateCandidateCertificationRequest.class)))
                .willReturn(candidateCertification);

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
                .andExpect(jsonPath("$.dateCompleted", is("1998-05-01")))
                .andExpect(jsonPath("$.name", is("BA")));

        verify(candidateCertificationService).createCandidateCertification(any(CreateCandidateCertificationRequest.class));
    }

    @Test
    @DisplayName("update candidate certification succeeds")
    void updateCertificationSucceeds() throws Exception {
        UpdateCandidateCertificationRequest request = new UpdateCandidateCertificationRequest();

        given(candidateCertificationService
                .updateCandidateCertification(any(UpdateCandidateCertificationRequest.class)))
                .willReturn(candidateCertification);

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
                .andExpect(jsonPath("$.dateCompleted", is("1998-05-01")))
                .andExpect(jsonPath("$.name", is("BA")));

        verify(candidateCertificationService).updateCandidateCertification(any(UpdateCandidateCertificationRequest.class));
    }

    @Test
    @DisplayName("delete candidate certification by id succeeds")
    void deleteCertificationByIdSucceeds() throws Exception {
        mockMvc.perform(delete(BASE_PATH + "/" + CANDIDATE_ID)
                .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token"))

                .andExpect(status().isOk());

        verify(candidateCertificationService).deleteCandidateCertification(anyLong());
    }
}
