/*
 * Copyright (c) 2025 Talent Catalog.
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

package org.tctalent.server.api.dto;

import java.util.LinkedHashMap;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExportColumnsBuilderSelectorTest {

  private final ExportColumnsBuilderSelector selector = new ExportColumnsBuilderSelector();

  @Test
  void selectBuilder_buildsExportColumnShape() {
    Map<String, Object> props = new LinkedHashMap<>();
    props.put("header", "ID");
    props.put("constant", "CONST");

    Map<String, Object> col = new LinkedHashMap<>();
    col.put("key", "id");
    col.put("properties", props);

    Map<String, Object> out = selector.selectBuilder().build(col);

    assertEquals("id", out.get("key"));
    @SuppressWarnings("unchecked")
    Map<String, Object> outProps = (Map<String, Object>) out.get("properties");
    assertEquals("ID", outProps.get("header"));
    assertEquals("CONST", outProps.get("constant"));
  }

  @Test
  void whenPropertiesNull_omitsPropertiesKey() {
    Map<String, Object> col = new LinkedHashMap<>();
    col.put("key", "name");
    col.put("properties", null); // ok with mutable map

    Map<String, Object> out = selector.selectBuilder().build(col);

    assertEquals("name", out.get("key"));
    assertFalse(out.containsKey("properties")); // DtoBuilder skips nulls
  }

  @Test
  void buildList_multipleColumns() {
    Map<String, Object> c1 = new LinkedHashMap<>();
    c1.put("key", "id");
    c1.put("properties", Map.of("header", "ID"));

    Map<String, Object> c2Props = new LinkedHashMap<>();
    c2Props.put("header", "Country");
    c2Props.put("constant", null); // omitted

    Map<String, Object> c2 = new LinkedHashMap<>();
    c2.put("key", "country");
    c2.put("properties", c2Props);

    List<Map<String,Object>> out = selector.selectBuilder().buildList(List.of(c1, c2));

    assertEquals(2, out.size());
    assertEquals("id", out.get(0).get("key"));
    assertEquals("Country", ((Map<?,?>)out.get(1).get("properties")).get("header"));
    assertFalse(((Map<?,?>)out.get(1).get("properties")).containsKey("constant"));
  }

}
