/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */


package org.tctalent.server.service.db.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.tctalent.server.service.db.util.DocxFormatterHelper.DOCX_GENERATION_ERROR_MESSAGE;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.tctalent.server.exception.CvGenerationException;
import org.tctalent.server.model.db.Candidate;

@ExtendWith(MockitoExtension.class)
class DocxHelperTest {

  private static final String RESOURCE_BASE_URL = "file:/test/cv/";

  @Mock
  private CvTemplateHelper cvTemplateHelper;

  @Mock
  private DocxFormatterHelper docxFormatterHelper;

  private DocxHelper docxHelper;

  @BeforeEach
  void setUp() {
    docxHelper = new DocxHelper(cvTemplateHelper, docxFormatterHelper);
  }

  @Test
  void generateDocx_whenSuccessful_returnsByteArrayResource() {
    Candidate candidate = new Candidate();
    String xhtml = "<html><body>CV</body></html>";
    byte[] docxBytes = "generated-docx".getBytes(StandardCharsets.UTF_8);

    when(cvTemplateHelper.renderCvXhtml(candidate, true, false)).thenReturn(xhtml);
    when(cvTemplateHelper.getResourceBaseUrl()).thenReturn(RESOURCE_BASE_URL);
    when(docxFormatterHelper.formatXhtmlAsDocx(xhtml, RESOURCE_BASE_URL)).thenReturn(docxBytes);

    Resource resource = docxHelper.generateDocx(candidate, true, false);

    assertInstanceOf(ByteArrayResource.class, resource);
    assertArrayEquals(docxBytes, ((ByteArrayResource) resource).getByteArray());

    verify(cvTemplateHelper).renderCvXhtml(candidate, true, false);
    verify(cvTemplateHelper).getResourceBaseUrl();
    verify(docxFormatterHelper).formatXhtmlAsDocx(xhtml, RESOURCE_BASE_URL);
  }

  @Test
  void generateDocx_whenFormatterThrowsCvGenerationException_rethrowsSameException() {
    Candidate candidate = new Candidate();
    String xhtml = "<html><body>CV</body></html>";

    CvGenerationException originalException = new CvGenerationException("DOCX formatting failed");

    when(cvTemplateHelper.renderCvXhtml(candidate, true, true)).thenReturn(xhtml);
    when(cvTemplateHelper.getResourceBaseUrl()).thenReturn(RESOURCE_BASE_URL);
    when(docxFormatterHelper.formatXhtmlAsDocx(xhtml, RESOURCE_BASE_URL)).thenThrow(
        originalException);

    CvGenerationException thrown = assertThrows(CvGenerationException.class,
        () -> docxHelper.generateDocx(candidate, true, true));

    assertSame(originalException, thrown);

    verify(cvTemplateHelper).getResourceBaseUrl();
    verify(docxFormatterHelper).formatXhtmlAsDocx(xhtml, RESOURCE_BASE_URL);
  }

  @Test
  void generateDocx_whenUnexpectedExceptionOccurs_wrapsInCvGenerationException() {
    Candidate candidate = new Candidate();
    String xhtml = "<html><body>CV</body></html>";

    RuntimeException formatterException = new RuntimeException("Unexpected formatter failure");

    when(cvTemplateHelper.renderCvXhtml(candidate, false, false)).thenReturn(xhtml);
    when(cvTemplateHelper.getResourceBaseUrl()).thenReturn(RESOURCE_BASE_URL);
    when(docxFormatterHelper.formatXhtmlAsDocx(xhtml, RESOURCE_BASE_URL)).thenThrow(
        formatterException);

    CvGenerationException thrown = assertThrows(CvGenerationException.class,
        () -> docxHelper.generateDocx(candidate, false, false));

    assertEquals(DOCX_GENERATION_ERROR_MESSAGE, thrown.getMessage());
    assertSame(formatterException, thrown.getCause());

    verify(cvTemplateHelper).getResourceBaseUrl();
    verify(docxFormatterHelper).formatXhtmlAsDocx(xhtml, RESOURCE_BASE_URL);
  }

  @Test
  void generateDocx_whenTemplateRenderingThrowsCvGenerationException_doesNotCallFormatter() {
    Candidate candidate = new Candidate();

    CvGenerationException originalException = new CvGenerationException(
        "Template rendering failed");

    when(cvTemplateHelper.renderCvXhtml(candidate, true, true)).thenThrow(originalException);

    CvGenerationException thrown = assertThrows(CvGenerationException.class,
        () -> docxHelper.generateDocx(candidate, true, true));

    assertSame(originalException, thrown);
    verifyNoInteractions(docxFormatterHelper);
  }

  @Test
  void generateDocx_whenResourceBaseUrlFails_rethrowsSameException() {
    Candidate candidate = new Candidate();
    String xhtml = "<html><body>CV</body></html>";

    CvGenerationException originalException = new CvGenerationException("Resource URL failed");

    when(cvTemplateHelper.renderCvXhtml(candidate, true, true)).thenReturn(xhtml);
    when(cvTemplateHelper.getResourceBaseUrl()).thenThrow(originalException);

    CvGenerationException thrown = assertThrows(CvGenerationException.class,
        () -> docxHelper.generateDocx(candidate, true, true));

    assertSame(originalException, thrown);
    verifyNoInteractions(docxFormatterHelper);
  }
}