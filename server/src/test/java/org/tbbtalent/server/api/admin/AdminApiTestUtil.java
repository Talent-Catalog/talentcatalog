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

package org.tbbtalent.server.api.admin;

import org.tbbtalent.server.model.db.*;

import java.time.LocalDate;
import java.util.List;

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
        return new SalesforceJobOpp();
    }
}
