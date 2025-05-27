/*
 * Copyright (c) 2024 Talent Catalog.
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

package org.tctalent.server.api.admin;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.tctalent.anonymization.model.CandidateAssistanceType;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateCertification;
import org.tctalent.server.model.db.CandidateCitizenship;
import org.tctalent.server.model.db.CandidateCouponCode;
import org.tctalent.server.model.db.CandidateDependant;
import org.tctalent.server.model.db.CandidateDestination;
import org.tctalent.server.model.db.CandidateEducation;
import org.tctalent.server.model.db.CandidateExam;
import org.tctalent.server.model.db.CandidateJobExperience;
import org.tctalent.server.model.db.CandidateLanguage;
import org.tctalent.server.model.db.CandidateNote;
import org.tctalent.server.model.db.CandidateOccupation;
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.model.db.CandidateOpportunityStage;
import org.tctalent.server.model.db.CandidateReviewStatusItem;
import org.tctalent.server.model.db.CandidateSavedList;
import org.tctalent.server.model.db.CandidateSkill;
import org.tctalent.server.model.db.CandidateVisaCheck;
import org.tctalent.server.model.db.CandidateVisaJobCheck;
import org.tctalent.server.model.db.ChatPost;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.DependantRelations;
import org.tctalent.server.model.db.DocumentStatus;
import org.tctalent.server.model.db.DuolingoCoupon;
import org.tctalent.server.model.db.DuolingoCouponStatus;
import org.tctalent.server.model.db.EducationLevel;
import org.tctalent.server.model.db.EducationMajor;
import org.tctalent.server.model.db.EducationType;
import org.tctalent.server.model.db.Employer;
import org.tctalent.server.model.db.Exam;
import org.tctalent.server.model.db.ExportColumn;
import org.tctalent.server.model.db.FamilyRelations;
import org.tctalent.server.model.db.Gender;
import org.tctalent.server.model.db.HasPassport;
import org.tctalent.server.model.db.HelpLink;
import org.tctalent.server.model.db.Industry;
import org.tctalent.server.model.db.JobChat;
import org.tctalent.server.model.db.JobChatUserInfo;
import org.tctalent.server.model.db.JobOppIntake;
import org.tctalent.server.model.db.JobOpportunityStage;
import org.tctalent.server.model.db.Language;
import org.tctalent.server.model.db.LanguageLevel;
import org.tctalent.server.model.db.NoteType;
import org.tctalent.server.model.db.Occupation;
import org.tctalent.server.model.db.OfferToAssist;
import org.tctalent.server.model.db.OtherVisas;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.Registration;
import org.tctalent.server.model.db.ReviewStatus;
import org.tctalent.server.model.db.RiskLevel;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.SavedSearch;
import org.tctalent.server.model.db.SavedSearchType;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.SurveyType;
import org.tctalent.server.model.db.SystemLanguage;
import org.tctalent.server.model.db.TBBEligibilityAssessment;
import org.tctalent.server.model.db.TaskAssignmentImpl;
import org.tctalent.server.model.db.TaskImpl;
import org.tctalent.server.model.db.Translation;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.VisaEligibility;
import org.tctalent.server.model.db.YesNo;
import org.tctalent.server.model.db.YesNoUnsure;
import org.tctalent.server.model.sf.Opportunity;
import org.tctalent.server.request.candidate.PublishedDocColumnProps;
import org.tctalent.server.request.user.UpdateUserRequest;

/**
 * @author sadatmalik
 */
public class AdminApiTestUtil {

    private static final User caller =
            new User("test_user",
                    "test",
                    "user",
                    "test.user@tbb.org",
                    Role.admin);
    private static final User candidate1 =
            new User("candidate1",
                    "test",
                    "candidate1",
                    "test.candidate1@some.thing",
                    Role.user);

    private static final User candidate2 =
            new User("candidate2",
                    "test",
                    "candidate2",
                    "test.candidate2@some.thing",
                    Role.user);

    private static final User candidate3 =
            new User("candidate3",
                    "test",
                    "candidate3",
                    "test.candidate3@some.thing",
                    Role.user);

    static User getUser() {
        return caller;
    }

    static List<Candidate> listOfCandidates() {
        return List.of(
                new Candidate(candidate1, "+123-456-789", "+123-456-789", caller),
                new Candidate(candidate2, "+234-567-890", "+123-456-789", caller),
                new Candidate(candidate3, "+345-678-901", "+345-678-901", caller)
        );
    }

