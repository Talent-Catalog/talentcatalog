/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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

package org.tctalent.server.repository.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedRootRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tctalent.server.model.db.RootRequest;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;

public class RootRequestRepositoryIntTest extends BaseDBIntegrationTest {

  @Autowired
  private RootRequestRepository repo;
  private RootRequest rootRequest;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());
    rootRequest = getSavedRootRequest(repo);
  }

  @Test
  public void testGetMostRecentRootRequest() {
    // Create another one.
    RootRequest newRootRequest = getSavedRootRequest(repo);
    RootRequest result = repo.getMostRecentRootRequest(rootRequest.getIpAddress());
    assertNotNull(result);
    assertEquals(newRootRequest.getIpAddress(), result.getIpAddress());
    assertEquals(newRootRequest.getId(), result.getId());
  }

  @Test
  public void testGetMostRecentRootRequestFail() {
    // Create another one.
    getSavedRootRequest(repo);
    RootRequest result = repo.getMostRecentRootRequest(null);
    assertNull(result);
  }
}
