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
import org.tctalent.server.model.db.*;
import org.tctalent.server.request.country.SearchCountryRequest;
import org.tctalent.server.request.education.level.SearchEducationLevelRequest;
import org.tctalent.server.request.education.major.SearchEducationMajorRequest;
import org.tctalent.server.request.language.SearchLanguageRequest;
import org.tctalent.server.request.language.level.SearchLanguageLevelRequest;
import org.tctalent.server.request.occupation.SearchOccupationRequest;
import org.tctalent.server.request.survey.SearchSurveyTypeRequest;
import org.tctalent.server.request.translation.CreateTranslationRequest;
import org.tctalent.server.request.translation.UpdateTranslationRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.tctalent.server.api.admin.AdminApiTestUtil.getTranslationFile;
import static org.tctalent.server.data.CandidateTestData.getListOfOccupations;
import static org.tctalent.server.data.CountryTestData.getSourceCountryListA;
import static org.tctalent.server.data.LanguageTestData.getLanguageLevelList;
import static org.tctalent.server.data.LanguageTestData.getLanguageList;

/**
 * Unit tests for Translation Admin Api endpoints.
 *
 * @author Caroline Cameorn
 */
@WebMvcTest(TranslationAdminApi.class)
@AutoConfigureMockMvc
class TranslationAdminApiTest extends ApiTestBase {
  private static final String BASE_PATH = "/api/admin/translation";
  private static final String COUNTRY_PATH = "/country";
  private static final String NATIONALITY_PATH = "/nationality";
  private static final String LANGUAGE_PATH = "/language";
  private static final String LANGUAGE_LEVEL_PATH = "/language_level";
  private static final String OCCUPATION_PATH = "/occupation";
  private static final String EDUCATION_LEVEL_PATH = "/education_level";
  private static final String EDUCATION_MAJOR_PATH = "/education_major";
  private static final String SURVEY_TYPE_PATH = "/survey_type";
  private static final String TRANSLATION_PATH = "/file/{language}";

  private static final Translation translation = AdminApiTestUtil.getTranslation();
  private final Page<Country> countryPage = new PageImpl<>(
      getSourceCountryListA(), PageRequest.of(0, 10, Sort.unsorted()), 1);

  private final Page<Language> languagePage = new PageImpl<>(
      getLanguageList(), PageRequest.of(0, 10, Sort.unsorted()), 1);

  private final Page<LanguageLevel> languageLevelPage = new PageImpl<>(
      getLanguageLevelList(), PageRequest.of(0, 10, Sort.unsorted()), 1);

  private final Page<Occupation> occupationPage = new PageImpl<>(
      getListOfOccupations(), PageRequest.of(0, 10, Sort.unsorted()), 1);

  private final Page<EducationLevel> educationLevelPage = new PageImpl<>(
      AdminApiTestUtil.getEducationLevels(), PageRequest.of(0, 10, Sort.unsorted()), 1);

  private final Page<EducationMajor> educationMajorPage = new PageImpl<>(
      AdminApiTestUtil.getEducationMajors(), PageRequest.of(0, 10, Sort.unsorted()), 1);

  private final Page<SurveyType> surveyTypePage = new PageImpl<>(
      AdminApiTestUtil.getSurveyTypes(), PageRequest.of(0, 10, Sort.unsorted()), 1);

  @MockBean
  TranslationService translationService;
  @MockBean
  AuthService authService;
  @MockBean
  CountryService countryService;
  @MockBean
  LanguageService languageService;
  @MockBean
  LanguageLevelService languageLevelService;
  @MockBean
  OccupationService occupationService;
  @MockBean
  EducationLevelService educationLevelService;
  @MockBean
  EducationMajorService educationMajorService;
  @MockBean
  SurveyTypeService surveyTypeService;

  @Autowired
  MockMvc mockMvc;
  @Autowired
  ObjectMapper objectMapper;
  @Autowired
  TranslationAdminApi translationAdminApi;

  @BeforeEach
  void setUp() {
    configureAuthentication();
  }

  @Test
  public void testWebOnlyContextLoads() {
    assertThat(translationAdminApi).isNotNull();
  }

