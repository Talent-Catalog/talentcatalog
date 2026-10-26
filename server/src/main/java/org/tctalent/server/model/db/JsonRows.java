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

import org.springframework.lang.Nullable;
import tools.jackson.databind.JsonNode;

/**
 * Json implementation of HasMultipleRows.
 * <p>
 * Accepts an array of Json objects.
 *
 * @author John Cameron
 */
public class JsonRows implements HasMultipleRows {
    private final JsonNode jsonNode;

    public JsonRows(JsonNode jsonNode) {
        this.jsonNode = jsonNode;
    }

    @Override
    public int nRows() {
        int n = 0;
        if (jsonNode.isArray()) {
            n = jsonNode.size();
        }
        return n;
    }

    @Override
    public String get(int n, @Nullable String name) {
        String s = null;
        if (name != null && jsonNode.isArray()) {
            final JsonNode element = jsonNode.get(n);
            if (element != null) {
                JsonNode node = element.get(name);
                if (node != null) {
                    s = asText(node);
                }
            }
        }
        return s;
    }

    /**
     * Returns the same text as Jackson 2's JsonNode.asText() did, which differs from Jackson 3's
     * asString() for nulls ("null" rather than "") and for objects and arrays ("" rather than an
     * exception) - TC-1189.
     */
    private static String asText(JsonNode node) {
        if (node.isNull()) {
            return "null";
        }
        if (node.isContainer()) {
            return "";
        }
        return node.asString();
    }
}
