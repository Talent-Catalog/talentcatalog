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

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * Represents the overall evaluation report containing all test results,
 * statistics, and summary information.
 */
@Data
public class EvaluationReport {

  /** Name of the evaluation suite (e.g., "Golden Dataset", "Edge Cases") */
  private String suiteName;

  /** Timestamp when evaluation started */
  private OffsetDateTime startTime;

  /** Timestamp when evaluation completed */
  private OffsetDateTime endTime;

  /** All evaluation results */
  private List<EvaluationResult> results;

  /** Total number of test cases */
  private int totalTests;

  /** Number of tests that passed */
  private int passedTests;

  /** Number of tests that failed */
  private int failedTests;

  /** Overall pass rate (0.0 to 1.0) */
  private double passRate;

  /** Average score across all tests */
  private double averageScore;

  /** Results grouped by category */
  private Map<String, List<EvaluationResult>> resultsByCategory;

  public EvaluationReport() {
    this.results = new ArrayList<>();
    this.resultsByCategory = new HashMap<>();
  }

  /**
   * Adds a result to the report and updates statistics.
   *
   * @param result The evaluation result to add
   */
  public void addResult(EvaluationResult result) {
    this.results.add(result);

    // Update category grouping
    String category =
        result.getTestCase().getCategory() != null
            ? result.getTestCase().getCategory()
            : "uncategorized";
    resultsByCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(result);
  }

  /**
   * Calculates and updates statistics based on all results.
   * Should be called after all results are added.
   */
  public void calculateStatistics() {
    this.totalTests = results.size();
    this.passedTests = (int) results.stream().filter(EvaluationResult::isPassed).count();
    this.failedTests = totalTests - passedTests;
    this.passRate = totalTests > 0 ? (double) passedTests / totalTests : 0.0;
    this.averageScore =
        results.stream().mapToDouble(EvaluationResult::getScore).average().orElse(0.0);
  }

  /**
   * Gets the duration of the evaluation in milliseconds.
   *
   * @return Duration in milliseconds
   */
  public long getDurationMillis() {
    if (startTime != null && endTime != null) {
      return java.time.Duration.between(startTime, endTime).toMillis();
    }
    return 0;
  }

  /**
   * Gets failed results for easier debugging.
   *
   * @return List of failed results
   */
  public List<EvaluationResult> getFailedResults() {
    return results.stream().filter(r -> !r.isPassed()).toList();
  }

  /**
   * Gets results by category.
   *
   * @param category The category to filter by
   * @return List of results in that category
   */
  public List<EvaluationResult> getResultsForCategory(String category) {
    return resultsByCategory.getOrDefault(category, new ArrayList<>());
  }

  /**
   * Gets pass rate for a specific category.
   *
   * @param category The category to check
   * @return Pass rate for the category (0.0 to 1.0)
   */
  public double getPassRateForCategory(String category) {
    List<EvaluationResult> categoryResults = getResultsForCategory(category);
    if (categoryResults.isEmpty()) {
      return 0.0;
    }
    long passed = categoryResults.stream().filter(EvaluationResult::isPassed).count();
    return (double) passed / categoryResults.size();
  }
}
