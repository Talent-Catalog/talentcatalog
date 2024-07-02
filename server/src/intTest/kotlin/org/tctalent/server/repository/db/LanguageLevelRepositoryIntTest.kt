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
import org.tctalent.server.model.db.LanguageLevel
import org.tctalent.server.model.db.Status
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest
import org.tctalent.server.repository.db.integrationhelp.getLanguageLevel
import org.tctalent.server.repository.db.integrationhelp.getSavedLanguageLevel

class LanguageLevelRepositoryIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repo: LanguageLevelRepository
  private lateinit var languageLevel: LanguageLevel

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    languageLevel = getSavedLanguageLevel(repo)
  }

  @Test
  fun `test find by status`() {
    val levels = repo.findByStatus(Status.active)
    assertNotNull(levels)
    assertTrue { levels.isNotEmpty() }
    val names = levels.map { it.name }
    assertTrue { names.contains(languageLevel.name) }
  }

  @Test
  fun `test find by status fail`() {
    val newLevel = getLanguageLevel().apply { status = Status.inactive }
    repo.save(newLevel)
    assertTrue { newLevel.id > 0 }
    val savedIndustry = repo.findByStatus(Status.active)
    assertNotNull(savedIndustry)
    assertTrue { savedIndustry.isNotEmpty() }
    val ids = savedIndustry.map { it.id }
    assertFalse { ids.contains(newLevel.id) }
  }

  @Test
  fun `test find by name ignore case`() {
    val name = languageLevel.name.uppercase(Locale.getDefault())
    val i = repo.findByNameIgnoreCase(name)
    assertNotNull(i)
    assertEquals(languageLevel.name, i.name)
  }

  // TODO (this test case tests wrong code - i.e. name and code don't match)
  @Test
  fun `find by level ignore case`() {
    // The code actually finds by level and not deleted.
    val level = repo.findByLevelIgnoreCase(1)
    assertNotNull(level)
  }

  // TODO (this test case tests wrong code - i.e. name and code don't match)
  @Test
  fun `find by level ignore case fail`() {
    // The code actually finds by level and not deleted. Testing the deleted bit.
    repo.save(languageLevel.apply { status = Status.deleted })
    val level = repo.findByLevelIgnoreCase(1)
    assertNull(level)
  }

  @Test
  fun `find all active`() {
    // add a second inactive.
    repo.save(getLanguageLevel().apply { status = Status.inactive })
    val level = repo.findAllActive()
    assertNotNull(level)
    assertTrue(level.isNotEmpty())
    val ids = level.map { it.id }
    assertTrue { ids.contains(languageLevel.id) }
  }
}