    static Candidate getCandidate() {
        Candidate candidate = new Candidate(candidate1, "+123-456-789", "+123-456-789", caller);
        candidate.setId(99L);
        candidate.setNationality(new Country("Pakistan", Status.active));
        return candidate;
    }

    static List<CandidateCertification> getListOfCandidateCertifications() {
        return List.of(getCandidateCertification());
    }

    static CandidateCertification getCandidateCertification() {
        return new CandidateCertification(
                getCandidate(),
                "BA",
                "Cambridge",
                LocalDate.of(1998, 5, 1)
        );
    }

    static CandidateCitizenship getCandidateCitizenship() {
        CandidateCitizenship citizenship = new CandidateCitizenship();
        citizenship.setCandidate(getCandidate());
        citizenship.setHasPassport(HasPassport.ValidPassport);
        citizenship.setPassportExp(LocalDate.of(2035, 12, 25));
        citizenship.setNationality(new Country("Pakistan", Status.active));
        citizenship.setNotes("Some example citizenship notes");
        return citizenship;
    }

    static CandidateDependant getCandidateDependant() {
        CandidateDependant dependant = new CandidateDependant();
        dependant.setCandidate(getCandidate());
        dependant.setRelation(DependantRelations.Partner);
        dependant.setRelationOther("Husband");
        dependant.setDob(LocalDate.of(1998, 1, 1));
        dependant.setGender(Gender.male);
        dependant.setName("Ahmad Fatah");
        dependant.setRegistered(Registration.UNHCR);
        dependant.setRegisteredNumber("123456");
        dependant.setRegisteredNotes("Some dependant registration notes");
        dependant.setHealthConcern(YesNo.No);
        dependant.setHealthNotes("Some dependant health notes");
        return dependant;
    }

    static CandidateDestination getCandidateDestination() {
        CandidateDestination destination = new CandidateDestination();
        destination.setCandidate(getCandidate());
        destination.setCountry(new Country("USA", Status.active));
        destination.setInterest(YesNoUnsure.Yes);
        destination.setNotes("Some destination notes");
        return destination;
    }

    static List<CandidateEducation> getListOfCandidateEducations() {
        return List.of(getCandidateEducation());
    }

    static CandidateEducation getCandidateEducation() {
        return new CandidateEducation(
                getCandidate(),
                EducationType.Masters,
                new Country("UK", Status.active),
                new EducationMajor("MA", Status.active),
                4,
                "Cambridge",
                "Computer Science",
                1998,
                false
        );
    }

    static CandidateExam getCandidateExam() {
        CandidateExam exam = new CandidateExam();
        exam.setCandidate(getCandidate());
        exam.setExam(Exam.IELTSGen);
        exam.setOtherExam("IELTS");
        exam.setScore("100");
        exam.setYear(2023L);
        exam.setNotes("Some exam notes");
        return exam;
    }

    static CandidateJobExperience getCandidateJobExperience() {
        CandidateJobExperience jobExperience = new CandidateJobExperience(
                getCandidate(),
                new Country("Syria", Status.active),
                getCandidateOccupation(),
                "Microsoft",
                "Software Engineer",
                LocalDate.of(1998, 1, 1),
                LocalDate.of(2008, 1, 1),
                "Some job experience description"
        );
        jobExperience.setFullTime(true);
        jobExperience.setPaid(true);
        return jobExperience;
    }

    static List<Occupation> getListOfOccupations() {
        return List.of(
                new Occupation("Builder", Status.active),
                new Occupation("Baker", Status.active)
        );
    }

    static List<CandidateOccupation> getListOfCandidateOccupations() {
        return List.of(getCandidateOccupation());
    }

    static CandidateOccupation getCandidateOccupation() {
        return new CandidateOccupation(
                getCandidate(),
                new Occupation("Software Engineer", Status.active),
                10L
        );
    }

    static List<CandidateLanguage> getListOfCandidateLanguages() {
        return List.of(getCandidateLanguage());
    }

    static CandidateLanguage getCandidateLanguage() {
        return new CandidateLanguage(
                getCandidate(),
                new Language("Arabic", Status.active),
                new LanguageLevel("Good", Status.active, 9),
                new LanguageLevel("Good", Status.active, 9)
        );
    }

    static CandidateNote getCandidateNote() {
        CandidateNote candidateNote = new CandidateNote();
        candidateNote.setCandidate(getCandidate());
        candidateNote.setTitle("A title");
        candidateNote.setComment("Some comments");
        candidateNote.setNoteType(NoteType.candidate);
        return candidateNote;
    }

