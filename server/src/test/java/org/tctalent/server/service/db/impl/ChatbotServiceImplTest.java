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

package org.tctalent.server.service.db.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.model.db.chatbot.ChatbotMessage;
import org.tctalent.server.repository.db.ChatbotMessageRepository;
import org.tctalent.server.service.ai.AnthropicService;
import org.tctalent.server.service.db.QAService;
import org.tctalent.server.util.html.HtmlSanitizer;

@ExtendWith(MockitoExtension.class)
class ChatbotServiceImplTest {

  private static final String SESSION_ID = UUID.randomUUID().toString();
  private static final String QA_CONTEXT = "context";

  @Mock private AnthropicService anthropicService;
  @Mock private QAService qaService;
  @Mock private ChatbotMessageRepository chatbotMessageRepository;

  @InjectMocks private ChatbotServiceImpl chatbotService;

  @Test
  void sendMessage_success() {
    when(qaService.loadQAContext()).thenReturn(QA_CONTEXT);
    when(anthropicService.sendMessage("Hello world", QA_CONTEXT)).thenReturn("Bot response");

    try (MockedStatic<HtmlSanitizer> sanitizer = mockStatic(HtmlSanitizer.class)) {
      sanitizer.when(() -> HtmlSanitizer.sanitize("Hello <b>world</b>")).thenReturn("Hello world");

      ChatbotMessage response = chatbotService.sendMessage("Hello <b>world</b>", SESSION_ID);

      assertEquals("Bot response", response.getMessage());
      assertEquals(ChatbotMessage.ChatbotSender.bot, response.getSender());

      ArgumentCaptor<ChatbotMessage> captor = ArgumentCaptor.forClass(ChatbotMessage.class);
      verify(chatbotMessageRepository, times(2)).save(captor.capture());

      List<ChatbotMessage> savedMessages = captor.getAllValues();
      ChatbotMessage savedUserMessage = savedMessages.get(0);
      assertEquals("Hello world", savedUserMessage.getMessage());
      assertEquals(ChatbotMessage.ChatbotSender.user, savedUserMessage.getSender());

      ChatbotMessage savedBotMessage = savedMessages.get(1);
      assertEquals("Bot response", savedBotMessage.getMessage());
      assertEquals(ChatbotMessage.ChatbotSender.bot, savedBotMessage.getSender());
    }

    verify(qaService).loadQAContext();
    verify(anthropicService).sendMessage("Hello world", QA_CONTEXT);
  }

  @Test
  void sendMessage_invalidSessionId_throwsException() {
    assertThrows(
        IllegalArgumentException.class, () -> chatbotService.sendMessage("Hello", "invalid"));

    verifyNoInteractions(chatbotMessageRepository, qaService, anthropicService);
  }

  @Test
  void sendMessage_aiServiceThrows_returnsFallbackMessage() {
    when(qaService.loadQAContext()).thenReturn(QA_CONTEXT);
    when(anthropicService.sendMessage(anyString(), anyString()))
        .thenThrow(new RuntimeException("failure"));

    try (MockedStatic<HtmlSanitizer> sanitizer = mockStatic(HtmlSanitizer.class)) {
      sanitizer.when(() -> HtmlSanitizer.sanitize("Help")).thenReturn("Help");

      ChatbotMessage response = chatbotService.sendMessage("Help", SESSION_ID);

      assertEquals(
          "I apologize, but I encountered an error processing your request. Please try again later.",
          response.getMessage());
      assertEquals(ChatbotMessage.ChatbotSender.bot, response.getSender());
    }

    verify(chatbotMessageRepository, times(2)).save(any(ChatbotMessage.class));
  }

  @Test
  void getWelcomeMessage_returnsStaticText() {
    String welcomeMessage = chatbotService.getWelcomeMessage();

    assertThat(welcomeMessage).contains("Talent Catalog");
  }
}

