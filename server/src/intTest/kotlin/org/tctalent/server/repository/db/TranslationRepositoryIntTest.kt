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

import org.springframework.beans.factory.annotation.Autowired
import org.tctalent.server.model.db.Translation
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest
import org.tctalent.server.repository.db.integrationhelp.getSavedTranslation
import kotlin.jvm.optionals.getOrNull
import kotlin.test.*

class TranslationRepositoryIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repo: TranslationRepository
  private lateinit var translation: Translation

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    translation = getSavedTranslation(repo)
  }

  @Test
  fun `find by type language`() {
    val t = repo.findByTypeLanguage(translation.objectType, translation.language)
    assertNotNull(t)
    assertTrue { t.isNotEmpty() }
    assertEquals(1, t.size)
    val ids = t.map { it.id }
    assertEquals(translation.id, ids.first())
  }

  @Test
  fun `find by type language fail`() {
    val t = repo.findByTypeLanguage(translation.objectType, null)
    assertNotNull(t)
    assertTrue { t.isEmpty() }
  }

  @Test
  fun `find by ids type language`() {
    val t = repo.findByIdsTypeLanguage(listOf(1), translation.objectType, translation.language)
    assertNotNull(t)
    assertTrue { t.isNotEmpty() }
    assertEquals(1, t.size)
    val ids = t.map { it.id }
    assertEquals(translation.id, ids.first())
  }

  @Test
  fun `find by ids type language fail`() {
    val t = repo.findByIdsTypeLanguage(listOf(1L), null, translation.language)
    assertNotNull(t)
    assertTrue { t.isEmpty() }
  }

  @Test
  fun `find by object id type lang`() {
    val t =
      repo
        .findByObjectIdTypeLang(translation.objectId, translation.objectType, translation.language)
        .getOrNull()
    assertNotNull(t)
    assertEquals(translation.id, t.id)
  }

  @Test
  fun `find by object id type lang fail`() {
    val t =
      repo.findByObjectIdTypeLang(translation.objectId, null, translation.language).getOrNull()
    assertNull(t)
  }

  @Test
  fun `test delete translations`() {
    repo.delete(translation)
    val t = repo.findAll()
    assertNotNull(t)
    assertTrue { t.isEmpty() }
  }
}
