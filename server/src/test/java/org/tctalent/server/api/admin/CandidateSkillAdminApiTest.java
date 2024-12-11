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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.tctalent.server.model.db.CandidateSkill;
import org.tctalent.server.request.skill.SearchCandidateSkillRequest;
import org.tctalent.server.service.db.CandidateSkillService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.tctalent.server.api.admin.AdminApiTestUtil.getCandidateSkill;

/**
 * Unit tests for Candidate Visa Job Check Admin Api endpoints.
 *
 * @author Caroline Cameron
 */
@WebMvcTest(CandidateSkillAdminApi.class)
@AutoConfigureMockMvc
public class CandidateSkillAdminApiTest extends ApiTestBase {
    private static final String BASE_PATH = "/api/admin/candidate-skill";
    private static final String SEARCH_PATH = "/search";

    private static final CandidateSkill candidateSkill = getCandidateSkill();

    private final Page<CandidateSkill> candidateSkillsPage =
            new PageImpl<>(
                    List.of(candidateSkill),
                    PageRequest.of(0,10, Sort.unsorted()),
                    1
            );

    @MockBean
    CandidateSkillService candidateSkillService;

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired CandidateSkillAdminApi candidateSkillAdminApi;

    @BeforeEach
    void setUp() {
        configureAuthentication();
    }

    @Test
    public void testWebOnlyContextLoads() {
        assertThat(candidateSkillAdminApi).isNotNull();
    }

    @Test
    @DisplayName("search candidate skill succeeds")
    void searchCandidateSkillsSucceeds() throws Exception {
        SearchCandidateSkillRequest request = new SearchCandidateSkillRequest();
        request.setCandidateId(99L);

        given(candidateSkillService
                .searchCandidateSkills(any(SearchCandidateSkillRequest.class)))
                .willReturn(candidateSkillsPage);

        mockMvc.perform(post(BASE_PATH + SEARCH_PATH)
                        .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.number", is(0)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)))
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.content.[0].id", notNullValue()))
                .andExpect(jsonPath("$.content.[0].skill", is("Adobe Photoshop")))
                .andExpect(jsonPath("$.content.[0].timePeriod", is("3-5 years")));

        verify(candidateSkillService).searchCandidateSkills(any(SearchCandidateSkillRequest.class));
    }
}
