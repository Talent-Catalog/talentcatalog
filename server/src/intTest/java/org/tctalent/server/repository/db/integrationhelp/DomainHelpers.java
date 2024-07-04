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

import java.time.OffsetDateTime;
import java.util.Random;
import org.springframework.data.jpa.repository.JpaRepository;
import org.tctalent.server.model.db.*;
import org.tctalent.server.repository.db.*;
import org.tctalent.server.service.db.audit.AuditAction;
import org.tctalent.server.service.db.audit.AuditType;

public class DomainHelpers {

  public static TaskImpl getTask(String taskName, String taskDisplay) {
    TaskImpl task = new TaskImpl();
    task.setName(taskName != null ? taskName : "DEFAULT");
    task.setDisplayName(taskDisplay != null ? taskDisplay : "DEFAULT DISPLAY");
    task.setCreatedBy(systemUser());
    task.setCreatedDate(OffsetDateTime.now());
    return task;
  }

  public static TaskImpl getSavedTask(TaskRepository repo) {
    return saveHelperObject(repo, getTask(null, null));
  }

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

  public static User getSavedUser(UserRepository userRepo) {
    return userRepo.save(getUser(null));
  }

  public static Candidate getSavedCandidate(CandidateRepository repo, User savedUser) {
    Candidate candidate = getCandidate();
    candidate.setUser(savedUser);
    return saveHelperObject(repo, candidate);
  }

  public static CandidateCertification getSavedCandidateCert(
      CandidateCertificationRepository repo) {
    return saveHelperObject(repo, getCandidateCert());
  }

  public static CandidateCertification getCandidateCert() {
    CandidateCertification cert = new CandidateCertification();
    cert.setName("GREAT CERT");
    return cert;
  }

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

  public static SavedList getSavedSavedList(SavedListRepository savedListRepo) {
    return saveHelperObject(savedListRepo, getSavedList());
  }

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

  public static Reaction getSavedReaction(ReactionRepository repo) {
    return saveHelperObject(repo, getReaction());
  }

  public static Reaction getReaction() {
    Reaction reaction = new Reaction();
    reaction.setEmoji("Smile");
    return reaction;
  }

  public static JobChat getJobChat() {
    JobChat jobChat = new JobChat();
    jobChat.setType(JobChatType.JobCreatorAllSourcePartners);
    jobChat.setCreatedBy(systemUser());
    jobChat.setCreatedDate(OffsetDateTime.now());
    return jobChat;
  }

  public static ChatPost getSavedChatPost(ChatPostRepository repo) {
    return saveHelperObject(repo, getChatPost());
  }

  public static ChatPost getChatPost() {
    ChatPost chatPost = new ChatPost();
    chatPost.setContent("NothingChatContent");
    chatPost.setCreatedBy(systemUser());
    chatPost.setCreatedDate(OffsetDateTime.now());
    return chatPost;
  }

  public static SurveyType getSavedSurveyType(SurveyTypeRepository repo) {
    return saveHelperObject(repo, getSurveyType());
  }

  public static SurveyType getSurveyType() {
    SurveyType surveyType = new SurveyType();
    surveyType.setName("IntTestSurvey");
    surveyType.setStatus(Status.active);
    return surveyType;
  }

  public static SystemLanguage getSavedSystemLanguage(SystemLanguageRepository repo) {
    return saveHelperObject(repo, getSystemLanguage());
  }

  public static SystemLanguage getSystemLanguage() {
    SystemLanguage language = new SystemLanguage();
    language.setLanguage("en");
    language.setLabel("English");
    language.setStatus(Status.active);
    return language;
  }

  public static <T, ID> T saveHelperObject(JpaRepository<T, ID> repo, T entity) {
    return repo.saveAndFlush(entity);
  }

  public static User getUser(Long idToUse) {
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

  public static CandidateDependant getCandidateDependent() {
    CandidateDependant dependent = new CandidateDependant();
    dependent.setName(String.format("James%04d", new Random().nextInt(10000)));
    dependent.setGender(Gender.male);
    return dependent;
  }

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

  public static JobChat getSavedJobChat(JobChatRepository repo) {
    return saveHelperObject(repo, getJobChat());
  }

  public static JobChatUser getSavedJobChatUser(JobChatUserRepository repository, User savedUser,
      JobChat savedJobChat) {
    return saveHelperObject(repository, getJobChatUser(savedUser, savedJobChat));
  }

  public static JobChatUser getJobChatUser(User savedUser, JobChat savedChat) {
    JobChatUserKey key = getJobChatUserKey(savedUser, savedChat);
    JobChatUser jobChatUser = new JobChatUser();
    jobChatUser.setId(key);
    jobChatUser.setChat(savedChat);
    jobChatUser.setUser(savedUser);
    return jobChatUser;
  }

  public static JobChatUserKey getJobChatUserKey(User savedUser, JobChat savedChat) {
    JobChatUserKey key = new JobChatUserKey();
    key.setUserId(savedUser.getId());
    key.setJobChatId(savedChat.getId());
    return key;
  }

  public static SalesforceJobOpp getSavedSalesforceJobOpp(SalesforceJobOppRepository repo) {
    return saveHelperObject(repo, getSalesforceJobOpp());
  }

  public static CandidateEducation getSavedCandidateEducation(CandidateEducationRepository repo) {
    return saveHelperObject(repo, getCandidateEducation());
  }

  private static User systemUser() {
    User system = new User();
    system.setId(1L); // This is a System user in the dump.
    system.setUsername("SYSTEM");
    system.setRole(Role.systemadmin);
    system.setStatus(Status.active);
    return system;
  }
}
