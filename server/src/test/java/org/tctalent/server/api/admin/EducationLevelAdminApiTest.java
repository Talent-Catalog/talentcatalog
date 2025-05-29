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
import org.tctalent.server.model.db.EducationLevel;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.SystemLanguage;
import org.tctalent.server.request.education.level.CreateEducationLevelRequest;
import org.tctalent.server.request.education.level.SearchEducationLevelRequest;
import org.tctalent.server.request.education.level.UpdateEducationLevelRequest;
import org.tctalent.server.service.db.EducationLevelService;
import org.tctalent.server.service.db.LanguageService;

/**
 * Unit tests for Education Level Admin Api endpoints.
 *
 * @author sadatmalik
 */
@WebMvcTest(EducationLevelAdminApi.class)
@AutoConfigureMockMvc
class EducationLevelAdminApiTest extends ApiTestBase {
  private static final String LANG_CODE = "SPANISH_LANG_CODE";
  private static final long EDUCATION_LEVEL_ID = 111L;

  private static final String BASE_PATH = "/api/admin/education-level";
  private static final String SYSTEM_LANGUAGE_TRANSLATION_PATH = "/system/{langCode}";
  private static final String SEARCH_PAGED_PATH = "/search";

  private final SystemLanguage systemLanguage = getSystemLanguage();
  private final List<EducationLevel> educationLevels = AdminApiTestUtil.getEducationLevels();

  private final Page<EducationLevel> educationLevelPage =
      new PageImpl<>(
          educationLevels,
          PageRequest.of(0,10, Sort.unsorted()),
          1
      );

  @MockBean EducationLevelService educationLevelService;
  @MockBean LanguageService languageService;

  @Autowired MockMvc mockMvc;

  @Autowired ObjectMapper objectMapper;
  @Autowired EducationLevelAdminApi educationLevelAdminApi;

  @BeforeEach
  void setUp() {
    configureAuthentication();
  }

  @Test
  public void testWebOnlyContextLoads() {
    assertThat(educationLevelAdminApi).isNotNull();
  }

  @Test
  @DisplayName("add system language translations succeeds")
  void addSystemLanguageTranslationSucceeds() throws Exception {
    MockMultipartFile file = new MockMultipartFile("lang_translation", "lang_translation.txt",
        "text/plain", "some mock language translation text".getBytes());

    given(languageService
        .addSystemLanguageTranslations(anyString(), anyString(), any(InputStream.class)))
        .willReturn(systemLanguage);

    mockMvc.perform(multipart(BASE_PATH + SYSTEM_LANGUAGE_TRANSLATION_PATH.replace("{langCode}", LANG_CODE))
            .file("file", file.getBytes())
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token"))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.language", is("Spanish")))
        .andExpect(jsonPath("$.rtl", is(false)));

