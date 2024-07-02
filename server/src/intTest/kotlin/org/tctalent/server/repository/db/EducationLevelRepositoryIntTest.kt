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
import org.tctalent.server.model.db.EducationLevel
import org.tctalent.server.model.db.Status
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest
import org.tctalent.server.repository.db.integrationhelp.getSavedEducationLevel

class EducationLevelRepositoryIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repo: EducationLevelRepository
  private lateinit var educationLevel: EducationLevel

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    educationLevel = getSavedEducationLevel(repo)
  }

  @Test
  fun `test find by status`() {
    val result = repo.findByStatus(Status.active)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
  }

  @Test
  fun `test find by status single`() {
    val result = repo.findByStatus(Status.deleted)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    val ids = result.map { it.id }
    assertEquals(educationLevel.id, ids.first())
  }

  @Test
  fun `test find by status single fail`() {
    repo.save(educationLevel.apply { status = Status.active })
    val result = repo.findByStatus(Status.deleted)
    assertNotNull(result)
    assertTrue { result.isEmpty() }
  }

  @Test
  fun `test find by name ignore case fail status`() {
    val result = repo.findByNameIgnoreCase(educationLevel.name)
    assertNull(result)
  }

  @Test
  fun `test find by name ignore case`() {
    repo.save(educationLevel.apply { status = Status.active })
    val result = repo.findByNameIgnoreCase(educationLevel.name)
    assertNotNull(result)
    assertEquals(educationLevel.id, result.id)
  }

  @Test
  fun `test find by name ignore case fail`() {
    val result = repo.findByNameIgnoreCase("NONE TO FIND")
    assertNull(result)
  }

  @Test
  fun `test find by level ignore case fail level`() {
    val result = repo.findByLevelIgnoreCase(9)
    assertNull(result)
  }

  @Test
  fun `test find by level ignore case fail status`() {
    val result = repo.findByLevelIgnoreCase(7)
    assertNull(result)
  }

  @Test
  fun `test find by level ignore case`() {
    repo.save(educationLevel.apply { status = Status.active })
    val result = repo.findByLevelIgnoreCase(9)
    assertNotNull(result)
    assertEquals(educationLevel.id, result.id)
  }

  @Test
  fun `test find all active`() {
    val results = repo.findAllActive()
    assertNotNull(results)
    assertTrue { results.isNotEmpty() }
  }
}
