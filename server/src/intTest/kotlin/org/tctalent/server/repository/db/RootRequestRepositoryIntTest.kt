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

package org.tctalent.server.repository.db

import kotlin.test.*
import org.springframework.beans.factory.annotation.Autowired
import org.tctalent.server.model.db.RootRequest
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest
import org.tctalent.server.repository.db.integrationhelp.getSavedRootRequest

class RootRequestRepositoryIntTest : BaseDBIntegrationTest() {
  @Autowired private lateinit var repo: RootRequestRepository
  private lateinit var rootRequest: RootRequest

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    rootRequest = getSavedRootRequest(repo)
  }

  @Test
  fun `test get most recent root request`() {
    // create another one.
    val newRootRequest = getSavedRootRequest(repo)
    val result = repo.getMostRecentRootRequest(rootRequest.ipAddress)
    assertNotNull(result)
    assertEquals(newRootRequest.ipAddress, result.ipAddress)
    assertEquals(newRootRequest.id, result.id)
  }

  @Test
  fun `test get most recent root request fail`() {
    // create another one.
    val newRootRequest = getSavedRootRequest(repo)
    val result = repo.getMostRecentRootRequest(null)
    assertNull(result)
  }
}
