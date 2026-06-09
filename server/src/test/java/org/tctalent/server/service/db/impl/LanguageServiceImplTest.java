package org.tctalent.server.service.db.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.CandidateLanguage;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.Language;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.SystemLanguage;
import org.tctalent.server.repository.db.CandidateLanguageRepository;
import org.tctalent.server.repository.db.LanguageRepository;
import org.tctalent.server.repository.db.SystemLanguageRepository;
import org.tctalent.server.request.language.CreateLanguageRequest;
import org.tctalent.server.request.language.SearchLanguageRequest;
import org.tctalent.server.request.language.UpdateLanguageRequest;
import org.tctalent.server.request.translation.CreateTranslationRequest;
import org.tctalent.server.response.DatePickerNames;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.service.db.TranslationService;
import org.tctalent.server.util.locale.LocaleHelper;

@ExtendWith(MockitoExtension.class)
class LanguageServiceImplTest {

  @Mock
  private CandidateLanguageRepository candidateLanguageRepository;

  @Mock
  private LanguageRepository languageRepository;

  @Mock
  private CountryService countryService;

  @Mock
  private SystemLanguageRepository systemLanguageRepository;

  @Mock
  private TranslationService translationService;

  private LanguageServiceImpl service() {
    return new LanguageServiceImpl(
        candidateLanguageRepository,
        languageRepository,
        countryService,
        systemLanguageRepository,
        translationService
    );
  }

  @Test
  @DisplayName("addSystemLanguage throws when language code is unknown")
  void addSystemLanguageThrowsWhenLanguageCodeUnknown() {
    assertThrows(
        NoSuchObjectException.class,
        () -> service().addSystemLanguage("not-a-real-language-code")
    );
  }

  @Test
  @DisplayName("addSystemLanguage throws when system language already exists")
  void addSystemLanguageThrowsWhenSystemLanguageAlreadyExists() {
    given(systemLanguageRepository.findByStatus(Status.active))
        .willReturn(List.of(systemLanguage("es")));

    assertThrows(
        EntityExistsException.class,
        () -> service().addSystemLanguage("es")
    );
  }

  @Test
  @DisplayName("addSystemLanguage creates translations and skips missing translation values")
  void addSystemLanguageCreatesTranslationsAndSkipsMissingValues() {
    Country translatedCountry = country(1L, "AA");
    Country untranslatedCountry = country(2L, "BB");
    Language translatedLanguage = language(10L, "Language AA");
    translatedLanguage.setIsoCode("aa");
    Language untranslatedLanguage = language(11L, "Language BB");
    untranslatedLanguage.setIsoCode("bb");

    List<String> createdTranslations = new ArrayList<>();

    doAnswer(invocation -> {
      CreateTranslationRequest request = invocation.getArgument(1);
      createdTranslations.add(
          request.getObjectType() + ":" + request.getObjectId() + ":" + request.getValue()
      );
      return null;
    }).when(translationService).createTranslation(isNull(), any(CreateTranslationRequest.class));

    try (MockedStatic<LocaleHelper> localeHelper = Mockito.mockStatic(LocaleHelper.class)) {
      localeHelper.when(() -> LocaleHelper.isKnownLanguageCode("zz")).thenReturn(true);
      localeHelper.when(() -> LocaleHelper.getCountryNameTranslations("zz"))
          .thenReturn(Map.of("AA", "Translated Country AA"));
      localeHelper.when(() -> LocaleHelper.getLanguageNameTranslations("zz"))
          .thenReturn(Map.of("aa", "Translated Language AA"));

      given(systemLanguageRepository.findByStatus(Status.active)).willReturn(List.of());
      given(countryService.listCountries(false))
          .willReturn(List.of(translatedCountry, untranslatedCountry));
      given(languageRepository.findByStatus(Status.active))
          .willReturn(List.of(translatedLanguage, untranslatedLanguage));

      SystemLanguage result = service().addSystemLanguage("zz");

      assertEquals("zz", result.getLanguage());
      assertEquals(
          List.of(
              "country:1:Translated Country AA",
              "language:10:Translated Language AA"
          ),
          createdTranslations
      );
      verify(systemLanguageRepository).save(any(SystemLanguage.class));
      verify(translationService).translate(
          List.of(translatedLanguage, untranslatedLanguage), "language");
    }
  }