    static CandidateOpportunity getCandidateOpportunity() {
        CandidateOpportunity opportunity = new CandidateOpportunity();
        opportunity.setCandidate(getCandidate());
        opportunity.setClosingCommentsForCandidate("Some closing comments for candidate");
        opportunity.setEmployerFeedback("Some employer feedback");
        opportunity.setStage(CandidateOpportunityStage.offer);
        opportunity.setJobOpp(getSalesforceJobOpp());
        opportunity.setRelocatingDependantIds(List.of(1L, 2L));
        return opportunity;
    }

    static HelpLink getHelpLink() {
        HelpLink helpLink = new HelpLink();
        helpLink.setId(99L);
        helpLink.setCountry(new Country("Jordan", Status.active));
        helpLink.setCaseStage(CandidateOpportunityStage.cvReview);
        helpLink.setJobStage(JobOpportunityStage.jobOffer);
        helpLink.setLabel("Test label");
        helpLink.setLink("https://www.talentbeyondboundaries.org/");
        return helpLink;
    }

    static SalesforceJobOpp getSalesforceJobOpp() {
        SalesforceJobOpp salesforceJobOpp = new SalesforceJobOpp();
        salesforceJobOpp.setId(135L);
        salesforceJobOpp.setSfId("sales-force-job-opp-id");
        return salesforceJobOpp;
    }

    static Opportunity getSalesforceOpportunity() {
        Opportunity opportunity = new Opportunity();
        opportunity.setName("SF Opportunity");
        return opportunity;
    }

    static CandidateReviewStatusItem getCandidateReviewStatusItem() {
        CandidateReviewStatusItem reviewStatusItem = new CandidateReviewStatusItem();
        reviewStatusItem.setCandidate(getCandidate());
        reviewStatusItem.setSavedSearch(new SavedSearch());
        reviewStatusItem.setComment("A review comment");
        reviewStatusItem.setReviewStatus(ReviewStatus.verified);
        return reviewStatusItem;
    }

    static CandidateVisaCheck getCandidateVisaCheck(boolean completed) {
        CandidateVisaCheck candidateVisaCheck = new CandidateVisaCheck();
        candidateVisaCheck.setCandidate(getCandidate());
        candidateVisaCheck.setCountry((new Country("Australia", Status.active)));
        if (completed) {
            candidateVisaCheck.setId(1L);
            candidateVisaCheck.setProtection(YesNo.Yes);
            candidateVisaCheck.setProtectionGrounds("These are some protection grounds.");
            candidateVisaCheck.setEnglishThreshold(YesNo.No);
            candidateVisaCheck.setEnglishThresholdNotes("These are some english threshold notes.");
            candidateVisaCheck.setHealthAssessment(YesNo.Yes);
            candidateVisaCheck.setHealthAssessmentNotes("These are some health assessment notes.");
            candidateVisaCheck.setCharacterAssessment(YesNo.No);
            candidateVisaCheck.setCharacterAssessmentNotes( "These are some character assessment notes.");
            candidateVisaCheck.setSecurityRisk(YesNo.Yes);
            candidateVisaCheck.setSecurityRiskNotes( "These are some security risk notes.");
            candidateVisaCheck.setOverallRisk(RiskLevel.Medium);
            candidateVisaCheck.setOverallRiskNotes( "These are some overall risk notes.");
            candidateVisaCheck.setValidTravelDocs(DocumentStatus.Valid);
            candidateVisaCheck.setValidTravelDocsNotes( "These are some travel docs notes.");
            candidateVisaCheck.setPathwayAssessment(YesNoUnsure.No);
            candidateVisaCheck.setPathwayAssessmentNotes( "These are some pathway assessment notes.");
            candidateVisaCheck.setDestinationFamily(FamilyRelations.Cousin);
            candidateVisaCheck.setDestinationFamilyLocation("New York");
        }
        return candidateVisaCheck;
    }

