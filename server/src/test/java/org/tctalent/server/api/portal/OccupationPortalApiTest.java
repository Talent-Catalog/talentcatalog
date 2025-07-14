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
import org.tctalent.server.model.db.Occupation;
import org.tctalent.server.service.db.OccupationService;
import org.tctalent.server.util.dto.DtoBuilder;

class OccupationPortalApiTest {

  @Mock
  private OccupationService occupationService;

  @InjectMocks
  private OccupationPortalApi occupationPortalApi;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    when(occupationService.selectBuilder()).thenReturn(new DtoBuilder()
        .add("id")
        .add("name"));
  }

  @Test
  void testListAllOccupations_Success() {
    List<Occupation> occupations = List.of(createSampleOccupation());
    when(occupationService.listOccupations()).thenReturn(occupations);

    List<Map<String, Object>> result = occupationPortalApi.listAllOccupations();

    assertNotNull(result);
    assertEquals(1, result.size());
    Map<String, Object> occupationDto = result.get(0);
    assertEquals(1L, occupationDto.get("id"));
    assertEquals("Software Engineer", occupationDto.get("name"));
    verify(occupationService).listOccupations();
    verify(occupationService).selectBuilder();
  }

  @Test
  void testListAllOccupations_EmptyList() {
    when(occupationService.listOccupations()).thenReturn(Collections.emptyList());

    List<Map<String, Object>> result = occupationPortalApi.listAllOccupations();

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(occupationService).listOccupations();
    verify(occupationService).selectBuilder();
  }

  private Occupation createSampleOccupation() {
    Occupation occupation = new Occupation();
    occupation.setId(1L);
    occupation.setName("Software Engineer");
    return occupation;
  }
}