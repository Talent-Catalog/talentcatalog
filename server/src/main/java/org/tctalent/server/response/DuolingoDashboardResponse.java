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
  private String testDate;

  // The unique coupon ID associated with the test
  private String couponId;

  // The language of the test
  private String language;

  // The email address of the applicant
  private String email;

  // The full name of the applicant
  private String fullName;

  // The birthdate of the applicant
  private String birthdate;

  // The first name of the applicant
  private String firstName;

  // The last name of the applicant
  private String lastName;

  // The middle names of the applicant
  private String middleNames;

  // The given names of the applicant
  private String givenNames;

  // The surnames of the applicant
  private String surnames;

  // The country of the applicant
  private String country;

  // The unique user ID in the Duolingo system
  private String userId;

  // The session ID for the test session
  private String sessionId;

  // The URL to the certificate if the applicant is certified
  private String certificateUrl;

  // The applicant's unique applicant ID
  private String applicantId;

  // A list of applicant IDs, possibly for associated candidates
  private Object applicantIds;

  // The overall score of the applicant on the test
  private int overallScore;

  // The interview URL if available
  private String interviewUrl;

  // The embedded URL for sharing the test results
  private String embedUrl;

  // The date when the test results were verified
  private String verificationDate;

  // The date when the test results were shared
  private String shareDate;

  // A flag indicating whether the applicant is certified
  private boolean certified;

  // The percentage score achieved by the applicant
  private int percentScore;

  // The score scale used in the test
  private int scale;

  // The literacy subscore of the applicant
  private int literacySubscore;

  // The conversation subscore of the applicant
  private int conversationSubscore;

  // The comprehension subscore of the applicant
  private int comprehensionSubscore;

  // The production subscore of the applicant
  private int productionSubscore;
}