    static CandidateVisaJobCheck getCandidateVisaJobCheck(boolean completed) {
        CandidateVisaJobCheck candidateVisaJobCheck = new CandidateVisaJobCheck();
        candidateVisaJobCheck.setCandidateVisaCheck(getCandidateVisaCheck(true));

        SalesforceJobOpp jobOpp = new SalesforceJobOpp();
        jobOpp.setId(99L);
        candidateVisaJobCheck.setJobOpp(jobOpp);

        if (completed) {
            candidateVisaJobCheck.setId(1L);
            candidateVisaJobCheck.setInterest(YesNo.Yes);
            candidateVisaJobCheck.setInterestNotes("These are some interest notes.");
            candidateVisaJobCheck.setRegional(YesNo.No);
            candidateVisaJobCheck.setSalaryTsmit(YesNo.Yes);
            candidateVisaJobCheck.setQualification(YesNo.Yes);
            candidateVisaJobCheck.setEligible_494(YesNo.No);
            candidateVisaJobCheck.setEligible_494_Notes("These are some eligible for visa 494 notes.");
            candidateVisaJobCheck.setEligible_186(YesNo.Yes);
            candidateVisaJobCheck.setEligible_186_Notes("These are some eligible for visa 186 notes.");
            candidateVisaJobCheck.setEligibleOther(OtherVisas.SpecialHum);
            candidateVisaJobCheck.setEligibleOtherNotes("These are some eligible for other visa notes.");
            candidateVisaJobCheck.setPutForward(VisaEligibility.DiscussFurther);
            candidateVisaJobCheck.setTbbEligibility(TBBEligibilityAssessment.Discuss);
            candidateVisaJobCheck.setNotes("These are some notes.");
            candidateVisaJobCheck.setOccupation((new Occupation("Accountant", Status.active)));
            candidateVisaJobCheck.setOccupationNotes("These are some occupation notes.");
            candidateVisaJobCheck.setQualificationNotes("These are some qualification notes.");
            candidateVisaJobCheck.setRelevantWorkExp("These are some relevant work experience notes.");
            candidateVisaJobCheck.setAgeRequirement("There are some age requirements.");
            candidateVisaJobCheck.setPreferredPathways("These are some preferred pathways.");
            candidateVisaJobCheck.setIneligiblePathways("These are some ineligible pathways.");
            candidateVisaJobCheck.setEligiblePathways("These are some eligible pathways.");
            candidateVisaJobCheck.setOccupationCategory("This is the occupation category.");
            candidateVisaJobCheck.setOccupationSubCategory("This is the occupation subcategory.");
            candidateVisaJobCheck.setEnglishThreshold(YesNo.Yes);
            candidateVisaJobCheck.setLanguagesRequired(List.of(342L, 344L));
            candidateVisaJobCheck.setLanguagesThresholdMet(YesNo.Yes);
            candidateVisaJobCheck.setLanguagesThresholdNotes("These are some language threshold notes.");
        }
        return candidateVisaJobCheck;
    }

    static CandidateSkill getCandidateSkill() {
        CandidateSkill candidateSkill = new CandidateSkill();
        candidateSkill.setCandidate(getCandidate());
        candidateSkill.setId(1L);
        candidateSkill.setSkill("Adobe Photoshop");
        candidateSkill.setTimePeriod("3-5 years");
        return candidateSkill;
    }

    static PublishedDocColumnProps getPublishedDocColumnProps() {
        PublishedDocColumnProps publishedDocColumnProps = new PublishedDocColumnProps();
        publishedDocColumnProps.setHeader("non default column header");
        publishedDocColumnProps.setConstant("non default constant column value");
        return publishedDocColumnProps;
    }

    static ExportColumn getExportColumn() {
        ExportColumn exportColumn = new ExportColumn();
        exportColumn.setKey("key");
        exportColumn.setProperties(getPublishedDocColumnProps());
        return exportColumn;
    }

    static SavedSearch getSavedSearch() {
        SavedSearch savedSearch = new SavedSearch();
        savedSearch.setId(123L);
        savedSearch.setDescription("This is a search about nothing.");
        savedSearch.setName("My Search");
        savedSearch.setSavedSearchType(SavedSearchType.other);
        savedSearch.setSimpleQueryString("search + term");
        savedSearch.setStatuses("active,pending");
        savedSearch.setGender(Gender.male);
        savedSearch.setOccupationIds("8577,8484");
        return savedSearch;
    }

    static TaskImpl getTask() {
        TaskImpl task = new TaskImpl();
        task.setId(148L);
        task.setName("a test task");
        task.setDaysToComplete(7);
        task.setDescription("a test task description");
        task.setDisplayName("task display name");
        task.setOptional(false);
        task.setDocLink("http://help.link");
        return task;
    }

    public static List<TaskImpl> getListOfTasks() {
        TaskImpl task1 = getTask();
        TaskImpl task2 = getTask();
        task2.setName("test task 2");
        TaskImpl task3 = getTask();
        task3.setName("test task 3");
        return List.of(task1, task2, task3);
    }

