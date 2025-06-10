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

package org.tctalent.server.data;

import static org.tctalent.server.data.PartnerImplTestData.getSourcePartner;
import static org.tctalent.server.data.UserTestData.getAuditUser;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
import org.tctalent.server.model.db.CandidateReviewStatusItem;
import org.tctalent.server.model.db.CandidateSkill;
import org.tctalent.server.model.db.CandidateStatus;
import org.tctalent.server.model.db.CandidateVisaCheck;
import org.tctalent.server.model.db.CandidateVisaJobCheck;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.DependantRelations;
import org.tctalent.server.model.db.DocumentStatus;
import org.tctalent.server.model.db.EducationLevel;
import org.tctalent.server.model.db.EducationMajor;
import org.tctalent.server.model.db.EducationType;
import org.tctalent.server.model.db.Exam;
import org.tctalent.server.model.db.FamilyRelations;
import org.tctalent.server.model.db.Gender;
import org.tctalent.server.model.db.HasPassport;
import org.tctalent.server.model.db.Industry;
import org.tctalent.server.model.db.Language;
import org.tctalent.server.model.db.LanguageLevel;
import org.tctalent.server.model.db.NoteType;
import org.tctalent.server.model.db.Occupation;
import org.tctalent.server.model.db.OfferToAssist;
import org.tctalent.server.model.db.OtherVisas;
import org.tctalent.server.model.db.Registration;
import org.tctalent.server.model.db.ReviewStatus;
import org.tctalent.server.model.db.RiskLevel;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.SavedSearch;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.SurveyType;
import org.tctalent.server.model.db.TBBEligibilityAssessment;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.VisaEligibility;
import org.tctalent.server.model.db.YesNo;
import org.tctalent.server.model.db.YesNoUnsure;

public class CandidateTestData {

    private static final User candidate1user =
        new User("candidate1",
            "test",
            "candidate1",
            "test.candidate1@some.thing",
            Role.user);

    private static final User candidate2user =
        new User("candidate2",
            "test",
            "candidate2",
            "test.candidate2@some.thing",
            Role.user);

    private static final User candidate3user =
        new User("candidate3",
            "test",
            "candidate3",
            "test.candidate3@some.thing",
            Role.user);

    public static Candidate getCandidate() {
        Candidate candidate = new Candidate(
            candidate1user,
            "+123-456-789",
            "+123-456-789",
            getAuditUser()
        );
        candidate.setId(99L);
        candidate.setNationality(CountryTestData.PAKISTAN);
        candidate.setStatus(CandidateStatus.active);
        return candidate;
    }

    public static List<Candidate> getListOfCandidates() {
        return List.of(
            new Candidate(candidate1user, "+123-456-789", "+123-456-789", getAuditUser()),
            new Candidate(candidate2user, "+234-567-890", "+123-456-789", getAuditUser()),
            new Candidate(candidate3user, "+345-678-901", "+345-678-901", getAuditUser())
        );
    }

    public static List<CandidateCertification> getListOfCandidateCertifications() {
        return List.of(getCandidateCertification());
    }

    public static CandidateCertification getCandidateCertification() {
        return new CandidateCertification(
            getCandidate(),
            "BA",
            "Cambridge",
            LocalDate.of(1998, 5, 1)
        );
    }

    public static CandidateCitizenship getCandidateCitizenship() {
        CandidateCitizenship citizenship = new CandidateCitizenship();
        citizenship.setCandidate(getCandidate());
        citizenship.setHasPassport(HasPassport.ValidPassport);
        citizenship.setPassportExp(LocalDate.of(2035, 12, 25));
        citizenship.setNationality(new Country("Pakistan", Status.active));
        citizenship.setNotes("Some example citizenship notes");
        return citizenship;
    }

