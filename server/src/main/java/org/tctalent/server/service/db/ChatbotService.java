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

package org.tctalent.server.service.db;

import org.springframework.lang.NonNull;
import org.tctalent.server.model.db.chatbot.ChatbotMessage;

/**
 * Service for handling chatbot interactions and AI responses.
 * Conversations are tracked by browser session ID.
 */
public interface ChatbotService {

    /**
     * Sends a message to the chatbot and gets an AI response.
     * 
     * @param message The user's message
     * @param sessionId The browser session ID
     * @return The chatbot's response
     */
    @NonNull
    ChatbotMessage sendMessage(@NonNull String message, @NonNull String sessionId);

    /**
     * Gets a welcome message for new users.
     * 
     * @return Welcome message
     */
    @NonNull
    String getWelcomeMessage();
}
