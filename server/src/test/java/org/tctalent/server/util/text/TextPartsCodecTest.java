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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.tctalent.server.util.html.HtmlSanitizer;

class TextPartsCodecTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void readsNullAsEmptyOriginalText() {
        TextParts parts = TextPartsCodec.read(null);

        assertEquals("", parts.getOriginal());
        assertNull(parts.getTidied());
        assertTrue(parts.getKeywords().isEmpty());
    }

    @Test
    void readsBlankAsEmptyOriginalText() {
        TextParts parts = TextPartsCodec.read("   ");

        assertEquals("", parts.getOriginal());
        assertNull(parts.getTidied());
        assertTrue(parts.getKeywords().isEmpty());
    }

    @Test
    void readsLegacyPlainTextAsOriginal() {
        TextParts parts = TextPartsCodec.read("I work electrician 5 years");

        assertEquals("I work electrician 5 years", parts.getOriginal());
        assertNull(parts.getTidied());
        assertTrue(parts.getKeywords().isEmpty());
    }

    @Test
    void readsLegacyHtmlAsOriginal() {
        TextParts parts = TextPartsCodec.read("<p>Line 1</p><div>Line 2</div>");

        assertEquals("<p>Line 1</p><div>Line 2</div>", parts.getOriginal());
        assertNull(parts.getTidied());
        assertTrue(parts.getKeywords().isEmpty());
    }

    @Test
    void readsJsonTextParts() {
        String stored = """
            {
              "parts": {
                "original": "i work electrician",
                "tidied": "I worked as an electrician.",
                "keywords": ["electrician", "wiring"]
              }
            }
            """;

        TextParts parts = TextPartsCodec.read(stored);

        assertEquals("i work electrician", parts.getOriginal());
        assertEquals("I worked as an electrician.", parts.getTidied());
        assertEquals(List.of("electrician", "wiring"), parts.getKeywords());
    }

    @Test
    void readsJsonTextPartsContainingHtml() {
        String stored = """
            {
              "parts": {
                "original": "<p>Line 1</p><div>Line 2</div>",
                "tidied": "<p>Line 1</p><p>Line 2</p>",
                "keywords": ["html", "line"]
              }
            }
            """;

        TextParts parts = TextPartsCodec.read(stored);

        assertEquals("<p>Line 1</p><div>Line 2</div>", parts.getOriginal());
        assertEquals("<p>Line 1</p><p>Line 2</p>", parts.getTidied());
        assertEquals(List.of("html", "line"), parts.getKeywords());
    }

    @Test
    void survivesJsonEncodedHtmlSanitization() {
        //This is the html that we want stored in json.
        String html = """
        <ul type="circle"><li>John</li></ul>""";

        //When the html is stored in the database as part of a JSON string, it will need the quotes
        //around the type attribute to be escaped. So it will be stored as:
        String htmlStored = "<ul type=\\\"circle\\\"><li>John</li></ul>";

        //So the full json stored in the database will be:
        String s = """
            {"parts":{"original":\""""
            + htmlStored + "\"}}";

        String sanitized = HtmlSanitizer.sanitize(s);

        TextParts parts = TextPartsCodec.read(sanitized);

        assertEquals(html, parts.getOriginal());
        assertNull(parts.getTidied());
        assertEquals(List.of(), parts.getKeywords());
    }

    @Test
    void survivesNonJsonEncodedHtmlSanitization() {
        //For html that is stored in the database just as itself, not as part of a JSON
        //string, it won't need any escaping of the quotes around the type attribute.
        //So it will just be stored as:
        String html = """
        <ul type="circle"><li>John</li></ul>""";

        String sanitized = HtmlSanitizer.sanitize(html);

        TextParts parts = TextPartsCodec.read(sanitized);

        //The original html is encoded into the original text part,
        // and the tidied and keywords are left null/empty.
        assertEquals(html, parts.getOriginal());
        assertNull(parts.getTidied());
        assertEquals(List.of(), parts.getKeywords());
    }

    @Test
    void readsJsonTextPartsWithMissingTidied() {
        String stored = """
            {
              "parts": {
                "original": "candidate text",
                "keywords": ["hospitality"]
              }
            }
            """;

        TextParts parts = TextPartsCodec.read(stored);

        assertEquals("candidate text", parts.getOriginal());
        assertNull(parts.getTidied());
        assertEquals(List.of("hospitality"), parts.getKeywords());
    }

    @Test
    void readsJsonTextPartsWithMissingKeywordsAsEmptyList() {
        String stored = """
            {
              "parts": {
                "original": "candidate text",
                "tidied": "Candidate text."
              }
            }
            """;

        TextParts parts = TextPartsCodec.read(stored);

        assertEquals("candidate text", parts.getOriginal());
        assertEquals("Candidate text.", parts.getTidied());
        assertTrue(parts.getKeywords().isEmpty());
    }

    @Test
    void readsJsonTextPartsWithNullKeywordsAsEmptyList() {
        String stored = """
            {
              "parts": {
                "original": "candidate text",
                "keywords": null
              }
            }
            """;

        TextParts parts = TextPartsCodec.read(stored);

        assertEquals("candidate text", parts.getOriginal());
        assertTrue(parts.getKeywords().isEmpty());
    }

    @Test
    void readsJsonTextPartsWithNullOriginalAsLegacyText() {
        String stored = """
            {
              "parts": {
                "original": null,
                "tidied": "Tidied text",
                "keywords": ["skill"]
              }
            }
            """;

        TextParts parts = TextPartsCodec.read(stored);

        assertEquals(stored, parts.getOriginal());
        assertNull(parts.getTidied());
        assertTrue(parts.getKeywords().isEmpty());
    }

    @Test
    void fallsBackToLegacyTextIfJsonIsNotTextParts() {
        String stored = "{\"hello\":\"world\"}";

        TextParts parts = TextPartsCodec.read(stored);

        assertEquals(stored, parts.getOriginal());
        assertNull(parts.getTidied());
        assertTrue(parts.getKeywords().isEmpty());
    }

    @Test
    void treatsMalformedJsonAsLegacyOriginalText() {
        String malformedJson = "{\"parts\":{\"original\":\"hello\nworld\"}}";

        TextParts parts = TextPartsCodec.read(malformedJson);

        assertEquals(malformedJson, parts.getOriginal());
        assertNull(parts.getTidied());
        assertTrue(parts.getKeywords().isEmpty());
    }

    @Test
    void writesTextPartsAsJson() throws Exception {
        TextParts parts = new TextParts(
            "i work electrician",
            "I worked as an electrician.",
            List.of("electrician", "wiring")
        );

        String stored = TextPartsCodec.write(parts);
        JsonNode json = objectMapper.readTree(stored);

        assertEquals("i work electrician", json.at("/parts/original").asText());
        assertEquals("I worked as an electrician.", json.at("/parts/tidied").asText());
        assertEquals("electrician", json.at("/parts/keywords/0").asText());
        assertEquals("wiring", json.at("/parts/keywords/1").asText());
    }

    @Test
    void writesHtmlSafelyAsJsonString() throws Exception {
        TextParts parts = new TextParts(
            "Line 1<div>Line 2</div>",
            "<p>Line 1</p><p>Line 2</p>",
            List.of("html")
        );

        String stored = TextPartsCodec.write(parts);
        JsonNode json = objectMapper.readTree(stored);

        assertEquals("Line 1<div>Line 2</div>", json.at("/parts/original").asText());
        assertEquals("<p>Line 1</p><p>Line 2</p>", json.at("/parts/tidied").asText());
    }

    @Test
    void writesNewlinesAsValidJsonEscapes() throws Exception {
        TextParts parts = new TextParts(
            "Line 1\nLine 2",
            "Tidied line 1\nTidied line 2",
            List.of()
        );

        String stored = TextPartsCodec.write(parts);
        JsonNode json = objectMapper.readTree(stored);

        assertEquals("Line 1\nLine 2", json.at("/parts/original").asText());
        assertEquals("Tidied line 1\nTidied line 2", json.at("/parts/tidied").asText());
    }

    @Test
    void writesNullPartsAsEmptyOriginal() throws Exception {
        String stored = TextPartsCodec.write(null);
        JsonNode json = objectMapper.readTree(stored);

        assertEquals("", json.at("/parts/original").asText());
        assertTrue(json.at("/parts/keywords").isArray());
        assertEquals(0, json.at("/parts/keywords").size());
    }

    @Test
    void writesNullOriginalAsEmptyString() throws Exception {
        TextParts parts = new TextParts(null, null, List.of("skill"));

        String stored = TextPartsCodec.write(parts);
        JsonNode json = objectMapper.readTree(stored);

        assertEquals("", json.at("/parts/original").asText());
        assertEquals("skill", json.at("/parts/keywords/0").asText());
    }

    @Test
    void writesNullKeywordsAsEmptyArray() throws Exception {
        TextParts parts = new TextParts("text", null, null);

        String stored = TextPartsCodec.write(parts);
        JsonNode json = objectMapper.readTree(stored);

        assertEquals("text", json.at("/parts/original").asText());
        assertTrue(json.at("/parts/keywords").isArray());
        assertEquals(0, json.at("/parts/keywords").size());
    }

    @Test
    void removesNullAndBlankKeywordsWhenWriting() throws Exception {
        TextParts parts = new TextParts(
            "text",
            null,
            new ArrayList<>(Arrays.asList("electrician", null, "", "   ", "wiring"))
        );

        String stored = TextPartsCodec.write(parts);
        JsonNode json = objectMapper.readTree(stored);

        assertEquals(2, json.at("/parts/keywords").size());
        assertEquals("electrician", json.at("/parts/keywords/0").asText());
        assertEquals("wiring", json.at("/parts/keywords/1").asText());
    }

    @Test
    void roundTripsTextParts() {
        TextParts original = new TextParts(
            "<p>candidate html</p>",
            "<p>tidied html</p>",
            List.of("candidate", "html")
        );

        TextParts result = TextPartsCodec.read(TextPartsCodec.write(original));

        assertEquals(original.getOriginal(), result.getOriginal());
        assertEquals(original.getTidied(), result.getTidied());
        assertEquals(original.getKeywords(), result.getKeywords());
    }
}
