package org.tctalent.server.api.portal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.CandidateExam;
import org.tctalent.server.model.db.Exam;
import org.tctalent.server.request.candidate.exam.CreateCandidateExamRequest;
import org.tctalent.server.request.candidate.exam.UpdateCandidateExamRequest;
import org.tctalent.server.service.db.CandidateExamService;
import org.tctalent.server.service.db.CandidateService;

class CandidateExamPortalApiTest {

  @Mock
  private CandidateExamService candidateExamService;

  @Mock
  private CandidateService candidateService;

  @InjectMocks
  private CandidateExamPortalApi candidateExamPortalApi;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testCreate_Success() {
    long parentId = 1L;
    CreateCandidateExamRequest request = new CreateCandidateExamRequest();
    CandidateExam candidateExam = createSampleCandidateExam();
    when(candidateExamService.createExam(eq(parentId), any(CreateCandidateExamRequest.class)))
        .thenReturn(candidateExam);

    Map<String, Object> result = candidateExamPortalApi.create(parentId, request);

    assertNotNull(result);
    assertEquals(1L, result.get("id"));
    assertEquals(Exam.DETOfficial, result.get("exam"));
    assertEquals("Custom Exam", result.get("otherExam"));
    assertEquals("7.5", result.get("score"));
    assertEquals(2023L, result.get("year")); // Expect Long
    assertEquals("Test notes", result.get("notes"));
    verify(candidateExamService).createExam(parentId, request);
  }

  @Test
  void testCreate_EntityExistsException() {
    long parentId = 1L;
    CreateCandidateExamRequest request = new CreateCandidateExamRequest();
    when(candidateExamService.createExam(eq(parentId), any(CreateCandidateExamRequest.class)))
        .thenThrow(
            new EntityExistsException("A Exam already exists with this name already exists"));

    EntityExistsException exception = assertThrows(
        EntityExistsException.class,
        () -> candidateExamPortalApi.create(parentId, request)
    );

    assertTrue(
        exception.getMessage().contains("A Exam already exists with this name already exists"));
    verify(candidateExamService).createExam(parentId, request);
  }

  @Test
  void testUpdate_Success() {
    long id = 1L;
    UpdateCandidateExamRequest request = new UpdateCandidateExamRequest();
    CandidateExam candidateExam = createSampleCandidateExam();
    when(candidateExamService.updateCandidateExam(any(UpdateCandidateExamRequest.class)))
        .thenReturn(candidateExam);

    Map<String, Object> result = candidateExamPortalApi.update(id, request);

    assertNotNull(result);
    assertEquals(1L, result.get("id"));
    assertEquals(Exam.DETOfficial, result.get("exam"));
    assertEquals("Custom Exam", result.get("otherExam"));
    assertEquals("7.5", result.get("score"));
    assertEquals(2023L, result.get("year")); // Expect Long
    assertEquals("Test notes", result.get("notes"));
    verify(candidateExamService).updateCandidateExam(request);
  }

  @Test
  void testUpdate_EntityExistsException() {
    long id = 1L;
    UpdateCandidateExamRequest request = new UpdateCandidateExamRequest();
    when(candidateExamService.updateCandidateExam(any(UpdateCandidateExamRequest.class)))
        .thenThrow(
            new EntityExistsException("A Exam already exists with this name already exists"));

    EntityExistsException exception = assertThrows(
        EntityExistsException.class,
        () -> candidateExamPortalApi.update(id, request)
    );

    assertTrue(
        exception.getMessage().contains("A Exam already exists with this name already exists"));
    verify(candidateExamService).updateCandidateExam(request);
  }

  @Test
  void testUpdate_InvalidRequestException() {
    long id = 1L;
    UpdateCandidateExamRequest request = new UpdateCandidateExamRequest();
    when(candidateExamService.updateCandidateExam(any(UpdateCandidateExamRequest.class)))
        .thenThrow(new InvalidRequestException("Not authorized"));

    InvalidRequestException exception = assertThrows(
        InvalidRequestException.class,
        () -> candidateExamPortalApi.update(id, request)
    );

    assertEquals("Not authorized", exception.getMessage());
    verify(candidateExamService).updateCandidateExam(request);
  }

  @Test
  void testUpdate_NoSuchObjectException() {
    long id = 1L;
    UpdateCandidateExamRequest request = new UpdateCandidateExamRequest();
    when(candidateExamService.updateCandidateExam(any(UpdateCandidateExamRequest.class)))
        .thenThrow(new NoSuchObjectException("No such exam"));

    NoSuchObjectException exception = assertThrows(
        NoSuchObjectException.class,
        () -> candidateExamPortalApi.update(id, request)
    );

    assertEquals("No such exam", exception.getMessage());
    verify(candidateExamService).updateCandidateExam(request);
  }

  @Test
  void testDelete_Success() {
    long id = 1L;
    when(candidateService.deleteCandidateExam(id)).thenReturn(true);

    boolean result = candidateExamPortalApi.delete(id);

    assertTrue(result);
    verify(candidateService).deleteCandidateExam(id);
  }

  @Test
  void testDelete_NotFound() {
    long id = 1L;
    when(candidateService.deleteCandidateExam(id)).thenReturn(false);

    boolean result = candidateExamPortalApi.delete(id);

    assertFalse(result);
    verify(candidateService).deleteCandidateExam(id);
  }

  @Test
  void testDelete_EntityReferencedException() {
    long id = 1L;
    when(candidateService.deleteCandidateExam(id))
        .thenThrow(new EntityReferencedException(
            "This Exam is referenced by another entity is referenced by another object and cannot be deleted"));

    EntityReferencedException exception = assertThrows(
        EntityReferencedException.class,
        () -> candidateExamPortalApi.delete(id)
    );

    assertTrue(exception.getMessage().contains(
        "This Exam is referenced by another entity is referenced by another object and cannot be deleted"));
    verify(candidateService).deleteCandidateExam(id);
  }

  @Test
  void testDelete_InvalidRequestException() {
    long id = 1L;
    when(candidateService.deleteCandidateExam(id))
        .thenThrow(new InvalidRequestException("Not authorized to delete"));

    InvalidRequestException exception = assertThrows(
        InvalidRequestException.class,
        () -> candidateExamPortalApi.delete(id)
    );

    assertEquals("Not authorized to delete", exception.getMessage());
    verify(candidateService).deleteCandidateExam(id);
  }

  private CandidateExam createSampleCandidateExam() {
    CandidateExam exam = new CandidateExam();
    exam.setId(1L);
    exam.setExam(Exam.DETOfficial);
    exam.setOtherExam("Custom Exam");
    exam.setScore("7.5");
    exam.setYear(2023L); // Use Long
    exam.setNotes("Test notes");
    return exam;
  }
}