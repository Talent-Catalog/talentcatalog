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

package org.tctalent.server.model.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

class JsonRowsTest {
    ObjectMapper objectMapper = new JsonMapper();
    JsonRows jsonRows;

    @BeforeEach
    void setUp() {
        String json = "[{\"name\":\"John\"},{\"name\":\"Jane\"},{\"name\":\"Jill\"}]";

        JsonNode jsonNode = objectMapper.readTree(json);

        jsonRows = new JsonRows(jsonNode);
    }

    @Test
    void nRows() {
        assertEquals(3, jsonRows.nRows());
    }

    @Test
    void get() {
        final String s = jsonRows.get(1, "name");
        assertEquals("Jane", s );

    }

    /**
     * The text of each value type is the same as Jackson 2's JsonNode.asText() returned
     * (checked against Jackson 2.21) - TC-1189.
     */
    @Test
    void getReturnsSameTextAsJackson2() {
        String json = """
            [{"text": "x", "nullValue": null, "decimal": 1.50, "bool": true, "object": {"k": 1},
              "array": [1, 2], "long": 12345678901234, "exponent": 1e3, "empty": ""}]
            """;
        JsonRows rows = new JsonRows(objectMapper.readTree(json));

        assertEquals("x", rows.get(0, "text"));
        assertEquals("null", rows.get(0, "nullValue"));
        assertEquals("1.5", rows.get(0, "decimal"));
        assertEquals("true", rows.get(0, "bool"));
        assertEquals("", rows.get(0, "object"));
        assertEquals("", rows.get(0, "array"));
        assertEquals("12345678901234", rows.get(0, "long"));
        assertEquals("1000.0", rows.get(0, "exponent"));
        assertEquals("", rows.get(0, "empty"));
        assertNull(rows.get(0, "missing"));
    }
}
