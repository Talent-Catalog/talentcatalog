package org.tctalent.server.service.db.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
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
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.InvalidCredentialsException;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateOccupation;
import org.tctalent.server.model.db.Occupation;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidateOccupationRepository;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.OccupationRepository;
import org.tctalent.server.request.candidate.occupation.CreateCandidateOccupationRequest;
import org.tctalent.server.request.candidate.occupation.UpdateCandidateOccupationRequest;
import org.tctalent.server.request.candidate.occupation.UpdateCandidateOccupationsRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.email.EmailHelper;

@ExtendWith(MockitoExtension.class)
class CandidateOccupationServiceImplTest {

  @Mock
  private CandidateOccupationRepository candidateOccupationRepository;

  @Mock
  private OccupationRepository occupationRepository;

  @Mock
  private CandidateRepository candidateRepository;

  @Mock
  private CandidateService candidateService;

  @Mock
  private AuthService authService;

  @Mock
  private EmailHelper emailHelper;

  @InjectMocks
  private CandidateOccupationServiceImpl service;

  @Test
  void createCandidateOccupationThrowsWhenUserNotLoggedIn() {
    CreateCandidateOccupationRequest request = mock(CreateCandidateOccupationRequest.class);

    when(authService.getLoggedInUser()).thenReturn(Optional.empty());

    assertThrows(InvalidSessionException.class,
        () -> service.createCandidateOccupation(request));

    verifyNoInteractions(occupationRepository);
    verifyNoInteractions(candidateOccupationRepository);
  }

  @Test
  void createCandidateOccupationThrowsWhenOccupationMissing() {
    User user = mock(User.class);
    CreateCandidateOccupationRequest request = mock(CreateCandidateOccupationRequest.class);

    when(authService.getLoggedInUser()).thenReturn(Optional.of(user));
    when(request.getOccupationId()).thenReturn(10L);
    when(occupationRepository.findById(10L)).thenReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class,
        () -> service.createCandidateOccupation(request));

