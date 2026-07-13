/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */
package org.tctalent.server.service.db.verify.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.tctalent.server.exception.InvalidVerifyPlusPayloadException;
import org.tctalent.server.service.db.verify.VerifyPlusPayload;

class VerifyPlusPayloadParserImplTest {

    private VerifyPlusPayloadParserImpl parser;

    @BeforeEach
    void setUp() {
        parser = new VerifyPlusPayloadParserImpl(new ObjectMapper());
    }

    @Test
    @DisplayName("Given a valid mock-1 payload, when parsed, then returns a VerifyPlusPayload with correct fields")
    void parse_validMock1Payload_returnsParsedPayload() {
        String raw = "{\"v\":\"mock-1\",\"unhcrId\":\"123-45C67890\",\"name\":\"Test User\"}";

        VerifyPlusPayload payload = parser.parse(raw);

        assertEquals("mock-1", payload.getVersion());
        assertEquals("123-45C67890", payload.getUnhcrId());
        assertEquals(raw, payload.getRawPayload());
    }

    @Test
    @DisplayName("Given a payload missing the version field, when parsed, then throws InvalidVerifyPlusPayloadException")
    void parse_missingVersion_throwsInvalidVerifyPlusPayloadException() {
        String raw = "{\"unhcrId\":\"123-45C67890\"}";

        assertThrows(InvalidVerifyPlusPayloadException.class, () -> parser.parse(raw));
    }

    @Test
    @DisplayName("Given a payload with an unknown version, when parsed, then throws InvalidVerifyPlusPayloadException")
    void parse_unknownVersion_throwsInvalidVerifyPlusPayloadException() {
        String raw = "{\"v\":\"mock-2\",\"unhcrId\":\"123-45C67890\"}";

        assertThrows(InvalidVerifyPlusPayloadException.class, () -> parser.parse(raw));
    }

    @Test
    @DisplayName("Given a malformed JSON payload, when parsed, then throws InvalidVerifyPlusPayloadException")
    void parse_malformedJson_throwsInvalidVerifyPlusPayloadException() {
        String raw = "{\"v\":\"mock-1\",\"unhcrId\":\"123-45C67890\"";

        assertThrows(InvalidVerifyPlusPayloadException.class, () -> parser.parse(raw));
    }

    @Test
    @DisplayName("Given a payload missing the unhcrId field, when parsed, then throws InvalidVerifyPlusPayloadException")
    void parse_missingUnhcrId_throwsInvalidVerifyPlusPayloadException() {
        String raw = "{\"v\":\"mock-1\"}";

        assertThrows(InvalidVerifyPlusPayloadException.class, () -> parser.parse(raw));
    }

    @Test
    @DisplayName("Given a payload with extra whitespace, when parsed, then preserves the raw payload exactly")
    void parse_preservesRawPayloadExactly() {
        String raw = "  {\"v\":\"mock-1\",\"unhcrId\":\"123-45C67890\"}  ";

        VerifyPlusPayload payload = parser.parse(raw);

        assertEquals(raw, payload.getRawPayload());
    }
}
