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
import org.tctalent.server.model.db.CandidateReviewStatusItem;
import org.tctalent.server.model.db.ReviewStatus;
import org.tctalent.server.request.reviewstatus.CreateCandidateReviewStatusRequest;
import org.tctalent.server.request.reviewstatus.UpdateCandidateReviewStatusRequest;
import org.tctalent.server.service.db.CandidateReviewStatusService;

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

/**
 * Unit tests for Candidate Review Status Admin Api endpoints.
 *
 * @author sadatmalik
 */
@WebMvcTest(CandidateReviewStatusAdminApi.class)
@AutoConfigureMockMvc
class CandidateReviewStatusAdminApiTest extends ApiTestBase {

    private static final long CANDIDATE_ID = 99L;

    private static final String BASE_PATH = "/api/admin/candidate-reviewstatus";

    private static final CandidateReviewStatusItem reviewStatusItem = AdminApiTestUtil.getCandidateReviewStatusItem();

    @MockBean CandidateReviewStatusService candidateReviewStatusService;

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired CandidateReviewStatusAdminApi candidateReviewStatusAdminApi;

    @BeforeEach
    void setUp() {
        configureAuthentication();
    }

    @Test
    public void testWebOnlyContextLoads() {
        assertThat(candidateReviewStatusAdminApi).isNotNull();
    }

    @Test
    @DisplayName("get candidate review status succeeds")
    void getCandidateReviewStatusSucceeds() throws Exception {
        given(candidateReviewStatusService
                .getCandidateReviewStatusItem(anyLong()))
                .willReturn(reviewStatusItem);

        mockMvc.perform(get(BASE_PATH + "/" + CANDIDATE_ID)
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.reviewStatus", is("verified")))
                .andExpect(jsonPath("$.comment", is("A review comment")));

        verify(candidateReviewStatusService).getCandidateReviewStatusItem(CANDIDATE_ID);
    }

    @Test
    @DisplayName("create candidate review status succeeds")
    void createCandidateReviewStatusSucceeds() throws Exception {
        CreateCandidateReviewStatusRequest request = new CreateCandidateReviewStatusRequest();
        request.setCandidateId(1L);
        request.setSavedSearchId(2L);
        request.setReviewStatus(ReviewStatus.verified);

        given(candidateReviewStatusService
                .createCandidateReviewStatusItem(any(CreateCandidateReviewStatusRequest.class)))
                .willReturn(reviewStatusItem);

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
                .andExpect(jsonPath("$.reviewStatus", is("verified")))
                .andExpect(jsonPath("$.comment", is("A review comment")));

        verify(candidateReviewStatusService).createCandidateReviewStatusItem(any(CreateCandidateReviewStatusRequest.class));
    }

    @Test
    @DisplayName("update candidate review status by id succeeds")
    void updateCandidateReviewStatusByIdSucceeds() throws Exception {
        UpdateCandidateReviewStatusRequest request = new UpdateCandidateReviewStatusRequest();
        request.setCandidateReviewStatusId(1L);
        request.setReviewStatus(ReviewStatus.verified);

        given(candidateReviewStatusService
                .updateCandidateReviewStatusItem(anyLong(), any(UpdateCandidateReviewStatusRequest.class)))
                .willReturn(reviewStatusItem);

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
                .andExpect(jsonPath("$.reviewStatus", is("verified")))
                .andExpect(jsonPath("$.comment", is("A review comment")));

        verify(candidateReviewStatusService).updateCandidateReviewStatusItem(anyLong(), any(UpdateCandidateReviewStatusRequest.class));
    }

}
