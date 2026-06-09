package org.tctalent.server.service.db.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.CandidateLanguage;
import org.tctalent.server.model.db.CefrLevel;
import org.tctalent.server.model.db.LanguageLevel;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.repository.db.CandidateLanguageRepository;
import org.tctalent.server.repository.db.LanguageLevelRepository;
import org.tctalent.server.request.language.level.CreateLanguageLevelRequest;
import org.tctalent.server.request.language.level.SearchLanguageLevelRequest;
import org.tctalent.server.request.language.level.UpdateLanguageLevelRequest;
import org.tctalent.server.service.db.TranslationService;

@ExtendWith(MockitoExtension.class)
class LanguageLevelServiceImplTest {

  @Mock
  private CandidateLanguageRepository candidateLanguageRepository;

  @Mock
  private LanguageLevelRepository languageLevelRepository;

  @Mock
  private TranslationService translationService;

  private LanguageLevelServiceImpl service() {
    return new LanguageLevelServiceImpl(
        candidateLanguageRepository,
        languageLevelRepository,
        translationService
    );
  }

  @Test
  @DisplayName("listLanguageLevels returns active language levels and translates them")
  void listLanguageLevelsReturnsActiveLanguageLevelsAndTranslates() {
    List<LanguageLevel> levels = List.of(
        languageLevel(1L, "Beginner", 1, CefrLevel.A1),
        languageLevel(2L, "Intermediate", 2, CefrLevel.B1)
    );

    given(languageLevelRepository.findByStatus(Status.active)).willReturn(levels);

    List<LanguageLevel> result = service().listLanguageLevels();

    assertSame(levels, result);
    verify(translationService).translate(levels, "language_level");
  }

  @Test
  @DisplayName("searchLanguageLevels returns page without translation when language is blank")
  void searchLanguageLevelsReturnsPageWithoutTranslationWhenLanguageBlank() {
    SearchLanguageLevelRequest request = mock(SearchLanguageLevelRequest.class);
    PageRequest pageable = PageRequest.of(0, 10);
    List<LanguageLevel> content = List.of(
        languageLevel(1L, "Beginner", 1, CefrLevel.A1)
    );
    Page<LanguageLevel> page = new PageImpl<>(content, pageable, content.size());

    given(request.getPageRequest()).willReturn(pageable);
    given(request.getLanguage()).willReturn(" ");
    given(languageLevelRepository.findAll(any(Specification.class), eq(pageable)))
        .willReturn(page);

    Page<LanguageLevel> result = service().searchLanguageLevels(request);

    assertSame(page, result);
    verify(translationService, never())
        .translate(anyList(), eq("language_level"), anyString());
  }

  @Test
  @DisplayName("searchLanguageLevels translates page content when language is present")
  void searchLanguageLevelsTranslatesWhenLanguagePresent() {
    SearchLanguageLevelRequest request = mock(SearchLanguageLevelRequest.class);
    PageRequest pageable = PageRequest.of(0, 10);
    List<LanguageLevel> content = List.of(
        languageLevel(1L, "Beginner", 1, CefrLevel.A1)
    );
    Page<LanguageLevel> page = new PageImpl<>(content, pageable, content.size());

    given(request.getPageRequest()).willReturn(pageable);
    given(request.getLanguage()).willReturn("es");
    given(languageLevelRepository.findAll(any(Specification.class), eq(pageable)))
        .willReturn(page);

    Page<LanguageLevel> result = service().searchLanguageLevels(request);

    assertSame(page, result);
    verify(translationService).translate(content, "language_level", "es");
  }

  @Test
  @DisplayName("findByLevel returns language level")
  void findByLevelReturnsLanguageLevel() {
    LanguageLevel level = languageLevel(1L, "Beginner", 1, CefrLevel.A1);

    given(languageLevelRepository.findByLevel(1)).willReturn(Optional.of(level));

    LanguageLevel result = service().findByLevel(1);

    assertSame(level, result);
  }

