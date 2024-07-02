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
import org.tctalent.server.model.db.Language
import org.tctalent.server.model.db.Status
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest
import org.tctalent.server.repository.db.integrationhelp.getLanguage
import org.tctalent.server.repository.db.integrationhelp.getSavedLanguage

class LanguageRepositoryIntTest : BaseDBIntegrationTest() {
  @Autowired private lateinit var repo: LanguageRepository
  private lateinit var language: Language

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    language = getSavedLanguage(repo)
  }

  @Test
  fun `test find by status`() {
    val lang = repo.findByStatus(Status.active)
    assertNotNull(lang)
    assertTrue { lang.isNotEmpty() }
    val names = lang.map { it.name }
    assertTrue { names.contains(language.name) }
  }

  @Test
  fun `test find by status fail`() {
    val newLang = getLanguage().apply { status = Status.inactive }
    repo.save(newLang)
    assertTrue { newLang.id > 0 }
    val savedIndustry = repo.findByStatus(Status.active)
    assertNotNull(savedIndustry)
    assertTrue { savedIndustry.isNotEmpty() }
    val ids = savedIndustry.map { it.id }
    assertFalse { ids.contains(newLang.id) }
  }

  @Test
  fun `test find by name ignore case`() {
    val name = language.name.uppercase(Locale.getDefault())
    val i = repo.findByNameIgnoreCase(name)
    assertNotNull(i)
    assertEquals(language.name, i.name)
  }
}
