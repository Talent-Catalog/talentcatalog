/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.integration.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.tctalent.server.data.ChatbotTestData.createBotMessage;
import static org.tctalent.server.data.ChatbotTestData.createUserMessage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.tctalent.server.integration.helper.BaseJpaIntegrationTest;
import org.tctalent.server.integration.helper.PostgresTestContainer;
import org.tctalent.server.model.db.chatbot.ChatbotMessage;
import org.tctalent.server.repository.db.ChatbotMessageRepository;

class ChatbotMessageRepositoryIntegrationTest extends BaseJpaIntegrationTest {

  @Autowired private ChatbotMessageRepository chatbotMessageRepository;
  @Autowired private JdbcTemplate jdbcTemplate;

  private UUID sessionId;

  @BeforeAll
  public static void setup() throws IOException, InterruptedException {
    PostgresTestContainer.startContainer();
  }

  @BeforeEach
  void setUp() {
    assertTrue(isContainerInitialised(), "Database container should be initialized");
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
        ChatbotMessage.ChatbotSender.USER,
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
        matchedMessages.stream().anyMatch(msg -> msg.getSender() == ChatbotMessage.ChatbotSender.USER),
        "User message should be persisted");
    assertTrue(
        matchedMessages.stream().anyMatch(msg -> msg.getSender() == ChatbotMessage.ChatbotSender.BOT),
        "Bot message should be persisted");
  }

  @Test
  void shouldStoreReferencedFaqIdsAsJsonArray() throws Exception {
    ChatbotMessage message = createBotMessage(sessionId, UUID.randomUUID(), "Answer");
    message.setReferencedFaqIds(List.of("FAQ-001", "FAQ-003"));
    chatbotMessageRepository.saveAndFlush(message);

    //TC-1189 regression: the jsonb column format must not change
    String stored = jdbcTemplate.queryForObject(
        "select referenced_faq_ids::text from chatbot_message where id = ?",
        String.class, message.getId());
    JSONAssert.assertEquals("Stored JSON: " + stored,
        "[\"FAQ-001\", \"FAQ-003\"]", stored, JSONCompareMode.STRICT);
  }

  @Test
  void shouldStoreNullReferencedFaqIdsAsNull() {
    ChatbotMessage message = createUserMessage(sessionId, UUID.randomUUID(), "Question");
    chatbotMessageRepository.saveAndFlush(message);

    Integer nullCount = jdbcTemplate.queryForObject(
        "select count(*) from chatbot_message where id = ? and referenced_faq_ids is null",
        Integer.class, message.getId());
    assertEquals(1, nullCount);
  }

  @Test
  void shouldReadPreviouslyStoredReferencedFaqIds() {
    UUID id = UUID.randomUUID();
    jdbcTemplate.update("""
        insert into chatbot_message
            (id, session_id, question_id, sender, message, timestamp, referenced_faq_ids)
        values (?, ?, ?, 'BOT', 'Answer', now(), cast(? as jsonb))
        """, id, sessionId, UUID.randomUUID(), "[\"FAQ-002\", \"FAQ-010\"]");

    ChatbotMessage message = chatbotMessageRepository.findById(id).orElseThrow();

    assertEquals(List.of("FAQ-002", "FAQ-010"), message.getReferencedFaqIds());
  }

}

