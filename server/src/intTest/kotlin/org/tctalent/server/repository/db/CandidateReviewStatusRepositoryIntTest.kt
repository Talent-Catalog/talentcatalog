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
import org.tctalent.server.model.db.Candidate
import org.tctalent.server.model.db.CandidateReviewStatusItem
import org.tctalent.server.model.db.ReviewStatus
import org.tctalent.server.model.db.SavedSearch
import org.tctalent.server.repository.db.integrationhelp.*

class CandidateReviewStatusRepositoryIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repo: CandidateReviewStatusRepository
  @Autowired lateinit var savedSearchRepository: SavedSearchRepository
  @Autowired private lateinit var candidateRepo: CandidateRepository
  @Autowired private lateinit var userRepo: UserRepository
  private lateinit var testCandidate: Candidate
  private lateinit var testSavedSearch: SavedSearch
  private lateinit var candidateReviewStatusItem: CandidateReviewStatusItem

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    // saved search
    testSavedSearch = getSavedSavedSearch(savedSearchRepository)

    savedSearchRepository.save(testSavedSearch)
    assertTrue { testSavedSearch.id > 0 }

    testCandidate = getSavedCandidate(candidateRepo, getSavedUser(userRepo))

    candidateReviewStatusItem =
      getCandidateReviewStatusItem().apply {
        savedSearch = testSavedSearch
        candidate = testCandidate
      }
    repo.save(candidateReviewStatusItem)
    assertTrue { candidateReviewStatusItem.id > 0 }
  }

  @Test
  fun `find reviewed candidates for search`() {
    val item =
      repo.findReviewedCandidatesForSearch(testSavedSearch.id, listOf(ReviewStatus.verified))
    assertNotNull(item)
    assertTrue { item.isNotEmpty() }
    assertEquals(1, item.size)
    assertEquals(candidateReviewStatusItem.candidate.id, item.first().id)
  }

  @Test
  fun `find reviewed candidates for search fail`() {
    val item =
      repo.findReviewedCandidatesForSearch(testSavedSearch.id, listOf(ReviewStatus.unverified))
    assertNotNull(item)
    assertTrue { item.isEmpty() }
  }
}
