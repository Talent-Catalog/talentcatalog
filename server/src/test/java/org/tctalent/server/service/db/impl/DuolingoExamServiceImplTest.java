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

package org.tctalent.server.service.db.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateExam;
import org.tctalent.server.model.db.DuolingoCouponStatus;
import org.tctalent.server.model.db.Exam;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.TaskAssignmentImpl;
import org.tctalent.server.model.db.TaskImpl;
import org.tctalent.server.request.candidate.exam.CreateCandidateExamRequest;
import org.tctalent.server.request.duolingo.DuolingoExtraFieldsRequest;
import org.tctalent.server.response.DuolingoDashboardResponse;
import org.tctalent.server.service.db.CandidateExamService;
import org.tctalent.server.service.db.DuolingoApiService;
import org.tctalent.server.service.db.DuolingoCouponService;
import org.tctalent.server.service.db.DuolingoExtraFieldsService;
import org.tctalent.server.service.db.TaskAssignmentService;
import org.tctalent.server.service.db.TaskService;

@ExtendWith(MockitoExtension.class)
class DuolingoExamServiceImplTest {

  // Test constants
  private static final String COUPON_CODE = "TEST123";
  private static final Long CANDIDATE_ID = 456L;
  private static final Long SAVED_EXAM_ID = 789L;
  private static final String TEST_DATE = "2024-06-15";
  private static final int OVERALL_SCORE = 120;
  private static final String CERTIFICATE_URL = "https://example.com/certificate";
  private static final String INTERVIEW_URL = "https://example.com/interview";
  private static final String VERIFICATION_DATE = "2024-06-16";
  private static final int PERCENT_SCORE = 85;
  private static final int SCALE = 160;
  private static final int LITERACY_SUBSCORE = 115;
  private static final int CONVERSATION_SUBSCORE = 125;
  private static final int COMPREHENSION_SUBSCORE = 120;
  private static final int PRODUCTION_SUBSCORE = 118;
  private static final String TASK_NAME = "duolingoTest";
  private static final Long TASK_ID = 101L;
  private static final Long TASK_ASSIGNMENT_ID = 102L;

  @Mock private DuolingoApiService duolingoApiService;
  @Mock private DuolingoCouponService duolingoCouponService;
  @Mock private CandidateExamService candidateExamService;
  @Mock private DuolingoExtraFieldsService duolingoExtraFieldsService;
  @Mock private TaskAssignmentService taskAssignmentService;
  @Mock private TaskService taskService;

  @InjectMocks
  DuolingoExamServiceImpl duolingoExamService;

  private DuolingoDashboardResponse dashboardResponse;
  private Candidate candidate;
  private CandidateExam existingExam;
  private CandidateExam savedExam;
  private TaskImpl duolingoTask;
  private TaskAssignmentImpl taskAssignment;

  @BeforeEach
  void setUp() {
    // Set up dashboard response
    dashboardResponse = new DuolingoDashboardResponse();
    dashboardResponse.setCouponId(COUPON_CODE);
    dashboardResponse.setTestDate(TEST_DATE);
    dashboardResponse.setOverallScore(OVERALL_SCORE);
    dashboardResponse.setCertificateUrl(CERTIFICATE_URL);
    dashboardResponse.setInterviewUrl(INTERVIEW_URL);
    dashboardResponse.setVerificationDate(VERIFICATION_DATE);
    dashboardResponse.setPercentScore(PERCENT_SCORE);
    dashboardResponse.setScale(SCALE);
    dashboardResponse.setLiteracySubscore(LITERACY_SUBSCORE);
    dashboardResponse.setConversationSubscore(CONVERSATION_SUBSCORE);
    dashboardResponse.setComprehensionSubscore(COMPREHENSION_SUBSCORE);
    dashboardResponse.setProductionSubscore(PRODUCTION_SUBSCORE);

    // Set up candidate
    candidate = new Candidate();
    candidate.setId(CANDIDATE_ID);

    // Set up existing exam
    existingExam = new CandidateExam();
    existingExam.setId(123L);
    existingExam.setExam(Exam.DETOfficial);
    existingExam.setScore("120");
    existingExam.setYear(2024L);
    existingExam.setNotes("Different notes");

    // Set up saved exam
    savedExam = new CandidateExam();
    savedExam.setId(SAVED_EXAM_ID);
    savedExam.setExam(Exam.DETOfficial);
    savedExam.setScore("120");
    savedExam.setYear(2024L);

    // Set up task and task assignment
    duolingoTask = new TaskImpl();
    duolingoTask.setId(TASK_ID);
    duolingoTask.setName(TASK_NAME);

    taskAssignment = new TaskAssignmentImpl();
    taskAssignment.setId(TASK_ASSIGNMENT_ID);
    taskAssignment.setTask(duolingoTask);
    taskAssignment.setCandidate(candidate);
    taskAssignment.setStatus(Status.active);
  }

