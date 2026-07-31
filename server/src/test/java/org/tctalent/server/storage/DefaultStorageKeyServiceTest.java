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

package org.tctalent.server.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DefaultStorageKeyServiceTest {

  private final DefaultStorageKeyService service = new DefaultStorageKeyService();

  @Test
  void newStorageKeyReturnsObjectKeyWithShardPrefixAndUuidWithoutDashes() {
    String key = service.newStorageKey();

    assertTrue(key.startsWith("o/"));
    assertFalse(key.contains("-"));
    assertTrue(key.matches("o/[0-9a-f]{2}/[0-9a-f]{2}/[0-9a-f]{32}"));

    String[] parts = key.split("/");

    assertEquals(4, parts.length);
    assertEquals("o", parts[0]);

    String firstShard = parts[1];
    String secondShard = parts[2];
    String uuid = parts[3];

    assertEquals(2, firstShard.length());
    assertEquals(2, secondShard.length());
    assertEquals(32, uuid.length());

    assertEquals(uuid.substring(0, 2), firstShard);
    assertEquals(uuid.substring(2, 4), secondShard);
  }
}