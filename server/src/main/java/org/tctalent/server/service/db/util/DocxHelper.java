/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.db.util;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.docx4j.convert.in.xhtml.XHTMLImporterImpl;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.DocxGenerationException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Candidate;

/**
 * Service for generating DOCX candidate CVs.
 *
 * <p>This helper follows the same high-level approach as PDF generation. The candidate data is
 * first rendered through the shared Thymeleaf CV template as XHTML, then that XHTML is converted
 * into a Word DOCX document using docx4j.</p>
 *
 * <p>Using the shared XHTML keeps the CV layout in one place and avoids maintaining separate PDF
 * and DOCX templates.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocxHelper {

  private static final String PDF_RESOURCE_BASE_PATH = "/pdf/";

  private final PdfHelper pdfHelper;

  /**
   * Generates a DOCX CV for the given candidate.
   *
   * @param candidate candidate whose CV should be generated
   * @param showName whether the candidate name should be included in the CV
   * @param showContact whether contact details should be included in the CV
   * @return generated DOCX as a Spring {@link Resource}
   * @throws DocxGenerationException if the CV cannot be rendered or converted to DOCX
   */
  public Resource generateDocx(Candidate candidate, Boolean showName, Boolean showContact) {
    try {
      String xhtml = pdfHelper.renderCvXhtml(candidate, showName, showContact);

      WordprocessingMLPackage wordPackage = WordprocessingMLPackage.createPackage();
      XHTMLImporterImpl importer = new XHTMLImporterImpl(wordPackage);

      wordPackage.getMainDocumentPart()
          .getContent()
          .addAll(importer.convert(xhtml, getResourceBaseUrl()));

      try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
        wordPackage.save(outputStream);
        return new ByteArrayResource(outputStream.toByteArray());
      }
    } catch (Exception e) {
      LogBuilder.builder(log)
          .action("generateDocx")
          .message("Error generating DOCX")
          .logError(e);

      throw new DocxGenerationException("Error generating DOCX", e);
    }
  }

  /**
   * Returns the base URL used by docx4j to resolve relative resources in the rendered CV XHTML.
   *
   * <p>The existing PDF templates use resources under {@code classpath:pdf/}. docx4j needs a real
   * URL string instead of Spring's {@code classpath:} prefix, so this method resolves the classpath
   * location to a URL.</p>
   *
   * @return external URL string for the PDF/CV template resource directory
   */
  private String getResourceBaseUrl() {
    URL resource = getClass().getResource(PDF_RESOURCE_BASE_PATH);

    if (resource == null) {
      LogBuilder.builder(log)
          .action("getResourceBaseUrl")
          .message("PDF resource base path was not found. DOCX images/styles may not resolve.")
          .logWarn();

      return "";
    }

    return resource.toExternalForm();
  }
}