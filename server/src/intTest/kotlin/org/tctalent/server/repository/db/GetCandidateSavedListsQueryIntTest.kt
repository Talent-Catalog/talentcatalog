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
import org.tctalent.server.model.db.CandidateSavedList
import org.tctalent.server.model.db.SavedList
import org.tctalent.server.repository.db.integrationhelp.*

class GetCandidateSavedListsQueryIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repo: CandidateSavedListRepository
  @Autowired lateinit var savedListRepository: SavedListRepository
  @Autowired lateinit var candidateRepository: CandidateRepository
  @Autowired lateinit var userRepository: UserRepository

  private lateinit var candidateSavedList: CandidateSavedList
  private lateinit var testSavedList: SavedList
  private lateinit var testCandidate: Candidate

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    testCandidate = getSavedCandidate(candidateRepository, getSavedUser(userRepository))
    testSavedList = getSavedSavedList(savedListRepository)

    candidateSavedList = getCandidateSavedList(testCandidate, testSavedList)
    repo.save(candidateSavedList)
    assertNotNull(candidateSavedList.id)
  }

  @Test
  fun `test candidate saved lists query`() {
    val spec = GetCandidateSavedListsQuery(testCandidate.id)
    val result = savedListRepository.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isNotEmpty())
    assertEquals(1, result.size)
    assertEquals(testSavedList.id, result.first().id)
  }

  @Test
  fun `test candidate saved lists query with no matching candidate`() {
    val spec = GetCandidateSavedListsQuery(-1L)
    val result = savedListRepository.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isEmpty())
  }
}