    static SavedList getSavedList() {
        SavedList savedList = new SavedList();
        savedList.setId(1L);
        savedList.setDescription("Saved list description");
        savedList.setDisplayedFieldsLong(List.of("user.firstName", "user.lastName"));
        savedList.setExportColumns(List.of(getExportColumn()));
        savedList.setStatus(Status.active);
        savedList.setName("Saved list name");
        savedList.setFixed(true);
        savedList.setGlobal(false);
        savedList.setSavedSearchSource(getSavedSearch());
        savedList.setSfJobOpp(getSalesforceJobOpp());
        savedList.setFileJdLink("http://file.jd.link");
        savedList.setFileJdName("JobDescriptionFileName");
        savedList.setFileJoiLink("http://file.joi.link");
        savedList.setFileJoiName("JoiFileName");
        savedList.setFileInterviewGuidanceLink("http://file.interview.guidance.link");
        savedList.setFileInterviewGuidanceName("InterviewGuidanceFileName");
        savedList.setFileMouLink("http://file.mou.link");
        savedList.setFileMouName("MouFileName");
        savedList.setFolderlink("http://folder.link");
        savedList.setFolderjdlink("http://folder.jd.link");
        savedList.setPublishedDocLink("http://published.doc.link");
        savedList.setRegisteredJob(true);
        savedList.setTcShortName("Saved list Tc short name");
        savedList.setCreatedBy(caller);
        savedList.setCreatedDate(OffsetDateTime.parse("2023-10-30T12:30:00+02:00"));
        savedList.setUpdatedBy(caller);
        savedList.setUpdatedDate(OffsetDateTime.parse("2023-10-30T12:30:00+02:00"));
        savedList.setUsers(Set.of(caller));
        savedList.setTasks(Set.of(getTask()));
        savedList.setCandidateSavedLists(getSetOfCandidateSavedLists());

        return savedList;
    }

    static SavedList getSavedListWithCandidates() {
        SavedList savedList = getSavedList();

        final Candidate candidate1 = getCandidate();
        candidate1.setId(101L);
        CandidateSavedList csl1 = new CandidateSavedList(candidate1, savedList);
        final Candidate candidate2 = getCandidate();
        candidate2.setId(102L);
        CandidateSavedList csl2 = new CandidateSavedList(candidate2, savedList);
        final Candidate candidate3 = getCandidate();
        candidate3.setId(103L);
        CandidateSavedList csl3 = new CandidateSavedList(candidate3, savedList);

        savedList.setCandidateSavedLists(Set.of(csl1, csl2, csl3));

        return savedList;
    }

    static List<SavedList> getSavedLists() {
        return List.of(
            getSavedList()
        );
    }

    static LanguageLevel getLanguageLevel() {
        LanguageLevel languageLevel = new LanguageLevel(
                "Excellent", Status.active, 1
        );
        return languageLevel;
    }

    static List<LanguageLevel> getLanguageLevelList() {
        return List.of(
                getLanguageLevel()
        );
    }

    static SystemLanguage getSystemLanguage() {
        SystemLanguage systemLanguage = new SystemLanguage(
                "Spanish"
        );
        systemLanguage.setId(1L);
        return systemLanguage;
    }

    static Occupation getOccupation() {
        Occupation occupation = new Occupation("Nurse", Status.active);
        occupation.setId(1L);
        return occupation;
    }

    static Language getLanguage() {
        Language language = new Language(
                "Arabic", Status.active
        );
        language.setId(99L);
        return language;
    }

    static List<Language> getLanguageList() {
        return List.of(
                getLanguage()
        );
    }

    static List<SystemLanguage> getSystemLanguageList() {
        return List.of(
                getSystemLanguage()
        );
    }

    static SalesforceJobOpp getJob() {
        SalesforceJobOpp job = new SalesforceJobOpp();
        job.setContactUser(new User("contact_user",
                "contact",
                "user",
                "test.contact@tbb.org",
                Role.admin));
        job.setSfId("123456");
        job.setStage(JobOpportunityStage.cvReview);
        job.setSubmissionDueDate(LocalDate.parse("2020-01-01"));
        job.setNextStep("This is the next step.");
        job.setNextStepDueDate(LocalDate.parse("2020-01-01"));
        job.setClosingComments("These are some closing comments.");
        job.setId(99L);
        job.setAccountId("789");
        job.setCandidateOpportunities(Set.of(getCandidateOpportunity()));
        job.setCountry(new Country("Australia", Status.active));
        job.setDescription("This is a description.");
        job.setEmployerEntity(getEmployer());
        job.setExclusionList(getSavedList());
        job.setJobSummary("This is a job summary.");
        job.setOwnerId("321");
        job.setPublishedBy(caller);
        job.setPublishedDate(OffsetDateTime.parse("2023-10-30T12:30:00+02:00"));
        job.setJobCreator(getPartnerImpl());
        job.setStage(JobOpportunityStage.cvReview);
        job.setStarringUsers(Set.of(caller));
        job.setSubmissionDueDate(LocalDate.parse("2020-01-01"));
        job.setSubmissionList(getSavedList());
        job.setSuggestedList(getSavedList());
        job.setSuggestedSearches(Set.of(getSavedSearch()));
        job.setJobOppIntake(getJobOppIntake());
        job.setHiringCommitment(1L);
        job.setOpportunityScore("Opp Score");
        job.setClosed(false);
        job.setWon(false);
        job.setClosingComments(null);
        job.setName("Opp Name");
        job.setNextStep("Next Step");
        job.setNextStepDueDate(LocalDate.parse("2020-01-01"));
        job.setStageOrder(1);
        job.setCreatedBy(caller);
        job.setCreatedDate(OffsetDateTime.parse("2023-10-30T12:30:00+02:00"));
        job.setUpdatedBy(caller);
        job.setUpdatedDate(OffsetDateTime.parse("2023-10-30T12:30:00+02:00"));
        return job;
    }

