package org.tctalent.server.api.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.tctalent.server.model.db.LinkPreview;
import org.tctalent.server.request.chat.link_preview.BuildLinkPreviewRequest;
import org.tctalent.server.service.db.LinkPreviewService;
import org.tctalent.server.util.dto.DtoBuilder;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class LinkPreviewAdminApiTest {

  @Mock
  private LinkPreviewService linkPreviewService;

  @InjectMocks
  private LinkPreviewAdminApi linkPreviewAdminApi;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testDelete_shouldCallServiceAndReturnTrue() {
    // Arrange
    long id = 42L;
    when(linkPreviewService.deleteLinkPreview(id)).thenReturn(true);

    // Act
    boolean result = linkPreviewAdminApi.delete(id);

    // Assert
    assertEquals(true, result);
    verify(linkPreviewService).deleteLinkPreview(id);
  }

  @Test
  void testBuildLinkPreview_shouldReturnMappedDto() throws IOException {
    // Arrange
    BuildLinkPreviewRequest request = new BuildLinkPreviewRequest();
    request.setUrl("https://example.com");

    LinkPreview preview = new LinkPreview();
    preview.setUrl("https://example.com");

    Map<String, Object> expectedDto = Map.of(
        "id", "123",
        "url", "https://example.com",
        "title", "Example Title",
        "description", "Example Description",
        "imageUrl", "https://example.com/image.png",
        "domain", "example.com",
        "faviconUrl", "https://example.com/favicon.ico"
    );

    when(linkPreviewService.buildLinkPreview("https://example.com")).thenReturn(preview);

    // Spy on the real controller to mock private method
    LinkPreviewAdminApi spyApi = Mockito.spy(linkPreviewAdminApi);

    DtoBuilder mockBuilder = mock(DtoBuilder.class);
    when(mockBuilder.build(preview)).thenReturn(expectedDto);
    doReturn(mockBuilder).when(spyApi).linkPreviewDto();

    // Act
    Map<String, Object> result = spyApi.buildLinkPreview(request);

    // Assert
    assertEquals(expectedDto, result);
    verify(linkPreviewService).buildLinkPreview("https://example.com");
  }

  @Test
  void testLinkPreviewDto_shouldContainExpectedFields() {
    DtoBuilder dtoBuilder = linkPreviewAdminApi.linkPreviewDto();
    assertNotNull(dtoBuilder);

    LinkPreview dummy = new LinkPreview();
    dummy.setId(1L);
    dummy.setUrl("https://example.com");
    dummy.setTitle("Test Title");
    dummy.setDescription("Test Desc");
    dummy.setImageUrl("https://example.com/image.png");
    dummy.setDomain("example.com");
    dummy.setFaviconUrl("https://example.com/favicon.ico");

    Map<String, Object> dto = dtoBuilder.build(dummy);

    assertEquals(1L, dto.get("id"));
    assertEquals("https://example.com", dto.get("url"));
    assertEquals("Test Title", dto.get("title"));
    assertEquals("Test Desc", dto.get("description"));
    assertEquals("https://example.com/image.png", dto.get("imageUrl"));
    assertEquals("example.com", dto.get("domain"));
    assertEquals("https://example.com/favicon.ico", dto.get("faviconUrl"));
  }

}
