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

import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.CvGenerationException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.service.db.impl.TcInstanceService;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Shared service for rendering candidate CV data through the common Thymeleaf CV template.
 * <p>
 * See also {@link org.tctalent.server.configuration.CvConfiguration} for the template engine
 * configuration.
 * </p>
 *
 * <p>The XHTML it returns is always well-formed XML, whatever HTML candidates have entered, so it
 * can be passed directly to XML-based converters.</p>
 *
 * <p>This class deliberately contains only the shared template/XHTML work. PDF conversion belongs
 * in {@link PdfHelper}; DOCX conversion belongs in
 * {@link DocxHelper}.</p>
 */
@Service
@Slf4j
public class CvTemplateHelper {

  private static final Pattern NULL_BYTE_PATTERN = Pattern.compile("\\x00");

  private final TemplateEngine cvTemplateEngine;
  private final TcInstanceService tcInstanceService;

  /**
   * Note - we can't use Lombok RequiredArgsConstructor because currently Lombok doesn't copy
   * the @Qualifier annotation to the constructor.
   */
  public CvTemplateHelper(
      @Qualifier("cvTemplateEngine") TemplateEngine cvTemplateEngine,
      TcInstanceService tcInstanceService
  ) {
    this.cvTemplateEngine = cvTemplateEngine;
    this.tcInstanceService = tcInstanceService;
  }

  public String getResourceBaseUrl() {
    try {
      return new ClassPathResource("/cv/").getURL().toExternalForm();
    } catch (Exception e) {
      LogBuilder.builder(log)
          .action("getResourceBaseUrl")
          .message("Error getting resource base URL for CV template")
          .logError(e);

      throw new CvGenerationException("Error getting resource base URL for CV template: " + e.getMessage());
    }
  }

  /**
   * Renders candidate CV data using the shared Thymeleaf template and returns well-formed XHTML.
   * <p>
   * The rendered output is normalised so that candidate-supplied HTML (named entities, unclosed
   * void elements) is safe for the XML parsers used by the PDF and DOCX converters.
   * </p>
   *
   * @param candidate candidate whose CV should be rendered
   * @param showName whether candidate name should be included
   * @param showContact whether candidate contact details should be included
   * @return rendered CV as XHTML
   * @throws CvGenerationException if the template cannot be rendered as XHTML
   */
  public String renderCvXhtml(Candidate candidate, Boolean showName, Boolean showContact) {
    try {
      Context context = new Context();
      context.setVariable("candidate", candidate);
      context.setVariable("showName", showName);
      context.setVariable("showContact", showContact);
      context.setVariable("logoFile", tcInstanceService.getLogoFile());

      String html = cvTemplateEngine.process("cvTemplate", context);

      //TODO JC This shouldn't be necessary once the data in the database is cleaned of invalid characters.
      // Remove null bytes to avoid invalid XML character errors in PDF/DOCX converters.
      html = NULL_BYTE_PATTERN.matcher(html).replaceAll("");

      return toWellFormedXhtml(html);
    } catch (Exception e) {
      LogBuilder.builder(log)
          .action("renderCvXhtml")
          .message("Error rendering CV XHTML")
          .logError(e);

      throw new CvGenerationException(e.getMessage());
    }
  }

  /**
   * Re-serialises rendered CV HTML as well-formed XHTML.
   * <p>
   * The template itself is XHTML, but candidate-supplied rich text (e.g. job descriptions) is
   * inserted unescaped via {@code th:utext} and may contain HTML-only constructs such as named
   * entities ({@code &nbsp;}) or unclosed void elements ({@code <br>}). These are fatal to the
   * XML parsers used by the PDF and DOCX converters. Parsing with jsoup's HTML parser and
   * writing with XML syntax and XHTML escaping produces equivalent, well-formed markup
   * (e.g. {@code &#xa0;}, {@code <br />}).
   * </p>
   *
   * @param html rendered CV HTML
   * @return equivalent well-formed XHTML
   */
  private static String toWellFormedXhtml(String html) {
    Document document = Jsoup.parse(html);
    document.outputSettings()
        .syntax(Document.OutputSettings.Syntax.xml)
        .escapeMode(Entities.EscapeMode.xhtml)
        .prettyPrint(false);
    return document.outerHtml();
  }
}
