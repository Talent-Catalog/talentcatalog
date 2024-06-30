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
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.test.*
import org.springframework.beans.factory.annotation.Autowired
import org.tctalent.server.model.db.Candidate
import org.tctalent.server.model.db.CandidateStatus
import org.tctalent.server.repository.db.integrationhelp.*
import org.tctalent.server.request.candidate.SearchCandidateRequest

class CandidateSpecificationIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repo: CandidateRepository
  @Autowired lateinit var userRepository: UserRepository
  @Autowired lateinit var educationLevelRepository: EducationLevelRepository
  private lateinit var testCandidate: Candidate

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    testCandidate = getSavedCandidate(repo, getSavedUser(userRepository))
  }

  @Test
  fun `test keyword with empty name`() {
    val request = SearchCandidateRequest().apply { keyword = "" }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val results = repo.findAll(spec)
    assertTrue { results.isEmpty() }
  }

  @Test
  fun `test keyword case insensitive`() {
    val request = SearchCandidateRequest().apply { keyword = testCandidate.user.firstName }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val results = repo.findAll(spec)
    assertNotNull(results)
    assertTrue(results.isNotEmpty())
    assertEquals(1, results.size)
    assertEquals(testCandidate.id, results.first().id)
  }

  @Test
  fun `test empty status with additional filters`() {
    val request =
      SearchCandidateRequest().apply {
        occupationIds = listOf(1) // Replace 1 with a valid occupation id
        minYrs = 2
        maxYrs = 5
      }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val results = repo.findAll(spec)
    assertNotNull(results)
    fail("Not implemented completely.")
  }

  @Test
  fun `test OK status`() {
    val request = SearchCandidateRequest().apply { statuses = listOf(CandidateStatus.active) }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val results = repo.findAll(spec)
    assertNotNull(results)
    assertTrue(results.isNotEmpty())
    assertEquals(1, results.size)
    assertEquals(testCandidate.id, results.first().id)
  }

  @Test
  fun `test invalid status`() {
    val request = SearchCandidateRequest().apply { statuses = listOf(CandidateStatus.ineligible) }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val results = repo.findAll(spec)
    assertNotNull(results)
    assertTrue(results.isEmpty())
  }

  @Test
  fun `test occupation with additional filters`() {
    val request =
      SearchCandidateRequest().apply {
        statuses = listOf(CandidateStatus.employed)
        minYrs = 2
        maxYrs = 5
        occupationIds = testCandidate.candidateOccupations.map { it.id }
      }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val results = repo.findAll(spec)
    assertNotNull(results)
    assertEquals(1, results.size)
  }

  @Test
  fun `test invalid occupation id`() {
    val request = SearchCandidateRequest().apply { occupationIds = listOf(-1) } // Invalid id
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val results = repo.findAll(spec)
    assertNotNull(results)
    assertTrue(results.isEmpty())
  }

  @Test
  fun `test min yrs experience only`() {
    val request = SearchCandidateRequest().apply { minYrs = 2 }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val results = repo.findAll(spec)
    assertNotNull(results)
    fail("Not implemented completely.")
  }

  @Test
  fun `test max yrs experience only`() {
    val request = SearchCandidateRequest().apply { maxYrs = 5 }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val results = repo.findAll(spec)
    assertNotNull(results)
    fail("Not implemented completely.")
  }

  @Test
  fun `test min yrs experience greater than max`() {
    val request =
      SearchCandidateRequest().apply {
        minYrs = 5
        maxYrs = 2
      }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val results = repo.findAll(spec)
    assertNotNull(results)
    assertTrue(results.isEmpty())
    fail("Not implemented completely.")
  }

  /**  */
  @Test
  fun `test nationality search`() {
    val request =
      SearchCandidateRequest().apply { nationalityIds = listOf(testCandidate.nationality.id) }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val result = repo.findAll(spec)

    assertNotNull(result)
    assertTrue(result.isNotEmpty())
    assertEquals(1, result.size)
    assertTrue(result.any { it.nationality.id == testCandidate.nationality.id })
  }

  @Test
  fun `test min age search`() {
    val request = SearchCandidateRequest().apply { minAge = 18 }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val result = repo.findAll(spec)

    assertNotNull(result)
    assertTrue(result.isNotEmpty())
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
    assertTrue(result.isNotEmpty())
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
    assertTrue(result.isNotEmpty())
    assertEquals(1, result.size)
    assertTrue(result.any { it.maxEducationLevel.level >= testCandidate.maxEducationLevel.level })
  }

  @Test
  fun `test language search`() {
    val request =
      SearchCandidateRequest().apply {
        englishMinWrittenLevel = testCandidate.candidateLanguages.first().writtenLevel.level
        englishMinSpokenLevel = testCandidate.candidateLanguages.first().spokenLevel.level
      }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val result = repo.findAll(spec)

    assertNotNull(result)
    assertTrue(result.isNotEmpty())
    assertTrue(
      result.any {
        it.candidateLanguages.any { cl ->
          cl.language.name.equals("english", ignoreCase = true) &&
            cl.writtenLevel.level >= request.englishMinWrittenLevel!! &&
            cl.spokenLevel.level >= request.englishMinSpokenLevel!!
        }
      }
    )
  }

  @Test
  fun `test exclusion search`() {
    val excludedCandidates = listOf(testCandidate)
    val request = SearchCandidateRequest()
    val spec = CandidateSpecification.buildSearchQuery(request, null, excludedCandidates)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isEmpty())
    assertTrue(result.none { it.id == testCandidate.id })
  }

  @Test
  fun `test last modified search`() {
    val request =
      SearchCandidateRequest().apply {
        lastModifiedFrom = LocalDate.now().minusDays(1)
        timezone = ZoneOffset.systemDefault().id
      }
    val spec = CandidateSpecification.buildSearchQuery(request, null, null)
    val result = repo.findAll(spec)

    assertNotNull(result)
    assertTrue(result.isNotEmpty())
    assertTrue(
      result.all {
        it.updatedDate.isAfter(
          OffsetDateTime.of(
            request.lastModifiedFrom,
            LocalTime.MIN,
            ZoneOffset.of(request.timezone),
          )
        )
      }
    )
  }
}
