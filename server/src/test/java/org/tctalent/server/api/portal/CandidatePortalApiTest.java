package org.tctalent.server.api.portal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateCertification;
import org.tctalent.server.model.db.CandidateEducation;
import org.tctalent.server.model.db.CandidateExam;
import org.tctalent.server.model.db.CandidateJobExperience;
import org.tctalent.server.model.db.CandidateLanguage;
import org.tctalent.server.model.db.CandidateOccupation;
import org.tctalent.server.model.db.CandidateStatus;
import org.tctalent.server.model.db.EducationLevel;
import org.tctalent.server.model.db.Exam;
import org.tctalent.server.model.db.Gender;
import org.tctalent.server.model.db.Language;
import org.tctalent.server.model.db.LanguageLevel;
import org.tctalent.server.model.db.Occupation;
import org.tctalent.server.model.db.SurveyType;
import org.tctalent.server.model.db.User;
import org.tctalent.server.request.candidate.SubmitRegistrationRequest;
import org.tctalent.server.request.candidate.UpdateCandidateEducationRequest;
import org.tctalent.server.request.candidate.UpdateCandidateOtherInfoRequest;
import org.tctalent.server.request.candidate.UpdateCandidatePersonalRequest;
import org.tctalent.server.request.candidate.UpdateCandidateSurveyRequest;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.service.db.OccupationService;
import org.tctalent.server.util.dto.DtoBuilder;

class CandidatePortalApiTest {

  @Mock
  private CandidateService candidateService;

  @Mock
  private CountryService countryService;

  @Mock
  private OccupationService occupationService;

  @Mock
  private HttpServletResponse response;

  @InjectMocks
  private CandidatePortalApi candidatePortalApi;

