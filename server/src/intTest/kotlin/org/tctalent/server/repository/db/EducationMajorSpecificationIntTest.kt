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
import org.tctalent.server.model.db.EducationMajor
import org.tctalent.server.model.db.Status
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest
import org.tctalent.server.repository.db.integrationhelp.getSavedEducationMajor
import org.tctalent.server.request.education.major.SearchEducationMajorRequest

class EducationMajorSpecificationIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repo: EducationMajorRepository
  private lateinit var educationMajor: EducationMajor

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    educationMajor = getSavedEducationMajor(repo)
  }

  @Test
  fun `test keyword`() {
    val request = SearchEducationMajorRequest().apply { keyword = educationMajor.name }
    val spec = EducationMajorSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(educationMajor.id, result.first().id)
  }

  @Test
  fun `test keyword fail`() {
    val request = SearchEducationMajorRequest().apply { keyword = "NOTHING" }
    val spec = EducationMajorSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isEmpty() }
  }

  @Test
  fun `test  status`() {
    val request = SearchEducationMajorRequest().apply { status = Status.active }
    val spec = EducationMajorSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isNotEmpty())
    val ids = result.map { it.id }
    assertTrue { ids.contains(educationMajor.id) }
  }

  @Test
  fun `test status fail`() {
    val request = SearchEducationMajorRequest().apply { status = Status.deleted }
    val spec = EducationMajorSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isEmpty())
  }
}
