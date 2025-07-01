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
import org.tctalent.server.model.db.EducationMajor;
import org.tctalent.server.service.db.EducationMajorService;

class EducationMajorPortalApiTest {

  @Mock
  private EducationMajorService educationMajorService;

  @InjectMocks
  private EducationMajorPortalApi educationMajorPortalApi;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testListAllEducationMajors_Success() {
    List<EducationMajor> educationMajors = List.of(createSampleEducationMajor());
    when(educationMajorService.listActiveEducationMajors()).thenReturn(educationMajors);

    List<Map<String, Object>> result = educationMajorPortalApi.listAllEducationMajors();

    assertNotNull(result);
    assertEquals(1, result.size());
    Map<String, Object> educationMajorDto = result.get(0);
    assertEquals(1L, educationMajorDto.get("id"));
    assertEquals("Computer Science", educationMajorDto.get("name"));
    verify(educationMajorService).listActiveEducationMajors();
  }

  @Test
  void testListAllEducationMajors_EmptyList() {
    when(educationMajorService.listActiveEducationMajors()).thenReturn(Collections.emptyList());

    List<Map<String, Object>> result = educationMajorPortalApi.listAllEducationMajors();

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(educationMajorService).listActiveEducationMajors();
  }

  private EducationMajor createSampleEducationMajor() {
    EducationMajor educationMajor = new EducationMajor();
    educationMajor.setId(1L);
    educationMajor.setName("Computer Science");
    return educationMajor;
  }
}