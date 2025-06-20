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
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.CandidateJobExperience;
import org.tctalent.server.model.db.CandidateOccupation;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.Occupation;
import org.tctalent.server.request.work.experience.CreateJobExperienceRequest;
import org.tctalent.server.request.work.experience.UpdateJobExperienceRequest;
import org.tctalent.server.service.db.CandidateJobExperienceService;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.service.db.OccupationService;
import org.tctalent.server.util.dto.DtoBuilder;

class CandidateJobExperiencePortalApiTest {

  @Mock
  private CandidateJobExperienceService candidateJobExperienceService;

  @Mock
  private CountryService countryService;

  @Mock
  private OccupationService occupationService;

  @InjectMocks
  private CandidateJobExperiencePortalApi candidateJobExperiencePortalApi;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    when(countryService.selectBuilder()).thenReturn(new DtoBuilder()
        .add("id")
        .add("name"));
    when(occupationService.selectBuilder()).thenReturn(new DtoBuilder()
        .add("id")
        .add("name"));
  }

  @Test
  void testCreateJobExperience_Success() {
    CreateJobExperienceRequest request = new CreateJobExperienceRequest();
    CandidateJobExperience jobExperience = createSampleJobExperience();
    when(candidateJobExperienceService.createCandidateJobExperience(
        any(CreateJobExperienceRequest.class)))
        .thenReturn(jobExperience);

    Map<String, Object> result = candidateJobExperiencePortalApi.createJobExperience(request);

    assertNotNull(result);
    assertEquals(1L, result.get("id"));
    assertEquals("Test Company", result.get("companyName"));
    assertEquals("Software Engineer", result.get("role"));
    assertEquals(LocalDate.of(2020, 1, 1), result.get("startDate"));
    assertEquals(LocalDate.of(2022, 12, 31), result.get("endDate"));
    assertTrue((Boolean) result.get("fullTime"));
    assertTrue((Boolean) result.get("paid"));
    assertEquals("Developed software applications", result.get("description"));

    Object countryObj = result.get("country");
    assertInstanceOf(Map.class, countryObj);
    Map<?, ?> country = (Map<?, ?>) countryObj;
    assertEquals(1L, country.get("id"));
    assertEquals("Test Country", country.get("name"));

    Object candidateOccupationObj = result.get("candidateOccupation");
    assertInstanceOf(Map.class, candidateOccupationObj);
    Map<?, ?> candidateOccupation = (Map<?, ?>) candidateOccupationObj;
    assertEquals(1L, candidateOccupation.get("id"));

    Object occupationObj = candidateOccupation.get("occupation");
    assertInstanceOf(Map.class, occupationObj);
    Map<?, ?> occupation = (Map<?, ?>) occupationObj;
    assertEquals(101L, occupation.get("id"));
    assertEquals("Software Developer", occupation.get("name"));

    verify(candidateJobExperienceService).createCandidateJobExperience(request);
  }


  @Test
  void testUpdateJobExperience_Success() {
    UpdateJobExperienceRequest request = new UpdateJobExperienceRequest();
    CandidateJobExperience jobExperience = createSampleJobExperience();
    when(candidateJobExperienceService.updateCandidateJobExperience(
        any(UpdateJobExperienceRequest.class)))
        .thenReturn(jobExperience);

    Map<String, Object> result = candidateJobExperiencePortalApi.updateJobExperience(request);

    assertNotNull(result);
    assertEquals(1L, result.get("id"));
    assertEquals("Test Company", result.get("companyName"));
    assertEquals("Software Engineer", result.get("role"));
    assertEquals(LocalDate.of(2020, 1, 1), result.get("startDate"));
    assertEquals(LocalDate.of(2022, 12, 31), result.get("endDate"));
    assertTrue((Boolean) result.get("fullTime"));
    assertTrue((Boolean) result.get("paid"));
    assertEquals("Developed software applications", result.get("description"));

    Object countryObj = result.get("country");
    assertInstanceOf(Map.class, countryObj);
    Map<?, ?> country = (Map<?, ?>) countryObj;
    assertEquals(1L, country.get("id"));
    assertEquals("Test Country", country.get("name"));

    Object candidateOccupationObj = result.get("candidateOccupation");
    assertInstanceOf(Map.class, candidateOccupationObj);
    Map<?, ?> candidateOccupation = (Map<?, ?>) candidateOccupationObj;
    assertEquals(1L, candidateOccupation.get("id"));

    Object occupationObj = candidateOccupation.get("occupation");
    assertInstanceOf(Map.class, occupationObj);
    Map<?, ?> occupation = (Map<?, ?>) occupationObj;
    assertEquals(101L, occupation.get("id"));
    assertEquals("Software Developer", occupation.get("name"));

    verify(candidateJobExperienceService).updateCandidateJobExperience(request);
  }


  @Test
  void testDeleteJobExperience_Success() {
    Long id = 1L;
    doNothing().when(candidateJobExperienceService).deleteCandidateJobExperience(id);

    ResponseEntity result = candidateJobExperiencePortalApi.deleteJobExperience(id);

    assertNotNull(result);
    assertEquals(200, result.getStatusCodeValue());
    verify(candidateJobExperienceService).deleteCandidateJobExperience(id);
  }

  @Test
  void testDeleteJobExperience_NoSuchObjectException() {
    Long id = 1L;
    doThrow(new NoSuchObjectException("No such job experience")).when(candidateJobExperienceService)
        .deleteCandidateJobExperience(id);

    NoSuchObjectException exception = assertThrows(
        NoSuchObjectException.class,
        () -> candidateJobExperiencePortalApi.deleteJobExperience(id)
    );

    assertEquals("No such job experience", exception.getMessage());
    verify(candidateJobExperienceService).deleteCandidateJobExperience(id);
  }

  private CandidateJobExperience createSampleJobExperience() {
    CandidateJobExperience jobExperience = new CandidateJobExperience();
    jobExperience.setId(1L);
    Country country = new Country();
    country.setId(1L);
    country.setName("Test Country");
    jobExperience.setCountry(country);
    CandidateOccupation candidateOccupation = new CandidateOccupation();
    candidateOccupation.setId(1L);
    Occupation occupation = new Occupation();
    occupation.setId(101L);
    occupation.setName("Software Developer");
    candidateOccupation.setOccupation(occupation);
    jobExperience.setCandidateOccupation(candidateOccupation);
    jobExperience.setCompanyName("Test Company");
    jobExperience.setRole("Software Engineer");
    jobExperience.setStartDate(LocalDate.of(2020, 1, 1));
    jobExperience.setEndDate(LocalDate.of(2022, 12, 31));
    jobExperience.setFullTime(true);
    jobExperience.setPaid(true);
    jobExperience.setDescription("Developed software applications");
    return jobExperience;
  }
}