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
import org.tctalent.server.model.db.CandidateOpportunity
import org.tctalent.server.model.db.SalesforceJobOpp
import org.tctalent.server.repository.db.integrationhelp.*

class CandidateOpportunityRepositoryIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repo: CandidateOpportunityRepository
  @Autowired private lateinit var userRepo: UserRepository
  @Autowired private lateinit var candidateRepo: CandidateRepository
  @Autowired private lateinit var sfJobOppRepository: SalesforceJobOppRepository
  @Autowired private lateinit var partnerRepository: PartnerRepository
  @Autowired private lateinit var jobChatRepository: JobChatRepository
  @Autowired private lateinit var chatPostRepository: ChatPostRepository
  @Autowired private lateinit var jobChatUserRepository: JobChatUserRepository

  private lateinit var testCandidate: Candidate
  private lateinit var candidateOpportunity: CandidateOpportunity
  private lateinit var sfJobOpp: SalesforceJobOpp

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    val savedPartner = getPartner().apply { id = 999999L }
    partnerRepository.saveAndFlush(savedPartner)
    assertTrue { savedPartner.id > 0 }

    // something weird going on here with the partner and user....works this way
    // but not when putting savedpartner on the user from above.
    val newP = partnerRepository.findAll()
    newP.forEach { println(it) }
    val toUse = newP.filter { it.abbreviation == "GTP" }.first()

    val user = getUser().apply { partner = toUse }
    userRepo.save(user)

    testCandidate = getSavedCandidate(candidateRepo, user)
    sfJobOpp = getSalesforceJobOpp().apply { createdBy = user }
    sfJobOppRepository.save(sfJobOpp)

    val savedJobChat = getSavedJobChat(jobChatRepository)
    val chatPost = getChatPost().apply { jobChat = savedJobChat }
    chatPostRepository.save(chatPost)
    assertTrue { chatPost.id > 0 }
    val jobChatUser = getSavedJobChatUser(jobChatUserRepository, user, savedJobChat)
    jobChatUser.apply { lastReadPost = chatPost }
    jobChatUserRepository.save(jobChatUser)

    candidateOpportunity =
      getCandidateOpportunity().apply {
        jobOpp = sfJobOpp
        candidate = testCandidate
        sfId = sfJobOpp.sfId
      }
    repo.save(candidateOpportunity)
    assertTrue { candidateOpportunity.id > 0 }
  }

  @Test
  fun `find by sf id`() {
    val candidateOpp = repo.findBySfId(sfJobOpp.sfId).getOrNull()
    assertNotNull(candidateOpp)
    assertEquals(candidateOpp.id, candidateOpp.id)
  }

  @Test
  fun `find by sf id fail`() {
    val candidateOpp = repo.findBySfId(null).getOrNull()
    assertNull(candidateOpp)
  }

  @Test
  fun `find by candidate id and job id`() {
    val opp = repo.findByCandidateIdAndJobId(testCandidate.id, sfJobOpp.id)
    assertNotNull(opp)
    assertEquals(candidateOpportunity.id, opp.id)
  }

  @Test
  fun `find by candidate id and job id fail`() {
    val opp = repo.findByCandidateIdAndJobId(testCandidate.id, 999999999L)
    assertNull(opp)
  }

  @Test
  fun `find partner opps`() {
    val opp = repo.findPartnerOpps(sfJobOpp.createdBy.partner.id)
    assertNotNull(opp)
    assertTrue { opp.isNotEmpty() }
    assertEquals(1, opp.size)
    assertEquals(candidateOpportunity.id, opp.first().id)
  }

  @Test
  fun `find partner opps fail`() {
    val opp = repo.findPartnerOpps(999999999L)
    assertNotNull(opp)
    assertTrue { opp.isEmpty() }
  }
}
