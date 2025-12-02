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

package org.tctalent.server.api.chatbot;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.tctalent.server.model.db.chatbot.ChatbotMessage;
import org.tctalent.server.service.db.ChatbotService;
import org.tctalent.server.service.db.email.EmailHelper;

@WebMvcTest(ChatbotApi.class)
@AutoConfigureMockMvc(addFilters = false)
class ChatbotApiTest {

  private static final String BASE_PATH = "/api/chatbot";
  private static final String SESSION_ID = UUID.randomUUID().toString();

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private ChatbotService chatbotService;
  @MockBean private EmailHelper emailHelper;

  private ChatbotMessage chatbotMessage;

  @BeforeEach
  void setUp() {
    chatbotMessage = buildChatbotMessage();
  }

  @Test
  void sendMessage_success() throws Exception {
    when(chatbotService.sendMessage("Hello there", SESSION_ID)).thenReturn(chatbotMessage);

    Map<String, String> body = new HashMap<>();
    body.put("message", "Hello there");
    body.put("sessionId", SESSION_ID);

    mockMvc
        .perform(
            post(BASE_PATH + "/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.message", is("How can I assist you today?")))
        .andExpect(jsonPath("$.sender", is("bot")))
        .andExpect(jsonPath("$.sessionId", notNullValue()));

    verify(chatbotService).sendMessage("Hello there", SESSION_ID);
  }

  @Test
  void sendMessage_missingSessionId_returnsBadRequest() throws Exception {
    Map<String, String> body = new HashMap<>();
    body.put("message", "Hello there");

    mockMvc
        .perform(
            post(BASE_PATH + "/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
        .andDo(print())
        .andExpect(status().isBadRequest());

    verifyNoInteractions(chatbotService);
  }

  @Test
  void sendMessage_blankMessage_returnsBadRequest() throws Exception {
    Map<String, String> body = new HashMap<>();
    body.put("message", "   ");
    body.put("sessionId", SESSION_ID);

    mockMvc
        .perform(
            post(BASE_PATH + "/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
        .andDo(print())
        .andExpect(status().isBadRequest());

    verifyNoInteractions(chatbotService);
  }

  @Test
  void sendMessage_messageTooLong_returnsBadRequest() throws Exception {
    String longMessage = "a".repeat(2001);
    Map<String, String> body = new HashMap<>();
    body.put("message", longMessage);
    body.put("sessionId", SESSION_ID);

    mockMvc
        .perform(
            post(BASE_PATH + "/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
        .andDo(print())
        .andExpect(status().isBadRequest());

    verifyNoInteractions(chatbotService);
  }

  @Test
  void sendMessage_serviceThrows_returnsInternalServerError() throws Exception {
    when(chatbotService.sendMessage(anyString(), anyString()))
        .thenThrow(new IllegalStateException("boom"));

    Map<String, String> body = new HashMap<>();
    body.put("message", "Hello");
    body.put("sessionId", SESSION_ID);

    mockMvc
        .perform(
            post(BASE_PATH + "/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
        .andDo(print())
        .andExpect(status().isInternalServerError());

    verify(chatbotService).sendMessage("Hello", SESSION_ID);
  }

  @Test
  void getWelcomeMessage_success() throws Exception {
    when(chatbotService.getWelcomeMessage()).thenReturn("Hi");

    mockMvc
        .perform(get(BASE_PATH + "/welcome").contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.message", is("Hi")));

    verify(chatbotService).getWelcomeMessage();
  }

  @Test
  void getWelcomeMessage_serviceThrows_returnsInternalServerError() throws Exception {
    when(chatbotService.getWelcomeMessage()).thenThrow(new RuntimeException("fail"));

    mockMvc
        .perform(get(BASE_PATH + "/welcome").contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isInternalServerError());

    verify(chatbotService).getWelcomeMessage();
  }

  private ChatbotMessage buildChatbotMessage() {
    ChatbotMessage message = new ChatbotMessage();
    message.setId(UUID.randomUUID());
    message.setQuestionId(UUID.randomUUID());
    message.setSessionId(UUID.randomUUID());
    message.setSender(ChatbotMessage.ChatbotSender.bot);
    message.setMessage("How can I assist you today?");
    message.setTimestamp(OffsetDateTime.now());
    return message;
  }
}

