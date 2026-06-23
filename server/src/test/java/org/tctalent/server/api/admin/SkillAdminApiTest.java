package org.tctalent.server.api.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.tctalent.server.service.api.ExtractSkillsRequest;
import org.tctalent.server.service.api.SkillName;
import org.tctalent.server.service.db.SkillsService;

@ExtendWith(MockitoExtension.class)
class SkillAdminApiTest {

  @Mock
  private SkillsService skillsService;

  @InjectMocks
  private SkillAdminApi skillAdminApi;

  @Test
  void getSkillNamesReturnsPagedSkillNames() {
    int page = 2;
    int size = 25;
    String lang = "ar";

    PageRequest expectedPageRequest = PageRequest.of(page, size);
    Page<SkillName> expectedPage = new PageImpl<>(
        List.of(),
        expectedPageRequest,
        0
    );

    when(skillsService.getSkillNames(expectedPageRequest, lang))
        .thenReturn(expectedPage);

    Page<SkillName> result = skillAdminApi.getSkillNames(page, size, lang);

    assertSame(expectedPage, result);
    assertEquals(page, result.getPageable().getPageNumber());
    assertEquals(size, result.getPageable().getPageSize());

    verify(skillsService).getSkillNames(expectedPageRequest, lang);
  }

  @Test
  void extractSkillNamesReturnsExtractedSkillNames() {
    ExtractSkillsRequest request = org.mockito.Mockito.mock(ExtractSkillsRequest.class);
    List<SkillName> expectedSkills = List.of();

    when(request.getText()).thenReturn("Java Spring SQL");
    when(request.getLang()).thenReturn("en");
    when(skillsService.extractSkillNames(eq("Java Spring SQL"), eq("en")))
        .thenReturn(expectedSkills);

    List<SkillName> result = skillAdminApi.extractSkillNames(request);

    assertSame(expectedSkills, result);

    verify(skillsService).extractSkillNames("Java Spring SQL", "en");
  }
}