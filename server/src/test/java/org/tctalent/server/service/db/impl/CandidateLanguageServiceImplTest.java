package org.tctalent.server.service.db.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateLanguage;
import org.tctalent.server.model.db.Language;
import org.tctalent.server.model.db.LanguageLevel;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidateLanguageRepository;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.LanguageLevelRepository;
import org.tctalent.server.repository.db.LanguageRepository;
import org.tctalent.server.request.candidate.language.CreateCandidateLanguageRequest;
import org.tctalent.server.request.candidate.language.UpdateCandidateLanguageRequest;
import org.tctalent.server.request.candidate.language.UpdateCandidateLanguagesRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateService;

@ExtendWith(MockitoExtension.class)
class CandidateLanguageServiceImplTest {

  @Mock
  private CandidateLanguageRepository candidateLanguageRepository;

  @Mock
  private LanguageRepository languageRepository;

  @Mock
  private CandidateRepository candidateRepository;

  @Mock
  private CandidateService candidateService;

  @Mock
  private LanguageLevelRepository languageLevelRepository;

  @Mock
  private AuthService authService;

  @InjectMocks
  private CandidateLanguageServiceImpl service;

  @Test
  void createCandidateLanguageThrowsWhenUserNotLoggedIn() {
    CreateCandidateLanguageRequest request = createRequest(1L, 2L, 3L, 4L);

    when(authService.getLoggedInUser()).thenReturn(Optional.empty());

    assertThrows(InvalidSessionException.class,
        () -> service.createCandidateLanguage(request));

    verifyNoInteractions(candidateService);
    verifyNoInteractions(languageRepository);
    verifyNoInteractions(languageLevelRepository);
    verifyNoInteractions(candidateLanguageRepository);
  }

  @Test
  void createCandidateLanguageThrowsWhenLanguageMissing() {
    CreateCandidateLanguageRequest request = createRequest(1L, 2L, 3L, 4L);
    Candidate candidate = candidate(1L);

    when(authService.getLoggedInUser()).thenReturn(Optional.of(new User()));
    when(candidateService.getCandidateFromRequest(1L)).thenReturn(candidate);
    when(languageRepository.findById(2L)).thenReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class,
        () -> service.createCandidateLanguage(request));

