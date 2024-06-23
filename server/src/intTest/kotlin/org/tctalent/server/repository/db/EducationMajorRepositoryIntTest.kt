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

import java.util.*
import kotlin.test.*
import org.springframework.beans.factory.annotation.Autowired
import org.tctalent.server.model.db.EducationMajor
import org.tctalent.server.model.db.Status
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest
import org.tctalent.server.repository.db.integrationhelp.getEducationMajor
import org.tctalent.server.repository.db.integrationhelp.getSavedEducationMajor

class EducationMajorRepositoryIntTest : BaseDBIntegrationTest() {
  @Autowired private lateinit var repo: EducationMajorRepository
  private lateinit var educationMajor: EducationMajor

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    educationMajor = getSavedEducationMajor(repo)
  }

  @Test
  fun `test find by status`() {
    val levels = repo.findByStatus(Status.active)
    assertNotNull(levels)
    assertTrue { levels.isNotEmpty() }
    val names = levels.map { it.name }
    assertTrue { names.contains(educationMajor.name) }
  }

  @Test
  fun `test find by status fail`() {
    val em = getEducationMajor().apply { status = Status.inactive }
    repo.save(em)
    assertTrue { em.id > 0 }
    val savedIndustry = repo.findByStatus(Status.active)
    assertNotNull(savedIndustry)
    assertTrue { savedIndustry.isNotEmpty() }
    val ids = savedIndustry.map { it.id }
    assertFalse { ids.contains(em.id) }
  }

  @Test
  fun `test find by name ignore case`() {
    val name = educationMajor.name.uppercase(Locale.getDefault())
    val i = repo.findByNameIgnoreCase(name)
    assertNotNull(i)
    assertEquals(educationMajor.name, i.name)
  }

  @Test
  fun `test get names for ids`() {
    val em = getEducationMajor()
    repo.save(em)
    val em2 = getEducationMajor()
    repo.save(em2)
    val names = repo.getNamesForIds(listOf(em.id, educationMajor.id, em2.id))
    assertEquals(3, names.size)
    assertTrue { names.all { it.startsWith("TEST_EDUCATION") } }
  }
}
