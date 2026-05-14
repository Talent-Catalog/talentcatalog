/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.model.db;

import jakarta.persistence.AttributeConverter;
import java.util.List;
import org.tctalent.server.util.EnumHelper;

/**
 * Converts between List of VisaIssue enums and comma separated string of enum
 * names which is how they are stored in the database.
 *
 * @author John Cameron
 */
public class LeftHomeReasonsConverter
        implements AttributeConverter<List<LeftHomeReason>, String> {
    @Override
    public String convertToDatabaseColumn(List<LeftHomeReason> leftHomeReasons) { return EnumHelper.toString(leftHomeReasons); }

    @Override
    public List<LeftHomeReason> convertToEntityAttribute(String leftHomeReasonsString) {
        return EnumHelper.fromString(LeftHomeReason.class, leftHomeReasonsString);
    }
}
