/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

package org.tbbtalent.server.service.db.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.PdfGenerationException;
import org.tbbtalent.server.model.db.Candidate;
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

    private static final Logger log = LoggerFactory.getLogger(PdfHelper.class);

    @Value("${server.url}")
    private String serverUrl;

    private static final String UTF_8 = "UTF-8";
    private final TemplateEngine pdfTemplateEngine;

    @Autowired
    public PdfHelper(TemplateEngine pdfTemplateEngine) {
        this.pdfTemplateEngine = pdfTemplateEngine;
    }

    public Resource generatePdf(Candidate candidate){
        try {

            Context context = new Context();
            context.setVariable("candidate", candidate);

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
        tidy.setInputEncoding(UTF_8);
        tidy.setOutputEncoding(UTF_8);
        tidy.setXHTML(true);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(html.getBytes(UTF_8));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        tidy.parseDOM(inputStream, outputStream);
        return outputStream.toString(UTF_8);
    }

}
