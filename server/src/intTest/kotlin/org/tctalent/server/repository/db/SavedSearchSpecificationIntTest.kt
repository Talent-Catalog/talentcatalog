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
import kotlin.test.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.tctalent.server.model.db.SavedSearch
import org.tctalent.server.model.db.SavedSearchType
import org.tctalent.server.model.db.User
import org.tctalent.server.repository.db.integrationhelp.*
import org.tctalent.server.request.search.SearchSavedSearchRequest

/**
 * In tests designed to check failure, set default = true, else a result will always be returned.
 */
class SavedSearchSpecificationIntTest : BaseDBIntegrationTest() {
  @Autowired private lateinit var repo: SavedSearchRepository
  @Autowired private lateinit var userRepository: UserRepository
  private lateinit var savedSearch: SavedSearch
  private lateinit var loggedInUser: User

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    savedSearch = repo.save(getSavedSearch().apply { defaultSearch = false })
    loggedInUser = getSavedUser(userRepository)
  }

  @Test
  fun `test keyword`() {
    val request = SearchSavedSearchRequest().apply { keyword = "Test" }
    val spec = SavedSearchSpecification.buildSearchQuery(request, loggedInUser)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isNotEmpty())
    assertEquals(1, result.size)
    assertEquals(savedSearch.id, result.first().id)
  }

  @Test
  fun `test status fail`() {
    val request = SearchSavedSearchRequest().apply { keyword = "NOTHING" }
    val spec = SavedSearchSpecification.buildSearchQuery(request, loggedInUser)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isEmpty())
  }

  @Test
  fun `test default fail`() {
    repo.save(savedSearch.apply { defaultSearch = true })
    val request = SearchSavedSearchRequest().apply { keyword = "Test" }
    val spec = SavedSearchSpecification.buildSearchQuery(request, loggedInUser)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isEmpty())
  }

  @Test
  fun `test search type`() {
    repo.save(savedSearch.apply { type = SavedSearchType.job.name })
    val request = SearchSavedSearchRequest().apply { savedSearchType = SavedSearchType.job }
    val spec = SavedSearchSpecification.buildSearchQuery(request, loggedInUser)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isNotEmpty())
    assertEquals(1, result.size)
    assertEquals(savedSearch.id, result.first().id)
  }

  @Test
  fun `test search type fail`() {
    repo.save(savedSearch.apply { type = SavedSearchType.job.name })
    val request = SearchSavedSearchRequest().apply { savedSearchType = SavedSearchType.profession }
    val spec = SavedSearchSpecification.buildSearchQuery(request, loggedInUser)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isEmpty())
  }

  @Test
  fun `test fixed`() {
    repo.save(savedSearch.apply { fixed = true })
    val request = SearchSavedSearchRequest().apply { fixed = true }
    val spec = SavedSearchSpecification.buildSearchQuery(request, loggedInUser)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isNotEmpty())
    assertEquals(1, result.size)
    assertEquals(savedSearch.id, result.first().id)
  }

  @Test
  fun `test fixed false`() {
    repo.save(savedSearch.apply { defaultSearch = true })
    val request = SearchSavedSearchRequest().apply { fixed = false }
    val spec = SavedSearchSpecification.buildSearchQuery(request, loggedInUser)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isEmpty())
  }

  @Test
  fun `test global`() {
    repo.save(savedSearch.apply { global = true })
    val request = SearchSavedSearchRequest().apply { global = true }
    val spec = SavedSearchSpecification.buildSearchQuery(request, loggedInUser)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isNotEmpty())
    assertEquals(1, result.size)
    assertEquals(savedSearch.id, result.first().id)
  }

  @Test
  fun `test global false`() {
    val request =
      SearchSavedSearchRequest().apply {
        keyword = "NOTHING"
        global = false
      }
    val spec = SavedSearchSpecification.buildSearchQuery(request, loggedInUser)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isEmpty())
  }

  @Test
  fun `test owned`() {
    repo.save(savedSearch.apply { createdBy = loggedInUser })
    val request = SearchSavedSearchRequest().apply { owned = true }
    val spec = SavedSearchSpecification.buildSearchQuery(request, loggedInUser)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isNotEmpty())
    assertEquals(1, result.size)
    assertEquals(savedSearch.id, result.first().id)
  }

  @Test
  fun `test owned fail`() {
    repo.save(savedSearch.apply { defaultSearch = true })
    val request = SearchSavedSearchRequest().apply { owned = false }
    val spec = SavedSearchSpecification.buildSearchQuery(request, loggedInUser)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isEmpty())
  }

  @Test
  fun `test owned no loggedin user`() {
    repo.save(savedSearch.apply { defaultSearch = true })
    val request = SearchSavedSearchRequest().apply { owned = false }
    val spec = SavedSearchSpecification.buildSearchQuery(request, null)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isEmpty())
  }

  @Test
  fun `test shared`() {
    userRepository.save(loggedInUser.apply { sharedSearches = setOf(savedSearch) })
    val request = SearchSavedSearchRequest().apply { shared = true }
    val spec = SavedSearchSpecification.buildSearchQuery(request, loggedInUser)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isNotEmpty())
    assertEquals(1, result.size)
    assertEquals(savedSearch.id, result.first().id)
  }

  @Test
  fun `test shared empty`() {
    userRepository.save(loggedInUser.apply { sharedSearches = emptySet() })
    val request = SearchSavedSearchRequest().apply { shared = true }
    val spec = SavedSearchSpecification.buildSearchQuery(request, loggedInUser)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isNotEmpty())
  }

  @Test
  fun `test shared false`() {
    repo.save(savedSearch.apply { defaultSearch = true })
    val request = SearchSavedSearchRequest().apply { shared = false }
    val spec = SavedSearchSpecification.buildSearchQuery(request, loggedInUser)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isEmpty())
  }
}
