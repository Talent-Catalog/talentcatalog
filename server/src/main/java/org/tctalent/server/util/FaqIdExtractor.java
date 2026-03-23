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

package org.tctalent.server.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for extracting FAQ ID citations from chatbot responses.
 * Claude cites FAQs inline using format [FAQ-001], which we extract for tracking
 * and remove from the user-facing response.
 */
public class FaqIdExtractor {

    // Pattern to match [FAQ-001], [FAQ-019], etc. (case insensitive)
    private static final Pattern FAQ_ID_PATTERN = 
        Pattern.compile("\\[FAQ-\\d{3}\\]", Pattern.CASE_INSENSITIVE);

    /**
     * Extracts FAQ IDs from a response and returns cleaned response without citations.
     *
     * @param response The raw response from Claude containing FAQ citations
     * @return ExtractionResult with cleaned response and list of FAQ IDs
     */
    public static ExtractionResult extractFaqIds(String response) {
        if (response == null || response.trim().isEmpty()) {
            return new ExtractionResult(response, new ArrayList<>());
        }

        List<String> faqIds = new ArrayList<>();
        Matcher matcher = FAQ_ID_PATTERN.matcher(response);

        // Extract all FAQ IDs
        while (matcher.find()) {
            String rawId = matcher.group(); // e.g., "[FAQ-001]"
            // Convert to standard format: faq_001
            String normalizedId = rawId.toLowerCase()
                .replace("[", "")
                .replace("]", "")
                .replace("-", "_");
            
            // Avoid duplicates
            if (!faqIds.contains(normalizedId)) {
                faqIds.add(normalizedId);
            }
        }

        // Remove all FAQ citations from the response
        String cleanedResponse = FAQ_ID_PATTERN.matcher(response)
            .replaceAll("")
            .trim();
        
        // Clean up any resulting double spaces or trailing newlines
        cleanedResponse = cleanedResponse.replaceAll("\\s+", " ").trim();

        return new ExtractionResult(cleanedResponse, faqIds);
    }

    /**
     * Result of FAQ ID extraction containing the cleaned response and extracted IDs.
     */
    @Data
    @AllArgsConstructor
    public static class ExtractionResult {
        /**
         * The response with FAQ citations removed (shown to user)
         */
        private String cleanedResponse;

        /**
         * List of extracted FAQ IDs in format faq_001, faq_002, etc.
         */
        private List<String> faqIds;
    }
}
