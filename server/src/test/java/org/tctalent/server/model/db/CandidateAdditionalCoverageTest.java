/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.model.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.tctalent.server.configuration.SystemAdminConfiguration;

class CandidateAdditionalCoverageTest {

  @Test
  void getEducationsSummaryCoversMajorCourseNameTypeOnlyAndMajorFallback() {
    Candidate candidate = new Candidate();

    EducationMajor computerScience = new EducationMajor("Computer Science", Status.active);
    EducationMajor emptyMajor = new EducationMajor();

    CandidateEducation withTypeAndMajor = new CandidateEducation(
        null, EducationType.Bachelor, null, computerScience,
        null, null, null, null, null
    );
    CandidateEducation withTypeAndCourse = new CandidateEducation(
        null, EducationType.Masters, null, null,
        null, null, "Data Science", null, null
    );
    CandidateEducation withTypeOnly = new CandidateEducation(
        null, EducationType.Doctoral, null, null,
        null, null, null, null, null
    );
    CandidateEducation withMajorFallbackToCourse = new CandidateEducation(
        null, null, null, emptyMajor,
        null, null, "Nursing", null, null
    );

    candidate.setCandidateEducations(List.of(
        withTypeAndMajor,
        withTypeAndCourse,
        withTypeOnly,
        withMajorFallbackToCourse
    ));

    assertEquals(
        "Bachelor in Computer Science, Masters in Data Science, Doctoral, Nursing",
        candidate.getEducationsSummary()
    );
  }

  @Test
  void getEnglishExamsSummaryCoversScoreNoScoreOtherExamAndSkippedNullOtherExam() {
    Candidate candidate = new Candidate();

    candidate.setCandidateExams(List.of(
        exam(Exam.IELTSGen, "7.5", null),
        exam(Exam.TOEFL, null, null),
        exam(Exam.Other, "A", "Cambridge"),
        exam(Exam.Other, "ignored", null)
    ));

    assertEquals(
        "IELTSGen: 7.5, TOEFL, Cambridge: A, ",
        candidate.getEnglishExamsSummary()
    );
  }

  @Test
  void getOetScoreMethodsReturnMatchingExamScoresAndNullWhenMissing() {
    Candidate candidate = new Candidate();

    candidate.setCandidateExams(List.of(
        exam(Exam.TOEFL, "100", null),
        exam(Exam.OET, "B", null),
        exam(Exam.OETRead, "A", null),
        exam(Exam.OETList, "C+", null),
        exam(Exam.OETLang, "B+", null)
    ));

    assertEquals("B", candidate.getOetOverall());
    assertEquals("A", candidate.getOetReading());
    assertEquals("C+", candidate.getOetListening());
    assertEquals("B+", candidate.getOetLanguage());

    Candidate emptyCandidate = new Candidate();
    emptyCandidate.setCandidateExams(List.of(exam(Exam.TOEFL, "100", null)));

    assertNull(emptyCandidate.getOetOverall());
    assertNull(emptyCandidate.getOetReading());
    assertNull(emptyCandidate.getOetListening());
    assertNull(emptyCandidate.getOetLanguage());
  }

  @Test
  void getOccupationSummaryJoinsOccupationNames() {
    Candidate candidate = new Candidate();

    CandidateOccupation developer = candidateOccupation("Software Developer");
    CandidateOccupation nurse = candidateOccupation("Nurse");

    candidate.setCandidateOccupations(List.of(developer, nurse));

    assertEquals("Software Developer, Nurse", candidate.getOccupationSummary());
  }

  @Test
  void getCertificationsSummaryJoinsCertificationDetails() {
    Candidate candidate = new Candidate();

    CandidateCertification aws = new CandidateCertification(
        null, "AWS", "Amazon", LocalDate.of(2020, 5, 1)
    );
    CandidateCertification pmp = new CandidateCertification(
        null, "PMP", "PMI", LocalDate.of(2021, 6, 1)
    );

    candidate.setCandidateCertifications(List.of(aws, pmp));

    assertEquals(
        "AWS: Amazon 2020, PMP: PMI 2021",
        candidate.getCertificationsSummary()
    );
  }

