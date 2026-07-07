package org.tctalent.server.service.db.verify.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
    void parse_validMock1Payload_returnsParsedPayload() {
        String raw = "{\"v\":\"mock-1\",\"unhcrId\":\"123-45C67890\",\"name\":\"Test User\"}";

        VerifyPlusPayload payload = parser.parse(raw);

        assertEquals("mock-1", payload.getVersion());
        assertEquals("123-45C67890", payload.getUnhcrId());
        assertEquals(raw, payload.getRawPayload());
    }

    @Test
    void parse_missingVersion_throwsInvalidVerifyPlusPayloadException() {
        String raw = "{\"unhcrId\":\"123-45C67890\"}";

        assertThrows(InvalidVerifyPlusPayloadException.class, () -> parser.parse(raw));
    }

    @Test
    void parse_unknownVersion_throwsInvalidVerifyPlusPayloadException() {
        String raw = "{\"v\":\"mock-2\",\"unhcrId\":\"123-45C67890\"}";

        assertThrows(InvalidVerifyPlusPayloadException.class, () -> parser.parse(raw));
    }

    @Test
    void parse_malformedJson_throwsInvalidVerifyPlusPayloadException() {
        String raw = "{\"v\":\"mock-1\",\"unhcrId\":\"123-45C67890\"";

        assertThrows(InvalidVerifyPlusPayloadException.class, () -> parser.parse(raw));
    }

    @Test
    void parse_missingUnhcrId_throwsInvalidVerifyPlusPayloadException() {
        String raw = "{\"v\":\"mock-1\"}";

        assertThrows(InvalidVerifyPlusPayloadException.class, () -> parser.parse(raw));
    }

    @Test
    void parse_preservesRawPayloadExactly() {
        String raw = "  {\"v\":\"mock-1\",\"unhcrId\":\"123-45C67890\"}  ";

        VerifyPlusPayload payload = parser.parse(raw);

        assertEquals(raw, payload.getRawPayload());
    }
}
