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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateJobExperience;
import org.tctalent.server.util.html.HtmlSanitizer;
import org.tctalent.server.util.html.StringSanitizer;

class CvExportDataPreparerTest {

  private CvExportDataPreparer preparer;

  @BeforeEach
  void setUp() {
    preparer = new CvExportDataPreparer();
  }

  @Test
  void prepare_whenShowContactTrue_sanitizesContactFieldsAndCleansJobFields() {
    Candidate candidate = new Candidate();
    candidate.setPhone("\u202A  0123\u00A0 456 \u202C");
    candidate.setWhatsapp("\u202B  0093   77 \u202C");

    CandidateJobExperience jobExperience = new CandidateJobExperience();
    jobExperience.setRole("Developer – Trainer");
    jobExperience.setCompanyName("Women’s Tech “Hub”");

    String description = "<p>Built “apps” — safely</p>Line\u2028Two<script>alert('x')</script>";
    jobExperience.setDescription(description);

    candidate.setCandidateJobExperiences(List.of(jobExperience));

    Candidate result = preparer.prepare(candidate, true);

    assertSame(candidate, result);

    assertEquals("0123 456", candidate.getPhone());
    assertEquals("0093 77", candidate.getWhatsapp());

    assertEquals("Developer - Trainer", jobExperience.getRole());
    assertEquals("Women's Tech \"Hub\"", jobExperience.getCompanyName());

    String expectedDescription = StringSanitizer.normalizeUnicodeText(description);
    expectedDescription = HtmlSanitizer.sanitize(expectedDescription);
    expectedDescription = StringSanitizer.replaceLsepWithBr(expectedDescription);

    assertEquals(expectedDescription, jobExperience.getDescription());
  }

  @Test
  void prepare_whenShowContactFalse_doesNotSanitizeContactButStillCleansJobFields() {
    Candidate candidate = new Candidate();
    candidate.setPhone("\u202A  0123\u00A0 456 \u202C");
    candidate.setWhatsapp("\u202B  0093   77 \u202C");

    CandidateJobExperience jobExperience = new CandidateJobExperience();
    jobExperience.setRole("Engineer — Mentor");
    jobExperience.setCompanyName("Candidate’s Company");
    jobExperience.setDescription("Line\u2028Two");

    candidate.setCandidateJobExperiences(List.of(jobExperience));

    Candidate result = preparer.prepare(candidate, false);

    assertSame(candidate, result);

    assertEquals("\u202A  0123\u00A0 456 \u202C", candidate.getPhone());
    assertEquals("\u202B  0093   77 \u202C", candidate.getWhatsapp());

    assertEquals("Engineer - Mentor", jobExperience.getRole());
    assertEquals("Candidate's Company", jobExperience.getCompanyName());
    assertEquals("Line<br>Two", jobExperience.getDescription());
  }

  @Test
  void prepare_whenContactFieldsAreNullAndJobFieldsAreNull_keepsNullValues() {
    Candidate candidate = new Candidate();

    CandidateJobExperience jobExperience = new CandidateJobExperience();
    candidate.setCandidateJobExperiences(List.of(jobExperience));

    Candidate result = preparer.prepare(candidate, true);

    assertSame(candidate, result);

    assertNull(candidate.getPhone());
    assertNull(candidate.getWhatsapp());

    assertNull(jobExperience.getRole());
    assertNull(jobExperience.getCompanyName());
    assertNull(jobExperience.getDescription());
  }

  @Test
  void prepare_whenCandidateJobExperiencesIsNull_returnsCandidateWithoutFailure() {
    Candidate candidate = new Candidate();
    candidate.setPhone("\u202A  0123\u00A0 456 \u202C");
    candidate.setWhatsapp("\u202B  0093   77 \u202C");
    candidate.setCandidateJobExperiences(null);

    Candidate result = preparer.prepare(candidate, true);

    assertSame(candidate, result);

    assertEquals("0123 456", candidate.getPhone());
    assertEquals("0093 77", candidate.getWhatsapp());
    assertNull(candidate.getCandidateJobExperiences());
  }
}