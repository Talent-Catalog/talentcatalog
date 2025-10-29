/*
 * Copyright (c) 2024 Talent Catalog.
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
import org.tctalent.server.util.html.HtmlSanitizer;
import org.tctalent.server.util.html.StringSanitizer;
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

    /**
     * Note - we can't use Lombok RequiredArgsConstructor because currently Lombok doesn't copy
     * the @Qualifier annotation to the constructor.
     * <p/>
     * See <a href="https://www.jetbrains.com.cn/en-us/help/inspectopedia/SpringQualifierCopyableLombok.html">
     *     Intellij doc</a>
     */
    public PdfHelper(@Qualifier("pdfTemplateEngine") TemplateEngine pdfTemplateEngine) {
        this.pdfTemplateEngine = pdfTemplateEngine;
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

            if (Boolean.TRUE.equals(showContact)) {
                cleanCandidateContactInfo(candidate);
            }
            cleanCandidateJobDescriptions(candidate);

            Context context = new Context();
            context.setVariable("candidate", candidate);
            context.setVariable("showName", showName);
            context.setVariable("showContact", showContact);

            String renderedHtmlContent = pdfTemplateEngine.process("template", context);
            String xHtml = convertToXhtml(renderedHtmlContent);

            // Remove any null bytes to avoid an invalid XML character (Unicode: 0x0) error
            xHtml = NULL_BYTE_PATTERN.matcher(xHtml).replaceAll("");

            // And finally, we create the PDF:
            return createPdf(xHtml);

        } catch (Exception e) {
            LogBuilder.builder(log)
                .action("generatePdf")
                .message("Error generating PDF")
                .logError(e);

           throw new PdfGenerationException(e.getMessage());
        }

    }
    private static void cleanCandidateContactInfo(Candidate candidate) {
        if (candidate.getPhone() != null) {
            candidate.setPhone(StringSanitizer.sanitizeContactField(candidate.getPhone()));
        }

        if (candidate.getWhatsapp() != null) {
            candidate.setWhatsapp(StringSanitizer.sanitizeContactField(candidate.getWhatsapp()));
        }
    }


    private static void cleanCandidateJobDescriptions(Candidate candidate) {
        candidate.getCandidateJobExperiences().forEach(jobExperience -> {
            jobExperience.setRole(StringSanitizer.normalizeUnicodeText(jobExperience.getRole()));
            jobExperience.setCompanyName(
                StringSanitizer.normalizeUnicodeText(jobExperience.getCompanyName()));

            String description = StringSanitizer.normalizeUnicodeText(jobExperience.getDescription());
            String sanitizedDescription = HtmlSanitizer.sanitize(description);
            sanitizedDescription = StringSanitizer.replaceLsepWithBr(sanitizedDescription);
            jobExperience.setDescription(sanitizedDescription);
        });
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
