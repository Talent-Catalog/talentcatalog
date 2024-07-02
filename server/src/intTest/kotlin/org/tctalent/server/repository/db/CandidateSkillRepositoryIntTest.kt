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
import org.springframework.data.domain.Pageable
import org.tctalent.server.model.db.Candidate
import org.tctalent.server.model.db.CandidateSkill
import org.tctalent.server.repository.db.integrationhelp.*

class CandidateSkillRepositoryIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repo: CandidateSkillRepository
  @Autowired lateinit var candidateRepository: CandidateRepository
  @Autowired lateinit var userRepository: UserRepository
  private lateinit var candidateSkill: CandidateSkill
  private lateinit var testCandidate: Candidate

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    testCandidate = getSavedCandidate(candidateRepository, getSavedUser(userRepository))
    candidateSkill = getCandidateSkill().apply { candidate = testCandidate }
    repo.save(candidateSkill)
    assertTrue { candidateSkill.id > 0 }
  }

  @Test
  fun `test find by candidate id`() {

    val result = repo.findByCandidateId(testCandidate.id, Pageable.ofSize(10))
    assertNotNull(result)
    assertEquals(1, result.content.size)
    assertEquals(candidateSkill.id, result.content.first().id)
  }
}
