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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.CvGenerationException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.service.db.impl.TcInstanceService;
import org.tctalent.server.util.text.CandidateTidiedTextViewFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.w3c.tidy.Tidy;
/**
 * Shared service for rendering candidate CV data through the common Thymeleaf CV template.
 *
 * <p>This class deliberately contains only the shared template/XHTML work. PDF conversion belongs
 * in {@link PdfHelper}; DOCX conversion belongs in
 * {@link DocxHelper}.</p>
 */
@Service
@Slf4j
public class CvTemplateHelper {

  private static final String UTF_8 = "UTF-8";
  private static final Pattern NULL_BYTE_PATTERN = Pattern.compile("\\x00");

  private final TemplateEngine pdfTemplateEngine;
  private final TcInstanceService tcInstanceService;
  private final CandidateTidiedTextViewFactory candidateTidiedTextViewFactory;
  private final CvExportDataPreparer cvExportDataPreparer;

  /**
   * Note - we can't use Lombok RequiredArgsConstructor because currently Lombok doesn't copy
   * the @Qualifier annotation to the constructor.
   */
  public CvTemplateHelper(
      @Qualifier("pdfTemplateEngine") TemplateEngine pdfTemplateEngine,
      TcInstanceService tcInstanceService,
      CandidateTidiedTextViewFactory candidateTidiedTextViewFactory,
      CvExportDataPreparer cvExportDataPreparer
  ) {
    this.pdfTemplateEngine = pdfTemplateEngine;
    this.tcInstanceService = tcInstanceService;
    this.candidateTidiedTextViewFactory = candidateTidiedTextViewFactory;
    this.cvExportDataPreparer = cvExportDataPreparer;
  }

  /**
   * Renders candidate CV data using the shared Thymeleaf template and returns XHTML.
   *
   * @param candidate candidate whose CV should be rendered
   * @param showName whether candidate name should be included
   * @param showContact whether candidate contact details should be included
   * @return rendered CV as XHTML
   * @throws CvGenerationException if the template cannot be rendered as XHTML
   */
  public String renderCvXhtml(Candidate candidate, Boolean showName, Boolean showContact) {
    try {
      Candidate preparedCandidate = cvExportDataPreparer.prepare(candidate, showContact);

      Context context = new Context();
      context.setVariable("candidate", candidateTidiedTextViewFactory.create(preparedCandidate));
      context.setVariable("showName", showName);
      context.setVariable("showContact", showContact);
      context.setVariable("logoFile", tcInstanceService.getLogoFile());

      String renderedHtmlContent = pdfTemplateEngine.process("template", context);
      String xhtml = convertToXhtml(renderedHtmlContent);

      // Remove null bytes to avoid invalid XML character errors in PDF/DOCX converters.
      return NULL_BYTE_PATTERN.matcher(xhtml).replaceAll("");
    } catch (Exception e) {
      LogBuilder.builder(log)
          .action("renderCvXhtml")
          .message("Error rendering CV XHTML")
          .logError(e);

      throw new CvGenerationException(e.getMessage());
    }
  }

  private String convertToXhtml(String html) {
    Tidy tidy = new Tidy();
    tidy.setInputEncoding(UTF_8);
    tidy.setOutputEncoding(UTF_8);
    tidy.setXHTML(true);

    try (ByteArrayInputStream inputStream = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
    ) {
      tidy.parseDOM(inputStream, outputStream);

      String xhtml = outputStream.toString(StandardCharsets.UTF_8);

      LogBuilder.builder(log)
          .action("convertToXhtml")
          .message("Converted HTML to XHTML")
          .logInfo();

      return xhtml;
    } catch (Exception e) {
      LogBuilder.builder(log)
          .action("convertToXhtml")
          .message("Error converting HTML to XHTML")
          .logError(e);

      throw new CvGenerationException(e.getMessage());
    }
  }
}