    public static CandidateDependant getCandidateDependant() {
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

    public static CandidateDestination getCandidateDestination() {
        CandidateDestination destination = new CandidateDestination();
        destination.setCandidate(getCandidate());
        destination.setCountry(new Country("USA", Status.active));
        destination.setInterest(YesNoUnsure.Yes);
        destination.setNotes("Some destination notes");
        return destination;
    }

    public static List<CandidateEducation> getListOfCandidateEducations() {
        return List.of(getCandidateEducation());
    }

    public static CandidateEducation getCandidateEducation() {
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

    public static CandidateExam getCandidateExam() {
        CandidateExam exam = new CandidateExam();
        exam.setCandidate(getCandidate());
        exam.setExam(Exam.IELTSGen);
        exam.setOtherExam("IELTS");
        exam.setScore("100");
        exam.setYear(2023L);
        exam.setNotes("Some exam notes");
        return exam;
    }

    public static CandidateJobExperience getCandidateJobExperience() {
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

    public static List<Occupation> getListOfOccupations() {
        return List.of(
            new Occupation("Builder", Status.active),
            new Occupation("Baker", Status.active)
        );
    }

    public static List<CandidateOccupation> getListOfCandidateOccupations() {
        return List.of(getCandidateOccupation());
    }

    public static CandidateOccupation getCandidateOccupation() {
        return new CandidateOccupation(
            getCandidate(),
            new Occupation("Software Engineer", Status.active),
            10L
        );
    }

    public static List<CandidateLanguage> getListOfCandidateLanguages() {
        return List.of(getCandidateLanguage());
    }

    public static CandidateLanguage getCandidateLanguage() {
        return new CandidateLanguage(
            getCandidate(),
            new Language("Arabic", Status.active),
            new LanguageLevel("Good", Status.active, 9),
            new LanguageLevel("Good", Status.active, 9)
        );
    }

    public static CandidateNote getCandidateNote() {
        CandidateNote candidateNote = new CandidateNote();
        candidateNote.setCandidate(getCandidate());
        candidateNote.setTitle("A title");
        candidateNote.setComment("Some comments");
        candidateNote.setNoteType(NoteType.candidate);
        return candidateNote;
    }

    public static CandidateReviewStatusItem getCandidateReviewStatusItem() {
        CandidateReviewStatusItem reviewStatusItem = new CandidateReviewStatusItem();
        reviewStatusItem.setCandidate(getCandidate());
        reviewStatusItem.setSavedSearch(new SavedSearch());
        reviewStatusItem.setComment("A review comment");
        reviewStatusItem.setReviewStatus(ReviewStatus.verified);
        return reviewStatusItem;
    }


    public static CandidateVisaCheck getCandidateVisaCheck(boolean completed) {
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

    public static CandidateVisaJobCheck getCandidateVisaJobCheck(boolean completed) {
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

    public static CandidateSkill getCandidateSkill() {
        CandidateSkill candidateSkill = new CandidateSkill();
        candidateSkill.setCandidate(getCandidate());
        candidateSkill.setId(1L);
        candidateSkill.setSkill("Adobe Photoshop");
        candidateSkill.setTimePeriod("3-5 years");
        return candidateSkill;
    }

    public static Occupation getOccupation() {
        Occupation occupation = new Occupation("Nurse", Status.active);
        occupation.setId(1L);
        return occupation;
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

    public static List<Industry> getIndustries() {
        return List.of(
            new Industry("Tech", Status.active),
            new Industry("Finance", Status.active),
            new Industry("Health", Status.active)
        );
    }

    public static OfferToAssist getOfferToAssist() {
        OfferToAssist offerToAssist = new OfferToAssist();
        offerToAssist.setAdditionalNotes("Notes");
        offerToAssist.setPartner(getSourcePartner());

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

    public static List<SurveyType> getSurveyTypes() {
        SurveyType surveyType1 = new SurveyType("Survey Type One", Status.active);
        SurveyType surveyType2 = new SurveyType("Survey Type Two", Status.inactive);
        return List.of(
            surveyType1, surveyType2
        );
    }
}
