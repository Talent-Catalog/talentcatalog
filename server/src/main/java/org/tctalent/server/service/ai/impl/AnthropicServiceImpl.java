/*
 * Copyright (c) 2025 Talent Beyond Boundaries.
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

package org.tctalent.server.service.ai.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.tctalent.server.configuration.properties.AnthropicProperties;
import org.tctalent.server.service.ai.AnthropicService;
import org.tctalent.server.service.ai.dto.AnthropicRequest;
import org.tctalent.server.service.ai.dto.AnthropicResponse;

import java.util.Arrays;

/**
 * Implementation of AnthropicService using Spring WebClient.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnthropicServiceImpl implements AnthropicService {

    private final AnthropicProperties properties;
    private final WebClient anthropicWebClient;

    @Override
    public String sendMessage(String userMessage, String systemContext) {

        try {
            // Build the system prompt with instructions
            String systemPrompt = buildSystemPrompt(systemContext);
            
            // Create the request
            AnthropicRequest request = AnthropicRequest.builder()
                .model(properties.getModel())
                .maxTokens(properties.getMaxTokens())
                .temperature(properties.getTemperature())
                .system(systemPrompt)
                .messages(Arrays.asList(
                    AnthropicRequest.Message.builder()
                        .role("user")
                        .content(userMessage)
                        .build()
                ))
                .build();

            log.info("Sending message to Claude API: {}", userMessage);

            AnthropicResponse response = anthropicWebClient.post()
                .uri("/v1/messages")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AnthropicResponse.class)
                .block();

            if (response == null || response.getContent() == null || response.getContent().isEmpty()) {
                log.error("Empty response from Claude API");
                return "I apologize, but I could not generate a response. Please try again.";
            }

            String aiResponse = response.getContent().get(0).getText();
            log.info("Received response from Claude API: {}", aiResponse);
            
            return aiResponse;

        } catch (WebClientResponseException e) {
            log.error("Error calling Claude API: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return "I apologize, but I encountered an error processing your request. Please try again later.";
        } catch (Exception e) {
            log.error("Unexpected error calling Claude API", e);
            return "I apologize, but I encountered an error processing your request. Please try again later.";
        }
    }

    /**
     * Builds the system prompt with QA context and instructions.
     */
    private String buildSystemPrompt(String qaContext) {
        return "You are a helpful assistant for Talent Beyond Boundaries (TBB), " +
            "a nonprofit organization that creates labour mobility pathways for refugees and displaced people. " +
            "Your role is to answer questions about TBB's services, the registration process, visa requirements, " +
            "and labour mobility opportunities.\n\n" +
            "IMPORTANT INSTRUCTIONS:\n" +
            "1. ONLY answer questions that are covered in the QA knowledge base provided below.\n" +
            "2. If a question is NOT in the knowledge base, respond with: " +
            "\"I am sorry, I am not smart enough to answer that question with confidence, but I now have it logged and hope to learn in the future!\"\n" +
            "3. Be concise, helpful, and professional in your responses.\n" +
            "4. Do not make up information or provide answers outside the knowledge base.\n" +
            "5. If applicable, reference relevant links from the knowledge base.\n\n" +
            "QA KNOWLEDGE BASE:\n" + qaContext;
    }
}

