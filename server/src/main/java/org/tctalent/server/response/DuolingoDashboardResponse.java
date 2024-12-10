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
 * DuolingoDashboardResponse:
 * Represents the response object for the Duolingo Dashboard API. It contains various fields
 * that store the details of a Duolingo test result, such as personal information, test scores,
 * certificate URL, and other related metadata.
 *
 * For more details, see the Duolingo API documentation:
 * [https://englishtest.duolingo.com/dashboard/resources/api]
 */
@Data
public class DuolingoDashboardResponse {

  // The date the test was taken
  private String test_date;

  // The unique coupon ID associated with the test
  private String coupon_id;

  // The language of the test
  private String language;

  // The email address of the applicant
  private String email;

  // The full name of the applicant
  private String full_name;

  // The birthdate of the applicant
  private String birthdate;

  // The first name of the applicant
  private String first_name;

  // The last name of the applicant
  private String last_name;

  // The middle names of the applicant
  private String middle_names;

  // The given names of the applicant
  private String given_names;

  // The surnames of the applicant
  private String surnames;

  // The country of the applicant
  private String country;

  // The unique user ID in the Duolingo system
  private String user_id;

  // The session ID for the test session
  private String session_id;

  // The URL to the certificate if the applicant is certified
  private String certificate_url;

  // The applicant's unique applicant ID
  private String applicant_id;

  // A list of applicant IDs, possibly for associated candidates
  private Object applicant_ids;

  // The overall score of the applicant on the test
  private int overall_score;

  // The interview URL if available
  private String interview_url;

  // The embedded URL for sharing the test results
  private String embed_url;

  // The date when the test results were verified
  private String verification_date;

  // The date when the test results were shared
  private String share_date;

  // A flag indicating whether the applicant is certified
  private boolean certified;

  // The percentage score achieved by the applicant
  private int percent_score;

  // The score scale used in the test
  private int scale;

  // The literacy subscore of the applicant
  private int literacy_subscore;

  // The conversation subscore of the applicant
  private int conversation_subscore;

  // The comprehension subscore of the applicant
  private int comprehension_subscore;

  // The production subscore of the applicant
  private int production_subscore;
}
