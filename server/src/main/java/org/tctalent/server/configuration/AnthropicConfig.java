/*
 * Copyright (c) 2025 Talent Beyond Boundaries.
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

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.tctalent.server.configuration.properties.AnthropicProperties;

/**
 * Configuration class for setting up a WebClient to interact with the Anthropic Claude API.
 */
@Configuration
@Slf4j
public class AnthropicConfig {

    private final AnthropicProperties properties;

    public AnthropicConfig(AnthropicProperties properties) {
        this.properties = properties;
    }

    /**
     * Creates and configures a WebClient to interact with the Anthropic Claude API.
     *
     * @param builder WebClient builder
     * @return the configured WebClient instance
     */
    @Bean
    public WebClient anthropicWebClient(WebClient.Builder builder) {
        log.info("Configuring Anthropic WebClient with baseUrl: {}", properties.getBaseUrl());
        
        if (properties.getApiKey() == null || properties.getApiKey().trim().isEmpty()) {
            log.error("ANTHROPIC_API_KEY is not set or is empty!");
        } else {
            String apiKey = properties.getApiKey();
            String maskedKey = apiKey.length() > 8 
                ? "***" + apiKey.substring(apiKey.length() - 4) 
                : "***";
            log.info("API Key configured (length: {}): {}", apiKey.length(), maskedKey);
        }
        
        return builder
                .baseUrl(properties.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("x-api-key", properties.getApiKey())
                .defaultHeader("anthropic-version", "2023-06-01")
                .build();
    }
}

