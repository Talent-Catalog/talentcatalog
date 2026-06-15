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

package org.tctalent.server.integration.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.integration.helper.BaseDBIntegrationTest;
import org.tctalent.server.integration.helper.TestDataFactory;
import org.tctalent.server.model.db.AbstractCandidateDataDomainObject;
import org.tctalent.server.model.db.AttachmentType;
import org.tctalent.server.model.db.Auditable;
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
import org.tctalent.server.model.db.CandidateReviewStatusItem;
import org.tctalent.server.model.db.CandidateVisaCheck;
import org.tctalent.server.model.db.CandidateVisaJobCheck;
import org.tctalent.server.model.db.CefrLevel;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.DependantRelations;
import org.tctalent.server.model.db.EducationType;
import org.tctalent.server.model.db.Exam;
import org.tctalent.server.model.db.HasPassport;
import org.tctalent.server.model.db.Language;
import org.tctalent.server.model.db.LanguageLevel;
import org.tctalent.server.model.db.NoteType;
import org.tctalent.server.model.db.Occupation;
import org.tctalent.server.model.db.ReviewStatus;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.SavedSearch;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.YesNo;
import org.tctalent.server.model.db.YesNoUnsure;
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
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.CandidateReviewStatusRepository;
import org.tctalent.server.repository.db.CandidateVisaJobRepository;
import org.tctalent.server.repository.db.CandidateVisaRepository;
import org.tctalent.server.repository.db.CountryRepository;
import org.tctalent.server.repository.db.LanguageLevelRepository;
import org.tctalent.server.repository.db.LanguageRepository;
import org.tctalent.server.repository.db.OccupationRepository;
import org.tctalent.server.repository.db.SavedSearchRepository;
import org.tctalent.server.repository.db.UserRepository;
import org.tctalent.server.repository.db.read.cache.CandidateRedisCache;
import org.tctalent.server.security.TcUserDetails;
import org.tctalent.server.service.db.SavedSearchService;
import org.tctalent.server.service.db.UserService;

@SpringBootTest
@Transactional
class CandidateDataAuditingIntegrationTest extends BaseDBIntegrationTest {

    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private CandidateRepository candidateRepository;
    @Autowired private CandidateAttachmentRepository candidateAttachmentRepository;
    @Autowired private CandidateOccupationRepository candidateOccupationRepository;
    @Autowired private CandidateCertificationRepository candidateCertificationRepository;
    @Autowired private CandidateDestinationRepository candidateDestinationRepository;
    @Autowired private CandidateEducationRepository candidateEducationRepository;
    @Autowired private CandidateExamRepository candidateExamRepository;
    @Autowired private CandidateJobExperienceRepository candidateJobExperienceRepository;
    @Autowired private CandidateLanguageRepository candidateLanguageRepository;
    @Autowired private CandidateDependantRepository candidateDependantRepository;
    @Autowired private CandidateNoteRepository candidateNoteRepository;
    @Autowired private CandidateReviewStatusRepository candidateReviewStatusRepository;
    @Autowired private CandidateCitizenshipRepository candidateCitizenshipRepository;
    @Autowired private CandidateVisaRepository candidateVisaRepository;
    @Autowired private CandidateVisaJobRepository candidateVisaJobRepository;
    @Autowired private SavedSearchRepository savedSearchRepository;
    @Autowired private OccupationRepository occupationRepository;
    @Autowired private CountryRepository countryRepository;
    @Autowired private LanguageRepository languageRepository;
    @Autowired private LanguageLevelRepository languageLevelRepository;

