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

package org.tctalent.server.configuration;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.tctalent.server.configuration.properties.AnthropicProperties;

class AnthropicConfigTest {

  private AnthropicProperties properties;
  private WebClient.Builder builder;
  private WebClient webClient;

  @BeforeEach
  void setUp() {
    properties = mock(AnthropicProperties.class);
    builder = mock(WebClient.Builder.class);
    webClient = mock(WebClient.class);

    when(builder.baseUrl(any())).thenReturn(builder);
    when(builder.defaultHeader(any(), any())).thenReturn(builder);
    when(builder.build()).thenReturn(webClient);
  }

  @Test
  void anthropicWebClient_configuresHeadersAndBaseUrl() {
    when(properties.getBaseUrl()).thenReturn("https://api.anthropic.com");
    when(properties.getApiKey()).thenReturn("secret");

    AnthropicConfig config = new AnthropicConfig(properties);
    WebClient result = config.anthropicWebClient(builder);

    assertSame(webClient, result);
    verify(builder).baseUrl("https://api.anthropic.com");
    verify(builder).defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    verify(builder).defaultHeader("x-api-key", "secret");
    verify(builder).defaultHeader("anthropic-version", "2023-06-01");
    verify(builder).build();
  }

  @Test
  void anthropicWebClient_handlesMissingApiKey() {
    when(properties.getBaseUrl()).thenReturn("https://api.anthropic.com");
    when(properties.getApiKey()).thenReturn("   ");

    AnthropicConfig config = new AnthropicConfig(properties);
    config.anthropicWebClient(builder);

    verify(builder).defaultHeader("x-api-key", "   ");
  }
}

