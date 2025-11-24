package org.tctalent.server.api.portal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.tctalent.server.model.db.CandidateCertification;
import org.tctalent.server.request.candidate.certification.CreateCandidateCertificationRequest;
import org.tctalent.server.request.candidate.certification.UpdateCandidateCertificationRequest;
import org.tctalent.server.service.db.CandidateCertificationService;

class CandidateCertificationPortalApiTest {

  @Mock
  private CandidateCertificationService candidateCertificationService;

  @InjectMocks
  private CandidateCertificationPortalApi candidateCertificationPortalApi;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testCreateCandidateCertification_Success() {
    CreateCandidateCertificationRequest request = new CreateCandidateCertificationRequest();
    CandidateCertification certification = createSampleCertification();
    when(candidateCertificationService.createCandidateCertification(
        any(CreateCandidateCertificationRequest.class)))
        .thenReturn(certification);

    Map<String, Object> result = candidateCertificationPortalApi.createCandidateCertification(
        request);

    assertNotNull(result);
    assertEquals(1L, result.get("id"));
    assertEquals("Test Certification", result.get("name"));
    assertEquals("Test Institution", result.get("institution"));
    assertEquals(LocalDate.of(2023, 6, 1), result.get("dateCompleted"));
    verify(candidateCertificationService).createCandidateCertification(request);
  }

  @Test
  void testUpdate_Success() {
    UpdateCandidateCertificationRequest request = new UpdateCandidateCertificationRequest();
    CandidateCertification certification = createSampleCertification();
    when(candidateCertificationService.updateCandidateCertification(
        any(UpdateCandidateCertificationRequest.class)))
        .thenReturn(certification);

    Map<String, Object> result = candidateCertificationPortalApi.update(request);

    assertNotNull(result);
    assertEquals(1L, result.get("id"));
    assertEquals("Test Certification", result.get("name"));
    assertEquals("Test Institution", result.get("institution"));
    assertEquals(LocalDate.of(2023, 6, 1), result.get("dateCompleted"));
    verify(candidateCertificationService).updateCandidateCertification(request);
  }

  @Test
  void testDeleteCandidateCertification_Success() {
    Long id = 1L;
    doNothing().when(candidateCertificationService).deleteCandidateCertification(id);

    ResponseEntity result = candidateCertificationPortalApi.deleteCandidateCertification(id);

    assertNotNull(result);
    assertEquals(200, result.getStatusCodeValue());
    verify(candidateCertificationService).deleteCandidateCertification(id);
  }

  private CandidateCertification createSampleCertification() {
    CandidateCertification certification = new CandidateCertification();
    certification.setId(1L);
    certification.setName("Test Certification");
    certification.setInstitution("Test Institution");
    certification.setDateCompleted(LocalDate.of(2023, 6, 1));
    return certification;
  }
}