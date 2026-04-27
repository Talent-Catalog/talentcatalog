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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.reactive.function.client.WebClient;
import org.tctalent.server.configuration.properties.DuolingoProperties;

class DuolingoConfigTest {

  @Test
  void testDuolingoWebClientConfiguration() {
    DuolingoProperties properties = Mockito.mock(DuolingoProperties.class);
    Mockito.when(properties.getBaseUrl()).thenReturn("https://api.duolingo.com");
    Mockito.when(properties.getApiSecret()).thenReturn("test-secret");
    WebClient.Builder mockBuilder = Mockito.mock(WebClient.Builder.class);

    DuolingoConfig config = new DuolingoConfig(properties, mockBuilder);

    assertNotNull(config, "WebClient should not be null");
  }

  @Test
  void testAuthenticationFilter() {
    DuolingoProperties properties = Mockito.mock(DuolingoProperties.class);
    Mockito.when(properties.getBaseUrl()).thenReturn("https://api.duolingo.com");
    Mockito.when(properties.getApiSecret()).thenReturn("test-secret");

    WebClient.Builder mockBuilder = Mockito.mock(WebClient.Builder.class);
    WebClient mockWebClient = Mockito.mock(WebClient.class);
    Mockito.when(mockBuilder.baseUrl(Mockito.anyString())).thenReturn(mockBuilder);
    Mockito.when(mockBuilder.filter(Mockito.any())).thenReturn(mockBuilder);
    Mockito.when(mockBuilder.build()).thenReturn(mockWebClient);

    DuolingoConfig config = new DuolingoConfig(properties, mockBuilder);
    WebClient webClient = config.duolingoWebClient(mockBuilder);

    assertNotNull(webClient, "WebClient should not be null");

    // Verify interactions
    Mockito.verify(mockBuilder).baseUrl("https://api.duolingo.com");
    Mockito.verify(mockBuilder).filter(Mockito.any());
    Mockito.verify(mockBuilder).build();
  }


}
