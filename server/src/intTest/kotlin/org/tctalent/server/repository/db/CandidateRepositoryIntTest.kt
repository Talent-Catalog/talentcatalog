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
import org.springframework.data.domain.Pageable
import org.tctalent.server.model.db.*
import org.tctalent.server.repository.db.integrationhelp.*

class CandidateRepositoryIntTest : BaseDBIntegrationTest() {
  @Autowired private lateinit var repo: CandidateRepository
  @Autowired lateinit var userRepository: UserRepository
  @Autowired lateinit var savedSearchRepository: SavedSearchRepository
  @Autowired lateinit var crsiRepository: CandidateReviewStatusRepository
  @Autowired lateinit var countryRepository: CountryRepository

  private lateinit var testCandidate: Candidate

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    testCandidate = getSavedCandidate(repo, getSavedUser(userRepository))
  }

  @Test
  fun `test find by phone ignore case`() {
    val result = repo.findByPhoneIgnoreCase(testCandidate.phone)
    assertNotNull(result)
    assertEquals(testCandidate.id, result.id)
  }

  @Test
  fun `test find by phone ignore case fail`() {
    val result = repo.findByPhoneIgnoreCase("nothing")
    assertNull(result)
  }

  @Test
  fun `test find by whatsapp ignore case`() {
    val result = repo.findByWhatsappIgnoreCase(testCandidate.whatsapp)
    assertNotNull(result)
    assertEquals(testCandidate.id, result.id)
  }

  @Test
  fun `test find by whatsapp ignore case fail`() {
    val result = repo.findByWhatsappIgnoreCase("nothing")
    assertNull(result)
  }

  @Test
  fun `test find by id load candidate occupations`() {
    testCandidate.apply {
      candidateOccupations = listOf(CandidateOccupation().apply { id = 999999999 })
    }
    val result = repo.findByIdLoadCandidateOccupations(testCandidate.id)
    assertNotNull(result)
    assertEquals(testCandidate.id, result.id)
    assertNotNull(result.candidateOccupations)
    assertTrue { result.candidateOccupations.isNotEmpty() }
  }

  @Test
  fun `test find by id load certifications`() {
    testCandidate.apply {
      candidateCertifications = listOf(CandidateCertification().apply { id = 999999999 })
    }
    val result = repo.findByIdLoadCertifications(testCandidate.id)
    assertNotNull(result)
    assertEquals(testCandidate.id, result.id)
    assertNotNull(result.candidateCertifications)
    assertTrue { result.candidateCertifications.isNotEmpty() }
  }

  @Test
  fun `test find by id load candidate languages`() {
    testCandidate.apply {
      candidateLanguages = listOf(CandidateLanguage().apply { id = 999999999 })
    }
    val result = repo.findByIdLoadCandidateLanguages(testCandidate.id)
    assertNotNull(result)
    assertEquals(testCandidate.id, result.id)
    assertNotNull(result.candidateLanguages)
    assertTrue { result.candidateLanguages.isNotEmpty() }
  }

  @Test
  fun `test find by user id`() {
    val result = repo.findByUserId(testCandidate.user.id)
    assertNotNull(result)
    assertEquals(testCandidate.id, result.id)
  }

  @Test
  fun `test find by ids`() {
    val ids = listOf(testCandidate.id)
    val result = repo.findByIds(ids)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(testCandidate.id, result.first().id)
  }

  @Test
  fun `test find by id load saved lists`() {
    testCandidate.addSavedList(getSavedList())

    val result = repo.findByIdLoadSavedLists(testCandidate.id)
    assertNotNull(result)
    assertEquals(testCandidate.id, result.id)
    assertNotNull(result.candidateSavedLists)
    assertTrue { result.candidateSavedLists.isNotEmpty() }
  }

  @Test
  fun `test find candidates where status not deleted`() {
    val result = repo.findCandidatesWhereStatusNotDeleted(Pageable.unpaged())
    assertNotNull(result)
    assertTrue { result.content.isNotEmpty() }
    assertTrue { result.content.none { it.status == CandidateStatus.deleted } }
  }

  @Test
  fun `test find by statuses`() {
    val result = repo.findByStatuses(listOf(CandidateStatus.active))
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
  }

  @Test
  fun `test find by statuses or sf link is not null`() {
    val statuses = listOf(CandidateStatus.active)
    val result = repo.findByStatusesOrSfLinkIsNotNull(statuses, Pageable.unpaged())
    assertNotNull(result)
    assertTrue { result.content.isNotEmpty() }
    assertEquals(testCandidate.id, result.content.first().id)
  }

  @Test
  fun `test find by statuses or sf link is not null status test`() {
    val statuses = listOf(CandidateStatus.pending)
    val result = repo.findByStatusesOrSfLinkIsNotNull(statuses, Pageable.unpaged())
    assertNotNull(result)
    assertTrue { result.content.isEmpty() }
  }

  @Test
  fun `test find by statuses or sf link is not null sflink test`() {
    repo.save(testCandidate.apply { sflink = "NOTNULL" })
    val statuses = listOf(CandidateStatus.pending)
    val result = repo.findByStatusesOrSfLinkIsNotNull(statuses, Pageable.unpaged())
    assertNotNull(result)
    assertTrue { result.content.isNotEmpty() }
    assertEquals(testCandidate.id, result.content.first().id)
  }

  @Test
  fun `test clear all candidate text search ids`() {
    repo.clearAllCandidateTextSearchIds()
    val result = repo.findById(testCandidate.id).getOrNull()
    assertNotNull(result)
    assertNull(result.textSearchId)
  }

  @Test
  fun `test find reviewed candidates by saved search id`() {
    val testSavedSearch = getSavedSavedSearch(savedSearchRepository)
    crsiRepository.save(
      getCandidateReviewStatusItem().apply {
        candidate = testCandidate
        savedSearch = testSavedSearch
      }
    )

    val result =
      repo.findReviewedCandidatesBySavedSearchId(
        testSavedSearch.id,
        listOf(ReviewStatus.rejected),
        Pageable.unpaged(),
      )
    assertNotNull(result)
    assertTrue { result.content.isNotEmpty() }
    assertEquals(testCandidate.id, result.content.first().id)
  }

  @Test
  fun `test find reviewed candidates by saved search id fail status`() {
    val testSavedSearch = getSavedSavedSearch(savedSearchRepository)
    crsiRepository.save(
      getCandidateReviewStatusItem().apply {
        candidate = testCandidate
        savedSearch = testSavedSearch
      }
    )

    val result =
      repo.findReviewedCandidatesBySavedSearchId(
        testSavedSearch.id,
        listOf(ReviewStatus.verified),
        Pageable.unpaged(),
      )
    assertNotNull(result)
    assertTrue { result.content.isEmpty() }
  }

  @Test
  fun `test find by candidate number restricted`() {
    val country = getSavedCountry(countryRepository)
    repo.save(testCandidate.apply { this.country = country })

    val result = repo.findByCandidateNumberRestricted(testCandidate.candidateNumber, setOf(country))
    assertTrue(result.isPresent)
    assertEquals(testCandidate.id, result.get().id)
  }

  @Test
  fun `test find by candidate number restricted fail`() {
    val country = getSavedCountry(countryRepository)
    repo.save(testCandidate.apply { this.country = country })
    val result = repo.findByCandidateNumberRestricted("INVALID_NUMBER", setOf(country))
    assertFalse(result.isPresent)
  }

  @Test
  fun `test search candidate email`() {
    val country = getSavedCountry(countryRepository)
    repo.save(testCandidate.apply { this.country = country })
    val result =
      repo.searchCandidateEmail(testCandidate.user.email, setOf(country), Pageable.unpaged())
    assertNotNull(result)
    assertTrue { result.content.isNotEmpty() }
    assertEquals(testCandidate.id, result.content.first().id)
  }

  @Test
  fun `test search candidate email fail`() {
    val country = getSavedCountry(countryRepository)
    repo.save(testCandidate.apply { this.country = country })
    val result =
      repo.searchCandidateEmail("invalid@example.com", setOf(country), Pageable.unpaged())
    assertNotNull(result)
    assertTrue { result.content.isEmpty() }
  }
}
