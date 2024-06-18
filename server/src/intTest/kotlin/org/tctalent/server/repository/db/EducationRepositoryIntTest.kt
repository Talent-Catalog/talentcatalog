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
import org.tctalent.server.model.db.Candidate
import org.tctalent.server.model.db.CandidateEducation
import org.tctalent.server.model.db.User
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest
import org.tctalent.server.repository.db.integrationhelp.getCandidateEducation
import org.tctalent.server.repository.db.integrationhelp.getSavedCandidate
import org.tctalent.server.repository.db.integrationhelp.getSavedUser

class EducationRepositoryIntTest : BaseDBIntegrationTest() {
  @Autowired private lateinit var repository: EducationRepository
  @Autowired private lateinit var candidateRepository: CandidateRepository
  @Autowired private lateinit var userRepository: UserRepository
  private lateinit var user: User
  private lateinit var testCandidate: Candidate
  private lateinit var ce: CandidateEducation

  @BeforeTest
  fun setup() {
    user = getSavedUser(userRepository)
    testCandidate = getSavedCandidate(candidateRepository, user)
    ce = getCandidateEducation()
    ce.apply { candidate = testCandidate }
    repository.save(ce)
    assertNotNull(ce.id)
    assertTrue { ce.id > 0 }
  }

  @Test
  fun `test find candidate by id`() {
    assertTrue { isContainerInitialized() }

    val savedCE = repository.findByIdLoadCandidate(ce.id).getOrNull()
    assertNotNull(savedCE)
    assertEquals(ce.institution, savedCE.institution)
  }

  @Test
  fun `test find candidate by id fail`() {
    assertTrue { isContainerInitialized() }

    val savedCE = repository.findByIdLoadCandidate(99999999L).getOrNull()
    assertNull(savedCE)
  }

  @Test
  fun `test find by id and education type`() {
    assertTrue { isContainerInitialized() }

    val savedCE = repository.findByIdLoadEducationType(ce.educationType)
    assertNotNull(savedCE)
    assertEquals(ce.institution, savedCE.institution)
  }

  /** Make sure it fails to find the saved one. */
  @Test
  fun `test find by id and education type fail`() {
    assertTrue { isContainerInitialized() }

    val savedCE = repository.findByIdLoadEducationType(ce.educationType)
    assertNull(savedCE)
  }
}
