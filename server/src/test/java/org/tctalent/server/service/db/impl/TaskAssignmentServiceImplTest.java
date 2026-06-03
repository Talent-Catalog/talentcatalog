package org.tctalent.server.service.db.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.files.UploadType;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateExam;
import org.tctalent.server.model.db.CandidateProperty;
import org.tctalent.server.model.db.Exam;
import org.tctalent.server.model.db.QuestionTaskAssignmentImpl;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.TaskAssignmentImpl;
import org.tctalent.server.model.db.TaskImpl;
import org.tctalent.server.model.db.UploadTaskAssignmentImpl;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.task.QuestionTask;
import org.tctalent.server.model.db.task.TaskAssignedEvent;
import org.tctalent.server.model.db.task.TaskType;
import org.tctalent.server.model.db.task.UploadTask;
import org.tctalent.server.repository.db.TaskAssignmentRepository;
import org.tctalent.server.request.task.TaskListRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateAttachmentService;
import org.tctalent.server.service.db.CandidatePropertyService;
import org.tctalent.server.service.db.TaskService;

@ExtendWith(MockitoExtension.class)
class TaskAssignmentServiceImplTest {

  @Mock
  private CandidateAttachmentService candidateAttachmentService;

  @Mock
  private CandidatePropertyService candidatePropertyService;

  @Mock
  private TaskAssignmentRepository taskAssignmentRepository;

  @Mock
  private TaskService taskService;

  @Mock
  private AuthService authService;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @InjectMocks
  private TaskAssigmentServiceImpl service;