  @Test
  void getNumberDependantsReturnsNullForNullOrEmptyAndCountForDependants() {
    Candidate candidate = new Candidate();

    candidate.setCandidateDependants(null);
    assertNull(candidate.getNumberDependants());

    candidate.setCandidateDependants(List.of());
    assertNull(candidate.getNumberDependants());

    CandidateDependant first = mock(CandidateDependant.class);
    CandidateDependant second = mock(CandidateDependant.class);

    candidate.setCandidateDependants(List.of(first, second));

    assertEquals(2L, candidate.getNumberDependants());
  }

  @Test
  void topLevelIntakeCompletedUsesFullBeforeMiniAndReturnsDashWhenMissing() {
    Candidate candidate = new Candidate();

    assertEquals("-", candidate.getTopLevelIntakeCompleted());
    assertEquals("", candidate.getTopLevelIntakeCompletedDate());

    candidate.setMiniIntakeCompletedDate(OffsetDateTime.parse("2026-01-15T10:15:30Z"));

    assertEquals("Mini", candidate.getTopLevelIntakeCompleted());
    assertEquals("2026-01-1", candidate.getTopLevelIntakeCompletedDate());

    candidate.setFullIntakeCompletedDate(OffsetDateTime.parse("2026-02-20T10:15:30Z"));

    assertEquals("Full", candidate.getTopLevelIntakeCompleted());
    assertEquals("2026-02-2", candidate.getTopLevelIntakeCompletedDate());
  }

  @Test
  void contextSavedListMethodsReturnMatchingContextValues() {
    Candidate candidate = new Candidate();

    SavedList savedList = mock(SavedList.class);
    CandidateSavedList candidateSavedList = mock(CandidateSavedList.class);
    CandidateAttachment shareableCv = mock(CandidateAttachment.class);
    CandidateAttachment shareableDoc = mock(CandidateAttachment.class);

    when(savedList.getId()).thenReturn(77L);
    when(candidateSavedList.getSavedList()).thenReturn(savedList);
    when(candidateSavedList.getContextNote()).thenReturn("Important context");
    when(candidateSavedList.getShareableCv()).thenReturn(shareableCv);
    when(candidateSavedList.getShareableDoc()).thenReturn(shareableDoc);

    candidate.setCandidateSavedLists(Set.of(candidateSavedList));
    candidate.setContextSavedListId(77L);

    assertEquals("Important context", candidate.getContextNote());
    assertSame(shareableCv, candidate.getListShareableCv());
    assertSame(shareableDoc, candidate.getListShareableDoc());
  }

  @Test
  void contextSavedListMethodsReturnNullWhenNoContextOrNoMatch() {
    Candidate candidate = new Candidate();

    SavedList savedList = mock(SavedList.class);
    CandidateSavedList candidateSavedList = mock(CandidateSavedList.class);

    when(savedList.getId()).thenReturn(77L);
    when(candidateSavedList.getSavedList()).thenReturn(savedList);

    candidate.setCandidateSavedLists(Set.of(candidateSavedList));

    assertNull(candidate.getContextNote());
    assertNull(candidate.getListShareableCv());
    assertNull(candidate.getListShareableDoc());

    candidate.setContextSavedListId(88L);

    assertNull(candidate.getContextNote());
    assertNull(candidate.getListShareableCv());
    assertNull(candidate.getListShareableDoc());
  }

  @Test
  void savedListHelpersAddListsAndDetectTags() {
    Candidate candidate = new Candidate();
    candidate.setId(123L);

    SavedList testCandidatesList = savedList(SystemAdminConfiguration.TEST_CANDIDATE_LIST_ID);
    SavedList pendingTermsList = savedList(SystemAdminConfiguration.PENDING_TERMS_ACCEPTANCE_LIST_ID);

    candidate.addSavedLists(Set.of(testCandidatesList, pendingTermsList));

    assertTrue(candidate.isTestCandidate());
    assertTrue(candidate.isPendingTerms());
    assertTrue(candidate.isTagged(SystemAdminConfiguration.TEST_CANDIDATE_LIST_ID));
    assertEquals(1, candidate.getSavedLists().size());
  }