    static PartnerImpl getPartnerImpl() {
        PartnerImpl partnerImpl = new PartnerImpl();
        partnerImpl.setId(99L);
        partnerImpl.setName("Partner");
        partnerImpl.setAbbreviation("prt");
        partnerImpl.setWebsiteUrl("www.partner.com");
        return partnerImpl;
    }

    static JobOppIntake getJobOppIntake() {
        JobOppIntake joi = new JobOppIntake();
        joi.setId(99L);
        joi.setSalaryRange("80-90k");
        joi.setRecruitmentProcess("The recruitment process.");
        joi.setEmployerCostCommitment("Employer cost commitments.");
        joi.setLocation("Melbourne");
        joi.setLocationDetails("Western suburbs");
        joi.setBenefits("These are the benefits.");
        joi.setLanguageRequirements("These are the language reqs.");
        joi.setEducationRequirements("These are the education reqs.");
        joi.setSkillRequirements("These are the skill reqs.");
        joi.setEmploymentExperience("This is the employment experience.");
        joi.setOccupationCode("Occupation code");
        joi.setMinSalary("80k");
        joi.setVisaPathways("The visa pathways");
        return joi;
    }

  public static JobChat getChat() {
        JobChat chat = new JobChat();
        chat.setId(99L);
        chat.setJobOpp(getJob());
        return chat;
  }

    public static List<JobChat> getListOfChats() {
        JobChat chat1 = getChat();
        JobChat chat2 = getChat();
        chat2.setId(100L);
        JobChat chat3 = getChat();
        chat2.setId(101L);
        return List.of(
            chat1, chat2, chat3
        );
    }

    public static ChatPost getChatPost() {
        ChatPost post = new ChatPost();
        post.setId(199L);
        post.setContent("Post 1");
        return post;
    }

    public static List<ChatPost> getListOfPosts() {
        ChatPost post1 = getChatPost();
        ChatPost post2 = getChatPost();
        post2.setContent("Post 2");
        ChatPost post3 = getChatPost();
        post2.setContent("Post 3");
        return List.of(
            post1, post2, post3
        );
    }

  public static List<Country> getCountries() {
        return List.of(
            new Country("Jordan", Status.active),
            new Country("Pakistan", Status.active),
            new Country("Palestine", Status.active)
        );
  }

    public static List<EducationLevel> getEducationLevels() {
        return List.of(
            new EducationLevel("Excellent", Status.active, 1),
            new EducationLevel("Great", Status.active, 2),
            new EducationLevel("Good", Status.active, 3)
        );
    }

    public static List<EducationMajor> getEducationMajors() {
        return List.of(
            new EducationMajor("Computer Science", Status.active),
            new EducationMajor("Mathematics", Status.active),
            new EducationMajor("Psychology", Status.active)
        );
    }

    public static Employer getEmployer() {
        Employer employer = new Employer();
        employer.setName("ABC Accounts");
        employer.setWebsite("www.ABCAccounts.com");
        employer.setDescription("This is an employer description.");
        employer.setHasHiredInternationally(true);
        return employer;
    }

    public static List<Industry> getIndustries() {
        return List.of(
            new Industry("Tech", Status.active),
            new Industry("Finance", Status.active),
            new Industry("Health", Status.active)
        );
    }

    public static JobChatUserInfo getJobChatUserInfo() {
        JobChatUserInfo info = new JobChatUserInfo();
        info.setLastPostId(123L);
        info.setLastReadPostId(100L);
        return info;
    }

