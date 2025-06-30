/*
 * Copyright (c) 2025 Talent Catalog.
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

package org.tctalent.server.integration.helper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.Nullable;
import org.tctalent.server.model.db.*;
import org.tctalent.server.model.db.task.UploadType;
import org.tctalent.server.repository.db.*;
import org.tctalent.server.service.db.audit.AuditAction;
import org.tctalent.server.service.db.audit.AuditType;

/**
 * Utility class for creating and persisting test data entities for integration testing.
 */
public class TestDataFactory {

  /**
   * Creates a new TaskImpl instance with specified or default values.
   *
   * @param name        The task name (defaults to "DEFAULT" if null).
   * @param displayName The task display name (defaults to "DEFAULT DISPLAY" if null).
   * @return A configured TaskImpl instance.
   */
  public static TaskImpl createTask(@Nullable String name, @Nullable String displayName) {
    TaskImpl task = new TaskImpl();
    task.setName(name != null ? name : "DEFAULT");
    task.setDisplayName(displayName != null ? displayName : "DEFAULT DISPLAY");
    task.setCreatedBy(createSystemUser());
    task.setCreatedDate(OffsetDateTime.now());
    return task;
  }

  /**
   * Creates and persists a TaskImpl instance to the provided repository.
   *
   * @param repository The TaskRepository to save the task.
   * @return The persisted TaskImpl instance.
   */
  public static TaskImpl createAndSaveTask(TaskRepository repository) {
    return saveEntity(repository, createTask(null, null));
  }

  /**
   * Creates a new TaskAssignmentImpl instance for the specified user.
   *
   * @param user The user assigned to the task.
   * @return A configured TaskAssignmentImpl instance.
   */
  public static TaskAssignmentImpl createTaskAssignment(User user) {
    TaskAssignmentImpl taskAssignment = new TaskAssignmentImpl();
    taskAssignment.setActivatedBy(user);
    taskAssignment.setActivatedDate(OffsetDateTime.now());
    taskAssignment.setStatus(Status.active);
    Candidate candidate = new Candidate();
    candidate.setId(99999999L);
    taskAssignment.setCandidate(candidate);
    return taskAssignment;
  }

  /**
   * Creates and persists a User instance to the provided repository.
   *
   * @param repository The UserRepository to save the user.
   * @return The persisted User instance.
   */
  public static User createAndSaveUser(UserRepository repository) {
    return repository.save(createUser(null));
  }

  /**
   * Creates and persists a Candidate instance with the specified user.
   *
   * @param repository The CandidateRepository to save the candidate.
   * @param user       The User associated with the candidate.
   * @return The persisted Candidate instance.
   */
  public static Candidate createAndSaveCandidate(CandidateRepository repository, User user) {
    Candidate candidate = createCandidate();
    candidate.setUser(user);
    return saveEntity(repository, candidate);
  }

  /**
   * Creates and persists a CandidateCertification instance.
   *
   * @param repository The CandidateCertificationRepository to save the certification.
   * @return The persisted CandidateCertification instance.
   */
  public static CandidateCertification createAndSaveCandidateCertification(
      CandidateCertificationRepository repository) {
    return saveEntity(repository, createCandidateCertification());
  }

  /**
   * Creates a new CandidateCertification instance with default values.
   *
   * @return A configured CandidateCertification instance.
   */
  public static CandidateCertification createCandidateCertification() {
    CandidateCertification certification = new CandidateCertification();
    certification.setName("GREAT CERT");
    return certification;
  }

  /**
   * Creates a new Candidate instance with randomized test data.
   *
   * @return A configured Candidate instance.
   */
  public static Candidate createCandidate() {
    Candidate candidate = new Candidate();
    candidate.setCandidateNumber(String.format("TEMP%04d", new Random().nextInt(10000)));
    candidate.setPhone(String.format("999999999%04d", new Random().nextInt(10000)));
    candidate.setContactConsentPartners(true);
    candidate.setContactConsentRegistration(true);
    candidate.setWorkAbroadNotes(String.format("GOOD FOR TEST%04d", new Random().nextInt(10000)));
    candidate.setWhatsapp(String.format("WHATSAPP%04d", new Random().nextInt(10000)));
    candidate.setStatus(CandidateStatus.active);
    candidate.setUnhcrStatus(UnhcrStatus.Unsure);
    candidate.setGender(Gender.male);
    candidate.setLinkedInLink("LINKEDIN");
    candidate.setDob(OffsetDateTime.now().minusYears(28).toLocalDate());
    candidate.setCreatedBy(createSystemUser());
    candidate.setCreatedDate(OffsetDateTime.now().minusDays(5));
    return candidate;
  }

