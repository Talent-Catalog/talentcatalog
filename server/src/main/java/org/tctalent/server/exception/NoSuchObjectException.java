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

package org.tctalent.server.exception;

public class NoSuchObjectException extends ServiceException {

    public NoSuchObjectException(Class objectClass, long id) {
        super("missing_object", "Missing " + objectClass.getSimpleName() + " with ID " + id);
    }

    public NoSuchObjectException(Class objectClass, String idStr) {
        super("missing_object", "Missing " + objectClass.getSimpleName() + " matching " + idStr);
    }

    public NoSuchObjectException(String message) {
        super("missing_object", message);
    }
}