    static OfferToAssist getOfferToAssist() {
        OfferToAssist offerToAssist = new OfferToAssist();
        offerToAssist.setAdditionalNotes("Notes");
        offerToAssist.setPartner(getPartner());

        List<CandidateCouponCode> candidateCouponCodes  = new ArrayList<>();
        CandidateCouponCode ccc = new CandidateCouponCode();
        ccc.setCandidate(getCandidate());
        ccc.setOfferToAssist(offerToAssist);
        ccc.setId(12345678L);
        candidateCouponCodes.add(ccc);

        offerToAssist.setCandidateCouponCodes(candidateCouponCodes);
        offerToAssist.setId(99L);
        offerToAssist.setPublicId("123456");
        offerToAssist.setReason(CandidateAssistanceType.OTHER);
        return offerToAssist;
    }

    public static PartnerImpl getPartner() {
        PartnerImpl partner = new PartnerImpl();
        partner.setId(123L);
        partner.setName("TC Partner");
        partner.setAbbreviation("TCP");
        partner.setJobCreator(true);
        partner.setSourcePartner(true);
        partner.setLogo("logo_url");
        partner.setWebsiteUrl("website_url");
        partner.setRegistrationLandingPage("registration_landing_page");
        partner.setNotificationEmail("notification@email.address");
        partner.setStatus(Status.active);
        return partner;
    }

    public static List<PartnerImpl> getListOfPartners() {
        PartnerImpl partner1 = getPartner();
        PartnerImpl partner2 = getPartner();
        partner2.setName("TC Partner 2");
        PartnerImpl partner3 = getPartner();
        partner3.setName("TC Partner 3");
        return List.of(
          partner1, partner2, partner3
        );
    }

    public static List<SurveyType> getSurveyTypes() {
        SurveyType surveyType1 = new SurveyType("Survey Type One", Status.active);
        SurveyType surveyType2 = new SurveyType("Survey Type Two", Status.inactive);
        return List.of(
            surveyType1, surveyType2
        );
    }

    public static TaskAssignmentImpl getTaskAssignment() {
        TaskAssignmentImpl ta = new TaskAssignmentImpl();
        ta.setId(99L);
        ta.setTask(getTask());
        ta.setStatus(Status.active);
        ta.setDueDate(LocalDate.of(2025, 1, 1));
        return ta;
    }

    public static TaskAssignmentImpl getCompletedTaskAssignment() {
        TaskAssignmentImpl ta = new TaskAssignmentImpl();
        ta.setId(99L);
        ta.setTask(getTask());
        ta.setStatus(Status.active);
        ta.setDueDate(LocalDate.of(2025, 1, 1));
        ta.setCompletedDate(OffsetDateTime.parse("2023-10-30T12:30:00+02:00"));
        ta.setAbandonedDate(OffsetDateTime.parse("2022-10-30T12:30:00+02:00"));
        ta.setCandidateNotes("These are candidate notes.");
        return ta;
    }

    public static List<TaskAssignmentImpl> getTaskAssignments() {
        TaskAssignmentImpl taskAssignment = getCompletedTaskAssignment();
        return List.of(
            taskAssignment
        );
    }

    public static Translation getTranslation() {
        Translation trans = new Translation(
                getUser(),
                1L,
                "Country",
                "French",
                "Australie"
        );
        return trans;
    }

    public static Map<String, Object> getTranslationFile() {
        Map<String, Object> map = new HashMap<>();
        map.put("my key", "my value");
        return map;
    }

    public static User getAdminUser() {
        User u = new User("full_user",
                "full",
                "user",
                "full.user@tbb.org",
                Role.admin);
        u.setJobCreator(true);
        u.setApprover(caller);
        u.setPurpose("Complete intakes");
        u.setSourceCountries(new HashSet<>(List.of(new Country("Jordan", Status.active))));
        u.setReadOnly(false);
        u.setStatus(Status.active);
        u.setCreatedDate(OffsetDateTime.parse("2023-10-30T12:30:00+02:00"));
        u.setCreatedBy(caller);
        u.setLastLogin(OffsetDateTime.parse("2023-10-30T12:30:00+02:00"));
        u.setUsingMfa(true);
        u.setPartner(getPartner());
        return u;
    }

    public static User getSystemAdminUser() {
        User u = new User("system_admin",
            "system",
            "admin",
            "systemadmin@tbb.org",
            Role.systemadmin);
        u.setId(55L);
        u.setJobCreator(false);
        u.setSourceCountries(Collections.emptySet());
        u.setReadOnly(false);
        u.setStatus(Status.active);
        u.setCreatedDate(OffsetDateTime.parse("2023-10-30T12:30:00+02:00"));
        u.setCreatedBy(caller);
        u.setLastLogin(OffsetDateTime.parse("2023-10-30T12:30:00+02:00"));
        u.setUsingMfa(true);
        u.setPartner(getPartner());
        return u;
    }

