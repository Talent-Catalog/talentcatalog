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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.model.db.chatbot.ChatbotMessage;
import org.tctalent.server.repository.db.ChatbotMessageRepository;
import org.tctalent.server.service.ai.AnthropicService;
import org.tctalent.server.service.db.ChatbotService;
import org.tctalent.server.service.db.QAService;
import org.tctalent.server.util.html.HtmlSanitizer;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Implementation of ChatbotService.
 * Conversations are tracked by browser session ID and persisted to the database.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotServiceImpl implements ChatbotService {

    private final AnthropicService anthropicService;
    private final QAService qaService;
    private final ChatbotMessageRepository chatbotMessageRepository;

    @Override
    @Transactional
    public ChatbotMessage sendMessage(String message, String sessionId) {
        log.info("Chatbot message received from session {}: {}", sessionId, message);
        
        try {
            // Parse sessionId from String to UUID
            UUID sessionUUID = UUID.fromString(sessionId);
            
            // Generate a question_id that will link the user question and bot answer
            UUID questionId = UUID.randomUUID();
            
            // Sanitize user message before saving to database
            String sanitizedMessage = HtmlSanitizer.sanitize(message);
            
            // Save user message to database
            ChatbotMessage userMessage = new ChatbotMessage();
            userMessage.setId(UUID.randomUUID());
            userMessage.setSessionId(sessionUUID);
            userMessage.setQuestionId(questionId);
            userMessage.setSender(ChatbotMessage.ChatbotSender.user);
            userMessage.setMessage(sanitizedMessage);
            userMessage.setTimestamp(OffsetDateTime.now());
            chatbotMessageRepository.save(userMessage);
            log.debug("Saved user message to database with question_id: {}", questionId);
            
            // Load QA context
            String qaContext = qaService.loadQAContext();
            
            // Send sanitized message to AI service
            String aiResponse = anthropicService.sendMessage(sanitizedMessage, qaContext);
            
            // Create and save bot response with the same question_id
            ChatbotMessage response = new ChatbotMessage();
            response.setId(UUID.randomUUID());
            response.setSessionId(sessionUUID);
            response.setQuestionId(questionId); // Same question_id links question to answer
            response.setSender(ChatbotMessage.ChatbotSender.bot);
            response.setMessage(aiResponse);
            response.setTimestamp(OffsetDateTime.now());
            chatbotMessageRepository.save(response);
            
            log.info("Chatbot response sent to session {} with question_id: {}", sessionId, questionId);
            return response;
            
        } catch (Exception e) {
            log.error("Error processing chatbot message for session {}", sessionId, e);
            
            // Return fallback response on error
            UUID sessionUUID;
            try {
                sessionUUID = UUID.fromString(sessionId);
            } catch (IllegalArgumentException ex) {
                log.error("Invalid session ID format: {}", sessionId);
                throw new IllegalArgumentException("Invalid session ID format", ex);
            }
            
            UUID questionId = UUID.randomUUID();
            ChatbotMessage response = new ChatbotMessage();
            response.setId(UUID.randomUUID());
            response.setSessionId(sessionUUID);
            response.setQuestionId(questionId);
            response.setSender(ChatbotMessage.ChatbotSender.bot);
            response.setMessage("I apologize, but I encountered an error processing your request. Please try again later.");
            response.setTimestamp(OffsetDateTime.now());
            chatbotMessageRepository.save(response);
            return response;
        }
    }

    @Override
    public String getWelcomeMessage() {
        log.info("Generating welcome message");
        
        return "Hello! I'm here to help you learn about Talent Catalog. " +
            "What would you like to know?";
    }
}
