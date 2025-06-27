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
import org.tctalent.server.model.db.CandidateNote;
import org.tctalent.server.request.note.CreateCandidateNoteRequest;
import org.tctalent.server.request.note.SearchCandidateNotesRequest;
import org.tctalent.server.request.note.UpdateCandidateNoteRequest;
import org.tctalent.server.service.db.CandidateNoteService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.tctalent.server.data.CandidateTestData.getCandidateNote;

/**
 * Unit tests for Candidate Note Admin Api endpoints.
 *
 * @author sadatmalik
 */
@WebMvcTest(CandidateNoteAdminApi.class)
@AutoConfigureMockMvc
class CandidateNoteAdminApiTest extends ApiTestBase {

    private static final long CANDIDATE_ID = 99L;
    private static final String BASE_PATH = "/api/admin/candidate-note";
    private static final String SEARCH_PATH = "/search";

    private static final CandidateNote candidateNote = getCandidateNote();

    private final Page<CandidateNote> candidateNotesPage =
            new PageImpl<>(
                    List.of(candidateNote),
                    PageRequest.of(0,10, Sort.unsorted()),
                    1
            );

    @MockBean CandidateNoteService candidateNoteService;

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired CandidateNoteAdminApi candidateNoteAdminApi;

    @BeforeEach
    void setUp() {
        configureAuthentication();
    }

    @Test
    public void testWebOnlyContextLoads() {
        assertThat(candidateNoteAdminApi).isNotNull();
    }

    @Test
    @DisplayName("search candidate notes succeeds")
    void searchCandidateNotesSucceeds() throws Exception {
        SearchCandidateNotesRequest request = new SearchCandidateNotesRequest();

        given(candidateNoteService
                .searchCandidateNotes(any(SearchCandidateNotesRequest.class)))
                .willReturn(candidateNotesPage);

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
                .andExpect(jsonPath("$.content.[0].noteType", is("candidate")))
                .andExpect(jsonPath("$.content.[0].comment", is("Some comments")))
                .andExpect(jsonPath("$.content.[0].title", is("A title")));

        verify(candidateNoteService).searchCandidateNotes(any(SearchCandidateNotesRequest.class));
    }

    @Test
    @DisplayName("create candidate note succeeds")
    void createCandidateNoteSucceeds() throws Exception {
        CreateCandidateNoteRequest request = new CreateCandidateNoteRequest(
                CANDIDATE_ID, "A title", "Some Comments"
        );

        given(candidateNoteService
                .createCandidateNote(any(CreateCandidateNoteRequest.class)))
                .willReturn(candidateNote);

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
                .andExpect(jsonPath("$.noteType", is("candidate")))
                .andExpect(jsonPath("$.comment", is("Some comments")))
                .andExpect(jsonPath("$.title", is("A title")));

        verify(candidateNoteService).createCandidateNote(any(CreateCandidateNoteRequest.class));
    }

    @Test
    @DisplayName("update candidate note by id succeeds")
    void updateCandidateNoteByIdSucceeds() throws Exception {
        UpdateCandidateNoteRequest request = new UpdateCandidateNoteRequest();
        request.setTitle("A title");
        request.setComment("A comment");

        given(candidateNoteService
                .updateCandidateNote(anyLong(), any(UpdateCandidateNoteRequest.class)))
                .willReturn(candidateNote);

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
                .andExpect(jsonPath("$.noteType", is("candidate")))
                .andExpect(jsonPath("$.comment", is("Some comments")))
                .andExpect(jsonPath("$.title", is("A title")));

        verify(candidateNoteService).updateCandidateNote(anyLong(), any(UpdateCandidateNoteRequest.class));
    }

}
