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
import org.junit.jupiter.api.Assertions.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.tctalent.server.model.db.Candidate
import org.tctalent.server.model.db.CandidateVisaJobCheck
import org.tctalent.server.model.db.SalesforceJobOpp
import org.tctalent.server.repository.db.integrationhelp.*

class CandidateVisaJobRepositoryIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repo: CandidateVisaJobRepository
  @Autowired private lateinit var userRepo: UserRepository
  @Autowired private lateinit var candidateRepo: CandidateRepository
  @Autowired private lateinit var countryRepo: CountryRepository
  @Autowired private lateinit var candidateVisaJobCheckRepository: CandidateVisaRepository
  @Autowired private lateinit var salesforceJobOppRepository: SalesforceJobOppRepository
  private lateinit var testCandidate: Candidate
  private lateinit var candidateVisaJobCheck: CandidateVisaJobCheck
  private lateinit var sfJobOpp: SalesforceJobOpp

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    testCandidate = getSavedCandidate(candidateRepo, getSavedUser(userRepo))
    val testCandidateVisaCheck =
      getCandidateVisaCheck().apply {
        candidate = testCandidate
        country = getSavedCountry(countryRepo)
      }
    candidateVisaJobCheckRepository.save(testCandidateVisaCheck)
    assertTrue { testCandidateVisaCheck.id > 0 }

    sfJobOpp = getSalesforceJobOpp()
    salesforceJobOppRepository.save(sfJobOpp)
    assertTrue { sfJobOpp.id > 0 }

    candidateVisaJobCheck =
      getCandidateVisaJobCheck().apply {
        candidateVisaCheck = testCandidateVisaCheck
        jobOpp = sfJobOpp
      }
    repo.save(candidateVisaJobCheck)
    assertTrue { candidateVisaJobCheck.id > 0 }
  }

  @Test
  fun `find by candidate id and job opp id`() {
    val cvjc = repo.findByCandidateIdAndJobOppId(testCandidate.id, sfJobOpp.id)
    assertNotNull(cvjc)
    assertEquals(cvjc.name, candidateVisaJobCheck.name)
  }

  @Test
  fun `find by candidate id and job opp id fail candidate id`() {
    val cvjc = repo.findByCandidateIdAndJobOppId(99999999999L, sfJobOpp.id)
    assertNull(cvjc)
  }

  @Test
  fun `find by candidate id and job opp id fail jobopp`() {
    val cvjc = repo.findByCandidateIdAndJobOppId(testCandidate.id, 9999999999L)
    assertNull(cvjc)
  }
}
