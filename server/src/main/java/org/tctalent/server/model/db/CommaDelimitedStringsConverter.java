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
import jakarta.persistence.Converter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Converts between List of Strings and single comma separated String which is
 * how they are stored in the database.
 *
 * @author John Cameron
 */
@Converter
public class CommaDelimitedStringsConverter
        implements AttributeConverter<List<String>, String> {
    private final static String DELIMITER = ",";

    @Override
    public String convertToDatabaseColumn(List<String> strings) {
        return strings == null ? null : String.join(DELIMITER, strings);
    }

    @Override
    public List<String> convertToEntityAttribute(String delimitedString) {
        return delimitedString == null || delimitedString.trim().isEmpty()
                ? null
                : Stream.of(delimitedString.split(DELIMITER))
                        .collect(Collectors.toList());
    }
}
