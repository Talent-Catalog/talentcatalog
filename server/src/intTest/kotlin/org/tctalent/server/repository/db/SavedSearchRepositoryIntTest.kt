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

import kotlin.jvm.optionals.getOrNull
import kotlin.test.*
import org.springframework.beans.factory.annotation.Autowired
import org.tctalent.server.model.db.SalesforceJobOpp
import org.tctalent.server.model.db.SavedSearch
import org.tctalent.server.model.db.Status
import org.tctalent.server.model.db.User
import org.tctalent.server.repository.db.integrationhelp.*

class SavedSearchRepositoryIntTest : BaseDBIntegrationTest() {
  @Autowired private lateinit var repo: SavedSearchRepository
  @Autowired private lateinit var sfJobOppRepository: SalesforceJobOppRepository
  @Autowired private lateinit var userRepository: UserRepository
  private lateinit var savedSearch: SavedSearch
  private lateinit var testSFJobOpp: SalesforceJobOpp
  private lateinit var testUser: User

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    savedSearch = getSavedSavedSearch(repo)
    testUser = getSavedUser(userRepository)
    testSFJobOpp = getSavedSfJobOpp(sfJobOppRepository)
  }

  @Test
  fun `test delete`() {
    repo.save(savedSearch.apply { sfJobOpp = testSFJobOpp })
    repo.deleteByJobId(testSFJobOpp.id)
    val result = repo.findAll()
    assertNotNull(result)
    assertTrue { result.isEmpty() }
  }

  @Test
  fun `test find by name ignore case`() {
    repo.save(savedSearch.apply { users = mutableSetOf(testUser) })
    val results = repo.findByNameIgnoreCase(savedSearch.name, savedSearch.createdBy.id)
    assertNotNull(results)
    assertEquals(savedSearch.id, results.id)
  }

  @Test
  fun `test find by name ignore case fail`() {
    repo.save(savedSearch.apply { users = mutableSetOf(testUser) })
    val results = repo.findByNameIgnoreCase("NONE", testUser.id)
    assertNull(results)
  }

  @Test
  fun `test find by id load search joins`() {
    val result = repo.findByIdLoadSearchJoins(savedSearch.id).getOrNull()
    assertNotNull(result)
    assertEquals(savedSearch.id, result.id)
  }

  @Test
  fun `test find by id load search joins fail`() {
    val result = repo.findByIdLoadSearchJoins(0L).getOrNull()
    assertNull(result)
  }

  @Test
  fun `test find by id load users`() {
    val result = repo.findByIdLoadUsers(savedSearch.id).getOrNull()
    assertNotNull(result)
    assertEquals(savedSearch.id, result.id)
  }

  @Test
  fun `test find by id load users fail deleted`() {
    repo.save(savedSearch.apply { status = Status.deleted })
    val result = repo.findByIdLoadUsers(savedSearch.id).getOrNull()
    assertNull(result)
  }

  @Test
  fun `test find by id load users fail no id`() {
    val result = repo.findByIdLoadUsers(0L).getOrNull()
    assertNull(result)
  }

  @Test
  fun `test find by id load audit`() {
    val result = repo.findByIdLoadAudit(savedSearch.id).getOrNull()
    assertNotNull(result)
    assertEquals(savedSearch.id, result.id)
  }

  @Test
  fun `test find by id load audit fail no id`() {
    val result = repo.findByIdLoadAudit(0L).getOrNull()
    assertNull(result)
  }

  @Test
  fun `test find by watcher ids is not null fail deleted`() {
    repo.save(savedSearch.apply { status = Status.deleted })
    val results = repo.findByWatcherIdsIsNotNull()
    assertNotNull(results)
    assertTrue { results.isEmpty() }
  }

  @Test
  fun `test find by watcher ids is not null fail is null`() {
    repo.save(savedSearch.apply { watcherIds = null })
    val results = repo.findByWatcherIdsIsNotNull()
    assertNotNull(results)
    assertTrue { results.isEmpty() }
  }

  @Test
  fun `test find by watcher ids is not null`() {
    // Put watcher on.
    repo.save(savedSearch.apply { watcherIds = "${testUser.id}" })
    val results = repo.findByWatcherIdsIsNotNull()
    assertNotNull(results)
    assertEquals(1, results.size)
    val ids = results.map { it.id }
    assertEquals(savedSearch.id, ids.first())
  }

  @Test
  fun `test find user watched searches`() {
    repo.save(savedSearch.apply { watcherIds = "${testUser.id}" })
    val results = repo.findUserWatchedSearches(testUser.id)
    assertNotNull(results)
    assertTrue { results.isNotEmpty() }
    assertEquals(1, results.size)
    val ids = results.map { it.id }
    assertEquals(savedSearch.id, ids.first())
  }

  @Test
  fun `test find user watched searches fail deleted`() {
    repo.save(savedSearch.apply { status = Status.deleted })
    val results = repo.findUserWatchedSearches(testUser.id)
    assertNotNull(results)
    assertTrue { results.isEmpty() }
  }

  @Test
  fun `test find user watched searches fail id`() {
    val results = repo.findUserWatchedSearches(0L)
    assertNotNull(results)
    assertTrue { results.isEmpty() }
  }

  @Test
  fun `test find default saved search`() {
    val results = repo.findDefaultSavedSearch(savedSearch.createdBy.id).getOrNull()
    assertNotNull(results)
    assertEquals(savedSearch.id, results.id)
  }

  @Test
  fun `test find default saved search fail`() {
    val results = repo.findDefaultSavedSearch(testUser.id).getOrNull()
    assertNull(results)
  }
}
