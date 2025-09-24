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

import org.springframework.lang.Nullable;
import org.tctalent.server.request.candidate.PublishedDocValueSource;

/**
 * Object which supports multiple values organized into a number of rows.
 * <p>
 * Data from within a row is identified using a {@link PublishedDocValueSource}.
 *
 * @author John Cameron
 */
public interface HasMultipleRows {

    /**
     * Number of rows
     * @return Number of rows
     */
    int nRows();

    /**
     * Gets the value specified by name from the n'th row (base 0)
     * @param n Index
     * @param name Specifies which element of the row to fetch
     *
     * @return Data - can be null if there is no such data or if the index or name is not
     * recognized.
     */
    @Nullable
    Object get(int n, String name);
}
