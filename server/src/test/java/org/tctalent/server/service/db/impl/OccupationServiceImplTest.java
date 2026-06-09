package org.tctalent.server.service.db.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import org.tctalent.server.exception.NotImplementedException;
import org.tctalent.server.model.db.CandidateOccupation;
import org.tctalent.server.model.db.Occupation;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.repository.db.CandidateOccupationRepository;
import org.tctalent.server.repository.db.OccupationRepository;
import org.tctalent.server.request.occupation.CreateOccupationRequest;
import org.tctalent.server.request.occupation.SearchOccupationRequest;
import org.tctalent.server.request.occupation.UpdateOccupationRequest;
import org.tctalent.server.service.db.TranslationService;
import org.tctalent.server.util.dto.DtoBuilder;

@ExtendWith(MockitoExtension.class)
class OccupationServiceImplTest {

  @Mock
  private CandidateOccupationRepository candidateOccupationRepository;

  @Mock
  private OccupationRepository occupationRepository;

  @Mock
  private TranslationService translationService;

  private OccupationServiceImpl service() {
    return new OccupationServiceImpl(
        candidateOccupationRepository,
        occupationRepository,
        translationService
    );
  }

  @Test
  @DisplayName("findByIsco08Code returns occupation")
  void findByIsco08CodeReturnsOccupation() {
    Occupation occupation = occupation(1L, "Software Developer", Status.active);

    given(occupationRepository.findByIsco08Code("2512"))
        .willReturn(Optional.of(occupation));

    Occupation result = service().findByIsco08Code("2512");

    assertSame(occupation, result);
  }

  @Test
  @DisplayName("findByIsco08Code throws when missing")
  void findByIsco08CodeThrowsWhenMissing() {
    given(occupationRepository.findByIsco08Code("9999"))
        .willReturn(Optional.empty());

    assertThrows(
        NoSuchObjectException.class,
        () -> service().findByIsco08Code("9999")
    );
  }

  @Test
  @DisplayName("listOccupations returns active occupations and translates them")
  void listOccupationsReturnsActiveOccupationsAndTranslates() {
    List<Occupation> occupations = List.of(
        occupation(1L, "Developer", Status.active),
        occupation(2L, "Designer", Status.active)
    );

    given(occupationRepository.findByStatus(Status.active)).willReturn(occupations);

    List<Occupation> result = service().listOccupations();

    assertSame(occupations, result);
    verify(translationService).translate(occupations, "occupation");
  }

  @Test
  @DisplayName("searchOccupations returns page without language translation when language is blank")
  void searchOccupationsReturnsPageWithoutTranslationWhenLanguageBlank() {
    SearchOccupationRequest request = mock(SearchOccupationRequest.class);
    PageRequest pageable = PageRequest.of(0, 10);
    List<Occupation> content = List.of(occupation(1L, "Developer", Status.active));
    Page<Occupation> page = new PageImpl<>(content, pageable, content.size());

    given(request.getPageRequest()).willReturn(pageable);
    given(request.getLanguage()).willReturn(" ");
    given(occupationRepository.findAll(any(Specification.class), eq(pageable)))
        .willReturn(page);

    Page<Occupation> result = service().searchOccupations(request);

    assertSame(page, result);
    verify(translationService, never()).translate(content, "occupation", " ");
  }

  @Test
  @DisplayName("searchOccupations translates page content when language is present")
  void searchOccupationsTranslatesWhenLanguagePresent() {
    SearchOccupationRequest request = mock(SearchOccupationRequest.class);
    PageRequest pageable = PageRequest.of(0, 10);
    List<Occupation> content = List.of(occupation(1L, "Developer", Status.active));
    Page<Occupation> page = new PageImpl<>(content, pageable, content.size());

    given(request.getPageRequest()).willReturn(pageable);
    given(request.getLanguage()).willReturn("es");
    given(occupationRepository.findAll(any(Specification.class), eq(pageable)))
        .willReturn(page);

    Page<Occupation> result = service().searchOccupations(request);

    assertSame(page, result);
    verify(translationService).translate(content, "occupation", "es");
  }

