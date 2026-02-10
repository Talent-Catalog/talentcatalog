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

package org.tctalent.server.eval.chatbot;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.tctalent.server.eval.chatbot.model.EvaluationReport;
import org.tctalent.server.eval.chatbot.model.EvaluationResult;
import org.tctalent.server.eval.chatbot.model.EvaluationTestCase;
import org.tctalent.server.eval.chatbot.util.ChatbotEvaluator;
import org.tctalent.server.eval.chatbot.util.EvaluationReportGenerator;
import org.tctalent.server.model.db.chatbot.ChatbotMessage;
import org.tctalent.server.service.db.ChatbotService;

/**
 * Main evaluation test for chatbot.
 * Tests response quality, accuracy, and knowledge coverage against expected keywords and patterns.
 *
 * <p>Tagged with "chatbot-eval" to run separately via: ./gradlew chatbotEval
 *
 * <p>This test makes real API calls to Anthropic and will incur costs.
 * 
 * <p>Note: Requires a running database (start with docker-compose up).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("chatbot-eval")
public class ChatbotEvaluationTest {

  @Autowired private ChatbotService chatbotService;

  private EvaluationReport report;
  private List<EvaluationTestCase> testCases;
  private static final String SESSION_ID = UUID.randomUUID().toString();

  @BeforeAll
  void setup() throws IOException {
    // Load test dataset
    testCases = loadTestDataset("chatbot/eval/chatbot_evaluation_dataset.json");

    // Initialize report
    report = new EvaluationReport();
    report.setSuiteName("Chatbot Evaluation");
    report.setStartTime(OffsetDateTime.now());

    System.out.println("\n========================================");
    System.out.println("Starting Chatbot Evaluation");
    System.out.println("Test cases loaded: " + testCases.size());
    System.out.println("Session ID: " + SESSION_ID);
    System.out.println("========================================\n");
  }

  @Test
  void evaluateChatbot() {
    System.out.println("Running chatbot evaluation...\n");

    int testNumber = 1;
    for (EvaluationTestCase testCase : testCases) {
      System.out.printf(
          "[%d/%d] Testing: %s\n", testNumber++, testCases.size(), testCase.getId());

      try {
        // Get response from chatbot (includes FAQ IDs)
        ChatbotMessage response = chatbotService.sendMessage(testCase.getQuestion(), SESSION_ID);

        // Evaluate the response
        EvaluationResult result = ChatbotEvaluator.evaluate(testCase, response);
        report.addResult(result);

        // Print result summary
        System.out.println("  Status: " + (result.isPassed() ? "✓ PASS" : "✗ FAIL"));
        System.out.println("  Score: " + String.format("%.2f", result.getScore()));
        if (!result.isPassed()) {
          System.out.println("  Feedback: " + String.join(", ", result.getFeedback()));
        }
        System.out.println();

      } catch (Exception e) {
        System.err.println("  ERROR: " + e.getMessage());
        // Create failed result for exceptions
        EvaluationResult errorResult = new EvaluationResult();
        errorResult.setTestCase(testCase);
        errorResult.setActualResponse("ERROR: " + e.getMessage());
        errorResult.setPassed(false);
        errorResult.setScore(0.0);
        errorResult.addFeedback("Exception during evaluation: " + e.getMessage());
        report.addResult(errorResult);
        System.out.println();
      }
    }
  }

  @AfterAll
  void generateReport() {
    report.setEndTime(OffsetDateTime.now());
    report.calculateStatistics();

    // Print console report
    String consoleReport = EvaluationReportGenerator.generateConsoleReport(report);
    System.out.println(consoleReport);

    // Write reports to files
    try {
      EvaluationReportGenerator.writeReportToFile(report, "chatbot-evaluation-report.txt");
      EvaluationReportGenerator.writeHtmlReportToFile(report, "chatbot-evaluation-report.html");
      System.out.println("Reports written to build/reports/chatbot-eval/");
    } catch (IOException e) {
      System.err.println("Failed to write report files: " + e.getMessage());
    }

    // Assert overall pass rate meets threshold
    double minimumPassRate = 0.80; // 80% pass rate required
    assertTrue(
        report.getPassRate() >= minimumPassRate,
        String.format(
            "Chatbot evaluation pass rate %.1f%% is below threshold %.1f%%",
            report.getPassRate() * 100, minimumPassRate * 100));
  }

  /**
   * Loads test cases from a JSON file.
   *
   * @param resourcePath Path to the JSON file in resources
   * @return List of test cases
   * @throws IOException If file cannot be read
   */
  private List<EvaluationTestCase> loadTestDataset(String resourcePath) throws IOException {
    ClassPathResource resource = new ClassPathResource(resourcePath);
    InputStream inputStream = resource.getInputStream();
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.readValue(inputStream, new TypeReference<List<EvaluationTestCase>>() {});
  }
}
