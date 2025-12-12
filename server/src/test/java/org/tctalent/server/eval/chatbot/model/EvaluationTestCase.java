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

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

/**
 * Represents a single test case for chatbot evaluation.
 * Loaded from JSON test datasets.
 */
@Data
public class EvaluationTestCase {

  /** Unique identifier for the test case */
  @JsonProperty("id")
  private String id;

  /** The question to ask the chatbot */
  @JsonProperty("question")
  private String question;

  /** FAQ IDs that should be referenced in the response (e.g., ["faq_001", "faq_003"]).
   * Empty list means no FAQs should be referenced (for out-of-scope questions). */
  @JsonProperty("expectedFaqIds")
  private List<String> expectedFaqIds;

  /** Expected response pattern (for edge cases like "not smart enough") */
  @JsonProperty("expectedResponse")
  private String expectedResponse;

  /** Category of the test case (e.g., "about_tbb", "registration", "cost") */
  @JsonProperty("category")
  private String category;

  /** Type of test case (e.g., "out_of_scope", "malicious_input") */
  @JsonProperty("type")
  private String type;

  /** Human-readable description of what this test validates */
  @JsonProperty("description")
  private String description;
}
