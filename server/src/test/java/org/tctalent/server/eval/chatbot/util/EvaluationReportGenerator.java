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

package org.tctalent.server.eval.chatbot.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import org.tctalent.server.eval.chatbot.model.EvaluationReport;
import org.tctalent.server.eval.chatbot.model.EvaluationResult;

/**
 * Utility class for generating human-readable evaluation reports.
 * Outputs reports to console and optionally to file.
 */
public class EvaluationReportGenerator {

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private static final String SEPARATOR = "=".repeat(80);
  private static final String SUBSEPARATOR = "-".repeat(80);

  /**
   * Generates and prints a formatted console report.
   *
   * @param report The evaluation report to format
   * @return Formatted report string
   */
  public static String generateConsoleReport(EvaluationReport report) {
    StringBuilder sb = new StringBuilder();

    // Header
    sb.append("\n").append(SEPARATOR).append("\n");
    sb.append("CHATBOT EVALUATION REPORT: ").append(report.getSuiteName()).append("\n");
    sb.append(SEPARATOR).append("\n\n");

    // Timing information
    if (report.getStartTime() != null) {
      sb.append("Start Time: ").append(report.getStartTime().format(DATE_FORMATTER)).append("\n");
    }
    if (report.getEndTime() != null) {
      sb.append("End Time:   ").append(report.getEndTime().format(DATE_FORMATTER)).append("\n");
    }
    sb.append("Duration:   ")
        .append(report.getDurationMillis() / 1000.0)
        .append(" seconds\n\n");

    // Overall statistics
    sb.append("OVERALL RESULTS\n");
    sb.append(SUBSEPARATOR).append("\n");
    sb.append(String.format("Total Tests:    %d\n", report.getTotalTests()));
    sb.append(String.format("Passed:         %d\n", report.getPassedTests()));
    sb.append(String.format("Failed:         %d\n", report.getFailedTests()));
    sb.append(String.format("Pass Rate:      %.1f%%\n", report.getPassRate() * 100));
    sb.append(String.format("Average Score:  %.2f\n\n", report.getAverageScore()));

    // Results by category
    if (!report.getResultsByCategory().isEmpty()) {
      sb.append("RESULTS BY CATEGORY\n");
      sb.append(SUBSEPARATOR).append("\n");
      for (Map.Entry<String, List<EvaluationResult>> entry :
          report.getResultsByCategory().entrySet()) {
        String category = entry.getKey();
        double categoryPassRate = report.getPassRateForCategory(category);
        int categoryTotal = entry.getValue().size();
        int categoryPassed = (int) (categoryPassRate * categoryTotal);

        sb.append(
            String.format(
                "%-25s %2d/%2d passed (%.1f%%)\n",
                category + ":", categoryPassed, categoryTotal, categoryPassRate * 100));
      }
      sb.append("\n");
    }

    // Failed test details
    List<EvaluationResult> failedResults = report.getFailedResults();
    if (!failedResults.isEmpty()) {
      sb.append("FAILED TESTS DETAILS\n");
      sb.append(SUBSEPARATOR).append("\n");
      for (EvaluationResult result : failedResults) {
        sb.append(formatFailedResult(result));
        sb.append("\n");
      }
    } else {
      sb.append("All tests passed! 🎉\n\n");
    }

    sb.append(SEPARATOR).append("\n");

    return sb.toString();
  }

  /**
   * Formats a single failed test result for detailed output.
   */
  private static String formatFailedResult(EvaluationResult result) {
    StringBuilder sb = new StringBuilder();

    sb.append(String.format("\n[%s] %s\n", result.getTestCase().getId(), "FAILED"));
    sb.append(String.format("Score: %.2f\n", result.getScore()));

    if (result.getTestCase().getQuestion() != null) {
      sb.append("Question: ").append(result.getTestCase().getQuestion()).append("\n");
    }

    if (result.getTestCase().getDescription() != null) {
      sb.append("Description: ").append(result.getTestCase().getDescription()).append("\n");
    }

    sb.append("Response: ").append(truncate(result.getActualResponse(), 200)).append("\n");

    if (!result.getFeedback().isEmpty()) {
      sb.append("Feedback:\n");
      for (String feedback : result.getFeedback()) {
        sb.append("  - ").append(feedback).append("\n");
      }
    }

    return sb.toString();
  }

  /**
   * Truncates a string to a maximum length, adding ellipsis if needed.
   */
  private static String truncate(String str, int maxLength) {
    if (str == null) {
      return "null";
    }
    if (str.length() <= maxLength) {
      return str;
    }
    return str.substring(0, maxLength) + "...";
  }

