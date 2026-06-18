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
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
/**
 * Codec (Code/Decode) for TextParts.
 * <p>
 * Provides methods for encoding and decoding TextParts objects to/from stored text.
 * <p>
 * The stored text is usually JSON but can be plain text.
 * </p>
 *
 * <p>
 *     It is static so that it can be used with HtmlSanitizer which also acts statically.
 * </p>
 *
 * @author John Cameron
 */
@Slf4j
public class TextPartsCodec {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private TextPartsCodec() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Reads the given JSON string and returns a TextParts object.
     * @param json JSON string representing a stored TextParts object.
     * @return TextParts object represented by the given JSON string or null if the string does
     * not represent a valid TextParts JSON object.
     * @throws IllegalArgumentException If the given string is not valid JSON
     * or does not represent a valid stored TextParts JSON object.
     */
    public static @NonNull TextParts readJson(@NonNull String json) {
        try {
            StoredTextParts stored = OBJECT_MAPPER.readValue(json, StoredTextParts.class);

            if (stored.getParts() != null && stored.getParts().getOriginal() != null) {
                return normalise(stored.getParts());
            }
            throw new IllegalArgumentException("Missing text parts in JSON: " + json);
        } catch (JsonProcessingException ex) {
            boolean isTextPartsJson = json.startsWith("{\"parts");
            if (isTextPartsJson) {
                //Probably a corrupted TextParts JSON string.
                log.error("Corrupted TextParts JSON string: {}", json);
                log.error("Json parsing exception", ex);
            }
            throw new IllegalArgumentException(
                "Could not parse text parts from JSON: " + json, ex);
        }
    }

    /**
     * Reads the given text and returns a TextParts object.
     * The text can be either valid JSON representing a stored TextParts object or plain text.
     * <p>
     *     If the text is not valid JSON, it is assumed to be plain text, and a TextParts object
     *     is returned with the original text set to the given value, and tidied and keywords
     *     set to null/empty.
     * </p>
     * @param value Text to be read. If null or blank, an empty TextParts object is returned.
     * @return TextParts object represented by the given text.
     */
    public static @NonNull TextParts read(@Nullable String value) {
        if (value == null || value.isBlank()) {
            return new TextParts("");
        }

        try {
            return readJson(value);
        } catch (IllegalArgumentException ex) {
            // Treat as plain text. Construct a TextParts object with the original text set to the
            // given value.
            return new TextParts(value);
        }
    }

    /**
     * Writes the given TextParts object to a JSON string.
     * @param parts Parts to be written as a JSON string.
     * @throws IllegalArgumentException If the parts object cannot be serialized to JSON.
     */
    public static String write(TextParts parts) {
        try {
            return OBJECT_MAPPER.writeValueAsString(new StoredTextParts(normalise(parts)));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not serialize text parts", e);
        }
    }

    /**
     * Normalises the given TextParts object by ensuring that all properties except tidied are
     * non-null.
     * @param parts Parts to be normalised. If null, an empty TextParts object is returned.
     * @return Normalised TextParts object.
     */
    private static TextParts normalise(@Nullable TextParts parts) {
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

        /**
         * DO NOT REMOVE null constructor. Required by Jackson.
         * <p>
         * Intellij will flag this constructor as unused, but it is used by Jackson when parsing
         * JSON. Parsing will always fail if there is no null constructor.
         */
        public StoredTextParts() {
        }

        StoredTextParts(TextParts parts) {
            this.parts = parts;
        }
    }
}
