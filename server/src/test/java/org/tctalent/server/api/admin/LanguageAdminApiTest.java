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
import org.tctalent.server.model.db.Language;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.SystemLanguage;
import org.tctalent.server.request.language.CreateLanguageRequest;
import org.tctalent.server.request.language.SearchLanguageRequest;
import org.tctalent.server.request.language.UpdateLanguageRequest;
import org.tctalent.server.service.db.LanguageService;

import java.util.List;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.tctalent.server.data.LanguageTestData.getLanguage;
import static org.tctalent.server.data.LanguageTestData.getLanguageList;
import static org.tctalent.server.data.LanguageTestData.getSystemLanguage;
import static org.tctalent.server.data.LanguageTestData.getSystemLanguageList;

/**
 * Unit tests for Candidate Occupation Admin Api endpoints.
 *
 * @author sadatmalik
 */
@WebMvcTest(LanguageAdminApi.class)
@AutoConfigureMockMvc
class LanguageAdminApiTest extends ApiTestBase {

    private static final long LANGUAGE_ID = 99L;

    private static final String BASE_PATH = "/api/admin/language";
    private static final String GET_SYSTEM_LANGUAGES = "/system";
    private static final String ADD_SYSTEM_LANGUAGE_TRANSLATIONS = "/system/{langCode}";
    private static final String SEARCH_PATH = "/search";

    private static final List<Language> languageList = getLanguageList();
    private static final Language language = getLanguage();
    private static final SystemLanguage systemLanguage = getSystemLanguage();
    private static final List<SystemLanguage> systemLanguageList = getSystemLanguageList();

    private final Page<Language> languagePage =
            new PageImpl<>(
                    List.of(language),
                    PageRequest.of(0,10, Sort.unsorted()),
                    1
            );

    @MockBean LanguageService languageService;

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired LanguageAdminApi languageAdminApi;

    @BeforeEach
    void setUp() {
        configureAuthentication();
    }

    @Test
    public void testWebOnlyContextLoads() {
        assertThat(languageAdminApi).isNotNull();
    }

    @Test
    @DisplayName("list all languages succeeds")
    void listAllLanguagesSucceeds() throws Exception {

        given(languageService
                .listLanguages())
                .willReturn(languageList);

        mockMvc.perform(get(BASE_PATH)
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].name", is("Arabic")))
                .andExpect(jsonPath("$.[0].status", is("active")))
                .andExpect(jsonPath("$.[0].id", is(99)));

        verify(languageService).listLanguages();
    }

    @Test
    @DisplayName("get system languages succeeds")
    void getSystemLanguagesSucceeds() throws Exception {
        given(languageService
                .listSystemLanguages())
                .willReturn(systemLanguageList);

        mockMvc.perform(get(BASE_PATH + GET_SYSTEM_LANGUAGES)
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].language", is("Spanish")))
                .andExpect(jsonPath("$.[0].label", is("spanish")))
                .andExpect(jsonPath("$.[0].rtl", is(false)))
                .andExpect(jsonPath("$.[0].id", is(1)));

        verify(languageService).listSystemLanguages();
    }

    @Test
    @DisplayName("search paged language succeeds")
    void searchPagedSucceeds() throws Exception {
        SearchLanguageRequest request = new SearchLanguageRequest();
        request.setKeyword("ara");
        request.setStatus(Status.active);
        request.setLanguage("english");

        given(languageService
                .searchLanguages(any(SearchLanguageRequest.class)))
                .willReturn(languagePage);

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
                .andExpect(jsonPath("$.content.[0].name", is("Arabic")))
                .andExpect(jsonPath("$.content.[0].status", is("active")))
                .andExpect(jsonPath("$.content.[0].id", is(99)));

        verify(languageService).searchLanguages(any(SearchLanguageRequest.class));
    }

    @Test
    @DisplayName("get language by id succeeds")
    void getLanguageByIdSucceeds() throws Exception {

        given(languageService
                .getLanguage(anyLong()))
                .willReturn(language);

        mockMvc.perform(get(BASE_PATH + "/" + LANGUAGE_ID)
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.name", is("Arabic")))
                .andExpect(jsonPath("$.status", is("active")))
                .andExpect(jsonPath("$.id", is(99)));

        verify(languageService).getLanguage(anyLong());
    }

    @Test
    @DisplayName("add system language translation succeeds")
    void addSystemLanguageTranslationsSucceeds() throws Exception {
        String langCode = "Spanish";

        given(languageService
                .addSystemLanguage(anyString()))
                .willReturn(systemLanguage);

        mockMvc.perform(post(BASE_PATH + ADD_SYSTEM_LANGUAGE_TRANSLATIONS.replace("{langCode}", langCode))
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

        verify(languageService).addSystemLanguage(anyString());
    }

    @Test
    @DisplayName("create language level succeeds")
    void createLanguageSucceeds() throws Exception {
        CreateLanguageRequest request = new CreateLanguageRequest();
        request.setName("Arabic");
        request.setStatus(Status.active);

        given(languageService
                .createLanguage(any(CreateLanguageRequest.class)))
                .willReturn(language);

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
                .andExpect(jsonPath("$.name", is("Arabic")))
                .andExpect(jsonPath("$.status", is("active")))
                .andExpect(jsonPath("$.id", is(99)));

        verify(languageService).createLanguage(any(CreateLanguageRequest.class));
    }

    @Test
    @DisplayName("update language by id succeeds")
    void updateLanguageByIdSucceeds() throws Exception {
        UpdateLanguageRequest request = new UpdateLanguageRequest();
        request.setName("Arabic");
        request.setStatus(Status.active);

        given(languageService
                .updateLanguage(anyLong(), any(UpdateLanguageRequest.class)))
                .willReturn(language);

        mockMvc.perform(put(BASE_PATH + "/" + LANGUAGE_ID)
                        .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.name", is("Arabic")))
                .andExpect(jsonPath("$.status", is("active")))
                .andExpect(jsonPath("$.id", is(99)));

        verify(languageService).updateLanguage(anyLong(), any(UpdateLanguageRequest.class));
    }

    @Test
    @DisplayName("delete language by id succeeds")
    void deleteLanguageByIdSucceeds() throws Exception {
        mockMvc.perform(delete(BASE_PATH + "/" + LANGUAGE_ID)
                        .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token"))

                .andExpect(status().isOk());

        verify(languageService).deleteLanguage(anyLong());
    }
}
