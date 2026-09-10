/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or any later version.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.Test;

class VisaIssuesConverterTest {

  private final VisaIssuesConverter converter = new VisaIssuesConverter();

  @Test
  void convertToDatabaseColumnConvertsVisaIssuesToCommaSeparatedEnumNames() {
    List<VisaIssue> visaIssues = List.of(
        VisaIssue.Health,
        VisaIssue.Military,
        VisaIssue.VisaRejections
    );

    assertEquals(
        "Health,Military,VisaRejections",
        converter.convertToDatabaseColumn(visaIssues)
    );
  }

  @Test
  void convertToDatabaseColumnReturnsNullWhenVisaIssuesAreNull() {
    assertNull(converter.convertToDatabaseColumn(null));
  }

  @Test
  void convertToEntityAttributeConvertsCommaSeparatedEnumNamesToVisaIssues() {
    List<VisaIssue> result = converter.convertToEntityAttribute(
        "Health,Military,VisaRejections"
    );

    assertEquals(
        List.of(VisaIssue.Health, VisaIssue.Military, VisaIssue.VisaRejections),
        result
    );
  }

  @Test
  void convertToEntityAttributeReturnsNullWhenDatabaseValueIsNull() {
    assertNull(converter.convertToEntityAttribute(null));
  }
}