  /**
   * Creates and persists a SavedList instance.
   *
   * @param repository The SavedListRepository to save the list.
   * @return The persisted SavedList instance.
   */
  public static SavedList createAndSaveSavedList(SavedListRepository repository) {
    return saveEntity(repository, createSavedList());
  }

  /**
   * Creates a new SavedList instance with default values.
   *
   * @return A configured SavedList instance.
   */
  public static SavedList createSavedList() {
    SavedList savedList = new SavedList();
    savedList.setDescription("SavedList");
    savedList.setName("SavedList");
    savedList.setGlobal(true);
    savedList.setRegisteredJob(true);
    savedList.setFixed(true);
    savedList.setCreatedBy(createSystemUser());
    savedList.setCreatedDate(OffsetDateTime.now());
    return savedList;
  }

  /**
   * Creates a new CandidateEducation instance with default values.
   *
   * @return A configured CandidateEducation instance.
   */
  public static CandidateEducation createCandidateEducation() {
    CandidateEducation education = new CandidateEducation();
    education.setLengthOfCourseYears(3);
    education.setInstitution("TEST INSTITUTION");
    Country country = new Country();
    country.setId(6192L); // Australia
    education.setCountry(country);
    education.setEducationType(EducationType.Masters);
    return education;
  }

  /**
   * Creates and persists a Reaction instance.
   *
   * @param repository The ReactionRepository to save the reaction.
   * @return The persisted Reaction instance.
   */
  public static Reaction createAndSaveReaction(ReactionRepository repository) {
    return saveEntity(repository, createReaction());
  }

  /**
   * Creates a new Reaction instance with default values.
   *
   * @return A configured Reaction instance.
   */
  public static Reaction createReaction() {
    Reaction reaction = new Reaction();
    reaction.setEmoji("Smile");
    return reaction;
  }

  /**
   * Creates a new JobChat instance with default values.
   *
   * @return A configured JobChat instance.
   */
  public static JobChat createJobChat() {
    JobChat jobChat = new JobChat();
    jobChat.setType(JobChatType.JobCreatorAllSourcePartners);
    jobChat.setCreatedBy(createSystemUser());
    jobChat.setCreatedDate(OffsetDateTime.now());
    return jobChat;
  }

  /**
   * Creates and persists a ChatPost instance.
   *
   * @param repository The ChatPostRepository to save the post.
   * @return The persisted ChatPost instance.
   */
  public static ChatPost createAndSaveChatPost(ChatPostRepository repository) {
    return saveEntity(repository, createChatPost());
  }

  /**
   * Creates a new ChatPost instance with default values.
   *
   * @return A configured ChatPost instance.
   */
  public static ChatPost createChatPost() {
    ChatPost chatPost = new ChatPost();
    chatPost.setContent("NothingChatContent");
    chatPost.setCreatedBy(createSystemUser());
    chatPost.setCreatedDate(OffsetDateTime.now());
    return chatPost;
  }

  /**
   * Creates and persists a SurveyType instance.
   *
   * @param repository The SurveyTypeRepository to save the survey type.
   * @return The persisted SurveyType instance.
   */
  public static SurveyType createAndSaveSurveyType(SurveyTypeRepository repository) {
    return saveEntity(repository, createSurveyType());
  }

  /**
   * Creates a new SurveyType instance with default values.
   *
   * @return A configured SurveyType instance.
   */
  public static SurveyType createSurveyType() {
    SurveyType surveyType = new SurveyType();
    surveyType.setName("IntTestSurvey");
    surveyType.setStatus(Status.active);
    return surveyType;
  }

  /**
   * Creates and persists a SystemLanguage instance.
   *
   * @param repository The SystemLanguageRepository to save the language.
   * @return The persisted SystemLanguage instance.
   */
  public static SystemLanguage createAndSaveSystemLanguage(SystemLanguageRepository repository) {
    return saveEntity(repository, createSystemLanguage());
  }

  /**
   * Creates a new SystemLanguage instance with default values.
   *
   * @return A configured SystemLanguage instance.
   */
  public static SystemLanguage createSystemLanguage() {
    SystemLanguage language = new SystemLanguage();
    language.setLanguage("en");
    language.setLabel("English");
    language.setStatus(Status.active);
    return language;
  }

