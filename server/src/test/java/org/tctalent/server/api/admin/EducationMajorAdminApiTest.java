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
import org.tctalent.server.model.db.EducationMajor;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.SystemLanguage;
import org.tctalent.server.request.education.major.CreateEducationMajorRequest;
import org.tctalent.server.request.education.major.SearchEducationMajorRequest;
import org.tctalent.server.request.education.major.UpdateEducationMajorRequest;
import org.tctalent.server.service.db.EducationMajorService;
import org.tctalent.server.service.db.LanguageService;

/**
 * Unit tests for Education Major Admin Api endpoints.
 *
 * @author sadatmalik
 */
@WebMvcTest(EducationMajorAdminApi.class)
@AutoConfigureMockMvc
class EducationMajorAdminApiTest extends ApiTestBase {
  private static final String LANG_CODE = "SPANISH_LANG_CODE";
  private static final long EDUCATION_MAJOR_ID = 786L;

  private static final String BASE_PATH = "/api/admin/education-major";
  private static final String SYSTEM_LANGUAGE_TRANSLATION_PATH = "/system/{langCode}";
  private static final String SEARCH_PAGED_PATH = "/search";

  private final SystemLanguage systemLanguage = getSystemLanguage();

  private final List<EducationMajor> educationMajors = AdminApiTestUtil.getEducationMajors();

  private final Page<EducationMajor> educationLevelPage =
      new PageImpl<>(
          educationMajors,
          PageRequest.of(0,10, Sort.unsorted()),
          1
      );

  @MockBean EducationMajorService educationMajorService;
  @MockBean LanguageService languageService;

  @Autowired MockMvc mockMvc;

  @Autowired ObjectMapper objectMapper;
  @Autowired EducationMajorAdminApi educationMajorAdminApi;

  @BeforeEach
  void setUp() {
    configureAuthentication();
  }

  @Test
  public void testWebOnlyContextLoads() {
    assertThat(educationMajorAdminApi).isNotNull();
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
  @DisplayName("list all education majors succeeds")
  void getAllEducationMajorsSucceeds() throws Exception {
    given(educationMajorService
        .listActiveEducationMajors())
        .willReturn(educationMajors);

    mockMvc.perform(get(BASE_PATH)
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$", hasSize(3)))
        .andExpect(jsonPath("$[0].name", is("Computer Science")))
        .andExpect(jsonPath("$[0].status", is("active")))
        .andExpect(jsonPath("$[1].name", is("Mathematics")))
        .andExpect(jsonPath("$[1].status", is("active")))
        .andExpect(jsonPath("$[2].name", is("Psychology")))
        .andExpect(jsonPath("$[2].status", is("active")));

    verify(educationMajorService).listActiveEducationMajors();
  }

  @Test
  @DisplayName("search paged education majors succeeds")
  void searchPagedEducationMajorsSucceeds() throws Exception {
    SearchEducationMajorRequest request = new SearchEducationMajorRequest();

    given(educationMajorService
        .searchEducationMajors(any(SearchEducationMajorRequest.class)))
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
        .andExpect(jsonPath("$.content[0].name", is("Computer Science")))
        .andExpect(jsonPath("$.content[0].status", is("active")))
        .andExpect(jsonPath("$.content[1].name", is("Mathematics")))
        .andExpect(jsonPath("$.content[1].status", is("active")))
        .andExpect(jsonPath("$.content[2].name", is("Psychology")))
        .andExpect(jsonPath("$.content[2].status", is("active")));

    verify(educationMajorService).searchEducationMajors(any(SearchEducationMajorRequest.class));
  }

  @Test
  @DisplayName("get education majors by id succeeds")
  void getEducationMajorByIdSucceeds() throws Exception {

    given(educationMajorService
        .getEducationMajor(EDUCATION_MAJOR_ID))
        .willReturn(new EducationMajor("Wizardry", Status.active));

    mockMvc.perform(get(BASE_PATH + "/" + EDUCATION_MAJOR_ID)
            .header("Authorization", "Bearer " + "jwt-token")
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$.name", is("Wizardry")))
        .andExpect(jsonPath("$.status", is("active")));

    verify(educationMajorService).getEducationMajor(EDUCATION_MAJOR_ID);
  }

  @Test
  @DisplayName("create education major succeeds")
  void createEducationMajorSucceeds() throws Exception {
    CreateEducationMajorRequest request = new CreateEducationMajorRequest();
    request.setName("Wizardry");
    request.setStatus(Status.active);

    given(educationMajorService
        .createEducationMajor(any(CreateEducationMajorRequest.class)))
        .willReturn(new EducationMajor("Wizardry", Status.active));

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
        .andExpect(jsonPath("$.name", is("Wizardry")))
        .andExpect(jsonPath("$.status", is("active")));

    verify(educationMajorService).createEducationMajor(any(CreateEducationMajorRequest.class));
  }

  @Test
  @DisplayName("update education major succeeds")
  void updateEducationMajorSucceeds() throws Exception {
    UpdateEducationMajorRequest request = new UpdateEducationMajorRequest();
    request.setName("Wizardry");
    request.setStatus(Status.active);

    given(educationMajorService
        .updateEducationMajor(anyLong(), any(UpdateEducationMajorRequest.class)))
        .willReturn(new EducationMajor("Wizardry", Status.active));

    mockMvc.perform(put(BASE_PATH + "/" + EDUCATION_MAJOR_ID)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$.name", is("Wizardry")))
        .andExpect(jsonPath("$.status", is("active")));

    verify(educationMajorService).updateEducationMajor(anyLong(), any(UpdateEducationMajorRequest.class));
  }

  @Test
  @DisplayName("delete education major by id succeeds")
  void deleteEducationMajorByIdSucceeds() throws Exception {

    given(educationMajorService
        .deleteEducationMajor(EDUCATION_MAJOR_ID))
        .willReturn(true);

    mockMvc.perform(delete(BASE_PATH + "/" + EDUCATION_MAJOR_ID)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$", is(true)));

    verify(educationMajorService).deleteEducationMajor(EDUCATION_MAJOR_ID);
  }
}
