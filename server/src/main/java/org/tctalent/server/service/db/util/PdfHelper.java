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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.PdfGenerationException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.service.db.impl.TcInstanceService;
import org.tctalent.server.util.text.CandidateTidiedTextViewFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.w3c.tidy.Tidy;
import org.xhtmlrenderer.pdf.ITextRenderer;

/**
 * Service for generating PDFs using Flying Saucer and Thymeleaf templates.
 * The PDF will display a letter styled with CSS.
 * <p/>
 * There is also a main method which will generate a letter.
 * The letter has two pages and will contain text and images.
 * <p>
 * Run main to generate the PDF. The file is called:
 * <p>
 * /test.pdf
 */
@Service
@Slf4j
public class PdfHelper {
    private static final String UTF_8 = "UTF-8";
    private static final Pattern NULL_BYTE_PATTERN = Pattern.compile("\\x00");

    private final TemplateEngine pdfTemplateEngine;
    private final TcInstanceService tcInstanceService;
    /**
     * Added this shared preparer so PDF and DOCX exports clean candidate data in the same way before rendering.
     */
    private final CvExportDataPreparer cvExportDataPreparer;
    private final CandidateTidiedTextViewFactory candidateTidiedTextViewFactory;

    /**
     * Note - we can't use Lombok RequiredArgsConstructor because currently Lombok doesn't copy
     * the @Qualifier annotation to the constructor.
     * <p/>
     * See <a href="https://www.jetbrains.com.cn/en-us/help/inspectopedia/SpringQualifierCopyableLombok.html">
     *     Intellij doc</a>
     */
    public PdfHelper(@Qualifier("pdfTemplateEngine") TemplateEngine pdfTemplateEngine,
        TcInstanceService tcInstanceService,
        CandidateTidiedTextViewFactory candidateTidiedTextViewFactory,CvExportDataPreparer cvExportDataPreparer) {
        this.pdfTemplateEngine = pdfTemplateEngine;
        this.tcInstanceService = tcInstanceService;
        this.candidateTidiedTextViewFactory = candidateTidiedTextViewFactory;
        this.cvExportDataPreparer = cvExportDataPreparer;
    }

    /**
     * Generates a PDF for a candidate.
     *
     * @param candidate the candidate data
     * @param showName whether to show the candidate's name
     * @param showContact whether to show the candidate's contact information
     * @return the generated PDF as a Resource
     */
    public Resource generatePdf(Candidate candidate, Boolean showName, Boolean showContact){
        try {
            return createPdf(renderCvXhtml(candidate, showName, showContact));
        } catch (Exception e) {
            LogBuilder.builder(log)
                .action("generatePdf")
                .message("Error generating PDF")
                .logError(e);
           throw new PdfGenerationException(e.getMessage());
        }
    }

    public String renderCvXhtml(Candidate candidate, Boolean showName, Boolean showContact) {
        // Prepare the candidate before rendering the PDF, so contact fields and job descriptions are cleaned before they go into the template.
        candidate = cvExportDataPreparer.prepare(candidate, showContact);

        Context context = new Context();
        context.setVariable("candidate", candidateTidiedTextViewFactory.create(candidate));
        context.setVariable("showName", showName);
        context.setVariable("showContact", showContact);
        context.setVariable("logoFile", tcInstanceService.getLogoFile());

        String renderedHtmlContent = pdfTemplateEngine.process("template", context);
        String xhtml = convertToXhtml(renderedHtmlContent);

        // Remove null bytes to avoid invalid XML character errors in PDF/DOCX converters.
        return NULL_BYTE_PATTERN.matcher(xhtml).replaceAll("");
    }

    private static String convertToXhtml(String html) {
        Tidy tidy = new Tidy();
        tidy.setInputEncoding(UTF_8);
        tidy.setOutputEncoding(UTF_8);
        tidy.setXHTML(true);

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

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

            throw new RuntimeException("Error converting HTML to XHTML", e);
        }
    }

    private Resource createPdf(String xHtml) throws Exception {
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(xHtml, "classpath:pdf/");
        renderer.layout();

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            renderer.createPDF(outputStream);
            return new ByteArrayResource(outputStream.toByteArray());
        }
    }

}