  @Test
  @DisplayName("should create new exam when no identical exam exists")
  void updateCandidateExams_shouldCreateNewExam_whenNoIdenticalExamExists() throws NoSuchObjectException {
    // Given
    List<DuolingoDashboardResponse> dashboardResults = List.of(dashboardResponse);
    List<CandidateExam> existingExams = List.of(existingExam); // Different notes, so not identical

    given(duolingoApiService.getDashboardResults(null, null))
        .willReturn(dashboardResults);
    given(duolingoCouponService.findCandidateByCouponCode(COUPON_CODE))
        .willReturn(candidate);
    given(candidateExamService.list(CANDIDATE_ID))
        .willReturn(existingExams);
    given(candidateExamService.createExam(eq(CANDIDATE_ID), any(CreateCandidateExamRequest.class)))
        .willReturn(savedExam);
    given(taskService.getByName(TASK_NAME))
        .willReturn(duolingoTask);
    given(taskAssignmentService.findByTaskIdAndCandidateIdAndStatus(TASK_ID, CANDIDATE_ID, Status.active))
        .willReturn(List.of(taskAssignment));

    // When
    duolingoExamService.updateCandidateExams();

    // Then
    verify(candidateExamService).createExam(eq(CANDIDATE_ID), any(CreateCandidateExamRequest.class));
    verify(duolingoCouponService).updateCouponStatus(COUPON_CODE, DuolingoCouponStatus.REDEEMED);
    verify(duolingoExtraFieldsService).createOrUpdateDuolingoExtraFields(any(DuolingoExtraFieldsRequest.class));
    verify(taskAssignmentService).completeTaskAssignment(taskAssignment);

    // Verify the exam request was created correctly
    ArgumentCaptor<CreateCandidateExamRequest> examRequestCaptor =
        ArgumentCaptor.forClass(CreateCandidateExamRequest.class);
    verify(candidateExamService).createExam(eq(CANDIDATE_ID), examRequestCaptor.capture());

    CreateCandidateExamRequest examRequest = examRequestCaptor.getValue();
    assertEquals(Exam.DETOfficial, examRequest.getExam());
    assertEquals("120", examRequest.getScore());
    assertEquals(Long.valueOf(2024), examRequest.getYear());

    // Verify the DuolingoExtraFields request was created correctly
    ArgumentCaptor<DuolingoExtraFieldsRequest> extraFieldsCaptor =
        ArgumentCaptor.forClass(DuolingoExtraFieldsRequest.class);
    verify(duolingoExtraFieldsService).createOrUpdateDuolingoExtraFields(extraFieldsCaptor.capture());

    DuolingoExtraFieldsRequest extraFieldsRequest = extraFieldsCaptor.getValue();
    assertEquals(CERTIFICATE_URL, extraFieldsRequest.getCertificateUrl());
    assertEquals(INTERVIEW_URL, extraFieldsRequest.getInterviewUrl());
    assertEquals(VERIFICATION_DATE, extraFieldsRequest.getVerificationDate());
    assertEquals(PERCENT_SCORE, extraFieldsRequest.getPercentScore());
    assertEquals(SCALE, extraFieldsRequest.getScale());
    assertEquals(LITERACY_SUBSCORE, extraFieldsRequest.getLiteracySubscore());
    assertEquals(CONVERSATION_SUBSCORE, extraFieldsRequest.getConversationSubscore());
    assertEquals(COMPREHENSION_SUBSCORE, extraFieldsRequest.getComprehensionSubscore());
    assertEquals(PRODUCTION_SUBSCORE, extraFieldsRequest.getProductionSubscore());
    assertEquals(SAVED_EXAM_ID, extraFieldsRequest.getCandidateExamId());
  }