  /**
   * Writes the report to a file in the build/reports/chatbot-eval directory.
   *
   * @param report The evaluation report
   * @param filename The filename (without path)
   * @throws IOException If file writing fails
   */
  public static void writeReportToFile(EvaluationReport report, String filename)
      throws IOException {
    String reportContent = generateConsoleReport(report);
    Path reportsDir = Paths.get("build", "reports", "chatbot-eval");
    Files.createDirectories(reportsDir);

    Path reportFile = reportsDir.resolve(filename);
    Files.writeString(reportFile, reportContent);
  }

  /**
   * Generates a detailed HTML report (basic implementation).
   * Can be enhanced with more styling and interactive features.
   *
   * @param report The evaluation report
   * @return HTML report string
   */
  public static String generateHtmlReport(EvaluationReport report) {
    StringBuilder sb = new StringBuilder();

    sb.append("<!DOCTYPE html>\n");
    sb.append("<html>\n<head>\n");
    sb.append(
        "<title>Chatbot Evaluation Report - ").append(report.getSuiteName()).append("</title>\n");
    sb.append("<style>\n");
    sb.append("body { font-family: Arial, sans-serif; margin: 20px; }\n");
    sb.append("h1 { color: #333; }\n");
    sb.append("table { border-collapse: collapse; width: 100%; margin-top: 20px; }\n");
    sb.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n");
    sb.append("th { background-color: #4CAF50; color: white; }\n");
    sb.append(".pass { color: green; font-weight: bold; }\n");
    sb.append(".fail { color: red; font-weight: bold; }\n");
    sb.append(".stats { background-color: #f0f0f0; padding: 15px; border-radius: 5px; }\n");
    sb.append("</style>\n");
    sb.append("</head>\n<body>\n");

    sb.append("<h1>Chatbot Evaluation Report: ").append(report.getSuiteName()).append("</h1>\n");

    // Statistics
    sb.append("<div class='stats'>\n");
    sb.append("<h2>Overall Statistics</h2>\n");
    sb.append("<p><strong>Total Tests:</strong> ").append(report.getTotalTests()).append("</p>\n");
    sb.append("<p><strong>Passed:</strong> ").append(report.getPassedTests()).append("</p>\n");
    sb.append("<p><strong>Failed:</strong> ").append(report.getFailedTests()).append("</p>\n");
    sb.append("<p><strong>Pass Rate:</strong> ")
        .append(String.format("%.1f%%", report.getPassRate() * 100))
        .append("</p>\n");
    sb.append("<p><strong>Average Score:</strong> ")
        .append(String.format("%.2f", report.getAverageScore()))
        .append("</p>\n");
    sb.append("</div>\n");

    // Results table
    sb.append("<h2>Test Results</h2>\n");
    sb.append("<table>\n");
    sb.append("<tr><th>ID</th><th>Question</th><th>Status</th><th>Score</th><th>Category</th></tr>\n");

    for (EvaluationResult result : report.getResults()) {
      String status = result.isPassed() ? "PASS" : "FAIL";
      String statusClass = result.isPassed() ? "pass" : "fail";

      sb.append("<tr>\n");
      sb.append("<td>").append(result.getTestCase().getId()).append("</td>\n");
      sb.append("<td>")
          .append(truncate(result.getTestCase().getQuestion(), 100))
          .append("</td>\n");
      sb.append("<td class='").append(statusClass).append("'>").append(status).append("</td>\n");
      sb.append("<td>").append(String.format("%.2f", result.getScore())).append("</td>\n");
      sb.append("<td>")
          .append(result.getTestCase().getCategory() != null ? result.getTestCase().getCategory() : "N/A")
          .append("</td>\n");
      sb.append("</tr>\n");
    }

    sb.append("</table>\n");
    sb.append("</body>\n</html>");

    return sb.toString();
  }

  /**
   * Writes an HTML report to a file.
   *
   * @param report The evaluation report
   * @param filename The filename (without path)
   * @throws IOException If file writing fails
   */
  public static void writeHtmlReportToFile(EvaluationReport report, String filename)
      throws IOException {
    String htmlContent = generateHtmlReport(report);
    Path reportsDir = Paths.get("build", "reports", "chatbot-eval");
    Files.createDirectories(reportsDir);

    Path reportFile = reportsDir.resolve(filename);
    Files.writeString(reportFile, htmlContent);
  }
}
