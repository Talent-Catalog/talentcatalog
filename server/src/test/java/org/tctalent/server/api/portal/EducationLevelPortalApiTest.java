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
import org.tctalent.server.model.db.EducationLevel;
import org.tctalent.server.model.db.EducationType;
import org.tctalent.server.service.db.EducationLevelService;

class EducationLevelPortalApiTest {

  @Mock
  private EducationLevelService educationLevelService;

  @InjectMocks
  private EducationLevelPortalApi educationLevelPortalApi;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testListAllEducationLevels_Success() {
    List<EducationLevel> educationLevels = List.of(createSampleEducationLevel());
    when(educationLevelService.listEducationLevels()).thenReturn(educationLevels);

    List<Map<String, Object>> result = educationLevelPortalApi.listAllEducationLevels();

    assertNotNull(result);
    assertEquals(1, result.size());
    Map<String, Object> educationLevelDto = result.get(0);
    assertEquals(1L, educationLevelDto.get("id"));
    assertEquals("Bachelor", educationLevelDto.get("name"));
    assertEquals(16, educationLevelDto.get("level"));
    assertEquals(EducationType.Doctoral, educationLevelDto.get("educationType"));
    verify(educationLevelService).listEducationLevels();
  }

  @Test
  void testListAllEducationLevels_EmptyList() {
    when(educationLevelService.listEducationLevels()).thenReturn(Collections.emptyList());

    List<Map<String, Object>> result = educationLevelPortalApi.listAllEducationLevels();

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(educationLevelService).listEducationLevels();
  }

  private EducationLevel createSampleEducationLevel() {
    EducationLevel educationLevel = new EducationLevel();
    educationLevel.setId(1L);
    educationLevel.setName("Bachelor");
    educationLevel.setLevel(16);
    educationLevel.setEducationType(EducationType.Doctoral);
    return educationLevel;
  }
}