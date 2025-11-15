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

package org.tctalent.server.api.chatbot;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tctalent.server.model.db.chatbot.ChatbotMessage;
import org.tctalent.server.service.db.ChatbotService;

import java.util.HashMap;
import java.util.Map;

/**
 * REST API for chatbot functionality.
 * Provides endpoints for sending messages and managing chatbot interactions.
 */
@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
@Slf4j
public class ChatbotApi {

    private final ChatbotService chatbotService;

    /**
     * Sends a message to the chatbot and gets a response.
     * 
     * @param request The message request containing the user's message and session ID
     * @return The chatbot's response
     */
    @PostMapping("/send")
    public ResponseEntity<ChatbotMessage> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        try {
            if (request.getSessionId() == null || request.getSessionId().trim().isEmpty()) {
                log.warn("Received message request without session ID");
                return ResponseEntity.badRequest().build();
            }
            
            ChatbotMessage response = chatbotService.sendMessage(request.getMessage(), request.getSessionId());
            
            log.info("Chatbot message processed for session: {}", request.getSessionId());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error processing chatbot message", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Gets a welcome message for the chatbot.
     * 
     * @return Welcome message
     */
    @GetMapping("/welcome")
    public ResponseEntity<Map<String, String>> getWelcomeMessage() {
        try {
            String welcomeMessage = chatbotService.getWelcomeMessage();
            
            Map<String, String> response = new HashMap<>();
            response.put("message", welcomeMessage);
            
            log.info("Welcome message generated");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error generating welcome message", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Request DTO for sending messages to the chatbot.
     */
    public static class SendMessageRequest {
        @NotBlank(message = "Message cannot be blank")
        @Size(max = 2000, message = "Message cannot exceed 2000 characters")
        private String message;
        private String sessionId;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }
    }
}
