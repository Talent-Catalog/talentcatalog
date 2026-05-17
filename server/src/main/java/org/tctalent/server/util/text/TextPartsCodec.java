/*
 * Copyright (c) 2026 Talent Catalog.
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

package org.tctalent.server.util.text;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Codec (Code/Decode) for TextParts.
 * <p>
 * Provides methods for encoding and decoding TextParts objects to/from stored text.
 * <p>
 * The stored text is usually JSON but can be plain text.
 * </p>
 *
 * @author John Cameron
 */
@Component
@Slf4j
public class TextPartsCodec {

    private final ObjectMapper objectMapper;

    public TextPartsCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Reads the given text and returns a TextParts object.
     * @param value Text to be read.
     */
    public TextParts read(String value) {
        if (value == null || value.isBlank()) {
            return new TextParts("");
        }

        try {
            StoredTextParts stored = objectMapper.readValue(value, StoredTextParts.class);

            if (stored.getParts() != null && stored.getParts().getOriginal() != null) {
                return normalise(stored.getParts());
            }
        } catch (JsonProcessingException ex) {
            // Legacy plain text or malformed JSON.
            log.info("Failed to parse text parts from JSON: {}", value, ex);
        }

        return new TextParts(value);
    }

    /**
     * Writes the given TextParts object to a JSON string.
     * @param parts Parts to be written as a JSON string.
     */
    public String write(TextParts parts) {
        try {
            return objectMapper.writeValueAsString(new StoredTextParts(normalise(parts)));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not serialize text parts", e);
        }
    }

    private TextParts normalise(TextParts parts) {
        if (parts == null) {
            return new TextParts("");
        }

        String original = parts.getOriginal() == null ? "" : parts.getOriginal();

        List<String> keywords = parts.getKeywords() == null
            ? new ArrayList<>()
            : parts.getKeywords()
              .stream()
              .filter(keyword -> keyword != null && !keyword.isBlank())
              .toList();

        return new TextParts(original, parts.getTidied(), keywords);
    }

    /**
     * Internal class for storing TextParts in a format suitable for serialization.
     * <p>
     * Basically, the TextParts object is stored as a simple JSON object with a single
     * "parts" property.
     * <p>
     * Example:
     * <pre>
     * {
     *   "parts": {
     *     "original": "Sample text",
     *     "tidied": "sample text",
     *     "keywords": ["sample", "text"]
     *   }
     * }
     * </pre>
     */
    @Data
    private static class StoredTextParts {
        private TextParts parts;

        StoredTextParts() {
        }

        StoredTextParts(TextParts parts) {
            this.parts = parts;
        }
    }
}
