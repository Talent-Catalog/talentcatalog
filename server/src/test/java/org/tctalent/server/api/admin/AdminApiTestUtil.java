/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import org.tctalent.server.model.db.Candidate;
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
import org.tctalent.server.model.db.CandidateOpportunityStage;
import org.tctalent.server.model.db.CandidateReviewStatusItem;
import org.tctalent.server.model.db.CandidateSkill;
import org.tctalent.server.model.db.CandidateVisaCheck;
import org.tctalent.server.model.db.CandidateVisaJobCheck;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.DependantRelations;
import org.tctalent.server.model.db.DocumentStatus;
import org.tctalent.server.model.db.EducationLevel;
import org.tctalent.server.model.db.EducationMajor;
import org.tctalent.server.model.db.EducationType;
import org.tctalent.server.model.db.Employer;
import org.tctalent.server.model.db.Exam;
import org.tctalent.server.model.db.ExportColumn;
import org.tctalent.server.model.db.FamilyRelations;
import org.tctalent.server.model.db.Gender;
import org.tctalent.server.model.db.HasPassport;
import org.tctalent.server.model.db.Industry;
import org.tctalent.server.model.db.JobOppIntake;
import org.tctalent.server.model.db.JobOpportunityStage;
import org.tctalent.server.model.db.Language;
import org.tctalent.server.model.db.LanguageLevel;
import org.tctalent.server.model.db.NoteType;
import org.tctalent.server.model.db.Occupation;
import org.tctalent.server.model.db.OtherVisas;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.Registration;
import org.tctalent.server.model.db.ReviewStatus;
import org.tctalent.server.model.db.RiskLevel;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.SavedSearch;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.SurveyType;
import org.tctalent.server.model.db.SystemLanguage;
import org.tctalent.server.model.db.TBBEligibilityAssessment;
import org.tctalent.server.model.db.TaskImpl;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.VisaEligibility;
import org.tctalent.server.model.db.YesNo;
import org.tctalent.server.model.db.YesNoUnsure;
import org.tctalent.server.model.sf.Opportunity;
import org.tctalent.server.request.candidate.PublishedDocColumnProps;

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
        destination.setFamily(FamilyRelations.Cousin);
        destination.setLocation("New York");
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
        return opportunity;
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
            candidateVisaJobCheck.setEnglishThresholdNotes("These are some english threshold notes.");
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
        savedSearch.setName("My Search");
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
        task.setHelpLink("http://help.link");
        return task;
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
        savedList.setFolderlink("http://folder.link");
        savedList.setFolderjdlink("http://folder.jd.link");
        savedList.setPublishedDocLink("http://published.doc.link");
        savedList.setRegisteredJob(true);
        savedList.setTbbShortName("Saved list Tbb short name");
        savedList.setCreatedBy(caller);
        savedList.setCreatedDate(OffsetDateTime.parse("2023-10-30T12:30:00+02:00"));
        savedList.setUpdatedBy(caller);
        savedList.setUpdatedDate(OffsetDateTime.parse("2023-10-30T12:30:00+02:00"));
        savedList.setUsers(Set.of(caller));
        savedList.setTasks(Set.of(getTask()));

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

    public static PartnerImpl getPartner() {
        PartnerImpl partner = new PartnerImpl();
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

}