  /**
   * Persists an entity to the specified repository and flushes the changes.
   *
   * @param repository The JpaRepository to save the entity.
   * @param entity     The entity to persist.
   * @param <T>        The type of the entity.
   * @param <ID>       The type of the entity's ID.
   * @return The persisted entity.
   */
  private static <T, ID> T saveEntity(JpaRepository<T, ID> repository, T entity) {
    return repository.saveAndFlush(entity);
  }

  /**
   * Creates a new User instance with default or specified ID.
   *
   * @param id The optional ID for the user (null for no specific ID).
   * @return A configured User instance.
   */
  public static User createUser(@Nullable Long id) {
    User user = new User();
    user.setId(id);
    user.setUsername("JO BLOGS");
    user.setEmail("JO.BLOgs@email.com");
    user.setFirstName("jo");
    user.setLastName("blogs");
    user.setRole(Role.user);
    user.setStatus(Status.active);
    user.setUsingMfa(false);
    PartnerImpl partner = new PartnerImpl();
    partner.setId(1L); // TBB partner
    user.setPartner(partner);
    user.setCreatedDate(OffsetDateTime.now().minusYears(1));
    return user;
  }

  /**
   * Creates a new AuditLog instance with the specified object reference.
   *
   * @param objectReference The reference to the audited object.
   * @return A configured AuditLog instance.
   */
  public static AuditLog createAuditLog(String objectReference) {
    AuditLog auditLog = new AuditLog();
    auditLog.setType(AuditType.CANDIDATE_OCCUPATION);
    auditLog.setUserId(9L);
    auditLog.setObjectRef(objectReference);
    auditLog.setEventDate(OffsetDateTime.now());
    auditLog.setAction(AuditAction.ADD);
    auditLog.setDescription("Create a test audit record.");
    return auditLog;
  }

  /**
   * Creates a new CandidateDependant instance with randomized name and default gender.
   *
   * @return A configured CandidateDependant instance.
   */
  public static CandidateDependant createCandidateDependant() {
    CandidateDependant dependant = new CandidateDependant();
    dependant.setName(String.format("James%04d", new Random().nextInt(10000)));
    dependant.setGender(Gender.male);
    return dependant;
  }

  /**
   * Creates a new SalesforceJobOpp instance with default values.
   *
   * @return A configured SalesforceJobOpp instance.
   */
  public static SalesforceJobOpp createSalesforceJobOpportunity() {
    SalesforceJobOpp jobOpp = new SalesforceJobOpp();
    jobOpp.setDescription("SF TEST JOB");
    jobOpp.setName("SF test JOB");
    jobOpp.setEmployer("Seraco Pty Ltd");
    Country country = new Country();
    country.setId(6192L); // Australia
    jobOpp.setCountry(country);
    jobOpp.setSfId("TESTSFID");
    jobOpp.setCreatedBy(createSystemUser());
    jobOpp.setCreatedDate(OffsetDateTime.now());
    return jobOpp;
  }

  /**
   * Creates and persists a JobChat instance.
   *
   * @param repository The JobChatRepository to save the job chat.
   * @return The persisted JobChat instance.
   */
  public static JobChat createAndSaveJobChat(JobChatRepository repository) {
    return saveEntity(repository, createJobChat());
  }

  /**
   * Creates and persists a JobChatUser instance with the specified user and job chat.
   *
   * @param repository The JobChatUserRepository to save the job chat user.
   * @param user       The associated User.
   * @param jobChat    The associated JobChat.
   * @return The persisted JobChatUser instance.
   */
  public static JobChatUser createAndSaveJobChatUser(JobChatUserRepository repository, User user, JobChat jobChat) {
    return saveEntity(repository, createJobChatUser(user, jobChat));
  }

  /**
   * Creates a new JobChatUser instance with the specified user and job chat.
   *
   * @param user    The associated User.
   * @param jobChat The associated JobChat.
   * @return A configured JobChatUser instance.
   */
  public static JobChatUser createJobChatUser(User user, JobChat jobChat) {
    JobChatUserKey key = createJobChatUserKey(user, jobChat);
    JobChatUser jobChatUser = new JobChatUser();
    jobChatUser.setId(key);
    jobChatUser.setChat(jobChat);
    jobChatUser.setUser(user);
    return jobChatUser;
  }