  @Test
  @DisplayName("getOccupation returns occupation")
  void getOccupationReturnsOccupation() {
    Occupation occupation = occupation(1L, "Developer", Status.active);

    given(occupationRepository.findById(1L)).willReturn(Optional.of(occupation));

    Occupation result = service().getOccupation(1L);

    assertSame(occupation, result);
  }

  @Test
  @DisplayName("getOccupation throws when missing")
  void getOccupationThrowsWhenMissing() {
    given(occupationRepository.findById(404L)).willReturn(Optional.empty());

    assertThrows(
        NoSuchObjectException.class,
        () -> service().getOccupation(404L)
    );
  }

  @Test
  @DisplayName("createOccupation throws not implemented")
  void createOccupationThrowsNotImplemented() {
    CreateOccupationRequest request = mock(CreateOccupationRequest.class);

    assertThrows(
        NotImplementedException.class,
        () -> service().createOccupation(request)
    );
  }

  @Test
  @DisplayName("updateOccupation updates name and status")
  void updateOccupationUpdatesNameAndStatus() {
    Occupation occupation = occupation(1L, "Old name", Status.active);
    UpdateOccupationRequest request = mock(UpdateOccupationRequest.class);

    given(request.getName()).willReturn("New name");
    given(request.getStatus()).willReturn(Status.inactive);
    given(occupationRepository.findById(1L)).willReturn(Optional.of(occupation));
    given(occupationRepository.findByNameIgnoreCase("New name")).willReturn(null);
    given(occupationRepository.save(occupation)).willReturn(occupation);

    Occupation result = service().updateOccupation(1L, request);

    assertSame(occupation, result);
    assertEquals("New name", occupation.getName());
    assertEquals(Status.inactive, occupation.getStatus());
    verify(occupationRepository).save(occupation);
  }

  @Test
  @DisplayName("updateOccupation allows same occupation name for same id")
  void updateOccupationAllowsDuplicateWhenExistingIsSameId() {
    Occupation occupation = occupation(1L, "Developer", Status.active);
    Occupation existing = occupation(1L, "Developer", Status.active);
    UpdateOccupationRequest request = mock(UpdateOccupationRequest.class);

    given(request.getName()).willReturn("Developer");
    given(request.getStatus()).willReturn(Status.active);
    given(occupationRepository.findById(1L)).willReturn(Optional.of(occupation));
    given(occupationRepository.findByNameIgnoreCase("Developer")).willReturn(existing);
    given(occupationRepository.save(occupation)).willReturn(occupation);

    Occupation result = service().updateOccupation(1L, request);

    assertSame(occupation, result);
    verify(occupationRepository).save(occupation);
  }

  @Test
  @DisplayName("updateOccupation throws when occupation missing")
  void updateOccupationThrowsWhenMissing() {
    UpdateOccupationRequest request = mock(UpdateOccupationRequest.class);

    given(occupationRepository.findById(404L)).willReturn(Optional.empty());

    assertThrows(
        NoSuchObjectException.class,
        () -> service().updateOccupation(404L, request)
    );

    verify(occupationRepository, never()).save(any());
  }

  @Test
  @DisplayName("updateOccupation throws when requested name belongs to another occupation")
  void updateOccupationThrowsWhenDuplicateNameBelongsToAnotherOccupation() {
    Occupation occupation = occupation(1L, "Old", Status.active);
    Occupation duplicate = occupation(2L, "Duplicate", Status.active);
    UpdateOccupationRequest request = mock(UpdateOccupationRequest.class);

    given(request.getName()).willReturn("Duplicate");
    given(occupationRepository.findById(1L)).willReturn(Optional.of(occupation));
    given(occupationRepository.findByNameIgnoreCase("Duplicate")).willReturn(duplicate);

    assertThrows(
        EntityExistsException.class,
        () -> service().updateOccupation(1L, request)
    );

    verify(occupationRepository, never()).save(any());
  }