  @Test
  @DisplayName("search country translations succeeds")
  void searchCountryTranslationsSucceeds() throws Exception {
    SearchCountryRequest request = new SearchCountryRequest();

    given(countryService
            .searchCountries(any(SearchCountryRequest.class)))
            .willReturn(countryPage);

    mockMvc.perform(post(BASE_PATH + COUNTRY_PATH)
                    .with(csrf())
                    .header("Authorization", "Bearer " + "jwt-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .accept(MediaType.APPLICATION_JSON))

            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.totalElements", is(2)))
            .andExpect(jsonPath("$.totalPages", is(1)))
            .andExpect(jsonPath("$.number", is(0)))
            .andExpect(jsonPath("$.hasNext", is(false)))
            .andExpect(jsonPath("$.hasPrevious", is(false)))
            .andExpect(jsonPath("$.content", notNullValue()))
            .andExpect(jsonPath("$.content.[0].name", is("Lebanon")))
            .andExpect(jsonPath("$.content.[0].status", is("active")))
            .andExpect(jsonPath("$.content.[0].translatedName", is("Lebanon")));

    verify(countryService).searchCountries(any(SearchCountryRequest.class));
  }

  @Test
  @DisplayName("search nationality translations succeeds")
  void searchNationalityTranslationsSucceeds() throws Exception {
    SearchCountryRequest request = new SearchCountryRequest();

    given(countryService
            .searchCountries(any(SearchCountryRequest.class)))
            .willReturn(countryPage);

    mockMvc.perform(post(BASE_PATH + NATIONALITY_PATH)
                    .with(csrf())
                    .header("Authorization", "Bearer " + "jwt-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .accept(MediaType.APPLICATION_JSON))

            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.totalElements", is(2)))
            .andExpect(jsonPath("$.totalPages", is(1)))
            .andExpect(jsonPath("$.number", is(0)))
            .andExpect(jsonPath("$.hasNext", is(false)))
            .andExpect(jsonPath("$.hasPrevious", is(false)))
            .andExpect(jsonPath("$.content", notNullValue()))
            .andExpect(jsonPath("$.content.[0].name", is("Lebanon")))
            .andExpect(jsonPath("$.content.[0].status", is("active")))
            .andExpect(jsonPath("$.content.[0].translatedName", is("Lebanon")));

    verify(countryService).searchCountries(any(SearchCountryRequest.class));
  }

  @Test
  @DisplayName("search language translations succeeds")
  void searchLanguageTranslationsSucceeds() throws Exception {
    SearchLanguageRequest request = new SearchLanguageRequest();

    given(languageService
            .searchLanguages(any(SearchLanguageRequest.class)))
            .willReturn(languagePage);

    mockMvc.perform(post(BASE_PATH + LANGUAGE_PATH)
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
            .andExpect(jsonPath("$.content.[0].translatedName", is("Arabic")));

    verify(languageService).searchLanguages(any(SearchLanguageRequest.class));
  }

  @Test
  @DisplayName("search language level translations succeeds")
  void searchLanguageLevelTranslationsSucceeds() throws Exception {
    SearchLanguageLevelRequest request = new SearchLanguageLevelRequest();

    given(languageLevelService
            .searchLanguageLevels(any(SearchLanguageLevelRequest.class)))
            .willReturn(languageLevelPage);

    mockMvc.perform(post(BASE_PATH + LANGUAGE_LEVEL_PATH)
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
            .andExpect(jsonPath("$.content.[0].translatedName", is("Excellent")));

    verify(languageLevelService).searchLanguageLevels(any(SearchLanguageLevelRequest.class));
  }

  @Test
  @DisplayName("search occupation translations succeeds")
  void searchOccupationTranslationsSucceeds() throws Exception {
    SearchOccupationRequest request = new SearchOccupationRequest();

    given(occupationService
            .searchOccupations(any(SearchOccupationRequest.class)))
            .willReturn(occupationPage);

    mockMvc.perform(post(BASE_PATH + OCCUPATION_PATH)
                    .with(csrf())
                    .header("Authorization", "Bearer " + "jwt-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .accept(MediaType.APPLICATION_JSON))

            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.totalElements", is(2)))
            .andExpect(jsonPath("$.totalPages", is(1)))
            .andExpect(jsonPath("$.number", is(0)))
            .andExpect(jsonPath("$.hasNext", is(false)))
            .andExpect(jsonPath("$.hasPrevious", is(false)))
            .andExpect(jsonPath("$.content", notNullValue()))
            .andExpect(jsonPath("$.content.[0].name", is("Builder")))
            .andExpect(jsonPath("$.content.[0].status", is("active")))
            .andExpect(jsonPath("$.content.[0].translatedName", is("Builder")));

    verify(occupationService).searchOccupations(any(SearchOccupationRequest.class));
  }

  @Test
  @DisplayName("search education level translations succeeds")
  void searchEducationLevelTranslationsSucceeds() throws Exception {
    SearchEducationLevelRequest request = new SearchEducationLevelRequest();

    given(educationLevelService
            .searchEducationLevels(any(SearchEducationLevelRequest.class)))
            .willReturn(educationLevelPage);

    mockMvc.perform(post(BASE_PATH + EDUCATION_LEVEL_PATH)
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
            .andExpect(jsonPath("$.content.[0].name", is("Excellent")))
            .andExpect(jsonPath("$.content.[0].status", is("active")))
            .andExpect(jsonPath("$.content.[0].translatedName", is("Excellent")));

    verify(educationLevelService).searchEducationLevels(any(SearchEducationLevelRequest.class));
  }

  @Test
  @DisplayName("search education major translations succeeds")
  void searchEducationMajorTranslationsSucceeds() throws Exception {
    SearchEducationMajorRequest request = new SearchEducationMajorRequest();

    given(educationMajorService
            .searchEducationMajors(any(SearchEducationMajorRequest.class)))
            .willReturn(educationMajorPage);

    mockMvc.perform(post(BASE_PATH + EDUCATION_MAJOR_PATH)
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
            .andExpect(jsonPath("$.content.[0].name", is("Computer Science")))
            .andExpect(jsonPath("$.content.[0].status", is("active")))
            .andExpect(jsonPath("$.content.[0].translatedName", is("Computer Science")));

    verify(educationMajorService).searchEducationMajors(any(SearchEducationMajorRequest.class));
  }

  @Test
  @DisplayName("search survey type translations succeeds")
  void searchSurveyTypeTranslationsSucceeds() throws Exception {
    SearchSurveyTypeRequest request = new SearchSurveyTypeRequest();

    given(surveyTypeService
            .searchActiveSurveyTypes(any(SearchSurveyTypeRequest.class)))
            .willReturn(surveyTypePage);

    mockMvc.perform(post(BASE_PATH + SURVEY_TYPE_PATH)
                    .with(csrf())
                    .header("Authorization", "Bearer " + "jwt-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .accept(MediaType.APPLICATION_JSON))

            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.totalElements", is(2)))
            .andExpect(jsonPath("$.totalPages", is(1)))
            .andExpect(jsonPath("$.number", is(0)))
            .andExpect(jsonPath("$.hasNext", is(false)))
            .andExpect(jsonPath("$.hasPrevious", is(false)))
            .andExpect(jsonPath("$.content", notNullValue()))
            .andExpect(jsonPath("$.content.[0].name", is("Survey Type One")))
            .andExpect(jsonPath("$.content.[0].status", is("active")))
            .andExpect(jsonPath("$.content.[0].translatedName", is("Survey Type One")));

    verify(surveyTypeService).searchActiveSurveyTypes(any(SearchSurveyTypeRequest.class));
  }

  @Test
  @DisplayName("create translation succeeds")
  void createTranslationSucceeds() throws Exception {
    CreateTranslationRequest request = new CreateTranslationRequest();
    request.setObjectId(1L);
    request.setObjectType("Country");
    request.setLanguage("French");
    request.setValue("Australie");

    given(authService.getLoggedInUser()).willReturn(Optional.of(user));

    given(translationService
            .createTranslation(any(User.class), any(CreateTranslationRequest.class)))
            .willReturn(translation);

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
            .andExpect(jsonPath("$.objectId", is(1)))
            .andExpect(jsonPath("$.objectType", is("Country")))
            .andExpect(jsonPath("$.language", is("French")))
            .andExpect(jsonPath("$.value", is("Australie")));

    verify(authService).getLoggedInUser();
    verify(translationService).createTranslation(any(User.class), any(CreateTranslationRequest.class));
  }

  @Test
  @DisplayName("update translation succeeds")
  void updateTranslationSucceeds() throws Exception {
    UpdateTranslationRequest request = new UpdateTranslationRequest();
    request.setValue("Australie");

    given(translationService
            .updateTranslation(anyLong(), any(UpdateTranslationRequest.class)))
            .willReturn(translation);

    mockMvc.perform(put(BASE_PATH + "/" + 1L)
                    .with(csrf())
                    .header("Authorization", "Bearer " + "jwt-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .accept(MediaType.APPLICATION_JSON))

            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", notNullValue()))
            .andExpect(jsonPath("$.objectId", is(1)))
            .andExpect(jsonPath("$.objectType", is("Country")))
            .andExpect(jsonPath("$.language", is("French")))
            .andExpect(jsonPath("$.value", is("Australie")));

    verify(translationService).updateTranslation(anyLong(), any(UpdateTranslationRequest.class));
  }

  @Test
  @DisplayName("get translation file succeeds")
  void getTranslationFileSucceeds() throws Exception {

    given(translationService
            .getTranslationFile(anyString()))
            .willReturn(getTranslationFile());

    mockMvc.perform(get(BASE_PATH + "/" + TRANSLATION_PATH.replace("{language}", "english"))
                    .header("Authorization", "Bearer " + "jwt-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))

            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", notNullValue()));

    verify(translationService).getTranslationFile(anyString());
  }

  @Test
  @DisplayName("update translation file succeeds")
  void updateTranslationFileSucceeds() throws Exception {
    Map<String, Object> translation = new HashMap<>();

    mockMvc.perform(put(BASE_PATH + "/" + TRANSLATION_PATH.replace("{language}", "english"))
                    .with(csrf())
                    .header("Authorization", "Bearer " + "jwt-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(translation))
                    .accept(MediaType.APPLICATION_JSON))

            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", notNullValue()));

    verify(translationService).updateTranslationFile(anyString(), anyMap());
  }
}
