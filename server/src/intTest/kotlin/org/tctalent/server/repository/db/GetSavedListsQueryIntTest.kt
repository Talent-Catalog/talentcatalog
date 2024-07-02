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
import org.tctalent.server.model.db.SalesforceJobOpp
import org.tctalent.server.model.db.SavedList
import org.tctalent.server.repository.db.integrationhelp.*
import org.tctalent.server.request.list.SearchSavedListRequest

/** For testing specific cases set saved search so that results aren't returned. */
class GetSavedListsQueryIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repo: SavedListRepository
  @Autowired private lateinit var sfJobOppRepository: SalesforceJobOppRepository
  @Autowired private lateinit var savedSearchRepository: SavedSearchRepository
  private lateinit var savedList: SavedList
  private lateinit var testSFJobOpp: SalesforceJobOpp

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    savedList = getSavedSavedList(repo)
  }

  @Test
  fun `test keyword`() {
    val request = SearchSavedListRequest().apply { keyword = savedList.name }
    val spec = GetSavedListsQuery(request, null)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(savedList.id, result.first().id)
  }

  @Test
  fun `test keyword fail`() {
    val ss = getSavedSavedSearch(savedSearchRepository)
    repo.save(savedList.apply { savedSearch = ss })
    val request = SearchSavedListRequest().apply { keyword = "NOTHING" }
    val spec = GetSavedListsQuery(request, null)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isEmpty() }
  }

  @Test
  fun `test fixed`() {
    val request = SearchSavedListRequest().apply { fixed = true }
    val spec = GetSavedListsQuery(request, null)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(savedList.id, result.first().id)
  }

  @Test
  fun `test fixed false`() {
    val ss = getSavedSavedSearch(savedSearchRepository)
    repo.save(savedList.apply { savedSearch = ss })
    val request = SearchSavedListRequest().apply { fixed = false }
    val spec = GetSavedListsQuery(request, null)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertNotNull(result)
    assertTrue { result.isEmpty() }
  }

  @Test
  fun `test registeredJob`() {
    val request = SearchSavedListRequest().apply { registeredJob = true }
    val spec = GetSavedListsQuery(request, null)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(savedList.id, result.first().id)
  }

  // TODO (the query is broken as it uses a method call as an attribute)
  // Requires confirmation and fixing.
  @Test
  fun `test sfOppClosed`() {
    testSFJobOpp = getSavedSfJobOpp(sfJobOppRepository).apply { isClosed = true }
    repo.save(savedList.apply { savedList.sfJobOpp = testSFJobOpp })

    val request = SearchSavedListRequest().apply { sfOppClosed = true }
    repo.save(savedList)
    val spec = GetSavedListsQuery(request, null)
    fail(
      "Expect to fail - query uses a method in place of an attribute so can't work? Should be fixed."
    )
    //    val result = repo.findAll(spec)
    //    assertNotNull(result)
    //    assertTrue { result.isNotEmpty() }
    //    assertEquals(1, result.size)
    //    assertEquals(savedList.id, result.first().id)
  }

  @Test
  fun `test shortName`() {
    val request = SearchSavedListRequest().apply { shortName = true }
    val spec = GetSavedListsQuery(request, null)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(savedList.id, result.first().id)
  }

  @Test
  fun `test shortName false`() {
    val ss = getSavedSavedSearch(savedSearchRepository)
    repo.save(savedList.apply { savedSearch = ss })
    val request = SearchSavedListRequest().apply { shortName = false }
    val spec = GetSavedListsQuery(request, null)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isEmpty() }
  }

  @Test
  fun `test global`() {
    val request = SearchSavedListRequest().apply { global = true }
    val spec = GetSavedListsQuery(request, null)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(savedList.id, result.first().id)
  }

  @Test
  fun `test global false`() {
    val request = SearchSavedListRequest().apply { global = false }
    val spec = GetSavedListsQuery(request, null)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
  }

  @Test
  fun `test shared`() {
    val loggedInUser = systemUser().apply { sharedLists = setOf(savedList) }
    val request = SearchSavedListRequest().apply { shared = true }
    val spec = GetSavedListsQuery(request, loggedInUser)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(savedList.id, result.first().id)
  }

  @Test
  fun `test shared false`() {
    val ss = getSavedSavedSearch(savedSearchRepository)
    repo.save(savedList.apply { savedSearch = ss })
    val loggedInUser = systemUser().apply { sharedLists = setOf(savedList) }
    val request = SearchSavedListRequest().apply { shared = false }
    val spec = GetSavedListsQuery(request, loggedInUser)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isEmpty() }
  }

  @Test
  fun `test owned`() {
    val loggedInUser = systemUser().apply { sharedLists = setOf(savedList) }
    repo.save(savedList.apply { createdBy = loggedInUser })
    val request = SearchSavedListRequest().apply { owned = true }
    val spec = GetSavedListsQuery(request, loggedInUser)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(savedList.id, result.first().id)
  }

  @Test
  fun `test owned false`() {
    val ss = getSavedSavedSearch(savedSearchRepository)
    repo.save(savedList.apply { savedSearch = ss })
    val request = SearchSavedListRequest().apply { owned = false }
    val spec = GetSavedListsQuery(request, null)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isEmpty() }
  }

  @Test
  fun `test owned no logged in user`() {
    val ss = getSavedSavedSearch(savedSearchRepository)
    repo.save(savedList.apply { savedSearch = ss })
    val request = SearchSavedListRequest().apply { owned = false }
    val spec = GetSavedListsQuery(request, null)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isEmpty() }
  }
}
