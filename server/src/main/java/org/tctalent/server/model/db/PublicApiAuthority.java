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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PublicApiAuthority {
    READ_CANDIDATE_DATA,
    SUBMIT_JOB_MATCHES,
    OFFER_CANDIDATE_SERVICES,
    REGISTER_CANDIDATES,
    ADMIN;

    @JsonCreator
    public static PublicApiAuthority from(String value) {
        try {
            // Allow case-insensitive matching
            return PublicApiAuthority.valueOf(value.toUpperCase());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid authority value: " + value + ". Allowed values are: " +
                String.join(", ", getAllowedValues()));
        }
    }

    private static String[] getAllowedValues() {
        PublicApiAuthority[] values = PublicApiAuthority.values();
        String[] names = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            names[i] = values[i].name();
        }
        return names;
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }

}
