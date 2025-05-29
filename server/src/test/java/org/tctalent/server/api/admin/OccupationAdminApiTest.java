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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.tctalent.server.data.LanguageTestData.getSystemLanguage;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.List;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.tctalent.server.data.CandidateTestData;
import org.tctalent.server.model.db.Occupation;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.SystemLanguage;
import org.tctalent.server.request.occupation.CreateOccupationRequest;
import org.tctalent.server.request.occupation.SearchOccupationRequest;
import org.tctalent.server.request.occupation.UpdateOccupationRequest;
import org.tctalent.server.service.db.LanguageService;
import org.tctalent.server.service.db.OccupationService;
import org.tctalent.server.util.dto.DtoBuilder;

/**
 * Unit tests for Candidate Occupation Admin Api endpoints.
 *
 * @author sadatmalik
 */
@WebMvcTest(OccupationAdminApi.class)
@AutoConfigureMockMvc
class OccupationAdminApiTest extends ApiTestBase {

    private static final long OCCUPATION_ID = 99L;

    private static final String BASE_PATH = "/api/admin/occupation";
    private static final String ADD_SYSTEM_LANGUAGE_TRANSLATIONS = "/system/{langCode}";
    private static final String SEARCH_PATH = "/search";

    private static final List<Occupation> occupationList = CandidateTestData.getListOfOccupations();
    private static final Occupation occupation = AdminApiTestUtil.getOccupation();
    private static final SystemLanguage systemLanguage = getSystemLanguage();

    private final Page<Occupation> occupationPage =
            new PageImpl<>(
                    List.of(occupation),
                    PageRequest.of(0,10, Sort.unsorted()),
                    1
            );

    @MockBean OccupationService occupationService;
    @MockBean LanguageService languageService;

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired OccupationAdminApi occupationAdminApi;

    @BeforeEach
    void setUp() {
        configureAuthentication();
        given(occupationService
            .selectBuilder())
            .willReturn(new DtoBuilder().add("id").add("name").add("status"));
    }

    @Test
    public void testWebOnlyContextLoads() {
        assertThat(occupationAdminApi).isNotNull();
    }

    @Test
    @DisplayName("add system language translation succeeds")
    void addSystemLanguageTranslationsSucceeds() throws Exception {
        String langCode = "Spanish";
        MockMultipartFile file = new MockMultipartFile("trans", "trans.txt", "text/plain", "some translations".getBytes());

        given(languageService
                .addSystemLanguageTranslations(anyString(), anyString(), any(InputStream.class)))
                .willReturn(systemLanguage);

        mockMvc.perform(multipart(BASE_PATH + ADD_SYSTEM_LANGUAGE_TRANSLATIONS.replace("{langCode}", langCode))
                        .file("file", file.getBytes())
                        .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.language", is("Spanish")))
                .andExpect(jsonPath("$.label", is("spanish")))
                .andExpect(jsonPath("$.rtl", is(false)));

        verify(languageService).addSystemLanguageTranslations(anyString(), anyString(), any(InputStream.class));
    }

    @Test
    @DisplayName("list all occupations succeeds")
    void listAllOccupationsSucceeds() throws Exception {

        given(occupationService
                .listOccupations())
                .willReturn(occupationList);

        mockMvc.perform(get(BASE_PATH)
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$.[0].name", is("Builder")))
                .andExpect(jsonPath("$.[0].status", is("active")))
                .andExpect(jsonPath("$.[1].name", is("Baker")))
                .andExpect(jsonPath("$.[1].status", is("active")));

        verify(occupationService).listOccupations();
    }

    @Test
    @DisplayName("search paged occupations succeeds")
    void searchPagedSucceeds() throws Exception {
        SearchOccupationRequest request = new SearchOccupationRequest();
        request.setKeyword("nur");
        request.setStatus(Status.active);
        request.setLanguage("english");

        given(occupationService
                .searchOccupations(any(SearchOccupationRequest.class)))
                .willReturn(occupationPage);

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
                .andExpect(jsonPath("$.content.[0].id", is(1)))
                .andExpect(jsonPath("$.content.[0].name", is("Nurse")))
                .andExpect(jsonPath("$.content.[0].status", is("active")));

        verify(occupationService).searchOccupations(any(SearchOccupationRequest.class));
    }

    @Test
    @DisplayName("get occupation by id succeeds")
    void getOccupationByIdSucceeds() throws Exception {

        given(occupationService
                .getOccupation(anyLong()))
                .willReturn(occupation);

        mockMvc.perform(get(BASE_PATH + "/" + OCCUPATION_ID)
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Nurse")))
                .andExpect(jsonPath("$.status", is("active")));

        verify(occupationService).getOccupation(anyLong());
    }

    @Test
    @DisplayName("create occupation succeeds")
    void createOccupationSucceeds() throws Exception {
        CreateOccupationRequest request = new CreateOccupationRequest();
        request.setName("Nurse");
        request.setStatus(Status.active);

        given(occupationService
                .createOccupation(any(CreateOccupationRequest.class)))
                .willReturn(occupation);

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
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Nurse")))
                .andExpect(jsonPath("$.status", is("active")));

        verify(occupationService).createOccupation(any(CreateOccupationRequest.class));
    }

    @Test
    @DisplayName("update occupation by id succeeds")
    void updateOccupationByIdSucceeds() throws Exception {
        UpdateOccupationRequest request = new UpdateOccupationRequest();
        request.setName("Nurse");
        request.setStatus(Status.active);

        given(occupationService
                .updateOccupation(anyLong(), any(UpdateOccupationRequest.class)))
                .willReturn(occupation);

        mockMvc.perform(put(BASE_PATH + "/" + OCCUPATION_ID)
                        .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Nurse")))
                .andExpect(jsonPath("$.status", is("active")));

        verify(occupationService).updateOccupation(anyLong(), any(UpdateOccupationRequest.class));
    }

    @Test
    @DisplayName("delete occupation by id succeeds")
    void deleteOccupationByIdSucceeds() throws Exception {
        mockMvc.perform(delete(BASE_PATH + "/" + OCCUPATION_ID)
                        .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token"))

                .andExpect(status().isOk());

        verify(occupationService).deleteOccupation(anyLong());
    }
}
