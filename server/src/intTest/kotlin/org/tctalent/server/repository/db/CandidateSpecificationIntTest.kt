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
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest
import org.tctalent.server.repository.db.integrationhelp.getSavedCandidate
import org.tctalent.server.repository.db.integrationhelp.getSavedUser

class CandidateSpecificationIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repo: CandidateRepository
  @Autowired lateinit var userRepository: UserRepository
  private lateinit var testCandidate: Candidate

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    testCandidate = getSavedCandidate(repo, getSavedUser(userRepository))
  }
}
