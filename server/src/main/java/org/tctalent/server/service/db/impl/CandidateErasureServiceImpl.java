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

import jakarta.transaction.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.files.UploadType;
import org.tctalent.server.logging.LogBuilder;
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
import org.tctalent.server.service.db.CandidateErasureService;
import org.tctalent.server.service.db.FileSystemService;
import org.tctalent.server.storage.StorageService;
import org.tctalent.server.util.filesystem.GoogleFileSystemFile;

/**
 * Default implementation of {@link CandidateErasureService}.
 *
 * <p>This service performs one full candidate data erasure. There is no partial,
 * reporting, anonymised, or mid-level erasure option.</p>
 *
 * <p>The candidate row and related rows are intentionally kept where possible so that database
 * relationships do not break. Instead of deleting rows, this service scrubs personal data,
 * candidate-submitted free text, documents, CV text, search text, external IDs, and login data.</p>
 *
 * <p>Where a field can be set to {@code null}, it is set to {@code null}. Where the database may
 * require a value, this service uses a safe non-identifying placeholder value.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateErasureServiceImpl implements CandidateErasureService {

  private static final String ACTION = "EraseCandidate";

  /**
   * Placeholder location used when an attachment row is kept but the real file location is erased.
   *
   * <p>Some schemas require candidate_attachment.location to be non-null.</p>
   */
  private static final String ERASED_ATTACHMENT_LOCATION = "erased://candidate-attachment";

  private final AuthService authService;
  private final CandidateRepository candidateRepository;
  private final CandidateAttachmentRepository candidateAttachmentRepository;
  private final CandidateSavedListRepository candidateSavedListRepository;
  private final CandidateOpportunityRepository candidateOpportunityRepository;
  private final TaskAssignmentRepository taskAssignmentRepository;
  private final DuolingoCouponRepository duolingoCouponRepository;
  private final CandidatePropertyRepository candidatePropertyRepository;
  private final CandidateNoteRepository candidateNoteRepository;
  private final CandidateCertificationRepository candidateCertificationRepository;
  private final CandidateCitizenshipRepository candidateCitizenshipRepository;
  private final CandidateDependantRepository candidateDependantRepository;
  private final CandidateDestinationRepository candidateDestinationRepository;
  private final CandidateEducationRepository candidateEducationRepository;
  private final CandidateExamRepository candidateExamRepository;
  private final CandidateJobExperienceRepository candidateJobExperienceRepository;
  private final CandidateLanguageRepository candidateLanguageRepository;
  private final CandidateSkillRepository candidateSkillRepository;
  private final CandidateVisaCheckRepository candidateVisaCheckRepository;
  private final CandidateOccupationRepository candidateOccupationRepository;
  private final FileSystemService fileSystemService;
  private final StorageService storageService;

  /**
   * Fully erases the personally identifiable data for the given candidate.
   *
   * <p>This method performs one full erasure flow only. It does not support reporting or partial
   * erasure.</p>
   *
   * @param candidateId ID of the candidate to erase.
   * @param request confirmation for the erasure.
   * @return the erased deleted-placeholder candidate.
   */
  @Override
  @Transactional
  public Candidate eraseCandidate(long candidateId, EraseCandidateRequest request) {
    final User actor = authService.getLoggedInUser()
        .orElseThrow(() -> new InvalidSessionException("Not logged in"));

    validateSystemAdmin(actor);

    final Candidate candidate = candidateRepository.findById(candidateId)
        .orElseThrow(() -> new NoSuchObjectException(Candidate.class, candidateId));

    validateCandidateNumberConfirmation(candidate, request);

    LogBuilder.builder(log)
        .user(Optional.of(actor))
        .candidateId(candidateId)
        .action(ACTION)
        .message("Starting full candidate data erasure.")
        .logInfo();

    clearShareableAttachmentReferences(candidate);
    eraseAttachments(candidateId);
    scrubRelatedData(candidateId);
    scrubCandidate(candidate);
    scrubUser(candidate.getUser(), candidate.getId());

    candidate.setDeletedFields(actor);
    candidate.setAuditFields(actor);
    Candidate erasedCandidate = candidateRepository.saveAndFlush(candidate);

    LogBuilder.builder(log)
        .user(Optional.of(actor))
        .candidateId(candidateId)
        .action(ACTION)
        .message("Full candidate data erasure completed")
        .logInfo();

    return erasedCandidate;
  }

  /**
   * Validates that the logged-in user can perform full candidate data erasure.
   *
   * <p>This is restricted to system admins because the action is destructive and irreversible.</p>
   *
   * @param actor logged-in user.
   */
  private void validateSystemAdmin(User actor) {
    if (!Role.systemadmin.equals(actor.getRole())) {
      throw new InvalidRequestException("Only system admin users can erase candidate data.");
    }

    if (actor.getReadOnly()) {
      throw new InvalidRequestException("Read-only users cannot erase candidate data.");
    }
  }

  /**
   * Validates the candidate number confirmation if provided.
   *
   * @param candidate candidate being erased.
   * @param request erasure request.
   */
  private void validateCandidateNumberConfirmation(
      Candidate candidate, EraseCandidateRequest request
  ) {
    String confirmation = request.getConfirmationCandidateNumber();

    if (!StringUtils.hasText(confirmation)
        || !confirmation.trim().equals(candidate.getCandidateNumber())) {
      throw new InvalidRequestException(
          "Candidate number confirmation does not match the selected candidate.");
    }
  }

  /**
   * Clears candidate and saved-list references to shareable attachments.
   *
   * <p>This happens before attachment rows are scrubbed because saved-list rows may reference
   * shareable CV/document attachments.</p>
   *
   * @param candidate candidate being erased.
   */
  private void clearShareableAttachmentReferences(Candidate candidate) {
    candidate.setShareableCv(null);
    candidate.setShareableDoc(null);
    candidateRepository.saveAndFlush(candidate);

    List<CandidateSavedList> savedLists =
        candidateSavedListRepository.findByCandidate_Id(candidate.getId());

    for (CandidateSavedList savedList : savedLists) {
      savedList.setContextNote(null);
      savedList.setShareableCv(null);
      savedList.setShareableDoc(null);
    }

    candidateSavedListRepository.saveAll(savedLists);
    candidateSavedListRepository.flush();
  }

  /**
   * Deletes physical attachment files where possible and scrubs attachment rows.
   *
   * <p>Attachment rows are not deleted. The real file location, extracted CV text, storage keys,
   * public IDs, hashes, names, and upload metadata are removed or replaced with safe placeholder
   * values.</p>
   *
   * @param candidateId candidate ID.
   */
  private void eraseAttachments(long candidateId) {
    List<CandidateAttachment> attachments =
        candidateAttachmentRepository.findByCandidateId(candidateId);

    for (CandidateAttachment attachment : attachments) {
      deleteExternalAttachmentFile(attachment);
      scrubAttachment(attachment);
    }

    candidateAttachmentRepository.saveAll(attachments);
    candidateAttachmentRepository.flush();
  }

  /**
   * Deletes the physical file represented by the attachment when supported.
   *
   * @param attachment candidate attachment.
   */
  private void deleteExternalAttachmentFile(CandidateAttachment attachment) {
    if (attachment == null || attachment.getType() == null) {
      return;
    }

    try {
      if (AttachmentType.googlefile.equals(attachment.getType())
          && StringUtils.hasText(attachment.getUrl())) {
        fileSystemService.deleteFile(new GoogleFileSystemFile(attachment.getUrl()));
      } else if (AttachmentType.grnfile.equals(attachment.getType())
          && StringUtils.hasText(attachment.getStorageKey())) {
        storageService.delete(attachment.getStorageKey());
      }
    } catch (IOException | RuntimeException ex) {
      LogBuilder.builder(log)
          .candidateId(
              attachment.getCandidate() == null ? null : attachment.getCandidate().getId())
          .action(ACTION)
          .message("Could not delete external attachment file. attachmentId="
              + attachment.getId())
          .logError(ex);
    }
  }

  /**
   * Scrubs one attachment row while preserving the row and candidate relationship.
   *
   * @param attachment attachment to scrub.
   */
  private void scrubAttachment(CandidateAttachment attachment) {
    if (attachment == null) {
      return;
    }

    attachment.setType(null);
    attachment.setName(null);
    attachment.setUrl(ERASED_ATTACHMENT_LOCATION + "/" + attachment.getId());
    attachment.setFileType(null);
    attachment.setMigrated(false);
    attachment.setPublicId(null);
    attachment.setStorageKey(null);
    attachment.setTextExtract(null);
    attachment.setUploadType((UploadType) null);
    attachment.setActive(false);
    attachment.setBucket(null);
    attachment.setContentLength(null);
    attachment.setSha256Hex(null);
  }

  /**
   * Scrubs all related candidate data through repositories.
   *
   * <p>No rows are deleted here. Related rows are kept where possible and personal/free-text fields
   * are cleared.</p>
   *
   * @param candidateId candidate ID.
   */
  private void scrubRelatedData(long candidateId) {
    scrubCandidateOpportunities(candidateId);
    scrubTaskAssignments(candidateId);
    scrubDuolingoCoupons(candidateId);
    scrubCandidateProperties(candidateId);
    scrubCandidateNotes(candidateId);
    scrubCandidateCertifications(candidateId);
    scrubCandidateCitizenships(candidateId);
    scrubCandidateDependants(candidateId);
    scrubCandidateDestinations(candidateId);
    scrubCandidateEducations(candidateId);
    scrubCandidateExams(candidateId);
    scrubCandidateJobExperiences(candidateId);
    scrubCandidateLanguages(candidateId);
    scrubCandidateSkills(candidateId);
    scrubCandidateVisaChecks(candidateId);
    scrubCandidateOccupations(candidateId);
  }

  /**
   * Scrubs candidate-specific opportunity data.
   *
   * @param candidateId candidate ID.
   */
  private void scrubCandidateOpportunities(long candidateId) {
    List<CandidateOpportunity> opportunities =
        candidateOpportunityRepository.findByCandidate_Id(candidateId);

    for (CandidateOpportunity opportunity : opportunities) {
      opportunity.setClosingCommentsForCandidate(null);
      opportunity.setEmployerFeedback(null);
      opportunity.setFileOfferLink(null);
      opportunity.setFileOfferName(null);
      opportunity.setRelocatingDependantIds(null);
      opportunity.setClosingComments(null);
      opportunity.setNextStep(null);
      opportunity.setNextStepDueDate(null);
      opportunity.setName(null);
    }

    candidateOpportunityRepository.saveAll(opportunities);
    candidateOpportunityRepository.flush();
  }

  /**
   * Scrubs candidate task assignments.
   *
   * @param candidateId candidate ID.
   */
  private void scrubTaskAssignments(long candidateId) {
    List<TaskAssignmentImpl> assignments =
        taskAssignmentRepository.findByCandidate_Id(candidateId);

    for (TaskAssignmentImpl assignment : assignments) {
      assignment.setCandidateNotes(null);
      assignment.setStatus(Status.deleted);
    }

    taskAssignmentRepository.saveAll(assignments);
    taskAssignmentRepository.flush();
  }

  /**
   * Scrubs Duolingo coupon records linked to the candidate.
   *
   * @param candidateId candidate ID.
   */
  private void scrubDuolingoCoupons(long candidateId) {
    List<DuolingoCoupon> coupons = duolingoCouponRepository.findAllByCandidateId(candidateId);

    for (DuolingoCoupon coupon : coupons) {
      coupon.setCandidate(null);
      coupon.setDateSent(null);
    }

    duolingoCouponRepository.saveAll(coupons);
    duolingoCouponRepository.flush();
  }

  /**
   * Scrubs candidate property values.
   *
   * @param candidateId candidate ID.
   */
  private void scrubCandidateProperties(long candidateId) {
    List<CandidateProperty> properties =
        candidatePropertyRepository.findByCandidate_Id(candidateId);

    for (CandidateProperty property : properties) {
      property.setValue(null);
      property.setRelatedTaskAssignment(null);
    }

    candidatePropertyRepository.saveAll(properties);
    candidatePropertyRepository.flush();
  }

  /**
   * Scrubs candidate notes.
   *
   * <p>The note title is replaced with a placeholder because it may be required by the database.
   * The note type is left unchanged because it is structural metadata.</p>
   *
   * @param candidateId candidate ID.
   */
  private void scrubCandidateNotes(long candidateId) {
    List<CandidateNote> notes = candidateNoteRepository.findByCandidateId(candidateId);

    for (CandidateNote note : notes) {
      note.setTitle("Deleted candidate note");
      note.setComment(null);
    }

    candidateNoteRepository.saveAll(notes);
    candidateNoteRepository.flush();
  }

  /**
   * Scrubs candidate certifications.
   *
   * @param candidateId candidate ID.
   */
  private void scrubCandidateCertifications(long candidateId) {
    List<CandidateCertification> certifications =
        candidateCertificationRepository.findByCandidateId(candidateId);

    for (CandidateCertification certification : certifications) {
      certification.setName(null);
      certification.setInstitution(null);
      certification.setDateCompleted(null);
    }

    candidateCertificationRepository.saveAll(certifications);
    candidateCertificationRepository.flush();
  }

  /**
   * Scrubs citizenship and passport data.
   *
   * @param candidateId candidate ID.
   */
  private void scrubCandidateCitizenships(long candidateId) {
    List<CandidateCitizenship> citizenships =
        candidateCitizenshipRepository.findByCandidate_Id(candidateId);

    for (CandidateCitizenship citizenship : citizenships) {
      citizenship.setNationality(null);
      citizenship.setHasPassport(null);
      citizenship.setPassportExp(null);
      citizenship.setNotes(null);
    }

    candidateCitizenshipRepository.saveAll(citizenships);
    candidateCitizenshipRepository.flush();
  }

  /**
   * Scrubs dependant data.
   *
   * @param candidateId candidate ID.
   */
  private void scrubCandidateDependants(long candidateId) {
    List<CandidateDependant> dependants =
        candidateDependantRepository.findByCandidateId(candidateId);

    for (CandidateDependant dependant : dependants) {
      dependant.setRelation(null);
      dependant.setRelationOther(null);
      dependant.setDob(null);
      dependant.setGender(null);
      dependant.setName(null);
      dependant.setRegistered(null);
      dependant.setRegisteredNumber(null);
      dependant.setRegisteredNotes(null);
      dependant.setHealthConcern(null);
      dependant.setHealthNotes(null);
    }

    candidateDependantRepository.saveAll(dependants);
    candidateDependantRepository.flush();
  }

  /**
   * Scrubs candidate destination data.
   *
   * @param candidateId candidate ID.
   */
  private void scrubCandidateDestinations(long candidateId) {
    List<CandidateDestination> destinations =
        candidateDestinationRepository.findByCandidateId(candidateId);

    for (CandidateDestination destination : destinations) {
      destination.setCountry(null);
      destination.setInterest(null);
      destination.setNotes(null);
    }

    candidateDestinationRepository.saveAll(destinations);
    candidateDestinationRepository.flush();
  }

  /**
   * Scrubs education data.
   *
   * <p>The education country is intentionally not cleared because some schemas have
   * candidate_education.country_id as a required field. Institution, course, major, and completion
   * details are cleared because they can identify the candidate.</p>
   *
   * @param candidateId candidate ID.
   */
  private void scrubCandidateEducations(long candidateId) {
    List<CandidateEducation> educations =
        candidateEducationRepository.findByCandidateId(candidateId);

    for (CandidateEducation education : educations) {
      education.setEducationType(null);
      education.setEducationMajor(null);
      education.setLengthOfCourseYears(null);
      education.setInstitution(null);
      education.setCourseName(null);
      education.setYearCompleted(null);
      education.setIncomplete(null);
    }

    candidateEducationRepository.saveAll(educations);
    candidateEducationRepository.flush();
  }

  /**
   * Scrubs exam data.
   *
   * @param candidateId candidate ID.
   */
  private void scrubCandidateExams(long candidateId) {
    List<CandidateExam> exams = candidateExamRepository.findByCandidateId(candidateId);

    for (CandidateExam exam : exams) {
      exam.setExam(null);
      exam.setOtherExam(null);
      exam.setScore(null);
      exam.setYear(null);
      exam.setNotes(null);
    }

    candidateExamRepository.saveAll(exams);
    candidateExamRepository.flush();
  }

  /**
   * Scrubs job experience data.
   *
   * @param candidateId candidate ID.
   */
  private void scrubCandidateJobExperiences(long candidateId) {
    List<CandidateJobExperience> experiences =
        candidateJobExperienceRepository.findByCandidateId(candidateId);

    for (CandidateJobExperience experience : experiences) {
      experience.setCountry(null);
      experience.setCompanyName(null);
      experience.setRole(null);
      experience.setStartDate(null);
      experience.setEndDate(null);
      experience.setFullTime(null);
      experience.setPaid(null);
      experience.setDescription(null);
    }

    candidateJobExperienceRepository.saveAll(experiences);
    candidateJobExperienceRepository.flush();
  }

  /**
   * Scrubs candidate language rows.
   *
   * @param candidateId candidate ID.
   */
  private void scrubCandidateLanguages(long candidateId) {
    List<CandidateLanguage> languages =
        candidateLanguageRepository.findByCandidateId(candidateId);

    for (CandidateLanguage language : languages) {
      language.setLanguage(null);
      language.setWrittenLevel(null);
      language.setSpokenLevel(null);
    }

    candidateLanguageRepository.saveAll(languages);
    candidateLanguageRepository.flush();
  }

  /**
   * Scrubs candidate skill rows.
   *
   * @param candidateId candidate ID.
   */
  private void scrubCandidateSkills(long candidateId) {
    List<CandidateSkill> skills = candidateSkillRepository.findByCandidateId(candidateId);

    for (CandidateSkill skill : skills) {
      skill.setSkill(null);
      skill.setTimePeriod(null);
    }

    candidateSkillRepository.saveAll(skills);
    candidateSkillRepository.flush();
  }

  /**
   * Scrubs candidate visa check rows and their related visa job check rows.
   *
   * @param candidateId candidate ID.
   */
  private void scrubCandidateVisaChecks(long candidateId) {
    List<CandidateVisaCheck> visaChecks =
        candidateVisaCheckRepository.findByCandidate_Id(candidateId);

    for (CandidateVisaCheck visaCheck : visaChecks) {
      visaCheck.setCountry(null);
      visaCheck.setProtection(null);
      visaCheck.setProtectionGrounds(null);
      visaCheck.setEnglishThreshold(null);
      visaCheck.setEnglishThresholdNotes(null);
      visaCheck.setHealthAssessment(null);
      visaCheck.setHealthAssessmentNotes(null);
      visaCheck.setCharacterAssessment(null);
      visaCheck.setCharacterAssessmentNotes(null);
      visaCheck.setSecurityRisk(null);
      visaCheck.setSecurityRiskNotes(null);
      visaCheck.setOverallRisk(null);
      visaCheck.setOverallRiskNotes(null);
      visaCheck.setValidTravelDocs(null);
      visaCheck.setValidTravelDocsNotes(null);
      visaCheck.setPathwayAssessment(null);
      visaCheck.setPathwayAssessmentNotes(null);
      visaCheck.setDestinationFamily(null);
      visaCheck.setDestinationFamilyLocation(null);

      for (CandidateVisaJobCheck visaJobCheck : visaCheck.getCandidateVisaJobChecks()) {
        scrubCandidateVisaJobCheck(visaJobCheck);
      }
    }

    candidateVisaCheckRepository.saveAll(visaChecks);
    candidateVisaCheckRepository.flush();
  }

  /**
   * Scrubs one candidate visa job check.
   *
   * @param visaJobCheck visa job check to scrub.
   */
  private void scrubCandidateVisaJobCheck(CandidateVisaJobCheck visaJobCheck) {
    visaJobCheck.setJobOpp(null);
    visaJobCheck.setInterest(null);
    visaJobCheck.setInterestNotes(null);
    visaJobCheck.setQualification(null);
    visaJobCheck.setQualificationNotes(null);
    visaJobCheck.setOccupation(null);
    visaJobCheck.setOccupationNotes(null);
    visaJobCheck.setSalaryTsmit(null);
    visaJobCheck.setRegional(null);
    visaJobCheck.setEligible_494(null);
    visaJobCheck.setEligible_494_Notes(null);
    visaJobCheck.setEligible_186(null);
    visaJobCheck.setEligible_186_Notes(null);
    visaJobCheck.setEligibleOther(null);
    visaJobCheck.setEligibleOtherNotes(null);
    visaJobCheck.setPutForward(null);
    visaJobCheck.setTbbEligibility(null);
    visaJobCheck.setNotes(null);
    visaJobCheck.setRelevantWorkExp(null);
    visaJobCheck.setAgeRequirement(null);
    visaJobCheck.setPreferredPathways(null);
    visaJobCheck.setIneligiblePathways(null);
    visaJobCheck.setEligiblePathways(null);
    visaJobCheck.setOccupationCategory(null);
    visaJobCheck.setOccupationSubCategory(null);
    visaJobCheck.setEnglishThreshold(null);
    visaJobCheck.setLanguagesRequired(null);
    visaJobCheck.setLanguagesThresholdMet(null);
    visaJobCheck.setLanguagesThresholdNotes(null);
  }

  /**
   * Scrubs candidate occupation rows.
   *
   * <p>yearsExperience is set to {@code 0} instead of {@code null} because the database does not
   * allow it to be null.</p>
   *
   * @param candidateId candidate ID.
   */
  private void scrubCandidateOccupations(long candidateId) {
    List<CandidateOccupation> occupations =
        candidateOccupationRepository.findByCandidateId(candidateId);

    for (CandidateOccupation occupation : occupations) {
      occupation.setOccupation(null);
      occupation.setYearsExperience(0L);
      occupation.setTopCandidate(false);
    }

    candidateOccupationRepository.saveAll(occupations);
    candidateOccupationRepository.flush();
  }

  /**
   * Scrubs identifying fields from the main candidate row.
   *
   * @param candidate candidate entity.
   */
  private void scrubCandidate(Candidate candidate) {
    candidate.setStatus(CandidateStatus.deleted);

    candidate.setPublicId(null);
    candidate.setAcceptedPrivacyPolicyId(null);
    candidate.setAcceptedPrivacyPolicyDate(null);
    candidate.setAcceptedPrivacyPolicyPartner(null);
    candidate.setAllNotifications(false);
    candidate.setPhone(null);
    candidate.setWhatsapp(null);
    candidate.setGender(null);
    candidate.setDob(null);
    candidate.setAddress1(null);
    candidate.setCity(null);
    candidate.setState(null);
    candidate.setYearOfArrival(null);
    candidate.setAdditionalInfo(null);
    candidate.setCandidateMessage(null);
    candidate.setLinkedInLink(null);
    candidate.setAspirations(null);
    candidate.setMuted(true);
    candidate.setChangePassword(false);
    candidate.setPartnerRef(null);
    candidate.setRegisteredBy(null);
    candidate.setRegoIp(null);
    candidate.setRegoPartnerParam(null);
    candidate.setRegoReferrerParam(null);
    candidate.setRegoUtmCampaign(null);
    candidate.setRegoUtmContent(null);
    candidate.setRegoUtmMedium(null);
    candidate.setRegoUtmSource(null);
    candidate.setRegoUtmTerm(null);
    candidate.setShareableNotes(null);
    candidate.setShareableCv(null);
    candidate.setShareableDoc(null);
    candidate.setSurveyType(null);
    candidate.setSurveyComment(null);
    candidate.setMaxEducationLevel(null);
    candidate.setText(null);

    candidate.setCountry(null);
    candidate.setNationality(null);
    candidate.setBirthCountry(null);

    scrubCandidateFolderAndExternalLinks(candidate);
    scrubCandidateIntakeFields(candidate);
    scrubCandidateAssessmentAndConsentFields(candidate);
  }

  /**
   * Clears folder, Drive, Salesforce, media, and external identifiers.
   *
   * @param candidate candidate entity.
   */
  private void scrubCandidateFolderAndExternalLinks(Candidate candidate) {
    candidate.setFolderlink(null);
    candidate.setFolderlinkAddress(null);
    candidate.setFolderlinkCharacter(null);
    candidate.setFolderlinkEmployer(null);
    candidate.setFolderlinkEngagement(null);
    candidate.setFolderlinkExperience(null);
    candidate.setFolderlinkFamily(null);
    candidate.setFolderlinkIdentity(null);
    candidate.setFolderlinkImmigration(null);
    candidate.setFolderlinkLanguage(null);
    candidate.setFolderlinkMedical(null);
    candidate.setFolderlinkQualification(null);
    candidate.setFolderlinkRegistration(null);
    candidate.setVideolink(null);
    candidate.setExternalId(null);
    candidate.setExternalIdSource(null);
  }

  /**
   * Clears detailed intake, eligibility, immigration, family, and refugee-related fields.
   *
   * @param candidate candidate entity.
   */
  private void scrubCandidateIntakeFields(Candidate candidate) {
    candidate.setReturnedHome(null);
    candidate.setReturnedHomeReason(null);
    candidate.setReturnedHomeReasonNo(null);
    candidate.setVisaIssues(null);
    candidate.setVisaIssuesNotes(null);
    candidate.setAvailDate(null);
    candidate.setAvailImmediate(null);
    candidate.setAvailImmediateJobOps(null);
    candidate.setAvailImmediateReason(null);
    candidate.setAvailImmediateNotes(null);
    candidate.setFamilyMove(null);
    candidate.setFamilyMoveNotes(null);
    candidate.setIntRecruitReasons(null);
    candidate.setIntRecruitOther(null);
    candidate.setIntRecruitRural(null);
    candidate.setIntRecruitRuralNotes(null);
    candidate.setReturnHomeSafe(null);
    candidate.setWorkPermit(null);
    candidate.setWorkPermitDesired(null);
    candidate.setWorkPermitDesiredNotes(null);
    candidate.setWorkDesired(null);
    candidate.setWorkDesiredNotes(null);
    candidate.setHostEntryYear(null);
    candidate.setHostEntryYearNotes(null);

    candidate.setUnhcrStatus(null);
    candidate.setUnhcrConsent(null);
    candidate.setUnhcrNumber(null);
    candidate.setUnhcrFile(null);
    candidate.setUnhcrNotRegStatus(null);
    candidate.setUnhcrNotes(null);
    candidate.setUnrwaRegistered(null);
    candidate.setUnrwaNumber(null);
    candidate.setUnrwaFile(null);
    candidate.setUnrwaNotRegStatus(null);
    candidate.setUnrwaNotes(null);

    candidate.setHomeLocation(null);
    candidate.setAsylumYear(null);
    candidate.setDestLimit(null);
    candidate.setDestLimitNotes(null);
    candidate.setCrimeConvict(null);
    candidate.setCrimeConvictNotes(null);
    candidate.setArrestImprison(null);
    candidate.setArrestImprisonNotes(null);
    candidate.setConflict(null);
    candidate.setConflictNotes(null);
    candidate.setResidenceStatus(null);
    candidate.setResidenceStatusNotes(null);
    candidate.setWorkAbroad(null);
    candidate.setWorkAbroadNotes(null);
    candidate.setHostEntryLegally(null);
    candidate.setHostEntryLegallyNotes(null);
    candidate.setLeftHomeReasons(null);
    candidate.setLeftHomeNotes(null);
    candidate.setReturnHomeFuture(null);
    candidate.setReturnHomeWhen(null);
    candidate.setResettleThird(null);
    candidate.setResettleThirdStatus(null);
    candidate.setHostChallenges(null);

    candidate.setMaritalStatus(null);
    candidate.setMaritalStatusNotes(null);
    candidate.setPartnerRegistered(null);
    candidate.setPartnerCandidate(null);
    candidate.setPartnerEduLevel(null);
    candidate.setPartnerEduLevelNotes(null);
    candidate.setPartnerOccupation(null);
    candidate.setPartnerOccupationNotes(null);
    candidate.setPartnerEnglish(null);
    candidate.setPartnerEnglishLevel(null);
    candidate.setPartnerIelts(null);
    candidate.setPartnerIeltsScore(null);
    candidate.setPartnerIeltsYr(null);
    candidate.setPartnerCitizenship(null);

    candidate.setMilitaryService(null);
    candidate.setMilitaryWanted(null);
    candidate.setMilitaryNotes(null);
    candidate.setMilitaryStart(null);
    candidate.setMilitaryEnd(null);
    candidate.setVisaReject(null);
    candidate.setVisaRejectNotes(null);
    candidate.setCanDrive(null);
    candidate.setDrivingLicense(null);
    candidate.setDrivingLicenseExp(null);
    candidate.setDrivingLicenseCountry(null);
  }

  /**
   * Clears assessment, medical, vaccination, media, consent, and relocation fields.
   *
   * @param candidate candidate entity.
   */
  private void scrubCandidateAssessmentAndConsentFields(Candidate candidate) {
    candidate.setEnglishAssessment(null);
    candidate.setEnglishAssessmentScoreIelts(null);
    candidate.setEnglishAssessmentScoreDet(null);
    candidate.setFrenchAssessment(null);
    candidate.setFrenchAssessmentScoreNclc(null);
    candidate.setIeltsScore(null);
    candidate.setHealthIssues(null);
    candidate.setHealthIssuesNotes(null);
    candidate.setCovidVaccinated(null);
    candidate.setCovidVaccinatedStatus(null);
    candidate.setCovidVaccinatedDate(null);
    candidate.setCovidVaccineName(null);
    candidate.setCovidVaccineNotes(null);
    candidate.setMediaWillingness(null);
    candidate.setContactConsentRegistration(false);
    candidate.setContactConsentPartners(false);
    candidate.setMiniIntakeCompletedBy(null);
    candidate.setMiniIntakeCompletedDate(null);
    candidate.setFullIntakeCompletedBy(null);
    candidate.setFullIntakeCompletedDate(null);
    candidate.setRelocatedAddress(null);
    candidate.setRelocatedCity(null);
    candidate.setRelocatedState(null);
    candidate.setRelocatedCountry(null);
    candidate.setPotentialDuplicate(false);
  }

  /**
   * Scrubs the linked user account so the candidate can no longer log in or be identified through
   * user account fields.
   *
   * @param user linked user account.
   * @param candidateId candidate ID used to generate non-identifying placeholder values.
   */
  private void scrubUser(User user, Long candidateId) {
    if (user == null) {
      return;
    }

    String placeholder = "deleted-candidate-" + candidateId;

    user.setUsername(placeholder);
    user.setFirstName("Deleted");
    user.setLastName("Candidate");
    user.setEmail(placeholder + "@deleted.invalid");

    user.setPasswordEnc("N/A [DELETED]");

    user.setStatus(Status.deleted);
    user.setLastLogin(null);
    user.setResetToken(null);
    user.setResetTokenIssuedDate(null);
    user.setPasswordUpdatedDate(null);
    user.setEmailVerificationToken(null);
    user.setEmailVerificationTokenIssuedDate(null);
    user.setEmailVerified(false);
    user.setUsingMfa(false);
    user.setMfaSecret(null);
    user.setPurpose(null);
    user.setApprover(null);
    user.setReadOnly(true);
    user.setJobCreator(false);
  }
}