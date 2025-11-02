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

package org.tctalent.server.configuration.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for the Anthropic Claude API integration.
 *
 * <p>This class contains the properties used to interact with the Anthropic Claude API,
 * including the API key, base URL, model, and request parameters. They are injected from
 * the application's configuration files, {@code application.yml}, using the prefix "anthropic.api".
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "anthropic.api")
public class AnthropicProperties {

    /**
     * The API key for authentication with the Anthropic Claude API.
     */
    private String apiKey;

    /**
     * The base URL for Anthropic API requests.
     */
    private String baseUrl;

    /**
     * The Claude model to use for generating responses.
     */
    private String model = "claude-haiku-4-5-20251001";

    /**
     * Maximum number of tokens to generate in the response.
     */
    private Integer maxTokens = 20000;

    /**
     * Temperature setting for response generation (0.0 to 2.0).
     */
    private Double temperature = 1.0;
}

