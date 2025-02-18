/*
 * Copyright (c) 2024 Talent Catalog.
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

package org.tctalent.server.service.db.impl;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.DuolingoCouponStatus;
import org.tctalent.server.model.db.Exam;
import org.tctalent.server.request.candidate.exam.CreateCandidateExamRequest;
import org.tctalent.server.response.DuolingoDashboardResponse;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.CandidateExam;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.service.db.CandidateExamService;
import org.tctalent.server.service.db.DuolingoApiService;
import org.tctalent.server.service.db.DuolingoCouponService;
import org.tctalent.server.service.db.DuolingoExamService;

@Service
@Slf4j
@RequiredArgsConstructor
public class DuolingoExamServiceImpl implements DuolingoExamService {

  private final DuolingoApiService duolingoApiService;
  private final DuolingoCouponService duolingoCouponService;
  private final CandidateExamService candidateExamService;

  @Override
  @Scheduled(cron = "0 0 0 * * ?", zone = "GMT")
  @SchedulerLock(name = "DuolingoSchedulerTask_updateCandidateExams", lockAtLeastFor = "PT23H", lockAtMostFor = "PT23H")
  public void updateCandidateExams() throws NoSuchObjectException {
    List<DuolingoDashboardResponse> dashboardResults = duolingoApiService.getDashboardResults(null, null);

    for (DuolingoDashboardResponse result : dashboardResults) {
      String couponCode = result.getCouponId();

      // Retrieve candidate based on coupon code
      Candidate candidateOpt = duolingoCouponService.findCandidateByCouponCode(couponCode);
      if (candidateOpt != null) {
        String newScore = String.valueOf(result.getOverallScore()).trim();
        String newYear = String.valueOf(extractYear(result.getTestDate())).trim();
        String newNotes = buildNotes(result);

        // Fetch all exams for the candidate in one go
        List<CandidateExam> existingExams = candidateExamService.list(candidateOpt.getId());

        // Check if an identical exam already exists for the candidate
        if (isIdenticalExamExists(existingExams, newScore, newYear, newNotes)) {
          LogBuilder.builder(log)
              .action("UpdateCandidateExams")
              .message(String.format("Identical Duolingo exam record already exists for candidate ID: %s. Skipping...", candidateOpt.getId()))
              .logInfo();
        }
        else {
          // Create and save new exam record
          createAndSaveNewExam(candidateOpt, couponCode, newScore, newYear, newNotes);
        }
      }
    }
  }

  private boolean isIdenticalExamExists(List<CandidateExam> existingExams, String newScore, String newYear, String newNotes) {
    return existingExams.stream().anyMatch(existingExam ->
        Exam.DETOfficial.equals(existingExam.getExam()) &&
            Objects.equals(newScore, existingExam.getScore().trim()) &&
            Objects.equals(newYear, String.valueOf(existingExam.getYear()).trim()) &&
            Objects.equals(newNotes, existingExam.getNotes())
    );
  }

  private void createAndSaveNewExam(Candidate candidate, String couponCode, String newScore, String newYear, String newNotes) {
    CreateCandidateExamRequest candidateExamRequest = new CreateCandidateExamRequest();
    candidateExamRequest.setExam(Exam.DETOfficial);
    candidateExamRequest.setScore(newScore);
    candidateExamRequest.setYear(Long.valueOf(newYear));
    candidateExamRequest.setNotes(newNotes);
    duolingoCouponService.updateCouponStatus(couponCode, DuolingoCouponStatus.REDEEMED);
    candidateExamService.createExam(candidate.getId(), candidateExamRequest);
    LogBuilder.builder(log)
        .action("UpdateCandidateExams")
        .message(String.format("New Duolingo exam record added for candidate ID: %s.", candidate.getId()))
        .logInfo();
  }

  private String buildNotes(DuolingoDashboardResponse result) {
    return String.format(
        "Generated from Duolingo dashboard | Certificate URL: %s | Interview URL: %s | Verification Date: %s | Percent Score: %s | "
            + "Scale: %s | Literacy Subscore: %s | Conversation Subscore: %s | Comprehension Subscore: %s | Production Subscore: %s",
        result.getCertificateUrl(), result.getInterviewUrl(), result.getVerificationDate(),
        result.getPercentScore(), result.getScale(), result.getLiteracySubscore(),
        result.getConversationSubscore(), result.getComprehensionSubscore(),
        result.getProductionSubscore());
  }

  /**
   * Extracts the year from a date string in the format "yyyy-MM-dd".
   *
   * @param dateStr the input date string
   * @return the year as a Long, or null if the input is invalid
   */
  private Long extractYear(String dateStr) {
    try {
      LocalDate date = LocalDate.parse(dateStr);
      return (long) date.getYear();
    } catch (DateTimeParseException e) {
      // Log and handle error
      LogBuilder.builder(log)
          .action("extractYear")
          .message(String.format("Invalid date format: %s", dateStr))
          .logError(e);
      return null;
    }
  }
}
