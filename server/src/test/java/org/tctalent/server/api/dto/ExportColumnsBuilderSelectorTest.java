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

import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.tctalent.server.util.dto.DtoBuilder;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExportColumnsBuilderSelectorTest {

  private final ExportColumnsBuilderSelector selector = new ExportColumnsBuilderSelector();

  @Test
  void selectBuilder_buildsExportColumnShape() {
    // given
    ExportColumnStub col = new ExportColumnStub()
        .setKey("id")
        .setProperties(new PublishedDocPropsStub()
            .setHeader("ID")
            .setConstant("CONST"));

    // when
    DtoBuilder b = selector.selectBuilder();
    Map<String, Object> out = b.build(col);

    // then
    assertEquals("id", out.get("key"));

    @SuppressWarnings("unchecked")
    Map<String, Object> props = (Map<String, Object>) out.get("properties");
    assertNotNull(props);
    assertEquals("ID", props.get("header"));
    assertEquals("CONST", props.get("constant"));
  }

  @Test
  void whenPropertiesNull_omitsPropertiesKey() {
    // given
    ExportColumnStub col = new ExportColumnStub()
        .setKey("name")
        .setProperties(null); // nested object absent

    // when
    Map<String, Object> out = selector.selectBuilder().build(col);

    // then
    assertEquals("name", out.get("key"));
    assertFalse(out.containsKey("properties")); // DtoBuilder skips nulls
  }

  @Test
  void buildList_multipleColumns() {
    // given
    ExportColumnStub c1 = new ExportColumnStub()
        .setKey("id")
        .setProperties(new PublishedDocPropsStub().setHeader("ID"));

    ExportColumnStub c2 = new ExportColumnStub()
        .setKey("country")
        .setProperties(new PublishedDocPropsStub().setHeader("Country").setConstant(null)); // null constant

    // when
    List<Map<String, Object>> out = selector.selectBuilder().buildList(List.of(c1, c2));

    // then
    assertEquals(2, out.size());

    assertEquals("id", out.get(0).get("key"));
    @SuppressWarnings("unchecked")
    Map<String, Object> p1 = (Map<String, Object>) out.get(0).get("properties");
    assertEquals("ID", p1.get("header"));
    assertFalse(p1.containsKey("constant"));

    assertEquals("country", out.get(1).get("key"));
    @SuppressWarnings("unchecked")
    Map<String, Object> p2 = (Map<String, Object>) out.get(1).get("properties");
    assertEquals("Country", p2.get("header"));
    assertFalse(p2.containsKey("constant")); // null is omitted
  }

  // Minimal POJO stubs

  @Getter
  public static class ExportColumnStub {
    private String key;
    private PublishedDocPropsStub properties;

    public ExportColumnStub setKey(String key) { this.key = key; return this; }
    public ExportColumnStub setProperties(PublishedDocPropsStub properties) { this.properties = properties; return this; }
  }

  @Getter
  public static class PublishedDocPropsStub {
    private String header;
    private String constant;

    public PublishedDocPropsStub setHeader(String header) { this.header = header; return this; }
    public PublishedDocPropsStub setConstant(String constant) { this.constant = constant; return this; }
  }
}