  /**
   * Creates a new JobChatUserKey instance with the specified user and job chat IDs.
   *
   * @param user    The associated User.
   * @param jobChat The associated JobChat.
   * @return A configured JobChatUserKey instance.
   */
  public static JobChatUserKey createJobChatUserKey(User user, JobChat jobChat) {
    JobChatUserKey key = new JobChatUserKey();
    key.setUserId(user.getId());
    key.setJobChatId(jobChat.getId());
    return key;
  }

  /**
   * Creates and persists a SalesforceJobOpp instance.
   *
   * @param repository The SalesforceJobOppRepository to save the job opportunity.
   * @return The persisted SalesforceJobOpp instance.
   */
  public static SalesforceJobOpp createAndSaveSalesforceJobOpportunity(SalesforceJobOppRepository repository) {
    return saveEntity(repository, createSalesforceJobOpportunity());
  }

  /**
   * Creates and persists a PartnerImpl instance.
   *
   * @param repository The PartnerRepository to save the partner.
   * @return The persisted PartnerImpl instance.
   */
  public static PartnerImpl createAndSavePartner(PartnerRepository repository) {
    return saveEntity(repository, createPartner());
  }

  /**
   * Creates a new PartnerImpl instance with default values.
   *
   * @return A configured PartnerImpl instance.
   */
  public static PartnerImpl createPartner() {
    PartnerImpl partner = new PartnerImpl();
    partner.setName("GREAT TEST PARTNER");
    partner.setStatus(Status.active);
    partner.setAbbreviation("GTP");
    partner.setJobCreator(true);
    partner.setDefaultSourcePartner(false);
    partner.setAutoAssignable(true);
    return partner;
  }

  /**
   * Creates and persists a Country instance.
   *
   * @param repository The CountryRepository to save the country.
   * @return The persisted Country instance.
   */
  public static Country createAndSaveCountry(CountryRepository repository) {
    return saveEntity(repository, createCountry());
  }

  /**
   * Creates a new Country instance with default values.
   *
   * @return A configured Country instance.
   */
  public static Country createCountry() {
    Country country = new Country();
    country.setIsoCode("ISOCODE");
    country.setName("NewAustralia");
    country.setStatus(Status.active);
    return country;
  }

  /**
   * Creates a new CandidateVisaCheck instance with default values.
   *
   * @return A configured CandidateVisaCheck instance.
   */
  public static CandidateVisaCheck createCandidateVisaCheck() {
    CandidateVisaCheck visaCheck = new CandidateVisaCheck();
    visaCheck.setProtection(YesNo.Yes);
    return visaCheck;
  }

  /**
   * Creates and persists an Industry instance.
   *
   * @param repository The IndustryRepository to save the industry.
   * @return The persisted Industry instance.
   */
  public static Industry createAndSaveIndustry(IndustryRepository repository) {
    return saveEntity(repository, createIndustry());
  }

  /**
   * Creates a new Industry instance with default values.
   *
   * @return A configured Industry instance.
   */
  public static Industry createIndustry() {
    Industry industry = new Industry();
    industry.setStatus(Status.active);
    industry.setName("TestIndustry");
    return industry;
  }

  /**
   * Creates a new CandidateVisaJobCheck instance with default values.
   *
   * @return A configured CandidateVisaJobCheck instance.
   */
  public static CandidateVisaJobCheck createCandidateVisaJobCheck() {
    CandidateVisaJobCheck visaJobCheck = new CandidateVisaJobCheck();
    visaJobCheck.setInterest(YesNo.Yes);
    return visaJobCheck;
  }

  /**
   * Creates a new CandidateReviewStatusItem instance with default values.
   *
   * @return A configured CandidateReviewStatusItem instance.
   */
  public static CandidateReviewStatusItem createCandidateReviewStatusItem() {
    CandidateReviewStatusItem reviewStatusItem = new CandidateReviewStatusItem();
    reviewStatusItem.setComment("TestCandidateReviewStatusItem");
    reviewStatusItem.setReviewStatus(ReviewStatus.verified);
    return reviewStatusItem;
  }

  /**
   * Creates and persists a SavedSearch instance.
   *
   * @param repository The SavedSearchRepository to save the search.
   * @return The persisted SavedSearch instance.
   */
  public static SavedSearch createAndSaveSavedSearch(SavedSearchRepository repository) {
    return saveEntity(repository, createSavedSearch());
  }

