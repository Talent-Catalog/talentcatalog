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

import java.time.LocalDate
import java.time.OffsetDateTime
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
  private lateinit var dateFrom: LocalDate
  private lateinit var dateTo: LocalDate
  private lateinit var testCountry: Country

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    testCountry = getSavedCountry(countryRepository)
    testCandidate = getSavedCandidate(repo, getSavedUser(userRepository))
    repo.save(testCandidate.apply { country = testCountry })

    dateFrom = OffsetDateTime.now().minusYears(4).toLocalDate()
    dateTo = OffsetDateTime.now().minusDays(10).toLocalDate()
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
    testCountry = getSavedCountry(countryRepository)
    repo.save(testCandidate.apply { this.country = testCountry })

    val result =
      repo.findByCandidateNumberRestricted(testCandidate.candidateNumber, setOf(testCountry))
    assertTrue(result.isPresent)
    assertEquals(testCandidate.id, result.get().id)
  }

  @Test
  fun `test find by candidate number restricted fail`() {
    testCountry = getSavedCountry(countryRepository)
    repo.save(testCandidate.apply { this.country = testCountry })
    val result = repo.findByCandidateNumberRestricted("INVALID_NUMBER", setOf(testCountry))
    assertFalse(result.isPresent)
  }

  @Test
  fun `test search candidate email`() {
    testCountry = getSavedCountry(countryRepository)
    repo.save(testCandidate.apply { this.country = testCountry })
    val result =
      repo.searchCandidateEmail(testCandidate.user.email, setOf(testCountry), Pageable.unpaged())
    assertNotNull(result)
    assertTrue { result.content.isNotEmpty() }
    assertEquals(testCandidate.id, result.content.first().id)
  }

  @Test
  fun `test search candidate email fail`() {
    testCountry = getSavedCountry(countryRepository)
    repo.save(testCandidate.apply { this.country = testCountry })
    val result =
      repo.searchCandidateEmail("invalid@example.com", setOf(testCountry), Pageable.unpaged())
    assertNotNull(result)
    assertTrue { result.content.isEmpty() }
  }

  @Test
  fun `test find by id load user`() {
    testCountry = getSavedCountry(countryRepository)
    repo.save(testCandidate.apply { country = testCountry })
    val result = repo.findByIdLoadUser(testCandidate.id, setOf(testCountry))
    assertTrue(result.isPresent)
    assertEquals(testCandidate.id, result.get().id)
  }

  @Test
  fun `test find by id load user fail`() {
    val result = repo.findByIdLoadUser(99999L, setOf(getSavedCountry(countryRepository)))
    assertFalse(result.isPresent)
  }

  //  @Test
  //  fun `test find by nationality id`() {
  //    val x = repo.save(testCandidate.apply { nationality = x })
  //    val result = repo.findByNationalityId(testCandidate.nationality.id)
  //    assertNotNull(result)
  //    assertTrue { result.isNotEmpty() }
  //    assertEquals(testCandidate.id, result.first().id)
  //  }

  // Nationality query doesn't appear to be used.
  // TOOD (Check this)
  @Test
  fun `test find by nationality id fail`() {
    val result = repo.findByNationalityId(99999L) // Assuming 99999L is an invalid ID
    assertNotNull(result)
    assertTrue { result.isEmpty() }
  }

  @Test
  fun `test find by country id`() {
    testCountry = getSavedCountry(countryRepository)
    repo.save(testCandidate.apply { country = testCountry })
    val result = repo.findByCountryId(testCountry.id)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(testCandidate.id, result.first().id)
  }

  @Test
  fun `test find by country id fail`() {
    val result = repo.findByCountryId(99999L) // Assuming 99999L is an invalid ID
    assertNotNull(result)
    assertTrue { result.isEmpty() }
  }

  @Test
  fun `test find by candidate number`() {
    val result = repo.findByCandidateNumber(testCandidate.candidateNumber)
    assertNotNull(result)
    assertEquals(testCandidate.id, result.id)
  }

  @Test
  fun `test find by candidate number fail`() {
    val result = repo.findByCandidateNumber("INVALID_NUMBER")
    assertNull(result)
  }

  @Test
  fun `test count by birth year order by year`() {
    val sourceIds = getSourceCountryIds(countryRepository, testCountry)
    val result = repo.countByBirthYearOrderByYear(Gender.male.name, sourceIds, dateFrom, dateTo)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
  }

  @Test
  fun `test count by birth year order by year with candidate ids`() {
    val sourceIds = getSourceCountryIds(countryRepository, testCountry)
    val result =
      repo.countByBirthYearOrderByYear(
        Gender.male.name,
        sourceIds,
        dateFrom,
        dateTo,
        getCandidateIds(repo, userRepository, testCandidate),
      )
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
  }

  @Test
  fun `test count by created date order by count`() {
    val sourceIds = getSourceCountryIds(countryRepository, testCountry)
    val result = repo.countByCreatedDateOrderByCount(sourceIds, dateFrom, dateTo)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
  }

  @Test
  fun `test count by created date order by count with candidate ids`() {
    val sourceIds = getSourceCountryIds(countryRepository, testCountry)
    val result =
      repo.countByCreatedDateOrderByCount(
        sourceIds,
        dateFrom,
        dateTo,
        getCandidateIds(repo, userRepository, testCandidate),
      )
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
  }

  @Test
  fun `test count LinkedIn by created date order by count`() {
    val sourceIds = getSourceCountryIds(countryRepository, testCountry)
    val result = repo.countLinkedInByCreatedDateOrderByCount(sourceIds, dateFrom, dateTo)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
  }

  @Test
  fun `test count LinkedIn by created date order by count with candidate ids`() {
    val sourceIds = getSourceCountryIds(countryRepository, testCountry)
    val result =
      repo.countLinkedInByCreatedDateOrderByCount(
        sourceIds,
        dateFrom,
        dateTo,
        getCandidateIds(repo, userRepository, testCandidate),
      )
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
  }

  @Test
  fun `test count by gender order by count`() {
    val sourceIds = getSourceCountryIds(countryRepository, testCountry)
    val result = repo.countByGenderOrderByCount(sourceIds, dateFrom, dateTo)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
  }

  @Test
  fun `test count by gender order by count with candidate ids`() {
    val sourceIds = getSourceCountryIds(countryRepository, testCountry)
    val result =
      repo.countByGenderOrderByCount(
        sourceIds,
        dateFrom,
        dateTo,
        getCandidateIds(repo, userRepository, testCandidate),
      )
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
  }

  // This test will fail, as the sql is checking for a lowercase name to be
  // passed in from the country rather than the query making like for like.
  // TODO (need to fix the query)
  @Test
  fun `test count by status order by count`() {

    val result =
      repo.countByStatusOrderByCount(
        Gender.male.name,
        testCountry.name.lowercase(),
        listOf(testCountry.id),
        dateFrom,
        dateTo,
      )
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
  }

  // This test will fail, as the sql is checking for a lowercase name to be
  // passed in from the country rather than the query making like for like.
  // TODO (need to fix the query)
  @Test
  fun `test count by status order by count with candidate ids`() {
    val sourceIds = getSourceCountryIds(countryRepository, testCountry)
    val result =
      repo.countByStatusOrderByCount(
        Gender.male.name,
        testCountry.name.lowercase(),
        sourceIds,
        dateFrom,
        dateTo,
        getCandidateIds(repo, userRepository, testCandidate),
      )
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
  }

  // This test will fail, as the sql is checking for a lowercase name to be
  // passed in from the country rather than the query making like for like.
  // TODO (need to fix the query)
  @Test
  fun `test count by referrer order by count`() {
    testCandidate.apply { regoReferrerParam = "REGOREFERRER" }
    val sourceIds = getSourceCountryIds(countryRepository, testCountry)
    val result =
      repo.countByReferrerOrderByCount(
        Gender.male.name,
        testCountry.name.lowercase(),
        sourceIds,
        dateFrom,
        dateTo,
      )
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
  }

  // This test will fail, as the sql is checking for a lowercase name to be
  // passed in from the country rather than the query making like for like.
  // TODO (need to fix the query)
  @Test
  fun `test count by referrer order by count with candidate ids`() {
    testCandidate.apply { regoReferrerParam = "REGOREFERRER" }
    val sourceIds = getSourceCountryIds(countryRepository, testCountry)
    val result =
      repo.countByReferrerOrderByCount(
        Gender.male.name,
        testCountry.name.lowercase(),
        sourceIds,
        dateFrom,
        dateTo,
        getCandidateIds(repo, userRepository, testCandidate),
      )
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
  }
}
