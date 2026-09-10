/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or any later version.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
class PdfHelperTest {

  @Mock
  private CvTemplateHelper cvTemplateHelper;

  private PdfHelper pdfHelper;

  @BeforeEach
  void setUp() {
    pdfHelper = new PdfHelper(cvTemplateHelper);
  }

  @Test
  void generatePdf_whenRenderingAndPdfCreationSucceed_returnsPdfResource() throws Exception {
    Candidate candidate = new Candidate();
    String xhtml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE html>
            <html xmlns="http://www.w3.org/1999/xhtml">
              <head>
                <title>Candidate CV</title>
              </head>
              <body>
                <h1>Candidate CV</h1>
                <p>This is a test CV.</p>
              </body>
            </html>
            """;

    when(cvTemplateHelper.renderCvXhtml(candidate, true, true))
        .thenReturn(xhtml);

    Resource resource = pdfHelper.generatePdf(candidate, true, true);

    ByteArrayResource byteArrayResource =
        assertInstanceOf(ByteArrayResource.class, resource);

    byte[] pdfBytes = byteArrayResource.getByteArray();

    assertTrue(pdfBytes.length > 0);
    assertTrue(
        new String(pdfBytes, 0, 4, StandardCharsets.US_ASCII).startsWith("%PDF")
    );

    verify(cvTemplateHelper).renderCvXhtml(candidate, true, true);
  }

  @Test
  void generatePdf_whenRenderingFails_throwsCvGenerationException() {
    Candidate candidate = new Candidate();
    RuntimeException renderingException = new RuntimeException("Template failed");

    when(cvTemplateHelper.renderCvXhtml(candidate, false, false))
        .thenThrow(renderingException);

    CvGenerationException thrown = assertThrows(
        CvGenerationException.class,
        () -> pdfHelper.generatePdf(candidate, false, false)
    );

    assertEquals("Template failed", thrown.getMessage());

    verify(cvTemplateHelper).renderCvXhtml(candidate, false, false);
  }
}