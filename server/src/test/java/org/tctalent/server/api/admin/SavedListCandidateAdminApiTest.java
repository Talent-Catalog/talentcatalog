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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.request.candidate.SavedListGetRequest;
import org.tctalent.server.request.list.UpdateExplicitSavedListContentsRequest;
import org.tctalent.server.service.db.CandidateOpportunityService;
import org.tctalent.server.service.db.CandidateSavedListService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.service.db.OccupationService;
import org.tctalent.server.service.db.SavedListService;
import org.tctalent.server.service.db.SavedSearchService;
import org.tctalent.server.service.db.UserService;

/**
 * @author John Cameron
 */
@WebMvcTest(SavedListCandidateAdminApi.class)
@AutoConfigureMockMvc
class SavedListCandidateAdminApiTest extends ApiTestBase {

    private static final String BASE_PATH = "/api/admin/saved-list-candidate";
    private static final String IS_EMPTY_PATH = "/is-empty";
    private static final String LIST_PATH = "/list";
    private static final String MERGE_PATH = "/merge";
    private static final String MERGE_FROM_FILE_PATH = "/merge-from-file";
    private static final String REMOVE_PATH = "/remove";
    private static final String REPLACE_PATH = "/replace";
    private static final String SEARCH_PATH = "/search";
    private static final String SEARCH_PAGED_PATH = "/search-paged";
    private static final String EXPORT_CSV_PATH = "/export/csv";
    private static final String CREATE_FOLDERS_PATH = "/create-folders";
    private static final String SAVE_SELECTION_PATH = "/save-selection";
    private static final SavedList savedList = AdminApiTestUtil.getSavedListWithCandidates();

    private static final List<Candidate> savedListCandidates = new ArrayList<>(savedList.getCandidates());
    private static final Page<Candidate> savedListCandidatesPage =
        new PageImpl<>(
            savedListCandidates,
            PageRequest.of(0,10, Sort.unsorted()),
            1
        );

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    SavedListCandidateAdminApi savedListCandidateAdminApi;

    @MockBean
    CandidateOpportunityService candidateOpportunityService;
    @MockBean
    CountryService countryService;
    @MockBean
    SavedListService savedListService;
    @MockBean
    SavedSearchService savedSearchService;
    @MockBean
    CandidateSavedListService candidateSavedListService;
    @MockBean
    CandidateService candidateService;
    @MockBean
    UserService userService;
    @MockBean
    OccupationService occupationService;


    @BeforeEach
    void setUp() {
        configureAuthentication();
    }

    @Test
    void isEmpty() throws Exception {
        boolean testEmptyCheck = true;

        given(savedListService.isEmpty(anyLong()))
            .willReturn(testEmptyCheck);

        mockMvc.perform(
                get(BASE_PATH + "/" + 123 + IS_EMPTY_PATH)
                    .header("Authorization", "Bearer " + "jwt-token")
            )

            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(testEmptyCheck ? "true" : "false"));

