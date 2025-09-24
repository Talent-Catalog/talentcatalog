// Copyright 2009 Cameron Edge Pty Ltd. All rights reserved.
// Reproduction in whole or in part in any form or medium without express
// written permission of Cameron Edge Pty Ltd is strictly prohibited.

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
