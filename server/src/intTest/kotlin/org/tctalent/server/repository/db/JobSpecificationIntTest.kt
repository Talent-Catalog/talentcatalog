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
import org.springframework.data.domain.Sort
import org.tctalent.server.model.db.*
import org.tctalent.server.repository.db.integrationhelp.*
import org.tctalent.server.request.job.SearchJobRequest

class JobSpecificationIntTest : BaseDBIntegrationTest() {
  @Autowired private lateinit var repo: SalesforceJobOppRepository
  @Autowired private lateinit var savedListRepository: SavedListRepository
  @Autowired private lateinit var countryRepository: CountryRepository
  @Autowired private lateinit var userRepository: UserRepository
  @Autowired private lateinit var partnerRepository: PartnerRepository
  @Autowired private lateinit var jobChatRepository: JobChatRepository
  @Autowired private lateinit var jcuRepository: JobChatUserRepository
  @Autowired private lateinit var sfJobOpportunityRepository: SalesforceJobOppRepository
  @Autowired private lateinit var chatPostRepository: ChatPostRepository
  @Autowired private lateinit var candidateRepo: CandidateRepository
  private lateinit var job: SalesforceJobOpp
  private lateinit var savedList: SavedList
  private lateinit var savedCountry: Country
  private lateinit var loggedInUser: User

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }

    val savedPartner = getSavedPartner(partnerRepository)
    loggedInUser = userRepository.save(getUser().apply { partner = savedPartner })
    savedList = getSavedSavedList(savedListRepository)
    savedCountry = getSavedCountry(countryRepository)
  }

  @Test
  fun `test keyword with nothing name`() {
    job = getSalesforceJobOpp()
    repo.save(job)
    val request = SearchJobRequest().apply { keyword = "NOTHING" }
    val spec = JobSpecification.buildSearchQuery(request, null)
    val results = repo.findAll(spec)
    assertNotNull(results)
    assertTrue { results.isEmpty() }
  }

  @Test
  fun `test keyword case insensitive`() {
    job = repo.save(getSalesforceJobOpp().apply { submissionList = savedList })

    val request = SearchJobRequest().apply { keyword = job.name.uppercase() }
    val spec = JobSpecification.buildSearchQuery(request, null)
    val results = repo.findAll(spec)
    assertNotNull(results)
    assertTrue { results.isNotEmpty() }
    assertEquals(1, results.count())
    assertEquals(job.id, results.first().id)
  }

  @Test
  fun `test stages`() {
    job =
      repo.save(
        getSalesforceJobOpp().apply {
          stage = JobOpportunityStage.cvPreparation
          submissionList = savedList
        }
      )
    val request = SearchJobRequest().apply { stages = listOf(JobOpportunityStage.cvPreparation) }
    val spec = JobSpecification.buildSearchQuery(request, null)
    val results = repo.findAll(spec)
    assertNotNull(results)
    assertTrue { results.isNotEmpty() }
    assertEquals(1, results.count())
    assertEquals(job.id, results.first().id)
  }

  @Test
  fun `test stages fail`() {
    job = repo.save(getSalesforceJobOpp().apply { submissionList = savedList })
    val request = SearchJobRequest().apply { stages = listOf(JobOpportunityStage.noJobOffer) }
    val spec = JobSpecification.buildSearchQuery(request, null)
    val results = repo.findAll(spec)
    assertNotNull(results)
    assertTrue { results.isEmpty() }
  }

  @Test
  fun `test destinations`() {
    job =
      repo.save(
        getSalesforceJobOpp().apply {
          submissionList = savedList
          country = savedCountry
        }
      )

    val countryId = savedCountry.id
    val request = SearchJobRequest().apply { destinationIds = listOf(countryId) }
    val spec = JobSpecification.buildSearchQuery(request, null)
    val results = repo.findAll(spec)
    assertNotNull(results)
    assertTrue { results.isNotEmpty() }
    assertEquals(1, results.count())
    assertEquals(job.id, results.first().id)
  }

  //  @Test
  //  fun `test closed fail`() {
  //    job =
  //      repo.save(
  //        getSalesforceJobOpp().apply {
  //          isClosed = false
  //          submissionList = savedList
  //        }
  //      )
  //
  //    val request = SearchJobRequest().apply { sfOppClosed = true }
  //
  //    val spec = JobSpecification.buildSearchQuery(request, loggedInUser)
  //    val results = repo.findAll(spec)
  //    assertNotNull(results)
  //    assertTrue { results.isEmpty() }
  //  }

  @Test
  fun `test closed`() {
    job =
      repo.save(
        getSalesforceJobOpp().apply {
          submissionList = savedList
          isClosed = true
        }
      )

    val request = SearchJobRequest().apply { sfOppClosed = true }

    val spec = JobSpecification.buildSearchQuery(request, null)
    val results = repo.findAll(spec)
    assertNotNull(results)
    assertTrue { results.isNotEmpty() }
    assertEquals(1, results.count())
    assertEquals(job.id, results.first().id)
  }

  @Test
  fun `test get starred`() {
    job =
      repo.save(
        getSalesforceJobOpp().apply {
          submissionList = savedList
          starringUsers = setOf(loggedInUser)
        }
      )
    val request = SearchJobRequest().apply { starred = true }

    val spec = JobSpecification.buildSearchQuery(request, loggedInUser)
    val results = repo.findAll(spec)
    assertNotNull(results)
    assertTrue { results.isNotEmpty() }
    assertEquals(1, results.count())
    assertEquals(job.id, results.first().id)
  }

  @Test
  fun `test get starred logged in user null`() {
    job = repo.save(getSalesforceJobOpp().apply { starringUsers = setOf(loggedInUser) })

    val request = SearchJobRequest().apply { starred = true }
    val spec = JobSpecification.buildSearchQuery(request, null)
    val results = repo.findAll(spec)
    assertNotNull(results)
    assertTrue { results.isEmpty() }
  }

  @Test
  fun `test get owned by my partner`() {
    job =
      repo.save(
        getSalesforceJobOpp().apply {
          submissionList = savedList
          createdBy = loggedInUser
        }
      )

    val request =
      SearchJobRequest().apply {
        sortFields = arrayOf("id")
        sortDirection = Sort.Direction.ASC
        ownedByMyPartner = true
      }

    val spec = JobSpecification.buildSearchQuery(request, loggedInUser)
    val results = repo.findAll(spec)
    assertNotNull(results)
    assertTrue { results.isNotEmpty() }
    assertEquals(1, results.count())
    assertEquals(job.id, results.first().id)
  }

  @Test
  fun `test get owned by me and order by list`() {
    job =
      repo.save(
        getSalesforceJobOpp().apply {
          submissionList = savedList
          createdBy = loggedInUser
          contactUser = loggedInUser
        }
      )

    val request =
      SearchJobRequest().apply {
        sortFields = arrayOf("submissionList.id")
        sortDirection = Sort.Direction.ASC
        ownedByMe = true
      }
    val spec = JobSpecification.buildSearchQuery(request, loggedInUser)
    val results = repo.findAll(spec)
    assertNotNull(results)
    assertTrue { results.isNotEmpty() }
    assertEquals(1, results.count())
    assertEquals(job.id, results.first().id)
  }

  @Test
  fun `test get owned by me no logged in user`() {
    job =
      repo.save(
        getSalesforceJobOpp().apply {
          createdBy = loggedInUser
          contactUser = loggedInUser
        }
      )

    val request = SearchJobRequest().apply { ownedByMe = true }
    val spec = JobSpecification.buildSearchQuery(request, null)
    val results = repo.findAll(spec)
    assertNotNull(results)
    assertTrue { results.isEmpty() }
  }

  @Test
  fun `test unread messages`() {
    job =
      repo.save(
        getSalesforceJobOpp().apply {
          submissionList = savedList
          createdBy = loggedInUser
          contactUser = loggedInUser
        }
      )
    val loggedInUser = getSavedUser(userRepository)
    val c = getSavedCandidate(candidateRepo, loggedInUser)
    val jc =
      jobChatRepository.save(
        getJobChat().apply {
          candidate = c
          type = JobChatType.JobCreatorSourcePartner
          jobOpp = job
        }
      )
    getSavedJobChatUser(jcuRepository, c.user, jc)
    chatPostRepository.save(getChatPost().apply { jobChat = jc })

    //    candidateOpportunity = repo.save(getCandidateOpportunity().apply { candidate = c })

    val request = SearchJobRequest().apply { withUnreadMessages = true }
    val spec = JobSpecification.buildSearchQuery(request, loggedInUser)
    val results = repo.findAll(spec)
    assertNotNull(results)
    assertTrue { results.isNotEmpty() }
    assertEquals(1, results.count())
    assertEquals(job.id, results.first().id)
  }
}
