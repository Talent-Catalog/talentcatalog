/*
 * Copyright (c) 2024 Talent Catalog.
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

public enum NotRegisteredStatus {
    NoResponse("No response"),
    WasRegistered("No longer registered, but was registered previously"),
    NeverRegistered("Never been registered"),
    Registering("Attempted to register and pending"),
    Unsure("Unsure"),
    NA("Not applicable");

    public final String label;

    NotRegisteredStatus(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
