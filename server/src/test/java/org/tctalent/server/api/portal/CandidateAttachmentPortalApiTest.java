package org.tctalent.server.api.portal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.model.db.AttachmentType;
import org.tctalent.server.model.db.CandidateAttachment;
import org.tctalent.server.model.db.User;
import org.tctalent.server.request.PagedSearchRequest;
import org.tctalent.server.request.attachment.CreateCandidateAttachmentRequest;
import org.tctalent.server.request.attachment.UpdateCandidateAttachmentRequest;
import org.tctalent.server.service.db.CandidateAttachmentService;

class CandidateAttachmentPortalApiTest {

  @Mock
  private CandidateAttachmentService candidateAttachmentService;

  @Mock
  private HttpServletResponse response;

  @Mock
  private MultipartFile file;

  @Mock
  private ServletOutputStream servletOutputStream;

  @InjectMocks
  private CandidateAttachmentPortalApi candidateAttachmentPortalApi;

  @BeforeEach
  void setUp() throws IOException {
    MockitoAnnotations.openMocks(this);
    when(response.getOutputStream()).thenReturn(servletOutputStream);
  }

  @Test
  void testList_Success() {
    CandidateAttachment attachment = createSampleAttachment();
    when(candidateAttachmentService.listCandidateAttachmentsForLoggedInCandidate())
        .thenReturn(List.of(attachment));

    List<Map<String, Object>> result = candidateAttachmentPortalApi.list();

    assertNotNull(result);
    assertEquals(1, result.size());
    Map<String, Object> attachmentDto = result.get(0);
    assertEquals(1L, attachmentDto.get("id"));
    assertEquals("test.pdf", attachmentDto.get("name"));
    assertEquals(AttachmentType.file, attachmentDto.get("type"));
    verify(candidateAttachmentService).listCandidateAttachmentsForLoggedInCandidate();
  }

  @Test
  void testSearch_Success() {
    PagedSearchRequest request = new PagedSearchRequest();
    CandidateAttachment attachment = createSampleAttachment();
    Page<CandidateAttachment> page = new PageImpl<>(List.of(attachment));
    when(candidateAttachmentService.searchCandidateAttachmentsForLoggedInCandidate(request))
        .thenReturn(page);

    Map<String, Object> result = candidateAttachmentPortalApi.search(request);

    assertNotNull(result);
    assertTrue(result.containsKey("content"));
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> content = (List<Map<String, Object>>) result.get("content");
    assertEquals(1, content.size());
    Map<String, Object> attachmentDto = content.get(0);
    assertEquals(1L, attachmentDto.get("id"));
    assertEquals("test.pdf", attachmentDto.get("name"));
    verify(candidateAttachmentService).searchCandidateAttachmentsForLoggedInCandidate(request);
  }

  @Test
  void testCreateCandidateAttachment_Success() {
    CreateCandidateAttachmentRequest request = new CreateCandidateAttachmentRequest();
    CandidateAttachment attachment = createSampleAttachment();
    when(candidateAttachmentService.createCandidateAttachment(request)).thenReturn(attachment);

    Map<String, Object> result = candidateAttachmentPortalApi.createCandidateAttachment(request);

    assertNotNull(result);
    assertEquals(1L, result.get("id"));
    assertEquals("test.pdf", result.get("name"));
    verify(candidateAttachmentService).createCandidateAttachment(request);
  }

  @Test
  void testDeleteCandidateAttachment_Success() {
    Long id = 1L;
    doNothing().when(candidateAttachmentService).deleteCandidateAttachment(id);

    ResponseEntity<Object> result = candidateAttachmentPortalApi.deleteCandidateAttachment(id);

    assertNotNull(result);
    assertEquals(200, result.getStatusCodeValue());
    verify(candidateAttachmentService).deleteCandidateAttachment(id);
  }

  @Test
  void testDownloadAttachment_Success() throws IOException {
    Long id = 1L;
    CandidateAttachment attachment = createSampleAttachment();
    attachment.setType(AttachmentType.googlefile);
    attachment.setName("test.pdf");
    when(candidateAttachmentService.getCandidateAttachment(id)).thenReturn(attachment);

    candidateAttachmentPortalApi.downloadAttachment(id, response);

    verify(response).setHeader("Content-Disposition", "attachment; filename=\"test.pdf\"");
    verify(response).setContentType("application/octet-stream");
    verify(candidateAttachmentService).getCandidateAttachment(id);
    verify(candidateAttachmentService).downloadCandidateAttachment(eq(attachment), eq(servletOutputStream));
    verify(response).flushBuffer();
  }

  @Test
  void testUploadAttachment_Success() throws IOException {
    Boolean cv = true;
    CandidateAttachment attachment = createSampleAttachment();
    when(candidateAttachmentService.uploadAttachment(cv, file)).thenReturn(attachment);

    Map<String, Object> result = candidateAttachmentPortalApi.uploadAttachment(cv, file);

    assertNotNull(result);
    assertEquals(1L, result.get("id"));
    assertEquals("test.pdf", result.get("name"));
    verify(candidateAttachmentService).uploadAttachment(cv, file);
  }

  @Test
  void testUpdate_Success() throws IOException {
    Long id = 1L;
    UpdateCandidateAttachmentRequest request = new UpdateCandidateAttachmentRequest();
    CandidateAttachment attachment = createSampleAttachment();
    when(candidateAttachmentService.updateCandidateAttachment(id, request)).thenReturn(attachment);

    Map<String, Object> result = candidateAttachmentPortalApi.update(id, request);

    assertNotNull(result);
    assertEquals(1L, result.get("id"));
    assertEquals("test.pdf", result.get("name"));
    verify(candidateAttachmentService).updateCandidateAttachment(id, request);
  }

  private CandidateAttachment createSampleAttachment() {
    CandidateAttachment attachment = new CandidateAttachment();
    attachment.setId(1L);
    attachment.setName("test.pdf");
    attachment.setType(AttachmentType.file);
    attachment.setLocation("path/to/test.pdf");
    attachment.setFileType("application/pdf");
    attachment.setMigrated(true);
    attachment.setCv(true);
    User createdBy = new User();
    createdBy.setId(1L);
    createdBy.setFirstName("John");
    createdBy.setLastName("Doe");
    attachment.setCreatedBy(createdBy);
    return attachment;
  }
}