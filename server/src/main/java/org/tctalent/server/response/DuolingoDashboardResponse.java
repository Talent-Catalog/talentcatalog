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

import com.fasterxml.jackson.annotation.JsonProperty;
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
  @JsonProperty("test_date")
  private String testDate;

  // The unique coupon ID associated with the test
  @JsonProperty("coupon_id")
  private String couponId;

  // The language of the test
  @JsonProperty("language")
  private String language;

  // The email address of the applicant
  @JsonProperty("email")
  private String email;

  // The full name of the applicant
  @JsonProperty("full_name")
  private String fullName;

  // The birthdate of the applicant
  @JsonProperty("birthdate")
  private String birthdate;

  // The first name of the applicant
  @JsonProperty("first_name")
  private String firstName;

  // The last name of the applicant
  @JsonProperty("last_name")
  private String lastName;

  // The middle names of the applicant
  @JsonProperty("middle_names")
  private String middleNames;

  // The given names of the applicant
  @JsonProperty("given_names")
  private String givenNames;

  // The surnames of the applicant
  @JsonProperty("surnames")
  private String surnames;

  // The country of the applicant
  @JsonProperty("country")
  private String country;

  // The unique user ID in the Duolingo system
  @JsonProperty("user_id")
  private String userId;

  // The session ID for the test session
  @JsonProperty("session_id")
  private String sessionId;

  // The URL to the certificate if the applicant is certified
  @JsonProperty("certificate_url")
  private String certificateUrl;

  // The applicant's unique applicant ID
  @JsonProperty("applicant_id")
  private String applicantId;

  // A list of applicant IDs, possibly for associated candidates
  @JsonProperty("applicant_ids")
  private Object applicantIds;

  // The overall score of the applicant on the test
  @JsonProperty("overall_score")
  private int overallScore;

  // The interview URL if available
  @JsonProperty("interview_url")
  private String interviewUrl;

  // The embedded URL for sharing the test results
  @JsonProperty("embed_url")
  private String embedUrl;

  // The date when the test results were verified
  @JsonProperty("verification_date")
  private String verificationDate;

  // The date when the test results were shared
  @JsonProperty("share_date")
  private String shareDate;

  // A flag indicating whether the applicant is certified
  @JsonProperty("certified")
  private boolean certified;

  // The percentage score achieved by the applicant
  @JsonProperty("percent_score")
  private int percentScore;

  // The score scale used in the test
  @JsonProperty("scale")
  private int scale;

  // The literacy subscore of the applicant
  @JsonProperty("literacy_subscore")
  private int literacySubscore;

  // The conversation subscore of the applicant
  @JsonProperty("conversation_subscore")
  private int conversationSubscore;

  // The comprehension subscore of the applicant
  @JsonProperty("comprehension_subscore")
  private int comprehensionSubscore;

  // The production subscore of the applicant
  @JsonProperty("production_subscore")
  private int productionSubscore;
}