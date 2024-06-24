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
import org.tctalent.server.model.db.SavedSearch
import org.tctalent.server.model.db.SearchJoin
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest
import org.tctalent.server.repository.db.integrationhelp.getSavedSavedSearch
import org.tctalent.server.repository.db.integrationhelp.getSearchJoin

class SearchJoinRepositoryIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repo: SearchJoinRepository
  @Autowired lateinit var savedSearchRepo: SavedSearchRepository
  private lateinit var searchJoin: SearchJoin
  private lateinit var testSavedSearch: SavedSearch

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }

    testSavedSearch = getSavedSavedSearch(savedSearchRepo)
    searchJoin =
      getSearchJoin().apply {
        savedSearch = testSavedSearch
        childSavedSearch = testSavedSearch
      }

    repo.save(searchJoin)
    assertTrue { searchJoin.id > 0 }
  }

  @Test
  fun `test delete by search id`() {
    // create a second item so we know the delete does a single one.
    val newTSS = getSavedSavedSearch(savedSearchRepo)
    val newSJ =
      getSearchJoin().apply {
        savedSearch = newTSS
        childSavedSearch = testSavedSearch
      }
    repo.save(newSJ)
    assertTrue { newSJ.id > 0 }

    repo.deleteBySearchId(testSavedSearch.id)
    val savedResults = repo.findById(newSJ.id).getOrNull()
    assertNotNull(savedResults)
    assertEquals(newSJ.id, savedResults.id)
  }
}