  @Test
  void setCandidateSavedListsLinksBackToCandidate() {
    Candidate candidate = new Candidate();

    CandidateSavedList candidateSavedList = mock(CandidateSavedList.class);

    candidate.setCandidateSavedLists(Set.of(candidateSavedList));

    verify(candidateSavedList).setCandidate(candidate);
  }

  @Test
  void parentLinkSettersSetCandidateOnChildren() {
    Candidate candidate = new Candidate();

    CandidateNote note = mock(CandidateNote.class);
    CandidateOpportunity opportunity = mock(CandidateOpportunity.class);
    CandidateEducation education = mock(CandidateEducation.class);
    CandidateLanguage language = mock(CandidateLanguage.class);
    CandidateJobExperience jobExperience = mock(CandidateJobExperience.class);
    CandidateCertification certification = mock(CandidateCertification.class);
    CandidateReviewStatusItem reviewStatusItem = mock(CandidateReviewStatusItem.class);
    CandidateSkill skill = mock(CandidateSkill.class);
    CandidateAttachment attachment = mock(CandidateAttachment.class);
    CandidateCitizenship citizenship = mock(CandidateCitizenship.class);
    CandidateExam exam = mock(CandidateExam.class);
    CandidateVisaCheck visaCheck = mock(CandidateVisaCheck.class);
    CandidateDependant dependant = mock(CandidateDependant.class);
    CandidateDestination destination = mock(CandidateDestination.class);
    CandidateOccupation occupation = mock(CandidateOccupation.class);

    candidate.setCandidateNotes(List.of(note));
    candidate.setCandidateOpportunities(List.of(opportunity));
    candidate.setCandidateEducations(List.of(education));
    candidate.setCandidateLanguages(List.of(language));
    candidate.setCandidateJobExperiences(List.of(jobExperience));
    candidate.setCandidateCertifications(List.of(certification));
    candidate.setCandidateReviewStatusItems(Set.of(reviewStatusItem));
    candidate.setCandidateSkills(List.of(skill));
    candidate.setCandidateAttachments(List.of(attachment));
    candidate.setCandidateCitizenships(List.of(citizenship));
    candidate.setCandidateExams(List.of(exam));
    candidate.setCandidateVisaChecks(List.of(visaCheck));
    candidate.setCandidateDependants(List.of(dependant));
    candidate.setCandidateDestinations(List.of(destination));
    candidate.setCandidateOccupations(List.of(occupation));

    verify(note).setCandidate(candidate);
    verify(opportunity).setCandidate(candidate);
    verify(education).setCandidate(candidate);
    verify(language).setCandidate(candidate);
    verify(jobExperience).setCandidate(candidate);
    verify(certification).setCandidate(candidate);
    verify(reviewStatusItem).setCandidate(candidate);
    verify(skill).setCandidate(candidate);
    verify(attachment).setCandidate(candidate);
    verify(citizenship).setCandidate(candidate);
    verify(exam).setCandidate(candidate);
    verify(visaCheck).setCandidate(candidate);
    verify(dependant).setCandidate(candidate);
    verify(destination).setCandidate(candidate);
    verify(occupation).setCandidate(candidate);
  }

  @Test
  void getUnhcrRegisteredMapsUnhcrStatusValues() {
    Candidate candidate = new Candidate();

    assertEquals(YesNoUnsure.NoResponse, candidate.getUnhcrRegistered());

    candidate.setUnhcrStatus(UnhcrStatus.NotRegistered);
    assertEquals(YesNoUnsure.No, candidate.getUnhcrRegistered());

    candidate.setUnhcrStatus(UnhcrStatus.NA);
    assertEquals(YesNoUnsure.No, candidate.getUnhcrRegistered());

    candidate.setUnhcrStatus(UnhcrStatus.RegisteredAsylum);
    assertEquals(YesNoUnsure.Yes, candidate.getUnhcrRegistered());

    candidate.setUnhcrStatus(UnhcrStatus.RegisteredStateless);
    assertEquals(YesNoUnsure.Yes, candidate.getUnhcrRegistered());

    candidate.setUnhcrStatus(UnhcrStatus.RegisteredStatusUnknown);
    assertEquals(YesNoUnsure.Yes, candidate.getUnhcrRegistered());

    candidate.setUnhcrStatus(UnhcrStatus.MandateRefugee);
    assertEquals(YesNoUnsure.Yes, candidate.getUnhcrRegistered());

    candidate.setUnhcrStatus(UnhcrStatus.Unsure);
    assertEquals(YesNoUnsure.Unsure, candidate.getUnhcrRegistered());

    candidate.setUnhcrStatus(UnhcrStatus.NoResponse);
    assertEquals(YesNoUnsure.NoResponse, candidate.getUnhcrRegistered());
  }