    verify(languageService).addSystemLanguageTranslations(anyString(), anyString(), any(InputStream.class));
  }

  @Test
  @DisplayName("get all education levels succeeds")
  void getAllEducationLevelsSucceeds() throws Exception {
    given(educationLevelService
        .listEducationLevels())
        .willReturn(educationLevels);

    mockMvc.perform(get(BASE_PATH)
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$", hasSize(3)))
        .andExpect(jsonPath("$[0].name", is("Excellent")))
        .andExpect(jsonPath("$[0].status", is("active")))
        .andExpect(jsonPath("$[0].level", is(1)))
        .andExpect(jsonPath("$[1].name", is("Great")))
        .andExpect(jsonPath("$[1].status", is("active")))
        .andExpect(jsonPath("$[1].level", is(2)))
        .andExpect(jsonPath("$[2].name", is("Good")))
        .andExpect(jsonPath("$[2].status", is("active")))
        .andExpect(jsonPath("$[2].level", is(3)));

    verify(educationLevelService).listEducationLevels();
  }

  @Test
  @DisplayName("search paged education levels succeeds")
  void searchPagedEducationLevelsSucceeds() throws Exception {
    SearchEducationLevelRequest request = new SearchEducationLevelRequest();

    given(educationLevelService
        .searchEducationLevels(any(SearchEducationLevelRequest.class)))
        .willReturn(educationLevelPage);

    mockMvc.perform(post(BASE_PATH + SEARCH_PAGED_PATH)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.totalElements", is(3)))
        .andExpect(jsonPath("$.totalPages", is(1)))
        .andExpect(jsonPath("$.number", is(0)))
        .andExpect(jsonPath("$.hasNext", is(false)))
        .andExpect(jsonPath("$.hasPrevious", is(false)))
        .andExpect(jsonPath("$.content", notNullValue()))
        .andExpect(jsonPath("$.content[0].name", is("Excellent")))
        .andExpect(jsonPath("$.content[0].status", is("active")))
        .andExpect(jsonPath("$.content[0].level", is(1)))
        .andExpect(jsonPath("$.content[1].name", is("Great")))
        .andExpect(jsonPath("$.content[1].status", is("active")))
        .andExpect(jsonPath("$.content[1].level", is(2)))
        .andExpect(jsonPath("$.content[2].name", is("Good")))
        .andExpect(jsonPath("$.content[2].status", is("active")))
        .andExpect(jsonPath("$.content[2].level", is(3)));

    verify(educationLevelService).searchEducationLevels(any(SearchEducationLevelRequest.class));
  }

  @Test
  @DisplayName("get education level by id succeeds")
  void getEducationLevelByIdSucceeds() throws Exception {

    given(educationLevelService
        .getEducationLevel(EDUCATION_LEVEL_ID))
        .willReturn(new EducationLevel("Amazing", Status.active, 0));

    mockMvc.perform(get(BASE_PATH + "/" + EDUCATION_LEVEL_ID)
            .header("Authorization", "Bearer " + "jwt-token")
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$.name", is("Amazing")))
        .andExpect(jsonPath("$.status", is("active")))
        .andExpect(jsonPath("$.level", is(0)));

    verify(educationLevelService).getEducationLevel(EDUCATION_LEVEL_ID);
  }

  @Test
  @DisplayName("create education level succeeds")
  void createEducationLevelSucceeds() throws Exception {
    CreateEducationLevelRequest request = new CreateEducationLevelRequest();
    request.setName("Amazing");
    request.setStatus(Status.active);

    given(educationLevelService
        .createEducationLevel(any(CreateEducationLevelRequest.class)))
        .willReturn(new EducationLevel("Amazing", Status.active, 0));

    mockMvc.perform(post(BASE_PATH)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$.name", is("Amazing")))
        .andExpect(jsonPath("$.status", is("active")))
        .andExpect(jsonPath("$.level", is(0)));

    verify(educationLevelService).createEducationLevel(any(CreateEducationLevelRequest.class));
  }

  @Test
  @DisplayName("update education level succeeds")
  void updateEducationLevelSucceeds() throws Exception {
    UpdateEducationLevelRequest request = new UpdateEducationLevelRequest();
    request.setName("Amazing");
    request.setStatus(Status.active);

    given(educationLevelService
        .updateEducationLevel(anyLong(), any(UpdateEducationLevelRequest.class)))
        .willReturn(new EducationLevel("Amazing", Status.active, 0));

    mockMvc.perform(put(BASE_PATH + "/" + EDUCATION_LEVEL_ID)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$.name", is("Amazing")))
        .andExpect(jsonPath("$.status", is("active")))
        .andExpect(jsonPath("$.level", is(0)));

    verify(educationLevelService).updateEducationLevel(anyLong(), any(UpdateEducationLevelRequest.class));
  }

  @Test
  @DisplayName("delete education level by id succeeds")
  void deleteEducationLevelByIdSucceeds() throws Exception {

    given(educationLevelService
        .deleteEducationLevel(EDUCATION_LEVEL_ID))
        .willReturn(true);

    mockMvc.perform(delete(BASE_PATH + "/" + EDUCATION_LEVEL_ID)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$", is(true)));

    verify(educationLevelService).deleteEducationLevel(EDUCATION_LEVEL_ID);
  }
}
