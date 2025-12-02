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

package org.tctalent.server.service.ai.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.tctalent.server.configuration.properties.AnthropicProperties;
import org.tctalent.server.service.ai.dto.AnthropicRequest;
import org.tctalent.server.service.ai.dto.AnthropicResponse;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class AnthropicServiceImplTest {

  private static final String QA_CONTEXT = "context";
  private static final String USER_MESSAGE = "How do I apply?";

  @Mock private AnthropicProperties anthropicProperties;
  @Mock private WebClient anthropicWebClient;
  @Mock private RequestBodyUriSpec requestBodyUriSpec;
  @Mock private RequestBodySpec requestBodySpec;
  @SuppressWarnings("rawtypes")
  @Mock
  private RequestHeadersSpec requestHeadersSpec;
  @Mock private ResponseSpec responseSpec;

  @InjectMocks private AnthropicServiceImpl anthropicService;

  @BeforeEach
  void setUp() {
    when(anthropicProperties.getModel()).thenReturn("claude");
    when(anthropicProperties.getMaxTokens()).thenReturn(1000);
    when(anthropicProperties.getTemperature()).thenReturn(0.5);
  }

  @Test
  void sendMessage_successfullyReturnsResponse() {
    AnthropicResponse.Content content = new AnthropicResponse.Content();
    content.setText("You can sign up on our portal.");
    AnthropicResponse response = new AnthropicResponse();
    response.setContent(List.of(content));

    mockWebClientChain(Mono.just(response));

    String actualResponse = anthropicService.sendMessage(USER_MESSAGE, QA_CONTEXT);

    assertEquals("You can sign up on our portal.", actualResponse);

    ArgumentCaptor<AnthropicRequest> requestCaptor =
        ArgumentCaptor.forClass(AnthropicRequest.class);
    verify(requestBodySpec).bodyValue(requestCaptor.capture());
    AnthropicRequest request = requestCaptor.getValue();

    assertEquals("claude", request.getModel());
    assertEquals(1000, request.getMaxTokens());
    assertEquals(0.5, request.getTemperature());
    String systemPrompt = request.getSystem();
    assertTrue(systemPrompt.endsWith(QA_CONTEXT));
    assertEquals(1, request.getMessages().size());
    assertEquals(USER_MESSAGE, request.getMessages().get(0).getContent());
  }

  @Test
  void sendMessage_emptyResponse_returnsFallback() {
    mockWebClientChain(Mono.just(new AnthropicResponse()));

    String actualResponse = anthropicService.sendMessage(USER_MESSAGE, QA_CONTEXT);

    assertEquals(
        "I apologize, but I could not generate a response. Please try again.", actualResponse);
  }

  @Test
  void sendMessage_webClientResponseException_returnsErrorMessage() {
    WebClientResponseException webClientException =
        WebClientResponseException.create(500, "error", null, null, null);
    mockWebClientChain(Mono.error(webClientException));

    String actualResponse = anthropicService.sendMessage(USER_MESSAGE, QA_CONTEXT);

    assertEquals(
        "I apologize, but I encountered an error processing your request. Please try again later.",
        actualResponse);
  }

  @Test
  void sendMessage_unexpectedException_returnsErrorMessage() {
    when(anthropicWebClient.post()).thenThrow(new RuntimeException("boom"));

    String actualResponse = anthropicService.sendMessage(USER_MESSAGE, QA_CONTEXT);

    assertEquals(
        "I apologize, but I encountered an error processing your request. Please try again later.",
        actualResponse);
  }

  private void mockWebClientChain(Mono<AnthropicResponse> monoResponse) {
    when(anthropicWebClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri("/v1/messages")).thenReturn(requestBodySpec);
    when(requestBodySpec.bodyValue(any(AnthropicRequest.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(AnthropicResponse.class)).thenReturn(monoResponse);
  }
}

