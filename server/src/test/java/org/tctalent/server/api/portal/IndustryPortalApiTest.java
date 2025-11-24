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
import org.tctalent.server.model.db.Industry;
import org.tctalent.server.service.db.IndustryService;

class IndustryPortalApiTest {

  @Mock
  private IndustryService industryService;

  @InjectMocks
  private IndustryPortalApi industryPortalApi;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testListAllIndustries_Success() {
    List<Industry> industries = List.of(createSampleIndustry());
    when(industryService.listIndustries()).thenReturn(industries);

    List<Map<String, Object>> result = industryPortalApi.listAllIndustries();

    assertNotNull(result);
    assertEquals(1, result.size());
    Map<String, Object> industryDto = result.get(0);
    assertEquals(1L, industryDto.get("id"));
    assertEquals("Information Technology", industryDto.get("name"));
    verify(industryService).listIndustries();
  }

  @Test
  void testListAllIndustries_EmptyList() {
    when(industryService.listIndustries()).thenReturn(Collections.emptyList());

    List<Map<String, Object>> result = industryPortalApi.listAllIndustries();

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(industryService).listIndustries();
  }

  private Industry createSampleIndustry() {
    Industry industry = new Industry();
    industry.setId(1L);
    industry.setName("Information Technology");
    return industry;
  }
}