  @Test
  @DisplayName("checkDuplicates throws when id is null and duplicate exists")
  void checkDuplicatesThrowsWhenIdNullAndDuplicateExists() {
    Occupation duplicate = occupation(2L, "Duplicate", Status.active);

    given(occupationRepository.findByNameIgnoreCase("Duplicate")).willReturn(duplicate);

    assertThrows(
        EntityExistsException.class,
        () -> invokeCheckDuplicates(service(), null, "Duplicate")
    );
  }

  @Test
  @DisplayName("checkDuplicates does not throw when duplicate does not exist")
  void checkDuplicatesDoesNotThrowWhenDuplicateDoesNotExist() {
    given(occupationRepository.findByNameIgnoreCase("Unique")).willReturn(null);

    assertDoesNotThrow(() -> invokeCheckDuplicates(service(), null, "Unique"));
  }

  @Test
  @DisplayName("deleteOccupation throws when occupation is referenced")
  void deleteOccupationThrowsWhenReferenced() {
    Occupation occupation = occupation(1L, "Developer", Status.active);
    CandidateOccupation candidateOccupation = new CandidateOccupation();

    given(occupationRepository.findById(1L)).willReturn(Optional.of(occupation));
    given(candidateOccupationRepository.findByOccupationId(1L))
        .willReturn(List.of(candidateOccupation));

    assertThrows(
        EntityReferencedException.class,
        () -> service().deleteOccupation(1L)
    );

    verify(occupationRepository, never()).save(any());
  }

  @Test
  @DisplayName("deleteOccupation marks existing unreferenced occupation as deleted")
  void deleteOccupationMarksExistingUnreferencedOccupationDeleted() {
    Occupation occupation = occupation(1L, "Developer", Status.active);

    given(occupationRepository.findById(1L)).willReturn(Optional.of(occupation));
    given(candidateOccupationRepository.findByOccupationId(1L)).willReturn(List.of());
    given(occupationRepository.save(occupation)).willReturn(occupation);

    boolean result = service().deleteOccupation(1L);

    assertEquals(true, result);
    assertEquals(Status.deleted, occupation.getStatus());
    verify(occupationRepository).save(occupation);
  }

  @Test
  @DisplayName("deleteOccupation returns false when occupation does not exist")
  void deleteOccupationReturnsFalseWhenMissing() {
    given(occupationRepository.findById(404L)).willReturn(Optional.empty());
    given(candidateOccupationRepository.findByOccupationId(404L)).willReturn(List.of());

    boolean result = service().deleteOccupation(404L);

    assertEquals(false, result);
    verify(occupationRepository, never()).save(any());
  }

  @Test
  @DisplayName("selectBuilder returns occupation select builder")
  void selectBuilderReturnsBuilder() {
    DtoBuilder builder = service().selectBuilder();

    assertNotNull(builder);
  }

  private static Occupation occupation(Long id, String name, Status status) {
    Occupation occupation = new Occupation();
    occupation.setId(id);
    occupation.setName(name);
    occupation.setStatus(status);
    return occupation;
  }

  private static void invokeCheckDuplicates(
      OccupationServiceImpl service,
      Long id,
      String name
  ) {
    try {
      Method method = OccupationServiceImpl.class
          .getDeclaredMethod("checkDuplicates", Long.class, String.class);
      method.setAccessible(true);
      method.invoke(service, id, name);
    } catch (InvocationTargetException e) {
      throwUnchecked(e.getCause());
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  private static void throwUnchecked(Throwable throwable) {
    if (throwable instanceof RuntimeException runtimeException) {
      throw runtimeException;
    }
    if (throwable instanceof Error error) {
      throw error;
    }
    throw new RuntimeException(throwable);
  }
}