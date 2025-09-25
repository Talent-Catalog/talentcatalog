/*
 * Copyright (c) 2025 Talent Catalog.
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

package org.tctalent.server.model.db;

import com.fasterxml.jackson.databind.JsonNode;

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
    public Object get(int n, String name) {
        Object o = null;
        if (jsonNode.isArray()) {
            o = jsonNode.get(n).get(name);
        }
        return o;
    }
}