  /**
   * Creates a new SavedSearch instance with default values.
   *
   * @return A configured SavedSearch instance.
   */
  public static SavedSearch createSavedSearch() {
    SavedSearch savedSearch = new SavedSearch();
    savedSearch.setType("TestSavedSearch");
    savedSearch.setStatus(Status.active);
    savedSearch.setName("TestSavedSearch");
    savedSearch.setDefaultSearch(true);
    savedSearch.setCreatedBy(createSystemUser());
    savedSearch.setCreatedDate(OffsetDateTime.now());
    return savedSearch;
  }

  /**
   * Creates and persists a CandidateOpportunity instance.
   *
   * @param repository The CandidateOpportunityRepository to save the opportunity.
   * @return The persisted CandidateOpportunity instance.
   */
  public static CandidateOpportunity createAndSaveCandidateOpportunity(CandidateOpportunityRepository repository) {
    return saveEntity(repository, createCandidateOpportunity());
  }

  /**
   * Creates a new CandidateOpportunity instance with default values.
   *
   * @return A configured CandidateOpportunity instance.
   */
  public static CandidateOpportunity createCandidateOpportunity() {
    CandidateOpportunity candidateOpportunity = new CandidateOpportunity();
    candidateOpportunity.setStage(CandidateOpportunityStage.cvPreparation);
    candidateOpportunity.setClosingCommentsForCandidate("WELLDONE");
    return candidateOpportunity;
  }

  /**
   * Creates a User instance representing a system user with a fixed ID.
   *
   * @return A configured User instance with ID 25000.
   */
  public static User createSystemUser() {
    return createUser(25000L);
  }

  /**
   * Creates and persists a LanguageLevel instance.
   *
   * @param repository The LanguageLevelRepository to save the language level.
   * @return The persisted LanguageLevel instance.
   */
  public static LanguageLevel createAndSaveLanguageLevel(LanguageLevelRepository repository) {
    return saveEntity(repository, createLanguageLevel());
  }

  /**
   * Creates a new LanguageLevel instance with default values.
   *
   * @return A configured LanguageLevel instance.
   */
  public static LanguageLevel createLanguageLevel() {
    LanguageLevel languageLevel = new LanguageLevel();
    languageLevel.setLevel(1);
    languageLevel.setStatus(Status.active);
    languageLevel.setName("VERY_HIGH_LEVEL");
    return languageLevel;
  }

  /**
   * Creates a new Occupation instance with randomized name.
   *
   * @return A configured Occupation instance.
   */
  public static Occupation createOccupation() {
    Occupation occupation = new Occupation();
    occupation.setStatus(Status.active);
    occupation.setName(String.format("TEST_OCCUPATION%04d", new Random().nextInt(10000)));
    return occupation;
  }

  /**
   * Creates and persists an Occupation instance.
   *
   * @param repository The OccupationRepository to save the occupation.
   * @return The persisted Occupation instance.
   */
  public static Occupation createAndSaveOccupation(OccupationRepository repository) {
    return saveEntity(repository, createOccupation());
  }

  /**
   * Creates and persists a Language instance.
   *
   * @param repository The LanguageRepository to save the language.
   * @return The persisted Language instance.
   */
  public static Language createAndSaveLanguage(LanguageRepository repository) {
    return saveEntity(repository, createLanguage());
  }

  /**
   * Creates a new Language instance with randomized name.
   *
   * @return A configured Language instance.
   */
  public static Language createLanguage() {
    Language language = new Language();
    language.setStatus(Status.active);
    language.setName(String.format("TEST_LANGUAGE%04d", new Random().nextInt(10000)));
    return language;
  }

  /**
   * Creates and persists a Translation instance.
   *
   * @param repository The TranslationRepository to save the translation.
   * @return The persisted Translation instance.
   */
  public static Translation createAndSaveTranslation(TranslationRepository repository) {
    return saveEntity(repository, createTranslation());
  }

  /**
   * Creates a new Translation instance with default values.
   *
   * @return A configured Translation instance.
   */
  public static Translation createTranslation() {
    Translation translation = new Translation();
    translation.setObjectId(1L);
    translation.setObjectType("country");
    translation.setLanguage("ar");
    translation.setValue("hello");
    return translation;
  }

