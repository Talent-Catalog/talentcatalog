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
import org.tctalent.server.model.db.LanguageLevel;
import org.tctalent.server.service.db.LanguageLevelService;

class LanguageLevelPortalApiTest {

  @Mock
  private LanguageLevelService languageLevelService;

  @InjectMocks
  private LanguageLevelPortalApi languageLevelPortalApi;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testListAllLanguageLevels_Success() {
    List<LanguageLevel> languageLevels = List.of(createSampleLanguageLevel());
    when(languageLevelService.listLanguageLevels()).thenReturn(languageLevels);

    List<Map<String, Object>> result = languageLevelPortalApi.listAllLanguageLevels();

    assertNotNull(result);
    assertEquals(1, result.size());
    Map<String, Object> languageLevelDto = result.get(0);
    assertEquals(1L, languageLevelDto.get("id"));
    assertEquals("Advanced", languageLevelDto.get("name"));
    assertEquals(5, languageLevelDto.get("level"));
    verify(languageLevelService).listLanguageLevels();
  }

  @Test
  void testListAllLanguageLevels_EmptyList() {
    when(languageLevelService.listLanguageLevels()).thenReturn(Collections.emptyList());

    List<Map<String, Object>> result = languageLevelPortalApi.listAllLanguageLevels();

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(languageLevelService).listLanguageLevels();
  }

  private LanguageLevel createSampleLanguageLevel() {
    LanguageLevel languageLevel = new LanguageLevel();
    languageLevel.setId(1L);
    languageLevel.setName("Advanced");
    languageLevel.setLevel(5);
    return languageLevel;
  }
}