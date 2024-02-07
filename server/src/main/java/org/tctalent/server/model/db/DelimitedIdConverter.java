/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

import javax.persistence.AttributeConverter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Converts between List of Strings and single comma separated String which is
 * how they are stored in the database.
 *
 * @author John Cameron
 */
public class DelimitedIdConverter
        implements AttributeConverter<List<Long>, String> {
    private final static String DELIMITER = ",";

    @Override
    public String convertToDatabaseColumn(List<Long> ids) {
        return ids == null ? null : ids.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(DELIMITER));
    }

    @Override
    public List<Long> convertToEntityAttribute(String delimitedString) {
        return delimitedString == null || delimitedString.trim().length() == 0
                ? null
                : Stream.of(delimitedString.split(DELIMITER))
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }
}
