/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.db.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.exception.UsernameTakenException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateDestination;
import org.tctalent.server.model.db.CandidateExam;
import org.tctalent.server.model.db.CandidateLanguage;
import org.tctalent.server.model.db.CandidateOccupation;
import org.tctalent.server.model.db.CandidateStatus;
import org.tctalent.server.model.db.CandidateSubfolderType;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.Language;
import org.tctalent.server.model.db.LanguageLevel;
import org.tctalent.server.model.db.Occupation;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.TaskAssignmentImpl;
import org.tctalent.server.model.db.UploadTaskImpl;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidateExamRepository;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.CountryRepository;
import org.tctalent.server.repository.db.TaskAssignmentRepository;
import org.tctalent.server.repository.db.UserRepository;
import org.tctalent.server.request.candidate.CandidateIntakeAuditRequest;
import org.tctalent.server.request.candidate.ResolveTaskAssignmentsRequest;
import org.tctalent.server.request.candidate.SubmitRegistrationRequest;
import org.tctalent.server.request.candidate.UpdateCandidateContactRequest;
import org.tctalent.server.request.candidate.UpdateCandidateNotificationPreferenceRequest;
import org.tctalent.server.request.candidate.UpdateCandidateStatusInfo;
import org.tctalent.server.request.note.CreateCandidateNoteRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateNoteService;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.service.db.UserService;
import org.tctalent.server.service.db.email.EmailHelper;

@ExtendWith(MockitoExtension.class)
public class CandidateServiceImplCharacterizationTest {

  private static final long AFGHANISTAN_COUNTRY_ID = 6180L;
  private static final long UKRAINE_COUNTRY_ID = 6406L;

  private PartnerImpl partner;
  private PartnerImpl otherPartner;
  private User staffUser;
  private User candidateUser;
  private Candidate candidate;
  private Country country;
  private Country nationality;

  @Mock private CandidateRepository candidateRepository;
  @Mock private CandidateExamRepository candidateExamRepository;
  @Mock private CandidateNoteService candidateNoteService;
  @Mock private CountryRepository countryRepository;
  @Mock private CountryService countryService;
  @Mock private EmailHelper emailHelper;
  @Mock private AuthService authService;
  @Mock private TaskAssignmentRepository taskAssignmentRepository;
  @Mock private UserRepository userRepository;
  @Mock private UserService userService;

  @Spy
  @InjectMocks
  private CandidateServiceImpl candidateService;

  @BeforeEach
  void setUp() {
    partner = new PartnerImpl();
    partner.setId(1L);
    partner.setName("Partner One");

    otherPartner = new PartnerImpl();
    otherPartner.setId(2L);
    otherPartner.setName("Partner Two");

    staffUser = new User();
    staffUser.setId(100L);
    staffUser.setRole(Role.admin);
    staffUser.setPartner(partner);
    staffUser.setEmail("staff@example.org");

    candidateUser = new User();
    candidateUser.setId(200L);
    candidateUser.setRole(Role.user);
    candidateUser.setPartner(partner);
    candidateUser.setFirstName("Amina");
    candidateUser.setLastName("Ahmadi");
    candidateUser.setEmail("amina@example.org");
    candidateUser.setStatus(Status.active);

    country = country(10L, "Jordan");
    nationality = country(20L, "Syria");

    candidate = new Candidate();
    candidate.setId(300L);
    candidate.setUser(candidateUser);
    candidate.setCandidateNumber("CAND-300");
    candidate.setStatus(CandidateStatus.active);
    candidate.setCountry(country);
    candidate.setNationality(nationality);
    candidate.setCandidateDestinations(new ArrayList<>());
    candidate.setCandidateExams(new ArrayList<>());
    candidate.setTaskAssignments(new ArrayList<>());
  }

  @Test
  @DisplayName("getCandidate returns candidate when repository finds it")
  void getCandidate_returnsCandidate_whenFound() {
    given(candidateRepository.findById(candidate.getId())).willReturn(Optional.of(candidate));

    Candidate result = candidateService.getCandidate(candidate.getId());

    assertSame(candidate, result);
  }

