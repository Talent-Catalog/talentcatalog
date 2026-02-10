/*
 * Copyright (c) 2025 Talent Catalog.
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

package org.tctalent.server.eval.chatbot.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * Represents the evaluation result for a single test case.
 * Contains the actual response, pass/fail status, and detailed feedback.
 */
@Data
public class EvaluationResult {

  /** The test case that was evaluated */
  private EvaluationTestCase testCase;

  /** The actual response from the chatbot */
  private String actualResponse;

  /** Whether this test case passed all checks */
  private boolean passed;

  /** Score from 0.0 to 1.0 (for partial credit evaluation) */
  private double score;

  /** Detailed feedback messages about why the test passed or failed */
  private List<String> feedback;

  /** Keywords that were found in the response */
  private List<String> foundKeywords;

  /** Keywords that were expected but missing */
  private List<String> missingKeywords;

  /** Forbidden keywords that were found (should not be present) */
  private List<String> forbiddenKeywordsFound;

  public EvaluationResult() {
    this.feedback = new ArrayList<>();
    this.foundKeywords = new ArrayList<>();
    this.missingKeywords = new ArrayList<>();
    this.forbiddenKeywordsFound = new ArrayList<>();
  }

  /**
   * Adds a feedback message to this result.
   *
   * @param message The feedback message
   */
  public void addFeedback(String message) {
    this.feedback.add(message);
  }

  /**
   * Gets a summary string for logging/reporting.
   *
   * @return Summary string
   */
  public String getSummary() {
    return String.format(
        "%s: %s (score: %.2f)",
        testCase.getId(), passed ? "PASS" : "FAIL", score);
  }
}