        verify(savedListService).isEmpty(anyLong());
    }

    @Test
    void list() throws Exception {
        given(savedListService.get(anyLong()))
            .willReturn(savedList);

        mockMvc.perform(
                get(BASE_PATH + "/123" + LIST_PATH)
                    .header("Authorization", "Bearer " + "jwt-token")
                    .accept(MediaType.APPLICATION_JSON)
            )

            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$.[0].nationality.name", is("Pakistan")))
             ;

        verify(savedListService).get(anyLong());

    }

    @Test
    void merge() throws Exception {
        UpdateExplicitSavedListContentsRequest request = new UpdateExplicitSavedListContentsRequest();

        mockMvc.perform(
                put(BASE_PATH + "/123" + MERGE_PATH)
                    .with(csrf())
                    .header("Authorization", "Bearer " + "jwt-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )

            .andDo(print())
            .andExpect(status().isOk())
        ;

        verify(savedListService).mergeSavedList(anyLong(),
            any(UpdateExplicitSavedListContentsRequest.class));
    }

    @Test
    void mergeFromFile() throws Exception {
        MockMultipartFile testFile = new MockMultipartFile(
            "file", "hello.txt", MediaType.TEXT_PLAIN_VALUE,
            "some content".getBytes()
        );

        mockMvc.perform(
                multipart(HttpMethod.PUT, BASE_PATH + "/123" + MERGE_FROM_FILE_PATH)
                    .file(testFile)
                    .with(csrf())
                    .header("Authorization", "Bearer " + "jwt-token")
            )

            .andDo(print())
            .andExpect(status().isOk())
        ;

        verify(savedListService).mergeSavedListFromInputStream(anyLong(), any(InputStream.class));
    }

    @Test
    void remove() throws Exception {
        UpdateExplicitSavedListContentsRequest request = new UpdateExplicitSavedListContentsRequest();

        mockMvc.perform(
                put(BASE_PATH + "/123" + REMOVE_PATH)
                    .with(csrf())
                    .header("Authorization", "Bearer " + "jwt-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )

            .andDo(print())
            .andExpect(status().isOk())
        ;

        verify(savedListService).removeCandidateFromList(anyLong(),
            any(UpdateExplicitSavedListContentsRequest.class));
    }

    @Test
    void replace() throws Exception {
        UpdateExplicitSavedListContentsRequest request = new UpdateExplicitSavedListContentsRequest();

        mockMvc.perform(
                put(BASE_PATH + "/123" + REPLACE_PATH)
                    .with(csrf())
                    .header("Authorization", "Bearer " + "jwt-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )

            .andDo(print())
            .andExpect(status().isOk())
        ;

        verify(candidateSavedListService).clearSavedList(anyLong());

        verify(savedListService).mergeSavedList(anyLong(),
            any(UpdateExplicitSavedListContentsRequest.class));
    }

    @Test
    void search() throws Exception {
        SavedListGetRequest request = new SavedListGetRequest();

        given(savedListService.get(anyLong()))
            .willReturn(savedList);
        given(candidateService.getSavedListCandidatesUnpaged(any(SavedList.class), any(
            SavedListGetRequest.class)))
            .willReturn(savedListCandidates);

        mockMvc.perform(
                post(BASE_PATH + "/123" + SEARCH_PATH)
                    .with(csrf())
                    .header("Authorization", "Bearer " + "jwt-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .accept(MediaType.APPLICATION_JSON)
            )

            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$.[0].nationality.name", is("Pakistan")))
        ;

        verify(savedListService).get(anyLong());
        verify(candidateService).getSavedListCandidatesUnpaged(any(SavedList.class),
            any(SavedListGetRequest.class));
    }

    @Test
    void searchPaged() throws Exception {
        SavedListGetRequest request = new SavedListGetRequest();

        given(savedListService.get(anyLong()))
            .willReturn(savedList);
        given(candidateService.getSavedListCandidates(any(SavedList.class), any(
            SavedListGetRequest.class)))
            .willReturn(savedListCandidatesPage);

        mockMvc.perform(
                post(BASE_PATH + "/123" + SEARCH_PAGED_PATH)
                    .with(csrf())
                    .header("Authorization", "Bearer " + "jwt-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .accept(MediaType.APPLICATION_JSON)
            )

            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements", is(3)))
            .andExpect(jsonPath("$.totalPages", is(1)))
            .andExpect(jsonPath("$.number", is(0)))
            .andExpect(jsonPath("$.hasNext", is(false)))
            .andExpect(jsonPath("$.hasPrevious", is(false)))
            .andExpect(jsonPath("$.content", notNullValue()))
            .andExpect(jsonPath("$.content[0].nationality.name", is("Pakistan")))
        ;

        verify(savedListService).get(anyLong());
        verify(candidateService).getSavedListCandidates(any(SavedList.class),
            any(SavedListGetRequest.class));
    }

    @Test
    void create() throws Exception {
        UpdateExplicitSavedListContentsRequest request = new UpdateExplicitSavedListContentsRequest();

        given(savedListService.createSavedList(any(UpdateExplicitSavedListContentsRequest.class)))
            .willReturn(savedList);

        mockMvc.perform(
                post(BASE_PATH)
                    .with(csrf())
                    .header("Authorization", "Bearer " + "jwt-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )

            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", notNullValue()))
            .andExpect(jsonPath("$.id", is(1)))
        ;

        verify(savedListService).createSavedList(any(UpdateExplicitSavedListContentsRequest.class));
    }

    @Test
    void export() throws Exception {
        SavedListGetRequest request = new SavedListGetRequest();

        given(savedListService.get(anyLong()))
            .willReturn(savedList);
        given(candidateService.getSavedListCandidatesUnpaged(any(SavedList.class), any(
            SavedListGetRequest.class)))
            .willReturn(savedListCandidates);

        mockMvc.perform(
                post(BASE_PATH + "/123" + EXPORT_CSV_PATH)
                    .with(csrf())
                    .header("Authorization", "Bearer " + "jwt-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .accept(MediaType.TEXT_PLAIN_VALUE)
            )

            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition",
                "attachment; filename=\"" + "candidates.csv\""))
            .andExpect(content().contentType("text/csv; charset=utf-8"))
        ;

        verify(savedListService).get(anyLong());
        verify(candidateService).exportToCsv(any(SavedList.class),
            any(SavedListGetRequest.class), any(PrintWriter.class));
    }

    @Test
    void createCandidateFolders() throws Exception {
        given(savedListService.get(anyLong()))
            .willReturn(savedList);

        mockMvc.perform(
                put(BASE_PATH + "/123" + CREATE_FOLDERS_PATH)
                    .with(csrf())
                    .header("Authorization", "Bearer " + "jwt-token")
            )

            .andDo(print())
            .andExpect(status().isOk())
        ;

        verify(savedListService).get(anyLong());
        verify(candidateService).createCandidateFolder(any(Collection.class));
    }

    @Test
    void saveSelection() throws Exception {
        UpdateExplicitSavedListContentsRequest request = new UpdateExplicitSavedListContentsRequest();

        mockMvc.perform(
                put(BASE_PATH + "/123" + SAVE_SELECTION_PATH)
                    .with(csrf())
                    .header("Authorization", "Bearer " + "jwt-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )

            .andDo(print())
            .andExpect(status().isOk())
        ;

        verify(savedListService).mergeSavedList(anyLong(),
            any(UpdateExplicitSavedListContentsRequest.class));
    }
}