    verify(languageLevelRepository, never()).findById(any());
    verify(candidateLanguageRepository, never()).save(any());
    verify(candidateService, never()).save(candidate);
  }

  @Test
  void createCandidateLanguageThrowsWhenSpokenLevelMissing() {
    CreateCandidateLanguageRequest request = createRequest(1L, 2L, 3L, 4L);
    Candidate candidate = candidate(1L);
    Language language = language(2L, "English");

    when(authService.getLoggedInUser()).thenReturn(Optional.of(new User()));
    when(candidateService.getCandidateFromRequest(1L)).thenReturn(candidate);
    when(languageRepository.findById(2L)).thenReturn(Optional.of(language));
    when(languageLevelRepository.findById(3L)).thenReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class,
        () -> service.createCandidateLanguage(request));

    verify(candidateLanguageRepository, never()).save(any());
    verify(candidateService, never()).save(candidate);
  }

  @Test
  void createCandidateLanguageThrowsWhenWrittenLevelMissing() {
    CreateCandidateLanguageRequest request = createRequest(1L, 2L, 3L, 4L);
    Candidate candidate = candidate(1L);
    Language language = language(2L, "English");
    LanguageLevel spokenLevel = level(3L, "Advanced");

    when(authService.getLoggedInUser()).thenReturn(Optional.of(new User()));
    when(candidateService.getCandidateFromRequest(1L)).thenReturn(candidate);
    when(languageRepository.findById(2L)).thenReturn(Optional.of(language));
    when(languageLevelRepository.findById(3L)).thenReturn(Optional.of(spokenLevel));
    when(languageLevelRepository.findById(4L)).thenReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class,
        () -> service.createCandidateLanguage(request));

    verify(candidateLanguageRepository, never()).save(any());
    verify(candidateService, never()).save(candidate);
  }

  @Test
  void createCandidateLanguageCreatesAndSavesCandidateLanguage() {
    CreateCandidateLanguageRequest request = createRequest(1L, 2L, 3L, 4L);
    Candidate candidate = candidate(1L);
    Language language = language(2L, "English");
    LanguageLevel spokenLevel = level(3L, "Advanced");
    LanguageLevel writtenLevel = level(4L, "Intermediate");

    when(authService.getLoggedInUser()).thenReturn(Optional.of(new User()));
    when(candidateService.getCandidateFromRequest(1L)).thenReturn(candidate);
    when(languageRepository.findById(2L)).thenReturn(Optional.of(language));
    when(languageLevelRepository.findById(3L)).thenReturn(Optional.of(spokenLevel));
    when(languageLevelRepository.findById(4L)).thenReturn(Optional.of(writtenLevel));
    when(candidateLanguageRepository.save(any(CandidateLanguage.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    CandidateLanguage result = service.createCandidateLanguage(request);

    assertSame(candidate, result.getCandidate());
    assertSame(language, result.getLanguage());
    assertSame(spokenLevel, result.getSpokenLevel());
    assertSame(writtenLevel, result.getWrittenLevel());

    verify(candidateLanguageRepository).save(result);
    verify(candidateService).save(candidate);
  }

  @Test
  void updateCandidateLanguageThrowsWhenUserNotLoggedIn() {
    UpdateCandidateLanguageRequest request = updateRequest(10L, 2L, 3L, 4L);

    when(authService.getLoggedInUser()).thenReturn(Optional.empty());

    assertThrows(InvalidSessionException.class,
        () -> service.updateCandidateLanguage(request));

    verifyNoInteractions(candidateLanguageRepository);
  }

  @Test
  void updateCandidateLanguageThrowsWhenCandidateLanguageMissing() {
    UpdateCandidateLanguageRequest request = updateRequest(10L, 2L, 3L, 4L);

    when(authService.getLoggedInUser()).thenReturn(Optional.of(new User()));
    when(candidateLanguageRepository.findById(10L)).thenReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class,
        () -> service.updateCandidateLanguage(request));

    verify(languageRepository, never()).findById(any());
    verify(candidateLanguageRepository, never()).save(any());
  }

  @Test
  void updateCandidateLanguageThrowsWhenLanguageMissing() {
    UpdateCandidateLanguageRequest request = updateRequest(10L, 2L, 3L, 4L);
    CandidateLanguage candidateLanguage = candidateLanguage(10L, candidate(1L),
        language(99L, "Old"), level(8L, "Old spoken"), level(9L, "Old written"));

    when(authService.getLoggedInUser()).thenReturn(Optional.of(new User()));
    when(candidateLanguageRepository.findById(10L))
        .thenReturn(Optional.of(candidateLanguage));
    when(languageRepository.findById(2L)).thenReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class,
        () -> service.updateCandidateLanguage(request));

    verify(languageLevelRepository, never()).findById(any());
    verify(candidateLanguageRepository, never()).save(any());
  }

  @Test
  void updateCandidateLanguageThrowsWhenSpokenLevelMissing() {
    UpdateCandidateLanguageRequest request = updateRequest(10L, 2L, 3L, 4L);
    CandidateLanguage candidateLanguage = candidateLanguage(10L, candidate(1L),
        language(99L, "Old"), level(8L, "Old spoken"), level(9L, "Old written"));
    Language language = language(2L, "English");

    when(authService.getLoggedInUser()).thenReturn(Optional.of(new User()));
    when(candidateLanguageRepository.findById(10L))
        .thenReturn(Optional.of(candidateLanguage));
    when(languageRepository.findById(2L)).thenReturn(Optional.of(language));
    when(languageLevelRepository.findById(3L)).thenReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class,
        () -> service.updateCandidateLanguage(request));

    verify(candidateLanguageRepository, never()).save(any());
  }

  @Test
  void updateCandidateLanguageThrowsWhenWrittenLevelMissing() {
    UpdateCandidateLanguageRequest request = updateRequest(10L, 2L, 3L, 4L);
    CandidateLanguage candidateLanguage = candidateLanguage(10L, candidate(1L),
        language(99L, "Old"), level(8L, "Old spoken"), level(9L, "Old written"));
    Language language = language(2L, "English");
    LanguageLevel spokenLevel = level(3L, "Advanced");

    when(authService.getLoggedInUser()).thenReturn(Optional.of(new User()));
    when(candidateLanguageRepository.findById(10L))
        .thenReturn(Optional.of(candidateLanguage));
    when(languageRepository.findById(2L)).thenReturn(Optional.of(language));
    when(languageLevelRepository.findById(3L)).thenReturn(Optional.of(spokenLevel));
    when(languageLevelRepository.findById(4L)).thenReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class,
        () -> service.updateCandidateLanguage(request));

    verify(candidateLanguageRepository, never()).save(any());
  }

  @Test
  void updateCandidateLanguageUpdatesAndSavesCandidateLanguage() {
    Candidate candidate = candidate(1L);
    UpdateCandidateLanguageRequest request = updateRequest(10L, 2L, 3L, 4L);
    CandidateLanguage candidateLanguage = candidateLanguage(10L, candidate,
        language(99L, "Old"), level(8L, "Old spoken"), level(9L, "Old written"));
    Language language = language(2L, "English");
    LanguageLevel spokenLevel = level(3L, "Advanced");
    LanguageLevel writtenLevel = level(4L, "Intermediate");

    when(authService.getLoggedInUser()).thenReturn(Optional.of(new User()));
    when(candidateLanguageRepository.findById(10L))
        .thenReturn(Optional.of(candidateLanguage));
    when(languageRepository.findById(2L)).thenReturn(Optional.of(language));
    when(languageLevelRepository.findById(3L)).thenReturn(Optional.of(spokenLevel));
    when(languageLevelRepository.findById(4L)).thenReturn(Optional.of(writtenLevel));
    when(candidateLanguageRepository.save(candidateLanguage)).thenReturn(candidateLanguage);

    CandidateLanguage result = service.updateCandidateLanguage(request);

    assertSame(candidateLanguage, result);
    assertSame(language, result.getLanguage());
    assertSame(spokenLevel, result.getSpokenLevel());
    assertSame(writtenLevel, result.getWrittenLevel());

    verify(candidateLanguageRepository).save(candidateLanguage);
    verify(candidateService).save(candidate);
  }

  @Test
  void deleteCandidateLanguageThrowsWhenUserNotLoggedIn() {
    when(authService.getLoggedInUser()).thenReturn(Optional.empty());

    assertThrows(InvalidSessionException.class,
        () -> service.deleteCandidateLanguage(10L));

    verifyNoInteractions(candidateLanguageRepository);
  }

  @Test
  void deleteCandidateLanguageThrowsWhenCandidateLanguageMissing() {
    when(authService.getLoggedInUser()).thenReturn(Optional.of(new User()));
    when(candidateLanguageRepository.findByIdLoadCandidate(10L)).thenReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class,
        () -> service.deleteCandidateLanguage(10L));

    verify(candidateLanguageRepository, never()).delete(any());
  }

  @Test
  void deleteCandidateLanguageDeletesAndSavesCandidate() {
    Candidate candidate = candidate(1L);
    CandidateLanguage candidateLanguage = candidateLanguage(10L, candidate,
        language(2L, "English"), level(3L, "Advanced"), level(4L, "Intermediate"));

    when(authService.getLoggedInUser()).thenReturn(Optional.of(new User()));
    when(candidateLanguageRepository.findByIdLoadCandidate(10L))
        .thenReturn(Optional.of(candidateLanguage));

    service.deleteCandidateLanguage(10L);

    verify(candidateLanguageRepository).delete(candidateLanguage);
    verify(candidateService).save(candidate);
  }

  @Test
  void listReturnsRepositoryResult() {
    CandidateLanguage candidateLanguage = candidateLanguage(10L, candidate(1L),
        language(2L, "English"), level(3L, "Advanced"), level(4L, "Intermediate"));

    when(candidateLanguageRepository.findByCandidateId(1L))
        .thenReturn(List.of(candidateLanguage));

    assertEquals(List.of(candidateLanguage), service.list(1L));
  }

  @Test
  void updateCandidateLanguagesThrowsWhenCandidateNotLoggedIn() {
    UpdateCandidateLanguagesRequest request = bulkRequest(List.of());

    when(candidateService.getLoggedInCandidate()).thenReturn(Optional.empty());

    assertThrows(InvalidSessionException.class,
        () -> service.updateCandidateLanguages(request));

    verifyNoInteractions(candidateLanguageRepository);
  }

  @Test
  void updateCandidateLanguagesUpdatesExistingLanguageWhenLanguageUnchanged() {
    Candidate candidate = candidate(1L);
    Language english = language(10L, "English");
    LanguageLevel spokenLevel = level(3L, "Advanced");
    LanguageLevel writtenLevel = level(4L, "Intermediate");

    CandidateLanguage existing = candidateLanguage(10L, candidate, english,
        level(30L, "Old spoken"), level(40L, "Old written"));

    UpdateCandidateLanguageRequest update = updateRequest(null, 10L, 3L, 4L);
    UpdateCandidateLanguagesRequest request = bulkRequest(List.of(update));

    when(candidateService.getLoggedInCandidate()).thenReturn(Optional.of(candidate));
    when(candidateLanguageRepository.findByCandidateId(1L)).thenReturn(List.of(existing));
    when(languageLevelRepository.findByStatus(Status.active))
        .thenReturn(List.of(spokenLevel, writtenLevel));
    when(candidateLanguageRepository.save(existing)).thenReturn(existing);

    List<CandidateLanguage> result = service.updateCandidateLanguages(request);

    assertEquals(List.of(existing), result);
    assertSame(english, existing.getLanguage());
    assertSame(spokenLevel, existing.getSpokenLevel());
    assertSame(writtenLevel, existing.getWrittenLevel());

    verify(languageRepository, never()).findById(any());
    verify(candidateLanguageRepository, never()).deleteById(any());
    verify(candidateService).save(candidate);
  }

  @Test
  void updateCandidateLanguagesUpdatesExistingLanguageWhenLanguageChanged() {
    Candidate candidate = candidate(1L);
    Language oldLanguage = language(99L, "Old");
    Language newLanguage = language(10L, "English");
    LanguageLevel spokenLevel = level(3L, "Advanced");
    LanguageLevel writtenLevel = level(4L, "Intermediate");

    CandidateLanguage existing = candidateLanguage(10L, candidate, oldLanguage,
        level(30L, "Old spoken"), level(40L, "Old written"));

    UpdateCandidateLanguageRequest update = updateRequest(null, 10L, 3L, 4L);
    UpdateCandidateLanguagesRequest request = bulkRequest(List.of(update));

    when(candidateService.getLoggedInCandidate()).thenReturn(Optional.of(candidate));
    when(candidateLanguageRepository.findByCandidateId(1L)).thenReturn(List.of(existing));
    when(languageLevelRepository.findByStatus(Status.active))
        .thenReturn(List.of(spokenLevel, writtenLevel));
    when(languageRepository.findById(10L)).thenReturn(Optional.of(newLanguage));
    when(candidateLanguageRepository.save(existing)).thenReturn(existing);

    service.updateCandidateLanguages(request);

    assertSame(newLanguage, existing.getLanguage());
    assertSame(spokenLevel, existing.getSpokenLevel());
    assertSame(writtenLevel, existing.getWrittenLevel());

    verify(candidateService).save(candidate);
  }

  @Test
  void updateCandidateLanguagesThrowsWhenExistingChangedLanguageMissing() {
    Candidate candidate = candidate(1L);
    CandidateLanguage existing = candidateLanguage(10L, candidate,
        language(99L, "Old"), level(30L, "Old spoken"), level(40L, "Old written"));

    UpdateCandidateLanguageRequest update = updateRequest(null, 10L, 3L, 4L);
    UpdateCandidateLanguagesRequest request = bulkRequest(List.of(update));

    when(candidateService.getLoggedInCandidate()).thenReturn(Optional.of(candidate));
    when(candidateLanguageRepository.findByCandidateId(1L)).thenReturn(List.of(existing));
    when(languageLevelRepository.findByStatus(Status.active))
        .thenReturn(List.of(level(3L, "Advanced"), level(4L, "Intermediate")));
    when(languageRepository.findById(10L)).thenReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class,
        () -> service.updateCandidateLanguages(request));

    verify(candidateLanguageRepository, never()).save(any());
    verify(candidateService, never()).save(any());
  }

  @Test
  void updateCandidateLanguagesCreatesNewCandidateLanguageWhenNotExisting() {
    Candidate candidate = candidate(1L);
    Language english = language(10L, "English");
    LanguageLevel spokenLevel = level(3L, "Advanced");
    LanguageLevel writtenLevel = level(4L, "Intermediate");

    UpdateCandidateLanguageRequest update = updateRequest(null, 10L, 3L, 4L);
    UpdateCandidateLanguagesRequest request = bulkRequest(List.of(update));

    when(candidateService.getLoggedInCandidate()).thenReturn(Optional.of(candidate));
    when(candidateLanguageRepository.findByCandidateId(1L)).thenReturn(List.of());
    when(languageLevelRepository.findByStatus(Status.active))
        .thenReturn(List.of(spokenLevel, writtenLevel));
    when(languageRepository.findById(10L)).thenReturn(Optional.of(english));
    when(candidateLanguageRepository.save(any(CandidateLanguage.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    service.updateCandidateLanguages(request);

    ArgumentCaptor<CandidateLanguage> captor =
        ArgumentCaptor.forClass(CandidateLanguage.class);
    verify(candidateLanguageRepository).save(captor.capture());

    CandidateLanguage saved = captor.getValue();
    assertSame(candidate, saved.getCandidate());
    assertSame(english, saved.getLanguage());
    assertSame(spokenLevel, saved.getSpokenLevel());
    assertSame(writtenLevel, saved.getWrittenLevel());

    verify(candidateService).save(candidate);
  }

  @Test
  void updateCandidateLanguagesThrowsWhenNewLanguageMissing() {
    Candidate candidate = candidate(1L);

    UpdateCandidateLanguageRequest update = updateRequest(null, 10L, 3L, 4L);
    UpdateCandidateLanguagesRequest request = bulkRequest(List.of(update));

    when(candidateService.getLoggedInCandidate()).thenReturn(Optional.of(candidate));
    when(candidateLanguageRepository.findByCandidateId(1L)).thenReturn(List.of());
    when(languageLevelRepository.findByStatus(Status.active))
        .thenReturn(List.of(level(3L, "Advanced"), level(4L, "Intermediate")));
    when(languageRepository.findById(10L)).thenReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class,
        () -> service.updateCandidateLanguages(request));

    verify(candidateLanguageRepository, never()).save(any());
    verify(candidateService, never()).save(any());
  }

  @Test
  void updateCandidateLanguagesDeletesExistingLanguagesMissingFromRequest() {
    Candidate candidate = candidate(1L);

    CandidateLanguage first = candidateLanguage(10L, candidate,
        language(10L, "English"), level(3L, "Advanced"), level(4L, "Intermediate"));
    CandidateLanguage second = candidateLanguage(20L, candidate,
        language(20L, "Dari"), level(3L, "Advanced"), level(4L, "Intermediate"));

    UpdateCandidateLanguageRequest update = updateRequest(null, 10L, 3L, 4L);
    UpdateCandidateLanguagesRequest request = bulkRequest(List.of(update));

    when(candidateService.getLoggedInCandidate()).thenReturn(Optional.of(candidate));
    when(candidateLanguageRepository.findByCandidateId(1L)).thenReturn(List.of(first, second));
    when(languageLevelRepository.findByStatus(Status.active))
        .thenReturn(List.of(level(3L, "Advanced"), level(4L, "Intermediate")));
    when(candidateLanguageRepository.save(first)).thenReturn(first);

    service.updateCandidateLanguages(request);

    verify(candidateLanguageRepository).deleteById(20L);
    verify(candidateService).save(candidate);
  }

  @Test
  void updateCandidateLanguagesDeletesAllExistingLanguagesWhenRequestIsEmpty() {
    Candidate candidate = candidate(1L);

    CandidateLanguage first = candidateLanguage(10L, candidate,
        language(10L, "English"), level(3L, "Advanced"), level(4L, "Intermediate"));
    CandidateLanguage second = candidateLanguage(20L, candidate,
        language(20L, "Dari"), level(3L, "Advanced"), level(4L, "Intermediate"));

    UpdateCandidateLanguagesRequest request = bulkRequest(List.of());

    when(candidateService.getLoggedInCandidate()).thenReturn(Optional.of(candidate));
    when(candidateLanguageRepository.findByCandidateId(1L)).thenReturn(List.of(first, second));
    when(languageLevelRepository.findByStatus(Status.active)).thenReturn(List.of());

    List<CandidateLanguage> result = service.updateCandidateLanguages(request);

    assertEquals(List.of(first, second), result);
    verify(candidateLanguageRepository).deleteById(10L);
    verify(candidateLanguageRepository).deleteById(20L);
    verify(candidateService).save(candidate);
  }

  private static Candidate candidate(Long id) {
    Candidate candidate = new Candidate();
    candidate.setId(id);
    return candidate;
  }

  private static Language language(Long id, String name) {
    Language language = new Language();
    language.setId(id);
    language.setName(name);
    return language;
  }

  private static LanguageLevel level(Long id, String name) {
    LanguageLevel level = new LanguageLevel();
    level.setId(id);
    level.setName(name);
    return level;
  }

  private static CandidateLanguage candidateLanguage(
      Long id,
      Candidate candidate,
      Language language,
      LanguageLevel spokenLevel,
      LanguageLevel writtenLevel) {
    CandidateLanguage candidateLanguage = new CandidateLanguage();
    candidateLanguage.setId(id);
    candidateLanguage.setCandidate(candidate);
    candidateLanguage.setLanguage(language);
    candidateLanguage.setSpokenLevel(spokenLevel);
    candidateLanguage.setWrittenLevel(writtenLevel);
    return candidateLanguage;
  }

  private static CreateCandidateLanguageRequest createRequest(
      Long candidateId,
      Long languageId,
      Long spokenLevelId,
      Long writtenLevelId) {
    CreateCandidateLanguageRequest request = new CreateCandidateLanguageRequest();
    request.setCandidateId(candidateId);
    request.setLanguageId(languageId);
    request.setSpokenLevelId(spokenLevelId);
    request.setWrittenLevelId(writtenLevelId);
    return request;
  }

  private static UpdateCandidateLanguageRequest updateRequest(
      Long id,
      Long languageId,
      Long spokenLevelId,
      Long writtenLevelId) {
    UpdateCandidateLanguageRequest request = new UpdateCandidateLanguageRequest();
    request.setId(id);
    request.setLanguageId(languageId);
    request.setSpokenLevelId(spokenLevelId);
    request.setWrittenLevelId(writtenLevelId);
    return request;
  }

  private static UpdateCandidateLanguagesRequest bulkRequest(
      List<UpdateCandidateLanguageRequest> updates) {
    UpdateCandidateLanguagesRequest request = new UpdateCandidateLanguagesRequest();
    request.setUpdates(updates);
    return request;
  }
}