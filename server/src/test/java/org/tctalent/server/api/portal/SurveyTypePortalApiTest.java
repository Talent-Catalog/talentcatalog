package org.tctalent.server.api.portal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.tctalent.server.model.db.SurveyType;
import org.tctalent.server.service.db.SurveyTypeService;
import org.tctalent.server.util.dto.DtoBuilder;

class SurveyTypePortalApiTest {

  @Mock
  private SurveyTypeService surveyTypeService;

  @InjectMocks
  private SurveyTypePortalApi surveyTypePortalApi;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testListActiveSurveyTypes_Success() {
    List<SurveyType> surveyTypes = List.of(createSampleSurveyType());
    when(surveyTypeService.listActiveSurveyTypes()).thenReturn(surveyTypes);

    List<Map<String, Object>> result = surveyTypePortalApi.listActiveSurveyTypes();

    assertNotNull(result);
    assertEquals(1, result.size());
    Map<String, Object> surveyTypeDto = result.get(0);
    assertEquals(1L, surveyTypeDto.get("id"));
    assertEquals("Feedback Survey", surveyTypeDto.get("name"));
    verify(surveyTypeService).listActiveSurveyTypes();
  }

  @Test
  void testListActiveSurveyTypes_EmptyList() {
    when(surveyTypeService.listActiveSurveyTypes()).thenReturn(Collections.emptyList());

    List<Map<String, Object>> result = surveyTypePortalApi.listActiveSurveyTypes();

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(surveyTypeService).listActiveSurveyTypes();
  }

  private SurveyType createSampleSurveyType() {
    SurveyType surveyType = new SurveyType();
    surveyType.setId(1L);
    surveyType.setName("Feedback Survey");
    return surveyType;
  }
}