/*
 * Copyright (c) 2026 Talent Catalog.
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



package org.tctalent.server.service.db.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.files.UploadType;
import org.tctalent.server.model.db.AttachmentType;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateAttachment;
import org.tctalent.server.model.db.CandidateCertification;
import org.tctalent.server.model.db.CandidateCitizenship;
import org.tctalent.server.model.db.CandidateDependant;
import org.tctalent.server.model.db.CandidateDestination;
import org.tctalent.server.model.db.CandidateEducation;
import org.tctalent.server.model.db.CandidateExam;
import org.tctalent.server.model.db.CandidateJobExperience;
import org.tctalent.server.model.db.CandidateLanguage;
import org.tctalent.server.model.db.CandidateNote;
import org.tctalent.server.model.db.CandidateOccupation;
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.model.db.CandidateProperty;
import org.tctalent.server.model.db.CandidatePropertyKey;
import org.tctalent.server.model.db.CandidateSavedList;
import org.tctalent.server.model.db.CandidateSkill;
import org.tctalent.server.model.db.CandidateStatus;
import org.tctalent.server.model.db.CandidateVisaCheck;
import org.tctalent.server.model.db.CandidateVisaJobCheck;
import org.tctalent.server.model.db.DuolingoCoupon;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.TaskAssignmentImpl;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidateAttachmentRepository;
import org.tctalent.server.repository.db.CandidateCertificationRepository;
import org.tctalent.server.repository.db.CandidateCitizenshipRepository;
import org.tctalent.server.repository.db.CandidateDependantRepository;
import org.tctalent.server.repository.db.CandidateDestinationRepository;
import org.tctalent.server.repository.db.CandidateEducationRepository;
import org.tctalent.server.repository.db.CandidateExamRepository;
import org.tctalent.server.repository.db.CandidateJobExperienceRepository;
import org.tctalent.server.repository.db.CandidateLanguageRepository;
import org.tctalent.server.repository.db.CandidateNoteRepository;
import org.tctalent.server.repository.db.CandidateOccupationRepository;
import org.tctalent.server.repository.db.CandidateOpportunityRepository;
import org.tctalent.server.repository.db.CandidatePropertyRepository;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.CandidateSavedListRepository;
import org.tctalent.server.repository.db.CandidateSkillRepository;
import org.tctalent.server.repository.db.CandidateVisaCheckRepository;
import org.tctalent.server.repository.db.DuolingoCouponRepository;
import org.tctalent.server.repository.db.TaskAssignmentRepository;
import org.tctalent.server.request.candidate.EraseCandidateRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.FileSystemService;
import org.tctalent.server.storage.StorageService;
import org.tctalent.server.util.filesystem.GoogleFileSystemFile;

@ExtendWith(MockitoExtension.class)
class CandidateErasureServiceImplTest {

  private static final long CANDIDATE_ID = 92520L;
  private static final String CANDIDATE_NUMBER = "92520";

  @Mock
  private AuthService authService;

  @Mock
  private CandidateRepository candidateRepository;

  @Mock
  private CandidateAttachmentRepository candidateAttachmentRepository;

  @Mock
  private CandidateSavedListRepository candidateSavedListRepository;

  @Mock
  private CandidateOpportunityRepository candidateOpportunityRepository;

  @Mock
  private TaskAssignmentRepository taskAssignmentRepository;

  @Mock
  private DuolingoCouponRepository duolingoCouponRepository;

  @Mock
  private CandidatePropertyRepository candidatePropertyRepository;

  @Mock
  private CandidateNoteRepository candidateNoteRepository;

  @Mock
  private CandidateCertificationRepository candidateCertificationRepository;

  @Mock
  private CandidateCitizenshipRepository candidateCitizenshipRepository;

  @Mock
  private CandidateDependantRepository candidateDependantRepository;

  @Mock
  private CandidateDestinationRepository candidateDestinationRepository;

  @Mock
  private CandidateEducationRepository candidateEducationRepository;

  @Mock
  private CandidateExamRepository candidateExamRepository;

  @Mock
  private CandidateJobExperienceRepository candidateJobExperienceRepository;

  @Mock
  private CandidateLanguageRepository candidateLanguageRepository;

  @Mock
  private CandidateSkillRepository candidateSkillRepository;

  @Mock
  private CandidateVisaCheckRepository candidateVisaCheckRepository;

  @Mock
  private CandidateOccupationRepository candidateOccupationRepository;

  @Mock
  private FileSystemService fileSystemService;

  @Mock
  private StorageService storageService;

  @InjectMocks
  private CandidateErasureServiceImpl candidateErasureService;

  private User actor;
  private User candidateUser;
  private Candidate candidate;
  private EraseCandidateRequest request;

  @BeforeEach
  void setUp() {
    actor = new User();
    actor.setId(1L);
    actor.setRole(Role.systemadmin);
    actor.setReadOnly(false);

    candidateUser = new User();
    candidateUser.setId(2L);
    candidateUser.setUsername("candidate-user");
    candidateUser.setFirstName("Jane");
    candidateUser.setLastName("Doe");
    candidateUser.setEmail("jane@example.com");
    candidateUser.setPasswordEnc("old-password");
    candidateUser.setStatus(Status.active);
    candidateUser.setLastLogin(OffsetDateTime.now());
    candidateUser.setResetToken("reset-token");
    candidateUser.setResetTokenIssuedDate(OffsetDateTime.now());
    candidateUser.setPasswordUpdatedDate(OffsetDateTime.now());
    candidateUser.setEmailVerificationToken("email-verification-token");
    candidateUser.setEmailVerificationTokenIssuedDate(OffsetDateTime.now());
    candidateUser.setEmailVerified(true);
    candidateUser.setUsingMfa(true);
    candidateUser.setMfaSecret("secret");
    candidateUser.setPurpose("Candidate purpose");
    candidateUser.setApprover(actor);
    candidateUser.setReadOnly(false);
    candidateUser.setJobCreator(true);

    candidate = new Candidate();
    candidate.setId(CANDIDATE_ID);
    candidate.setCandidateNumber(CANDIDATE_NUMBER);
    candidate.setUser(candidateUser);
    candidate.setStatus(CandidateStatus.active);
    candidate.setPublicId("public-id");
    candidate.setPhone("12345");
    candidate.setWhatsapp("67890");
    candidate.setDob(LocalDate.of(1990, 1, 1));
    candidate.setAddress1("Address");
    candidate.setCity("City");
    candidate.setState("State");
    candidate.setAdditionalInfo("Additional information");
    candidate.setCandidateMessage("Candidate message");
    candidate.setLinkedInLink("https://linkedin.example");
    candidate.setAspirations("Aspirations");
    candidate.setPartnerRef("partner-ref");
    candidate.setRegoIp("127.0.0.1");
    candidate.setShareableNotes("Shareable notes");
    candidate.setSurveyComment("Survey comment");
    candidate.setText("Search text");
    candidate.setExternalId("external-id");
    candidate.setExternalIdSource("external-source");
    candidate.setFolderlink("folder");
    candidate.setSflink("salesforce");
    candidate.setVideolink("video");
    candidate.setContactConsentRegistration(true);
    candidate.setContactConsentPartners(true);
    candidate.setPotentialDuplicate(true);

    request = new EraseCandidateRequest();
    request.setConfirmationCandidateNumber(CANDIDATE_NUMBER);
  }

  @Test
  void eraseCandidate_shouldThrowInvalidSessionException_whenNotLoggedIn() {
    when(authService.getLoggedInUser()).thenReturn(Optional.empty());

    assertThrows(
        InvalidSessionException.class,
        () -> candidateErasureService.eraseCandidate(CANDIDATE_ID, request)
    );

    verifyNoInteractions(candidateRepository);
  }

  @Test
  void eraseCandidate_shouldThrowInvalidRequestException_whenUserIsNotSystemAdmin() {
    actor.setRole(Role.admin);

    when(authService.getLoggedInUser()).thenReturn(Optional.of(actor));

    assertThrows(
        InvalidRequestException.class,
        () -> candidateErasureService.eraseCandidate(CANDIDATE_ID, request)
    );

    verify(candidateRepository, never()).findById(any());
  }

  @Test
  void eraseCandidate_shouldThrowInvalidRequestException_whenUserIsReadOnly() {
    actor.setReadOnly(true);

    when(authService.getLoggedInUser()).thenReturn(Optional.of(actor));

    assertThrows(
        InvalidRequestException.class,
        () -> candidateErasureService.eraseCandidate(CANDIDATE_ID, request)
    );

    verify(candidateRepository, never()).findById(any());
  }

  @Test
  void eraseCandidate_shouldThrowNoSuchObjectException_whenCandidateDoesNotExist() {
    when(authService.getLoggedInUser()).thenReturn(Optional.of(actor));
    when(candidateRepository.findById(CANDIDATE_ID)).thenReturn(Optional.empty());

    assertThrows(
        NoSuchObjectException.class,
        () -> candidateErasureService.eraseCandidate(CANDIDATE_ID, request)
    );
  }

  @Test
  void eraseCandidate_shouldThrowInvalidRequestException_whenCandidateNumberDoesNotMatch() {
    request.setConfirmationCandidateNumber("wrong-number");

    when(authService.getLoggedInUser()).thenReturn(Optional.of(actor));
    when(candidateRepository.findById(CANDIDATE_ID)).thenReturn(Optional.of(candidate));

    assertThrows(
        InvalidRequestException.class,
        () -> candidateErasureService.eraseCandidate(CANDIDATE_ID, request)
    );

    verify(candidateRepository, never()).saveAndFlush(any());
  }

  @Test
  void eraseCandidate_shouldAllowBlankCandidateNumberConfirmation() {
    request.setConfirmationCandidateNumber(" ");

    stubSuccessfulRepositoryCalls();

    Candidate result = candidateErasureService.eraseCandidate(CANDIDATE_ID, request);

    assertSame(candidate, result);
    verify(candidateRepository, times(2)).saveAndFlush(candidate);
  }

  @Test
  void eraseCandidate_shouldScrubCandidateAndUserAndSetDeletedAuditFields() {
    stubSuccessfulRepositoryCalls();

    Candidate result = candidateErasureService.eraseCandidate(CANDIDATE_ID, request);

    assertSame(candidate, result);

    assertEquals(CandidateStatus.deleted, candidate.getStatus());
    assertNull(candidate.getPublicId());
    assertNull(candidate.getPhone());
    assertNull(candidate.getWhatsapp());
    assertNull(candidate.getDob());
    assertNull(candidate.getAddress1());
    assertNull(candidate.getCity());
    assertNull(candidate.getState());
    assertNull(candidate.getAdditionalInfo());
    assertNull(candidate.getCandidateMessage());
    assertNull(candidate.getLinkedInLink());
    assertNull(candidate.getAspirations());
    assertNull(candidate.getPartnerRef());
    assertNull(candidate.getRegoIp());
    assertNull(candidate.getShareableNotes());
    assertNull(candidate.getShareableCv());
    assertNull(candidate.getShareableDoc());
    assertNull(candidate.getSurveyComment());
    assertNull(candidate.getText());
    assertNull(candidate.getCountry());
    assertNull(candidate.getNationality());
    assertNull(candidate.getBirthCountry());
    assertNull(candidate.getFolderlink());
    assertEquals("salesforce", candidate.getSflink());
    assertNull(candidate.getVideolink());
    assertNull(candidate.getExternalId());
    assertNull(candidate.getExternalIdSource());
    assertFalse(candidate.getContactConsentRegistration());
    assertFalse(candidate.getContactConsentPartners());
    assertFalse(candidate.getPotentialDuplicate());

    assertEquals(actor, candidate.getDeletedBy());
    assertNotNull(candidate.getDeletedDate());
    assertEquals(actor, candidate.getUpdatedBy());
    assertNotNull(candidate.getUpdatedDate());

    assertEquals("deleted-candidate-" + CANDIDATE_ID, candidateUser.getUsername());
    assertEquals("Deleted", candidateUser.getFirstName());
    assertEquals("Candidate", candidateUser.getLastName());
    assertEquals("deleted-candidate-" + CANDIDATE_ID + "@deleted.invalid",
        candidateUser.getEmail());
    assertEquals("N/A [DELETED]", candidateUser.getPasswordEnc());
    assertEquals(Status.deleted, candidateUser.getStatus());
    assertNull(candidateUser.getLastLogin());
    assertNull(candidateUser.getResetToken());
    assertNull(candidateUser.getResetTokenIssuedDate());
    assertNull(candidateUser.getPasswordUpdatedDate());
    assertNull(candidateUser.getEmailVerificationToken());
    assertNull(candidateUser.getEmailVerificationTokenIssuedDate());
    assertFalse(candidateUser.getEmailVerified());
    assertFalse(candidateUser.getUsingMfa());
    assertNull(candidateUser.getMfaSecret());
    assertNull(candidateUser.getPurpose());
    assertNull(candidateUser.getApprover());
    assertFalse(candidateUser.isJobCreator());
    assertEquals(true, candidateUser.getReadOnly());

    verify(candidateRepository, times(2)).saveAndFlush(candidate);
  }

  @Test
  void eraseCandidate_shouldScrubSavedListsAndAttachments() throws IOException {
    CandidateAttachment shareableCv = new CandidateAttachment();
    shareableCv.setId(10L);

    CandidateAttachment googleAttachment = new CandidateAttachment();
    googleAttachment.setId(11L);
    googleAttachment.setCandidate(candidate);
    googleAttachment.setType(AttachmentType.googlefile);
    googleAttachment.setName("cv.pdf");
    googleAttachment.setUrl("https://drive.google.com/file");
    googleAttachment.setFileType("pdf");
    googleAttachment.setMigrated(true);
    googleAttachment.setPublicId("public-file-id");
    googleAttachment.setStorageKey("storage-key");
    googleAttachment.setTextExtract("CV text");
    googleAttachment.setUploadType(UploadType.cv);
    googleAttachment.setActive(true);
    googleAttachment.setBucket("bucket");
    googleAttachment.setContentLength(100L);
    googleAttachment.setSha256Hex("sha");

    CandidateSavedList savedList = new CandidateSavedList();
    savedList.setContextNote("context note");
    savedList.setShareableCv(shareableCv);
    savedList.setShareableDoc(googleAttachment);

    candidate.setShareableCv(shareableCv);
    candidate.setShareableDoc(googleAttachment);

    stubSuccessfulRepositoryCalls();
    when(candidateSavedListRepository.findByCandidate_Id(CANDIDATE_ID))
        .thenReturn(List.of(savedList));
    when(candidateAttachmentRepository.findByCandidateId(CANDIDATE_ID))
        .thenReturn(List.of(googleAttachment));

    candidateErasureService.eraseCandidate(CANDIDATE_ID, request);

    assertNull(candidate.getShareableCv());
    assertNull(candidate.getShareableDoc());

    assertNull(savedList.getContextNote());
    assertNull(savedList.getShareableCv());
    assertNull(savedList.getShareableDoc());

    assertNull(googleAttachment.getType());
    assertNull(googleAttachment.getName());
    assertEquals("erased://candidate-attachment/11", googleAttachment.getUrl());
    assertNull(googleAttachment.getFileType());
    assertFalse(googleAttachment.isMigrated());
    assertNull(googleAttachment.getPublicId());
    assertNull(googleAttachment.getStorageKey());
    assertNull(googleAttachment.getTextExtract());
    assertNull(googleAttachment.getUploadType());
    assertFalse(googleAttachment.isActive());
    assertNull(googleAttachment.getBucket());
    assertNull(googleAttachment.getContentLength());
    assertNull(googleAttachment.getSha256Hex());

    verify(fileSystemService).deleteFile(any(GoogleFileSystemFile.class));
    verify(candidateSavedListRepository).saveAll(List.of(savedList));
    verify(candidateSavedListRepository).flush();
    verify(candidateAttachmentRepository).saveAll(List.of(googleAttachment));
    verify(candidateAttachmentRepository).flush();
  }

  @Test
  void eraseCandidate_shouldDeleteGrnFileFromStorageAndContinueIfFileDeleteFails()
      throws IOException {
    CandidateAttachment grnAttachment = new CandidateAttachment();
    grnAttachment.setId(12L);
    grnAttachment.setCandidate(candidate);
    grnAttachment.setType(AttachmentType.grnfile);
    grnAttachment.setStorageKey("storage-key");
    grnAttachment.setUrl("https://old-file.example");
    grnAttachment.setName("document.pdf");
    grnAttachment.setActive(true);

    stubSuccessfulRepositoryCalls();
    when(candidateAttachmentRepository.findByCandidateId(CANDIDATE_ID))
        .thenReturn(List.of(grnAttachment));
    doThrow(new RuntimeException("Storage failed"))
        .when(storageService).delete("storage-key");

    Candidate result = candidateErasureService.eraseCandidate(CANDIDATE_ID, request);

    assertSame(candidate, result);
    assertNull(grnAttachment.getType());
    assertEquals("erased://candidate-attachment/12", grnAttachment.getUrl());
    assertNull(grnAttachment.getStorageKey());
    assertFalse(grnAttachment.isActive());

    verify(storageService).delete("storage-key");
    verify(candidateAttachmentRepository).saveAll(List.of(grnAttachment));
    verify(candidateRepository, times(2)).saveAndFlush(candidate);
  }

  @Test
  void eraseCandidate_shouldScrubRelatedCandidateRows() {
    CandidateOpportunity opportunity = new CandidateOpportunity();
    opportunity.setClosingCommentsForCandidate("candidate comments");
    opportunity.setEmployerFeedback("feedback");
    opportunity.setFileOfferLink("file link");
    opportunity.setFileOfferName("file name");
    opportunity.setRelocatingDependantIds(List.of(1L, 2L));
    opportunity.setClosingComments("closing comments");
    opportunity.setNextStep("next step");
    opportunity.setNextStepDueDate(LocalDate.now());
    opportunity.setName("opportunity name");
    opportunity.setSfId("sf-id");

    TaskAssignmentImpl taskAssignment = new TaskAssignmentImpl();
    taskAssignment.setCandidateNotes("candidate notes");
    taskAssignment.setStatus(Status.active);

    DuolingoCoupon coupon = new DuolingoCoupon();
    coupon.setCandidate(candidate);
    coupon.setDateSent(OffsetDateTime.now().toLocalDateTime());

    CandidateProperty property = new CandidateProperty();
    property.setId(new CandidatePropertyKey(CANDIDATE_ID, "question"));
    property.setCandidate(candidate);
    property.setValue("answer");
    property.setRelatedTaskAssignment(taskAssignment);

    CandidateNote note = new CandidateNote();
    note.setTitle("Personal title");
    note.setComment("Personal comment");

    CandidateCertification certification = new CandidateCertification();
    certification.setName("Certificate");
    certification.setInstitution("Institution");
    certification.setDateCompleted(LocalDate.now());

    CandidateCitizenship citizenship = new CandidateCitizenship();
    citizenship.setHasPassport(null);
    citizenship.setPassportExp(LocalDate.now());
    citizenship.setNotes("notes");

    CandidateDependant dependant = new CandidateDependant();
    dependant.setName("Dependant name");
    dependant.setRegisteredNumber("registered number");
    dependant.setHealthNotes("health notes");

    CandidateDestination destination = new CandidateDestination();
    destination.setInterest(null);
    destination.setNotes("destination notes");

    CandidateEducation education = new CandidateEducation();
    education.setInstitution("University");
    education.setCourseName("Course");
    education.setYearCompleted(2020);

    CandidateExam exam = new CandidateExam();
    exam.setOtherExam("Other exam");
    exam.setScore("100");
    exam.setYear(2020L);
    exam.setNotes("exam notes");

    CandidateJobExperience experience = new CandidateJobExperience();
    experience.setCompanyName("Company");
    experience.setRole("Role");
    experience.setStartDate(LocalDate.now());
    experience.setEndDate(LocalDate.now());
    experience.setFullTime(true);
    experience.setPaid(true);
    experience.setDescription("description");

    CandidateLanguage language = new CandidateLanguage();

    CandidateSkill skill = new CandidateSkill();
    skill.setSkill("skill");
    skill.setTimePeriod("5 years");

    CandidateVisaJobCheck visaJobCheck = new CandidateVisaJobCheck();
    visaJobCheck.setInterestNotes("interest notes");
    visaJobCheck.setQualificationNotes("qualification notes");
    visaJobCheck.setOccupationNotes("occupation notes");
    visaJobCheck.setNotes("visa job notes");
    visaJobCheck.setRelevantWorkExp("work exp");
    visaJobCheck.setAgeRequirement("age");
    visaJobCheck.setPreferredPathways("preferred");
    visaJobCheck.setIneligiblePathways("ineligible");
    visaJobCheck.setEligiblePathways("eligible");
    visaJobCheck.setOccupationCategory("category");
    visaJobCheck.setOccupationSubCategory("sub-category");
    visaJobCheck.setLanguagesThresholdNotes("language notes");

    CandidateVisaCheck visaCheck = new CandidateVisaCheck();
    visaCheck.setProtectionGrounds("grounds");
    visaCheck.setEnglishThresholdNotes("english notes");
    visaCheck.setHealthAssessmentNotes("health notes");
    visaCheck.setCharacterAssessmentNotes("character notes");
    visaCheck.setSecurityRiskNotes("security notes");
    visaCheck.setOverallRiskNotes("overall notes");
    visaCheck.setValidTravelDocsNotes("travel notes");
    visaCheck.setPathwayAssessmentNotes("pathway notes");
    visaCheck.setDestinationFamilyLocation("family location");
    visaCheck.getCandidateVisaJobChecks().add(visaJobCheck);

    CandidateOccupation occupation = new CandidateOccupation();
    occupation.setYearsExperience(9L);
    occupation.setTopCandidate(true);

    stubSuccessfulRepositoryCalls();
    when(candidateOpportunityRepository.findByCandidate_Id(CANDIDATE_ID))
        .thenReturn(List.of(opportunity));
    when(taskAssignmentRepository.findByCandidate_Id(CANDIDATE_ID))
        .thenReturn(List.of(taskAssignment));
    when(duolingoCouponRepository.findAllByCandidateId(CANDIDATE_ID))
        .thenReturn(List.of(coupon));
    when(candidatePropertyRepository.findByCandidate_Id(CANDIDATE_ID))
        .thenReturn(List.of(property));
    when(candidateNoteRepository.findByCandidateId(CANDIDATE_ID))
        .thenReturn(List.of(note));
    when(candidateCertificationRepository.findByCandidateId(CANDIDATE_ID))
        .thenReturn(List.of(certification));
    when(candidateCitizenshipRepository.findByCandidate_Id(CANDIDATE_ID))
        .thenReturn(List.of(citizenship));
    when(candidateDependantRepository.findByCandidateId(CANDIDATE_ID))
        .thenReturn(List.of(dependant));
    when(candidateDestinationRepository.findByCandidateId(CANDIDATE_ID))
        .thenReturn(List.of(destination));
    when(candidateEducationRepository.findByCandidateId(CANDIDATE_ID))
        .thenReturn(List.of(education));
    when(candidateExamRepository.findByCandidateId(CANDIDATE_ID))
        .thenReturn(List.of(exam));
    when(candidateJobExperienceRepository.findByCandidateId(CANDIDATE_ID))
        .thenReturn(List.of(experience));
    when(candidateLanguageRepository.findByCandidateId(CANDIDATE_ID))
        .thenReturn(List.of(language));
    when(candidateSkillRepository.findByCandidateId(CANDIDATE_ID))
        .thenReturn(List.of(skill));
    when(candidateVisaCheckRepository.findByCandidate_Id(CANDIDATE_ID))
        .thenReturn(List.of(visaCheck));
    when(candidateOccupationRepository.findByCandidateId(CANDIDATE_ID))
        .thenReturn(List.of(occupation));

    candidateErasureService.eraseCandidate(CANDIDATE_ID, request);

    assertNull(opportunity.getClosingCommentsForCandidate());
    assertNull(opportunity.getEmployerFeedback());
    assertNull(opportunity.getFileOfferLink());
    assertNull(opportunity.getFileOfferName());
    assertNull(opportunity.getRelocatingDependantIds());
    assertNull(opportunity.getClosingComments());
    assertNull(opportunity.getNextStep());
    assertNull(opportunity.getNextStepDueDate());
    assertNull(opportunity.getName());
    assertEquals("sf-id", opportunity.getSfId());

    assertNull(taskAssignment.getCandidateNotes());
    assertEquals(Status.deleted, taskAssignment.getStatus());

    assertNull(coupon.getCandidate());
    assertNull(coupon.getDateSent());

    assertNull(property.getValue());
    assertNull(property.getRelatedTaskAssignment());

    assertEquals("Deleted candidate note", note.getTitle());
    assertNull(note.getComment());

    assertNull(certification.getName());
    assertNull(certification.getInstitution());
    assertNull(certification.getDateCompleted());

    assertNull(citizenship.getNationality());
    assertNull(citizenship.getHasPassport());
    assertNull(citizenship.getPassportExp());
    assertNull(citizenship.getNotes());

    assertNull(dependant.getName());
    assertNull(dependant.getRegisteredNumber());
    assertNull(dependant.getHealthNotes());

    assertNull(destination.getCountry());
    assertNull(destination.getInterest());
    assertNull(destination.getNotes());

    assertNull(education.getEducationType());
    assertNull(education.getEducationMajor());
    assertNull(education.getLengthOfCourseYears());
    assertNull(education.getInstitution());
    assertNull(education.getCourseName());
    assertNull(education.getYearCompleted());
    assertNull(education.getIncomplete());

    assertNull(exam.getExam());
    assertNull(exam.getOtherExam());
    assertNull(exam.getScore());
    assertNull(exam.getYear());
    assertNull(exam.getNotes());

    assertNull(experience.getCountry());
    assertNull(experience.getCompanyName());
    assertNull(experience.getRole());
    assertNull(experience.getStartDate());
    assertNull(experience.getEndDate());
    assertNull(experience.getFullTime());
    assertNull(experience.getPaid());
    assertNull(experience.getDescription());

    assertNull(language.getLanguage());
    assertNull(language.getWrittenLevel());
    assertNull(language.getSpokenLevel());

    assertNull(skill.getSkill());
    assertNull(skill.getTimePeriod());

    assertNull(visaCheck.getCountry());
    assertNull(visaCheck.getProtectionGrounds());
    assertNull(visaCheck.getEnglishThresholdNotes());
    assertNull(visaCheck.getHealthAssessmentNotes());
    assertNull(visaCheck.getCharacterAssessmentNotes());
    assertNull(visaCheck.getSecurityRiskNotes());
    assertNull(visaCheck.getOverallRiskNotes());
    assertNull(visaCheck.getValidTravelDocsNotes());
    assertNull(visaCheck.getPathwayAssessmentNotes());
    assertNull(visaCheck.getDestinationFamilyLocation());

    assertNull(visaJobCheck.getJobOpp());
    assertNull(visaJobCheck.getInterestNotes());
    assertNull(visaJobCheck.getQualificationNotes());
    assertNull(visaJobCheck.getOccupation());
    assertNull(visaJobCheck.getOccupationNotes());
    assertNull(visaJobCheck.getNotes());
    assertNull(visaJobCheck.getRelevantWorkExp());
    assertNull(visaJobCheck.getAgeRequirement());
    assertNull(visaJobCheck.getPreferredPathways());
    assertNull(visaJobCheck.getIneligiblePathways());
    assertNull(visaJobCheck.getEligiblePathways());
    assertNull(visaJobCheck.getOccupationCategory());
    assertNull(visaJobCheck.getOccupationSubCategory());
    assertNull(visaJobCheck.getLanguagesThresholdNotes());

    assertNull(occupation.getOccupation());
    assertEquals(0L, occupation.getYearsExperience());
    assertFalse(occupation.getTopCandidate());

    verify(candidateOpportunityRepository).saveAll(List.of(opportunity));
    verify(taskAssignmentRepository).saveAll(List.of(taskAssignment));
    verify(duolingoCouponRepository).saveAll(List.of(coupon));
    verify(candidatePropertyRepository).saveAll(List.of(property));
    verify(candidateNoteRepository).saveAll(List.of(note));
    verify(candidateCertificationRepository).saveAll(List.of(certification));
    verify(candidateCitizenshipRepository).saveAll(List.of(citizenship));
    verify(candidateDependantRepository).saveAll(List.of(dependant));
    verify(candidateDestinationRepository).saveAll(List.of(destination));
    verify(candidateEducationRepository).saveAll(List.of(education));
    verify(candidateExamRepository).saveAll(List.of(exam));
    verify(candidateJobExperienceRepository).saveAll(List.of(experience));
    verify(candidateLanguageRepository).saveAll(List.of(language));
    verify(candidateSkillRepository).saveAll(List.of(skill));
    verify(candidateVisaCheckRepository).saveAll(List.of(visaCheck));
    verify(candidateOccupationRepository).saveAll(List.of(occupation));
  }

  private void stubSuccessfulRepositoryCalls() {
    when(authService.getLoggedInUser()).thenReturn(Optional.of(actor));
    when(candidateRepository.findById(CANDIDATE_ID)).thenReturn(Optional.of(candidate));
    when(candidateRepository.saveAndFlush(candidate)).thenReturn(candidate);

    when(candidateSavedListRepository.findByCandidate_Id(CANDIDATE_ID))
        .thenReturn(List.of());
    when(candidateAttachmentRepository.findByCandidateId(CANDIDATE_ID))
        .thenReturn(List.of());
    when(candidateOpportunityRepository.findByCandidate_Id(CANDIDATE_ID))
        .thenReturn(List.of());
    when(taskAssignmentRepository.findByCandidate_Id(CANDIDATE_ID))
        .thenReturn(List.of());
    when(duolingoCouponRepository.findAllByCandidateId(CANDIDATE_ID))
        .thenReturn(List.of());
    when(candidatePropertyRepository.findByCandidate_Id(CANDIDATE_ID))
        .thenReturn(List.of());
    when(candidateNoteRepository.findByCandidateId(CANDIDATE_ID))
        .thenReturn(List.of());
    when(candidateCertificationRepository.findByCandidateId(CANDIDATE_ID))
        .thenReturn(List.of());
    when(candidateCitizenshipRepository.findByCandidate_Id(CANDIDATE_ID))
        .thenReturn(List.of());
    when(candidateDependantRepository.findByCandidateId(CANDIDATE_ID))
        .thenReturn(List.of());
    when(candidateDestinationRepository.findByCandidateId(CANDIDATE_ID))
        .thenReturn(List.of());
    when(candidateEducationRepository.findByCandidateId(CANDIDATE_ID))
        .thenReturn(List.of());
    when(candidateExamRepository.findByCandidateId(CANDIDATE_ID))
        .thenReturn(List.of());
    when(candidateJobExperienceRepository.findByCandidateId(CANDIDATE_ID))
        .thenReturn(List.of());
    when(candidateLanguageRepository.findByCandidateId(CANDIDATE_ID))
        .thenReturn(List.of());
    when(candidateSkillRepository.findByCandidateId(CANDIDATE_ID))
        .thenReturn(List.of());
    when(candidateVisaCheckRepository.findByCandidate_Id(CANDIDATE_ID))
        .thenReturn(List.of());
    when(candidateOccupationRepository.findByCandidateId(CANDIDATE_ID))
        .thenReturn(List.of());
  }
}