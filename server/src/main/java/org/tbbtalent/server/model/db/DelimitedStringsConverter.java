/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.model.db;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.AttributeConverter;

/**
 * Converts between List of Strings and single comma separated String which is 
 * how they are stored in the database.
 *
 * @author John Cameron
 */
public class DelimitedStringsConverter 
        implements AttributeConverter<List<String>, String> {
    private final static String DELIMITER = ",";
    
    @Override
    public String convertToDatabaseColumn(List<String> strings) {
        return strings == null ? null : String.join(DELIMITER, strings);
    }

    @Override
    public List<String> convertToEntityAttribute(String delimitedString) {
        return delimitedString == null || delimitedString.trim().length() == 0 
                ? null 
                : Stream.of(delimitedString.split(DELIMITER))
                        .collect(Collectors.toList());
    }
}