  @Test
  @DisplayName("getCandidate throws NoSuchObjectException when repository does not find it")
  void getCandidate_throwsNoSuchObjectException_whenMissing() {
    given(candidateRepository.findById(999L)).willReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class, () -> candidateService.getCandidate(999L));
  }

  @Test
  @DisplayName("deleteCandidate deletes and returns true when candidate exists")
  void deleteCandidate_deletesAndReturnsTrue_whenFound() {
    given(candidateRepository.findById(candidate.getId())).willReturn(Optional.of(candidate));

    boolean result = candidateService.deleteCandidate(candidate.getId());

    assertTrue(result);
    verify(candidateRepository).delete(candidate);
  }

  @Test
  @DisplayName("deleteCandidate returns false when candidate does not exist")
  void deleteCandidate_returnsFalse_whenMissing() {
    given(candidateRepository.findById(candidate.getId())).willReturn(Optional.empty());

    boolean result = candidateService.deleteCandidate(candidate.getId());

    assertFalse(result);
    verify(candidateRepository, never()).delete(any(Candidate.class));
  }

  @Test
  @DisplayName("addMissingDestinations adds and saves missing TC destination countries")
  void addMissingDestinations_addsAndSavesMissingDestination() {
    Country existingDestination = country(1L, "Canada");
    Country missingDestination = country(2L, "Australia");

    CandidateDestination existing = new CandidateDestination();
    existing.setCountry(existingDestination);
    existing.setCandidate(candidate);
    candidate.setCandidateDestinations(new ArrayList<>(List.of(existing)));

    given(countryService.getTCDestinations()).willReturn(List.of(existingDestination, missingDestination));
    doReturn(candidate).when(candidateService).save(candidate);

    Candidate result = candidateService.addMissingDestinations(candidate);

    assertSame(candidate, result);
    assertTrue(
        candidate.getCandidateDestinations().stream()
            .anyMatch(cd -> cd.getCountry() != null
                && "Australia".equals(cd.getCountry().getName())
                && cd.getCandidate() == candidate)
    );
    verify(candidateService).save(candidate);
  }

  @Test
  @DisplayName("addMissingDestinations does not save when all destinations already exist")
  void addMissingDestinations_doesNotSave_whenNoDestinationMissing() {
    Country existingDestination = country(1L, "Canada");

    CandidateDestination existing = new CandidateDestination();
    existing.setCountry(existingDestination);
    existing.setCandidate(candidate);
    candidate.setCandidateDestinations(new ArrayList<>(List.of(existing)));

    given(countryService.getTCDestinations()).willReturn(List.of(existingDestination));

    Candidate result = candidateService.addMissingDestinations(candidate);

    assertSame(candidate, result);
    assertEquals(1, candidate.getCandidateDestinations().size());
    verify(candidateService, never()).save(any(Candidate.class));
  }

  @Test
  @DisplayName("updateCandidateStatus saves candidate without side effects when status does not change")
  void updateCandidateStatus_savesWithoutSideEffects_whenStatusDoesNotChange() {
    candidate.setStatus(CandidateStatus.active);

    UpdateCandidateStatusInfo info = new UpdateCandidateStatusInfo();
    info.setStatus(CandidateStatus.active);
    info.setComment("No status change");

    doReturn(candidate).when(candidateService).save(candidate);

    Candidate result = candidateService.updateCandidateStatus(candidate, info);

    assertSame(candidate, result);
    assertEquals(CandidateStatus.active, candidate.getStatus());
    verify(candidateService).save(candidate);
    verify(candidateNoteService, never()).createCandidateNote(any(CreateCandidateNoteRequest.class));
    verify(emailHelper, never()).sendRegistrationEmail(any(Candidate.class));
    verify(emailHelper, never()).sendIncompleteApplication(any(User.class), any());
  }

  @Test
  @DisplayName("updateCandidateStatus sends registration email when draft candidate moves to non-deleted status")
  void updateCandidateStatus_sendsRegistrationEmail_whenDraftMovesToPending() {
    candidate.setStatus(CandidateStatus.draft);

    UpdateCandidateStatusInfo info = new UpdateCandidateStatusInfo();
    info.setStatus(CandidateStatus.pending);
    info.setComment("Submitted");

    given(authService.getLoggedInUser()).willReturn(Optional.of(staffUser));
    doReturn(candidate).when(candidateService).save(candidate);

    Candidate result = candidateService.updateCandidateStatus(candidate, info);

    assertSame(candidate, result);
    assertEquals(CandidateStatus.pending, candidate.getStatus());
    verify(candidateNoteService).createCandidateNote(any(CreateCandidateNoteRequest.class));
    verify(emailHelper).sendRegistrationEmail(candidate);
    verify(emailHelper, never()).sendIncompleteApplication(any(User.class), any());
  }

  @Test
  @DisplayName("updateCandidateStatus sends incomplete application email when new status is incomplete")
  void updateCandidateStatus_sendsIncompleteEmail_whenStatusBecomesIncomplete() {
    candidate.setStatus(CandidateStatus.active);

    UpdateCandidateStatusInfo info = new UpdateCandidateStatusInfo();
    info.setStatus(CandidateStatus.incomplete);
    info.setCandidateMessage("Please complete your profile.");
    info.setComment("Missing required information");

    given(authService.getLoggedInUser()).willReturn(Optional.of(staffUser));
    doReturn(candidate).when(candidateService).save(candidate);

    Candidate result = candidateService.updateCandidateStatus(candidate, info);

    assertSame(candidate, result);
    assertEquals(CandidateStatus.incomplete, candidate.getStatus());
    verify(candidateNoteService).createCandidateNote(any(CreateCandidateNoteRequest.class));
    verify(emailHelper).sendIncompleteApplication(candidateUser, "Please complete your profile.");
    verify(emailHelper, never()).sendRegistrationEmail(any(Candidate.class));
  }

  @Test
  @DisplayName("updateCandidateStatus syncs user status to deleted when candidate is deleted")
  void updateCandidateStatus_setsUserDeleted_whenCandidateDeleted() {
    candidate.setStatus(CandidateStatus.active);
    candidateUser.setStatus(Status.active);

    UpdateCandidateStatusInfo info = new UpdateCandidateStatusInfo();
    info.setStatus(CandidateStatus.deleted);
    info.setComment("Delete candidate");

    given(authService.getLoggedInUser()).willReturn(Optional.of(staffUser));
    doReturn(candidate).when(candidateService).save(candidate);

    Candidate result = candidateService.updateCandidateStatus(candidate, info);

    assertSame(candidate, result);
    assertEquals(CandidateStatus.deleted, candidate.getStatus());
    assertEquals(Status.deleted, candidateUser.getStatus());
    verify(userRepository).save(candidateUser);
    verify(candidateNoteService).createCandidateNote(any(CreateCandidateNoteRequest.class));
    verify(emailHelper, never()).sendRegistrationEmail(any(Candidate.class));
  }

  @Test
  @DisplayName("updateCandidateStatus reactivates deleted user when candidate is no longer deleted")
  void updateCandidateStatus_reactivatesUser_whenCandidateNoLongerDeleted() {
    candidate.setStatus(CandidateStatus.deleted);
    candidateUser.setStatus(Status.deleted);

    UpdateCandidateStatusInfo info = new UpdateCandidateStatusInfo();
    info.setStatus(CandidateStatus.active);
    info.setComment("Restore candidate");

    doReturn(candidate).when(candidateService).save(candidate);

    Candidate result = candidateService.updateCandidateStatus(candidate, info);

    assertSame(candidate, result);
    assertEquals(CandidateStatus.active, candidate.getStatus());
    assertEquals(Status.active, candidateUser.getStatus());
    verify(userRepository).save(candidateUser);
    verify(candidateNoteService).createCandidateNote(any(CreateCandidateNoteRequest.class));
  }

  @Test
  @DisplayName("submitRegistration marks candidate ineligible when country and nationality are same non-exception country")
  void submitRegistration_marksIneligible_whenCountryAndNationalitySameAndNotExceptionCountry() {
    Country sameCountryAndNationality = country(999L, "Same Country");
    candidate.setCountry(sameCountryAndNationality);
    candidate.setNationality(sameCountryAndNationality);
    candidate.setStatus(CandidateStatus.draft);

    SubmitRegistrationRequest request = submitRegistrationRequest("privacy-policy-1");
    stubLoggedInCandidate(candidate);
    doReturn(candidate).when(candidateService).save(candidate);
    doReturn(candidate)
        .when(candidateService)
        .updateCandidateStatus(eq(candidate), any(UpdateCandidateStatusInfo.class));

    Candidate result = candidateService.submitRegistration(request);

    ArgumentCaptor<UpdateCandidateStatusInfo> captor =
        ArgumentCaptor.forClass(UpdateCandidateStatusInfo.class);
    verify(candidateService).updateCandidateStatus(eq(candidate), captor.capture());

    assertSame(candidate, result);
    assertEquals(CandidateStatus.ineligible, captor.getValue().getStatus());
    assertEquals(
        "TC criteria not met: Country located is same as country of nationality.",
        captor.getValue().getComment());
    assertTrue(candidate.getContactConsentRegistration());
    assertEquals("privacy-policy-1", candidate.getAcceptedPrivacyPolicyId());
    assertNotNull(candidate.getAcceptedPrivacyPolicyDate());
    assertSame(partner, candidate.getAcceptedPrivacyPolicyPartner());
  }

  @Test
  @DisplayName("submitRegistration marks candidate pending when country and nationality are different")
  void submitRegistration_marksPending_whenCountryAndNationalityDifferent() {
    candidate.setCountry(country(1L, "Jordan"));
    candidate.setNationality(country(2L, "Syria"));
    candidate.setStatus(CandidateStatus.draft);

    SubmitRegistrationRequest request = submitRegistrationRequest("privacy-policy-1");
    stubLoggedInCandidate(candidate);
    doReturn(candidate).when(candidateService).save(candidate);
    doReturn(candidate)
        .when(candidateService)
        .updateCandidateStatus(eq(candidate), any(UpdateCandidateStatusInfo.class));

    Candidate result = candidateService.submitRegistration(request);

    ArgumentCaptor<UpdateCandidateStatusInfo> captor =
        ArgumentCaptor.forClass(UpdateCandidateStatusInfo.class);
    verify(candidateService).updateCandidateStatus(eq(candidate), captor.capture());

    assertSame(candidate, result);
    assertEquals(CandidateStatus.pending, captor.getValue().getStatus());
    assertEquals("Candidate submitted", captor.getValue().getComment());
    assertTrue(candidate.getContactConsentRegistration());
  }

  @Test
  @DisplayName("submitRegistration keeps current non-draft status when country and nationality are different")
  void submitRegistration_keepsCurrentStatus_whenCurrentStatusIsNotDraft() {
    candidate.setCountry(country(1L, "Jordan"));
    candidate.setNationality(country(2L, "Syria"));
    candidate.setStatus(CandidateStatus.active);

    SubmitRegistrationRequest request = submitRegistrationRequest("privacy-policy-1");
    stubLoggedInCandidate(candidate);
    doReturn(candidate).when(candidateService).save(candidate);
    doReturn(candidate)
        .when(candidateService)
        .updateCandidateStatus(eq(candidate), any(UpdateCandidateStatusInfo.class));

    Candidate result = candidateService.submitRegistration(request);

    ArgumentCaptor<UpdateCandidateStatusInfo> captor =
        ArgumentCaptor.forClass(UpdateCandidateStatusInfo.class);
    verify(candidateService).updateCandidateStatus(eq(candidate), captor.capture());

    assertSame(candidate, result);
    assertEquals(CandidateStatus.active, captor.getValue().getStatus());
    assertEquals("Candidate submitted", captor.getValue().getComment());
  }

  @Test
  @DisplayName("submitRegistration treats Afghanistan same-country candidate as pending")
  void submitRegistration_marksPending_whenAfghanCandidateIsInAfghanistan() {
    Country afghanistan = country(AFGHANISTAN_COUNTRY_ID, "Afghanistan");
    candidate.setCountry(afghanistan);
    candidate.setNationality(afghanistan);
    candidate.setStatus(CandidateStatus.draft);

    SubmitRegistrationRequest request = submitRegistrationRequest("privacy-policy-1");
    stubLoggedInCandidate(candidate);
    doReturn(candidate).when(candidateService).save(candidate);
    doReturn(candidate)
        .when(candidateService)
        .updateCandidateStatus(eq(candidate), any(UpdateCandidateStatusInfo.class));

    candidateService.submitRegistration(request);

    ArgumentCaptor<UpdateCandidateStatusInfo> captor =
        ArgumentCaptor.forClass(UpdateCandidateStatusInfo.class);
    verify(candidateService).updateCandidateStatus(eq(candidate), captor.capture());

    assertEquals(CandidateStatus.pending, captor.getValue().getStatus());
  }

  @Test
  @DisplayName("submitRegistration treats Ukraine same-country candidate as pending")
  void submitRegistration_marksPending_whenUkrainianCandidateIsInUkraine() {
    Country ukraine = country(UKRAINE_COUNTRY_ID, "Ukraine");
    candidate.setCountry(ukraine);
    candidate.setNationality(ukraine);
    candidate.setStatus(CandidateStatus.draft);

    SubmitRegistrationRequest request = submitRegistrationRequest("privacy-policy-1");
    stubLoggedInCandidate(candidate);
    doReturn(candidate).when(candidateService).save(candidate);
    doReturn(candidate)
        .when(candidateService)
        .updateCandidateStatus(eq(candidate), any(UpdateCandidateStatusInfo.class));

    candidateService.submitRegistration(request);

    ArgumentCaptor<UpdateCandidateStatusInfo> captor =
        ArgumentCaptor.forClass(UpdateCandidateStatusInfo.class);
    verify(candidateService).updateCandidateStatus(eq(candidate), captor.capture());

    assertEquals(CandidateStatus.pending, captor.getValue().getStatus());
  }

  @Test
  @DisplayName("submitRegistration does not update status when candidate is already pending")
  void submitRegistration_doesNotUpdateStatus_whenAlreadyPending() {
    candidate.setCountry(country(1L, "Jordan"));
    candidate.setNationality(country(2L, "Syria"));
    candidate.setStatus(CandidateStatus.pending);

    SubmitRegistrationRequest request = submitRegistrationRequest("privacy-policy-1");
    stubLoggedInCandidate(candidate);
    doReturn(candidate).when(candidateService).save(candidate);

    Candidate result = candidateService.submitRegistration(request);

    assertSame(candidate, result);
    verify(candidateService, never())
        .updateCandidateStatus(any(Candidate.class), any(UpdateCandidateStatusInfo.class));
    verify(candidateService).save(candidate);
  }

  @Test
  @DisplayName("submitRegistration throws when privacy policy id is null")
  void submitRegistration_throws_whenPrivacyPolicyIdIsNull() {
    SubmitRegistrationRequest request = submitRegistrationRequest(null);
    stubLoggedInCandidate(candidate);

    assertThrows(InvalidRequestException.class, () -> candidateService.submitRegistration(request));
  }

  @Test
  @DisplayName("submitRegistration throws when privacy policy id is literal null string")
  void submitRegistration_throws_whenPrivacyPolicyIdIsLiteralNullString() {
    SubmitRegistrationRequest request = submitRegistrationRequest("null");
    stubLoggedInCandidate(candidate);

    assertThrows(InvalidRequestException.class, () -> candidateService.submitRegistration(request));
  }

  @Test
  @DisplayName("updateAcceptedPrivacyPolicy stores accepted policy id, timestamp and partner")
  void updateAcceptedPrivacyPolicy_storesPolicyFields() {
    candidateUser.setCandidate(candidate);

    given(authService.getLoggedInUser()).willReturn(Optional.of(candidateUser));
    doReturn(candidate).when(candidateService).save(candidate);

    Candidate result = candidateService.updateAcceptedPrivacyPolicy("policy-2");

    assertSame(candidate, result);
    assertEquals("policy-2", candidate.getAcceptedPrivacyPolicyId());
    assertNotNull(candidate.getAcceptedPrivacyPolicyDate());
    assertSame(partner, candidate.getAcceptedPrivacyPolicyPartner());
    verify(candidateService).save(candidate);
    verify(candidateRepository, never()).findByUserId(anyLong());
  }

  @Test
  @DisplayName("completeIntake stores external full intake date at UTC noon")
  void completeIntake_setsExternalFullIntakeDateAtUtcNoon() {
    LocalDate completedDate = LocalDate.of(2025, 5, 10);

    CandidateIntakeAuditRequest request = new CandidateIntakeAuditRequest();
    request.setFullIntake(true);
    request.setCompletedDate(completedDate);

    doReturn(candidate).when(candidateService).getCandidate(candidate.getId());
    doReturn(candidate).when(candidateService).save(candidate);

    Candidate result = candidateService.completeIntake(candidate.getId(), request);

    assertSame(candidate, result);
    assertEquals(
        OffsetDateTime.of(completedDate, LocalTime.NOON, ZoneOffset.UTC),
        candidate.getFullIntakeCompletedDate());
    assertNull(candidate.getFullIntakeCompletedBy());
    verify(candidateService).save(candidate);
  }

  @Test
  @DisplayName("completeIntake stores current mini intake audit user and date")
  void completeIntake_setsCurrentMiniIntakeAuditUserAndDate() {
    CandidateIntakeAuditRequest request = new CandidateIntakeAuditRequest();
    request.setFullIntake(false);

    given(authService.getLoggedInUser()).willReturn(Optional.of(staffUser));
    doReturn(candidate).when(candidateService).getCandidate(candidate.getId());
    doReturn(candidate).when(candidateService).save(candidate);

    Candidate result = candidateService.completeIntake(candidate.getId(), request);

    assertSame(candidate, result);
    assertSame(staffUser, candidate.getMiniIntakeCompletedBy());
    assertNotNull(candidate.getMiniIntakeCompletedDate());
    verify(candidateService).save(candidate);
  }

  @Test
  @DisplayName("updateNotificationPreference saves and creates note when preference changes")
  void updateNotificationPreference_savesAndCreatesNote_whenPreferenceChanges() {
    candidate.setAllNotifications(false);

    UpdateCandidateNotificationPreferenceRequest request =
        new UpdateCandidateNotificationPreferenceRequest();
    request.setAllNotifications(true);

    given(candidateRepository.findById(candidate.getId())).willReturn(Optional.of(candidate));
    doReturn(candidate).when(candidateService).save(candidate);

    candidateService.updateNotificationPreference(candidate.getId(), request);

    assertTrue(candidate.isAllNotifications());
    verify(candidateService).save(candidate);
    verify(candidateNoteService).createCandidateNote(any(CreateCandidateNoteRequest.class));
  }

  @Test
  @DisplayName("updateNotificationPreference does nothing when preference is unchanged")
  void updateNotificationPreference_doesNothing_whenPreferenceUnchanged() {
    candidate.setAllNotifications(true);

    UpdateCandidateNotificationPreferenceRequest request =
        new UpdateCandidateNotificationPreferenceRequest();
    request.setAllNotifications(true);

    given(candidateRepository.findById(candidate.getId())).willReturn(Optional.of(candidate));

    candidateService.updateNotificationPreference(candidate.getId(), request);

    verify(candidateService, never()).save(any(Candidate.class));
    verify(candidateNoteService, never()).createCandidateNote(any(CreateCandidateNoteRequest.class));
  }

  @Test
  @DisplayName("deleteCandidateExam deletes exam, recomputes IELTS score and saves candidate")
  void deleteCandidateExam_deletesExamAndSavesCandidate() {
    CandidateExam candidateExam = new CandidateExam();
    candidateExam.setId(99L);
    candidateExam.setCandidate(candidate);
    candidate.setCandidateExams(new ArrayList<>());

    given(candidateExamRepository.findByIdLoadCandidate(99L)).willReturn(Optional.of(candidateExam));
    doReturn(candidate).when(candidateService).save(candidate);

    boolean result = candidateService.deleteCandidateExam(99L);

    assertTrue(result);
    assertNull(candidate.getIeltsScore());
    verify(candidateExamRepository).deleteById(99L);
    verify(candidateService).save(candidate);
  }

  @Test
  @DisplayName("deleteCandidateExam throws when exam does not exist")
  void deleteCandidateExam_throws_whenExamMissing() {
    given(candidateExamRepository.findByIdLoadCandidate(99L)).willReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class, () -> candidateService.deleteCandidateExam(99L));
  }

  @Test
  @DisplayName("resolveOutstandingTaskAssignments completes and abandons outstanding required tasks")
  void resolveOutstandingTaskAssignments_completesOutstandingRequiredTasks() {
    TaskAssignmentImpl requiredIncomplete = taskAssignment(false, false);
    TaskAssignmentImpl optionalIncomplete = taskAssignment(true, false);
    TaskAssignmentImpl deletedRequiredIncomplete = taskAssignment(false, false);
    deletedRequiredIncomplete.setStatus(Status.deleted);

    candidate.setTaskAssignments(
        new ArrayList<>(List.of(requiredIncomplete, optionalIncomplete, deletedRequiredIncomplete)));

    Set<Country> sourceCountries = Set.of(country);

    ResolveTaskAssignmentsRequest request = new ResolveTaskAssignmentsRequest();
    request.setCandidateIds(List.of(candidate.getId()));

    given(authService.getLoggedInUser()).willReturn(Optional.of(staffUser));
    given(userService.getDefaultSourceCountries(staffUser)).willReturn(sourceCountries);
    given(candidateRepository.findByIdLoadUser(candidate.getId(), sourceCountries))
        .willReturn(Optional.of(candidate));

    candidateService.resolveOutstandingTaskAssignments(request);

    assertNotNull(requiredIncomplete.getCompletedDate());
    assertNotNull(requiredIncomplete.getAbandonedDate());

    assertNull(optionalIncomplete.getCompletedDate());
    assertNull(optionalIncomplete.getAbandonedDate());

    assertNull(deletedRequiredIncomplete.getCompletedDate());
    assertNull(deletedRequiredIncomplete.getAbandonedDate());

    verify(taskAssignmentRepository, times(2)).save(any(TaskAssignmentImpl.class));
  }

  @Test
  @DisplayName("resolveOutstandingTaskAssignments ignores missing candidate ids")
  void resolveOutstandingTaskAssignments_ignoresMissingCandidates() {
    Set<Country> sourceCountries = Set.of(country);

    ResolveTaskAssignmentsRequest request = new ResolveTaskAssignmentsRequest();
    request.setCandidateIds(List.of(404L));

    given(authService.getLoggedInUser()).willReturn(Optional.of(staffUser));
    given(userService.getDefaultSourceCountries(staffUser)).willReturn(sourceCountries);
    given(candidateRepository.findByIdLoadUser(404L, sourceCountries)).willReturn(Optional.empty());

    assertDoesNotThrow(() -> candidateService.resolveOutstandingTaskAssignments(request));

    verify(taskAssignmentRepository, never()).save(any(TaskAssignmentImpl.class));
  }

  @Test
  @DisplayName("getCandidateFromRequest returns requested candidate for admin portal request")
  void getCandidateFromRequest_returnsRequestedCandidate_forAdminUserWithRequestCandidateId() {
    staffUser.setRole(Role.admin);

    given(authService.getLoggedInUser()).willReturn(Optional.of(staffUser));
    given(candidateRepository.findById(candidate.getId())).willReturn(Optional.of(candidate));

    Candidate result = candidateService.getCandidateFromRequest(candidate.getId());

    assertSame(candidate, result);
    verify(candidateRepository).findById(candidate.getId());
    verify(candidateRepository, never()).findByUserId(anyLong());
  }

  @Test
  @DisplayName("getCandidateFromRequest returns logged-in candidate for candidate portal request")
  void getCandidateFromRequest_returnsLoggedInCandidate_forCandidatePortalUser() {
    candidateUser.setRole(Role.user);

    given(authService.getLoggedInUser()).willReturn(Optional.of(candidateUser));
    given(candidateRepository.findByUserId(candidateUser.getId())).willReturn(candidate);

    Candidate result = candidateService.getCandidateFromRequest(candidate.getId());

    assertSame(candidate, result);
    verify(candidateRepository, never()).findById(candidate.getId());
    verify(candidateRepository).findByUserId(candidateUser.getId());
  }

  @Test
  @DisplayName("validateContactRequest throws when new email already belongs to another user")
  void validateContactRequest_throwsUsernameTaken_whenEmailAlreadyExistsForNewCandidate() {
    UpdateCandidateContactRequest request = new UpdateCandidateContactRequest();
    request.setEmail("duplicate@example.org");

    User existingUser = new User();
    existingUser.setId(999L);

    given(userRepository.findByEmailIgnoreCase("duplicate@example.org")).willReturn(existingUser);

    assertThrows(UsernameTakenException.class, () -> candidateService.validateContactRequest(null, request));
  }

  @Test
  @DisplayName("validateContactRequest allows unchanged email for same user")
  void validateContactRequest_allowsEmail_whenExistingUserIsSameUser() {
    UpdateCandidateContactRequest request = new UpdateCandidateContactRequest();
    request.setEmail("same@example.org");

    User existingUser = new User();
    existingUser.setId(123L);

    User currentUser = new User();
    currentUser.setId(123L);

    given(userRepository.findByEmailIgnoreCase("same@example.org")).willReturn(existingUser);

    assertDoesNotThrow(() -> candidateService.validateContactRequest(currentUser, request));
  }

  @Test
  @DisplayName("validateContactRequest converts non-unique email query result into UsernameTakenException")
  void validateContactRequest_throwsUsernameTaken_whenRepositoryReturnsTooManyResults() {
    UpdateCandidateContactRequest request = new UpdateCandidateContactRequest();
    request.setEmail("duplicate@example.org");

    given(userRepository.findByEmailIgnoreCase("duplicate@example.org"))
        .willThrow(new IncorrectResultSizeDataAccessException(1, 2));

    assertThrows(UsernameTakenException.class, () -> candidateService.validateContactRequest(null, request));
  }

  @Test
  @DisplayName("validateContactRequest ignores blank email")
  void validateContactRequest_ignoresBlankEmail() {
    UpdateCandidateContactRequest request = new UpdateCandidateContactRequest();
    request.setEmail("");

    assertDoesNotThrow(() -> candidateService.validateContactRequest(null, request));

    verify(userRepository, never()).findByEmailIgnoreCase(any());
  }

  @Test
  @DisplayName("getExportCandidateStrings shows full fields for unrestricted same-partner staff user")
  void getExportCandidateStrings_showsFullFields_forSamePartnerAdmin() {
    Candidate exportCandidate = exportCandidate();
    staffUser.setRole(Role.admin);
    staffUser.setPartner(partner);

    given(authService.getLoggedInUser()).willReturn(Optional.of(staffUser));

    String[] row = candidateService.getExportCandidateStrings(exportCandidate);

    assertEquals("CAND-300", row[0]);
    assertEquals("Amina", row[1]);
    assertEquals("Ahmadi", row[2]);
    assertEquals("Jordan", row[4]);
    assertEquals("Syria", row[5]);
    assertEquals("amina@example.org", row[7]);
    assertEquals("+1555000111", row[8]);
    assertEquals("https://tctalent.org/admin-portal/candidate/CAND-300", row[14]);
  }

  @Test
  @DisplayName("getExportCandidateStrings hides name and contact for semilimited user")
  void getExportCandidateStrings_hidesNameAndContact_forSemilimitedRole() {
    Candidate exportCandidate = exportCandidate();
    staffUser.setRole(Role.semilimited);
    staffUser.setPartner(partner);

    given(authService.getLoggedInUser()).willReturn(Optional.of(staffUser));

    String[] row = candidateService.getExportCandidateStrings(exportCandidate);

    assertEquals("CAND-300", row[0]);
    assertEquals("Hidden", row[1]);
    assertEquals("Hidden", row[2]);
    assertEquals("Jordan", row[4]);
    assertEquals("Syria", row[5]);
    assertEquals("Hidden", row[7]);
    assertEquals("Hidden", row[8]);
  }

  @Test
  @DisplayName("getExportCandidateStrings hides location for limited user")
  void getExportCandidateStrings_hidesLocation_forLimitedRole() {
    Candidate exportCandidate = exportCandidate();
    staffUser.setRole(Role.limited);
    staffUser.setPartner(partner);

    given(authService.getLoggedInUser()).willReturn(Optional.of(staffUser));

    String[] row = candidateService.getExportCandidateStrings(exportCandidate);

    assertEquals("CAND-300", row[0]);
    assertEquals("Hidden", row[1]);
    assertEquals("Hidden", row[2]);
    assertEquals("Hidden", row[4]);
    assertEquals("Hidden", row[5]);
    assertEquals("Hidden", row[7]);
    assertEquals("Hidden", row[8]);
  }

  @Test
  @DisplayName("getExportCandidateStrings treats different-partner candidate as semilimited")
  void getExportCandidateStrings_treatsDifferentPartnerCandidateAsSemilimited() {
    Candidate exportCandidate = exportCandidate();
    staffUser.setRole(Role.admin);
    staffUser.setPartner(otherPartner);

    given(authService.getLoggedInUser()).willReturn(Optional.of(staffUser));

    String[] row = candidateService.getExportCandidateStrings(exportCandidate);

    assertEquals("Hidden", row[1]);
    assertEquals("Hidden", row[2]);
    assertEquals("Jordan", row[4]);
    assertEquals("Syria", row[5]);
    assertEquals("Hidden", row[7]);
    assertEquals("Hidden", row[8]);
  }

  @Test
  @DisplayName("formatCandidateOccupation joins occupation names with newlines")
  void formatCandidateOccupation_joinsOccupationNames() {
    CandidateOccupation candidateOccupation = new CandidateOccupation();
    Occupation occupation = new Occupation();
    occupation.setName("Software Engineer");
    candidateOccupation.setOccupation(occupation);

    String result = candidateService.formatCandidateOccupation(List.of(candidateOccupation));

    assertEquals("Software Engineer\n", result);
  }

  @Test
  @DisplayName("formatCandidateOccupation returns empty string for null and empty lists")
  void formatCandidateOccupation_returnsEmptyString_forNullAndEmptyLists() {
    assertEquals("", candidateService.formatCandidateOccupation(null));
    assertEquals("", candidateService.formatCandidateOccupation(List.of()));
  }

  @Test
  @DisplayName("getEnglishSpokenProficiency returns only English spoken levels")
  void getEnglishSpokenProficiency_returnsOnlyEnglishSpokenLevels() {
    CandidateLanguage english = candidateLanguage("English", "Advanced");
    CandidateLanguage arabic = candidateLanguage("Arabic", "Native");

    String result = candidateService.getEnglishSpokenProficiency(List.of(english, arabic));

    assertEquals("Advanced\n", result);
  }

  @Test
  @DisplayName("getEnglishSpokenProficiency returns empty string when no English language exists")
  void getEnglishSpokenProficiency_returnsEmptyString_whenNoEnglishLanguageExists() {
    CandidateLanguage arabic = candidateLanguage("Arabic", "Native");

    String result = candidateService.getEnglishSpokenProficiency(List.of(arabic));

    assertEquals("", result);
  }

  @Test
  @DisplayName("candidate subfolder name and link helpers characterize every switch path")
  void candidateSubfolderHelpers_coverEverySubfolderType() {
    for (CandidateSubfolderType type : CandidateSubfolderType.values()) {
      String link = "https://drive.example.org/" + type.name();

      candidateService.setCandidateSubfolderlink(candidate, type, link);

      assertNotNull(candidateService.getCandidateSubfolderName(type));
      assertEquals(link, candidateService.getCandidateSubfolderlink(candidate, type));
    }

    assertEquals("Address", candidateService.getCandidateSubfolderName(CandidateSubfolderType.address));
    assertEquals("Registration", candidateService.getCandidateSubfolderName(CandidateSubfolderType.registration));
  }

  @Test
  @DisplayName("auditNoteIfRelocatedAddressChange creates added note when old address is empty")
  void auditNoteIfRelocatedAddressChange_createsAddedNote_whenOldAddressEmpty() {
    candidateService.auditNoteIfRelocatedAddressChange(
        candidate,
        "123 New Street",
        "Toronto",
        "ON",
        "Canada");

    verify(candidateNoteService).createCandidateNote(any(CreateCandidateNoteRequest.class));
  }

  @Test
  @DisplayName("auditNoteIfRelocatedAddressChange creates removed note when new address is empty")
  void auditNoteIfRelocatedAddressChange_createsRemovedNote_whenNewAddressEmpty() {
    candidate.setRelocatedAddress("123 Old Street");
    candidate.setRelocatedCity("Toronto");
    candidate.setRelocatedState("ON");
    candidate.setRelocatedCountry(country(500L, "Canada"));

    candidateService.auditNoteIfRelocatedAddressChange(candidate, null, null, null, null);

    verify(candidateNoteService).createCandidateNote(any(CreateCandidateNoteRequest.class));
  }

  @Test
  @DisplayName("auditNoteIfRelocatedAddressChange creates changed note when address changes")
  void auditNoteIfRelocatedAddressChange_createsChangedNote_whenAddressChanges() {
    candidate.setRelocatedAddress("123 Old Street");
    candidate.setRelocatedCity("Toronto");
    candidate.setRelocatedState("ON");
    candidate.setRelocatedCountry(country(500L, "Canada"));

    candidateService.auditNoteIfRelocatedAddressChange(
        candidate,
        "456 New Street",
        "Vancouver",
        "BC",
        "Canada");

    verify(candidateNoteService).createCandidateNote(any(CreateCandidateNoteRequest.class));
  }

  @Test
  @DisplayName("auditNoteIfRelocatedAddressChange does not create note when address is unchanged ignoring case")
  void auditNoteIfRelocatedAddressChange_doesNotCreateNote_whenAddressUnchangedIgnoringCase() {
    candidate.setRelocatedAddress("123 Street");
    candidate.setRelocatedCity("Toronto");
    candidate.setRelocatedState("ON");
    candidate.setRelocatedCountry(country(500L, "Canada"));

    candidateService.auditNoteIfRelocatedAddressChange(
        candidate,
        "123 street",
        "toronto",
        "on",
        "canada");

    verify(candidateNoteService, never()).createCandidateNote(any(CreateCandidateNoteRequest.class));
  }

  private Country country(long id, String name) {
    Country country = new Country();
    country.setId(id);
    country.setName(name);
    return country;
  }

  private SubmitRegistrationRequest submitRegistrationRequest(String acceptedPrivacyPolicyId) {
    SubmitRegistrationRequest request = new SubmitRegistrationRequest();
    request.setAcceptedPrivacyPolicyId(acceptedPrivacyPolicyId);
    return request;
  }

  private void stubLoggedInCandidate(Candidate candidate) {
    User user = candidate.getUser();
    given(authService.getLoggedInUser()).willReturn(Optional.of(user));
    given(candidateRepository.findByUserId(user.getId())).willReturn(candidate);
  }

  private TaskAssignmentImpl taskAssignment(boolean optional, boolean completed) {
    UploadTaskImpl task = new UploadTaskImpl();
    task.setName(optional ? "Optional task" : "Required task");
    task.setOptional(optional);

    TaskAssignmentImpl taskAssignment = new TaskAssignmentImpl();
    taskAssignment.setTask(task);
    taskAssignment.setDueDate(LocalDate.now().minusDays(1));
    taskAssignment.setCompletedDate(completed ? OffsetDateTime.now().minusDays(1) : null);
    return taskAssignment;
  }

  private Candidate exportCandidate() {
    Candidate exportCandidate = new Candidate();
    exportCandidate.setId(candidate.getId());
    exportCandidate.setCandidateNumber("CAND-300");
    exportCandidate.setUser(candidateUser);
    exportCandidate.setCountry(country);
    exportCandidate.setNationality(nationality);
    exportCandidate.setWhatsapp("+1555000111");
    exportCandidate.setCandidateOccupations(new ArrayList<>());
    exportCandidate.setCandidateEducations(new ArrayList<>());
    exportCandidate.setCandidateLanguages(new ArrayList<>());
    return exportCandidate;
  }

  private CandidateLanguage candidateLanguage(String languageName, String levelName) {
    Language language = new Language();
    language.setName(languageName);

    LanguageLevel level = new LanguageLevel();
    level.setName(levelName);

    CandidateLanguage candidateLanguage = new CandidateLanguage();
    candidateLanguage.setLanguage(language);
    candidateLanguage.setSpokenLevel(level);
    return candidateLanguage;
  }
}