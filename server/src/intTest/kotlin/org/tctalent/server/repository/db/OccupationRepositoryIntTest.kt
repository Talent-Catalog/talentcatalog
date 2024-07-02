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
import org.tctalent.server.model.db.Occupation
import org.tctalent.server.model.db.Status
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest
import org.tctalent.server.repository.db.integrationhelp.getOccupation
import org.tctalent.server.repository.db.integrationhelp.getSavedOccupation

class OccupationRepositoryIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repo: OccupationRepository
  private lateinit var occupation: Occupation

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    occupation = getSavedOccupation(repo)
  }

  @Test
  fun `test find by status`() {
    val levels = repo.findByStatus(Status.active)
    assertNotNull(levels)
    assertTrue { levels.isNotEmpty() }
    val names = levels.map { it.name }
    assertTrue { names.contains(occupation.name) }
  }

  @Test
  fun `test find by status fail`() {
    val newOcc = getOccupation().apply { status = Status.inactive }
    repo.save(newOcc)
    assertTrue { newOcc.id > 0 }
    val savedIndustry = repo.findByStatus(Status.active)
    assertNotNull(savedIndustry)
    assertTrue { savedIndustry.isNotEmpty() }
    val ids = savedIndustry.map { it.id }
    assertFalse { ids.contains(newOcc.id) }
  }

  @Test
  fun `test find by name ignore case`() {
    val name = occupation.name.uppercase(Locale.getDefault())
    val i = repo.findByNameIgnoreCase(name)
    assertNotNull(i)
    assertEquals(occupation.name, i.name)
  }

  @Test
  fun `test get names for ids`() {
    val occ1 = getOccupation()
    repo.save(occ1)
    val occ2 = getOccupation()
    repo.save(occ2)
    val names = repo.getNamesForIds(listOf(occ1.id, occupation.id, occ2.id))
    assertEquals(3, names.size)
    assertTrue { names.all { it.startsWith("TEST_OCCUPATION") } }
  }
}
