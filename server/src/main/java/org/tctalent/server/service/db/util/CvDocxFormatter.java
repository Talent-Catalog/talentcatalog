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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;
import org.tctalent.server.model.db.CandidateCertification;
import org.tctalent.server.model.db.CandidateEducation;
import org.tctalent.server.model.db.CandidateJobExperience;

/**
 * Formats candidate CV data into display-ready text for DOCX export.
 *
 * <p>This component centralizes text formatting logic used when generating
 * Word CV documents, such as:
 *
 * <ul>
 *   <li>date ranges
 *   <li>education summary lines
 *   <li>certification summary lines
 *   <li>company and country display text
 *   <li>HTML to plain text conversion
 * </ul>
 */
@Component
public class CvDocxFormatter {

  /**
   * Formatter used for rendering dates in month-year format, for example {@code Jan-2026}.
   */
  private static final DateTimeFormatter MONTH_YEAR = DateTimeFormatter.ofPattern("MMM-yyyy");

  /**
   * Formats a job experience date range for display in the CV.
   *
   * <p>If the end date is {@code null}, the range is treated as ongoing and
   * {@code present} is used. If both dates are missing, an empty string is returned.
   *
   * @param start the start date
   * @param end   the end date
   * @return the formatted date range string
   */
  public String formatDateRange(LocalDate start, LocalDate end) {
    String startText = start != null ? start.format(MONTH_YEAR) : "";
    String endText = end != null ? end.format(MONTH_YEAR) : "present";

    if (startText.isBlank() && end == null) {
      return "";
    }
    if (startText.isBlank()) {
      return endText;
    }
    return startText + " to " + endText;
  }

  /**
   * Builds a formatted education line from the supplied education record.
   *
   * <p>The output may include education type, major, course name, institution,
   * and completion year. If the year is not available, {@code Incomplete} is used.
   *
   * @param education the education record
   * @return a formatted single-line education summary
   */
  public String formatEducationLine(CandidateEducation education) {
    StringBuilder sb = new StringBuilder();

    if (education.getEducationType() != null) {
      sb.append(education.getEducationType()).append(" ");
    }
    if (education.getEducationMajor() != null && education.getEducationMajor().getName() != null) {
      sb.append(education.getEducationMajor().getName());
    }
    if (education.getCourseName() != null && !education.getCourseName().isBlank()) {
      sb.append(": ").append(education.getCourseName());
    }
    if (education.getInstitution() != null && !education.getInstitution().isBlank()) {
      sb.append(" - ").append(education.getInstitution());
    }
    if (education.getYearCompleted() != null) {
      sb.append(", ").append(education.getYearCompleted());
    } else {
      sb.append(", Incomplete");
    }

    return sb.toString().trim();
  }

  /**
   * Builds a formatted certification line from the supplied certification record.
   *
   * @param certification the certification record
   * @return a formatted certification summary line
   */
  public String formatCertificationLine(CandidateCertification certification) {
    String base = joinNonBlank(certification.getName(), certification.getInstitution());
    String date = certification.getDateCompleted() != null
        ? certification.getDateCompleted().format(MONTH_YEAR)
        : null;

    return joinNonBlank(base, date);
  }

  /**
   * Builds a formatted company and country line for a job experience record.
   *
   * @param experience the job experience
   * @return a formatted company-country string
   */
  public String formatCompanyAndCountry(CandidateJobExperience experience) {
    return joinNonBlank(
        experience.getCompanyName(),
        experience.getCountry() != null ? experience.getCountry().getName() : null
    );
  }

  /**
   * Converts HTML content into plain text for DOCX export.
   *
   * @param html the HTML content
   * @return plain text extracted from the HTML, or an empty string if the input is null
   */
  public String toPlainText(String html) {
    if (html == null) {
      return "";
    }
    return Jsoup.parse(html).text();
  }

  /**
   * Joins two non-blank strings with a separator.
   *
   * <p>If both values are present, they are joined with {@code " - "}. If only one
   * is present, that value is returned. If neither is present, an empty string is returned.
   *
   * @param first  the first value
   * @param second the second value
   * @return the joined string
   */
  public String joinNonBlank(String first, String second) {
    boolean firstOk = first != null && !first.isBlank();
    boolean secondOk = second != null && !second.isBlank();

    if (firstOk && secondOk) {
      return first + " - " + second;
    }
    if (firstOk) {
      return first;
    }
    if (secondOk) {
      return second;
    }
    return "";
  }
}