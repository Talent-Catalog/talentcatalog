/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.model.db;

import java.util.List;

import javax.persistence.AttributeConverter;

import org.tbbtalent.server.util.EnumHelper;

/**
 * Converts between List of VisaIssue enums and comma separated string of enum 
 * names which is how they are stored in the database.
 *
 * @author John Cameron
 */
public class VisaIssuesConverter 
        implements AttributeConverter<List<VisaIssue>, String> {
    @Override
    public String convertToDatabaseColumn(List<VisaIssue> visaIssues) {
        return EnumHelper.toString(visaIssues);
    }

    @Override
    public List<VisaIssue> convertToEntityAttribute(String visaIssuesString) {
        return EnumHelper.fromString(VisaIssue.class, visaIssuesString);
    }
}
