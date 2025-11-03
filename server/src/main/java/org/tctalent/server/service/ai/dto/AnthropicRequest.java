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

package org.tctalent.server.service.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for Anthropic Claude API messages.create call.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnthropicRequest {

    @JsonProperty("model")
    private String model;

    @JsonProperty("max_tokens")
    private Integer maxTokens;

    @JsonProperty("messages")
    private List<Message> messages;

    @JsonProperty("system")
    private String system;

    @JsonProperty("temperature")
    private Double temperature;

    /**
     * Represents a message in the conversation.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        @JsonProperty("role")
        private String role;

        @JsonProperty("content")
        private String content;
    }
}

