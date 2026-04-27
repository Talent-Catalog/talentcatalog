/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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

package org.tctalent.server.response;

import lombok.Data;

/*
 * The DuolingoVerifyScoreResponse class represents the response structure
 * returned by the Duolingo API when verifying a test score. It contains
 * various details about the applicant's test, including their name, test
 * scores, subscore details, and a URL to the certificate.
 *
 * For more details, see the Duolingo API documentation:
 * [https://englishtest.duolingo.com/dashboard/resources/api]
 */
@Data
public class DuolingoVerifyScoreResponse {
  private String full_name; // The full name of the applicant
  private String latin_full_name; // The latin version of the full name
  private String test_date; // The date the test was taken
  private int scale; // The score scale for the test
  private String certificate_url; // URL to the certificate
  private int overall_score; // Overall score of the test
  private int literacy_subscore; // Literacy subscore
  private int production_subscore; // Production subscore
  private int conversation_subscore; // Conversation subscore
  private int comprehension_subscore; // Comprehension subscore
  private Integer swrl_speaking_subscore; // SWRL Speaking subscore (can be null)
  private Integer swrl_writing_subscore; // SWRL Writing subscore (can be null)
  private Integer swrl_reading_subscore; // SWRL Reading subscore (can be null)
  private Integer swrl_listening_subscore; // SWRL Listening subscore (can be null)
}