  /**
   * Creates and persists an EducationMajor instance.
   *
   * @param repository The EducationMajorRepository to save the education major.
   * @return The persisted EducationMajor instance.
   */
  public static EducationMajor createAndSaveEducationMajor(EducationMajorRepository repository) {
    return saveEntity(repository, createEducationMajor());
  }

  /**
   * Creates a new EducationMajor instance with randomized name.
   *
   * @return A configured EducationMajor instance.
   */
  public static EducationMajor createEducationMajor() {
    EducationMajor educationMajor = new EducationMajor();
    educationMajor.setStatus(Status.active);
    educationMajor.setName(String.format("TEST_EDUCATION_MAJOR%04d", new Random().nextInt(10000)));
    return educationMajor;
  }

  /**
   * Creates and persists a SearchJoin instance.
   *
   * @param repository The SearchJoinRepository to save the search join.
   * @return The persisted SearchJoin instance.
   */
  public static SearchJoin createAndSaveSearchJoin(SearchJoinRepository repository) {
    return saveEntity(repository, createSearchJoin());
  }

  /**
   * Creates a new SearchJoin instance with default values.
   *
   * @return A configured SearchJoin instance.
   */
  public static SearchJoin createSearchJoin() {
    SearchJoin searchJoin = new SearchJoin();
    searchJoin.setSearchType(SearchType.or);
    return searchJoin;
  }

  /**
   * Creates and persists a SavedListLink instance.
   *
   * @param repository The SavedListLinkRepository to save the link.
   * @return The persisted SavedListLink instance.
   */
  public static SavedListLink createAndSaveSavedListLink(SavedListLinkRepository repository) {
    return saveEntity(repository, createSavedListLink());
  }

  /**
   * Creates a new SavedListLink instance with default values.
   *
   * @return A configured SavedListLink instance.
   */
  public static SavedListLink createSavedListLink() {
    SavedListLink savedListLink = new SavedListLink();
    savedListLink.setLink("TEST_SAVED_LINK");
    return savedListLink;
  }

  /**
   * Creates and persists a RootRequest instance.
   *
   * @param repository The RootRequestRepository to save the request.
   * @return The persisted RootRequest instance.
   */
  public static RootRequest createAndSaveRootRequest(RootRequestRepository repository) {
    return saveEntity(repository, createRootRequest());
  }

  /**
   * Creates a new RootRequest instance with default values.
   *
   * @return A configured RootRequest instance.
   */
  public static RootRequest createRootRequest() {
    RootRequest rootRequest = new RootRequest();
    rootRequest.setTimestamp(Instant.now());
    rootRequest.setPartnerAbbreviation("TEST_PARTNER_ABBREVIATION");
    rootRequest.setIpAddress("127.0.0.1");
    return rootRequest;
  }

  /**
   * Creates and persists an EducationLevel instance.
   *
   * @param repository The EducationLevelRepository to save the education level.
   * @return The persisted EducationLevel instance.
   */
  public static EducationLevel createAndSaveEducationLevel(EducationLevelRepository repository) {
    return saveEntity(repository, createEducationLevel());
  }

  /**
   * Creates a new EducationLevel instance with default values.
   *
   * @return A configured EducationLevel instance.
   */
  public static EducationLevel createEducationLevel() {
    EducationLevel educationLevel = new EducationLevel();
    educationLevel.setName("TEST HIGH EDUCATION LEVEL");
    educationLevel.setStatus(Status.deleted);
    educationLevel.setLevel(9);
    return educationLevel;
  }

  /**
   * Creates and persists a CandidateSkill instance.
   *
   * @param repository The CandidateSkillRepository to save the skill.
   * @return The persisted CandidateSkill instance.
   */
  public static CandidateSkill createAndSaveCandidateSkill(CandidateSkillRepository repository) {
    return saveEntity(repository, createCandidateSkill());
  }

  /**
   * Creates a new CandidateSkill instance with default values.
   *
   * @return A configured CandidateSkill instance.
   */
  public static CandidateSkill createCandidateSkill() {
    CandidateSkill candidateSkill = new CandidateSkill();
    candidateSkill.setSkill("TEST SKILL");
    return candidateSkill;
  }

  /**
   * Creates and persists a CandidateAttachment instance.
   *
   * @param repository The CandidateAttachmentRepository to save the attachment.
   * @return The persisted CandidateAttachment instance.
   */
  public static CandidateAttachment createAndSaveCandidateAttachment(CandidateAttachmentRepository repository) {
    return saveEntity(repository, createCandidateAttachment());
  }

