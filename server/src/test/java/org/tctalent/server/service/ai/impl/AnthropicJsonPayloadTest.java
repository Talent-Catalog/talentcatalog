/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.ai.impl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.tctalent.server.util.http.RecordingHttpServer.json;

import java.io.IOException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.RegularExpressionValueMatcher;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.http.codec.autoconfigure.CodecsAutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.webclient.autoconfigure.WebClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.tctalent.server.configuration.AnthropicConfig;
import org.tctalent.server.configuration.properties.AnthropicProperties;
import org.tctalent.server.util.http.RecordingHttpServer;
import org.tctalent.server.util.http.RecordingHttpServer.RecordedRequest;

/**
 * Regression test for the JSON exchanged with the Anthropic API.
 * <p>
 * The WebClient is built as in production from Spring Boot's auto-configured WebClient.Builder
 * (whose codecs use Spring Boot's JSON mapper), and talks to a real local HTTP server. So this
 * verifies the actual JSON on the wire independently of the Jackson version.
 * </p>
 * Added before the Spring Boot 4 (Jackson 3) migration - TC-1189.
 */
@SpringBootTest(classes = {
    AnthropicConfig.class,
    AnthropicProperties.class,
    AnthropicServiceImpl.class
})
@ImportAutoConfiguration({
    ConfigurationPropertiesAutoConfiguration.class,
    JacksonAutoConfiguration.class,
    CodecsAutoConfiguration.class,
    WebClientAutoConfiguration.class
})
class AnthropicJsonPayloadTest {

    private static final RecordingHttpServer server;

    static {
        try {
            server = RecordingHttpServer.start(json(200, """
                {
                  "id": "msg_01",
                  "type": "message",
                  "role": "assistant",
                  "model": "claude-haiku-4-5-20251001",
                  "content": [
                    {"type": "text", "text": "Registration is free. [FAQ-001]"}
                  ],
                  "stop_reason": "end_turn",
                  "stop_sequence": null,
                  "usage": {"input_tokens": 120, "output_tokens": 9, "unexpected_new_field": 1}
                }
                """));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @DynamicPropertySource
    static void anthropicProperties(DynamicPropertyRegistry registry) {
        //No trailing slash, as in application.yml
        registry.add("anthropic.api.baseUrl", () -> server.baseUrl().replaceAll("/$", ""));
        registry.add("anthropic.api.apiKey", () -> "test-key");
        registry.add("anthropic.api.model", () -> "claude-haiku-4-5-20251001");
        registry.add("anthropic.api.maxTokens", () -> "20000");
        registry.add("anthropic.api.temperature", () -> "1.0");
    }

    @AfterAll
    static void stopServer() {
        server.close();
    }

    @Autowired
    private AnthropicServiceImpl anthropicService;

    @Test
    @DisplayName("message request is sent as snake_case json and the response text is decoded")
    void messageRequestAndResponseJson() throws Exception {
        String answer = anthropicService.sendMessage("Is registration free?", "[FAQ-001] Yes");

        RecordedRequest request = server.lastRequest();
        assertAll(
            () -> assertEquals("Registration is free. [FAQ-001]", answer),
            () -> assertEquals("POST", request.method()),
            () -> assertEquals("/v1/messages", request.uri()),
            () -> assertTrue(request.contentType().startsWith("application/json"),
                request.contentType())
        );

        //Strict comparison of all fields. Only the (long) system prompt is matched by pattern.
        JSONAssert.assertEquals("Actual JSON: " + request.body(), """
                {
                  "model": "claude-haiku-4-5-20251001",
                  "max_tokens": 20000,
                  "messages": [{"role": "user", "content": "Is registration free?"}],
                  "system": "x",
                  "temperature": 1.0
                }
                """, request.body(),
            new CustomComparator(JSONCompareMode.STRICT,
                new Customization("system", new RegularExpressionValueMatcher<>(
                    "(?s)You are a helpful assistant.*QA KNOWLEDGE BASE:\\n\\[FAQ-001\\] Yes"))));
    }
}
