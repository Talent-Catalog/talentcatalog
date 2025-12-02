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

package org.tctalent.server.integration.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.tctalent.server.data.ChatbotTestData.createBotMessage;
import static org.tctalent.server.data.ChatbotTestData.createUserMessage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.tctalent.server.model.db.chatbot.ChatbotMessage;
import org.tctalent.server.repository.db.ChatbotMessageRepository;

@DataJpaTest
@TestPropertySource(
    properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create"})
class ChatbotMessageRepositoryIntegrationTest {

  @Autowired private ChatbotMessageRepository chatbotMessageRepository;

  private UUID sessionId;

  @BeforeEach
  void setUp() {
    sessionId = UUID.randomUUID();
  }

  @Test
  void shouldSaveAndRetrieveMessageById() {
    ChatbotMessage message =
        createUserMessage(sessionId, UUID.randomUUID(), "How do I register?");
    chatbotMessageRepository.save(message);

    Optional<ChatbotMessage> retrieved = chatbotMessageRepository.findById(message.getId());

    assertTrue(retrieved.isPresent());
    assertEquals(sessionId, retrieved.get().getSessionId());
    assertEquals(
        ChatbotMessage.ChatbotSender.user,
        retrieved.get().getSender(),
        "Sender should be persisted");
  }

  @Test
  void shouldLinkMessagesByQuestionId() {
    UUID questionId = UUID.randomUUID();

    ChatbotMessage userMessage =
        createUserMessage(sessionId, questionId, "What documents do I need?");
    ChatbotMessage botMessage =
        createBotMessage(sessionId, questionId, "A passport is recommended.");

    chatbotMessageRepository.save(userMessage);
    chatbotMessageRepository.save(botMessage);

    List<ChatbotMessage> matchedMessages =
        chatbotMessageRepository.findAll().stream()
            .filter(message -> questionId.equals(message.getQuestionId()))
            .collect(Collectors.toList());

    assertEquals(2, matchedMessages.size());
    assertTrue(
        matchedMessages.stream().anyMatch(msg -> msg.getSender() == ChatbotMessage.ChatbotSender.user),
        "User message should be persisted");
    assertTrue(
        matchedMessages.stream().anyMatch(msg -> msg.getSender() == ChatbotMessage.ChatbotSender.bot),
        "Bot message should be persisted");
  }

}

