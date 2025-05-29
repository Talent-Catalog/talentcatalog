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
import org.tctalent.server.model.db.CandidateLanguage;
import org.tctalent.server.request.candidate.language.CreateCandidateLanguageRequest;
import org.tctalent.server.request.candidate.language.UpdateCandidateLanguageRequest;
import org.tctalent.server.service.db.CandidateLanguageService;

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
 * Unit tests for Candidate Language Admin Api endpoints.
 *
 * @author sadatmalik
 */
@WebMvcTest(CandidateLanguageAdminApi.class)
@AutoConfigureMockMvc
class CandidateLanguageAdminApiTest extends ApiTestBase {

    private static final long CANDIDATE_ID = 99L;
    private static final String BASE_PATH = "/api/admin/candidate-language";
    private static final String GET_LIST_PATH = "/{id}/list";

    private static final CandidateLanguage candidateLanguage =
        CandidateTestData.getCandidateLanguage();

    private static final List<CandidateLanguage> candidateLanguageList =
        CandidateTestData.getListOfCandidateLanguages();

    @MockBean CandidateLanguageService candidateLanguageService;

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired CandidateLanguageAdminApi candidateLanguageAdminApi;

    @BeforeEach
    void setUp() {
        configureAuthentication();
    }

    @Test
    public void testWebOnlyContextLoads() {
        assertThat(candidateLanguageAdminApi).isNotNull();
    }

    @Test
    @DisplayName("get candidate languages succeeds")
    void getCandidateLanguagesSucceeds() throws Exception {

        given(candidateLanguageService
                .list(anyLong()))
                .willReturn(candidateLanguageList);

        mockMvc.perform(get(BASE_PATH + GET_LIST_PATH.replace("{id}", String.valueOf(CANDIDATE_ID)))
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].spokenLevel.level", is(9)))
                .andExpect(jsonPath("$.[0].spokenLevel.name", is("Good")))
                .andExpect(jsonPath("$.[0].writtenLevel.level", is(9)))
                .andExpect(jsonPath("$.[0].writtenLevel.name", is("Good")))
                .andExpect(jsonPath("$.[0].language.name", is("Arabic")));

        verify(candidateLanguageService).list(anyLong());
    }

    @Test
    @DisplayName("create candidate language succeeds")
    void createCandidateLanguageSucceeds() throws Exception {
        CreateCandidateLanguageRequest request = new CreateCandidateLanguageRequest();

        given(candidateLanguageService
                .createCandidateLanguage(any(CreateCandidateLanguageRequest.class)))
                .willReturn(candidateLanguage);

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
                .andExpect(jsonPath("$.spokenLevel.level", is(9)))
                .andExpect(jsonPath("$.spokenLevel.name", is("Good")))
                .andExpect(jsonPath("$.writtenLevel.level", is(9)))
                .andExpect(jsonPath("$.writtenLevel.name", is("Good")))
                .andExpect(jsonPath("$.language.name", is("Arabic")));

        verify(candidateLanguageService).createCandidateLanguage(any(CreateCandidateLanguageRequest.class));
    }

    @Test
    @DisplayName("update candidate language succeeds")
    void updateCandidateLanguageSucceeds() throws Exception {
        UpdateCandidateLanguageRequest request = new UpdateCandidateLanguageRequest();

        given(candidateLanguageService
                .updateCandidateLanguage(any(UpdateCandidateLanguageRequest.class)))
                .willReturn(candidateLanguage);

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
                .andExpect(jsonPath("$.spokenLevel.level", is(9)))
                .andExpect(jsonPath("$.spokenLevel.name", is("Good")))
                .andExpect(jsonPath("$.writtenLevel.level", is(9)))
                .andExpect(jsonPath("$.writtenLevel.name", is("Good")))
                .andExpect(jsonPath("$.language.name", is("Arabic")));

        verify(candidateLanguageService).updateCandidateLanguage(any(UpdateCandidateLanguageRequest.class));
    }

    @Test
    @DisplayName("delete candidate language by id succeeds")
    void deleteCandidateLanguageByIdSucceeds() throws Exception {
        mockMvc.perform(delete(BASE_PATH + "/" + CANDIDATE_ID)
                        .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token"))

                .andExpect(status().isOk());

        verify(candidateLanguageService).deleteCandidateLanguage(anyLong());
    }
}
