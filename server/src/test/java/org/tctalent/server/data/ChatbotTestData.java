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

package org.tctalent.server.data;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.tctalent.server.model.db.chatbot.ChatbotMessage;

/**
 * Utility class for creating chatbot-related test data.
 */
public final class ChatbotTestData {

  private ChatbotTestData() {}

  public static ChatbotMessage createUserMessage(UUID sessionId, UUID questionId, String message) {
    return createMessage(sessionId, questionId, ChatbotMessage.ChatbotSender.user, message);
  }

  public static ChatbotMessage createBotMessage(UUID sessionId, UUID questionId, String message) {
    return createMessage(sessionId, questionId, ChatbotMessage.ChatbotSender.bot, message);
  }

  public static ChatbotMessage createMessage(
      UUID sessionId,
      UUID questionId,
      ChatbotMessage.ChatbotSender sender,
      String message) {
    ChatbotMessage chatbotMessage = new ChatbotMessage();
    chatbotMessage.setId(UUID.randomUUID());
    chatbotMessage.setSessionId(sessionId);
    chatbotMessage.setQuestionId(questionId != null ? questionId : UUID.randomUUID());
    chatbotMessage.setSender(sender);
    chatbotMessage.setMessage(message);
    chatbotMessage.setTimestamp(OffsetDateTime.now());
    return chatbotMessage;
  }
}