  @Test
  void setUnhcrRegisteredUpdatesOnlyWhenCurrentStatusAllowsIt() {
    Candidate candidate = new Candidate();

    candidate.setUnhcrRegistered(null);
    assertEquals(UnhcrStatus.NoResponse, candidate.getUnhcrStatus());

    candidate.setUnhcrStatus(null);
    candidate.setUnhcrRegistered(YesNoUnsure.Yes);
    assertEquals(UnhcrStatus.RegisteredStatusUnknown, candidate.getUnhcrStatus());

    candidate.setUnhcrStatus(UnhcrStatus.NoResponse);
    candidate.setUnhcrRegistered(YesNoUnsure.No);
    assertEquals(UnhcrStatus.NotRegistered, candidate.getUnhcrStatus());

    candidate.setUnhcrStatus(UnhcrStatus.Unsure);
    candidate.setUnhcrRegistered(YesNoUnsure.Unsure);
    assertEquals(UnhcrStatus.Unsure, candidate.getUnhcrStatus());

    candidate.setUnhcrStatus(UnhcrStatus.MandateRefugee);
    candidate.setUnhcrRegistered(YesNoUnsure.No);
    assertEquals(UnhcrStatus.MandateRefugee, candidate.getUnhcrStatus());
  }

  @Test
  void setFolderlinkConvertsBlankToNullButKeepsRealLink() {
    Candidate candidate = new Candidate();

    candidate.setFolderlink("   ");
    assertNull(candidate.getFolderlink());

    candidate.setFolderlink("https://drive.google.com/folder/test");
    assertEquals("https://drive.google.com/folder/test", candidate.getFolderlink());
  }

  @Test
  void updateTextCombinesJobDescriptionsCvTextAndShareableNotes() {
    Candidate candidate = new Candidate();

    CandidateJobExperience javaJob = mock(CandidateJobExperience.class);
    CandidateJobExperience qaJob = mock(CandidateJobExperience.class);

    when(javaJob.getDescription()).thenReturn("Java developer");
    when(qaJob.getDescription()).thenReturn("QA tester");

    CandidateAttachment cvAttachment = mock(CandidateAttachment.class);
    CandidateAttachment nonCvAttachment = mock(CandidateAttachment.class);

    when(cvAttachment.isCv()).thenReturn(true);
    when(cvAttachment.getTextExtract()).thenReturn("CV text extract");
    when(nonCvAttachment.isCv()).thenReturn(false);

    candidate.setCandidateJobExperiences(List.of(javaJob, qaJob));
    candidate.setCandidateAttachments(List.of(cvAttachment, nonCvAttachment));
    candidate.setShareableNotes("Strong communication skills");

    candidate.updateText();

    assertEquals(
        "Java developer || QA tester || CV text extract || Strong communication skills",
        candidate.getText()
    );
  }

  private CandidateExam exam(Exam exam, String score, String otherExam) {
    CandidateExam candidateExam = mock(CandidateExam.class);
    when(candidateExam.getExam()).thenReturn(exam);
    when(candidateExam.getScore()).thenReturn(score);
    if (exam == Exam.Other) {
      when(candidateExam.getOtherExam()).thenReturn(otherExam);
    }
    return candidateExam;
  }

  private CandidateOccupation candidateOccupation(String occupationName) {
    Occupation occupation = mock(Occupation.class);
    CandidateOccupation candidateOccupation = mock(CandidateOccupation.class);

    when(occupation.getName()).thenReturn(occupationName);
    when(candidateOccupation.getOccupation()).thenReturn(occupation);

    return candidateOccupation;
  }

  private SavedList savedList(Long id) {
    SavedList savedList = mock(SavedList.class);

    when(savedList.getId()).thenReturn(id);
    when(savedList.getCandidateSavedLists()).thenReturn(new HashSet<>());

    return savedList;
  }
}