  /**
   * Creates a new CandidateAttachment instance with default values.
   *
   * @return A configured CandidateAttachment instance.
   */
  public static CandidateAttachment createCandidateAttachment() {
    CandidateAttachment candidateAttachment = new CandidateAttachment();
    candidateAttachment.setName("TEST_ATTACHMENT.pdf");
    candidateAttachment.setType(AttachmentType.googlefile);
    candidateAttachment.setFileType("pdf");
    candidateAttachment.setMigrated(true);
    candidateAttachment.setCv(false);
    candidateAttachment.setLocation("TEST LOCATION");
    candidateAttachment.setUploadType(UploadType.idCard);
    candidateAttachment.setCreatedBy(createSystemUser());
    candidateAttachment.setCreatedDate(OffsetDateTime.now());
    return candidateAttachment;
  }

  /**
   * Creates and persists a CandidateEducation instance.
   *
   * @param repository The CandidateEducationRepository to save the education.
   * @return The persisted CandidateEducation instance.
   */
  public static CandidateEducation createAndSaveCandidateEducation(CandidateEducationRepository repository) {
    return saveEntity(repository, createCandidateEducation());
  }

  /**
   * Creates and persists a CandidateExam instance.
   *
   * @param repository The CandidateExamRepository to save the exam.
   * @return The persisted CandidateExam instance.
   */
  public static CandidateExam createAndSaveCandidateExam(CandidateExamRepository repository) {
    return saveEntity(repository, createCandidateExam());
  }

  /**
   * Creates a new CandidateExam instance with default values.
   *
   * @return A configured CandidateExam instance.
   */
  public static CandidateExam createCandidateExam() {
    CandidateExam candidateExam = new CandidateExam();
    candidateExam.setExam(Exam.OET);
    return candidateExam;
  }

  /**
   * Creates and persists a HelpLink instance.
   *
   * @param repository The HelpLinkRepository to save the help link.
   * @return The persisted HelpLink instance.
   */
  public static HelpLink createAndSaveHelpLink(HelpLinkRepository repository) {
    return saveEntity(repository, createHelpLink());
  }

  /**
   * Creates a new HelpLink instance with default values.
   *
   * @return A configured HelpLink instance.
   */
  public static HelpLink createHelpLink() {
    HelpLink helpLink = new HelpLink();
    helpLink.setLink("TEST_HELP_LINK");
    helpLink.setLabel("TEST LABEL");
    return helpLink;
  }

  /**
   * Creates a new CandidateSavedList instance with the specified candidate and saved list.
   *
   * @param candidate  The associated Candidate.
   * @param savedList  The associated SavedList.
   * @return A configured CandidateSavedList instance.
   */
  public static CandidateSavedList createCandidateSavedList(Candidate candidate, SavedList savedList) {
    CandidateSavedList candidateSavedList = new CandidateSavedList();
    candidateSavedList.setContextNote("CONTEXT NOTE");
    candidateSavedList.setCandidate(candidate);
    candidateSavedList.setSavedList(savedList);
    candidateSavedList.setId(new CandidateSavedListKey(candidate.getId(), savedList.getId()));
    return candidateSavedList;
  }

  /**
   * Creates a list of country IDs including newly persisted and existing countries.
   *
   * @param repository The CountryRepository to save and fetch countries.
   * @param existing   The list of existing countries to include.
   * @return A list of country IDs.
   */
  public static List<Long> createSourceCountryIds(CountryRepository repository, List<Country> existing) {
    List<Country> countries = new ArrayList<>(Arrays.asList(createAndSaveCountry(repository), createAndSaveCountry(repository)));
    countries.addAll(existing);
    return countries.stream().map(Country::getId).collect(Collectors.toList());
  }

  /**
   * Creates a list of country IDs including newly persisted and an existing country.
   *
   * @param repository The CountryRepository to save and fetch countries.
   * @param existing   The existing country to include.
   * @return A list of country IDs.
   */
  public static List<Long> createSourceCountryIds(CountryRepository repository, Country existing) {
    List<Country> countries = new ArrayList<>(Arrays.asList(createAndSaveCountry(repository), createAndSaveCountry(repository), existing));
    return countries.stream().map(Country::getId).collect(Collectors.toList());
  }