  @Test
  @DisplayName("findByLevel throws when language level is missing")
  void findByLevelThrowsWhenMissing() {
    given(languageLevelRepository.findByLevel(99)).willReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class, () -> service().findByLevel(99));
  }

  @Test
  @DisplayName("getLanguageLevel returns language level")
  void getLanguageLevelReturnsLanguageLevel() {
    LanguageLevel level = languageLevel(1L, "Beginner", 1, CefrLevel.A1);

    given(languageLevelRepository.findById(1L)).willReturn(Optional.of(level));

    LanguageLevel result = service().getLanguageLevel(1L);

    assertSame(level, result);
  }

  @Test
  @DisplayName("getLanguageLevel throws when language level is missing")
  void getLanguageLevelThrowsWhenMissing() {
    given(languageLevelRepository.findById(404L)).willReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class, () -> service().getLanguageLevel(404L));
  }

  @Test
  @DisplayName("createLanguageLevel creates language level when name and level are unique")
  void createLanguageLevelCreatesWhenUnique() {
    CreateLanguageLevelRequest request = mock(CreateLanguageLevelRequest.class);
    LanguageLevel saved = languageLevel(1L, "Beginner", 1, CefrLevel.A1);

    given(request.getName()).willReturn("Beginner");
    given(request.getStatus()).willReturn(Status.active);
    given(request.getLevel()).willReturn(1);
    given(request.getCefrLevel()).willReturn(CefrLevel.A1);
    given(languageLevelRepository.findByNameIgnoreCase("Beginner")).willReturn(null);
    given(languageLevelRepository.findByLevelIgnoreCase(1)).willReturn(null);
    given(languageLevelRepository.save(any(LanguageLevel.class))).willReturn(saved);

    LanguageLevel result = service().createLanguageLevel(request);

    assertSame(saved, result);
  }

  @Test
  @DisplayName("createLanguageLevel throws when name already exists")
  void createLanguageLevelThrowsWhenNameExists() {
    CreateLanguageLevelRequest request = mock(CreateLanguageLevelRequest.class);

    given(request.getName()).willReturn("Beginner");
    given(request.getStatus()).willReturn(Status.active);
    given(request.getLevel()).willReturn(1);
    given(request.getCefrLevel()).willReturn(CefrLevel.A1);
    given(languageLevelRepository.findByNameIgnoreCase("Beginner"))
        .willReturn(languageLevel(1L, "Beginner", 1, CefrLevel.A1));

    assertThrows(EntityExistsException.class, () -> service().createLanguageLevel(request));

    verify(languageLevelRepository, never()).save(any(LanguageLevel.class));
    verify(languageLevelRepository, never()).findByLevelIgnoreCase(1);
  }

  @Test
  @DisplayName("createLanguageLevel throws when level already exists")
  void createLanguageLevelThrowsWhenLevelExists() {
    CreateLanguageLevelRequest request = mock(CreateLanguageLevelRequest.class);

    given(request.getName()).willReturn("Beginner");
    given(request.getStatus()).willReturn(Status.active);
    given(request.getLevel()).willReturn(1);
    given(request.getCefrLevel()).willReturn(CefrLevel.A1);
    given(languageLevelRepository.findByNameIgnoreCase("Beginner")).willReturn(null);
    given(languageLevelRepository.findByLevelIgnoreCase(1))
        .willReturn(languageLevel(2L, "Other", 1, CefrLevel.A1));

    assertThrows(EntityExistsException.class, () -> service().createLanguageLevel(request));

    verify(languageLevelRepository, never()).save(any(LanguageLevel.class));
  }

  @Test
  @DisplayName("updateLanguageLevel updates editable fields")
  void updateLanguageLevelUpdatesEditableFields() {
    LanguageLevel level = languageLevel(1L, "Old", 1, CefrLevel.A1);
    UpdateLanguageLevelRequest request = mock(UpdateLanguageLevelRequest.class);

    given(request.getName()).willReturn("New");
    given(request.getLevel()).willReturn(2);
    given(request.getStatus()).willReturn(Status.deleted);
    given(request.getCefrLevel()).willReturn(CefrLevel.B1);
    given(languageLevelRepository.findById(1L)).willReturn(Optional.of(level));
    given(languageLevelRepository.findByNameIgnoreCase("New")).willReturn(null);
    given(languageLevelRepository.findByLevelIgnoreCase(2)).willReturn(null);
    given(languageLevelRepository.save(level)).willReturn(level);

    LanguageLevel result = service().updateLanguageLevel(1L, request);

    assertSame(level, result);
    assertEquals("New", level.getName());
    assertEquals(2, level.getLevel());
    assertEquals(Status.deleted, level.getStatus());
    assertEquals(CefrLevel.B1, level.getCefrLevel());
  }

  @Test
  @DisplayName("updateLanguageLevel allows same name and same level for same id")
  void updateLanguageLevelAllowsSameNameAndLevelForSameId() {
    LanguageLevel level = languageLevel(1L, "Beginner", 1, CefrLevel.A1);
    LanguageLevel sameName = languageLevel(1L, "Beginner", 1, CefrLevel.A1);
    LanguageLevel sameLevel = languageLevel(1L, "Beginner", 1, CefrLevel.A1);
    UpdateLanguageLevelRequest request = mock(UpdateLanguageLevelRequest.class);

    given(request.getName()).willReturn("Beginner");
    given(request.getLevel()).willReturn(1);
    given(request.getStatus()).willReturn(Status.active);
    given(request.getCefrLevel()).willReturn(CefrLevel.A1);
    given(languageLevelRepository.findById(1L)).willReturn(Optional.of(level));
    given(languageLevelRepository.findByNameIgnoreCase("Beginner")).willReturn(sameName);
    given(languageLevelRepository.findByLevelIgnoreCase(1)).willReturn(sameLevel);
    given(languageLevelRepository.save(level)).willReturn(level);

    LanguageLevel result = service().updateLanguageLevel(1L, request);

    assertSame(level, result);
    verify(languageLevelRepository).save(level);
  }

  @Test
  @DisplayName("updateLanguageLevel throws when id is missing")
  void updateLanguageLevelThrowsWhenMissing() {
    UpdateLanguageLevelRequest request = mock(UpdateLanguageLevelRequest.class);

    given(languageLevelRepository.findById(404L)).willReturn(Optional.empty());

    assertThrows(
        NoSuchObjectException.class,
        () -> service().updateLanguageLevel(404L, request)
    );

    verify(languageLevelRepository, never()).save(any(LanguageLevel.class));
  }

  @Test
  @DisplayName("updateLanguageLevel throws when requested name belongs to another level")
  void updateLanguageLevelThrowsWhenNameBelongsToAnotherLevel() {
    LanguageLevel level = languageLevel(1L, "Old", 1, CefrLevel.A1);
    LanguageLevel duplicateName = languageLevel(2L, "Duplicate", 2, CefrLevel.B1);
    UpdateLanguageLevelRequest request = mock(UpdateLanguageLevelRequest.class);

    given(request.getName()).willReturn("Duplicate");
    given(request.getLevel()).willReturn(1);
    given(languageLevelRepository.findById(1L)).willReturn(Optional.of(level));
    given(languageLevelRepository.findByNameIgnoreCase("Duplicate"))
        .willReturn(duplicateName);

    assertThrows(
        EntityExistsException.class,
        () -> service().updateLanguageLevel(1L, request)
    );

    verify(languageLevelRepository, never()).save(any(LanguageLevel.class));
    verify(languageLevelRepository, never()).findByLevelIgnoreCase(1);
  }

  @Test
  @DisplayName("updateLanguageLevel throws when requested numeric level belongs to another row")
  void updateLanguageLevelThrowsWhenNumericLevelBelongsToAnotherRow() {
    LanguageLevel level = languageLevel(1L, "Old", 1, CefrLevel.A1);
    LanguageLevel duplicateLevel = languageLevel(2L, "Other", 2, CefrLevel.B1);
    UpdateLanguageLevelRequest request = mock(UpdateLanguageLevelRequest.class);

    given(request.getName()).willReturn("Unique");
    given(request.getLevel()).willReturn(2);
    given(languageLevelRepository.findById(1L)).willReturn(Optional.of(level));
    given(languageLevelRepository.findByNameIgnoreCase("Unique")).willReturn(null);
    given(languageLevelRepository.findByLevelIgnoreCase(2)).willReturn(duplicateLevel);

    assertThrows(
        EntityExistsException.class,
        () -> service().updateLanguageLevel(1L, request)
    );

    verify(languageLevelRepository, never()).save(any(LanguageLevel.class));
  }

  @Test
  @DisplayName("deleteLanguageLevel throws when level is referenced")
  void deleteLanguageLevelThrowsWhenReferenced() {
    LanguageLevel level = languageLevel(1L, "Beginner", 1, CefrLevel.A1);

    given(languageLevelRepository.findById(1L)).willReturn(Optional.of(level));
    given(candidateLanguageRepository.findByLanguageLevelId(1L))
        .willReturn(List.of(mock(CandidateLanguage.class)));

    assertThrows(EntityReferencedException.class, () -> service().deleteLanguageLevel(1L));

    verify(languageLevelRepository, never()).save(any(LanguageLevel.class));
  }

  @Test
  @DisplayName("deleteLanguageLevel marks existing unreferenced level as deleted")
  void deleteLanguageLevelMarksExistingUnreferencedLevelDeleted() {
    LanguageLevel level = languageLevel(1L, "Beginner", 1, CefrLevel.A1);

    given(languageLevelRepository.findById(1L)).willReturn(Optional.of(level));
    given(candidateLanguageRepository.findByLanguageLevelId(1L)).willReturn(List.of());
    given(languageLevelRepository.save(level)).willReturn(level);

    boolean result = service().deleteLanguageLevel(1L);

    assertTrue(result);
    assertEquals(Status.deleted, level.getStatus());
    verify(languageLevelRepository).save(level);
  }

  @Test
  @DisplayName("deleteLanguageLevel returns false when level does not exist")
  void deleteLanguageLevelReturnsFalseWhenMissing() {
    given(languageLevelRepository.findById(404L)).willReturn(Optional.empty());
    given(candidateLanguageRepository.findByLanguageLevelId(404L)).willReturn(List.of());

    boolean result = service().deleteLanguageLevel(404L);

    assertFalse(result);
    verify(languageLevelRepository, never()).save(any(LanguageLevel.class));
  }

  private static LanguageLevel languageLevel(
      Long id,
      String name,
      int level,
      CefrLevel cefrLevel
  ) {
    LanguageLevel languageLevel = new LanguageLevel(name, Status.active, level, cefrLevel);
    languageLevel.setId(id);
    return languageLevel;
  }
}