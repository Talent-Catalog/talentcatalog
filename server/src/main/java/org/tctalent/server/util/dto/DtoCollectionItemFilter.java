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

package org.tctalent.server.util.dto;

/**
 * Used to filter out certain objects from being extracted from a Collection by a {@link DtoBuilder}.
 * <p/>
 * A DtoBuilder can be configured with an instance of an object implementing this interface.
 * If so, the {@link #ignoreItem} method is called for each object in a collection to determine whether it should be
 * ignored or not.
 *
 * @author Tim Hill
 */
public interface DtoCollectionItemFilter<T> {

    /**
     * Returns true if the given object should be ignored.
     * @param o Object being processed by the DtoBuilder
     * @return True if object should be ignored
     */
    boolean ignoreItem(T o);

}
