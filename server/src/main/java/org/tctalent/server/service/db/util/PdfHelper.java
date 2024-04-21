/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.PdfGenerationException;
import org.tctalent.server.model.db.Candidate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.w3c.tidy.Tidy;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

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
public class PdfHelper {

    @Value("${server.url}")
    private String serverUrl;

    private final TemplateEngine pdfTemplateEngine;

    @Autowired
    public PdfHelper(TemplateEngine pdfTemplateEngine) {
        this.pdfTemplateEngine = pdfTemplateEngine;
    }

    public Resource generatePdf(Candidate candidate, Boolean showName, Boolean showContact){
        try {

            Context context = new Context();
            context.setVariable("candidate", candidate);
            context.setVariable("showName", showName);
            context.setVariable("showContact", showContact);

            String renderedHtmlContent = pdfTemplateEngine.process("template", context);
            String xHtml = convertToXhtml(renderedHtmlContent);

            // Remove any null bytes to avoid an invalid XML character (Unicode: 0x0) error
            xHtml = Pattern.compile("\\x00").matcher(xHtml).replaceAll("");

            ITextRenderer renderer = new ITextRenderer();

            renderer.setDocumentFromString(xHtml, "classpath:pdf/");
            renderer.layout();

            // And finally, we create the PDF:
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            renderer.createPDF(outputStream);
            outputStream.close();
            return new ByteArrayResource(outputStream.toByteArray());

        } catch (Exception e){
           throw new PdfGenerationException(e.getMessage());
        }

    }

    private static String convertToXhtml(String html) throws UnsupportedEncodingException {
        Tidy tidy = new Tidy();
        tidy.setInputEncoding(StandardCharsets.UTF_8.name());
        tidy.setOutputEncoding(StandardCharsets.UTF_8.name());
        tidy.setXHTML(true);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(html.getBytes(
            StandardCharsets.UTF_8));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        tidy.parseDOM(inputStream, outputStream);
        return outputStream.toString(StandardCharsets.UTF_8);
    }

}
