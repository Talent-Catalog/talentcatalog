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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.tctalent.server.data.CandidateTestData.getCandidateCitizenship;

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
import org.tctalent.server.model.db.CandidateCitizenship;
import org.tctalent.server.request.candidate.citizenship.CreateCandidateCitizenshipRequest;
import org.tctalent.server.service.db.CandidateCitizenshipService;
import org.tctalent.server.service.db.CountryService;

/**
 * Unit tests for Candidate Citizenship Admin Api endpoints.
 *
 * @author sadatmalik
 */
@WebMvcTest(CandidateCitizenshipAdminApi.class)
@AutoConfigureMockMvc
class CandidateCitizenshipAdminApiTest extends ApiTestBase {
    private static final String BASE_PATH = "/api/admin/candidate-citizenship";

    private static final long CANDIDATE_ID = 99L;

    private final CandidateCitizenship candidateCitizenship = getCandidateCitizenship();


    @MockBean
    CandidateCitizenshipService candidateCitizenshipService;
    @MockBean
    CountryService countryService;

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired CandidateCitizenshipAdminApi candidateCitizenshipAdminApi;

    @BeforeEach
    void setUp() {
        configureAuthentication();
    }

    @Test
    public void testWebOnlyContextLoads() {
        assertThat(candidateCitizenshipAdminApi).isNotNull();
    }

    @Test
    @DisplayName("create candidate citizenship succeeds")
    void createCitizenshipSucceeds() throws Exception {
        CreateCandidateCitizenshipRequest request = new CreateCandidateCitizenshipRequest();

        given(candidateCitizenshipService
                .createCitizenship(anyLong(), any(CreateCandidateCitizenshipRequest.class)))
                .willReturn(candidateCitizenship);

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
                .andExpect(jsonPath("$.nationality.name", is("Pakistan")))
                .andExpect(jsonPath("$.hasPassport", is("ValidPassport")))
                .andExpect(jsonPath("$.notes", is("Some example citizenship notes")));

        verify(candidateCitizenshipService).createCitizenship(anyLong(), any(CreateCandidateCitizenshipRequest.class));
    }

    @Test
    @DisplayName("delete candidate citizenship succeeds")
    void deleteCitizenshipByIdSucceeds() throws Exception {
        mockMvc.perform(delete(BASE_PATH + "/" + CANDIDATE_ID)
                .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token"))

                .andExpect(status().isOk());

        verify(candidateCitizenshipService).deleteCitizenship(anyLong());
    }
}
