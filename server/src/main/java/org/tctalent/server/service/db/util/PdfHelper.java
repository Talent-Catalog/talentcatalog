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

import java.io.ByteArrayOutputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.CvGenerationException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Candidate;
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
@RequiredArgsConstructor
@Slf4j
public class PdfHelper {
    private static final String PDF_RESOURCE_BASE_URL = "classpath:pdf/";
    private final CvTemplateHelper cvTemplateHelper;
    /**
     * Generates a PDF CV for the given candidate.
     *
     * @param candidate candidate whose CV should be generated
     * @param showName whether the candidate name should be included in the CV
     * @param showContact whether contact details should be included in the CV
     * @return generated PDF as a Spring {@link Resource}
     * @throws CvGenerationException if the CV cannot be rendered or converted to PDF
     */
    public Resource generatePdf(Candidate candidate, Boolean showName, Boolean showContact) {
        try {
            String xhtml = cvTemplateHelper.renderCvXhtml(candidate, showName, showContact);
            return createPdf(xhtml);
        } catch (Exception e) {
            LogBuilder.builder(log)
                .action("generatePdf")
                .message("Error generating PDF")
                .logError(e);

            throw new CvGenerationException(e.getMessage());
        }
    }

    private Resource createPdf(String xhtml) throws Exception {
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(xhtml, PDF_RESOURCE_BASE_URL);
        renderer.layout();

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            renderer.createPDF(outputStream);
            return new ByteArrayResource(outputStream.toByteArray());
        }
    }

}