  @Test
  @DisplayName("should skip exam creation when identical exam already exists")
  void updateCandidateExams_shouldSkipExamCreation_whenIdenticalExamExists() throws NoSuchObjectException {
    // Given
    String expectedNotes = buildExpectedNotes();
    CandidateExam identicalExam = new CandidateExam();
    identicalExam.setExam(Exam.DETOfficial);
    identicalExam.setScore("120");
    identicalExam.setYear(2024L);
    identicalExam.setNotes(expectedNotes);

    List<DuolingoDashboardResponse> dashboardResults = List.of(dashboardResponse);
    List<CandidateExam> existingExams = List.of(identicalExam);

    given(duolingoApiService.getDashboardResults(null, null))
        .willReturn(dashboardResults);
    given(duolingoCouponService.findCandidateByCouponCode(COUPON_CODE))
        .willReturn(candidate);
    given(candidateExamService.list(CANDIDATE_ID))
        .willReturn(existingExams);

    // When
    duolingoExamService.updateCandidateExams();

    // Then
    verify(candidateExamService, never()).createExam(anyLong(), any(CreateCandidateExamRequest.class));
    verify(duolingoCouponService, never()).updateCouponStatus(anyString(), any(DuolingoCouponStatus.class));
    verify(duolingoExtraFieldsService, never()).createOrUpdateDuolingoExtraFields(any(DuolingoExtraFieldsRequest.class));
    verify(taskService, never()).getByName(anyString());
    verify(taskAssignmentService, never()).findByTaskIdAndCandidateIdAndStatus(anyLong(), anyLong(), any(Status.class));
    verify(taskAssignmentService, never()).completeTaskAssignment(any(TaskAssignmentImpl.class));
  }

  @Test
  @DisplayName("should skip processing when candidate not found for coupon code")
  void updateCandidateExams_shouldSkipProcessing_whenCandidateNotFound() throws NoSuchObjectException {
    // Given
    List<DuolingoDashboardResponse> dashboardResults = List.of(dashboardResponse);

    given(duolingoApiService.getDashboardResults(null, null))
        .willReturn(dashboardResults);
    given(duolingoCouponService.findCandidateByCouponCode(COUPON_CODE))
        .willReturn(null);

    // When
    duolingoExamService.updateCandidateExams();

    // Then
    verify(candidateExamService, never()).list(anyLong());
    verify(candidateExamService, never()).createExam(anyLong(), any(CreateCandidateExamRequest.class));
    verify(duolingoExtraFieldsService, never()).createOrUpdateDuolingoExtraFields(any(DuolingoExtraFieldsRequest.class));
    verify(taskAssignmentService, never()).completeTaskAssignment(any(TaskAssignmentImpl.class));
  }

  @Test
  @DisplayName("should handle empty dashboard results")
  void updateCandidateExams_shouldHandleEmptyDashboardResults() throws NoSuchObjectException {
    // Given
    List<DuolingoDashboardResponse> emptyResults = Collections.emptyList();

    given(duolingoApiService.getDashboardResults(null, null))
        .willReturn(emptyResults);

    // When
    duolingoExamService.updateCandidateExams();

    // Then
    verifyNoInteractions(duolingoCouponService);
    verifyNoInteractions(candidateExamService);
    verifyNoInteractions(duolingoExtraFieldsService);
    verifyNoInteractions(taskAssignmentService);
    verifyNoInteractions(taskService);
  }

  @Test
  @DisplayName("should handle multiple dashboard results")
  void updateCandidateExams_shouldHandleMultipleDashboardResults() throws NoSuchObjectException {
    // Given
    DuolingoDashboardResponse secondResponse = new DuolingoDashboardResponse();
    secondResponse.setCouponId("TEST456");
    secondResponse.setTestDate("2024-06-20");
    secondResponse.setOverallScore(110);
    secondResponse.setCertificateUrl("https://example.com/certificate2");
    secondResponse.setInterviewUrl("https://example.com/interview2");
    secondResponse.setVerificationDate("2024-06-21");
    secondResponse.setPercentScore(80);
    secondResponse.setScale(160);
    secondResponse.setLiteracySubscore(105);
    secondResponse.setConversationSubscore(115);
    secondResponse.setComprehensionSubscore(110);
    secondResponse.setProductionSubscore(108);

    Candidate secondCandidate = new Candidate();
    secondCandidate.setId(789L);

    List<DuolingoDashboardResponse> dashboardResults = Arrays.asList(dashboardResponse, secondResponse);

    given(duolingoApiService.getDashboardResults(null, null))
        .willReturn(dashboardResults);
    given(duolingoCouponService.findCandidateByCouponCode(COUPON_CODE))
        .willReturn(candidate);
    given(duolingoCouponService.findCandidateByCouponCode("TEST456"))
        .willReturn(secondCandidate);
    given(candidateExamService.list(CANDIDATE_ID))
        .willReturn(Collections.emptyList());
    given(candidateExamService.list(789L))
        .willReturn(Collections.emptyList());
    given(candidateExamService.createExam(anyLong(), any(CreateCandidateExamRequest.class)))
        .willReturn(savedExam);
    given(taskService.getByName(TASK_NAME))
        .willReturn(duolingoTask);
    given(taskAssignmentService.findByTaskIdAndCandidateIdAndStatus(anyLong(), anyLong(), eq(Status.active)))
        .willReturn(List.of(taskAssignment));

    // When
    duolingoExamService.updateCandidateExams();

    // Then
    verify(candidateExamService, times(2)).createExam(anyLong(), any(CreateCandidateExamRequest.class));
    verify(duolingoCouponService).updateCouponStatus(COUPON_CODE, DuolingoCouponStatus.REDEEMED);
    verify(duolingoCouponService).updateCouponStatus("TEST456", DuolingoCouponStatus.REDEEMED);
    verify(duolingoExtraFieldsService, times(2)).createOrUpdateDuolingoExtraFields(any(DuolingoExtraFieldsRequest.class));
    verify(taskAssignmentService, times(2)).completeTaskAssignment(any(TaskAssignmentImpl.class));
  }

