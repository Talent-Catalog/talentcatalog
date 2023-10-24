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
import org.tbbtalent.server.model.db.CandidateVisaCheck;
import org.tbbtalent.server.service.db.CandidateVisaService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.tbbtalent.server.api.admin.AdminApiTestUtil.getCandidateVisaCheck;

/**
 * TODO CC doc
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

    private static final CandidateVisaCheck candidateVisaCheck = getCandidateVisaCheck();

    @MockBean CandidateVisaService candidateVisaService;

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
                .willReturn(candidateVisaCheck);

        mockMvc.perform(get(BASE_PATH + "/" + CANDIDATE_ID)
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.country.name", is("Australia")))
                .andExpect(jsonPath("$.candidateVisaJobChecks").isArray());

        verify(candidateVisaService).getVisaCheck(anyLong());
    }

    @Test
    @DisplayName("list by candidate id succeeds")
    void listByCandidateIdSucceeds() throws Exception {
        given(candidateVisaService
                .listCandidateVisaChecks(CANDIDATE_ID))
                .willReturn(List.of(candidateVisaCheck));

        mockMvc.perform(get(BASE_PATH + LIST_PATH.replace("{id}", Long.toString(CANDIDATE_ID)))
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.[0].country.name", is("Australia")));

        verify(candidateVisaService).listCandidateVisaChecks(CANDIDATE_ID);
    }
}
