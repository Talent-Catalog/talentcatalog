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

package org.tctalent.server.api.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tctalent.server.exception.*;

import static org.junit.jupiter.api.Assertions.*;

class ITableApiTest {

  private ITableApi<Object, Object, Object> api;

  @BeforeEach
  void setup() {
    // Anonymous implementation to invoke default methods
    api = new ITableApi<>() {};
  }

  @Test
  void testCreate_throwsNotImplementedException() {
    NotImplementedException ex = assertThrows(
        NotImplementedException.class,
        () -> api.create(new Object())
    );
    assertTrue(ex.getMessage().contains("create"));
  }

  @Test
  void testDelete_throwsNotImplementedException() {
    NotImplementedException ex = assertThrows(
        NotImplementedException.class,
        () -> api.delete(1L)
    );
    assertTrue(ex.getMessage().contains("delete"));
  }


  @Test
  void testList_throwsNotImplementedException() {
    NotImplementedException ex = assertThrows(
        NotImplementedException.class,
        api::list
    );
    assertTrue(ex.getMessage().contains("list"));
  }

  @Test
  void testSearch_throwsNotImplementedException() {
    NotImplementedException ex = assertThrows(
        NotImplementedException.class,
        () -> api.search(new Object())
    );
    assertTrue(ex.getMessage().contains("search"));
  }

  @Test
  void testSearchPaged_throwsNotImplementedException() {
    NotImplementedException ex = assertThrows(
        NotImplementedException.class,
        () -> api.searchPaged(new Object())
    );
    assertTrue(ex.getMessage().contains("searchPaged"));
  }

  @Test
  void testUpdate_throwsNotImplementedException() {
    NotImplementedException ex = assertThrows(
        NotImplementedException.class,
        () -> api.update(1L, new Object())
    );
    assertTrue(ex.getMessage().contains("update"));
  }
}
