/*
 * Copyright (c) 2024 Talent Catalog.
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

package org.tctalent.server.model.db.chatbot;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Represents a message in a chatbot conversation.
 * Messages are tracked by browser session ID, with question/answer pairs linked by question_id.
 */
@Entity
@Table(name = "chatbot_message")
@Data
public class ChatbotMessage {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @Column(name = "session_id", nullable = false, columnDefinition = "UUID")
    private UUID sessionId;

    @Column(name = "question_id", nullable = false, columnDefinition = "UUID")
    private UUID questionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender", nullable = false)
    private ChatbotSender sender;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "timestamp", nullable = false)
    private OffsetDateTime timestamp;

    @Type(JsonBinaryType.class)
    @Column(name = "referenced_faq_ids", columnDefinition = "jsonb")
    private List<String> referencedFaqIds;

    /**
     * Enum representing the sender of a chatbot message.
     */
    public enum ChatbotSender {
        user, bot
    }
}