  @Test
  @DisplayName("addSystemLanguageTranslations throws when system language is missing")
  void addSystemLanguageTranslationsThrowsWhenSystemLanguageMissing() {
    given(systemLanguageRepository.findByStatus(Status.active)).willReturn(List.of());

    assertThrows(
        NoSuchObjectException.class,
        () -> service().addSystemLanguageTranslations(
            "es",
            "language",
            inputStream("1,Hola\n")
        )
    );

    verify(translationService, never()).deleteTranslations(anyString(), anyString());
  }

  @Test
  @DisplayName("addSystemLanguageTranslations imports valid CSV rows")
  void addSystemLanguageTranslationsImportsValidRows() throws IOException {
    SystemLanguage systemLanguage = systemLanguage("es");
    List<String> createdTranslations = new ArrayList<>();

    given(systemLanguageRepository.findByStatus(Status.active))
        .willReturn(List.of(systemLanguage));

    doAnswer(invocation -> {
      CreateTranslationRequest request = invocation.getArgument(1);
      createdTranslations.add(
          request.getObjectType() + ":" + request.getObjectId() + ":" + request.getValue()
      );
      return null;
    }).when(translationService).createTranslation(isNull(), any(CreateTranslationRequest.class));

    SystemLanguage result = service().addSystemLanguageTranslations(
        "es",
        "language",
        inputStream("1,Hola\n2,Adios\n")
    );

    assertSame(systemLanguage, result);
    assertEquals(List.of("language:1:Hola", "language:2:Adios"), createdTranslations);
    verify(translationService).deleteTranslations("es", "language");
  }

  @Test
  @DisplayName("addSystemLanguageTranslations ignores empty token rows")
  void addSystemLanguageTranslationsIgnoresEmptyTokenRows() throws Exception {
    SystemLanguage systemLanguage = systemLanguage("es");

    given(systemLanguageRepository.findByStatus(Status.active))
        .willReturn(List.of(systemLanguage));

    try (MockedConstruction<CSVReader> ignored =
        Mockito.mockConstruction(CSVReader.class, (reader, context) ->
            given(reader.readNext()).willReturn(new String[0], (String[]) null)
        )) {

      SystemLanguage result = service().addSystemLanguageTranslations(
          "es",
          "language",
          inputStream("")
      );

      assertSame(systemLanguage, result);
      verify(translationService, never())
          .createTranslation(isNull(), any(CreateTranslationRequest.class));
    }
  }

  @Test
  @DisplayName("addSystemLanguageTranslations throws on wrong token count")
  void addSystemLanguageTranslationsThrowsOnWrongTokenCount() {
    given(systemLanguageRepository.findByStatus(Status.active))
        .willReturn(List.of(systemLanguage("es")));

    IOException exception = assertThrows(
        IOException.class,
        () -> service().addSystemLanguageTranslations(
            "es",
            "language",
            inputStream("1,Hola,Extra\n")
        )
    );

    assertEquals("Bad file format. Found 3 tokens", exception.getMessage());
  }

  @Test
  @DisplayName("addSystemLanguageTranslations throws on non numeric id")
  void addSystemLanguageTranslationsThrowsOnNonNumericId() {
    given(systemLanguageRepository.findByStatus(Status.active))
        .willReturn(List.of(systemLanguage("es")));

    IOException exception = assertThrows(
        IOException.class,
        () -> service().addSystemLanguageTranslations(
            "es",
            "language",
            inputStream("abc,Hola\n")
        )
    );

    assertTrue(exception.getMessage().startsWith("Bad file format. Non numeric id"));
  }

