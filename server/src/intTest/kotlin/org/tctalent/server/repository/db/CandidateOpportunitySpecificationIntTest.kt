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
import kotlin.test.*
import org.springframework.beans.factory.annotation.Autowired
import org.tctalent.server.model.db.CandidateOpportunity
import org.tctalent.server.model.db.CandidateOpportunityStage
import org.tctalent.server.model.db.JobChatType
import org.tctalent.server.model.db.PartnerJobRelation
import org.tctalent.server.model.db.PartnerJobRelationKey
import org.tctalent.server.repository.db.integrationhelp.*
import org.tctalent.server.request.candidate.opportunity.SearchCandidateOpportunityRequest
import org.tctalent.server.request.opportunity.OpportunityOwnershipType

/**
 * This spec seems to return 1=1 when no criteria are produced, always returning something. So won't
 * initialise by default in the setup.
 */
class CandidateOpportunitySpecificationIntTest : BaseDBIntegrationTest() {
  @Autowired private lateinit var repo: CandidateOpportunityRepository
  @Autowired private lateinit var candidateRepo: CandidateRepository
  @Autowired private lateinit var jobChatRepository: JobChatRepository
  @Autowired private lateinit var userRepository: UserRepository
  @Autowired private lateinit var jcuRepository: JobChatUserRepository
  @Autowired private lateinit var sfJobOpportunityRepository: SalesforceJobOppRepository
  @Autowired private lateinit var chatPostRepository: ChatPostRepository
  @Autowired private lateinit var partnerRepository: PartnerRepository
  @Autowired private lateinit var partnerJobRelationRepository: PartnerJobRelationRepository
  private lateinit var candidateOpportunity: CandidateOpportunity

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
  }

  @Test
  fun `test keyword`() {
    candidateOpportunity = getSavedCandidateOpportunity(repo)

    val request = SearchCandidateOpportunityRequest().apply { keyword = candidateOpportunity.name }
    val spec = CandidateOpportunitySpecification.buildSearchQuery(request, null)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(candidateOpportunity.id, result.first().id)
  }

  @Test
  fun `test keyword fail`() {
    candidateOpportunity = getSavedCandidateOpportunity(repo)

    val request = SearchCandidateOpportunityRequest().apply { keyword = "NOTHING" }
    val spec = CandidateOpportunitySpecification.buildSearchQuery(request, null)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isEmpty() }
  }

  @Test
  fun `test stages`() {
    candidateOpportunity = getSavedCandidateOpportunity(repo)

    val request =
      SearchCandidateOpportunityRequest().apply {
        stages = listOf(CandidateOpportunityStage.cvPreparation)
      }
    val spec = CandidateOpportunitySpecification.buildSearchQuery(request, null)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(candidateOpportunity.id, result.first().id)
  }

  @Test
  fun `test stages filter  active don't show`() {
    candidateOpportunity = getSavedCandidateOpportunity(repo)

    val request =
      SearchCandidateOpportunityRequest().apply {
        activeStages = null
        stages = emptyList()
      }
    val spec = CandidateOpportunitySpecification.buildSearchQuery(request, null)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(candidateOpportunity.id, result.first().id)
  }

  @Test
  fun `test stages filter active show not closed`() {
    candidateOpportunity = getSavedCandidateOpportunity(repo)

    val request =
      SearchCandidateOpportunityRequest().apply {
        activeStages = true
        sfOppClosed = false
        stages = emptyList()
      }
    val spec = CandidateOpportunitySpecification.buildSearchQuery(request, null)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(candidateOpportunity.id, result.first().id)
  }

  @Test
  fun `test stages filter active stage false`() {
    candidateOpportunity = getSavedCandidateOpportunity(repo)

    val request =
      SearchCandidateOpportunityRequest().apply {
        activeStages = false
        stages = emptyList()
      }
    val spec = CandidateOpportunitySpecification.buildSearchQuery(request, null)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(candidateOpportunity.id, result.first().id)
  }

  @Test
  fun `test stages filter active show closed`() {
    candidateOpportunity = getSavedCandidateOpportunity(repo)

    repo.save(candidateOpportunity.apply { isClosed = true })
    val request =
      SearchCandidateOpportunityRequest().apply {
        activeStages = true
        sfOppClosed = true
        stages = emptyList()
      }
    val spec = CandidateOpportunitySpecification.buildSearchQuery(request, null)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(candidateOpportunity.id, result.first().id)
  }

  @Test
  fun `test stages filter active and show closed not null`() {
    candidateOpportunity = getSavedCandidateOpportunity(repo)

    repo.save(candidateOpportunity.apply { isClosed = true })
    val request =
      SearchCandidateOpportunityRequest().apply {
        activeStages = true
        sfOppClosed = true
        stages = emptyList()
      }
    val spec = CandidateOpportunitySpecification.buildSearchQuery(request, null)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(candidateOpportunity.id, result.first().id)
  }

  @Test
  fun `test overdue`() {
    candidateOpportunity = getSavedCandidateOpportunity(repo)

    repo.save(candidateOpportunity.apply { nextStepDueDate = LocalDate.now().minusDays(10) })
    val request = SearchCandidateOpportunityRequest().apply { overdue = true }
    val spec = CandidateOpportunitySpecification.buildSearchQuery(request, null)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(candidateOpportunity.id, result.first().id)
  }

  @Test
  fun `test overdue false`() {
    val request = SearchCandidateOpportunityRequest().apply { overdue = false }
    val spec = CandidateOpportunitySpecification.buildSearchQuery(request, null)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isEmpty() }
  }

  @Test
  fun `test overdue null`() {
    val request = SearchCandidateOpportunityRequest().apply { overdue = null }
    val spec = CandidateOpportunitySpecification.buildSearchQuery(request, null)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isEmpty() }
  }

  @Test
  fun `test unread messages is null`() {
    val request = SearchCandidateOpportunityRequest().apply { withUnreadMessages = null }
    val spec = CandidateOpportunitySpecification.buildSearchQuery(request, null)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isEmpty() }
  }

  @Test
  fun `test unread messages`() {
    val sfJobOpp = getSavedSfJobOpp(sfJobOpportunityRepository)
    val loggedInUser = getSavedUser(userRepository)
    val c = getSavedCandidate(candidateRepo, loggedInUser)
    val jc =
      jobChatRepository.save(
        getJobChat().apply {
          candidate = c
          type = JobChatType.CandidateRecruiting
          jobOpp = sfJobOpp
        }
      )
    getSavedJobChatUser(jcuRepository, c.user, jc)
    chatPostRepository.save(getChatPost().apply { jobChat = jc })

    candidateOpportunity = repo.save(getCandidateOpportunity().apply { candidate = c })

    val request = SearchCandidateOpportunityRequest().apply { withUnreadMessages = true }
    val spec = CandidateOpportunitySpecification.buildSearchQuery(request, loggedInUser)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(candidateOpportunity.id, result.first().id)
  }

  @Test
  fun `test null unread messages`() {
    val request = SearchCandidateOpportunityRequest().apply { withUnreadMessages = null }
    val spec = CandidateOpportunitySpecification.buildSearchQuery(request, null)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isEmpty() }
  }

  @Test
  fun `test ownership type loggedinuser null`() {
    val request =
      SearchCandidateOpportunityRequest().apply {
        ownershipType = OpportunityOwnershipType.AS_JOB_CREATOR
      }
    val spec = CandidateOpportunitySpecification.buildSearchQuery(request, null)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isEmpty() }
  }

  @Test
  fun `test ownership type loggedinuser partner null`() {
    val loggedInUser = userRepository.save(getSavedUser(userRepository).apply { partner = null })
    val request =
      SearchCandidateOpportunityRequest().apply {
        ownershipType = OpportunityOwnershipType.AS_JOB_CREATOR
      }
    val spec = CandidateOpportunitySpecification.buildSearchQuery(request, loggedInUser)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isEmpty() }
  }

  @Test
  fun `test null ownership type`() {
    val request = SearchCandidateOpportunityRequest().apply { ownershipType = null }
    val spec = CandidateOpportunitySpecification.buildSearchQuery(request, null)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isEmpty() }
  }

  @Test
  fun `test ownership type job owned by creator`() {
    val savedPartner = getSavedPartner(partnerRepository)
    val sfJobOpp =
      sfJobOpportunityRepository.save(getSalesforceJobOpp().apply { jobCreator = savedPartner })

    val loggedInUser =
      userRepository.save(getSavedUser(userRepository).apply { partner = savedPartner })
    val request =
      SearchCandidateOpportunityRequest().apply {
        ownershipType = OpportunityOwnershipType.AS_JOB_CREATOR
        ownedByMyPartner = true
      }
    candidateOpportunity = repo.save(getCandidateOpportunity().apply { jobOpp = sfJobOpp })
    val spec = CandidateOpportunitySpecification.buildSearchQuery(request, loggedInUser)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(candidateOpportunity.id, result.first().id)
  }

  @Test
  fun `test ownership type job owned by me`() {
    val loggedInUser = getSavedUser(userRepository)
    val sfJobOpp =
      sfJobOpportunityRepository.save(
        getSalesforceJobOpp().apply {
          createdBy = loggedInUser
          contactUser = loggedInUser
        }
      )

    val request =
      SearchCandidateOpportunityRequest().apply {
        ownershipType = OpportunityOwnershipType.AS_JOB_CREATOR
        ownedByMe = true
      }
    candidateOpportunity = repo.save(getCandidateOpportunity().apply { jobOpp = sfJobOpp })
    val spec = CandidateOpportunitySpecification.buildSearchQuery(request, loggedInUser)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(candidateOpportunity.id, result.first().id)
  }

  @Test
  fun `test ownership type source partner owned by creator`() {
    val savedPartner = partnerRepository.save(getPartner().apply { isSourcePartner = true })
    val sfJobOpp =
      sfJobOpportunityRepository.save(getSalesforceJobOpp().apply { jobCreator = savedPartner })

    val loggedInUser =
      userRepository.save(getSavedUser(userRepository).apply { partner = savedPartner })
    val c = getSavedCandidate(candidateRepo, loggedInUser)
    val request =
      SearchCandidateOpportunityRequest().apply {
        ownershipType = OpportunityOwnershipType.AS_SOURCE_PARTNER
        ownedByMyPartner = true
      }
    candidateOpportunity =
      repo.save(
        getCandidateOpportunity().apply {
          candidate = c
          jobOpp = sfJobOpp
        }
      )
    val spec = CandidateOpportunitySpecification.buildSearchQuery(request, loggedInUser)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(candidateOpportunity.id, result.first().id)
  }

  @Test
  fun `test ownership type source partner owned by me`() {
    var savedPartner = partnerRepository.save(getPartner().apply { isSourcePartner = true })

    val loggedInUser =
      userRepository.save(getSavedUser(userRepository).apply { partner = savedPartner })

    savedPartner =
      partnerRepository.save(
        savedPartner.apply {
          defaultContact = loggedInUser
          isSourcePartner = true
        }
      )
    val c = getSavedCandidate(candidateRepo, loggedInUser)
    val sfJobOpp =
      sfJobOpportunityRepository.save(
        getSalesforceJobOpp().apply {
          createdBy = loggedInUser
          contactUser = loggedInUser
        }
      )

    candidateOpportunity =
      repo.save(
        getCandidateOpportunity().apply {
          candidate = c
          jobOpp = sfJobOpp
        }
      )

    val pjrKey =
      PartnerJobRelationKey().apply {
        tcJobId = sfJobOpp.id
        partnerId = savedPartner.id
      }
    val pjr =
      partnerJobRelationRepository.save(
        PartnerJobRelation().apply {
          id = pjrKey
          partner = savedPartner
          job = sfJobOpp
          contact = loggedInUser
        }
      )

    val request =
      SearchCandidateOpportunityRequest().apply {
        ownershipType = OpportunityOwnershipType.AS_SOURCE_PARTNER
        ownedByMe = true
      }

    val spec = CandidateOpportunitySpecification.buildSearchQuery(request, loggedInUser)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(candidateOpportunity.id, result.first().id)
  }
}