    verify(candidateOccupationRepository, never()).save(any());
  }

  @Test
  void createCandidateOccupationAsAdminThrowsWhenCandidateMissing() {
    User user = mock(User.class);
    CreateCandidateOccupationRequest request = mock(CreateCandidateOccupationRequest.class);
    Occupation occupation = occupation(10L, "Engineer");

    when(authService.getLoggedInUser()).thenReturn(Optional.of(user));
    when(authService.hasAdminPrivileges(user.getRole())).thenReturn(true);
    when(request.getOccupationId()).thenReturn(10L);
    when(request.getCandidateId()).thenReturn(20L);
    when(occupationRepository.findById(10L)).thenReturn(Optional.of(occupation));
    when(candidateRepository.findById(20L)).thenReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class,
        () -> service.createCandidateOccupation(request));

    verify(candidateOccupationRepository, never()).save(any());
  }

  @Test
  void createCandidateOccupationAsAdminCreatesAndSaves() {
    User user = mock(User.class);
    CreateCandidateOccupationRequest request = mock(CreateCandidateOccupationRequest.class);
    Candidate candidate = candidate(20L);
    Occupation occupation = occupation(10L, "Engineer");

    when(authService.getLoggedInUser()).thenReturn(Optional.of(user));
    when(authService.hasAdminPrivileges(user.getRole())).thenReturn(true);
    when(request.getOccupationId()).thenReturn(10L);
    when(request.getCandidateId()).thenReturn(20L);
    when(request.getYearsExperience()).thenReturn(7L);
    when(occupationRepository.findById(10L)).thenReturn(Optional.of(occupation));
    when(candidateRepository.findById(20L)).thenReturn(Optional.of(candidate));
    when(candidateOccupationRepository.save(any(CandidateOccupation.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    CandidateOccupation result = service.createCandidateOccupation(request);

    assertSame(candidate, result.getCandidate());
    assertSame(occupation, result.getOccupation());
    assertEquals(7L, result.getYearsExperience());

    verify(candidateOccupationRepository).save(result);
    verify(candidateService).save(candidate);
  }

  @Test
  void createCandidateOccupationAsNonAdminThrowsWhenCandidateSessionMissing() {
    User user = mock(User.class);
    CreateCandidateOccupationRequest request = mock(CreateCandidateOccupationRequest.class);
    Occupation occupation = occupation(10L, "Engineer");

    when(authService.getLoggedInUser()).thenReturn(Optional.of(user));
    when(authService.hasAdminPrivileges(user.getRole())).thenReturn(false);
    when(request.getOccupationId()).thenReturn(10L);
    when(occupationRepository.findById(10L)).thenReturn(Optional.of(occupation));
    when(authService.getLoggedInCandidate()).thenReturn(null);

    assertThrows(InvalidSessionException.class,
        () -> service.createCandidateOccupation(request));

    verify(candidateOccupationRepository, never()).save(any());
  }

  @Test
  void createCandidateOccupationAsNonAdminCreatesForLoggedInCandidate() {
    User user = mock(User.class);
    CreateCandidateOccupationRequest request = mock(CreateCandidateOccupationRequest.class);
    Candidate candidate = candidate(20L);
    Occupation occupation = occupation(10L, "Engineer");

    when(authService.getLoggedInUser()).thenReturn(Optional.of(user));
    when(authService.hasAdminPrivileges(user.getRole())).thenReturn(false);
    when(authService.getLoggedInCandidate()).thenReturn(candidate);
    when(request.getOccupationId()).thenReturn(10L);
    when(request.getYearsExperience()).thenReturn(4L);
    when(occupationRepository.findById(10L)).thenReturn(Optional.of(occupation));
    when(candidateOccupationRepository.save(any(CandidateOccupation.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    CandidateOccupation result = service.createCandidateOccupation(request);

    assertSame(candidate, result.getCandidate());
    assertSame(occupation, result.getOccupation());
    assertEquals(4L, result.getYearsExperience());

    verify(candidateService).save(candidate);
  }

  @Test
  void createCandidateOccupationThrowsWhenCandidateAlreadyHasOccupation() {
    User user = mock(User.class);
    CreateCandidateOccupationRequest request = mock(CreateCandidateOccupationRequest.class);
    Candidate candidate = candidate(20L);
    Occupation occupation = occupation(10L, "Engineer");
    CandidateOccupation existing = candidateOccupation(99L, candidate, occupation, 5);

    when(authService.getLoggedInUser()).thenReturn(Optional.of(user));
    when(authService.hasAdminPrivileges(user.getRole())).thenReturn(false);
    when(authService.getLoggedInCandidate()).thenReturn(candidate);
    when(request.getOccupationId()).thenReturn(10L);
    when(occupationRepository.findById(10L)).thenReturn(Optional.of(occupation));
    when(candidateOccupationRepository.findByCandidateIdAAndOccupationId(20L, 10L))
        .thenReturn(existing);

    assertThrows(EntityExistsException.class,
        () -> service.createCandidateOccupation(request));

    verify(candidateOccupationRepository, never()).save(any());
    verify(candidateService, never()).save(any());
  }

  @Test
  void deleteCandidateOccupationThrowsWhenUserNotLoggedIn() {
    when(authService.getLoggedInUser()).thenReturn(Optional.empty());

    assertThrows(InvalidSessionException.class,
        () -> service.deleteCandidateOccupation(1L));

    verifyNoInteractions(candidateOccupationRepository);
  }

  @Test
  void deleteCandidateOccupationThrowsWhenCandidateOccupationMissing() {
    User user = mock(User.class);

    when(authService.getLoggedInUser()).thenReturn(Optional.of(user));
    when(candidateOccupationRepository.findByIdLoadCandidate(1L)).thenReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class,
        () -> service.deleteCandidateOccupation(1L));
  }

  @Test
  void deleteCandidateOccupationAsAdminThrowsWhenCandidateMissing() {
    User user = mock(User.class);
    Candidate candidate = candidate(20L);
    CandidateOccupation candidateOccupation = candidateOccupation(1L, candidate,
        occupation(10L, "Engineer"), 5);

    when(authService.getLoggedInUser()).thenReturn(Optional.of(user));
    when(authService.hasAdminPrivileges(user.getRole())).thenReturn(true);
    when(candidateOccupationRepository.findByIdLoadCandidate(1L))
        .thenReturn(Optional.of(candidateOccupation));
    when(candidateRepository.findById(20L)).thenReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class,
        () -> service.deleteCandidateOccupation(1L));

    verify(candidateOccupationRepository, never()).delete(any());
  }

  @Test
  void deleteCandidateOccupationAsAdminDeletesAndSavesCandidate() {
    User user = mock(User.class);
    Candidate candidate = candidate(20L);
    CandidateOccupation candidateOccupation = candidateOccupation(1L, candidate,
        occupation(10L, "Engineer"), 5);

    when(authService.getLoggedInUser()).thenReturn(Optional.of(user));
    when(authService.hasAdminPrivileges(user.getRole())).thenReturn(true);
    when(candidateOccupationRepository.findByIdLoadCandidate(1L))
        .thenReturn(Optional.of(candidateOccupation));
    when(candidateRepository.findById(20L)).thenReturn(Optional.of(candidate));

    service.deleteCandidateOccupation(1L);

    verify(candidateOccupationRepository).delete(candidateOccupation);
    verify(candidateService).save(candidate);
  }

  @Test
  void deleteCandidateOccupationAsNonAdminThrowsWhenCandidateSessionMissing() {
    User user = mock(User.class);
    Candidate candidate = candidate(20L);
    CandidateOccupation candidateOccupation = candidateOccupation(1L, candidate,
        occupation(10L, "Engineer"), 5);

    when(authService.getLoggedInUser()).thenReturn(Optional.of(user));
    when(authService.hasAdminPrivileges(user.getRole())).thenReturn(false);
    when(candidateOccupationRepository.findByIdLoadCandidate(1L))
        .thenReturn(Optional.of(candidateOccupation));
    when(authService.getLoggedInCandidate()).thenReturn(null);

    assertThrows(InvalidSessionException.class,
        () -> service.deleteCandidateOccupation(1L));

    verify(candidateOccupationRepository, never()).delete(any());
  }

  @Test
  void deleteCandidateOccupationAsNonAdminThrowsWhenDeletingAnotherCandidatesOccupation() {
    User user = mock(User.class);
    Candidate owner = candidate(20L);
    Candidate loggedInCandidate = candidate(99L);
    CandidateOccupation candidateOccupation = candidateOccupation(1L, owner,
        occupation(10L, "Engineer"), 5);

    when(authService.getLoggedInUser()).thenReturn(Optional.of(user));
    when(authService.hasAdminPrivileges(user.getRole())).thenReturn(false);
    when(candidateOccupationRepository.findByIdLoadCandidate(1L))
        .thenReturn(Optional.of(candidateOccupation));
    when(authService.getLoggedInCandidate()).thenReturn(loggedInCandidate);

    assertThrows(InvalidCredentialsException.class,
        () -> service.deleteCandidateOccupation(1L));

    verify(candidateOccupationRepository, never()).delete(any());
  }

  @Test
  void deleteCandidateOccupationAsNonAdminDeletesOwnOccupation() {
    User user = mock(User.class);
    Candidate candidate = candidate(20L);
    CandidateOccupation candidateOccupation = candidateOccupation(1L, candidate,
        occupation(10L, "Engineer"), 5);

    when(authService.getLoggedInUser()).thenReturn(Optional.of(user));
    when(authService.hasAdminPrivileges(user.getRole())).thenReturn(false);
    when(candidateOccupationRepository.findByIdLoadCandidate(1L))
        .thenReturn(Optional.of(candidateOccupation));
    when(authService.getLoggedInCandidate()).thenReturn(candidate);

    service.deleteCandidateOccupation(1L);

    verify(candidateOccupationRepository).delete(candidateOccupation);
    verify(candidateService).save(candidate);
  }

  @Test
  void listMyOccupationsReturnsRepositoryResults() {
    CandidateOccupation candidateOccupation = candidateOccupation(1L, candidate(20L),
        occupation(10L, "Engineer"), 5);

    when(authService.getLoggedInCandidateId()).thenReturn(20L);
    when(candidateOccupationRepository.findByCandidateIdLoadOccupation(20L))
        .thenReturn(List.of(candidateOccupation));

    assertEquals(List.of(candidateOccupation), service.listMyOccupations());
  }

  @Test
  void listCandidateOccupationsReturnsRepositoryResults() {
    CandidateOccupation candidateOccupation = candidateOccupation(1L, candidate(20L),
        occupation(10L, "Engineer"), 5);

    when(candidateOccupationRepository.findByCandidateId(20L))
        .thenReturn(List.of(candidateOccupation));

    assertEquals(List.of(candidateOccupation), service.listCandidateOccupations(20L));
  }

  @Test
  void listOccupationsReturnsRepositoryResults() {
    Occupation occupation = occupation(10L, "Engineer");

    when(candidateOccupationRepository.findAllOccupations()).thenReturn(List.of(occupation));

    assertEquals(List.of(occupation), service.listOccupations());
  }

  @Test
  void updateCandidateOccupationsThrowsWhenCandidateNotLoggedIn() {
    UpdateCandidateOccupationsRequest request = mock(UpdateCandidateOccupationsRequest.class);

    when(candidateService.getLoggedInCandidate()).thenReturn(Optional.empty());

    assertThrows(InvalidSessionException.class,
        () -> service.updateCandidateOccupations(request));

    verifyNoInteractions(candidateOccupationRepository);
  }

  @Test
  void updateCandidateOccupationsUpdatesExistingChangedOccupationAndDeletesRemovedOnes() {
    User user = mock(User.class);
    Candidate candidate = candidate(20L);
    Occupation oldOccupation = occupation(10L, "Old occupation");
    Occupation newOccupation = occupation(11L, "New occupation");
    Occupation removedOccupation = occupation(12L, "Removed occupation");

    CandidateOccupation existingToUpdate = candidateOccupation(1L, candidate,
        oldOccupation, 3);
    CandidateOccupation existingToRemove = candidateOccupation(2L, candidate,
        removedOccupation, 8);

    UpdateCandidateOccupationRequest update = updateRequest(1L, 11L, 9L);
    UpdateCandidateOccupationsRequest request = bulkRequest(List.of(update));

    when(candidateService.getLoggedInCandidate()).thenReturn(Optional.of(candidate));
    when(authService.getLoggedInUser()).thenReturn(Optional.of(user));
    when(candidateOccupationRepository.findByCandidateId(20L))
        .thenReturn(List.of(existingToUpdate, existingToRemove));
    when(occupationRepository.findById(11L)).thenReturn(Optional.of(newOccupation));
    when(candidateOccupationRepository.save(existingToUpdate)).thenReturn(existingToUpdate);

    List<CandidateOccupation> result = service.updateCandidateOccupations(request);

    assertEquals(List.of(existingToUpdate, existingToRemove), result);
    assertSame(newOccupation, existingToUpdate.getOccupation());
    assertEquals(9L, existingToUpdate.getYearsExperience());

    verify(candidateOccupationRepository).deleteById(2L);
    verify(candidateService).save(candidate);
  }

  @Test
  void updateCandidateOccupationsUpdatesExistingYearsOnlyWhenOccupationSame() {
    User user = mock(User.class);
    Candidate candidate = candidate(20L);
    Occupation occupation = occupation(10L, "Engineer");
    CandidateOccupation existing = candidateOccupation(1L, candidate, occupation, 3);

    UpdateCandidateOccupationRequest update = updateRequest(1L, 10L, 12L);
    UpdateCandidateOccupationsRequest request = bulkRequest(List.of(update));

    when(candidateService.getLoggedInCandidate()).thenReturn(Optional.of(candidate));
    when(authService.getLoggedInUser()).thenReturn(Optional.of(user));
    when(candidateOccupationRepository.findByCandidateId(20L)).thenReturn(List.of(existing));
    when(candidateOccupationRepository.save(existing)).thenReturn(existing);

    service.updateCandidateOccupations(request);

    assertSame(occupation, existing.getOccupation());
    assertEquals(12L, existing.getYearsExperience());

    verify(occupationRepository, never()).findById(anyLong());
    verify(candidateOccupationRepository, never()).deleteById(anyLong());
    verify(candidateService).save(candidate);
  }

  @Test
  void updateCandidateOccupationsSendsAlertWhenExistingUpdateHasNullOccupationId() {
    User user = mock(User.class);
    Candidate candidate = candidate(20L);
    Occupation occupation = occupation(10L, "Engineer");
    CandidateOccupation existing = candidateOccupation(1L, candidate, occupation, 3);

    UpdateCandidateOccupationRequest update = updateRequest(1L, null, 12L);
    UpdateCandidateOccupationsRequest request = bulkRequest(List.of(update));

    when(candidateService.getLoggedInCandidate()).thenReturn(Optional.of(candidate));
    when(authService.getLoggedInUser()).thenReturn(Optional.of(user));
    when(candidateOccupationRepository.findByCandidateId(20L)).thenReturn(List.of(existing));
    when(candidateOccupationRepository.save(existing)).thenReturn(existing);

    service.updateCandidateOccupations(request);

    verify(emailHelper)
        .sendAlert("Avoided Null Pointer exception, check logs for warning. Search NULL-AVOID to find.");
    verify(occupationRepository, never()).findById(anyLong());
    verify(candidateService).save(candidate);
  }

  @Test
  void updateCandidateOccupationsSendsAlertWhenExistingCandidateOccupationHasNullOccupation() {
    User user = mock(User.class);
    Candidate candidate = candidate(20L);
    Occupation fallbackOccupation = occupation(10L, "Engineer");

    CandidateOccupation existing = mock(CandidateOccupation.class);
    when(existing.getId()).thenReturn(1L);
    when(existing.getOccupation()).thenReturn(null, fallbackOccupation);

    UpdateCandidateOccupationRequest update = updateRequest(1L, 10L, 12L);
    UpdateCandidateOccupationsRequest request = bulkRequest(List.of(update));

    when(candidateService.getLoggedInCandidate()).thenReturn(Optional.of(candidate));
    when(authService.getLoggedInUser()).thenReturn(Optional.of(user));
    when(candidateOccupationRepository.findByCandidateId(20L)).thenReturn(List.of(existing));
    when(candidateOccupationRepository.save(existing)).thenReturn(existing);

    service.updateCandidateOccupations(request);

    verify(emailHelper)
        .sendAlert("Avoided Null Pointer exception, check logs for warning. Search NULL-AVOID to find.");
    verify(existing, never()).setYearsExperience(12L);
    verify(candidateService).save(candidate);
  }
  @Test
  void updateCandidateOccupationsUsesExistingCandidateOccupationWhenSameOccupationAlreadyExists() {
    User user = mock(User.class);
    Candidate candidate = candidate(20L);
    Occupation occupation = occupation(10L, "Engineer");
    CandidateOccupation duplicate = candidateOccupation(5L, candidate, occupation, 2);

    UpdateCandidateOccupationRequest update = updateRequest(null, 10L, 15L);
    UpdateCandidateOccupationsRequest request = bulkRequest(List.of(update));

    when(candidateService.getLoggedInCandidate()).thenReturn(Optional.of(candidate));
    when(authService.getLoggedInUser()).thenReturn(Optional.of(user));
    when(candidateOccupationRepository.findByCandidateId(20L)).thenReturn(List.of());
    when(candidateOccupationRepository.findByCandidateIdAAndOccupationId(20L, 10L))
        .thenReturn(duplicate);
    when(occupationRepository.findById(10L)).thenReturn(Optional.of(occupation));
    when(candidateOccupationRepository.save(duplicate)).thenReturn(duplicate);

    service.updateCandidateOccupations(request);

    assertEquals(15L, duplicate.getYearsExperience());

    verify(candidateOccupationRepository).save(duplicate);
    verify(candidateService).save(candidate);
  }

  @Test
  void updateCandidateOccupationsCreatesNewCandidateOccupationWhenNoExistingDuplicate() {
    User user = mock(User.class);
    Candidate candidate = candidate(20L);
    Occupation occupation = occupation(10L, "Engineer");

    UpdateCandidateOccupationRequest update = updateRequest(null, 10L, 6L);
    UpdateCandidateOccupationsRequest request = bulkRequest(List.of(update));

    when(candidateService.getLoggedInCandidate()).thenReturn(Optional.of(candidate));
    when(authService.getLoggedInUser()).thenReturn(Optional.of(user));
    when(candidateOccupationRepository.findByCandidateId(20L)).thenReturn(List.of());
    when(candidateOccupationRepository.findByCandidateIdAAndOccupationId(20L, 10L))
        .thenReturn(null);
    when(occupationRepository.findById(10L)).thenReturn(Optional.of(occupation));
    when(candidateOccupationRepository.save(any(CandidateOccupation.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    service.updateCandidateOccupations(request);

    ArgumentCaptor<CandidateOccupation> captor =
        ArgumentCaptor.forClass(CandidateOccupation.class);
    verify(candidateOccupationRepository).save(captor.capture());

    CandidateOccupation saved = captor.getValue();
    assertSame(candidate, saved.getCandidate());
    assertSame(occupation, saved.getOccupation());
    assertEquals(6L, saved.getYearsExperience());

    verify(candidateService).save(candidate);
  }

  @Test
  void updateCandidateOccupationsThrowsWhenNewOccupationIsMissing() {
    User user = mock(User.class);
    Candidate candidate = candidate(20L);

    UpdateCandidateOccupationRequest update = updateRequest(null, 10L, 6L);
    UpdateCandidateOccupationsRequest request = bulkRequest(List.of(update));

    when(candidateService.getLoggedInCandidate()).thenReturn(Optional.of(candidate));
    when(authService.getLoggedInUser()).thenReturn(Optional.of(user));
    when(candidateOccupationRepository.findByCandidateId(20L)).thenReturn(List.of());
    when(candidateOccupationRepository.findByCandidateIdAAndOccupationId(20L, 10L))
        .thenReturn(null);
    when(occupationRepository.findById(10L)).thenReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class,
        () -> service.updateCandidateOccupations(request));

    verify(candidateOccupationRepository, never()).save(any());
    verify(candidateService, never()).save(any());
  }

  @Test
  void updateCandidateOccupationThrowsWhenCandidateOccupationMissing() {
    UpdateCandidateOccupationRequest request = updateRequest(1L, 10L, 5L);

    when(candidateOccupationRepository.findByIdLoadCandidate(1L)).thenReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class,
        () -> service.updateCandidateOccupation(request));
  }

  @Test
  void updateCandidateOccupationThrowsWhenOccupationMissing() {
    Candidate candidate = candidate(20L);
    CandidateOccupation candidateOccupation = candidateOccupation(1L, candidate,
        occupation(9L, "Old"), 2);
    UpdateCandidateOccupationRequest request = updateRequest(1L, 10L, 5L);

    when(candidateOccupationRepository.findByIdLoadCandidate(1L))
        .thenReturn(Optional.of(candidateOccupation));
    when(occupationRepository.findById(10L)).thenReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class,
        () -> service.updateCandidateOccupation(request));

    verify(candidateOccupationRepository, never()).save(any());
  }

  @Test
  void updateCandidateOccupationThrowsWhenAnotherCandidateOccupationAlreadyUsesOccupation() {
    Candidate candidate = candidate(20L);
    Occupation requestedOccupation = occupation(10L, "Engineer");
    CandidateOccupation candidateOccupation = candidateOccupation(1L, candidate,
        occupation(9L, "Old"), 2);
    CandidateOccupation duplicate = candidateOccupation(2L, candidate,
        requestedOccupation, 8);
    UpdateCandidateOccupationRequest request = updateRequest(1L, 10L, 5L);

    when(candidateOccupationRepository.findByIdLoadCandidate(1L))
        .thenReturn(Optional.of(candidateOccupation));
    when(occupationRepository.findById(10L)).thenReturn(Optional.of(requestedOccupation));
    when(candidateOccupationRepository.findByCandidateIdAAndOccupationId(20L, 10L))
        .thenReturn(duplicate);

    assertThrows(EntityExistsException.class,
        () -> service.updateCandidateOccupation(request));

    verify(candidateOccupationRepository, never()).save(any());
    verify(candidateService, never()).save(any());
  }

  @Test
  void updateCandidateOccupationAllowsSameCandidateOccupationToKeepOccupation() {
    Candidate candidate = candidate(20L);
    Occupation occupation = occupation(10L, "Engineer");
    CandidateOccupation candidateOccupation = candidateOccupation(1L, candidate,
        occupation, 2);
    UpdateCandidateOccupationRequest request = updateRequest(1L, 10L, 9L);

    when(candidateOccupationRepository.findByIdLoadCandidate(1L))
        .thenReturn(Optional.of(candidateOccupation));
    when(occupationRepository.findById(10L)).thenReturn(Optional.of(occupation));
    when(candidateOccupationRepository.findByCandidateIdAAndOccupationId(20L, 10L))
        .thenReturn(candidateOccupation);
    when(candidateOccupationRepository.save(candidateOccupation)).thenReturn(candidateOccupation);

    CandidateOccupation result = service.updateCandidateOccupation(request);

    assertSame(candidateOccupation, result);
    assertSame(occupation, candidateOccupation.getOccupation());
    assertEquals(9L, candidateOccupation.getYearsExperience());

    verify(candidateService).save(candidate);
    verify(candidateOccupationRepository).save(candidateOccupation);
  }

  @Test
  void updateCandidateOccupationUpdatesOccupationAndYearsExperience() {
    Candidate candidate = candidate(20L);
    Occupation newOccupation = occupation(10L, "Engineer");
    CandidateOccupation candidateOccupation = candidateOccupation(1L, candidate,
        occupation(9L, "Old"), 2);
    UpdateCandidateOccupationRequest request = updateRequest(1L, 10L, 9L);

    when(candidateOccupationRepository.findByIdLoadCandidate(1L))
        .thenReturn(Optional.of(candidateOccupation));
    when(occupationRepository.findById(10L)).thenReturn(Optional.of(newOccupation));
    when(candidateOccupationRepository.findByCandidateIdAAndOccupationId(20L, 10L))
        .thenReturn(null);
    when(candidateOccupationRepository.save(candidateOccupation)).thenReturn(candidateOccupation);

    CandidateOccupation result = service.updateCandidateOccupation(request);

    assertSame(candidateOccupation, result);
    assertSame(newOccupation, candidateOccupation.getOccupation());
    assertEquals(9, candidateOccupation.getYearsExperience());

    verify(candidateService).save(candidate);
    verify(candidateOccupationRepository).save(candidateOccupation);
  }

  private static Candidate candidate(Long id) {
    Candidate candidate = new Candidate();
    candidate.setId(id);
    return candidate;
  }

  private static Occupation occupation(Long id, String name) {
    Occupation occupation = new Occupation();
    occupation.setId(id);
    occupation.setName(name);
    return occupation;
  }

  private static CandidateOccupation candidateOccupation(
      Long id, Candidate candidate, Occupation occupation, Integer yearsExperience) {
    CandidateOccupation candidateOccupation = new CandidateOccupation();
    candidateOccupation.setId(id);
    candidateOccupation.setCandidate(candidate);
    candidateOccupation.setOccupation(occupation);
    candidateOccupation.setYearsExperience(Long.valueOf(yearsExperience));
    return candidateOccupation;
  }

  private static UpdateCandidateOccupationRequest updateRequest(
      Long id, Long occupationId, Long yearsExperience) {
    UpdateCandidateOccupationRequest request = new UpdateCandidateOccupationRequest();
    request.setId(id);
    request.setOccupationId(occupationId);
    request.setYearsExperience(yearsExperience);
    return request;
  }

  private static UpdateCandidateOccupationsRequest bulkRequest(
      List<UpdateCandidateOccupationRequest> updates) {
    UpdateCandidateOccupationsRequest request = new UpdateCandidateOccupationsRequest();
    request.setUpdates(updates);
    return request;
  }
}