  @Test
  @DisplayName("addSystemLanguageTranslations wraps CSV validation errors")
  void addSystemLanguageTranslationsWrapsCsvValidationErrors() throws Exception {
    given(systemLanguageRepository.findByStatus(Status.active))
        .willReturn(List.of(systemLanguage("es")));

    try (MockedConstruction<CSVReader> ignored =
        Mockito.mockConstruction(CSVReader.class, (reader, context) ->
            given(reader.readNext()).willThrow(new CsvValidationException("bad csv"))
        )) {

      IOException exception = assertThrows(
          IOException.class,
          () -> service().addSystemLanguageTranslations(
              "es",
              "language",
              inputStream("")
          )
      );

      assertEquals("Bad file format: bad csv", exception.getMessage());
    }
  }

  @Test
  @DisplayName("listLanguages returns active languages and translates them")
  void listLanguagesReturnsActiveLanguagesAndTranslates() {
    List<Language> languages = List.of(
        language(1L, "English"),
        language(2L, "Spanish")
    );

    given(languageRepository.findByStatus(Status.active)).willReturn(languages);

    List<Language> result = service().listLanguages();

    assertSame(languages, result);
    verify(translationService).translate(languages, "language");
  }

  @Test
  @DisplayName("listSystemLanguages returns active system languages")
  void listSystemLanguagesReturnsActiveSystemLanguages() {
    List<SystemLanguage> systemLanguages = List.of(systemLanguage("en"), systemLanguage("es"));

    given(systemLanguageRepository.findByStatus(Status.active)).willReturn(systemLanguages);

    List<SystemLanguage> result = service().listSystemLanguages();

    assertSame(systemLanguages, result);
  }

  @Test
  @DisplayName("getLanguage by name delegates to repository")
  void getLanguageByNameReturnsRepositoryResult() {
    Language language = language(1L, "English");

    given(languageRepository.findByNameIgnoreCase("English")).willReturn(language);

    assertSame(language, service().getLanguage("English"));
  }

  @Test
  @DisplayName("getDatePickerNames returns month and weekday names")
  void getDatePickerNamesReturnsMonthAndWeekdayNames() {
    DatePickerNames names = service().getDatePickerNames("en");

    assertNotNull(names);
    assertEquals(12, names.getMonthNames().size());
    assertEquals(7, names.getWeekdayNames().size());
  }

  @Test
  @DisplayName("searchLanguages returns page without translation when request language is blank")
  void searchLanguagesReturnsPageWithoutTranslationWhenLanguageBlank() {
    SearchLanguageRequest request = mock(SearchLanguageRequest.class);
    PageRequest pageable = PageRequest.of(0, 10);
    List<Language> content = List.of(language(1L, "English"));
    Page<Language> page = new PageImpl<>(content, pageable, content.size());

    given(request.getPageRequest()).willReturn(pageable);
    given(request.getLanguage()).willReturn(" ");
    given(languageRepository.findAll(any(Specification.class), eq(pageable))).willReturn(page);

    Page<Language> result = service().searchLanguages(request);

    assertSame(page, result);
    verify(translationService, never()).translate(anyList(), eq("language"), anyString());
  }

  @Test
  @DisplayName("searchLanguages translates page content when request language is present")
  void searchLanguagesTranslatesWhenLanguagePresent() {
    SearchLanguageRequest request = mock(SearchLanguageRequest.class);
    PageRequest pageable = PageRequest.of(0, 10);
    List<Language> content = List.of(language(1L, "English"));
    Page<Language> page = new PageImpl<>(content, pageable, content.size());

    given(request.getPageRequest()).willReturn(pageable);
    given(request.getLanguage()).willReturn("es");
    given(languageRepository.findAll(any(Specification.class), eq(pageable))).willReturn(page);

    Page<Language> result = service().searchLanguages(request);

    assertSame(page, result);
    verify(translationService).translate(content, "language", "es");
  }

