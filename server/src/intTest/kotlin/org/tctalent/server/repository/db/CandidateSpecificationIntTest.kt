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
import org.tctalent.server.model.db.Candidate
import org.tctalent.server.model.db.CandidateStatus
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest
import org.tctalent.server.repository.db.integrationhelp.getSavedCandidate
import org.tctalent.server.repository.db.integrationhelp.getSavedUser
import org.tctalent.server.request.candidate.SearchCandidateRequest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CandidateSpecificationIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repo: CandidateRepository
  @Autowired lateinit var userRepository: UserRepository
  private lateinit var testCandidate: Candidate

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    testCandidate = getSavedCandidate(repo, getSavedUser(userRepository))
  }

  @Test
  fun `test keyword with empty name`() {
    val request = SearchCandidateRequest().apply { keyword = "" }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val results = repo.findAll(spec)
    // TODO: Assert based on your implementation (should it return all or no results?)
  }

  @Test
  fun `test keyword case insensitive`() {
    val request =
      SearchCandidateRequest().apply { keyword = testCandidate.user.firstName.uppercase() }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val results = repo.findAll(spec)
    assertNotNull(results)
    assertTrue(results.isNotEmpty())
  }

  @Test
  fun `test empty status with additional filters`() {
    val request =
      SearchCandidateRequest().apply {
        occupationIds = listOf(1) // Replace 1 with a valid occupation id
        minYrs = 2
        maxYrs = 5
      }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val results = repo.findAll(spec)
    assertNotNull(results)
    // Assert if results have specified occupation and experience range
  }

  @Test
  fun `test invalid status`() {
    val request = SearchCandidateRequest().apply { statuses = listOf(CandidateStatus.ineligible) }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val results = repo.findAll(spec)
    assertNotNull(results)
    assertTrue(results.isEmpty()) // May need adjustment based on implementation
  }

  @Test
  fun `test empty occupation with additional filters`() {
    val request =
      SearchCandidateRequest().apply {
        statuses = listOf(CandidateStatus.employed)
        minYrs = 2
        maxYrs = 5
      }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val results = repo.findAll(spec)
    assertNotNull(results)
    // Assert if results have specified status and experience range
  }

  @Test
  fun `test invalid occupation id`() {
    val request = SearchCandidateRequest().apply { occupationIds = listOf(-1) } // Invalid id
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val results = repo.findAll(spec)
    assertNotNull(results)
    assertTrue(results.isEmpty()) // May need adjustment based on implementation
  }

  @Test
  fun `test min yrs experience only`() {
    val request = SearchCandidateRequest().apply { minYrs = 2 }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val results = repo.findAll(spec)
    assertNotNull(results)
    // Assert if results have experience greater than or equal to 2 years
  }

  @Test
  fun `test max yrs experience only`() {
    val request = SearchCandidateRequest().apply { maxYrs = 5 }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val results = repo.findAll(spec)
    assertNotNull(results)
    // Assert if results have experience less than or equal to 5 years
  }

  @Test
  fun `test min yrs experience greater than max`() {
    val request =
      SearchCandidateRequest().apply {
        minYrs = 5
        maxYrs = 2
      }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val results = repo.findAll(spec)
    assertNotNull(results)
    assertTrue(results.isEmpty()) // No results expected
  }

  // ... Add test cases for combinations and empty input as mentioned before

}
