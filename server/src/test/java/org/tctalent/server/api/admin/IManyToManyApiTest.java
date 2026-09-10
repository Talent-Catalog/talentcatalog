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

package org.tctalent.server.api.admin;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tctalent.server.exception.NotImplementedException;

class IManyToManyApiTest {

  private IManyToManyApi<Object, Object> api;

  @BeforeEach
  void setup() {
    api = new IManyToManyApi<>() {};
  }

  @Test
  void listThrowsNotImplementedException() {
    NotImplementedException ex = assertThrows(
        NotImplementedException.class,
        () -> api.list(1L)
    );

    assertTrue(ex.getMessage().contains("list"));
  }

  @Test
  void mergeThrowsNotImplementedException() {
    NotImplementedException ex = assertThrows(
        NotImplementedException.class,
        () -> api.merge(1L, new Object())
    );

    assertTrue(ex.getMessage().contains("merge"));
  }

  @Test
  void removeThrowsNotImplementedException() {
    NotImplementedException ex = assertThrows(
        NotImplementedException.class,
        () -> api.remove(1L, new Object())
    );

    assertTrue(ex.getMessage().contains("remove"));
  }

  @Test
  void replaceThrowsNotImplementedException() {
    NotImplementedException ex = assertThrows(
        NotImplementedException.class,
        () -> api.replace(1L, new Object())
    );

    assertTrue(ex.getMessage().contains("replace"));
  }

  @Test
  void searchThrowsNotImplementedException() {
    NotImplementedException ex = assertThrows(
        NotImplementedException.class,
        () -> api.search(1L, new Object())
    );

    assertTrue(ex.getMessage().contains("search"));
  }

  @Test
  void searchPagedThrowsNotImplementedException() {
    NotImplementedException ex = assertThrows(
        NotImplementedException.class,
        () -> api.searchPaged(1L, new Object())
    );

    assertTrue(ex.getMessage().contains("searchPaged"));
  }

  @Test
  void fetchPublicIdsThrowsNotImplementedException() {
    NotImplementedException ex = assertThrows(
        NotImplementedException.class,
        () -> api.fetchPublicIds("public-id")
    );

    assertTrue(ex.getMessage().contains("fetchPublicIds"));
  }

  @Test
  void fetchPublicIdsPagedThrowsNotImplementedException() {
    NotImplementedException ex = assertThrows(
        NotImplementedException.class,
        () -> api.fetchPublicIdsPaged("public-id", new Object())
    );

    assertTrue(ex.getMessage().contains("fetchPublicIdsPaged"));
  }
}