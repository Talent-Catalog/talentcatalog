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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.request.candidate.source.CopySourceContentsRequest;
import org.tctalent.server.request.list.ContentUpdateType;
import org.tctalent.server.request.list.SearchSavedListRequest;
import org.tctalent.server.request.list.UpdateSavedListInfoRequest;
import org.tctalent.server.service.db.CandidateSavedListService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.SalesforceService;
import org.tctalent.server.service.db.SavedListService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SavedListAdminApi.class)
@AutoConfigureMockMvc
class SavedListAdminApiTest extends ApiTestBase {

    private static final long SAVED_LIST_ID = 1L;

    private static final String BASE_PATH = "/api/admin/saved-list";
    private static final String RESTRICTED_LIST_PATH = "/restricted";
    private static final String COPY_PATH = "/copy/{id}";
    private static final String SEARCH_PAGED_PATH = "/search-paged";
    private static final String SEARCH_PATH = "/search";

    private static final SavedList savedList = AdminApiTestUtil.getSavedList();
    private static final List<SavedList> savedLists = AdminApiTestUtil.getSavedLists();

    private final Page<SavedList> savedListPage =
        new PageImpl<>(
            savedLists,
            PageRequest.of(0,10, Sort.unsorted()),
            1
        );

    @MockBean
    SavedListService savedListService;
    @MockBean
    CandidateService candidateService;
    @MockBean
    CandidateSavedListService candidateSavedListService;
    @MockBean
    SalesforceService salesforceService;

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired SavedListAdminApi savedListAdminApi;

    @BeforeEach
    void setUp() {
        configureAuthentication();
    }

    @Test
    public void testWebOnlyContextLoads() {
        assertThat(savedListAdminApi).isNotNull();
    }

