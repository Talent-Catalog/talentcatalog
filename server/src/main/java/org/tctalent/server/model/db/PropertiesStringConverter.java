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

import jakarta.persistence.AttributeConverter;
import org.tctalent.server.request.candidate.PublishedDocColumnProps;

/**
 * For converting PublishedDocColumnProps to and from a string representation on the database.
 *
 * @author John Cameron
 */
public class PropertiesStringConverter
        implements AttributeConverter<PublishedDocColumnProps, String> {

    @Override
    public String convertToDatabaseColumn(PublishedDocColumnProps props) {
        String s = null;
        if (props != null) {
            s = props.toString();
            if (s.trim().length() == 0) {
                //Don't store empty strings, replace with null.
                s = null;
            }
        }
        return s;
    }

    @Override
    public PublishedDocColumnProps convertToEntityAttribute(String dbData) {
        final PublishedDocColumnProps props = new PublishedDocColumnProps(dbData);
        //If props generated from string, themselves would convert back to a null string, return
        //null props. This makes these two conversion methods consistent.
        return convertToDatabaseColumn(props) == null ? null : props;
    }
}
