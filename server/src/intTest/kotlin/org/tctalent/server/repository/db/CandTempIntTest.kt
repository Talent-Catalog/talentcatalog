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

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.tctalent.server.model.db.Candidate
import org.tctalent.server.model.db.CandidateStatus
import org.tctalent.server.model.db.Gender
import org.tctalent.server.model.db.User
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest
import org.tctalent.server.repository.db.integrationhelp.getSavedCandidate
import org.tctalent.server.repository.db.integrationhelp.getSavedUser
import org.tctalent.server.request.candidate.SearchCandidateRequest

class CandTempIntTest : BaseDBIntegrationTest() {
  @Autowired private lateinit var repo: CandidateRepository
  @Autowired private lateinit var userRepository: UserRepository
  private lateinit var testCandidate: Candidate
  private lateinit var testUser: User

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    testUser = getSavedUser(userRepository)
    testCandidate = getSavedCandidate(repo, testUser)
  }

  @Test
  fun `test get status`() {
    val request = SearchCandidateRequest().apply { statuses = listOf(CandidateStatus.active) }
    val spec = CandidateSpecification.buildSearchQuery(request, testUser, null)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
  }

  @Test
  fun `test get gender`() {
    val request = SearchCandidateRequest().apply { gender = Gender.male }
    val spec = CandidateSpecification.buildSearchQuery(request, testUser, null)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
  }
}
