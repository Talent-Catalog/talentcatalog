/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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

package org.tctalent.server.repository.db.integrationhelp;

import java.time.Instant;
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

public class DomainHelpers {

  /**
   * Retrieves a new TaskImpl instance initialized with provided or default values.
   *
   * @param taskName    The name of the task (default: "DEFAULT").
   * @param taskDisplay The display name of the task (default: "DEFAULT DISPLAY").
   * @return A new TaskImpl instance.
   */
  public static TaskImpl getTask(String taskName, String taskDisplay) {
    TaskImpl task = new TaskImpl();
    task.setName(taskName != null ? taskName : "DEFAULT");
    task.setDisplayName(taskDisplay != null ? taskDisplay : "DEFAULT DISPLAY");
    task.setCreatedBy(systemUser());
    task.setCreatedDate(OffsetDateTime.now());
    return task;
  }

  /**
   * Retrieves a saved TaskImpl instance after saving it to the repository.
   *
   * @param repo The repository where the task will be saved.
   * @return The saved TaskImpl instance.
   */
  public static TaskImpl getSavedTask(TaskRepository repo) {
    return saveHelperObject(repo, getTask(null, null));
  }

  /**
   * Retrieves a new TaskAssignmentImpl instance initialized with provided user details.
   *
   * @param user The user to whom the task assignment is made.
   * @return A new TaskAssignmentImpl instance.
   */
  public static TaskAssignmentImpl getTaskAssignment(User user) {
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
   * Retrieves a saved User instance after saving it to the repository.
   *
   * @param userRepo The repository where the user will be saved.
   * @return The saved User instance.
   */
  public static User getSavedUser(UserRepository userRepo) {
    return userRepo.save(getUser());
  }

  /**
   * Retrieves a saved Candidate instance after saving it to the repository.
   *
   * @param repo      The repository where the candidate will be saved.
   * @param savedUser The user associated with the candidate.
   * @return The saved Candidate instance.
   */
  public static Candidate getSavedCandidate(CandidateRepository repo, User savedUser) {
    Candidate candidate = getCandidate();
    candidate.setUser(savedUser);
    return saveHelperObject(repo, candidate);
  }

  /**
   * Retrieves a saved CandidateCertification instance after saving it to the repository.
   *
   * @param repo The repository where the candidate certification will be saved.
   * @return The saved CandidateCertification instance.
   */
  public static CandidateCertification getSavedCandidateCert(
      CandidateCertificationRepository repo) {
    return saveHelperObject(repo, getCandidateCert());
  }

  /**
   * Retrieves a new CandidateCertification instance initialized with a default name.
   *
   * @return A new CandidateCertification instance.
   */
  public static CandidateCertification getCandidateCert() {
    CandidateCertification cert = new CandidateCertification();
    cert.setName("GREAT CERT");
    return cert;
  }

  /**
   * Retrieves a new Candidate instance initialized with random data.
   *
   * @return A new Candidate instance.
   */
  public static Candidate getCandidate() {
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
    candidate.setCreatedBy(systemUser());
    candidate.setCreatedDate(OffsetDateTime.now().minusDays(5));
    return candidate;
  }

  /**
   * Retrieves a saved SavedList instance after saving it to the repository.
   *
   * @param savedListRepo The repository where the saved list will be saved.
   * @return The saved SavedList instance.
   */
  public static SavedList getSavedSavedList(SavedListRepository savedListRepo) {
    return saveHelperObject(savedListRepo, getSavedList());
  }

  /**
   * Retrieves a new SavedList instance initialized with default values.
   *
   * @return A new SavedList instance.
   */
  public static SavedList getSavedList() {
    SavedList savedList = new SavedList();
    savedList.setDescription("SavedList");
    savedList.setName("SavedList");
    savedList.setTbbShortName("TEST_TBB_SHORT_NAME");
    savedList.setGlobal(true);
    savedList.setRegisteredJob(true);
    savedList.setFixed(true);
    savedList.setCreatedBy(systemUser());
    savedList.setCreatedDate(OffsetDateTime.now());
    return savedList;
  }

  /**
   * Retrieves a new CandidateEducation instance initialized with default values.
   *
   * @return A new CandidateEducation instance.
   */
  public static CandidateEducation getCandidateEducation() {
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
   * Retrieves a saved Reaction instance after saving it to the repository.
   *
   * @param repo The repository where the reaction will be saved.
   * @return The saved Reaction instance.
   */
  public static Reaction getSavedReaction(ReactionRepository repo) {
    return saveHelperObject(repo, getReaction());
  }

  /**
   * Retrieves a new Reaction instance initialized with default values.
   *
   * @return A new Reaction instance.
   */
  public static Reaction getReaction() {
    Reaction reaction = new Reaction();
    reaction.setEmoji("Smile");
    return reaction;
  }

  /**
   * Retrieves a new JobChat instance initialized with default values.
   *
   * @return A new JobChat instance.
   */
  public static JobChat getJobChat() {
    JobChat jobChat = new JobChat();
    jobChat.setType(JobChatType.JobCreatorAllSourcePartners);
    jobChat.setCreatedBy(systemUser());
    jobChat.setCreatedDate(OffsetDateTime.now());
    return jobChat;
  }

  /**
   * Retrieves a saved ChatPost instance after saving it to the repository.
   *
   * @param repo The repository where the chat post will be saved.
   * @return The saved ChatPost instance.
   */
  public static ChatPost getSavedChatPost(ChatPostRepository repo) {
    return saveHelperObject(repo, getChatPost());
  }

  /**
   * Retrieves a new ChatPost instance initialized with default values.
   *
   * @return A new ChatPost instance.
   */
  public static ChatPost getChatPost() {
    ChatPost chatPost = new ChatPost();
    chatPost.setContent("NothingChatContent");
    chatPost.setCreatedBy(systemUser());
    chatPost.setCreatedDate(OffsetDateTime.now());
    return chatPost;
  }

  /**
   * Retrieves a saved SurveyType instance after saving it to the repository.
   *
   * @param repo The repository where the survey type will be saved.
   * @return The saved SurveyType instance.
   */
  public static SurveyType getSavedSurveyType(SurveyTypeRepository repo) {
    return saveHelperObject(repo, getSurveyType());
  }

  /**
   * Retrieves a new SurveyType instance initialized with default values.
   *
   * @return A new SurveyType instance.
   */
  public static SurveyType getSurveyType() {
    SurveyType surveyType = new SurveyType();
    surveyType.setName("IntTestSurvey");
    surveyType.setStatus(Status.active);
    return surveyType;
  }

  /**
   * Retrieves a saved SystemLanguage instance after saving it to the repository.
   *
   * @param repo The repository where the system language will be saved.
   * @return The saved SystemLanguage instance.
   */
  public static SystemLanguage getSavedSystemLanguage(SystemLanguageRepository repo) {
    return saveHelperObject(repo, getSystemLanguage());
  }

  /**
   * Retrieves a new SystemLanguage instance initialized with default values.
   *
   * @return A new SystemLanguage instance.
   */
  public static SystemLanguage getSystemLanguage() {
    SystemLanguage language = new SystemLanguage();
    language.setLanguage("en");
    language.setLabel("English");
    language.setStatus(Status.active);
    return language;
  }

  /**
   * Saves the provided entity to the repository and returns the saved entity.
   *
   * @param repo   The repository where the entity will be saved.
   * @param entity The entity to be saved.
   * @param <T>    Type of the entity.
   * @param <ID>   Type of the entity's ID.
   * @return The saved entity.
   */
  public static <T, ID> T saveHelperObject(JpaRepository<T, ID> repo, T entity) {
    return repo.saveAndFlush(entity);
  }

  /**
   * Retrieves a User instance initialized with default values.
   *
   * @return A User instance.
   */
  public static User getUser() {
    return getUser(null);
  }

  /**
   * Retrieves a User instance initialized with default values.
   *
   * @param idToUse The optional ID to assign to the user.
   * @return A User instance.
   */
  public static User getUser(@Nullable Long idToUse) {
    User user = new User();
    user.setId(idToUse);
    user.setUsername("JO BLOGS");
    user.setEmail("JO.BLOgs@email.com");
    user.setFirstName("jo");
    user.setLastName("blogs");
    user.setRole(Role.user);
    user.setStatus(Status.active);
    user.setUsingMfa(false);
    PartnerImpl partner = new PartnerImpl();
    partner.setId(1L); // This is TBB in the dump.
    user.setPartner(partner);
    user.setCreatedDate(OffsetDateTime.now().minusYears(1));
    return user;
  }

  /**
   * Retrieves a new AuditLog instance initialized with provided object reference.
   *
   * @param objRef The reference to the object associated with the audit log.
   * @return A new AuditLog instance.
   */
  public static AuditLog getAuditLog(String objRef) {
    AuditLog auditLog = new AuditLog();
    auditLog.setType(AuditType.CANDIDATE_OCCUPATION);
    auditLog.setUserId(9L);
    auditLog.setObjectRef(objRef);
    auditLog.setEventDate(OffsetDateTime.now());
    auditLog.setAction(AuditAction.ADD);
    auditLog.setDescription("Create a test audit record.");
    return auditLog;
  }

  /**
   * Retrieves a new CandidateDependant instance initialized with a default name and gender.
   *
   * @return A new CandidateDependant instance.
   */
  public static CandidateDependant getCandidateDependent() {
    CandidateDependant dependent = new CandidateDependant();
    dependent.setName(String.format("James%04d", new Random().nextInt(10000)));
    dependent.setGender(Gender.male);
    return dependent;
  }

  /**
   * Retrieves a new SalesforceJobOpp instance initialized with default values.
   *
   * @return A new SalesforceJobOpp instance.
   */
  public static SalesforceJobOpp getSalesforceJobOpp() {
    SalesforceJobOpp jobOpp = new SalesforceJobOpp();
    jobOpp.setDescription("SF TEST JOB");
    jobOpp.setName("SF test JOB");
    jobOpp.setEmployer("Seraco Pty Ltd");
    Country country = new Country();
    country.setId(6192L); // Australia
    jobOpp.setCountry(country);
    jobOpp.setSfId("TESTSFID");
    jobOpp.setCreatedBy(systemUser());
    jobOpp.setCreatedDate(OffsetDateTime.now());
    return jobOpp;
  }

  /**
   * Retrieves a saved JobChat instance after saving it to the repository.
   *
   * @param repo The repository where the job chat will be saved.
   * @return The saved JobChat instance.
   */
  public static JobChat getSavedJobChat(JobChatRepository repo) {
    return saveHelperObject(repo, getJobChat());
  }

  /**
   * Retrieves a saved JobChatUser instance after saving it to the repository.
   *
   * @param repository   The repository where the job chat user will be saved.
   * @param savedUser    The user associated with the job chat.
   * @param savedJobChat The job chat associated with the user.
   * @return The saved JobChatUser instance.
   */
  public static JobChatUser getSavedJobChatUser(JobChatUserRepository repository, User savedUser,
      JobChat savedJobChat) {
    return saveHelperObject(repository, getJobChatUser(savedUser, savedJobChat));
  }

  /**
   * Retrieves a new JobChatUser instance initialized with provided user and job chat.
   *
   * @param savedUser The user associated with the job chat.
   * @param savedChat The job chat associated with the user.
   * @return A new JobChatUser instance.
   */
  public static JobChatUser getJobChatUser(User savedUser, JobChat savedChat) {
    JobChatUserKey key = getJobChatUserKey(savedUser, savedChat);
    JobChatUser jobChatUser = new JobChatUser();
    jobChatUser.setId(key);
    jobChatUser.setChat(savedChat);
    jobChatUser.setUser(savedUser);
    return jobChatUser;
  }

  /**
   * Retrieves a new JobChatUserKey instance initialized with provided user and job chat IDs.
   *
   * @param savedUser The user associated with the job chat.
   * @param savedChat The job chat associated with the user.
   * @return A new JobChatUserKey instance.
   */
  public static JobChatUserKey getJobChatUserKey(User savedUser, JobChat savedChat) {
    JobChatUserKey key = new JobChatUserKey();
    key.setUserId(savedUser.getId());
    key.setJobChatId(savedChat.getId());
    return key;
  }

  /**
   * Retrieves a saved Partner instance after saving it to the repository.
   *
   * @param repo The repository where the partner will be saved.
   * @return The saved Partner instance.
   */
  public static SalesforceJobOpp getSavedSalesforceJobOpp(SalesforceJobOppRepository repo) {
    return saveHelperObject(repo, getSalesforceJobOpp());
  }

  /**
   * Retrieves a saved Partner instance after saving it to the repository.
   *
   * @param repository The repository where the partner will be saved.
   * @return The saved Partner instance.
   */
  public static PartnerImpl getSavedPartner(PartnerRepository repository) {
    return saveHelperObject(repository, getPartner());
  }

  /**
   * Retrieves a new PartnerImpl instance initialized with a default name.
   *
   * @return A new PartnerImpl instance.
   */
  public static PartnerImpl getPartner() {
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
   * Retrieves a saved Country instance after saving it to the repository.
   *
   * @param repo The repository where the country will be saved.
   * @return The saved Country instance.
   */
  public static Country getSavedCountry(CountryRepository repo) {
    return saveHelperObject(repo, getCountry());
  }

  /**
   * Retrieves a new Country instance initialized with default ISO code and name.
   *
   * @return A new Country instance.
   */
  public static Country getCountry() {
    Country country = new Country();
    country.setIsoCode("ISOCODE");
    country.setName("NewAustralia");
    country.setStatus(Status.active);
    return country;
  }

  /**
   * Retrieves a new CandidateVisaCheck instance initialized with default protection status.
   *
   * @return A new CandidateVisaCheck instance.
   */
  public static CandidateVisaCheck getCandidateVisaCheck() {
    CandidateVisaCheck visaCheck = new CandidateVisaCheck();
    visaCheck.setProtection(YesNo.Yes);
    return visaCheck;
  }

  /**
   * Retrieves a saved Industry instance after saving it to the repository.
   *
   * @param repo The repository where the industry will be saved.
   * @return The saved Industry instance.
   */
  public static Industry getSavedIndustry(IndustryRepository repo) {
    return saveHelperObject(repo, getIndustry());
  }

  /**
   * Retrieves a new Industry instance initialized with default status and name.
   *
   * @return A new Industry instance.
   */
  public static Industry getIndustry() {
    Industry industry = new Industry();
    industry.setStatus(Status.active);
    industry.setName("TestIndustry");
    return industry;
  }

  /**
   * Retrieves a new CandidateVisaJobCheck instance initialized with default name and interest
   * status.
   *
   * @return A new CandidateVisaJobCheck instance.
   */
  public static CandidateVisaJobCheck getCandidateVisaJobCheck() {
    CandidateVisaJobCheck visaJobCheck = new CandidateVisaJobCheck();
    visaJobCheck.setName("TestCandidateVisaJobCheck");
    visaJobCheck.setInterest(YesNo.Yes);
    return visaJobCheck;
  }

  /**
   * Retrieves a new CandidateReviewStatusItem instance initialized with a default comment and
   * review status.
   *
   * @return A new CandidateReviewStatusItem instance.
   */
  public static CandidateReviewStatusItem getCandidateReviewStatusItem() {
    CandidateReviewStatusItem reviewStatusItem = new CandidateReviewStatusItem();
    reviewStatusItem.setComment("TestCandidateReviewStatusItem");
    reviewStatusItem.setReviewStatus(ReviewStatus.verified);
    return reviewStatusItem;
  }

  /**
   * Retrieves a saved SavedSearch instance after saving it to the repository.
   *
   * @param repository The repository where the saved search will be saved.
   * @return The saved SavedSearch instance.
   */
  public static SavedSearch getSavedSavedSearch(SavedSearchRepository repository) {
    return saveHelperObject(repository, getSavedSearch());
  }

  /**
   * Retrieves a new SavedSearch instance initialized with a default type and status and name.
   *
   * @return A new SavedSearch instance.
   */
  public static SavedSearch getSavedSearch() {
    SavedSearch savedSearch = new SavedSearch();
    savedSearch.setType("TestSavedSearch");
    savedSearch.setStatus(Status.active);
    savedSearch.setName("TestSavedSearch");
    savedSearch.setDefaultSearch(true);
    savedSearch.setCreatedBy(systemUser());
    savedSearch.setCreatedDate(OffsetDateTime.now());
    return savedSearch;
  }

  /**
   * Retrieves a saved CandidateOpportunity instance after saving it to the repository.
   *
   * @param repo The repository where the candidate opportunity will be saved.
   * @return The saved CandidateOpportunity instance.
   */
  public static CandidateOpportunity getSavedCandidateOpportunity(
      CandidateOpportunityRepository repo) {
    return saveHelperObject(repo, getCandidateOpportunity());
  }

  /**
   * Retrieves a new CandidateOpportunity instance initialized with default stage and closing
   * comments.
   *
   * @return A new CandidateOpportunity instance.
   */
  public static CandidateOpportunity getCandidateOpportunity() {
    CandidateOpportunity candidateOpportunity = new CandidateOpportunity();
    candidateOpportunity.setStage(CandidateOpportunityStage.cvPreparation);
    candidateOpportunity.setClosingCommentsForCandidate("WELLDONE");
    return candidateOpportunity;
  }

  /**
   * Retrieves a saved SalesforceJobOpp instance after saving it to the repository.
   *
   * @param repo The repository where the Salesforce job opportunity will be saved.
   * @return The saved SalesforceJobOpp instance.
   */
  public static SalesforceJobOpp getSavedSfJobOpp(SalesforceJobOppRepository repo) {
    return saveHelperObject(repo, getSalesforceJobOpp());
  }

  /**
   * Retrieves a User instance with a predefined ID.
   *
   * @return A User instance with ID 25000.
   */
  public static User systemUser() {
    return getUser(25000L);
  }

  /**
   * Retrieves a saved LanguageLevel instance after saving it to the repository.
   *
   * @param repo The repository where the language level will be saved.
   * @return The saved LanguageLevel instance.
   */
  public static LanguageLevel getSavedLanguageLevel(LanguageLevelRepository repo) {
    return saveHelperObject(repo, getLanguageLevel());
  }

  /**
   * Retrieves a new LanguageLevel instance initialized with default level, status, and name.
   *
   * @return A new LanguageLevel instance.
   */
  public static LanguageLevel getLanguageLevel() {
    LanguageLevel languageLevel = new LanguageLevel();
    languageLevel.setLevel(1);
    languageLevel.setStatus(Status.active);
    languageLevel.setName("VERY_HIGH_LEVEL");
    return languageLevel;
  }

  /**
   * Retrieves a new Occupation instance initialized with a default status and name.
   *
   * @return A new Occupation instance.
   */
  public static Occupation getOccupation() {
    Occupation occupation = new Occupation();
    occupation.setStatus(Status.active);
    occupation.setName(String.format("TEST_OCCUPATION%04d", new Random().nextInt(10000)));
    return occupation;
  }

  /**
   * Retrieves a saved Occupation instance after saving it to the repository.
   *
   * @param repo The repository where the occupation will be saved.
   * @return The saved Occupation instance.
   */
  public static Occupation getSavedOccupation(OccupationRepository repo) {
    return saveHelperObject(repo, getOccupation());
  }

  /**
   * Retrieves a saved Language instance after saving it to the repository.
   *
   * @param repo The repository where the language will be saved.
   * @return The saved Language instance.
   */
  public static Language getSavedLanguage(LanguageRepository repo) {
    return saveHelperObject(repo, getLanguage());
  }

  /**
   * Retrieves a new Language instance initialized with a default status and name.
   *
   * @return A new Language instance.
   */
  public static Language getLanguage() {
    Language language = new Language();
    language.setStatus(Status.active);
    language.setName(String.format("TEST_LANGUAGE%04d", new Random().nextInt(10000)));
    return language;
  }

  /**
   * Retrieves a saved Translation instance after saving it to the repository.
   *
   * @param repo The repository where the translation will be saved.
   * @return The saved Translation instance.
   */
  public static Translation getSavedTranslation(TranslationRepository repo) {
    return saveHelperObject(repo, getTranslation());
  }

  /**
   * Retrieves a new Translation instance initialized with default object ID, object type, language,
   * and value.
   *
   * @return A new Translation instance.
   */
  public static Translation getTranslation() {
    Translation translation = new Translation();
    translation.setObjectId(1L);
    translation.setObjectType("country");
    translation.setLanguage("ar");
    translation.setValue("hello");
    return translation;
  }

  /**
   * Retrieves a saved EducationMajor instance after saving it to the repository.
   *
   * @param repo The repository where the education major will be saved.
   * @return The saved EducationMajor instance.
   */
  public static EducationMajor getSavedEducationMajor(EducationMajorRepository repo) {
    return saveHelperObject(repo, getEducationMajor());
  }

  /**
   * Retrieves a new EducationMajor instance initialized with a default status and name.
   *
   * @return A new EducationMajor instance.
   */
  public static EducationMajor getEducationMajor() {
    EducationMajor educationMajor = new EducationMajor();
    educationMajor.setStatus(Status.active);
    educationMajor.setName(String.format("TEST_EDUCATION_MAJOR%04d", new Random().nextInt(10000)));
    return educationMajor;
  }

  /**
   * Retrieves a saved SearchJoin instance after saving it to the repository.
   *
   * @param repo The repository where the search join will be saved.
   * @return The saved SearchJoin instance.
   */
  public static SearchJoin getSavedSearchJoin(SearchJoinRepository repo) {
    return saveHelperObject(repo, getSearchJoin());
  }

  /**
   * Retrieves a new SearchJoin instance initialized with a default search type.
   *
   * @return A new SearchJoin instance.
   */
  public static SearchJoin getSearchJoin() {
    SearchJoin searchJoin = new SearchJoin();
    searchJoin.setSearchType(SearchType.or);
    return searchJoin;
  }

  /**
   * Retrieves a saved SavedListLink instance after saving it to the repository.
   *
   * @param repo The repository where the saved list link will be saved.
   * @return The saved SavedListLink instance.
   */
  public static SavedListLink getSavedSavedListLink(SavedListLinkRepository repo) {
    return saveHelperObject(repo, getSavedListLink());
  }

  /**
   * Retrieves a new SavedListLink instance initialized with a default link.
   *
   * @return A new SavedListLink instance.
   */
  public static SavedListLink getSavedListLink() {
    SavedListLink savedListLink = new SavedListLink();
    savedListLink.setLink("TEST_SAVED_LINK");
    return savedListLink;
  }

  /**
   * Retrieves a saved RootRequest instance after saving it to the repository.
   *
   * @param repo The repository where the root request will be saved.
   * @return The saved RootRequest instance.
   */
  public static RootRequest getSavedRootRequest(RootRequestRepository repo) {
    return saveHelperObject(repo, getRootRequest());
  }

  /**
   * Retrieves a new RootRequest instance initialized with a default timestamp and partner
   * abbreviation.
   *
   * @return A new RootRequest instance.
   */
  public static RootRequest getRootRequest() {
    RootRequest rootRequest = new RootRequest();
    rootRequest.setTimestamp(Instant.now());
    rootRequest.setPartnerAbbreviation("TEST_PARTNER_ABBREVIATION");
    rootRequest.setIpAddress("127.0.0.1");
    return rootRequest;
  }

  /**
   * Retrieves a saved EducationLevel instance after saving it to the repository.
   *
   * @param report The repository where the education level will be saved.
   * @return The saved EducationLevel instance.
   */
  public static EducationLevel getSavedEducationLevel(EducationLevelRepository report) {
    return saveHelperObject(report, getEducationLevel());
  }

  /**
   * Retrieves a new EducationLevel instance initialized with a default name and status.
   *
   * @return A new EducationLevel instance.
   */
  public static EducationLevel getEducationLevel() {
    EducationLevel educationLevel = new EducationLevel();
    educationLevel.setName("TEST HIGH EDUCATION LEVEL");
    educationLevel.setStatus(Status.deleted);
    educationLevel.setLevel(9);
    return educationLevel;
  }

  /**
   * Retrieves a saved CandidateSkill instance after saving it to the repository.
   *
   * @param repo The repository where the candidate skill will be saved.
   * @return The saved CandidateSkill instance.
   */
  public static CandidateSkill getSavedCandidateSkill(CandidateSkillRepository repo) {
    return saveHelperObject(repo, getCandidateSkill());
  }

  /**
   * Retrieves a new CandidateSkill instance initialized with a default skill.
   *
   * @return A new CandidateSkill instance.
   */
  public static CandidateSkill getCandidateSkill() {
    CandidateSkill candidateSkill = new CandidateSkill();
    candidateSkill.setSkill("TEST SKILL");
    return candidateSkill;
  }

  /**
   * Retrieves a saved CandidateAttachment instance after saving it to the repository.
   *
   * @param repo The repository where the candidate attachment will be saved.
   * @return The saved CandidateAttachment instance.
   */
  public static CandidateAttachment getSavedCandidateAttachment(
      CandidateAttachmentRepository repo) {
    return saveHelperObject(repo, getCandidateAttachment());
  }

  /**
   * Retrieves a new CandidateAttachment instance initialized with a default name and type.
   *
   * @return A new CandidateAttachment instance.
   */
  public static CandidateAttachment getCandidateAttachment() {
    CandidateAttachment candidateAttachment = new CandidateAttachment();
    candidateAttachment.setName("TEST_ATTACHMENT.pdf");
    candidateAttachment.setType(AttachmentType.googlefile);
    candidateAttachment.setFileType("pdf");
    candidateAttachment.setMigrated(true);
    candidateAttachment.setCv(false);
    candidateAttachment.setLocation("TEST LOCATION");
    candidateAttachment.setUploadType(UploadType.idCard);
    candidateAttachment.setCreatedBy(systemUser());
    candidateAttachment.setCreatedDate(OffsetDateTime.now());
    return candidateAttachment;
  }

  /**
   * Retrieves a saved CandidateEducation instance after saving it to the repository.
   *
   * @param repo The repository where the candidate education will be saved.
   * @return The saved CandidateEducation instance.
   */
  public static CandidateEducation getSavedCandidateEducation(CandidateEducationRepository repo) {
    return saveHelperObject(repo, getCandidateEducation());
  }

  /**
   * Retrieves a saved CandidateExam instance after saving it to the repository.
   *
   * @param repo The repository where the candidate exam will be saved.
   * @return The saved CandidateExam instance.
   */
  public static CandidateExam getSavedCandidateExam(CandidateExamRepository repo) {
    return saveHelperObject(repo, getCandidateExam());
  }

  /**
   * Retrieves a new CandidateExam instance initialized with a default exam.
   *
   * @return A new CandidateExam instance.
   */
  public static CandidateExam getCandidateExam() {
    CandidateExam candidateExam = new CandidateExam();
    candidateExam.setExam(Exam.OET);
    return candidateExam;
  }

  /**
   * Retrieves a saved HelpLink instance after saving it to the repository.
   *
   * @param repo The repository where the help link will be saved.
   * @return The saved HelpLink instance.
   */
  public static HelpLink getSavedHelpLink(HelpLinkRepository repo) {
    return saveHelperObject(repo, getHelpLink());
  }

  /**
   * Retrieves a new HelpLink instance initialized with a default link and label.
   *
   * @return A new HelpLink instance.
   */
  public static HelpLink getHelpLink() {
    HelpLink helpLink = new HelpLink();
    helpLink.setLink("TEST_HELP_LINK");
    helpLink.setLabel("TEST LABEL");
    return helpLink;
  }

  /**
   * Retrieves a new CandidateSavedList instance initialized with a given candidate, saved list, and
   * a default context note.
   *
   * @param candidate The candidate associated with the saved list.
   * @param savedList The saved list associated with the candidate.
   * @return A new CandidateSavedList instance.
   */
  public static CandidateSavedList getCandidateSavedList(Candidate candidate, SavedList savedList) {
    CandidateSavedList candidateSavedList = new CandidateSavedList();
    candidateSavedList.setContextNote("CONTEXT NOTE");
    candidateSavedList.setCandidate(candidate);
    candidateSavedList.setSavedList(savedList);
    candidateSavedList.setId(new CandidateSavedListKey(candidate.getId(), savedList.getId()));
    return candidateSavedList;
  }

  /**
   * Retrieves a list of country IDs including newly saved countries and existing ones.
   *
   * @param repo     The repository used to save and fetch countries.
   * @param existing A list of existing countries to include.
   * @return A list of country IDs.
   */
  public static List<Long> getSourceCountryIds(CountryRepository repo, List<Country> existing) {
    List<Country> countries = new ArrayList<>(
        Arrays.asList(getSavedCountry(repo), getSavedCountry(repo)));
    countries.addAll(existing);
    return countries.stream().map(Country::getId).collect(Collectors.toList());
  }

  /**
   * Retrieves a list of country IDs including newly saved countries and an existing country.
   *
   * @param repo     The repository used to save and fetch countries.
   * @param existing An existing country to include.
   * @return A list of country IDs.
   */
  public static List<Long> getSourceCountryIds(CountryRepository repo, Country existing) {
    List<Country> countries = new ArrayList<>(
        Arrays.asList(getSavedCountry(repo), getSavedCountry(repo), existing));
    return countries.stream().map(Country::getId).collect(Collectors.toList());
  }

  /**
   * Retrieves a set of candidate IDs after saving new candidates.
   *
   * @param repo     The repository used to save and fetch candidates.
   * @param userRepo The repository used to save and fetch users.
   * @return A mutable set of candidate IDs.
   */
  public static Set<Long> getCandidateIds(CandidateRepository repo, UserRepository userRepo) {
    Set<Candidate> candidates = new HashSet<>(Arrays.asList(
        getSavedCandidate(repo, getSavedUser(userRepo)),
        getSavedCandidate(repo, getSavedUser(userRepo))
    ));
    return candidates.stream().map(Candidate::getId).collect(Collectors.toSet());
  }

  /**
   * Retrieves a set of candidate IDs including newly saved candidates and an existing candidate.
   *
   * @param repo     The repository used to save and fetch candidates.
   * @param userRepo The repository used to save and fetch users.
   * @param existing An existing candidate to include.
   * @return A mutable set of candidate IDs.
   */
  public static Set<Long> getCandidateIds(CandidateRepository repo, UserRepository userRepo,
      Candidate existing) {
    Set<Candidate> candidates = new HashSet<>(Arrays.asList(
        getSavedCandidate(repo, getSavedUser(userRepo)),
        getSavedCandidate(repo, getSavedUser(userRepo)),
        existing
    ));
    return candidates.stream().map(Candidate::getId).collect(Collectors.toSet());
  }

  /**
   * Retrieves a new CandidateOccupation instance initialized with a default years of experience.
   *
   * @return A new CandidateOccupation instance.
   */
  public static CandidateOccupation getCandidateOccupation() {
    CandidateOccupation candidateOccupation = new CandidateOccupation();
    candidateOccupation.setYearsExperience(5L);
    return candidateOccupation;
  }

  /**
   * Retrieves a new LanguageLevel instance initialized with a default level.
   *
   * @return A new LanguageLevel instance.
   */
  public static LanguageLevel getWrittenLevel() {
    LanguageLevel languageLevel = new LanguageLevel();
    languageLevel.setLevel(9);
    return languageLevel;
  }

  /**
   * Retrieves a new CandidateLanguage instance initialized with a default migration language.
   *
   * @return A new CandidateLanguage instance.
   */
  public static CandidateLanguage getCandidateLanguage() {
    CandidateLanguage candidateLanguage = new CandidateLanguage();
    candidateLanguage.setMigrationLanguage("ENGLISH");
    return candidateLanguage;
  }
}
