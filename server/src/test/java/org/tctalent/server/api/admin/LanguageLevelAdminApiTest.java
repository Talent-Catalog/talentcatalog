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
import static org.tctalent.server.data.LanguageTestData.getLanguageLevel;
import static org.tctalent.server.data.LanguageTestData.getLanguageLevelList;
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
import org.tctalent.server.model.db.LanguageLevel;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.SystemLanguage;
import org.tctalent.server.request.language.level.CreateLanguageLevelRequest;
import org.tctalent.server.request.language.level.SearchLanguageLevelRequest;
import org.tctalent.server.request.language.level.UpdateLanguageLevelRequest;
import org.tctalent.server.service.db.LanguageLevelService;
import org.tctalent.server.service.db.LanguageService;

/**
 * Unit tests for Language Level Admin Api endpoints.
 *
 * @author sadatmalik
 */
@WebMvcTest(LanguageLevelAdminApi.class)
@AutoConfigureMockMvc
class LanguageLevelAdminApiTest extends ApiTestBase {

    private static final long LANGUAGE_LEVEL_ID = 99L;

    private static final String BASE_PATH = "/api/admin/language-level";
    private static final String ADD_SYSTEM_LANGUAGE_TRANSLATIONS = "/system/{langCode}";
    private static final String SEARCH_PATH = "/search";

    private static final List<LanguageLevel> languageLevelList = getLanguageLevelList();
    private static final LanguageLevel languageLevel = getLanguageLevel();
    private static final SystemLanguage systemLanguage = getSystemLanguage();

    private final Page<LanguageLevel> languageLevelPage =
            new PageImpl<>(
                    List.of(languageLevel),
                    PageRequest.of(0,10, Sort.unsorted()),
                    1
            );

    @MockBean LanguageLevelService languageLevelService;
    @MockBean LanguageService languageService;

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired LanguageLevelAdminApi languageLevelAdminApi;

    @BeforeEach
    void setUp() {
        configureAuthentication();
    }

    @Test
    public void testWebOnlyContextLoads() {
        assertThat(languageLevelAdminApi).isNotNull();
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
    @DisplayName("list all language levels succeeds")
    void listAllLanguageLevelsSucceeds() throws Exception {

        given(languageLevelService
                .listLanguageLevels())
                .willReturn(languageLevelList);

        mockMvc.perform(get(BASE_PATH)
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].name", is("Excellent")))
                .andExpect(jsonPath("$.[0].status", is("active")))
                .andExpect(jsonPath("$.[0].level", is(1)));

        verify(languageLevelService).listLanguageLevels();
    }

    @Test
    @DisplayName("search paged language levels succeeds")
    void searchPagedSucceeds() throws Exception {
        SearchLanguageLevelRequest request = new SearchLanguageLevelRequest();
        request.setKeyword("exc");
        request.setStatus(Status.active);
        request.setLanguage("english");

        given(languageLevelService
                .searchLanguageLevels(any(SearchLanguageLevelRequest.class)))
                .willReturn(languageLevelPage);

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
                .andExpect(jsonPath("$.content.[0].name", is("Excellent")))
                .andExpect(jsonPath("$.content.[0].status", is("active")))
                .andExpect(jsonPath("$.content.[0].level", is(1)));

        verify(languageLevelService).searchLanguageLevels(any(SearchLanguageLevelRequest.class));
    }

    @Test
    @DisplayName("get language level by id succeeds")
    void getLanguageLevelByIdSucceeds() throws Exception {

        given(languageLevelService
                .getLanguageLevel(anyLong()))
                .willReturn(languageLevel);

        mockMvc.perform(get(BASE_PATH + "/" + LANGUAGE_LEVEL_ID)
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.name", is("Excellent")))
                .andExpect(jsonPath("$.status", is("active")))
                .andExpect(jsonPath("$.level", is(1)));

        verify(languageLevelService).getLanguageLevel(anyLong());
    }

    @Test
    @DisplayName("create language level succeeds")
    void createLanguageLevelSucceeds() throws Exception {
        CreateLanguageLevelRequest request = new CreateLanguageLevelRequest();
        request.setName("Excellent");
        request.setStatus(Status.active);
        request.setLevel(1);

        given(languageLevelService
                .createLanguageLevel(any(CreateLanguageLevelRequest.class)))
                .willReturn(languageLevel);

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
                .andExpect(jsonPath("$.name", is("Excellent")))
                .andExpect(jsonPath("$.status", is("active")))
                .andExpect(jsonPath("$.level", is(1)));

        verify(languageLevelService).createLanguageLevel(any(CreateLanguageLevelRequest.class));
    }

    @Test
    @DisplayName("update language level by id succeeds")
    void updateLanguageLevelByIdSucceeds() throws Exception {
        UpdateLanguageLevelRequest request = new UpdateLanguageLevelRequest();
        request.setName("Excellent");
        request.setStatus(Status.active);
        request.setLevel(1);

        given(languageLevelService
                .updateLanguageLevel(anyLong(), any(UpdateLanguageLevelRequest.class)))
                .willReturn(languageLevel);

        mockMvc.perform(put(BASE_PATH + "/" + LANGUAGE_LEVEL_ID)
                        .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.name", is("Excellent")))
                .andExpect(jsonPath("$.status", is("active")))
                .andExpect(jsonPath("$.level", is(1)));

        verify(languageLevelService).updateLanguageLevel(anyLong(), any(UpdateLanguageLevelRequest.class));
    }

    @Test
    @DisplayName("delete language level by id succeeds")
    void deleteLanguageLevelByIdSucceeds() throws Exception {
        mockMvc.perform(delete(BASE_PATH + "/" + LANGUAGE_LEVEL_ID)
                        .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token"))

                .andExpect(status().isOk());

        verify(languageLevelService).deleteLanguageLevel(anyLong());
    }
}
