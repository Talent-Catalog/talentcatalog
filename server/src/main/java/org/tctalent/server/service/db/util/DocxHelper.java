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

import static org.tctalent.server.service.db.util.DocxFormatterHelper.DOCX_GENERATION_ERROR_MESSAGE;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.CvGenerationException;
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

  private final CvTemplateHelper cvTemplateHelper;
  private final DocxFormatterHelper docxFormatterHelper;

  /**
   * Generates a DOCX CV for the given candidate.
   *
   * @param candidate candidate whose CV should be generated
   * @param showName whether the candidate name should be included in the CV
   * @param showContact whether contact details should be included in the CV
   * @return generated DOCX as a Spring {@link Resource}
   * @throws CvGenerationException if the CV cannot be rendered or converted to DOCX
   */
  public Resource generateDocx(Candidate candidate, Boolean showName, Boolean showContact) {
    try {
      String xhtml = cvTemplateHelper.renderCvXhtml(candidate, showName, showContact);
      byte[] docxBytes = docxFormatterHelper.formatXhtmlAsDocx(xhtml, cvTemplateHelper.getResourceBaseUrl());

      return new ByteArrayResource(docxBytes);
    } catch (CvGenerationException e) {
      LogBuilder.builder(log)
          .action("generateDocx")
          .message(DOCX_GENERATION_ERROR_MESSAGE)
          .logError(e);

      throw e;
    } catch (Exception e) {
      LogBuilder.builder(log)
          .action("generateDocx")
          .message(DOCX_GENERATION_ERROR_MESSAGE)
          .logError(e);

      throw new CvGenerationException(DOCX_GENERATION_ERROR_MESSAGE, e);
    }
  }
}
