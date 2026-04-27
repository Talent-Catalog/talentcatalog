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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.tctalent.server.eval.chatbot.model.EvaluationResult;
import org.tctalent.server.eval.chatbot.model.EvaluationTestCase;
import org.tctalent.server.model.db.chatbot.ChatbotMessage;

/**
 * Utility class for evaluating chatbot responses against expected criteria.
 * Evaluates based on FAQ ID references for tracking and validation.
 */
public class ChatbotEvaluator {

  /**
   * Evaluates a chatbot response against a test case's expected criteria.
   *
   * @param testCase The test case with expected criteria
   * @param chatbotMessage The actual ChatbotMessage from the service (contains FAQ IDs)
   * @return Evaluation result with pass/fail status and detailed feedback
   */
  public static EvaluationResult evaluate(EvaluationTestCase testCase, ChatbotMessage chatbotMessage) {
    EvaluationResult result = new EvaluationResult();
    result.setTestCase(testCase);
    result.setActualResponse(chatbotMessage.getMessage());

    // Normalize response for comparison
    String normalizedResponse = chatbotMessage.getMessage().toLowerCase(Locale.ROOT);

    // Check for expected response pattern (for edge cases like "not smart enough")
    if (testCase.getExpectedResponse() != null && !testCase.getExpectedResponse().isEmpty()) {
      evaluateExpectedResponse(testCase, normalizedResponse, result);
    }
    // Check for expected FAQ IDs
    else if (testCase.getExpectedFaqIds() != null) {
      evaluateFaqIds(testCase, chatbotMessage, result);
    }
    // No specific criteria - just check it's not empty
    else {
      if (chatbotMessage.getMessage() == null || chatbotMessage.getMessage().trim().isEmpty()) {
        result.setPassed(false);
        result.setScore(0.0);
        result.addFeedback("Response is empty");
      } else {
        result.setPassed(true);
        result.setScore(1.0);
        result.addFeedback("Response received");
      }
    }

    return result;
  }

  /**
   * Evaluates if the response matches the expected response pattern.
   * Used for edge cases where we expect specific fallback messages.
   */
  private static void evaluateExpectedResponse(
      EvaluationTestCase testCase, String normalizedResponse, EvaluationResult result) {

    String expectedPattern = testCase.getExpectedResponse().toLowerCase(Locale.ROOT);

    if (normalizedResponse.contains(expectedPattern)) {
      result.setPassed(true);
      result.setScore(1.0);
      result.addFeedback("Response contains expected pattern: " + testCase.getExpectedResponse());
    } else {
      result.setPassed(false);
      result.setScore(0.0);
      result.addFeedback(
          "Response does not contain expected pattern: " + testCase.getExpectedResponse());
      result.addFeedback("Actual response: " + result.getActualResponse());
    }
  }

  /**
   * Evaluates FAQ ID references in the chatbot response.
   * Validates that the expected FAQs were referenced (or that no FAQs were referenced for out-of-scope).
   */
  private static void evaluateFaqIds(
      EvaluationTestCase testCase, ChatbotMessage chatbotMessage, EvaluationResult result) {

    List<String> expectedIds = testCase.getExpectedFaqIds();
    List<String> actualIds = chatbotMessage.getReferencedFaqIds();

    // Handle null/empty actual IDs
    if (actualIds == null) {
      actualIds = Collections.emptyList();
    }

    // Case 1: Empty expected IDs means we expect NO FAQ references (out-of-scope question)
    if (expectedIds.isEmpty()) {
      if (actualIds.isEmpty()) {
        result.setPassed(true);
        result.setScore(1.0);
        result.addFeedback("Correctly did not reference any FAQs (out-of-scope question handled properly)");
      } else {
        result.setPassed(false);
        result.setScore(0.0);
        result.addFeedback("FAIL: Expected no FAQ references, but found: " + String.join(", ", actualIds));
      }
      return;
    }

    // Case 2: Expected IDs specified - validate they are present
    List<String> foundIds = new ArrayList<>();
    List<String> missingIds = new ArrayList<>();

    for (String expectedId : expectedIds) {
      if (actualIds.contains(expectedId)) {
        foundIds.add(expectedId);
      } else {
        missingIds.add(expectedId);
      }
    }

    // Calculate score as percentage of expected IDs found
    double score = expectedIds.isEmpty() ? 1.0 : (double) foundIds.size() / expectedIds.size();
    result.setScore(score);

    // Pass if all expected IDs were found
    boolean passed = missingIds.isEmpty();
    result.setPassed(passed);

    // Add feedback
    if (passed) {
      result.addFeedback(
          String.format(
              "All expected FAQ IDs referenced: %s",
              String.join(", ", foundIds)));
      
      // Note if there were additional unexpected FAQs (not a failure, just informational)
      List<String> unexpectedIds = new ArrayList<>(actualIds);
      unexpectedIds.removeAll(expectedIds);
      if (!unexpectedIds.isEmpty()) {
        result.addFeedback(
            "Also referenced (not required): " + String.join(", ", unexpectedIds));
      }
    } else {
      result.addFeedback(
          String.format(
              "FAIL: Only found %d of %d expected FAQ IDs (%.0f%%)",
              foundIds.size(), expectedIds.size(), score * 100));
      result.addFeedback("Missing FAQ IDs: " + String.join(", ", missingIds));
      if (!foundIds.isEmpty()) {
        result.addFeedback("Found FAQ IDs: " + String.join(", ", foundIds));
      }
    }
  }

  /**
   * Checks if the response indicates the chatbot stayed within its knowledge bounds.
   * Used for validating out-of-scope question handling.
   *
   * @param response The chatbot response
   * @return true if the response indicates the bot declined to answer
   */
  public static boolean isOutOfScopeResponse(String response) {
    if (response == null) {
      return false;
    }
    String normalized = response.toLowerCase(Locale.ROOT);
    return normalized.contains("not smart enough")
        || normalized.contains("cannot answer")
        || normalized.contains("don't have information");
  }

  /**
   * Checks if the response appears to be a valid, non-error response.
   *
   * @param response The chatbot response
   * @return true if the response seems valid
   */
  public static boolean isValidResponse(String response) {
    if (response == null || response.trim().isEmpty()) {
      return false;
    }
    // Check for common error patterns
    String normalized = response.toLowerCase(Locale.ROOT);
    return !normalized.contains("error processing")
        && !normalized.contains("try again later")
        && response.length() > 10; // Arbitrary minimum for meaningful response
  }
}