  private Candidate loggedInCandidate;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    loggedInCandidate = createSampleCandidate();
    when(candidateService.getLoggedInCandidate()).thenReturn(Optional.of(loggedInCandidate));
    when(candidateService.getLoggedInCandidateLoadCandidateOccupations()).thenReturn(
        Optional.of(loggedInCandidate));
    when(candidateService.getLoggedInCandidateLoadCandidateExams()).thenReturn(
        Optional.of(loggedInCandidate));
    when(candidateService.getLoggedInCandidateLoadCandidateLanguages()).thenReturn(
        Optional.of(loggedInCandidate));
    when(candidateService.getLoggedInCandidateLoadCertifications()).thenReturn(
        Optional.of(loggedInCandidate));
    when(candidateService.getLoggedInCandidateLoadDestinations()).thenReturn(
        Optional.of(loggedInCandidate));
    when(countryService.selectBuilder()).thenReturn(new DtoBuilder().add("id").add("name"));
    when(occupationService.selectBuilder()).thenReturn(new DtoBuilder().add("id").add("name"));
  }

  @Test
  void testGetCandidateProfile_Success() {
    Map<String, Object> result = candidatePortalApi.getCandidateProfile();

    assertNotNull(result);
    assertEquals("12345", result.get("candidateNumber"));
    assertEquals(Gender.male, result.get("gender"));
    verify(candidateService).getLoggedInCandidate();
  }

  @Test
  void testGetCandidateProfile_NotLoggedIn() {
    when(candidateService.getLoggedInCandidate()).thenReturn(Optional.empty());

    InvalidSessionException exception = assertThrows(
        InvalidSessionException.class,
        () -> candidatePortalApi.getCandidateProfile()
    );
    assertEquals("Not logged in", exception.getMessage());
    verify(candidateService).getLoggedInCandidate();
  }

  @Test
  void testUpdateCandidatePersonal_Success() {
    UpdateCandidatePersonalRequest request = new UpdateCandidatePersonalRequest();
    request.setGender(Gender.male);
    when(candidateService.updatePersonal(request)).thenReturn(loggedInCandidate);

    Map<String, Object> result = candidatePortalApi.updateCandidatePersonal(request);

    assertNotNull(result);
    assertEquals(Gender.male, result.get("gender"));
    verify(candidateService).updatePersonal(request);
  }

  @SuppressWarnings("unchecked")
  @Test
  void testGetCandidateCandidateOccupations_Success() {
    CandidateOccupation occupation = createSampleCandidateOccupation();
    loggedInCandidate.setCandidateOccupations(List.of(occupation));
    when(candidateService.getLoggedInCandidateLoadCandidateOccupations())
        .thenReturn(Optional.of(loggedInCandidate));

    Map<String, Object> result = candidatePortalApi.getCandidateCandidateOccupations();

    assertNotNull(result);
    List<Map<String, Object>> occupations = (List<Map<String, Object>>) result.get(
        "candidateOccupations");
    assertEquals(1, occupations.size());

    Map<String, Object> occupationMap = (Map<String, Object>) occupations.get(0).get("occupation");
    assertEquals("Engineer", occupationMap.get("name"));

    verify(candidateService).getLoggedInCandidateLoadCandidateOccupations();
  }

  @SuppressWarnings("unchecked")
  @Test
  void testGetCandidateCandidateExams_Success() {
    CandidateExam exam = createSampleCandidateExam();
    loggedInCandidate.setCandidateExams(List.of(exam));

    Map<String, Object> result = candidatePortalApi.getCandidateCandidateExams();

    assertNotNull(result);
    List<Map<String, Object>> exams = (List<Map<String, Object>>) result.get("candidateExams");
    assertEquals(1, exams.size());
    assertEquals(Exam.IELTSGen, exams.get(0).get("exam"));
    verify(candidateService).getLoggedInCandidateLoadCandidateExams();
  }

  @SuppressWarnings("unchecked")
  @Test
  void testGetCandidateEducation_Success() {
    EducationLevel educationLevel = createSampleEducationLevel();
    CandidateEducation education = createSampleCandidateEducation();
    loggedInCandidate.setMaxEducationLevel(educationLevel);
    loggedInCandidate.setCandidateEducations(List.of(education));

    Map<String, Object> result = candidatePortalApi.getCandidateEducation();

    assertNotNull(result);
    Map<String, Object> maxEducationLevel = (Map<String, Object>) result.get("maxEducationLevel");
    assertEquals("Bachelor", maxEducationLevel.get("name"));
    List<Map<String, Object>> educations = (List<Map<String, Object>>) result.get(
        "candidateEducations");
    assertEquals("Computer Science", educations.get(0).get("courseName"));
    verify(candidateService).getLoggedInCandidate();
  }

  @Test
  void testUpdateCandidateEducationLevel_Success() {
    UpdateCandidateEducationRequest request = new UpdateCandidateEducationRequest();
    request.setMaxEducationLevelId(1L);
    when(candidateService.updateEducation(request)).thenReturn(loggedInCandidate);

    Map<String, Object> result = candidatePortalApi.updateCandidateEducationLevel(request);

    assertNotNull(result);
    verify(candidateService).updateEducation(request);
  }

  @SuppressWarnings("unchecked")
  @Test
  void testGetCandidateLanguages_Success() {
    CandidateLanguage language = createSampleCandidateLanguage();
    loggedInCandidate.setCandidateLanguages(List.of(language));

    Map<String, Object> result = candidatePortalApi.getCandidateLanguages();

    assertNotNull(result);
    List<Map<String, Object>> languages = (List<Map<String, Object>>) result.get(
        "candidateLanguages");
    assertEquals(1, languages.size());
    Map<String, Object> languageDto = (Map<String, Object>) languages.get(0).get("language");
    assertEquals("English", languageDto.get("name"));
    verify(candidateService).getLoggedInCandidateLoadCandidateLanguages();
  }

  @Test
  void testGetCandidateAdditionalInfo_Success() {
    Map<String, Object> result = candidatePortalApi.getCandidateAdditionalInfo();

    assertNotNull(result);
    assertEquals("Additional info", result.get("additionalInfo"));
    verify(candidateService).getLoggedInCandidate();
  }

  @Test
  void testUpdateCandidateAdditionalInfo_Success() {
    UpdateCandidateOtherInfoRequest request = new UpdateCandidateOtherInfoRequest();
    request.setAdditionalInfo("New info");
    when(candidateService.updateOtherInfo(request)).thenReturn(loggedInCandidate);

    Map<String, Object> result = candidatePortalApi.updateCandidateAdditionalInfo(request);

    assertNotNull(result);
    assertEquals("Additional info", result.get("additionalInfo"));
    verify(candidateService).updateOtherInfo(request);
  }

  @SuppressWarnings("unchecked")
  @Test
  void testGetCandidateSurvey_Success() {
    SurveyType surveyType = createSampleSurveyType();
    loggedInCandidate.setSurveyType(surveyType);
    loggedInCandidate.setSurveyComment("Comment");

    Map<String, Object> result = candidatePortalApi.getCandidateSurvey();

    assertNotNull(result);
    Map<String, Object> surveyTypeDto = (Map<String, Object>) result.get("surveyType");
    assertEquals("Feedback", surveyTypeDto.get("name"));
    assertEquals("Comment", result.get("surveyComment"));
    verify(candidateService).getLoggedInCandidate();
  }

  @Test
  void testUpdateCandidateSurvey_Success() {
    UpdateCandidateSurveyRequest request = new UpdateCandidateSurveyRequest();
    request.setSurveyComment("New comment");
    when(candidateService.updateCandidateSurvey(request)).thenReturn(loggedInCandidate);

    Map<String, Object> result = candidatePortalApi.updateCandidateSurvey(request);

    assertNotNull(result);
    verify(candidateService).updateCandidateSurvey(request);
  }

  @SuppressWarnings("unchecked")
  @Test
  void testGetCandidateJobExperiences_Success() {
    CandidateJobExperience jobExperience = createSampleJobExperience();
    loggedInCandidate.setCandidateJobExperiences(List.of(jobExperience));

    Map<String, Object> result = candidatePortalApi.getCandidateJobExperiences();

    assertNotNull(result);
    List<Map<String, Object>> experiences = (List<Map<String, Object>>) result.get(
        "candidateJobExperiences");
    assertEquals(1, experiences.size());
    assertEquals("Software Developer", experiences.get(0).get("role"));
    verify(candidateService).getLoggedInCandidate();
  }

  @SuppressWarnings("unchecked")
  @Test
  void testGetCandidateCertifications_Success() {
    CandidateCertification certification = createSampleCertification();
    loggedInCandidate.setCandidateCertifications(List.of(certification));

    Map<String, Object> result = candidatePortalApi.getCandidateCertifications();

    assertNotNull(result);
    List<Map<String, Object>> certifications = (List<Map<String, Object>>) result.get(
        "candidateCertifications");
    assertEquals(1, certifications.size());
    assertEquals("Java Certification", certifications.get(0).get("name"));
    verify(candidateService).getLoggedInCandidateLoadCertifications();
  }


  @Test
  void testGetCandidateStatus_Success() {
    Map<String, Object> result = candidatePortalApi.getCandidateStatus();

    assertNotNull(result);
    assertEquals(CandidateStatus.active, result.get("status"));
    verify(candidateService).getLoggedInCandidate();
  }

  @Test
  void testGetCandidateNumber_Success() {
    Map<String, Object> result = candidatePortalApi.getCandidateNumber();

    assertNotNull(result);
    assertEquals("12345", result.get("candidateNumber"));
    verify(candidateService).getLoggedInCandidate();
  }

  @Test
  void testGetCandidateCV_Success() throws IOException {
    Resource cvResource = new ByteArrayResource("CV content".getBytes());
    when(candidateService.generateCv(loggedInCandidate, true, true)).thenReturn(cvResource);
    when(response.getOutputStream()).thenReturn(new jakarta.servlet.ServletOutputStream() {
      @Override
      public void write(int b) {
      }

      @Override
      public boolean isReady() {
        return true;
      }

      @Override
      public void setWriteListener(jakarta.servlet.WriteListener writeListener) {
      }
    });

    candidatePortalApi.getCandidateCV(response);

    verify(candidateService).getLoggedInCandidate();
    verify(candidateService).generateCv(loggedInCandidate, true, true);
    verify(response).setContentType("application/pdf");
    verify(response).setHeader(eq("Content-Disposition"), contains("John Doe-CV.pdf"));
    verify(response).getOutputStream();
    verify(response).flushBuffer();
  }

  @Test
  void testSubmitRegistration_Success() {
    SubmitRegistrationRequest request = new SubmitRegistrationRequest();
    when(candidateService.submitRegistration(request)).thenReturn(loggedInCandidate);

    Map<String, Object> result = candidatePortalApi.submitRegistration(request);

    assertNotNull(result);
    assertEquals(CandidateStatus.active, result.get("status"));
    verify(candidateService).submitRegistration(request);
  }

  private Candidate createSampleCandidate() {
    Candidate candidate = new Candidate();
    candidate.setCandidateNumber("12345");
    candidate.setGender(Gender.male);
    candidate.setStatus(CandidateStatus.active);
    candidate.setAdditionalInfo("Additional info");
    User user = new User();
    user.setFirstName("John");
    user.setLastName("Doe");
    candidate.setUser(user);
    return candidate;
  }

  private CandidateOccupation createSampleCandidateOccupation() {
    CandidateOccupation occupation = new CandidateOccupation();
    Occupation occ = new Occupation();
    occ.setName("Engineer");
    occupation.setOccupation(occ);
    occupation.setYearsExperience(5L);
    return occupation;
  }

  private CandidateExam createSampleCandidateExam() {
    CandidateExam exam = new CandidateExam();
    exam.setExam(Exam.IELTSGen);
    exam.setScore("7.0");
    return exam;
  }

  private EducationLevel createSampleEducationLevel() {
    EducationLevel level = new EducationLevel();
    level.setName("Bachelor");
    return level;
  }

  private CandidateEducation createSampleCandidateEducation() {
    CandidateEducation education = new CandidateEducation();
    education.setCourseName("Computer Science");
    return education;
  }

  private CandidateLanguage createSampleCandidateLanguage() {
    CandidateLanguage language = new CandidateLanguage();
    Language lang = new Language();
    lang.setName("English");
    language.setLanguage(lang);
    LanguageLevel level = new LanguageLevel();
    level.setName("Fluent");
    language.setWrittenLevel(level);
    language.setSpokenLevel(level);
    return language;
  }

  private SurveyType createSampleSurveyType() {
    SurveyType surveyType = new SurveyType();
    surveyType.setName("Feedback");
    return surveyType;
  }

  private CandidateJobExperience createSampleJobExperience() {
    CandidateJobExperience experience = new CandidateJobExperience();
    experience.setRole("Software Developer");
    return experience;
  }

  private CandidateCertification createSampleCertification() {
    CandidateCertification certification = new CandidateCertification();
    certification.setName("Java Certification");
    return certification;
  }

}