    public static User getLimitedUser() {
        User u = new User("limited_user",
            "limited",
            "user",
            "limited@tbb.org",
            Role.limited);
        u.setId(58L);
        u.setJobCreator(false);
        u.setSourceCountries(Collections.emptySet());
        u.setReadOnly(false);
        u.setStatus(Status.active);
        u.setCreatedDate(OffsetDateTime.parse("2023-10-30T12:30:00+02:00"));
        u.setCreatedBy(caller);
        u.setLastLogin(OffsetDateTime.parse("2023-10-30T12:30:00+02:00"));
        u.setUsingMfa(true);
        u.setPartner(getPartner());
        return u;
    }

    public static User getCandidateUser() {
        User u = new User("candidate_user",
            "candidate",
            "user",
            "candidate@email.com",
            Role.user);
        u.setStatus(Status.active);
        u.setCreatedDate(OffsetDateTime.parse("2023-10-30T12:30:00+02:00"));
        u.setCreatedBy(caller);
        u.setLastLogin(OffsetDateTime.parse("2023-10-30T12:30:00+02:00"));
        u.setUsingMfa(true);
        u.setPartner(getPartner());
        u.setCandidate(getCandidate());
        return u;
    }


  public static List<SavedSearch> getListOfSavedSearches() {
      SavedSearch savedSearch1 = getSavedSearch();
      SavedSearch savedSearch2 = getSavedSearch();
      savedSearch2.setName("Saved Search 2");
      SavedSearch savedSearch3 = getSavedSearch();
      savedSearch3.setName("Saved Search 3");
      return List.of(
          savedSearch1, savedSearch2, savedSearch3
      );
  }

  static CandidateSavedList getCandidateSavedList() {
        CandidateSavedList csl = new CandidateSavedList();
        csl.setCandidate(getCandidate());
        return csl;
  }

  static Set<CandidateSavedList> getSetOfCandidateSavedLists() {
        Set<CandidateSavedList> scsl = Set.of(getCandidateSavedList());
        return scsl;
  }
  static DuolingoCoupon getDuolingoCoupon() {
        DuolingoCoupon coupon = new DuolingoCoupon();
        coupon.setId(1L);
        coupon.setCouponCode("COUPON123");
        coupon.setExpirationDate(LocalDateTime.now().plusDays(30));
        coupon.setDateSent(LocalDateTime.now().minusDays(1));
        coupon.setCouponStatus(DuolingoCouponStatus.AVAILABLE);

        return coupon;
    }

    /**
     * Holds an {@link UpdateUserRequest} along with the expected {@link User}
     * that should result from applying the request.
     */
    public record CreateUpdateUserTestData(UpdateUserRequest request, User expectedUser) { }

    /**
     * Constructs a {@link CreateUpdateUserTestData} record containing an {@link UpdateUserRequest}
     * and the expected {@link User} that should result from using it.
     */
    public static CreateUpdateUserTestData createUpdateUserRequestAndExpectedUser() {
        final String email = "alice@email.com";
        final String firstName = "Alice";
        final String lastName = "Alison";
        final String password = "password";
        final boolean readOnly = false;
        final Role role = Role.admin;
        final boolean jobCreator = false;
        final String purpose = "Testing";
        final Status status = Status.active;
        final String username = "aalison";
        final boolean usingMfa = true;

        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail(email);
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setPassword(password);
        request.setReadOnly(readOnly);
        request.setRole(role);
        request.setJobCreator(jobCreator);
        request.setPurpose(purpose);
        request.setStatus(status);
        request.setUsername(username);
        request.setUsingMfa(usingMfa);
        request.setPartnerId(1L);

        User expectedUser = new User();
        expectedUser.setEmail(email);
        expectedUser.setFirstName(firstName);
        expectedUser.setLastName(lastName);
        expectedUser.setReadOnly(readOnly);
        expectedUser.setRole(role);
        expectedUser.setJobCreator(jobCreator);
        expectedUser.setPurpose(purpose);
        expectedUser.setStatus(status);
        expectedUser.setUsername(username);
        expectedUser.setUsingMfa(usingMfa);
        expectedUser.setPasswordEnc(password);

        return new CreateUpdateUserTestData(request, expectedUser);
    }
}
