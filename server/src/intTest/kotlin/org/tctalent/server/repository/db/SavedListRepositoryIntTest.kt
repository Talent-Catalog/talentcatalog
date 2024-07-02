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
import org.tctalent.server.model.db.*
import org.tctalent.server.repository.db.integrationhelp.*

class SavedListRepositoryIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repo: SavedListRepository
  @Autowired private lateinit var sfJobOppRepository: SalesforceJobOppRepository
  @Autowired private lateinit var userRepository: UserRepository
  @Autowired private lateinit var savedSearchRepository: SavedSearchRepository
  private lateinit var savedList: SavedList
  private lateinit var testSFJobOpp: SalesforceJobOpp
  private lateinit var testUser: User
  private lateinit var testSavedSearch: SavedSearch

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    savedList = getSavedSavedList(repo)
    testSFJobOpp = getSavedSfJobOpp(sfJobOppRepository)
    testUser = getSavedUser(userRepository)
  }

  @Test
  fun `test find by job ids`() {
    repo.save(savedList.apply { sfJobOpp = testSFJobOpp })
    val result = repo.findByJobIds(testSFJobOpp.id)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(savedList.id, result.first().id)
  }

  @Test
  fun `test find by job ids fail`() {
    val result = repo.findByJobIds(testSFJobOpp.id)
    assertNotNull(result)
    assertTrue { result.isEmpty() }
  }

  @Test
  fun `test find by name ignore case`() {
    repo.save(savedList.apply { users = mutableSetOf(testUser) })
    val results = repo.findByNameIgnoreCase(savedList.name, savedList.createdBy.id).getOrNull()
    assertNotNull(results)
    assertEquals(savedList.id, results.id)
  }

  @Test
  fun `test find by name ignore case fail`() {
    repo.save(savedList.apply { users = mutableSetOf(testUser) })
    val results = repo.findByNameIgnoreCase("NONE", testUser.id).getOrNull()
    assertNull(results)
  }

  @Test
  fun `test find by id load users`() {
    val result = repo.findByIdLoadUsers(savedList.id).getOrNull()
    assertNotNull(result)
    assertEquals(savedList.id, result.id)
  }

  @Test
  fun `test find by id load users fail no id`() {
    val result = repo.findByIdLoadUsers(0L).getOrNull()
    assertNull(result)
  }

  @Test
  fun `test find by id load candidates`() {
    val result = repo.findByIdLoadCandidates(savedList.id).getOrNull()
    assertNotNull(result)
    assertEquals(savedList.id, result.id)
  }

  @Test
  fun `test find by id load candidates fail no id`() {
    val result = repo.findByIdLoadCandidates(0L).getOrNull()
    assertNull(result)
  }

  @Test
  fun `test find selection list`() {
    testSavedSearch = getSavedSavedSearch(savedSearchRepository)
    repo.save(savedList.apply { savedSearch = testSavedSearch })
    val result = repo.findSelectionList(testSavedSearch.id, savedList.createdBy.id).getOrNull()
    assertNotNull(result)
    assertEquals(savedList.id, result.id)
  }

  @Test
  fun `test find selection list fail no search`() {
    testSavedSearch = getSavedSavedSearch(savedSearchRepository)
    val result = repo.findSelectionList(testSavedSearch.id, savedList.createdBy.id).getOrNull()
    assertNull(result)
  }

  @Test
  fun `test find selection list fail user`() {
    testSavedSearch = getSavedSavedSearch(savedSearchRepository)
    val result = repo.findSelectionList(testSavedSearch.id, null).getOrNull()
    assertNull(result)
  }

  @Test
  fun `test find registered job list`() {
    repo.save(savedList.apply { sfJobOpp = testSFJobOpp })
    val results = repo.findRegisteredJobList(savedList.sfJobOpp?.sfId ?: fail()).getOrNull()
    assertNotNull(results)
    assertEquals(savedList.id, results.id)
  }

  @Test
  fun `test find registered job list fail not registered`() {
    repo.save(
      savedList.apply {
        sfJobOpp = testSFJobOpp
        registeredJob = false
      }
    )

    val results = repo.findRegisteredJobList("${testSFJobOpp.id}").getOrNull()
    assertNull(results)
  }

  @Test
  fun `test find registered job list fail id`() {
    repo.save(savedList.apply { sfJobOpp = testSFJobOpp })
    // This is unusual to have a string for the sfJobLink
    val results = repo.findRegisteredJobList("").getOrNull()
    assertNull(results)
  }

  @Test
  fun `test find short name ignore case`() {
    val result = repo.findByShortNameIgnoreCase(savedList.tbbShortName).getOrNull()
    assertNotNull(result)
    assertEquals(savedList.id, result.id)
  }

  @Test
  fun `test find short name ignore case fail`() {
    val result = repo.findByShortNameIgnoreCase("BOB").getOrNull()
    assertNull(result)
  }

  @Test
  fun `test find lists with jobs`() {
    repo.save(savedList.apply { sfJobOpp = testSFJobOpp })
    val result = repo.findListsWithJobs()
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(savedList.id, result.first().id)
  }

  @Test
  fun `test find lists with jobs fail`() {
    repo.save(savedList.apply { status = Status.deleted })
    val result = repo.findListsWithJobs()
    assertNotNull(result)
    assertTrue { result.isEmpty() }
  }

  @Test
  fun `test find lists with jobs fail job opp null`() {
    val result = repo.findListsWithJobs()
    assertNotNull(result)
    assertTrue { result.isEmpty() }
  }
}
