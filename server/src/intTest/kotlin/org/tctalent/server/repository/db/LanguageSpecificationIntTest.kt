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
import org.tctalent.server.model.db.Language
import org.tctalent.server.model.db.Status
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest
import org.tctalent.server.repository.db.integrationhelp.getSavedLanguage
import org.tctalent.server.request.language.SearchLanguageRequest

class LanguageSpecificationIntTest : BaseDBIntegrationTest() {
  @Autowired private lateinit var repo: LanguageRepository
  private lateinit var testLanguage: Language

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    testLanguage = getSavedLanguage(repo)
  }

  @Test
  fun `test keyword`() {
    val request = SearchLanguageRequest().apply { keyword = testLanguage.name }
    val spec = LanguageSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(testLanguage.id, result.first().id)
  }

  @Test
  fun `test keyword fail`() {
    val request = SearchLanguageRequest().apply { keyword = "NOTHING" }
    val spec = LanguageSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isEmpty() }
  }

  @Test
  fun `test build search query with status`() {
    val request = SearchLanguageRequest().apply { status = Status.active }
    val spec = LanguageSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isNotEmpty())
    val ids = result.map { it.id }
    assertTrue { ids.contains(testLanguage.id) }
  }

  @Test
  fun `test build search query with status fail`() {
    val request = SearchLanguageRequest().apply { status = Status.deleted }
    val spec = LanguageSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isEmpty())
  }
}
