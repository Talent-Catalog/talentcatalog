/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.model.db;

import org.springframework.util.CollectionUtils;

import javax.persistence.AttributeConverter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Converts between List of VisaIssue enums and comma separated string of enum 
 * names which is how they are stored in the database.
 *
 * @author John Cameron
 */
public class ModelConverter
        implements AttributeConverter<List<Long>, String> {
    @Override
    public String convertToDatabaseColumn(List<Long> ids) {
        return !CollectionUtils.isEmpty(ids) ? ids.stream().map(String::valueOf)
                .collect(Collectors.joining(",")) : null;
    }

    @Override
    public List<Long> convertToEntityAttribute(String idString) {
        return idString != null ? Stream.of(idString.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList()) : null;
    }
}
