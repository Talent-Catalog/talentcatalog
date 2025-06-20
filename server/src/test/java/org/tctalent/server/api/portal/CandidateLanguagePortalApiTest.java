package org.tctalent.server.api.portal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.CandidateLanguage;
import org.tctalent.server.model.db.Language;
import org.tctalent.server.model.db.LanguageLevel;
import org.tctalent.server.request.candidate.language.CreateCandidateLanguageRequest;
import org.tctalent.server.request.candidate.language.UpdateCandidateLanguagesRequest;
import org.tctalent.server.service.db.CandidateLanguageService;

class CandidateLanguagePortalApiTest {

  @Mock
  private CandidateLanguageService candidateLanguageService;

  @InjectMocks
  private CandidateLanguagePortalApi candidateLanguagePortalApi;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testCreateCandidateLanguage_Success() {
    CreateCandidateLanguageRequest request = new CreateCandidateLanguageRequest();
    CandidateLanguage candidateLanguage = createSampleCandidateLanguage();
    when(candidateLanguageService.createCandidateLanguage(any(CreateCandidateLanguageRequest.class)))
        .thenReturn(candidateLanguage);

    Map<String, Object> result = candidateLanguagePortalApi.createCandidateLanguage(request);

    assertNotNull(result);
    assertEquals(1L, result.get("id"));

    Object languageObj = result.get("language");
    assertInstanceOf(Map.class, languageObj, "language should be a map");
    Map<?, ?> language = (Map<?, ?>) languageObj;
    assertEquals(1L, language.get("id"));
    assertEquals("English", language.get("name"));

    Object writtenLevelObj = result.get("writtenLevel");
    assertInstanceOf(Map.class, writtenLevelObj, "writtenLevel should be a map");
    Map<?, ?> writtenLevel = (Map<?, ?>) writtenLevelObj;
    assertEquals(1L, writtenLevel.get("id"));
    assertEquals(1, writtenLevel.get("level"));

    Object spokenLevelObj = result.get("spokenLevel");
    assertInstanceOf(Map.class, spokenLevelObj, "spokenLevel should be a map");
    Map<?, ?> spokenLevel = (Map<?, ?>) spokenLevelObj;
    assertEquals(2L, spokenLevel.get("id"));
    assertEquals(2, spokenLevel.get("level"));

    verify(candidateLanguageService).createCandidateLanguage(request);
  }


  @Test
  void testUpdateCandidateLanguage_Success() {
    UpdateCandidateLanguagesRequest request = new UpdateCandidateLanguagesRequest();
    List<CandidateLanguage> candidateLanguages = List.of(createSampleCandidateLanguage());
    when(candidateLanguageService.updateCandidateLanguages(any(UpdateCandidateLanguagesRequest.class)))
        .thenReturn(candidateLanguages);

    List<Map<String, Object>> result = candidateLanguagePortalApi.updateCandidateLanguage(request);

    assertNotNull(result);
    assertEquals(1, result.size());

    Map<String, Object> languageDto = result.get(0);
    assertEquals(1L, languageDto.get("id"));

    Object languageObj = languageDto.get("language");
    assertInstanceOf(Map.class, languageObj, "language should be a map");
    Map<?, ?> language = (Map<?, ?>) languageObj;
    assertEquals(1L, language.get("id"));
    assertEquals("English", language.get("name"));

    Object writtenLevelObj = languageDto.get("writtenLevel");
    assertInstanceOf(Map.class, writtenLevelObj, "writtenLevel should be a map");
    Map<?, ?> writtenLevel = (Map<?, ?>) writtenLevelObj;
    assertEquals(1L, writtenLevel.get("id"));
    assertEquals(1, writtenLevel.get("level"));

    Object spokenLevelObj = languageDto.get("spokenLevel");
    assertInstanceOf(Map.class, spokenLevelObj, "spokenLevel should be a map");
    Map<?, ?> spokenLevel = (Map<?, ?>) spokenLevelObj;
    assertEquals(2L, spokenLevel.get("id"));
    assertEquals(2, spokenLevel.get("level"));

    verify(candidateLanguageService).updateCandidateLanguages(request);
  }


  @Test
  void testDeleteCandidateLanguage_Success() {
    Long id = 1L;
    doNothing().when(candidateLanguageService).deleteCandidateLanguage(id);

    ResponseEntity result = candidateLanguagePortalApi.deleteCandidateLanguage(id);

    assertNotNull(result);
    assertEquals(200, result.getStatusCodeValue());
    verify(candidateLanguageService).deleteCandidateLanguage(id);
  }

  @Test
  void testDeleteCandidateLanguage_NoSuchObjectException() {
    Long id = 1L;
    doThrow(new NoSuchObjectException("No such candidate language")).when(candidateLanguageService).deleteCandidateLanguage(id);

    NoSuchObjectException exception = assertThrows(
        NoSuchObjectException.class,
        () -> candidateLanguagePortalApi.deleteCandidateLanguage(id)
    );

    assertEquals("No such candidate language", exception.getMessage());
    verify(candidateLanguageService).deleteCandidateLanguage(id);
  }

  private CandidateLanguage createSampleCandidateLanguage() {
    CandidateLanguage candidateLanguage = new CandidateLanguage();
    candidateLanguage.setId(1L);
    Language language = new Language();
    language.setId(1L);
    language.setName("English");
    candidateLanguage.setLanguage(language);
    LanguageLevel writtenLevel = new LanguageLevel();
    writtenLevel.setId(1L);
    writtenLevel.setLevel(1);
    candidateLanguage.setWrittenLevel(writtenLevel);
    LanguageLevel spokenLevel = new LanguageLevel();
    spokenLevel.setId(2L);
    spokenLevel.setLevel(2);
    candidateLanguage.setSpokenLevel(spokenLevel);
    return candidateLanguage;
  }
}