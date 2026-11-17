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

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.tctalent.server.configuration.CvConfiguration;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateJobExperience;
import org.tctalent.server.service.db.impl.TcInstanceService;

/**
 * Renders a CV through the real Thymeleaf template, {@link CvTemplateHelper} and
 * {@link PdfHelper} to check that stored rich-text descriptions produce a valid PDF.
 */
@ExtendWith(MockitoExtension.class)
class CvPdfGenerationTest {

  @Mock
  private TcInstanceService tcInstanceService;

  private PdfHelper pdfHelper;

  @BeforeEach
  void setUp() {
    CvTemplateHelper cvTemplateHelper =
        new CvTemplateHelper(new CvConfiguration().cvTemplateEngine(), tcInstanceService);
    pdfHelper = new PdfHelper(cvTemplateHelper);
  }

  @Test
  void generatePdf_whenDescriptionContainsHtmlEntitiesAndVoidElements_returnsPdf() {
    when(tcInstanceService.getLogoFile()).thenReturn("tbblogo.png");

    CandidateJobExperience experience = new CandidateJobExperience();
    experience.setRole("Engineer");
    experience.setStartDate(LocalDate.of(2020, 1, 1));
    experience.setDescription(
        "<p>Managed&nbsp;team of 5<br>Led &amp; built</p><p><br></p><ul><li>Item&rsquo;s</li></ul>");

    Candidate candidate = new Candidate();
    candidate.setCandidateNumber("123456");
    candidate.setCandidateJobExperiences(List.of(experience));
    candidate.setCandidateEducations(List.of());
    candidate.setCandidateCertifications(List.of());
    candidate.setCandidateLanguages(List.of());

    Resource resource = pdfHelper.generatePdf(candidate, false, false);

    byte[] pdfBytes = assertInstanceOf(ByteArrayResource.class, resource).getByteArray();
    assertTrue(new String(pdfBytes, 0, 4, StandardCharsets.US_ASCII).startsWith("%PDF"));
  }
}