  @Test
  @DisplayName("should throw NoSuchObjectException when task assignment not found")
  void updateCandidateExams_shouldThrowException_whenTaskAssignmentNotFound() throws NoSuchObjectException {
    // Given
    List<DuolingoDashboardResponse> dashboardResults = List.of(dashboardResponse);

    given(duolingoApiService.getDashboardResults(null, null))
        .willReturn(dashboardResults);
    given(duolingoCouponService.findCandidateByCouponCode(COUPON_CODE))
        .willReturn(candidate);
    given(candidateExamService.list(CANDIDATE_ID))
        .willReturn(Collections.emptyList());
    given(candidateExamService.createExam(eq(CANDIDATE_ID), any(CreateCandidateExamRequest.class)))
        .willReturn(savedExam);
    given(taskService.getByName(TASK_NAME))
        .willReturn(duolingoTask);
    given(taskAssignmentService.findByTaskIdAndCandidateIdAndStatus(TASK_ID, CANDIDATE_ID, Status.active))
        .willReturn(Collections.emptyList()); // No task assignments found

    // When & Then
    assertThrows(NoSuchObjectException.class, () -> duolingoExamService.updateCandidateExams());
  }

  @Test
  @DisplayName("should verify correct notes format is built")
  void updateCandidateExams_shouldBuildCorrectNotesFormat() throws NoSuchObjectException {
    // Given
    List<DuolingoDashboardResponse> dashboardResults = List.of(dashboardResponse);

    given(duolingoApiService.getDashboardResults(null, null))
        .willReturn(dashboardResults);
    given(duolingoCouponService.findCandidateByCouponCode(COUPON_CODE))
        .willReturn(candidate);
    given(candidateExamService.list(CANDIDATE_ID))
        .willReturn(Collections.emptyList());
    given(candidateExamService.createExam(eq(CANDIDATE_ID), any(CreateCandidateExamRequest.class)))
        .willReturn(savedExam);
    given(taskService.getByName(TASK_NAME))
        .willReturn(duolingoTask);
    given(taskAssignmentService.findByTaskIdAndCandidateIdAndStatus(TASK_ID, CANDIDATE_ID, Status.active))
        .willReturn(List.of(taskAssignment));

    // When
    duolingoExamService.updateCandidateExams();

    // Then
    ArgumentCaptor<CreateCandidateExamRequest> examRequestCaptor =
        ArgumentCaptor.forClass(CreateCandidateExamRequest.class);
    verify(candidateExamService).createExam(eq(CANDIDATE_ID), examRequestCaptor.capture());

    CreateCandidateExamRequest examRequest = examRequestCaptor.getValue();
    String expectedNotes = buildExpectedNotes();
    assertEquals(expectedNotes, examRequest.getNotes());
  }

  private String buildExpectedNotes() {
    return String.format(
        "Generated from Duolingo dashboard | Certificate URL: %s | Interview URL: %s | Verification Date: %s | Percent Score: %s | "
            + "Scale: %s | Literacy Subscore: %s | Conversation Subscore: %s | Comprehension Subscore: %s | Production Subscore: %s",
        CERTIFICATE_URL, INTERVIEW_URL, VERIFICATION_DATE,
        PERCENT_SCORE, SCALE, LITERACY_SUBSCORE,
        CONVERSATION_SUBSCORE, COMPREHENSION_SUBSCORE,
        PRODUCTION_SUBSCORE);
  }
}