  /**
   * Creates a set of candidate IDs from newly persisted candidates.
   *
   * @param repository The CandidateRepository to save and fetch candidates.
   * @param userRepository The UserRepository to save and fetch users.
   * @return A mutable set of candidate IDs.
   */
  public static Set<Long> createCandidateIds(CandidateRepository repository, UserRepository userRepository) {
    Set<Candidate> candidates = new HashSet<>(Arrays.asList(
        createAndSaveCandidate(repository, createAndSaveUser(userRepository)),
        createAndSaveCandidate(repository, createAndSaveUser(userRepository))
    ));
    return candidates.stream().map(Candidate::getId).collect(Collectors.toSet());
  }

  /**
   * Creates a set of candidate IDs including newly persisted and an existing candidate.
   *
   * @param repository The CandidateRepository to save and fetch candidates.
   * @param userRepository The UserRepository to save and fetch users.
   * @param existing   The existing candidate to include.
   * @return A mutable set of candidate IDs.
   */
  public static Set<Long> createCandidateIds(CandidateRepository repository, UserRepository userRepository, Candidate existing) {
    Set<Candidate> candidates = new HashSet<>(Arrays.asList(
        createAndSaveCandidate(repository, createAndSaveUser(userRepository)),
        createAndSaveCandidate(repository, createAndSaveUser(userRepository)),
        existing
    ));
    return candidates.stream().map(Candidate::getId).collect(Collectors.toSet());
  }

  /**
   * Creates a new CandidateOccupation instance with default values.
   *
   * @return A configured CandidateOccupation instance.
   */
  public static CandidateOccupation createCandidateOccupation() {
    CandidateOccupation candidateOccupation = new CandidateOccupation();
    candidateOccupation.setYearsExperience(5L);
    return candidateOccupation;
  }

  /**
   * Creates a new LanguageLevel instance for written proficiency.
   *
   * @return A configured LanguageLevel instance.
   */
  public static LanguageLevel createWrittenLanguageLevel() {
    LanguageLevel languageLevel = new LanguageLevel();
    languageLevel.setLevel(9);
    return languageLevel;
  }

  /**
   * Creates a new CandidateLanguage instance.
   *
   * @return A configured CandidateLanguage instance.
   */
  public static CandidateLanguage createCandidateLanguage() {
    return new CandidateLanguage();
  }

  /**
   * Creates a new DuolingoCoupon instance with default values assigned to a candidate.
   *
   * @return A configured DuolingoCoupon instance.
   */
  public static DuolingoCoupon createAssignedDuolingoCoupon(Candidate candidate) {
    DuolingoCoupon coupon = new DuolingoCoupon();
    coupon.setCouponCode("TEST-COUPON-1");
    coupon.setCandidate(candidate);
    coupon.setCouponStatus(DuolingoCouponStatus.AVAILABLE);
    coupon.setTestType(DuolingoTestType.PROCTORED);
    coupon.setExpirationDate(LocalDateTime.now().plusDays(30));
    coupon.setDateSent(LocalDateTime.now());
    return coupon;
  }
  /**
   * Creates and persists a DuolingoCoupon instance to the provided repository.
   *
   * @param repository The DuolingoCouponRepository to save the coupon.
   * @return The persisted DuolingoCoupon instance.
   */
  public static DuolingoCoupon createAndSaveAssignedDuolingoCoupon(DuolingoCouponRepository repository, Candidate candidate) {
    return saveEntity(repository, createAssignedDuolingoCoupon(candidate));
  }

  /**
   * Creates a new DuolingoCoupon instance with default values assigned to a candidate.
   *
   * @return A configured DuolingoCoupon instance.
   */
  public static DuolingoCoupon createUnassignedDuolingoCoupon() {
    DuolingoCoupon coupon = new DuolingoCoupon();
    coupon.setCouponCode("TEST-COUPON-2");
    coupon.setCouponStatus(DuolingoCouponStatus.AVAILABLE);
    coupon.setTestType(DuolingoTestType.PROCTORED);
    coupon.setExpirationDate(LocalDateTime.now().plusDays(30));
    coupon.setDateSent(LocalDateTime.now());
    return coupon;
  }
  /**
   * Creates and persists a DuolingoCoupon instance to the provided repository.
   *
   * @param repository The DuolingoCouponRepository to save the coupon.
   * @return The persisted DuolingoCoupon instance.
   */
  public static DuolingoCoupon createAndSaveUnassignedDuolingoCoupon(DuolingoCouponRepository repository) {
    return saveEntity(repository, createUnassignedDuolingoCoupon());
  }
}