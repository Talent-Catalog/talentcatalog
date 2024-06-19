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
import org.tctalent.server.model.db.CandidateCertification
import org.tctalent.server.repository.db.integrationhelp.*

class CandidateCertificationRepositoryIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repository: CandidateCertificationRepository
  @Autowired lateinit var candidateRepository: CandidateRepository
  @Autowired lateinit var userRepository: UserRepository
  private lateinit var cert: CandidateCertification
  private lateinit var testCandidate: Candidate

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    val savedUser = getSavedUser(userRepository)
    testCandidate = getSavedCandidate(candidateRepository, savedUser)
    cert = getCandidateCert().apply { candidate = testCandidate }
    val cert2 = getCandidateCert().apply { candidate = testCandidate }
    repository.save(cert)
    assertTrue { cert.id > 0 }
    repository.save(cert2)
    assertTrue { cert2.id > 0 }
  }

  @Test
  fun `test find by id and load candidate`() {
    val savedCert = repository.findByIdLoadCandidate(cert.id).getOrNull()
    assertNotNull(savedCert)
    assertEquals("999999999", savedCert.candidate.phone)
  }

  @Test
  fun `test find by id and load candidate fails`() {
    val savedCert = repository.findByIdLoadCandidate(99999999999).getOrNull()
    assertNull(savedCert)
  }

  @Test
  fun `test find by candidate id`() {
    val savedCert = repository.findByCandidateId(testCandidate.id)
    assertNotNull(savedCert)
    assertTrue { savedCert.isNotEmpty() }
    assertTrue { savedCert.size == 2 }
    val names = savedCert.map { it.name }
    assertTrue { names.contains("GREAT CERT") }
  }

  @Test
  fun `test find by candidate id fails`() {
    val savedCert = repository.findByCandidateId(999999999)
    assertTrue { savedCert.isEmpty() }
  }
}