    @Test
    @DisplayName("create saved list succeeds")
    void createSavedListSucceeds() throws Exception {
        UpdateSavedListInfoRequest request = new UpdateSavedListInfoRequest();

        given(savedListService
            .createSavedList(any(UpdateSavedListInfoRequest.class)))
            .willReturn(savedList);

        mockMvc.perform(post(BASE_PATH)
                .header("Authorization", "Bearer " + "jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))

            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", notNullValue()))
            .andExpect(jsonPath("$.id", is(1)));

        verify(savedListService).createSavedList(any(UpdateSavedListInfoRequest.class));
    }

    @Test
    @DisplayName("delete saved list by id succeeds")
    void deleteSavedListByIdSucceeds() throws Exception {

        given(candidateSavedListService
            .deleteSavedList(SAVED_LIST_ID))
            .willReturn(true);

        mockMvc.perform(delete(BASE_PATH + "/" + SAVED_LIST_ID)
                .header("Authorization", "Bearer " + "jwt-token")
                .accept(MediaType.APPLICATION_JSON))

            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", notNullValue()))
            .andExpect(jsonPath("$", is(true)));

        verify(candidateSavedListService).deleteSavedList(SAVED_LIST_ID);
    }

    @Test
    @DisplayName("get saved list by id succeeds")
    void getSavedListByIdSucceeds() throws Exception {

        given(savedListService
            .get(SAVED_LIST_ID))
            .willReturn(savedList);

        mockMvc.perform(get(BASE_PATH + "/" + SAVED_LIST_ID)
                .header("Authorization", "Bearer " + "jwt-token")
                .accept(MediaType.APPLICATION_JSON))

            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", notNullValue()))
            .andExpect(jsonPath("$.id", is(1)));

        verify(savedListService).get(SAVED_LIST_ID);
    }


    @Test
    @DisplayName("search saved lists succeeds")
    void searchSavedListsSucceeds() throws Exception {
        SearchSavedListRequest request = new SearchSavedListRequest();
        given(savedListService
            .listSavedLists(request))
            .willReturn(savedLists);

        mockMvc.perform(post(BASE_PATH + SEARCH_PATH)
                .header("Authorization", "Bearer " + "jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))

            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", notNullValue()));

        verify(savedListService).listSavedLists(any(SearchSavedListRequest.class));
    }

    @Test
    @DisplayName("search paged saved lists succeeds")
    void searchPagedSavedListsSucceeds() throws Exception {
        SearchSavedListRequest request = new SearchSavedListRequest();

        given(savedListService
            .searchSavedLists(any(SearchSavedListRequest.class)))
            .willReturn(savedListPage);

        mockMvc.perform(post(BASE_PATH + SEARCH_PAGED_PATH)
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
            .andExpect(jsonPath("$.content.[0].id", is(1)));

        verify(savedListService).searchSavedLists(any(SearchSavedListRequest.class));
    }

    @Test
    @DisplayName("update saved list succeeds")
    void updateSavedListSucceeds() throws Exception {
        UpdateSavedListInfoRequest request = new UpdateSavedListInfoRequest();

        given(savedListService
            .updateSavedList(anyLong(), any(UpdateSavedListInfoRequest.class)))
            .willReturn(savedList);

        mockMvc.perform(put(BASE_PATH + "/" + SAVED_LIST_ID)
                .header("Authorization", "Bearer " + "jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))

            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", notNullValue()))
            .andExpect(jsonPath("$.id", is(1)));

        verify(savedListService).updateSavedList(anyLong(), any(UpdateSavedListInfoRequest.class));
    }
    

    @Test
    @DisplayName("copy saved list succeeds")
    void copySavedListSucceeds() throws Exception {
        // Set up the request to save to my test savedList (id = 1). Not sure if mocking a full request is neccessary,
        // but tried as part of trying to fix content type not set problem.
        CopySourceContentsRequest request = new CopySourceContentsRequest();
        request.setSavedListId(0L);
        request.setNewListName("test list");
        request.setSourceListId(SAVED_LIST_ID);
        request.setUpdateType(ContentUpdateType.add);

        // Set up the source list which I am copying across to my test saved list.
        SavedList targetList = new SavedList();
        targetList.setId(11L);
        targetList.setName("test list");

        
        given(savedListService
            .get(SAVED_LIST_ID))
            .willReturn(savedList);

        // todo this stub is returning null when debugging - can't figure out why.
        //  Getting java.lang.AssertionError: Content type not set. Nothing is being returned in the response body.
        given(candidateSavedListService
            .copy(savedList, request))
            .willReturn(targetList);

        mockMvc.perform(put(BASE_PATH + COPY_PATH.replace("{id}", Long.toString(SAVED_LIST_ID)))
                .header("Authorization", "Bearer " + "jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))

            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", notNullValue()))
            .andExpect(jsonPath("$.id", is(1)));

        verify(savedListService).get(anyLong());
        verify(candidateSavedListService).copy(savedList, request);
    }
//
//    @Test
//    @DisplayName("get destination countries succeeds")
//    void getDestinationCountriesSucceeds() throws Exception {
//        given(savedListService
//            .getTBBDestinations())
//            .willReturn(countries);
//
//        mockMvc.perform(get(BASE_PATH + "/" + DESTINATIONS_LIST_PATH)
//                .header("Authorization", "Bearer " + "jwt-token")
//                .contentType(MediaType.APPLICATION_JSON))
//
//            .andDo(print())
//            .andExpect(status().isOk())
//            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//            .andExpect(jsonPath("$", notNullValue()))
//            .andExpect(jsonPath("$").isArray())
//            .andExpect(jsonPath("$", hasSize(3)))
//            .andExpect(jsonPath("$[0].name", is("Jordan")))
//            .andExpect(jsonPath("$[0].status", is("active")))
//            .andExpect(jsonPath("$[1].name", is("Pakistan")))
//            .andExpect(jsonPath("$[1].status", is("active")))
//            .andExpect(jsonPath("$[2].name", is("Palestine")))
//            .andExpect(jsonPath("$[2].status", is("active")));
//
//
//        verify(savedListService).getTBBDestinations();
//    }
//
//    @Test
//    @DisplayName("search paged countries succeeds")
//    void searchPagedCountriesSucceeds() throws Exception {
//        SearchCountryRequest request = new SearchCountryRequest();
//
//        given(savedListService
//            .searchCountries(any(SearchCountryRequest.class)))
//            .willReturn(countryPage);
//
//        mockMvc.perform(post(BASE_PATH + SEARCH_PAGED_PATH)
//                .header("Authorization", "Bearer " + "jwt-token")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(request))
//                .accept(MediaType.APPLICATION_JSON))
//
//            .andDo(print())
//            .andExpect(status().isOk())
//            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//            .andExpect(jsonPath("$.totalElements", is(3)))
//            .andExpect(jsonPath("$.totalPages", is(1)))
//            .andExpect(jsonPath("$.number", is(0)))
//            .andExpect(jsonPath("$.hasNext", is(false)))
//            .andExpect(jsonPath("$.hasPrevious", is(false)))
//            .andExpect(jsonPath("$.content", notNullValue()))
//            .andExpect(jsonPath("$.content.[0].name", is("Jordan")))
//            .andExpect(jsonPath("$.content.[0].status", is("active")))
//            .andExpect(jsonPath("$.content.[1].name", is("Pakistan")))
//            .andExpect(jsonPath("$.content.[1].status", is("active")))
//            .andExpect(jsonPath("$.content.[2].name", is("Palestine")))
//            .andExpect(jsonPath("$.content.[2].status", is("active")));
//
//        verify(savedListService).searchCountries(any(SearchCountryRequest.class));
//    }
//
//    @Test
//    @DisplayName("get country by id succeeds")
//    void getCountryByIdSucceeds() throws Exception {
//
//        given(savedListService
//            .getCountry(COUNTRY_ID))
//            .willReturn(new Country("Ukraine", Status.active));
//
//        mockMvc.perform(get(BASE_PATH + "/" + COUNTRY_ID)
//                .header("Authorization", "Bearer " + "jwt-token")
//                .accept(MediaType.APPLICATION_JSON))
//
//            .andDo(print())
//            .andExpect(status().isOk())
//            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//            .andExpect(jsonPath("$", notNullValue()))
//            .andExpect(jsonPath("$.name", is("Ukraine")))
//            .andExpect(jsonPath("$.status", is("active")));
//
//        verify(savedListService).getCountry(COUNTRY_ID);
//    }
//
//    
//
//    @Test
//    @DisplayName("update country succeeds")
//    void updateCountrySucceeds() throws Exception {
//        UpdateCountryRequest request = new UpdateCountryRequest();
//        request.setName("Ukraine");
//        request.setStatus(Status.active);
//
//        given(savedListService
//            .updateCountry(anyLong(), any(UpdateCountryRequest.class)))
//            .willReturn(new Country("Ukraine", Status.active));
//
//        mockMvc.perform(put(BASE_PATH + "/" + COUNTRY_ID)
//                .header("Authorization", "Bearer " + "jwt-token")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(request))
//                .accept(MediaType.APPLICATION_JSON))
//
//            .andDo(print())
//            .andExpect(status().isOk())
//            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//            .andExpect(jsonPath("$", notNullValue()))
//            .andExpect(jsonPath("$.name", is("Ukraine")))
//            .andExpect(jsonPath("$.status", is("active")));
//
//        verify(savedListService).updateCountry(anyLong(), any(UpdateCountryRequest.class));
//    }

    

}
