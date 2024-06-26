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
import org.tctalent.server.model.db.SalesforceJobOpp
import org.tctalent.server.model.db.SavedList
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest
import org.tctalent.server.repository.db.integrationhelp.getSavedSavedList
import org.tctalent.server.repository.db.integrationhelp.getSavedSfJobOpp
import org.tctalent.server.repository.db.integrationhelp.systemUser
import org.tctalent.server.request.list.SearchSavedListRequest
import kotlin.test.*

class GetSavedListsQueryIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repo: SavedListRepository
  @Autowired private lateinit var sfJobOppRepository: SalesforceJobOppRepository
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
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(savedList.id, result.first().id)
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
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(savedList.id, result.first().id)
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
  fun `test owned`() {
    val request = SearchSavedListRequest().apply { owned = true }
    val spec = GetSavedListsQuery(request, null)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(savedList.id, result.first().id)
  }
}