  @Test
  void assignTaskToCandidateCreatesQuestionAssignmentWithDefaultDueDateAndPublishesEvent() {
    User user = new User();
    Candidate candidate = candidate(1L, "CAND-001");
    SavedList savedList = new SavedList();

    TaskImpl task = mock(TaskImpl.class);
    when(task.getTaskType()).thenReturn(TaskType.Question);
    when(task.getDaysToComplete()).thenReturn(5);

    when(taskAssignmentRepository.save(any(TaskAssignmentImpl.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    TaskAssignmentImpl result =
        service.assignTaskToCandidate(user, task, candidate, savedList, null);

    assertInstanceOf(QuestionTaskAssignmentImpl.class, result);
    assertSame(task, result.getTask());
    assertSame(user, result.getActivatedBy());
    assertNotNull(result.getActivatedDate());
    assertSame(candidate, result.getCandidate());
    assertEquals(Status.active, result.getStatus());
    assertSame(savedList, result.getRelatedList());
    assertEquals(LocalDate.now().plusDays(5), result.getDueDate());

    verify(taskAssignmentRepository).save(result);

    ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
    verify(eventPublisher).publishEvent(eventCaptor.capture());
    assertInstanceOf(TaskAssignedEvent.class, eventCaptor.getValue());
  }

  @Test
  void assignTaskToCandidateCreatesUploadAssignmentWithExplicitDueDate() {
    User user = new User();
    Candidate candidate = candidate(1L, "CAND-001");
    LocalDate dueDate = LocalDate.of(2030, 1, 2);

    TaskImpl task = mock(TaskImpl.class);
    when(task.getTaskType()).thenReturn(TaskType.Upload);

    when(taskAssignmentRepository.save(any(TaskAssignmentImpl.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    TaskAssignmentImpl result =
        service.assignTaskToCandidate(user, task, candidate, null, dueDate);

    assertInstanceOf(UploadTaskAssignmentImpl.class, result);
    assertSame(task, result.getTask());
    assertSame(user, result.getActivatedBy());
    assertNotNull(result.getActivatedDate());
    assertSame(candidate, result.getCandidate());
    assertEquals(Status.active, result.getStatus());
    assertEquals(dueDate, result.getDueDate());
    assertNull(result.getRelatedList());

    verify(taskAssignmentRepository).save(result);
    verify(eventPublisher).publishEvent(any(Object.class));
  }
  @Test
  void assignTaskToCandidateCreatesBaseAssignmentForDefaultTaskTypeAndLeavesDueDateNull() {
    User user = new User();
    Candidate candidate = candidate(1L, "CAND-001");
    TaskImpl task = task( "standardTask", null);

    when(taskAssignmentRepository.save(any(TaskAssignmentImpl.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    TaskAssignmentImpl result =
        service.assignTaskToCandidate(user, task, candidate, null, null);

    assertEquals(TaskAssignmentImpl.class, result.getClass());
    assertNull(result.getDueDate());
    verify(eventPublisher).publishEvent(any(Object.class));
  }

  @Test
  void getReturnsTaskAssignmentWhenFound() {
    TaskAssignmentImpl taskAssignment = new TaskAssignmentImpl();

    when(taskAssignmentRepository.findById(7L)).thenReturn(Optional.of(taskAssignment));

    assertSame(taskAssignment, service.get(7L));
  }

  @Test
  void getThrowsWhenTaskAssignmentMissing() {
    when(taskAssignmentRepository.findById(7L)).thenReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class, () -> service.get(7L));
  }

  @Test
  void updateUploadTaskAssignmentDelegatesToUpdateWithoutChangingCompletion() {
    TaskImpl task = task("upload", null);
    TaskAssignmentImpl taskAssignment = assignment(task, candidate(1L, "CAND-001"));
    LocalDate dueDate = LocalDate.of(2030, 2, 3);

    when(taskAssignmentRepository.save(taskAssignment)).thenReturn(taskAssignment);

    TaskAssignmentImpl result =
        service.updateUploadTaskAssignment(taskAssignment, false, "notes", dueDate);

    assertSame(taskAssignment, result);
    assertEquals("notes", taskAssignment.getCandidateNotes());
    assertEquals(dueDate, taskAssignment.getDueDate());
    assertNull(taskAssignment.getCompletedDate());
    assertNull(taskAssignment.getAbandonedDate());
    verify(taskService).populateTransientFields(task);
  }

  @Test
  void updateSetsCompletedDateWhenCompletedTrueAndCompletedDateMissing() {
    TaskImpl task = task("normalTask", null);
    TaskAssignmentImpl taskAssignment = assignment(task, candidate(1L, "CAND-001"));

    when(taskAssignmentRepository.save(taskAssignment)).thenReturn(taskAssignment);

    service.update(taskAssignment, true, false, null, null);

    assertNotNull(taskAssignment.getCompletedDate());
    assertNull(taskAssignment.getAbandonedDate());
    verify(taskAssignmentRepository).save(taskAssignment);
    verify(taskService).populateTransientFields(task);
  }

  @Test
  void updateKeepsExistingCompletedDateWhenCompletedTrue() {
    TaskImpl task = task( "normalTask", null);
    TaskAssignmentImpl taskAssignment = assignment(task, candidate(1L, "CAND-001"));
    OffsetDateTime completedDate = OffsetDateTime.parse("2025-01-01T10:15:30+00:00");
    taskAssignment.setCompletedDate(completedDate);

    when(taskAssignmentRepository.save(taskAssignment)).thenReturn(taskAssignment);

    service.update(taskAssignment, true, false, null, null);

    assertEquals(completedDate, taskAssignment.getCompletedDate());
  }

  @Test
  void updateClearsCompletedDateWhenCompletedFalse() {
    TaskImpl task = task("normalTask", null);
    TaskAssignmentImpl taskAssignment = assignment(task, candidate(1L, "CAND-001"));
    taskAssignment.setCompletedDate(OffsetDateTime.now());

    when(taskAssignmentRepository.save(taskAssignment)).thenReturn(taskAssignment);

    service.update(taskAssignment, false, false, null, null);

    assertNull(taskAssignment.getCompletedDate());
  }

  @Test
  void updateAssignsDuolingoTaskWhenClaimCouponButtonCompleted() {
    User user = new User();
    Candidate candidate = candidate(1L, "CAND-001");

    TaskImpl claimCouponTask = task( "claimCouponButton", null);
    TaskImpl duolingoTask = task( "duolingoTest", 10);

    TaskAssignmentImpl taskAssignment = assignment(claimCouponTask, candidate);

    when(taskService.getByName("duolingoTest")).thenReturn(duolingoTask);
    when(authService.getLoggedInUser()).thenReturn(Optional.of(user));
    when(taskAssignmentRepository.save(any(TaskAssignmentImpl.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    service.update(taskAssignment, true, false, null, null);

    ArgumentCaptor<TaskAssignmentImpl> captor = ArgumentCaptor.forClass(TaskAssignmentImpl.class);
    verify(taskAssignmentRepository, atLeastOnce()).save(captor.capture());

    assertTrue(captor.getAllValues().stream()
        .anyMatch(saved -> saved != taskAssignment
            && saved.getTask() == duolingoTask
            && saved.getCandidate() == candidate
            && LocalDate.now().plusDays(10).equals(saved.getDueDate())));
    verify(eventPublisher).publishEvent(any(Object.class));
  }

  @Test
  void updateThrowsWhenClaimCouponButtonCompletedAndNoLoggedInUser() {
    Candidate candidate = candidate(1L, "CAND-001");
    TaskImpl claimCouponTask = task("claimCouponButton", null);
    TaskImpl duolingoTask = task( "duolingoTest", 10);
    TaskAssignmentImpl taskAssignment = assignment(claimCouponTask, candidate);

    when(taskService.getByName("duolingoTest")).thenReturn(duolingoTask);
    when(authService.getLoggedInUser()).thenReturn(Optional.empty());

    assertThrows(InvalidSessionException.class,
        () -> service.update(taskAssignment, true, false, null, null));

    verify(taskAssignmentRepository, never()).save(taskAssignment);
  }

  @Test
  void updateSetsAbandonedDateWhenAbandonedAndMissing() {
    TaskImpl task = task( "normalTask", null);
    TaskAssignmentImpl taskAssignment = assignment(task, candidate(1L, "CAND-001"));

    when(taskAssignmentRepository.save(taskAssignment)).thenReturn(taskAssignment);

    service.update(taskAssignment, null, true, null, null);

    assertNotNull(taskAssignment.getAbandonedDate());
  }

  @Test
  void updateKeepsExistingAbandonedDateWhenAbandoned() {
    TaskImpl task = task( "normalTask", null);
    TaskAssignmentImpl taskAssignment = assignment(task, candidate(1L, "CAND-001"));
    OffsetDateTime abandonedDate = OffsetDateTime.parse("2025-01-01T10:15:30+00:00");
    taskAssignment.setAbandonedDate(abandonedDate);

    when(taskAssignmentRepository.save(taskAssignment)).thenReturn(taskAssignment);

    service.update(taskAssignment, null, true, null, null);

    assertEquals(abandonedDate, taskAssignment.getAbandonedDate());
  }

  @Test
  void updateClearsAbandonedDateWhenNotAbandoned() {
    TaskImpl task = task("normalTask", null);
    TaskAssignmentImpl taskAssignment = assignment(task, candidate(1L, "CAND-001"));
    taskAssignment.setAbandonedDate(OffsetDateTime.now());

    when(taskAssignmentRepository.save(taskAssignment)).thenReturn(taskAssignment);

    service.update(taskAssignment, null, false, null, null);

    assertNull(taskAssignment.getAbandonedDate());
  }

  @Test
  void deactivateTaskAssignmentSetsInactiveAndAuditFields() {
    User user = new User();
    TaskAssignmentImpl taskAssignment = new TaskAssignmentImpl();

    when(taskAssignmentRepository.findById(1L)).thenReturn(Optional.of(taskAssignment));

    service.deactivateTaskAssignment(user, 1L);

    assertSame(user, taskAssignment.getDeactivatedBy());
    assertNotNull(taskAssignment.getDeactivatedDate());
    assertEquals(Status.inactive, taskAssignment.getStatus());
    verify(taskAssignmentRepository).save(taskAssignment);
  }

  @Test
  void deactivateTaskAssignmentThrowsWhenMissing() {
    when(taskAssignmentRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class,
        () -> service.deactivateTaskAssignment(new User(), 1L));
  }

  @Test
  void deleteTaskAssignmentMarksDeletedAndReturnsTrue() {
    User user = new User();
    TaskAssignmentImpl taskAssignment = new TaskAssignmentImpl();

    when(taskAssignmentRepository.findById(1L)).thenReturn(Optional.of(taskAssignment));

    assertTrue(service.deleteTaskAssignment(user, 1L));

    assertSame(user, taskAssignment.getDeactivatedBy());
    assertNotNull(taskAssignment.getDeactivatedDate());
    assertEquals(Status.deleted, taskAssignment.getStatus());
    verify(taskAssignmentRepository).save(taskAssignment);
  }

  @Test
  void deleteTaskAssignmentThrowsWhenMissing() {
    when(taskAssignmentRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class,
        () -> service.deleteTaskAssignment(new User(), 1L));
  }

  @Test
  void completeTaskAssignmentSetsCompletedDateAndSaves() {
    TaskAssignmentImpl taskAssignment = new TaskAssignmentImpl();

    service.completeTaskAssignment(taskAssignment);

    assertNotNull(taskAssignment.getCompletedDate());
    verify(taskAssignmentRepository).save(taskAssignment);
  }

  @Test
  void completeUploadTaskAssignmentUploadsAttachmentWithComputedNameAndCompletesTask()
      throws IOException {
    Candidate candidate = candidate(1L, "CAND-001");
    MultipartFile file = mock(MultipartFile.class);
    UploadType uploadType = UploadType.values()[0];
    TaskImpl uploadTaskImpl = uploadTask("passportUpload", "docs", uploadType);

    UploadTaskAssignmentImpl taskAssignment = new UploadTaskAssignmentImpl();
    taskAssignment.setCandidate(candidate);
    taskAssignment.setTask(uploadTaskImpl);

    when(file.getOriginalFilename()).thenReturn("passport.pdf");

    service.completeUploadTaskAssignment(taskAssignment, file);

    verify(candidateAttachmentService).uploadAttachment(
        candidate,
        "CAND-001_passportUpload-passport.pdf",
        "docs",
        file,
        uploadType
    );
    assertNotNull(taskAssignment.getCompletedDate());
    verify(taskAssignmentRepository).save(taskAssignment);
  }

  @Test
  void completeUploadTaskAssignmentThrowsClassCastExceptionForNonUploadTask() {
    TaskAssignmentImpl taskAssignment = assignment(
        task("notUpload", null),
        candidate(1L, "CAND-001")
    );

    assertThrows(ClassCastException.class,
        () -> service.completeUploadTaskAssignment(taskAssignment, mock(MultipartFile.class)));
  }

  @Test
  void listTaskAssignmentsReturnsRepositoryResults() {
    TaskListRequest request = mock(TaskListRequest.class);
    TaskAssignmentImpl taskAssignment = new TaskAssignmentImpl();

    when(request.getTaskId()).thenReturn(10L);
    when(request.getSavedListId()).thenReturn(20L);
    when(taskAssignmentRepository.findByTaskAndList(10L, 20L))
        .thenReturn(List.of(taskAssignment));

    assertEquals(List.of(taskAssignment), service.listTaskAssignments(request));
  }

  @Test
  void findByTaskIdAndCandidateIdAndStatusReturnsRepositoryResults() {
    TaskAssignmentImpl taskAssignment = new TaskAssignmentImpl();

    when(taskAssignmentRepository.findByTask_IdAndCandidate_IdAndStatus(
        10L, 20L, Status.active))
        .thenReturn(List.of(taskAssignment));

    assertEquals(List.of(taskAssignment),
        service.findByTaskIdAndCandidateIdAndStatus(10L, 20L, Status.active));
  }

  @Test
  void populateTransientTaskAssignmentFieldsPopulatesTaskOnlyWhenNotCompleted() {
    TaskImpl task = task("normalTask", null);
    TaskAssignmentImpl taskAssignment = assignment(task, candidate(1L, "CAND-001"));

    service.populateTransientTaskAssignmentFields(List.of(taskAssignment));

    verify(taskService).populateTransientFields(task);
  }

  @Test
  void populateTransientTaskAssignmentFieldsSetsQuestionAnswerFromCandidateProperty() {
    Candidate candidate = candidate(1L, "CAND-001");
    TaskImpl questionTask = questionTaskForCandidateProperty("customQuestion");
    QuestionTaskAssignmentImpl taskAssignment =
        completedQuestionAssignment(questionTask, candidate);

    CandidateProperty property = new CandidateProperty();
    property.setValue("property answer");

    when(candidatePropertyService.findProperty(candidate, "customQuestion"))
        .thenReturn(property);

    service.populateTransientTaskAssignmentFields(List.of(taskAssignment));

    assertEquals("property answer", taskAssignment.getAnswer());
    verify(taskService).populateTransientFields(questionTask);
  }

  @Test
  void populateTransientTaskAssignmentFieldsSetsNullAnswerWhenCandidatePropertyMissing() {
    Candidate candidate = candidate(1L, "CAND-001");
    TaskImpl questionTask = questionTaskForCandidateProperty("customQuestion");
    QuestionTaskAssignmentImpl taskAssignment =
        completedQuestionAssignment(questionTask, candidate);

    when(candidatePropertyService.findProperty(candidate, "customQuestion"))
        .thenReturn(null);

    service.populateTransientTaskAssignmentFields(List.of(taskAssignment));

    assertNull(taskAssignment.getAnswer());
  }

  @Test
  void populateTransientTaskAssignmentFieldsSetsQuestionAnswerFromCandidateField() {
    Candidate candidate = candidate(1L, "CAND-001");
    TaskImpl questionTask = questionTaskForCandidateField("candidateNumber");
    QuestionTaskAssignmentImpl taskAssignment =
        completedQuestionAssignment(questionTask, candidate);

    service.populateTransientTaskAssignmentFields(List.of(taskAssignment));

    assertEquals("CAND-001", taskAssignment.getAnswer());
  }

  @Test
  void populateTransientTaskAssignmentFieldsSetsNullWhenCandidateFieldValueIsNull() {
    Candidate candidate = candidate(1L, null);
    TaskImpl questionTask = questionTaskForCandidateField("candidateNumber");
    QuestionTaskAssignmentImpl taskAssignment =
        completedQuestionAssignment(questionTask, candidate);

    service.populateTransientTaskAssignmentFields(List.of(taskAssignment));

    assertNull(taskAssignment.getAnswer());
  }

  @Test
  void populateTransientTaskAssignmentFieldsSetsQuestionAnswerFromCandidateExam() {
    Exam exam = Exam.values()[0];
    Candidate candidate = candidate(1L, "CAND-001");

    CandidateExam candidateExam = new CandidateExam();
    candidateExam.setExam(exam);
    candidateExam.setScore("85");
    candidate.setCandidateExams(List.of(candidateExam));

    TaskImpl questionTask = questionTaskForCandidateField("candidateExams." + exam.name());
    QuestionTaskAssignmentImpl taskAssignment =
        completedQuestionAssignment(questionTask, candidate);

    service.populateTransientTaskAssignmentFields(List.of(taskAssignment));

    assertEquals("85", taskAssignment.getAnswer());
  }

  @Test
  void populateTransientTaskAssignmentFieldsSetsNullWhenCandidateExamListIsNull() {
    Exam exam = Exam.values()[0];
    Candidate candidate = candidate(1L, "CAND-001");
    candidate.setCandidateExams(null);

    TaskImpl questionTask = questionTaskForCandidateField("candidateExams." + exam.name());
    QuestionTaskAssignmentImpl taskAssignment =
        completedQuestionAssignment(questionTask, candidate);

    service.populateTransientTaskAssignmentFields(List.of(taskAssignment));

    assertNull(taskAssignment.getAnswer());
  }

  @Test
  void populateTransientTaskAssignmentFieldsSetsNullWhenCandidateExamMissing() {
    Exam requestedExam = Exam.values()[0];
    Candidate candidate = candidate(1L, "CAND-001");
    candidate.setCandidateExams(List.of());

    TaskImpl questionTask = questionTaskForCandidateField("candidateExams." + requestedExam.name());
    QuestionTaskAssignmentImpl taskAssignment =
        completedQuestionAssignment(questionTask, candidate);

    service.populateTransientTaskAssignmentFields(List.of(taskAssignment));

    assertNull(taskAssignment.getAnswer());
  }

  @Test
  void populateTransientTaskAssignmentFieldsThrowsWhenQuestionTaskHasInvalidCandidateField() {
    Candidate candidate = candidate(1L, "CAND-001");
    TaskImpl questionTask =
        questionTaskForInvalidCandidateField("doesNotExist", "Bad Question");
    QuestionTaskAssignmentImpl taskAssignment =
        completedQuestionAssignment(questionTask, candidate);

    assertThrows(NoSuchObjectException.class,
        () -> service.populateTransientTaskAssignmentFields(List.of(taskAssignment)));
  }

  @Test
  void populateTransientTaskAssignmentFieldsThrowsWhenQuestionAssignmentTaskIsNotQuestionTask() {
    Candidate candidate = candidate(1L, "CAND-001");
    TaskImpl nonQuestionTask = task("notAQuestion", null);
    QuestionTaskAssignmentImpl taskAssignment =
        completedQuestionAssignment(nonQuestionTask, candidate);

    assertThrows(InvalidRequestException.class,
        () -> service.populateTransientTaskAssignmentFields(List.of(taskAssignment)));
  }

  private static TaskAssignmentImpl assignment(TaskImpl task, Candidate candidate) {
    TaskAssignmentImpl taskAssignment = new TaskAssignmentImpl();
    taskAssignment.setTask(task);
    taskAssignment.setCandidate(candidate);
    return taskAssignment;
  }

  private static QuestionTaskAssignmentImpl completedQuestionAssignment(
      TaskImpl task, Candidate candidate) {
    QuestionTaskAssignmentImpl taskAssignment = new QuestionTaskAssignmentImpl();
    taskAssignment.setTask(task);
    taskAssignment.setCandidate(candidate);
    taskAssignment.setCompletedDate(OffsetDateTime.now());
    return taskAssignment;
  }

  private static Candidate candidate(Long id, String candidateNumber) {
    Candidate candidate = new Candidate();
    candidate.setId(id);
    candidate.setCandidateNumber(candidateNumber);
    return candidate;
  }

  private static TaskImpl task(String name, Integer daysToComplete) {
    TaskImpl task = new TaskImpl();
    task.setName(name);
    task.setDaysToComplete(daysToComplete);
    return task;
  }

  private static TaskImpl questionTaskForCandidateProperty(String taskName) {
    TaskImpl task = mock(TaskImpl.class, withSettings().extraInterfaces(QuestionTask.class));
    when(((QuestionTask) task).getCandidateAnswerField()).thenReturn(null);
    when(task.getName()).thenReturn(taskName);
    return task;
  }

  private static TaskImpl questionTaskForCandidateField(String candidateAnswerField) {
    TaskImpl task = mock(TaskImpl.class, withSettings().extraInterfaces(QuestionTask.class));
    when(((QuestionTask) task).getCandidateAnswerField()).thenReturn(candidateAnswerField);
    return task;
  }

  private static TaskImpl questionTaskForInvalidCandidateField(
      String candidateAnswerField, String displayName) {
    TaskImpl task = mock(TaskImpl.class, withSettings().extraInterfaces(QuestionTask.class));
    when(((QuestionTask) task).getCandidateAnswerField()).thenReturn(candidateAnswerField);
    when(task.getDisplayName()).thenReturn(displayName);
    return task;
  }

  private static TaskImpl uploadTask(String name, String subfolderName, UploadType uploadType) {
    TaskImpl task = mock(TaskImpl.class, withSettings().extraInterfaces(UploadTask.class));
    when(((UploadTask) task).getName()).thenReturn(name);
    when(((UploadTask) task).getUploadSubfolderName()).thenReturn(subfolderName);
    when(((UploadTask) task).getUploadType()).thenReturn(uploadType);
    return task;
  }

  private static TaskType defaultTaskType() {
    return Arrays.stream(TaskType.values())
        .filter(type -> type != TaskType.Question)
        .filter(type -> type != TaskType.Upload)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(
            "TaskType must have at least one non Question/Upload value to cover default branch"));
  }
}