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
import org.junit.jupiter.api.Assertions.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.tctalent.server.model.db.Candidate
import org.tctalent.server.model.db.CandidateEducation
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest
import org.tctalent.server.repository.db.integrationhelp.getCandidateEducation
import org.tctalent.server.repository.db.integrationhelp.getSavedCandidate
import org.tctalent.server.repository.db.integrationhelp.getSavedUser

class CandidateEducationRepositoryIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repo: CandidateEducationRepository
  @Autowired lateinit var candidateRepository: CandidateRepository
  @Autowired lateinit var userRepository: UserRepository
  private lateinit var candidateEducation: CandidateEducation
  private lateinit var testCandidate: Candidate

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    testCandidate = getSavedCandidate(candidateRepository, getSavedUser(userRepository))
    candidateEducation = getCandidateEducation().apply { candidate = testCandidate }
    repo.save(candidateEducation)
    assertTrue { candidateEducation.id > 0 }
  }

  @Test
  fun `test find by id load candidate`() {
    val result = repo.findByIdLoadCandidate(candidateEducation.id).getOrNull()
    assertNotNull(result)
    assertEquals(candidateEducation.id, result?.id)
  }

  @Test
  fun `test find by id load candidate fail`() {
    val result = repo.findByIdLoadCandidate(9999L).getOrNull()
    assertNull(result)
  }

  @Test
  fun `test find by candidate id`() {
    val results = repo.findByCandidateId(testCandidate.id)
    assertNotNull(results)
    assertTrue { results.isNotEmpty() }
    assertEquals(candidateEducation.id, results.first().id)
  }

  @Test
  fun `test find by candidate id fail`() {
    val results = repo.findByCandidateId(99999L)
    assertNotNull(results)
    assertTrue { results.isEmpty() }
  }

  @Test
  fun `test find by id and candidate id`() {
    val results = repo.findByIdAndCandidateId(candidateEducation.id, testCandidate.id)
    assertNotNull(results)
    assertEquals(candidateEducation.id, results.id)
  }

  @Test
  fun `test find by id and candidate id fail candidate`() {
    val results = repo.findByIdAndCandidateId(candidateEducation.id, 9999L)
    assertNull(results)
  }

  @Test
  fun `test find by id and candidate id fail id`() {
    val results = repo.findByIdAndCandidateId(9999L, testCandidate.id)
    assertNull(results)
  }
}
