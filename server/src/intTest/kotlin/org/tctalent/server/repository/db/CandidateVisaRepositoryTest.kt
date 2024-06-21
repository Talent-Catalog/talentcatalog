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
import org.tctalent.server.model.db.CandidateVisaCheck
import org.tctalent.server.repository.db.integrationhelp.*

class CandidateVisaRepositoryTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repo: CandidateVisaRepository
  @Autowired lateinit var candidateRepository: CandidateRepository
  @Autowired lateinit var userRepository: UserRepository
  @Autowired lateinit var countryRepository: CountryRepository
  private lateinit var testCandidate: Candidate
  private lateinit var candidateVisaCheck: CandidateVisaCheck

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    testCandidate = getSavedCandidate(candidateRepository, getSavedUser(userRepository))
    candidateVisaCheck =
      getCandidateVisaCheck().apply {
        candidate = testCandidate
        country = getSavedCountry(countryRepository)
      }
    repo.save(candidateVisaCheck)
    assertTrue { candidateVisaCheck.id > 0 }
  }

  @Test
  fun `test find by candidate id`() {
    val visaChecks = repo.findByCandidateId(testCandidate.id)
    assertNotNull(visaChecks)
    assertTrue { visaChecks.isNotEmpty() }
    assertEquals(1, visaChecks.size)
    val ids = visaChecks.map { it.id }
    assertTrue { ids.contains(candidateVisaCheck.id) }
  }

  @Test
  fun `test find by candidate id and country id`() {
    val visaChecks =
      repo.findByCandidateIdCountryId(testCandidate.id, candidateVisaCheck.country.id).getOrNull()
    assertNotNull(visaChecks)
    assertEquals(candidateVisaCheck.id, visaChecks.id)
  }

  @Test
  fun `test find by candidate id and country id fail`() {
    val visaChecks =
      repo.findByCandidateIdCountryId(testCandidate.id, null).getOrNull()
    assertNull(visaChecks)
  }
}
