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
import kotlin.test.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.tctalent.server.model.db.Candidate
import org.tctalent.server.model.db.CandidateAttachment
import org.tctalent.server.repository.db.integrationhelp.*

class CandidateAttachmentRepositoryIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repo: CandidateAttachmentRepository
  @Autowired lateinit var candidateRepository: CandidateRepository
  @Autowired lateinit var userRepository: UserRepository
  private lateinit var candidateAttachment: CandidateAttachment
  private lateinit var testCandidate: Candidate

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    testCandidate = getSavedCandidate(candidateRepository, getSavedUser(userRepository))
    candidateAttachment = getCandidateAttachment().apply { candidate = testCandidate }
    repo.save(candidateAttachment)
    assertTrue { candidateAttachment.id > 0 }
  }

  @Test fun `test find by candidate id load audit`() {}

  @Test fun `test find by candidate id load audit fail`() {}
}
