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
import org.tctalent.server.request.candidate.SavedListGetRequest

class GetSavedListCandidatesQueryIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repo: CandidateSavedListRepository
  @Autowired lateinit var savedListRepository: SavedListRepository
  @Autowired lateinit var candidateRepository: CandidateRepository
  @Autowired lateinit var salesforceJobOppRepository: SalesforceJobOppRepository
  @Autowired lateinit var userRepository: UserRepository

  private lateinit var testSavedList: SavedList
  private lateinit var testCandidate: Candidate
  private lateinit var candidateSavedList: CandidateSavedList

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    val testSfJobOpp = getSavedSfJobOpp(salesforceJobOppRepository)
    testCandidate = getSavedCandidate(candidateRepository, getSavedUser(userRepository))

    testSavedList = getSavedList().apply { sfJobOpp = testSfJobOpp }
    savedListRepository.save(testSavedList)

    val co =
      getCandidateOpportunity().apply {
        jobOpp = testSfJobOpp
        candidate = testCandidate
      }

    testCandidate.apply { candidateOpportunities = mutableListOf(co) }.addSavedList(testSavedList)
    candidateRepository.save(testCandidate)

    candidateSavedList = getCandidateSavedList(testCandidate, testSavedList)
    repo.save(candidateSavedList)

    assertNotNull(candidateSavedList.id)
  }

  @Test
  fun `test candidate saved lists query`() {
    val spec = GetSavedListCandidatesQuery(testSavedList, SavedListGetRequest())
    val result = candidateRepository.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isNotEmpty())
    assertEquals(1, result.size)
    assertEquals(testCandidate.id, result.first().id)
  }

  @Test
  fun `test candidate saved lists query with keyword`() {
    val request = SavedListGetRequest().apply { keyword = testCandidate.candidateNumber }
    val spec = GetSavedListCandidatesQuery(testSavedList, request)
    val result = candidateRepository.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isNotEmpty())
    assertEquals(1, result.size)
    assertEquals(testCandidate.id, result.first().id)
  }

  @Test
  fun `test candidate saved lists query with no matching keyword`() {
    val request = SavedListGetRequest().apply { keyword = "NOTHING" }
    val spec = GetSavedListCandidatesQuery(testSavedList, request)
    val result = candidateRepository.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isEmpty())
  }

  @Test
  fun `test candidate saved lists query with job opp`() {
    val spec = GetSavedListCandidatesQuery(testSavedList, SavedListGetRequest())
    val result = candidateRepository.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isNotEmpty())
    assertEquals(1, result.size)
    assertEquals(testCandidate.id, result.first().id)
  }

  @Test
  fun `test candidate saved lists query with closed opps`() {
    val request = SavedListGetRequest().apply { showClosedOpps = false }
    val spec = GetSavedListCandidatesQuery(testSavedList, request)
    val result = candidateRepository.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isNotEmpty())
    assertEquals(1, result.size)
    assertEquals(testCandidate.id, result.first().id)
  }

  @Test
  fun `test candidate saved lists query with no matching candidate`() {
    val nonExistentSavedList = getSavedList()
    val spec = GetSavedListCandidatesQuery(nonExistentSavedList, SavedListGetRequest())
    val result = candidateRepository.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isEmpty())
  }
}
