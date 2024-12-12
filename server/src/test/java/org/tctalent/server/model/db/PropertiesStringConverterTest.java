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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tctalent.server.request.candidate.PublishedDocColumnProps;

class PropertiesStringConverterTest {
  PropertiesStringConverter converter;

  @BeforeEach
  void setUp() {
    converter = new PropertiesStringConverter();
  }

  @Test
  void convertToDatabaseColumn() {
    PublishedDocColumnProps props;
    String s;

    s = converter.convertToDatabaseColumn(null);
    assertNull(s);

    props = new PublishedDocColumnProps();
    s = converter.convertToDatabaseColumn(props);
    assertNull(s);

    props.setConstant("");
    s = converter.convertToDatabaseColumn(props);
    assertNotNull(s);
    assertEquals("constant=", s);

    props.setConstant("val");
    s = converter.convertToDatabaseColumn(props);
    assertNotNull(s);
    assertEquals("constant=val", s);

    props.setHeader("head");
    s = converter.convertToDatabaseColumn(props);
    assertNotNull(s);
    assertEquals("header=head\tconstant=val", s);

    props.setConstant(null);
    s = converter.convertToDatabaseColumn(props);
    assertNotNull(s);
    assertEquals("header=head", s);

    props.setHeader(null);
    s = converter.convertToDatabaseColumn(props);
    assertNull(s);
  }

  @Test
  void convertToEntityAttribute() {
    PublishedDocColumnProps props;

    props = converter.convertToEntityAttribute(null);
    assertNull(props);

    props = converter.convertToEntityAttribute("  ");
    assertNull(props);

    props = converter.convertToEntityAttribute("  sghsgshg");
    assertNull(props);

    props = converter.convertToEntityAttribute("constant=");
    assertNotNull(props);
    assertEquals("", props.getConstant());
    assertNull(props.getHeader());

    props = converter.convertToEntityAttribute("constant=val");
    assertNotNull(props);
    assertEquals("val", props.getConstant());
    assertNull(props.getHeader());

    props = converter.convertToEntityAttribute("header=head");
    assertNotNull(props);
    assertEquals("head", props.getHeader());
    assertNull(props.getConstant());

    props = converter.convertToEntityAttribute("constant=val\theader=head");
    assertNotNull(props);
    assertEquals("head", props.getHeader());
    assertEquals("val", props.getConstant());

    props = converter.convertToEntityAttribute("constant=\theader=");
    assertNotNull(props);
    assertEquals("", props.getHeader());
    assertEquals("", props.getConstant());


  }
}
