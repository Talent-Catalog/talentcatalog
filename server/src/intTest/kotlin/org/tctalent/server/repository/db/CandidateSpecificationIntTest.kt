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

import org.springframework.beans.factory.annotation.Autowired
import org.tctalent.server.model.db.Candidate
import org.tctalent.server.model.db.CandidateStatus
import org.tctalent.server.model.db.SearchType
import org.tctalent.server.model.db.UnhcrStatus
import org.tctalent.server.repository.db.integrationhelp.*
import org.tctalent.server.request.candidate.SearchCandidateRequest
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.test.*

class CandidateSpecificationIntTest : BaseDBIntegrationTest() {
  @Autowired private lateinit var repo: CandidateRepository
  @Autowired private lateinit var userRepository: UserRepository
  @Autowired private lateinit var countryRepository: CountryRepository
  @Autowired private lateinit var educationLevelRepository: EducationLevelRepository
  @Autowired private lateinit var languageRepository: LanguageRepository
  @Autowired private lateinit var languageLevelRepository: LanguageLevelRepository
  @Autowired private lateinit var candidateLanguageRepository: CandidateLanguageRepository
  @Autowired lateinit var occupationRepository: OccupationRepository
  @Autowired lateinit var candidateOccupationRepository: CandidateOccupationRepository
  @Autowired lateinit var surveyTypeRepository: SurveyTypeRepository
  private lateinit var testCandidate: Candidate

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    testCandidate = getSavedCandidate(repo, getSavedUser(userRepository))
    repo.save(
      testCandidate.apply {
        nationality = getSavedCountry(countryRepository)
        country = getSavedCountry(countryRepository)
        maxEducationLevel = getSavedEducationLevel(educationLevelRepository)
      }
    )
  }

  @Test
  fun `test keyword with empty name`() {
    val request = SearchCandidateRequest().apply { keyword = "" }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val results = repo.findAll(spec)
    assertNotNull(results)
    assertTrue { results.isNotEmpty() }
    assertEquals(1, results.count())
  }

  @Test
  fun `test keyword case insensitive`() {

    val request =
      SearchCandidateRequest().apply { keyword = testCandidate.user.firstName.uppercase() }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val results = repo.findAll(spec)
    assertNotNull(results)
    assertTrue { results.isNotEmpty() }
    assertEquals(1, results.size)
    assertEquals(testCandidate.id, results.first().id)
  }

  @Test
  fun `test empty status with additional filters`() {

    val request =
      SearchCandidateRequest().apply {
        minYrs = 2
        maxYrs = 5
      }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val results = repo.findAll(spec)
    assertNotNull(results)
    assertTrue { results.isNotEmpty() }
    assertEquals(1, results.size)
    assertEquals(testCandidate.id, results.first().id)
  }

  @Test
  fun `test OK status`() {

    val request = SearchCandidateRequest().apply { statuses = listOf(CandidateStatus.active) }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val results = repo.findAll(spec)
    assertNotNull(results)
    assertTrue { results.isNotEmpty() }
    assertEquals(1, results.size)
    assertEquals(testCandidate.id, results.first().id)
  }

  @Test
  fun `test invalid status`() {

    val request = SearchCandidateRequest().apply { statuses = listOf(CandidateStatus.ineligible) }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val results = repo.findAll(spec)
    assertNotNull(results)
    assertTrue { results.isEmpty() }
  }

  @Test
  fun `test occupation with additional filters`() {
    val occ1 = getOccupation()
    occupationRepository.save(occ1)
    val co =
      getCandidateOccupation().apply {
        candidate = testCandidate
        occupation = occ1
      }
    candidateOccupationRepository.save(co)

    val request = SearchCandidateRequest().apply { occupationIds = listOf(co.id) }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val results = repo.findAll(spec)
    assertNotNull(results)
    assertEquals(1, results.size)
  }

  @Test
  fun `test invalid occupation id`() {

    val request = SearchCandidateRequest().apply { occupationIds = listOf(-1) }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val results = repo.findAll(spec)
    assertNotNull(results)
    assertTrue { results.isEmpty() }
  }

  @Test
  fun `test min yrs experience only`() {

    val occ1 = getCandidateOccupation()
    val occ2 = getCandidateOccupation()
    repo.save(testCandidate.apply { candidateOccupations = listOf(occ1, occ2) })
    val request = SearchCandidateRequest().apply { minYrs = 2 }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val results = repo.findAll(spec)
    assertNotNull(results)
    assertTrue { results.isNotEmpty() }
    println(results.first().candidateOccupations.first().yearsExperience)
    assertEquals(1, results.size)
  }

  @Test
  fun `test max yrs experience only`() {
    val request = SearchCandidateRequest().apply { maxYrs = 5 }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val results = repo.findAll(spec)
    assertNotNull(results)
    assertTrue { results.isNotEmpty() }
    assertEquals(testCandidate.id, results.first().id)
  }

  @Test
  fun `test min yrs experience greater than max`() {
    val request =
      SearchCandidateRequest().apply {
        minYrs = 2
        maxYrs = 5
      }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val results = repo.findAll(spec)
    assertNotNull(results)
    assertTrue { results.isNotEmpty() }
    assertEquals(testCandidate.id, results.first().id)
  }

  @Test
  fun `test min yrs experience greater than max fail`() {
    val request =
      SearchCandidateRequest().apply {
        minYrs = 10
        maxYrs = 5
      }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val results = repo.findAll(spec)
    assertNotNull(results)
    assertTrue { results.isEmpty() }
  }

  @Test
  fun `test survey type`() {
    val st = getSavedSurveyType(surveyTypeRepository)
    repo.save(testCandidate.apply { surveyType = st })
    val request = SearchCandidateRequest().apply { surveyTypeIds = listOf(st.id) }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val results = repo.findAll(spec)
    assertNotNull(results)
    assertTrue { results.isNotEmpty() }
    assertEquals(1, results.size)
  }

  /**  */
  @Test
  fun `test nationality search`() {

    val request =
      SearchCandidateRequest().apply { nationalityIds = listOf(testCandidate.nationality.id) }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val result = repo.findAll(spec)

    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertTrue(result.any { it.nationality.id == testCandidate.nationality.id })
  }

  @Test
  fun `test min age search`() {
    val request = SearchCandidateRequest().apply { minAge = 18 }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val result = repo.findAll(spec)

    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertTrue(result.all { it.dob?.isBefore(LocalDate.now().minusYears(18)) ?: true })
  }

  @Test
  fun `test max age search`() {
    val request = SearchCandidateRequest().apply { maxAge = 30 }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val result = repo.findAll(spec)

    assertNotNull(result)
    assertTrue(result.isNotEmpty(), "Expected results")
    assertEquals(1, result.size)
    assertTrue(
      result.all { it.dob?.isAfter(LocalDate.now().minusYears(30)) ?: true },
      "Wrong date of birth result.",
    )
  }

  @Test
  fun `test gender search`() {
    val request = SearchCandidateRequest().apply { gender = testCandidate.gender }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val result = repo.findAll(spec)

    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertTrue(result.any { it.gender == testCandidate.gender })
  }

  @Test
  fun `test education level search`() {
    val el = getSavedEducationLevel(educationLevelRepository)
    repo.save(testCandidate.apply { maxEducationLevel = el })
    val request =
      SearchCandidateRequest().apply { minEducationLevel = testCandidate.maxEducationLevel.level }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val result = repo.findAll(spec)

    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertTrue(result.any { it.maxEducationLevel.level >= testCandidate.maxEducationLevel.level })
  }

  /**
   * I think this test is problematic also. The query has 'english' hardcoded against the language.
   */
  @Test
  fun `test language search`() {
    val savedLanguage = languageRepository.save(getLanguage().apply { name = "english" })
    val cl =
      candidateLanguageRepository.save(
        getCandidateLanguage().apply {
          candidate = testCandidate
          language = savedLanguage
          writtenLevel = getSavedLanguageLevel(languageLevelRepository)
          spokenLevel = getSavedLanguageLevel(languageLevelRepository)
        }
      )

    repo.save(testCandidate.apply {})

    val request =
      SearchCandidateRequest().apply {
        englishMinWrittenLevel = cl.writtenLevel.level
        englishMinSpokenLevel = cl.spokenLevel.level
      }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val result = repo.findAll(spec)

    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(testCandidate.id, result.first().id)
  }

  /**
   * I think this test is problematic also. The query has 'english' hardcoded against the language.
   */
  @Test
  fun `test language search not english fails`() {
    val cl =
      candidateLanguageRepository.save(
        getCandidateLanguage().apply {
          candidate = testCandidate
          language = getSavedLanguage(languageRepository)
          writtenLevel = getSavedLanguageLevel(languageLevelRepository)
          spokenLevel = getSavedLanguageLevel(languageLevelRepository)
        }
      )

    repo.save(testCandidate.apply {})

    val request =
      SearchCandidateRequest().apply {
        englishMinWrittenLevel = cl.writtenLevel.level
        englishMinSpokenLevel = cl.spokenLevel.level
      }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val result = repo.findAll(spec)

    assertNotNull(result)
    assertTrue { result.isEmpty() }
  }

  @Test
  fun `test exclusion search`() {
    val excludedCandidates = listOf(testCandidate)
    val request = SearchCandidateRequest()
    val spec = CandidateSpecification.buildSearchQuery(request, null, excludedCandidates)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isEmpty() }
  }

  @Test
  fun `test last modified from search`() {
    repo.save(testCandidate.apply { updatedDate = OffsetDateTime.now().minusDays(1) })
    val request =
      SearchCandidateRequest().apply {
        lastModifiedFrom = LocalDate.now().minusDays(1)
        timezone = ZoneOffset.systemDefault().id
      }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val result = repo.findAll(spec)

    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(testCandidate.id, result.first().id)
  }

  @Test
  fun `test last modified to search`() {
    repo.save(testCandidate.apply { updatedDate = OffsetDateTime.now().minusDays(1) })
    val request =
      SearchCandidateRequest().apply {
        lastModifiedTo = LocalDate.now().minusDays(1)
        timezone = ZoneOffset.systemDefault().id
      }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val result = repo.findAll(spec)

    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(testCandidate.id, result.first().id)
  }

  @Test
  fun `test unhcr status`() {
    val request =
      SearchCandidateRequest().apply {
        unhcrStatuses = listOf(testCandidate.unhcrStatus, UnhcrStatus.NotRegistered)
      }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val result = repo.findAll(spec)

    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(testCandidate.id, result.first().id)
  }

  @Test
  fun `test country search with logged in user`() {
    val c = getSavedCountry(countryRepository)
    repo.save(testCandidate.apply { country = c })
    val user = testCandidate.user
    user.apply { sourceCountries = setOf(c) }
    val request = SearchCandidateRequest()
    val spec = CandidateSpecification.buildSearchQuery(request, user, null)
    val result = repo.findAll(spec)

    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(testCandidate.id, result.first().id)
  }

  @Test
  fun `test country search no search type`() {
    val c = getSavedCountry(countryRepository)
    repo.save(testCandidate.apply { country = c })

    val request =
      SearchCandidateRequest().apply {
        countryIds = listOf(c.id)
        countrySearchType = null
      }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val result = repo.findAll(spec)

    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(testCandidate.id, result.first().id)
  }

  @Test
  fun `test country search with search type`() {
    val c = getSavedCountry(countryRepository)
    repo.save(testCandidate.apply { country = c })

    val request =
      SearchCandidateRequest().apply {
        countryIds = listOf(12)
        countrySearchType = SearchType.and
      }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val result = repo.findAll(spec)

    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(testCandidate.id, result.first().id)
  }

  @Test
  fun `test referrer search`() {
    val referParam = "REFER"
    repo.save(testCandidate.apply { regoReferrerParam = referParam })

    val request =
      SearchCandidateRequest().apply {
        regoReferrerParam = referParam
      }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val result = repo.findAll(spec)

    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(testCandidate.id, result.first().id)
  }
}
