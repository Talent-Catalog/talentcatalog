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

import org.springframework.stereotype.Component;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.util.html.StringSanitizer;

/**
 * Prepares candidate data for CV export by sanitizing and normalizing fields that may be rendered
 * into downloadable documents.
 *
 * <p>This helper is intended to ensure exported CV content is clean and safe for output formats
 * such as PDF or DOCX. It currently sanitizes contact-related fields and normalizes text in job
 * experience entries.
 */
@Component
public class CvExportDataPreparer {

  /**
   * Prepares the supplied candidate for CV export.
   *
   * <p>If contact details are enabled for export, contact fields are sanitized. Job experience
   * fields are always normalized to clean up Unicode text before rendering.
   *
   * @param candidate the candidate whose data will be prepared for export
   * @param showContact whether contact information should be included and sanitized for export
   * @return the prepared candidate instance
   */
  public Candidate prepare(Candidate candidate, Boolean showContact) {
    if (Boolean.TRUE.equals(showContact)) {
      cleanCandidateContactInfo(candidate);
    }
    cleanCandidateJobDescriptions(candidate);
    return candidate;
  }

  /**
   * Sanitizes candidate contact fields that may be shown in the exported CV.
   *
   * <p>This currently includes phone and WhatsApp values.
   *
   * @param candidate the candidate whose contact fields should be sanitized
   */
  private void cleanCandidateContactInfo(Candidate candidate) {
    if (candidate.getPhone() != null) {
      candidate.setPhone(StringSanitizer.sanitizeContactField(candidate.getPhone()));
    }
    if (candidate.getWhatsapp() != null) {
      candidate.setWhatsapp(StringSanitizer.sanitizeContactField(candidate.getWhatsapp()));
    }
  }

  /**
   * Normalizes text fields in the candidate's job experience records before export.
   *
   * <p>This helps ensure consistent Unicode rendering in generated CV documents. The role, company
   * name, and description fields are normalized when present.
   *
   * @param candidate the candidate whose job experience text should be normalized
   */
  private void cleanCandidateJobDescriptions(Candidate candidate) {
    // Nothing to normalize if the candidate has no job experience records.
    if (candidate.getCandidateJobExperiences() == null) {
      return;
    }

    candidate.getCandidateJobExperiences().forEach(jobExperience -> {
      if (jobExperience.getRole() != null) {
        jobExperience.setRole(StringSanitizer.normalizeUnicodeText(jobExperience.getRole()));
      }

      if (jobExperience.getCompanyName() != null) {
        jobExperience.setCompanyName(
            StringSanitizer.normalizeUnicodeText(jobExperience.getCompanyName()));
      }

      if (jobExperience.getDescription() != null) {
        jobExperience.setDescription(
            StringSanitizer.normalizeUnicodeText(jobExperience.getDescription()));
      }
    });
  }
}