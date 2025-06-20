package org.tctalent.server.api.portal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Language;
import org.tctalent.server.model.db.SystemLanguage;
import org.tctalent.server.service.db.LanguageService;
import org.tctalent.server.service.db.TranslationService;

class LanguagePortalApiTest {

  @Mock
  private LanguageService languageService;

  @Mock
  private TranslationService translationService;

  @InjectMocks
  private LanguagePortalApi languagePortalApi;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testListAllLanguages_Success() {
    List<Language> languages = List.of(createSampleLanguage());
    when(languageService.listLanguages()).thenReturn(languages);

    List<Map<String, Object>> result = languagePortalApi.listAllLanguages();

    assertNotNull(result);
    assertEquals(1, result.size());
    Map<String, Object> languageDto = result.get(0);
    assertEquals(1L, languageDto.get("id"));
    assertEquals("English", languageDto.get("name"));
    verify(languageService).listLanguages();
  }

  @Test
  void testListAllLanguages_EmptyList() {
    when(languageService.listLanguages()).thenReturn(Collections.emptyList());

    List<Map<String, Object>> result = languagePortalApi.listAllLanguages();

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(languageService).listLanguages();
  }

  @Test
  void testGetLanguage_Success() {
    String languageName = "English";
    Language language = createSampleLanguage();
    when(languageService.getLanguage(languageName)).thenReturn(language);

    Map<String, Object> result = languagePortalApi.getLanguage(languageName);

    assertNotNull(result);
    assertEquals(1L, result.get("id"));
    assertEquals("English", result.get("name"));
    verify(languageService).getLanguage(languageName);
  }

  @Test
  void testGetLanguage_NoSuchObjectException() {
    String languageName = "NonExistent";
    when(languageService.getLanguage(languageName)).thenThrow(new NoSuchObjectException("Language not found"));

    NoSuchObjectException exception = assertThrows(
        NoSuchObjectException.class,
        () -> languagePortalApi.getLanguage(languageName)
    );

    assertEquals("Language not found", exception.getMessage());
    verify(languageService).getLanguage(languageName);
  }

  @Test
  void testGetSystemLanguages_Success() {
    List<SystemLanguage> systemLanguages = List.of(createSampleSystemLanguage());
    when(languageService.listSystemLanguages()).thenReturn(systemLanguages);

    List<Map<String, Object>> result = languagePortalApi.getSystemLanguages();

    assertNotNull(result);
    assertEquals(1, result.size());
    Map<String, Object> systemLanguageDto = result.get(0);
    assertEquals("en", systemLanguageDto.get("language"));
    verify(languageService).listSystemLanguages();
  }

  @Test
  void testGetSystemLanguages_EmptyList() {
    when(languageService.listSystemLanguages()).thenReturn(Collections.emptyList());

    List<Map<String, Object>> result = languagePortalApi.getSystemLanguages();

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(languageService).listSystemLanguages();
  }

  @Test
  void testGetTranslationFile_Success() {
    String language = "en";
    Map<String, Object> translations = Map.of("key", "value");
    when(translationService.getTranslationFile(language)).thenReturn(translations);

    Map<String, Object> result = languagePortalApi.getTranslationFile(language);

    assertNotNull(result);
    assertEquals("value", result.get("key"));
    verify(translationService).getTranslationFile(language);
  }

  @Test
  void testGetTranslationFile_NoSuchObjectException() {
    String language = "xx";
    when(translationService.getTranslationFile(language)).thenThrow(new NoSuchObjectException("Translation file not found"));

    NoSuchObjectException exception = assertThrows(
        NoSuchObjectException.class,
        () -> languagePortalApi.getTranslationFile(language)
    );

    assertEquals("Translation file not found", exception.getMessage());
    verify(translationService).getTranslationFile(language);
  }

  private Language createSampleLanguage() {
    Language language = new Language();
    language.setId(1L);
    language.setName("English");
    return language;
  }

  private SystemLanguage createSampleSystemLanguage() {
    SystemLanguage systemLanguage = new SystemLanguage();
    systemLanguage.setLanguage("en");
    return systemLanguage;
  }
}