    private User systemAdmin;
    @MockitoBean
    private SavedSearchService savedSearchService;
    @MockitoBean
    private CandidateRedisCache candidateRedisCache;
    @BeforeEach
    void setUp() {
        systemAdmin = userService.getSystemAdminUser();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("CandidateOccupation: admin flow sets audit fields on create and update")
    void candidateOccupationAuditsAdminFlow() {
        authenticateAs(systemAdmin);
        Candidate candidate = createCandidateFor(createUser("occ-admin", Role.user));
        Occupation occupation = getAnyOccupation();

        CandidateOccupation candidateOccupation = new CandidateOccupation();
        candidateOccupation.setCandidate(candidate);
        candidateOccupation.setOccupation(occupation);
        candidateOccupation.setYearsExperience(4L);
        candidateOccupation = candidateOccupationRepository.saveAndFlush(candidateOccupation);

        assertAuditFields(candidateOccupation, systemAdmin);
        assertAuditUpdate(candidateOccupation, occupationEntity -> occupationEntity.setYearsExperience(6L));
    }

    @Test
    @DisplayName("CandidateOccupation: candidate user flow sets audit fields on create and update")
    void candidateOccupationAuditsCandidateFlow() {
        User candidateUser = createUser("occ-candidate", Role.user);
        authenticateAs(candidateUser);
        Candidate candidate = createCandidateFor(candidateUser);
        Occupation occupation = getAnyOccupation();

        CandidateOccupation candidateOccupation = new CandidateOccupation();
        candidateOccupation.setCandidate(candidate);
        candidateOccupation.setOccupation(occupation);
        candidateOccupation.setYearsExperience(2L);
        candidateOccupation = candidateOccupationRepository.saveAndFlush(candidateOccupation);

        assertAuditFields(candidateOccupation, candidateUser);
        assertAuditUpdate(candidateOccupation, occupationEntity -> occupationEntity.setYearsExperience(3L));
    }

    @Test
    @DisplayName("CandidateAttachment: admin flow sets audit fields on create and update")
    void candidateAttachmentAuditsAdminFlow() {
        authenticateAs(systemAdmin);
        Candidate candidate = createCandidateFor(createUser("att-admin", Role.user));

        CandidateAttachment attachment = new CandidateAttachment();
        attachment.setCandidate(candidate);
        attachment.setType(AttachmentType.link);
        attachment.setName("portfolio");
        attachment.setUrl("https://example.com/portfolio");
        attachment = candidateAttachmentRepository.saveAndFlush(attachment);

        assertAuditFields(attachment, systemAdmin);
        assertAuditUpdate(attachment, attachmentEntity -> attachmentEntity.setName("portfolio-updated"));
    }

    @Test
    @DisplayName("CandidateAttachment: candidate user flow sets audit fields on create and update")
    void candidateAttachmentAuditsCandidateFlow() {
        User candidateUser = createUser("att-candidate", Role.user);
        authenticateAs(candidateUser);
        Candidate candidate = createCandidateFor(candidateUser);

        CandidateAttachment attachment = new CandidateAttachment();
        attachment.setCandidate(candidate);
        attachment.setType(AttachmentType.link);
        attachment.setName("profile-link");
        attachment.setUrl("https://example.com/profile");
        attachment = candidateAttachmentRepository.saveAndFlush(attachment);

        assertAuditFields(attachment, candidateUser);
        assertAuditUpdate(attachment, attachmentEntity -> attachmentEntity.setName("profile-link-updated"));
    }

    @Test
    @DisplayName("CandidateCertification: sets audit fields on create and update")
    void candidateCertificationAudits() {
        authenticateAs(systemAdmin);
        Candidate candidate = createCandidateFor(createUser("cert", Role.user));

        CandidateCertification certification = new CandidateCertification();
        certification.setCandidate(candidate);
        certification.setName("Nursing Certificate");
        certification.setInstitution("Institution");
        certification.setDateCompleted(LocalDate.now().minusYears(1));
        certification = candidateCertificationRepository.saveAndFlush(certification);

        assertAuditFields(certification, systemAdmin);
        assertAuditUpdate(certification, entity -> entity.setInstitution("Updated Institution"));
    }

    @Test
    @DisplayName("CandidateDestination: sets audit fields on create and update")
    void candidateDestinationAudits() {
        authenticateAs(systemAdmin);
        Candidate candidate = createCandidateFor(createUser("dest", Role.user));

        CandidateDestination destination = new CandidateDestination();
        destination.setCandidate(candidate);
        destination.setCountry(getAnyCountry());
        destination.setInterest(YesNoUnsure.Yes);
        destination.setNotes("Initial notes");
        destination = candidateDestinationRepository.saveAndFlush(destination);

        assertAuditFields(destination, systemAdmin);
        assertAuditUpdate(destination, entity -> entity.setNotes("Updated notes"));
    }

    @Test
    @DisplayName("CandidateEducation: sets audit fields on create and update")
    void candidateEducationAudits() {
        authenticateAs(systemAdmin);
        Candidate candidate = createCandidateFor(createUser("edu", Role.user));

        CandidateEducation education = new CandidateEducation();
        education.setCandidate(candidate);
        education.setCountry(getAnyCountry());
        education.setEducationType(EducationType.Bachelor);
        education.setInstitution("University");
        education.setCourseName("Computer Science");
        education.setYearCompleted(2022);
        education = candidateEducationRepository.saveAndFlush(education);

        assertAuditFields(education, systemAdmin);
        assertAuditUpdate(education, entity -> entity.setInstitution("Updated University"));
    }

    @Test
    @DisplayName("CandidateExam: sets audit fields on create and update")
    void candidateExamAudits() {
        authenticateAs(systemAdmin);
        Candidate candidate = createCandidateFor(createUser("exam", Role.user));

        CandidateExam exam = new CandidateExam();
        exam.setCandidate(candidate);
        exam.setExam(Exam.IELTSAca);
        exam.setScore("7.5");
        exam.setYear(2023L);
        exam = candidateExamRepository.saveAndFlush(exam);

        assertAuditFields(exam, systemAdmin);
        assertAuditUpdate(exam, entity -> entity.setScore("8.0"));
    }

    @Test
    @DisplayName("CandidateJobExperience: sets audit fields on create and update")
    void candidateJobExperienceAudits() {
        authenticateAs(systemAdmin);
        Candidate candidate = createCandidateFor(createUser("job-exp", Role.user));
        CandidateOccupation occupation = new CandidateOccupation();
        occupation.setCandidate(candidate);
        occupation.setOccupation(getAnyOccupation());
        occupation.setYearsExperience(5L);
        occupation = candidateOccupationRepository.saveAndFlush(occupation);

        CandidateJobExperience experience = new CandidateJobExperience();
        experience.setCandidate(candidate);
        experience.setCandidateOccupation(occupation);
        experience.setCountry(getAnyCountry());
        experience.setCompanyName("Clinic");
        experience.setRole("Nurse");
        experience.setStartDate(LocalDate.now().minusYears(2));
        experience.setEndDate(LocalDate.now().minusMonths(1));
        experience = candidateJobExperienceRepository.saveAndFlush(experience);

        assertAuditFields(experience, systemAdmin);
        assertAuditUpdate(experience, entity -> entity.setCompanyName("Updated Clinic"));
    }

    @Test
    @DisplayName("CandidateLanguage: sets audit fields on create and update")
    void candidateLanguageAudits() {
        authenticateAs(systemAdmin);
        Candidate candidate = createCandidateFor(createUser("language", Role.user));
        Language language = getAnyLanguage();
        LanguageLevel level = getAnyLanguageLevel();

        CandidateLanguage candidateLanguage = new CandidateLanguage();
        candidateLanguage.setCandidate(candidate);
        candidateLanguage.setLanguage(language);
        candidateLanguage.setSpokenLevel(level);
        candidateLanguage.setWrittenLevel(level);
        candidateLanguage = candidateLanguageRepository.saveAndFlush(candidateLanguage);

        assertAuditFields(candidateLanguage, systemAdmin);
        assertAuditUpdate(candidateLanguage, entity -> entity.setWrittenLevel(getAnyLanguageLevel()));
    }

    @Test
    @DisplayName("CandidateDependant: sets audit fields on create and update")
    void candidateDependantAudits() {
        authenticateAs(systemAdmin);
        Candidate candidate = createCandidateFor(createUser("dependant", Role.user));

        CandidateDependant dependant = new CandidateDependant();
        dependant.setCandidate(candidate);
        dependant.setName("Jane");
        dependant.setRelation(DependantRelations.Child);
        dependant.setDob(LocalDate.now().minusYears(8));
        dependant = candidateDependantRepository.saveAndFlush(dependant);

        assertAuditFields(dependant, systemAdmin);
        assertAuditUpdate(dependant, entity -> entity.setName("Jane Updated"));
    }

    @Test
    @DisplayName("CandidateNote: admin flow sets audit fields on create and update")
    void candidateNoteAuditsAdminFlow() {
        authenticateAs(systemAdmin);
        Candidate candidate = createCandidateFor(createUser("note-admin", Role.user));

        CandidateNote note = new CandidateNote();
        note.setCandidate(candidate);
        note.setNoteType(NoteType.admin);
        note.setTitle("Initial title");
        note.setComment("Initial comment");
        note = candidateNoteRepository.saveAndFlush(note);

        assertAuditFields(note, systemAdmin);
        assertAuditUpdate(note, entity -> entity.setComment("Updated comment"));
    }

    @Test
    @DisplayName("CandidateNote: candidate user flow sets audit fields on create and update")
    void candidateNoteAuditsCandidateFlow() {
        User candidateUser = createUser("note-candidate", Role.user);
        authenticateAs(candidateUser);
        Candidate candidate = createCandidateFor(candidateUser);

        CandidateNote note = new CandidateNote();
        note.setCandidate(candidate);
        note.setNoteType(NoteType.candidate);
        note.setTitle("Candidate title");
        note.setComment("Candidate comment");
        note = candidateNoteRepository.saveAndFlush(note);

        assertAuditFields(note, candidateUser);
        assertAuditUpdate(note, entity -> entity.setTitle("Candidate title updated"));
    }

    @Test
    @DisplayName("CandidateReviewStatusItem: sets audit fields on create and update")
    void candidateReviewStatusItemAudits() {
        authenticateAs(systemAdmin);
        Candidate candidate = createCandidateFor(createUser("review-status", Role.user));
        SavedSearch savedSearch = getOrCreateSavedSearch();

        CandidateReviewStatusItem item = new CandidateReviewStatusItem();
        item.setCandidate(candidate);
        item.setSavedSearch(savedSearch);
        item.setReviewStatus(ReviewStatus.verified);
        item.setComment("Initial review comment");
        item = candidateReviewStatusRepository.saveAndFlush(item);

        assertAuditFields(item, systemAdmin);
        assertAuditUpdate(item, entity -> entity.setComment("Updated review comment"));
    }

    @Test
    @DisplayName("CandidateVisaCheck: sets audit fields on create and update")
    void candidateVisaCheckAudits() {
        authenticateAs(systemAdmin);
        Candidate candidate = createCandidateFor(createUser("visa-check", Role.user));

        CandidateVisaCheck visaCheck = new CandidateVisaCheck();
        visaCheck.setCandidate(candidate);
        visaCheck.setCountry(getAnyCountry());
        visaCheck.setProtection(YesNo.Yes);
        visaCheck = candidateVisaRepository.saveAndFlush(visaCheck);

        assertAuditFields(visaCheck, systemAdmin);
        assertAuditUpdate(visaCheck, entity -> entity.setProtection(YesNo.No));
    }

    @Test
    @DisplayName("CandidateCitizenship: sets audit fields on create and update")
    void candidateCitizenshipAudits() {
        authenticateAs(systemAdmin);
        Candidate candidate = createCandidateFor(createUser("citizenship", Role.user));

        CandidateCitizenship citizenship = new CandidateCitizenship();
        citizenship.setCandidate(candidate);
        citizenship.setNationality(getAnyCountry());
        citizenship.setHasPassport(HasPassport.ValidPassport);
        citizenship.setNotes("Initial citizenship notes");
        citizenship = candidateCitizenshipRepository.saveAndFlush(citizenship);

        assertAuditFields(citizenship, systemAdmin);
        assertAuditUpdate(citizenship, entity -> entity.setNotes("Updated citizenship notes"));
    }

    @Test
    @DisplayName("CandidateVisaJobCheck: sets audit fields on create and update")
    void candidateVisaJobCheckAudits() {
        authenticateAs(systemAdmin);
        Candidate candidate = createCandidateFor(createUser("visa-job-check", Role.user));

        CandidateVisaCheck visaCheck = new CandidateVisaCheck();
        visaCheck.setCandidate(candidate);
        visaCheck.setCountry(getAnyCountry());
        visaCheck.setProtection(YesNo.Yes);
        visaCheck = candidateVisaRepository.saveAndFlush(visaCheck);

        CandidateVisaJobCheck visaJobCheck = new CandidateVisaJobCheck();
        visaJobCheck.setCandidateVisaCheck(visaCheck);
        visaJobCheck.setInterest(YesNo.Yes);
        visaJobCheck = candidateVisaJobRepository.saveAndFlush(visaJobCheck);

        assertAuditFields(visaJobCheck, systemAdmin);
        assertAuditUpdate(visaJobCheck, entity -> entity.setInterest(YesNo.No));
    }

    private void assertAuditFields(Auditable auditable, User expectedUser) {
        assertNotNull(auditable.getCreatedBy());
        assertNotNull(auditable.getCreatedDate());
        assertNotNull(auditable.getUpdatedBy());
        assertNotNull(auditable.getUpdatedDate());
        assertEquals(expectedUser.getId(), auditable.getCreatedBy().getId());
        assertEquals(expectedUser.getId(), auditable.getUpdatedBy().getId());
    }

    private <T extends AbstractCandidateDataDomainObject<Long>> void assertAuditUpdate(
        T entity,
        java.util.function.Consumer<T> mutator
    ) {
        OffsetDateTime createdDate = entity.getCreatedDate();
        Long createdById = entity.getCreatedBy().getId();
        OffsetDateTime previousUpdatedDate = entity.getUpdatedDate();
        mutator.accept(entity);
        entity = saveByType(entity);
        assertEquals(createdDate, entity.getCreatedDate());
        assertEquals(createdById, entity.getCreatedBy().getId());
        assertNotNull(entity.getUpdatedDate());
        org.junit.jupiter.api.Assertions.assertFalse(entity.getUpdatedDate().isBefore(previousUpdatedDate));
    }

    @SuppressWarnings("unchecked")
    private <T extends AbstractCandidateDataDomainObject<Long>> T saveByType(T entity) {
        if (entity instanceof CandidateAttachment attachment) {
            return (T) candidateAttachmentRepository.saveAndFlush(attachment);
        }
        if (entity instanceof CandidateOccupation occupation) {
            return (T) candidateOccupationRepository.saveAndFlush(occupation);
        }
        if (entity instanceof CandidateCertification certification) {
            return (T) candidateCertificationRepository.saveAndFlush(certification);
        }
        if (entity instanceof CandidateDestination destination) {
            return (T) candidateDestinationRepository.saveAndFlush(destination);
        }
        if (entity instanceof CandidateEducation education) {
            return (T) candidateEducationRepository.saveAndFlush(education);
        }
        if (entity instanceof CandidateExam exam) {
            return (T) candidateExamRepository.saveAndFlush(exam);
        }
        if (entity instanceof CandidateJobExperience experience) {
            return (T) candidateJobExperienceRepository.saveAndFlush(experience);
        }
        if (entity instanceof CandidateLanguage language) {
            return (T) candidateLanguageRepository.saveAndFlush(language);
        }
        if (entity instanceof CandidateDependant dependant) {
            return (T) candidateDependantRepository.saveAndFlush(dependant);
        }
        if (entity instanceof CandidateNote note) {
            return (T) candidateNoteRepository.saveAndFlush(note);
        }
        if (entity instanceof CandidateReviewStatusItem reviewStatusItem) {
            return (T) candidateReviewStatusRepository.saveAndFlush(reviewStatusItem);
        }
        if (entity instanceof CandidateCitizenship citizenship) {
            return (T) candidateCitizenshipRepository.saveAndFlush(citizenship);
        }
        if (entity instanceof CandidateVisaCheck visaCheck) {
            return (T) candidateVisaRepository.saveAndFlush(visaCheck);
        }
        if (entity instanceof CandidateVisaJobCheck visaJobCheck) {
            return (T) candidateVisaJobRepository.saveAndFlush(visaJobCheck);
        }
        throw new IllegalArgumentException("Unsupported entity type: " + entity.getClass().getName());
    }

    private Candidate createCandidateFor(User user) {
        Candidate candidate = TestDataFactory.createCandidate();
        candidate.setUser(user);
        candidate.setCreatedBy(null);
        candidate.setCreatedDate(null);
        candidate.setUpdatedBy(null);
        candidate.setUpdatedDate(null);
        return candidateRepository.saveAndFlush(candidate);
    }

    private User createUser(String prefix, Role role) {
        User user = new User();
        String token = prefix + "-" + System.nanoTime();
        user.setUsername(token);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail(token + "@example.com");
        user.setRole(role);
        user.setStatus(Status.active);
        user.setPasswordEnc("encoded");
        user.setUsingMfa(false);
        user.setPartner(systemAdmin.getPartner());
        user.setCreatedBy(systemAdmin);
        user.setCreatedDate(OffsetDateTime.now().minusDays(1));
        return userRepository.saveAndFlush(user);
    }

    private SavedSearch getOrCreateSavedSearch() {
        return savedSearchRepository.findAll().stream().findFirst().orElseGet(() -> {
            SavedSearch savedSearch = new SavedSearch();
            String token = "saved-search-" + System.nanoTime();
            savedSearch.setName(token);
            savedSearch.setType("candidate-search");
            savedSearch.setStatus(Status.active);
            savedSearch.setDefaultSearch(false);
            savedSearch.setCreatedBy(systemAdmin);
            savedSearch.setCreatedDate(OffsetDateTime.now().minusMinutes(1));
            return savedSearchRepository.saveAndFlush(savedSearch);
        });
    }

    private void authenticateAs(User user) {
        TcUserDetails userDetails = new TcUserDetails(user);
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(userDetails, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private Occupation getAnyOccupation() {
        return occupationRepository.findAll().stream()
            .findFirst()
            .orElseGet(() -> occupationRepository.saveAndFlush(
                new Occupation("Test Occupation", Status.active)
            ));
    }

    private Country getAnyCountry() {
        return countryRepository.findAll().stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No country records available for test"));
    }

    private Language getAnyLanguage() {
        return languageRepository.findAll().stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No language records available for test"));
    }

    private LanguageLevel getAnyLanguageLevel() {
        return languageLevelRepository.findAll().stream()
            .findFirst()
            .orElseGet(() -> languageLevelRepository.saveAndFlush(
                new LanguageLevel("Test Language Level", Status.active, 1, CefrLevel.A1)
            ));
    }
}
