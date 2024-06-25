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
import org.tctalent.server.model.db.Occupation
import org.tctalent.server.model.db.Status
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest
import org.tctalent.server.repository.db.integrationhelp.getSavedOccupation
import org.tctalent.server.request.occupation.SearchOccupationRequest

class OccupationSpecificationIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repo: OccupationRepository
  private lateinit var occupation: Occupation

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    occupation = getSavedOccupation(repo)
  }

  @Test
  fun `test keyword`() {
    val request = SearchOccupationRequest().apply { keyword = occupation.name }
    val spec = OccupationSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(occupation.id, result.first().id)
  }

  @Test
  fun `test keyword fail`() {
    val request = SearchOccupationRequest().apply { keyword = "NOTHING" }
    val spec = OccupationSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isEmpty() }
  }

  @Test
  fun `test build search query with status`() {
    val request = SearchOccupationRequest().apply { status = Status.active }
    val spec = OccupationSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isNotEmpty())
    val ids = result.map { it.id }
    assertTrue { ids.contains(occupation.id) }
  }

  @Test
  fun `test build search query with status fail`() {
    val request = SearchOccupationRequest().apply { status = Status.deleted }
    val spec = OccupationSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isEmpty())
  }
}
