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

import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.lang.Nullable;

/**
 * Used to filter out certain properties from being extracted from an Object by a {@link DtoBuilder}.
 * <p/>
 * A DtoBuilder can be configured with an instance of an object implementing this interface.
 * If so, the {@link #ignoreProperty} method is called for each property to determine whether it should be
 * ignored or not.
 *
 * @author John Cameron
 */
public interface DtoPropertyFilter {

    /**
     * Returns true if the given property of the given object should be ignored.
     * @param o Object being processed by the DtoBuilder
     * @param property Name of object property
     * @return True if property should be ignored
     */
    boolean ignoreProperty(Object o, String property);

    /**
     * Useful utility method for fetching a property from an object.
     * <p/>
     * Note that this should never throw an exception but if there is a problem accessing a property
     * (because, for example, no such property exists), it will return null.
     * @param o Object whose property we are trying to fetch.
     * @param property Name of property to fetch
     * @return Value of property. Can be null. If null it could mean that the property is present
     * but its value is null, but it could also mean that the property does not exist.
     */
    @Nullable
    default Object getProperty(Object o, String property) {
        Object value;
        try {
            value = PropertyUtils.getProperty(o, property);
        } catch (Exception e) {
            value = null;
        }
        return value;
    }

}
