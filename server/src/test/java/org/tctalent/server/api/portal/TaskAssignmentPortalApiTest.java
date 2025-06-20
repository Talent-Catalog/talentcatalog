package org.tctalent.server.api.portal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.exception.UnauthorisedActionException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.QuestionTaskAssignmentImpl;
import org.tctalent.server.model.db.TaskAssignmentImpl;
import org.tctalent.server.request.task.UpdateQuestionTaskAssignmentRequestCandidate;
import org.tctalent.server.request.task.UpdateTaskAssignmentCommentRequest;
import org.tctalent.server.request.task.UpdateTaskAssignmentRequestCandidate;
import org.tctalent.server.request.task.UpdateUploadTaskAssignmentRequestCandidate;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.TaskAssignmentService;

class TaskAssignmentPortalApiTest {

  @Mock
  private AuthService authService;

  @Mock
  private CandidateService candidateService;

  @Mock
  private TaskAssignmentService taskAssignmentService;

  @InjectMocks
  private TaskAssignmentPortalApi taskAssignmentPortalApi;

  private TaskAssignmentImpl taskAssignment;
  private QuestionTaskAssignmentImpl questionTaskAssignment;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    Candidate loggedInCandidate = createSampleCandidate();
    taskAssignment = createSampleTaskAssignment(loggedInCandidate);
    questionTaskAssignment = createSampleQuestionTaskAssignment(loggedInCandidate);
    when(authService.getLoggedInCandidateId()).thenReturn(loggedInCandidate.getId());
  }

  @Test
  void testCompleteUploadTask_Success() throws IOException, NoSuchObjectException, UnauthorisedActionException {
    long id = 1L;
    MultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());
    when(taskAssignmentService.get(id)).thenReturn(taskAssignment);
    Map<String, Object> result = taskAssignmentPortalApi.completeUploadTask(id, file);

    assertNotNull(result);
    verify(authService).getLoggedInCandidateId();
    verify(taskAssignmentService).get(id);
    verify(taskAssignmentService).completeUploadTaskAssignment(taskAssignment, file);
  }

  @Test
  void testUpdateUploadTaskAssignment_Success() throws NoSuchObjectException, UnauthorisedActionException {
    long id = 1L;
    UpdateUploadTaskAssignmentRequestCandidate request = new UpdateUploadTaskAssignmentRequestCandidate();
    request.setAbandoned(true);
    request.setCandidateNotes("Notes");
    when(taskAssignmentService.get(id)).thenReturn(taskAssignment);
    when(taskAssignmentService.updateUploadTaskAssignment(taskAssignment, true, "Notes", null)).thenReturn(taskAssignment);

    Map<String, Object> result = taskAssignmentPortalApi.updateUploadTaskAssignment(id, request);

    assertNotNull(result);
    verify(authService).getLoggedInCandidateId();
    verify(taskAssignmentService).get(id);
    verify(taskAssignmentService).updateUploadTaskAssignment(taskAssignment, true, "Notes", null);
  }

  @Test
  void testUpdateQuestionTask_Success() throws InvalidRequestException, NoSuchObjectException, UnauthorisedActionException {
    long id = 1L;
    UpdateQuestionTaskAssignmentRequestCandidate request = new UpdateQuestionTaskAssignmentRequestCandidate();
    request.setAnswer("Answer");
    request.setCandidateNotes("Notes");
    when(taskAssignmentService.get(id)).thenReturn(questionTaskAssignment);
    when(taskAssignmentService.update(questionTaskAssignment, true, false, "Notes", null)).thenReturn(questionTaskAssignment);

    Map<String, Object> result = taskAssignmentPortalApi.updateQuestionTask(id, request);

    assertNotNull(result);
    verify(authService).getLoggedInCandidateId();
    verify(taskAssignmentService).get(id);
    verify(taskAssignmentService).update(questionTaskAssignment, true, false, "Notes", null);
    verify(candidateService).storeCandidateTaskAnswer(questionTaskAssignment, "Answer");
  }

  @Test
  void testUpdateQuestionTask_Abandoned() throws Exception {
    long id = 1L;
    UpdateQuestionTaskAssignmentRequestCandidate request = new UpdateQuestionTaskAssignmentRequestCandidate();
    request.setAbandoned(true);
    request.setCandidateNotes("Abandoned notes");
    when(taskAssignmentService.get(id)).thenReturn(questionTaskAssignment);
    when(taskAssignmentService.update(questionTaskAssignment, false, true, "Abandoned notes", null)).thenReturn(questionTaskAssignment);

    Map<String, Object> result = taskAssignmentPortalApi.updateQuestionTask(id, request);

    assertNotNull(result);
    verify(authService).getLoggedInCandidateId();
    verify(taskAssignmentService).get(id);
    verify(taskAssignmentService).update(questionTaskAssignment, false, true, "Abandoned notes", null);
    verify(candidateService, never()).storeCandidateTaskAnswer(any(), any());
  }

  @Test
  void testUpdateTaskAssignment_Success() throws NoSuchObjectException, UnauthorisedActionException {
    long id = 1L;
    UpdateTaskAssignmentRequestCandidate request = new UpdateTaskAssignmentRequestCandidate();
    request.setCompleted(true);
    request.setCandidateNotes("Notes");
    when(taskAssignmentService.get(id)).thenReturn(taskAssignment);
    when(taskAssignmentService.update(taskAssignment, true, false, "Notes", null)).thenReturn(taskAssignment);

    Map<String, Object> result = taskAssignmentPortalApi.updateTaskAssignment(id, request);

    assertNotNull(result);
    verify(authService).getLoggedInCandidateId();
    verify(taskAssignmentService).get(id);
    verify(taskAssignmentService).update(taskAssignment, true, false, "Notes", null);
  }

  @Test
  void testUpdateTaskAssignment_Abandoned() throws NoSuchObjectException, UnauthorisedActionException {
    long id = 1L;
    UpdateTaskAssignmentRequestCandidate request = new UpdateTaskAssignmentRequestCandidate();
    request.setAbandoned(true);
    request.setCandidateNotes("Abandoned");
    when(taskAssignmentService.get(id)).thenReturn(taskAssignment);
    when(taskAssignmentService.update(taskAssignment, false, true, "Abandoned", null)).thenReturn(taskAssignment);

    Map<String, Object> result = taskAssignmentPortalApi.updateTaskAssignment(id, request);

    assertNotNull(result);
    verify(authService).getLoggedInCandidateId();
    verify(taskAssignmentService).get(id);
    verify(taskAssignmentService).update(taskAssignment, false, true, "Abandoned", null);
  }

  @Test
  void testUpdateTaskComment_Success() throws NoSuchObjectException, UnauthorisedActionException {
    long id = 1L;
    UpdateTaskAssignmentCommentRequest request = new UpdateTaskAssignmentCommentRequest();
    request.setCandidateNotes("Comment");
    taskAssignment.setCompletedDate(OffsetDateTime.now());
    when(taskAssignmentService.get(id)).thenReturn(taskAssignment);
    when(taskAssignmentService.update(taskAssignment, true, false, "Comment", null)).thenReturn(taskAssignment);

    Map<String, Object> result = taskAssignmentPortalApi.updateTaskComment(id, request);

    assertNotNull(result);
    verify(authService).getLoggedInCandidateId();
    verify(taskAssignmentService).get(id);
    verify(taskAssignmentService).update(taskAssignment, true, false, "Comment", null);
  }

  private Candidate createSampleCandidate() {
    Candidate candidate = new Candidate();
    candidate.setId(1L);
    return candidate;
  }

  private TaskAssignmentImpl createSampleTaskAssignment(Candidate candidate) {
    TaskAssignmentImpl ta = new TaskAssignmentImpl();
    ta.setId(1L);
    ta.setCandidate(candidate);
    return ta;
  }

  private QuestionTaskAssignmentImpl createSampleQuestionTaskAssignment(Candidate candidate) {
    QuestionTaskAssignmentImpl ta = new QuestionTaskAssignmentImpl();
    ta.setId(1L);
    ta.setCandidate(candidate);
    return ta;
  }
}