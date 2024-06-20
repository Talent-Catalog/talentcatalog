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

import java.time.OffsetDateTime
import kotlin.test.*
import org.springframework.beans.factory.annotation.Autowired
import org.tctalent.server.model.db.Candidate
import org.tctalent.server.model.db.JobChat
import org.tctalent.server.model.db.JobChatType
import org.tctalent.server.model.db.SalesforceJobOpp
import org.tctalent.server.repository.db.integrationhelp.*

class JobChatRepositoryIntTest : BaseDBIntegrationTest() {
  @Autowired private lateinit var repo: JobChatRepository
  @Autowired private lateinit var candidateRepository: CandidateRepository
  @Autowired private lateinit var userRepository: UserRepository
  @Autowired private lateinit var sfJobOppRepository: SalesforceJobOppRepository
  @Autowired private lateinit var partnerRepository: PartnerRepository
  @Autowired private lateinit var chatPostRepository: ChatPostRepository
  private lateinit var testCandidate: Candidate
  private lateinit var testJobChat: JobChat
  private lateinit var sfJobOpp: SalesforceJobOpp

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }

    testCandidate = getSavedCandidate(candidateRepository, getSavedUser(userRepository))
    sfJobOpp = getSavedSalesforceJobOpp(sfJobOppRepository)
    val partner = getSavedPartner(partnerRepository)

    testJobChat =
      getJobChat().apply {
        jobOpp = sfJobOpp
        candidate = testCandidate
        sourcePartner = partner
      }
    repo.save(testJobChat)
    assertTrue(testJobChat.id > 0)
  }

  @Test
  fun `test find by job opp id`() {
    val savedJobChat = repo.findByIds(listOf(testJobChat.id))
    assertNotNull(savedJobChat)
    assertTrue { savedJobChat.isNotEmpty() }
    val ids = savedJobChat.map { it.id }
    assertTrue { ids.contains(testJobChat.id) }
  }

  @Test
  fun `test find by job opp id fail`() {
    val savedJobChat = repo.findByIds(listOf(testJobChat.id + 9009999))
    assertNotNull(savedJobChat)
    assertTrue { savedJobChat.isEmpty() }
  }

  @Test
  fun `test find by type and job`() {
    val savedJobChat = repo.findByTypeAndJob(JobChatType.JobCreatorAllSourcePartners, sfJobOpp.id)
    assertNotNull(savedJobChat)
    assertEquals(
      testCandidate.workAbroadNotes,
      savedJobChat.candidate?.workAbroadNotes ?: fail("Candidate is wrong"),
    )
  }

  @Test
  fun `test find by type and job fail`() {
    var savedJobChat =
      repo.findByTypeAndJob(JobChatType.JobCreatorAllSourcePartners, sfJobOpp.id + 1)
    assertNull(savedJobChat)

    savedJobChat = repo.findByTypeAndJob(JobChatType.CandidateProspect, sfJobOpp.id)
    assertNull(savedJobChat)
  }

  @Test
  fun `test find by ids`() {
    val jc1 = getSavedJobChat(repo)
    getSavedJobChat(repo) // This one won't be used in the search.
    val jc3 = getSavedJobChat(repo)
    val jc4 = getSavedJobChat(repo)
    val results = repo.findByIds(listOf(jc1.id, jc3.id, jc4.id))
    assertNotNull(results)
    assertTrue(results.isNotEmpty())
    assertEquals(3, results.count())
  }

  @Test
  fun `test find by type and candidate`() {
    val jobChatResult =
      repo.findByTypeAndCandidate(JobChatType.JobCreatorAllSourcePartners, testCandidate.id)
    assertNotNull(jobChatResult)
    assertEquals(testJobChat.id, jobChatResult.id)
  }

  @Test
  fun `test find by type and candidate fail`() {
    val jobChatResult = repo.findByTypeAndCandidate(JobChatType.CandidateProspect, testCandidate.id)
    assertNull(jobChatResult)
  }

  @Test
  fun `test find by type and candidate and job`() {
    val jobChatResult =
      repo.findByTypeAndCandidateAndJob(
        JobChatType.JobCreatorAllSourcePartners,
        testCandidate.id,
        testJobChat.jobOpp.id,
      )
    assertNotNull(jobChatResult)
    assertEquals(testJobChat.id, jobChatResult.id)
  }

  @Test
  fun `test find by type and candidate and job fail`() {
    var jobChatResult =
      repo.findByTypeAndCandidateAndJob(
        JobChatType.CandidateRecruiting,
        testCandidate.id,
        testJobChat.jobOpp.id,
      )
    assertNull(jobChatResult)

    jobChatResult =
      repo.findByTypeAndCandidateAndJob(
        JobChatType.JobCreatorAllSourcePartners,
        testCandidate.id,
        null,
      )
    assertNull(jobChatResult)

    jobChatResult =
      repo.findByTypeAndCandidateAndJob(
        JobChatType.JobCreatorAllSourcePartners,
        null,
        testJobChat.id,
      )
    assertNull(jobChatResult)
  }

  @Test
  fun `test find by type and job and partner`() {
    val jobChatResult =
      repo.findByTypeAndJobAndPartner(
        JobChatType.JobCreatorAllSourcePartners,
        testJobChat.jobOpp.id,
        testJobChat.sourcePartner?.id,
      )
    assertNotNull(jobChatResult)
    assertEquals(testJobChat.id, jobChatResult.id)
  }

  @Test
  fun `test find by type and job and partner fail`() {
    val jobChatResult =
      repo.findByTypeAndJobAndPartner(
        JobChatType.JobCreatorAllSourcePartners,
        testJobChat.jobOpp.id,
        null,
      )
    assertNull(jobChatResult)
  }

  @Test
  fun `test find with posts since date`() {

    val cp = getChatPost().apply { jobChat = testJobChat }
    chatPostRepository.save(cp)
    assertTrue { cp.id > 0 }

    val yesterday = OffsetDateTime.now().minusDays(1)
    val ids = repo.myFindChatsWithPostsSinceDate(yesterday)
    assertNotNull(ids)
    assertTrue { ids.isNotEmpty() }
    assertEquals(1, ids.count())
  }

  @Test
  fun `test find with posts since date fail`() {
    val yesterday = OffsetDateTime.now()
    val ids = repo.myFindChatsWithPostsSinceDate(yesterday)
    assertNotNull(ids)
    assertTrue { ids.isEmpty() }
  }
}
