package org.tctalent.server.api.portal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.tctalent.server.model.db.CandidateEducation;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.EducationMajor;
import org.tctalent.server.model.db.EducationType;
import org.tctalent.server.request.candidate.education.CreateCandidateEducationRequest;
import org.tctalent.server.request.candidate.education.UpdateCandidateEducationRequest;
import org.tctalent.server.service.db.CandidateEducationService;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.util.dto.DtoBuilder;

class CandidateEducationPortalApiTest {

  @Mock
  private CandidateEducationService candidateEducationService;

  @Mock
  private CountryService countryService;

  @InjectMocks
  private CandidateEducationPortalApi candidateEducationPortalApi;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    when(countryService.selectBuilder()).thenReturn(new DtoBuilder()
        .add("id")
        .add("name"));
  }

  @Test
  void testCreateCandidateEducation_Success() {
    CreateCandidateEducationRequest request = new CreateCandidateEducationRequest();
    CandidateEducation education = createSampleEducation();
    when(candidateEducationService.createCandidateEducation(
        any(CreateCandidateEducationRequest.class)))
        .thenReturn(education);

    Map<String, Object> result = candidateEducationPortalApi.createCandidateEducation(request);

    assertNotNull(result);
    assertEquals(1L, result.get("id"));
    assertEquals(EducationType.Associate, result.get("educationType"));
    assertEquals("Test University", result.get("institution"));
    assertEquals("Computer Science", result.get("courseName"));
    assertEquals(4, result.get("lengthOfCourseYears"));
    assertEquals(2020, result.get("yearCompleted"));
    assertFalse((Boolean) result.get("incomplete"));
    @SuppressWarnings("unchecked")
    Map<String, Object> country = (Map<String, Object>) result.get("country");
    assertEquals(1L, country.get("id"));
    assertEquals("Test Country", country.get("name"));
    @SuppressWarnings("unchecked")
    Map<String, Object> major = (Map<String, Object>) result.get("educationMajor");
    assertEquals(1L, major.get("id"));
    assertEquals("Computer Science", major.get("name"));
    verify(candidateEducationService).createCandidateEducation(request);
  }

  @Test
  void testUpdateCandidateEducation_Success() {
    UpdateCandidateEducationRequest request = new UpdateCandidateEducationRequest();
    CandidateEducation education = createSampleEducation();
    when(candidateEducationService.updateCandidateEducation(
        any(UpdateCandidateEducationRequest.class)))
        .thenReturn(education);

    Map<String, Object> result = candidateEducationPortalApi.updateCandidateEducation(request);

    assertNotNull(result);
    assertEquals(1L, result.get("id"));
    assertEquals(EducationType.Associate, result.get("educationType"));
    assertEquals("Test University", result.get("institution"));
    assertEquals("Computer Science", result.get("courseName"));
    assertEquals(4, result.get("lengthOfCourseYears"));
    assertEquals(2020, result.get("yearCompleted"));
    assertFalse((Boolean) result.get("incomplete"));
    @SuppressWarnings("unchecked")
    Map<String, Object> country = (Map<String, Object>) result.get("country");
    assertEquals(1L, country.get("id"));
    assertEquals("Test Country", country.get("name"));
    @SuppressWarnings("unchecked")
    Map<String, Object> major = (Map<String, Object>) result.get("educationMajor");
    assertEquals(1L, major.get("id"));
    assertEquals("Computer Science", major.get("name"));
    verify(candidateEducationService).updateCandidateEducation(request);
  }

  @Test
  void testDeleteCandidateEducation_Success() {
    Long id = 1L;
    doNothing().when(candidateEducationService).deleteCandidateEducation(id);

    ResponseEntity result = candidateEducationPortalApi.deleteCandidateEducation(id);

    assertNotNull(result);
    assertEquals(200, result.getStatusCodeValue());
    verify(candidateEducationService).deleteCandidateEducation(id);
  }

  private CandidateEducation createSampleEducation() {
    CandidateEducation education = new CandidateEducation();
    education.setId(1L);
    education.setEducationType(EducationType.Associate);
    Country country = new Country();
    country.setId(1L);
    country.setName("Test Country");
    education.setCountry(country);
    EducationMajor major = new EducationMajor();
    major.setId(1L);
    major.setName("Computer Science");
    education.setEducationMajor(major);
    education.setLengthOfCourseYears(4);
    education.setInstitution("Test University");
    education.setCourseName("Computer Science");
    education.setIncomplete(false);
    education.setYearCompleted(2020);
    return education;
  }
}