  @Test
  @DisplayName("findByIsoCode returns language")
  void findByIsoCodeReturnsLanguage() {
    Language language = language(1L, "English");

    given(languageRepository.findByIsoCode("en")).willReturn(Optional.of(language));

    assertSame(language, service().findByIsoCode("en"));
  }

  @Test
  @DisplayName("findByIsoCode throws when language is missing")
  void findByIsoCodeThrowsWhenMissing() {
    given(languageRepository.findByIsoCode("xx")).willReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class, () -> service().findByIsoCode("xx"));
  }

  @Test
  @DisplayName("getLanguage by id returns language")
  void getLanguageByIdReturnsLanguage() {
    Language language = language(1L, "English");

    given(languageRepository.findById(1L)).willReturn(Optional.of(language));

    assertSame(language, service().getLanguage(1L));
  }

  @Test
  @DisplayName("getLanguage by id throws when missing")
  void getLanguageByIdThrowsWhenMissing() {
    given(languageRepository.findById(404L)).willReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class, () -> service().getLanguage(404L));
  }

  @Test
  @DisplayName("createLanguage creates new language when name is unique")
  void createLanguageCreatesWhenUnique() {
    CreateLanguageRequest request = mock(CreateLanguageRequest.class);
    Language saved = language(1L, "Dari");

    given(request.getName()).willReturn("Dari");
    given(request.getStatus()).willReturn(Status.active);
    given(languageRepository.findByNameIgnoreCase("Dari")).willReturn(null);
    given(languageRepository.save(any(Language.class))).willReturn(saved);

    Language result = service().createLanguage(request);

    assertSame(saved, result);
  }

  @Test
  @DisplayName("createLanguage throws when name already exists")
  void createLanguageThrowsWhenDuplicateExists() {
    CreateLanguageRequest request = mock(CreateLanguageRequest.class);

    given(request.getName()).willReturn("English");
    given(request.getStatus()).willReturn(Status.active);
    given(languageRepository.findByNameIgnoreCase("English"))
        .willReturn(language(1L, "English"));

    assertThrows(EntityExistsException.class, () -> service().createLanguage(request));

    verify(languageRepository, never()).save(any(Language.class));
  }

  @Test
  @DisplayName("updateLanguage updates name and status")
  void updateLanguageUpdatesNameAndStatus() {
    Language language = language(1L, "Old");
    UpdateLanguageRequest request = mock(UpdateLanguageRequest.class);

    given(request.getName()).willReturn("New");
    given(request.getStatus()).willReturn(Status.deleted);
    given(languageRepository.findById(1L)).willReturn(Optional.of(language));
    given(languageRepository.findByNameIgnoreCase("New")).willReturn(null);
    given(languageRepository.save(language)).willReturn(language);

    Language result = service().updateLanguage(1L, request);

    assertSame(language, result);
    assertEquals("New", language.getName());
    assertEquals(Status.deleted, language.getStatus());
  }

  @Test
  @DisplayName("updateLanguage allows same name when existing language has same id")
  void updateLanguageAllowsSameNameForSameId() {
    Language language = language(1L, "English");
    Language existing = language(1L, "English");
    UpdateLanguageRequest request = mock(UpdateLanguageRequest.class);

    given(request.getName()).willReturn("English");
    given(request.getStatus()).willReturn(Status.active);
    given(languageRepository.findById(1L)).willReturn(Optional.of(language));
    given(languageRepository.findByNameIgnoreCase("English")).willReturn(existing);
    given(languageRepository.save(language)).willReturn(language);

    Language result = service().updateLanguage(1L, request);

    assertSame(language, result);
  }

  @Test
  @DisplayName("updateLanguage throws when language id is missing")
  void updateLanguageThrowsWhenMissing() {
    UpdateLanguageRequest request = mock(UpdateLanguageRequest.class);

    given(languageRepository.findById(404L)).willReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class, () -> service().updateLanguage(404L, request));

    verify(languageRepository, never()).save(any(Language.class));
  }

  @Test
  @DisplayName("updateLanguage throws when requested name belongs to another language")
  void updateLanguageThrowsWhenDuplicateNameBelongsToAnotherLanguage() {
    Language language = language(1L, "Old");
    Language duplicate = language(2L, "Duplicate");
    UpdateLanguageRequest request = mock(UpdateLanguageRequest.class);

    given(request.getName()).willReturn("Duplicate");
    given(languageRepository.findById(1L)).willReturn(Optional.of(language));
    given(languageRepository.findByNameIgnoreCase("Duplicate")).willReturn(duplicate);

    assertThrows(EntityExistsException.class, () -> service().updateLanguage(1L, request));

    verify(languageRepository, never()).save(any(Language.class));
  }

  @Test
  @DisplayName("deleteLanguage throws when language is referenced")
  void deleteLanguageThrowsWhenReferenced() {
    Language language = language(1L, "English");

    given(languageRepository.findById(1L)).willReturn(Optional.of(language));
    given(candidateLanguageRepository.findByLanguageId(1L))
        .willReturn(List.of(mock(CandidateLanguage.class)));

    assertThrows(EntityReferencedException.class, () -> service().deleteLanguage(1L));

    verify(languageRepository, never()).save(any(Language.class));
  }

  @Test
  @DisplayName("deleteLanguage marks existing unreferenced language as deleted")
  void deleteLanguageMarksExistingLanguageDeleted() {
    Language language = language(1L, "English");

    given(languageRepository.findById(1L)).willReturn(Optional.of(language));
    given(candidateLanguageRepository.findByLanguageId(1L)).willReturn(List.of());
    given(languageRepository.save(language)).willReturn(language);

    boolean result = service().deleteLanguage(1L);

    assertTrue(result);
    assertEquals(Status.deleted, language.getStatus());
    verify(languageRepository).save(language);
  }

  @Test
  @DisplayName("deleteLanguage returns false when language does not exist")
  void deleteLanguageReturnsFalseWhenMissing() {
    given(languageRepository.findById(404L)).willReturn(Optional.empty());
    given(candidateLanguageRepository.findByLanguageId(404L)).willReturn(List.of());

    boolean result = service().deleteLanguage(404L);

    assertFalse(result);
    verify(languageRepository, never()).save(any(Language.class));
  }

  @Test
  @DisplayName("updateIsoCodes saves known ISO codes and returns comma-separated missing names")
  void updateIsoCodesSavesKnownCodesAndReturnsMissingNames() {
    Language english = language(1L, " English ");
    Language missingOne = language(2L, " Definitely Not A Language One ");
    Language missingTwo = language(3L, " Definitely Not A Language Two ");

    given(languageRepository.findByStatus(Status.active))
        .willReturn(List.of(english, missingOne, missingTwo));

    String result = service().updateIsoCodes();

    assertEquals("en", english.getIsoCode());
    assertEquals("Definitely Not A Language One,Definitely Not A Language Two", result);
    verify(languageRepository).save(english);
    verify(languageRepository, never()).save(missingOne);
    verify(languageRepository, never()).save(missingTwo);
  }

  private static ByteArrayInputStream inputStream(String value) {
    return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
  }

  private static Language language(Long id, String name) {
    Language language = new Language(name, Status.active);
    language.setId(id);
    return language;
  }

  private static SystemLanguage systemLanguage(String languageCode) {
    return new SystemLanguage(languageCode);
  }

  private static Country country(Long id, String isoCode) {
    Country country = new Country();
    country.setId(id);
    country.setIsoCode(isoCode);
    return country;
  }
}