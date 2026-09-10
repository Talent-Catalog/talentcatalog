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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.InvalidVerifyPlusPayloadException;
import org.tctalent.server.service.db.verify.VerifyPlusPayload;
import org.tctalent.server.service.db.verify.VerifyPlusPayloadParser;

/**
 * Mock parser contract is currently provisionally mocked and versioned:
 * {"v":"mock-1","unhcrId":"..."}.
 * The raw payload string is never modified to preserve byte-exact handling.
 *
 * @author sadatmalik
 */
@Service
@RequiredArgsConstructor
public class VerifyPlusPayloadParserImpl implements VerifyPlusPayloadParser {

    private static final String MOCK_1 = "mock-1";

    private final ObjectMapper objectMapper;

    @Override
    public VerifyPlusPayload parse(String rawPayload) {
        try {
            JsonNode root = objectMapper.readTree(rawPayload);

            String version = textValue(root, "v");
            if (!MOCK_1.equals(version)) {
                throw new InvalidVerifyPlusPayloadException(
                    "Unsupported Verify+ payload version: " + version);
            }

            String unhcrId = textValue(root, "unhcrId");

            return new VerifyPlusPayload(version, rawPayload, unhcrId);
        } catch (InvalidVerifyPlusPayloadException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InvalidVerifyPlusPayloadException("Malformed Verify+ payload", ex);
        }
    }

    private String textValue(JsonNode root, String fieldName) {
        JsonNode node = root.get(fieldName);
        if (node == null || node.isNull()) {
            throw new InvalidVerifyPlusPayloadException("Missing field: " + fieldName);
        }
        if (!node.isTextual()) {
            throw new InvalidVerifyPlusPayloadException("Field must be text: " + fieldName);
        }

        String value = node.textValue();
        if (value == null || value.isEmpty()) {
            throw new InvalidVerifyPlusPayloadException("Field cannot be empty: " + fieldName);
        }
        return value;
    }
}
