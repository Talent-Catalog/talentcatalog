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
import java.time.ZoneOffset
import kotlin.test.*
import org.springframework.beans.factory.annotation.Autowired
import org.tctalent.server.model.db.Candidate
import org.tctalent.server.model.db.CandidateStatus
import org.tctalent.server.model.db.SearchType
import org.tctalent.server.model.db.UnhcrStatus
import org.tctalent.server.repository.db.integrationhelp.*
import org.tctalent.server.request.candidate.SearchCandidateRequest

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
  @Autowired lateinit var partnerRepository: PartnerRepository
  @Autowired lateinit var educationMajorRepository: EducationMajorRepository
  @Autowired lateinit var candidateEducationRepository: CandidateEducationRepository
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
  fun `test occupation`() {
    val occ1 = getSavedOccupation(occupationRepository)
    val co =
      getCandidateOccupation().apply {
        candidate = testCandidate
        occupation = occ1
      }
    candidateOccupationRepository.save(co)

    val request = SearchCandidateRequest().apply { occupationIds = listOf(occ1.id) }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val results = repo.findAll(spec)
    assertNotNull(results)
    assertTrue { results.isNotEmpty() }
    assertEquals(1, results.size)
    assertEquals(testCandidate.id, results.first().id)
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
  fun `test min yrs experience`() {
    val occ1 = getSavedOccupation(occupationRepository)
    val co =
      getCandidateOccupation().apply {
        candidate = testCandidate
        occupation = occ1
      }
    candidateOccupationRepository.save(co)

    val request =
      SearchCandidateRequest().apply {
        occupationIds = listOf(occ1.id)
        minYrs = 2
      }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val results = repo.findAll(spec)
    assertNotNull(results)
    assertTrue { results.isNotEmpty() }
    assertEquals(1, results.size)
    assertEquals(testCandidate.id, results.first().id)
  }

  @Test
  fun `test min yrs experience fail`() {
    val occ1 = getSavedOccupation(occupationRepository)
    val co =
      getCandidateOccupation().apply {
        candidate = testCandidate
        occupation = occ1
      }
    candidateOccupationRepository.save(co)

    val request =
      SearchCandidateRequest().apply {
        occupationIds = listOf(occ1.id)
        minYrs = 25
      }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val results = repo.findAll(spec)
    assertNotNull(results)
    assertTrue { results.isEmpty() }
  }

  @Test
  fun `test max yrs`() {
    val occ1 = getSavedOccupation(occupationRepository)
    val co =
      getCandidateOccupation().apply {
        candidate = testCandidate
        occupation = occ1
      }
    candidateOccupationRepository.save(co)

    val request =
      SearchCandidateRequest().apply {
        occupationIds = listOf(occ1.id)
        maxYrs = 20
      }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val results = repo.findAll(spec)
    assertNotNull(results)
    assertTrue { results.isNotEmpty() }
    assertEquals(1, results.size)
    assertEquals(testCandidate.id, results.first().id)
  }

  @Test
  fun `test max yrs experience fail`() {
    val occ1 = getSavedOccupation(occupationRepository)
    val co =
      getCandidateOccupation().apply {
        candidate = testCandidate
        occupation = occ1
      }
    candidateOccupationRepository.save(co)

    val request =
      SearchCandidateRequest().apply {
        occupationIds = listOf(occ1.id)
        maxYrs = 1
      }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val results = repo.findAll(spec)
    assertNotNull(results)
    assertTrue { results.isEmpty() }
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
    val level = getSavedLanguageLevel(languageLevelRepository)
    val savedLanguage = languageRepository.save(getLanguage().apply { name = "english" })
    val cl =
      candidateLanguageRepository.save(
        getCandidateLanguage().apply {
          candidate = testCandidate
          language = savedLanguage
          writtenLevel = level
          spokenLevel = level
        }
      )

    val request =
      SearchCandidateRequest().apply {
        englishMinWrittenLevel = cl.writtenLevel.level
        englishMinSpokenLevel = cl.spokenLevel.level
      }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val result = repo.findAll(spec)

    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
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

    val request = SearchCandidateRequest().apply { regoReferrerParam = referParam }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val result = repo.findAll(spec)

    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(testCandidate.id, result.first().id)
  }

  @Test
  fun `test partner search`() {
    val savedPartners = getSavedPartner(partnerRepository)
    userRepository.save(testCandidate.user.apply { partner = savedPartners })

    val request = SearchCandidateRequest().apply { partnerIds = listOf(savedPartners.id) }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val result = repo.findAll(spec)

    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(testCandidate.id, result.first().id)
  }

  @Test
  fun `test mini intake true`() {
    repo.save(testCandidate.apply { miniIntakeCompletedDate = OffsetDateTime.now().minusDays(50) })

    val request = SearchCandidateRequest().apply { miniIntakeCompleted = true }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val result = repo.findAll(spec)

    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(testCandidate.id, result.first().id)
  }

  @Test
  fun `test mini intake false`() {
    val request = SearchCandidateRequest().apply { miniIntakeCompleted = false }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val result = repo.findAll(spec)

    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(testCandidate.id, result.first().id)
  }

  @Test
  fun `test full intake true`() {
    repo.save(testCandidate.apply { fullIntakeCompletedDate = OffsetDateTime.now().minusDays(50) })

    val request = SearchCandidateRequest().apply { fullIntakeCompleted = true }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val result = repo.findAll(spec)

    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(testCandidate.id, result.first().id)
  }

  @Test
  fun `test full intake false`() {
    val request = SearchCandidateRequest().apply { fullIntakeCompleted = false }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val result = repo.findAll(spec)

    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(testCandidate.id, result.first().id)
  }

  @Test
  fun `test education majors`() {
    val majors = getSavedEducationMajor(educationMajorRepository)
    repo.save(testCandidate.apply { country = getSavedCountry(countryRepository) })

    candidateEducationRepository.save(
      getCandidateEducation().apply {
        candidate = testCandidate
        educationMajor = majors
        country = testCandidate.country
      }
    )

    val request = SearchCandidateRequest().apply { educationMajorIds = listOf(majors.id) }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val result = repo.findAll(spec)

    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(testCandidate.id, result.first().id)
  }

  @Test
  fun `test migration majors`() {
    val majors = getSavedEducationMajor(educationMajorRepository)
    repo.save(
      testCandidate.apply {
        country = getSavedCountry(countryRepository)
        migrationEducationMajor = majors
      }
    )

    candidateEducationRepository.save(
      getCandidateEducation().apply {
        candidate = testCandidate
        educationMajor = majors
        country = testCandidate.country
      }
    )

    val request = SearchCandidateRequest().apply { educationMajorIds = listOf(majors.id) }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val result = repo.findAll(spec)

    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(testCandidate.id, result.first().id)
  }

  @Test
  fun `test min english written level`() {
    val cl =
      candidateLanguageRepository.save(
        getCandidateLanguage().apply {
          candidate = testCandidate
          language = getSavedLanguage(languageRepository)
          writtenLevel = getSavedLanguageLevel(languageLevelRepository)
        }
      )

    val request = SearchCandidateRequest().apply { englishMinWrittenLevel = cl.writtenLevel.level }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val result = repo.findAll(spec)

    assertNotNull(result)
    assertTrue { result.isEmpty() }
  }

  @Test
  fun `test min english spoken level`() {
    val cl =
      candidateLanguageRepository.save(
        getCandidateLanguage().apply {
          candidate = testCandidate
          language = getSavedLanguage(languageRepository)
          spokenLevel = getSavedLanguageLevel(languageLevelRepository)
        }
      )

    val request = SearchCandidateRequest().apply { englishMinSpokenLevel = cl.spokenLevel.level }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val result = repo.findAll(spec)

    assertNotNull(result)
    assertTrue { result.isEmpty() }
  }

  @Test
  fun `test other language`() {
    val level = getSavedLanguageLevel(languageLevelRepository)
    val savedLanguage = languageRepository.save(getLanguage().apply { name = "NOT ENGLISH" })
    candidateLanguageRepository.save(
      getCandidateLanguage().apply {
        candidate = testCandidate
        language = savedLanguage
        spokenLevel = level
        writtenLevel = level
      }
    )

    val request =
      SearchCandidateRequest().apply {
        otherLanguageId = savedLanguage.id
        otherMinWrittenLevel = level.level
        otherMinSpokenLevel = level.level
      }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val result = repo.findAll(spec)

    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(testCandidate.id, result.first().id)
  }

  @Test
  fun `test other language min spoken`() {
    val level = getSavedLanguageLevel(languageLevelRepository)
    val savedLanguage = languageRepository.save(getLanguage().apply { name = "NOT ENGLISH" })
    candidateLanguageRepository.save(
      getCandidateLanguage().apply {
        candidate = testCandidate
        language = savedLanguage
        spokenLevel = level
      }
    )

    val request =
      SearchCandidateRequest().apply {
        otherLanguageId = savedLanguage.id
        otherMinSpokenLevel = level.level
      }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val result = repo.findAll(spec)

    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(testCandidate.id, result.first().id)
  }

  @Test
  fun `test other language min written`() {
    val level = getSavedLanguageLevel(languageLevelRepository)
    val savedLanguage = languageRepository.save(getLanguage().apply { name = "NOT ENGLISH" })

    candidateLanguageRepository.save(
      getCandidateLanguage().apply {
        candidate = testCandidate
        language = savedLanguage
        writtenLevel = level
      }
    )

    val request =
      SearchCandidateRequest().apply {
        otherLanguageId = savedLanguage.id
        otherMinWrittenLevel = level.level
      }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val result = repo.findAll(spec)

    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(testCandidate.id, result.first().id)
  }

  @Test
  fun `test excluded candidates`() {
    val newCandidate = getSavedCandidate(repo, getSavedUser(userRepository))
    repo.save(
      newCandidate.apply {
        nationality = getSavedCountry(countryRepository)
        country = getSavedCountry(countryRepository)
        maxEducationLevel = getSavedEducationLevel(educationLevelRepository)
      }
    )
    val request =
      SearchCandidateRequest().apply { keyword = newCandidate.user.firstName.uppercase() }
    val spec = CandidateSpecification.buildSearchQuery(request, null, listOf(testCandidate))
    val results = repo.findAll(spec)
    assertNotNull(results)
    assertTrue { results.isNotEmpty() }
    assertEquals(1, results.size)
    assertEquals(newCandidate.id, results.first().id)
  }

  /** Code under test is not in use, so will not fill it out right now. */
  @Test
  fun `test filter by opps`() {
    assertTrue { true }
  }
}
