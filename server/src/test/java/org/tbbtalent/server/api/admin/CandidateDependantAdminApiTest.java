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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.CandidateDependant;
import org.tbbtalent.server.request.candidate.dependant.CreateCandidateDependantRequest;
import org.tbbtalent.server.service.db.CandidateDependantService;
import org.tbbtalent.server.service.db.CandidateService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.tbbtalent.server.api.admin.AdminApiTestUtil.getCandidate;
import static org.tbbtalent.server.api.admin.AdminApiTestUtil.getCandidateDependant;

/**
 * Unit tests for Candidate Dependant Admin Api endpoints.
 *
 * @author sadatmalik
 */
@WebMvcTest(CandidateDependantAdminApi.class)
@AutoConfigureMockMvc
class CandidateDependantAdminApiTest extends ApiTestBase {
    private static final String BASE_PATH = "/api/admin/candidate-dependant";
    private static final long CANDIDATE_ID = 99L;

    private final Candidate candidate = getCandidate();
    private final CandidateDependant candidateDependant = getCandidateDependant();

    @MockBean CandidateDependantService candidateDependantService;
    @MockBean CandidateService candidateService;

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired CandidateDependantAdminApi candidateDependantAdminApi;

    @BeforeEach
    void setUp() {
        configureAuthentication();
    }

    @Test
    public void testWebOnlyContextLoads() {
        assertThat(candidateDependantAdminApi).isNotNull();
    }

    @Test
    @DisplayName("create candidate dependant succeeds")
    void createDependantSucceeds() throws Exception {
        CreateCandidateDependantRequest request = new CreateCandidateDependantRequest();

        given(candidateDependantService
                .createDependant(anyLong(), any(CreateCandidateDependantRequest.class)))
                .willReturn(candidateDependant);

        mockMvc.perform(post(BASE_PATH + "/" + CANDIDATE_ID)
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.name", is("Ahmad Fatah")))
                .andExpect(jsonPath("$.dob", is("1998-01-01")))
                .andExpect(jsonPath("$.relation", is("Partner")))
                .andExpect(jsonPath("$.relationOther", is("Husband")))
                .andExpect(jsonPath("$.registered", is("UNHCR")))
                .andExpect(jsonPath("$.registeredNumber", is("123456")))
                .andExpect(jsonPath("$.registeredNotes", is("Some dependant registration notes")))
                .andExpect(jsonPath("$.healthConcern", is("No")))
                .andExpect(jsonPath("$.healthNotes", is("Some dependant health notes")));

        verify(candidateDependantService).createDependant(anyLong(), any(CreateCandidateDependantRequest.class));
    }

    @Test
    @DisplayName("delete candidate citizenship succeeds")
    void deleteCitizenshipByIdSucceeds() throws Exception {
        given(candidateDependantService
                .deleteDependant(anyLong()))
                .willReturn(candidate);

        mockMvc.perform(delete(BASE_PATH + "/" + CANDIDATE_ID)
                        .header("Authorization", "Bearer " + "jwt-token"))

                .andDo(print())
                .andExpect(status().isOk());

        verify(candidateDependantService).deleteDependant(anyLong());
        verify(candidateService).save(candidate, true);
    }

}
