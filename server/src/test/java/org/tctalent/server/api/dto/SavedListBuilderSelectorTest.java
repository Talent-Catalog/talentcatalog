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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.util.dto.DtoBuilder;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SavedListBuilderSelectorTest {

  @Mock ExportColumnsBuilderSelector exportColumnsBuilderSelector;

  private SavedListBuilderSelector selector;

  @BeforeEach
  void setUp() {
    selector = new SavedListBuilderSelector(exportColumnsBuilderSelector);
  }

  @Test
  void selectBuilder_minimal_buildsExpectedShape() {
    // given (source as a simple map)
    Map<String, Object> sl = Map.of(
        "id", 1L,
        "publicId", "pub-1",
        "name", "My List",
        "displayedFieldsLong", "long",
        "displayedFieldsShort", "short",
        "sfJobOpp", jobOpp(101L, "SF-101"),
        "createdBy", user(11L, "Alice", "Smith"),
        // fields NOT included on MINIMAL and should be omitted
        "description", "desc should not be present on minimal",
        "status", "OPEN"
    );

    DtoBuilder b = selector.selectBuilder(DtoType.MINIMAL);

    // when
    Map<String, Object> out = b.build(sl);

    // then (present)
    assertEquals(1L, out.get("id"));
    assertEquals("pub-1", out.get("publicId"));
    assertEquals("My List", out.get("name"));
    assertEquals("long", out.get("displayedFieldsLong"));
    assertEquals("short", out.get("displayedFieldsShort"));

    @SuppressWarnings("unchecked")
    Map<String, Object> sfJobOpp = (Map<String, Object>) out.get("sfJobOpp");
    assertNotNull(sfJobOpp);
    assertEquals(101L, sfJobOpp.get("id"));
    assertEquals("SF-101", sfJobOpp.get("sfId"));

    @SuppressWarnings("unchecked")
    Map<String, Object> createdBy = (Map<String, Object>) out.get("createdBy");
    assertNotNull(createdBy);
    assertEquals(11L, createdBy.get("id"));
    assertEquals("Alice", createdBy.get("firstName"));
    assertEquals("Smith", createdBy.get("lastName"));

    // then (absent on minimal)
    assertFalse(out.containsKey("description"));
    assertFalse(out.containsKey("status"));
    assertFalse(out.containsKey("users"));
    assertFalse(out.containsKey("exportColumns"));

    // verify export columns selector not used
    verify(exportColumnsBuilderSelector, never()).selectBuilder();
  }

  @Test
  void selectBuilder_full_buildsExpectedShape() {
    // given
    // Minimal export-columns builder used by full SavedList DTO
    when(exportColumnsBuilderSelector.selectBuilder())
        .thenReturn(new DtoBuilder()
            .add("key")
            .add("properties", new DtoBuilder()
                .add("header")
                .add("constant")
            )
        );

    Map<String, Object> sl = new LinkedHashMap<>();
    sl.put("id", 2L);
    sl.put("publicId", "pub-2");
    sl.put("name", "Full List");
    sl.put("description", "A long description");
    sl.put("status", "ACTIVE");
    sl.put("fixed", true);
    sl.put("global", true);
    sl.put("displayedFieldsLong", "long");
    sl.put("displayedFieldsShort", "short");
    sl.put("sfJobOpp", jobOpp(202L, "SF-202"));
    sl.put("createdBy", user(12L, "Bob", "Jones"));
    sl.put("updatedBy", user(13L, "Carol", "Lee"));
    sl.put("users", List.of(
        user(21L, "U1", "A"),
        user(22L, "U2", "B")
    ));

    DtoBuilder b = selector.selectBuilder(null); // full

    // when
    Map<String, Object> out = b.build(sl);

    // then (a few representative keys)
    assertEquals(2L, out.get("id"));
    assertEquals("pub-2", out.get("publicId"));
    assertEquals("A long description", out.get("description"));
    assertEquals("ACTIVE", out.get("status"));
    assertEquals(true, out.get("fixed"));
    assertEquals(true, out.get("global"));
    assertEquals("long", out.get("displayedFieldsLong"));
    assertEquals("short", out.get("displayedFieldsShort"));

    // nested: sfJobOpp
    @SuppressWarnings("unchecked")
    Map<String, Object> sfJobOpp = (Map<String, Object>) out.get("sfJobOpp");
    assertNotNull(sfJobOpp);
    assertEquals(202L, sfJobOpp.get("id"));
    assertEquals("SF-202", sfJobOpp.get("sfId"));

    // nested: createdBy / updatedBy
    @SuppressWarnings("unchecked")
    Map<String, Object> createdBy = (Map<String, Object>) out.get("createdBy");
    assertEquals(12L, createdBy.get("id"));
    assertEquals("Bob", createdBy.get("firstName"));
    assertEquals("Jones", createdBy.get("lastName"));

    @SuppressWarnings("unchecked")
    Map<String, Object> updatedBy = (Map<String, Object>) out.get("updatedBy");
    assertEquals(13L, updatedBy.get("id"));
    assertEquals("Carol", updatedBy.get("firstName"));
    assertEquals("Lee", updatedBy.get("lastName"));

    // collection: users
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> users = (List<Map<String, Object>>) out.get("users");
    assertNotNull(users);
    assertEquals(2, users.size());
    assertEquals(21L, users.get(0).get("id"));
    assertEquals("U1", users.get(0).get("firstName"));
    assertEquals("A", users.get(0).get("lastName"));

    // Note: exportColumns is only populated if the source has a non-null property.
    // We didn't set it on the stub, so absence is expected.
    assertFalse(out.containsKey("exportColumns"));

    // export columns builder should be obtained once when creating the full DTO builder
    verify(exportColumnsBuilderSelector, times(1)).selectBuilder();
  }

  @Test
  void selectBuilder_noArg_returnsFullBuilder() {
    Map<String, Object> sl = Map.of(
        "id", 3L,
        "description", "present only on full"
    );

    Map<String, Object> fullOut = selector.selectBuilder().build(sl);
    Map<String, Object> minimalOut = selector.selectBuilder(DtoType.MINIMAL).build(sl);

    assertTrue(fullOut.containsKey("description"));
    assertFalse(minimalOut.containsKey("description"));
  }

  // helpers methods

  private static Map<String, Object> jobOpp(long id, String sfId) {
    return Map.of("id", id, "sfId", sfId);
  }

  private static Map<String, Object> user(long id, String first, String last) {
    return Map.of("id", id, "firstName", first, "lastName", last);
  }
}
