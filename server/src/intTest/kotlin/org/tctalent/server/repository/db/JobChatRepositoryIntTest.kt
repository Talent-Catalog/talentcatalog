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
  @Autowired private lateinit var jobChatRepository: JobChatRepository
  private lateinit var testCandidate: Candidate
  private lateinit var jobChat: JobChat
  private lateinit var sfJobOpp: SalesforceJobOpp

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }

    testCandidate = getSavedCandidate(candidateRepository, getSavedUser(userRepository))
    sfJobOpp = getSavedSalesforceJobOpp(sfJobOppRepository)

    jobChat =
      getJobChat().apply {
        jobOpp = sfJobOpp
        candidate = testCandidate
      }
    jobChatRepository.save(jobChat)
    assertTrue(jobChat.id > 0)
  }

  @Test
  fun `test find by job opp id`() {
    val savedJobChat = repo.findByIds(listOf(jobChat.id))
    assertNotNull(savedJobChat)
    assertTrue { savedJobChat.isNotEmpty() }
    val ids = savedJobChat.map { it.id }
    assertTrue { ids.contains(jobChat.id) }
  }

  @Test
  fun `test find by job opp id fail`() {
    val savedJobChat = repo.findByIds(listOf(jobChat.id + 9009999))
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
    val savedJobChat =
      repo.findByTypeAndJob(JobChatType.JobCreatorAllSourcePartners, sfJobOpp.id + 1)
    assertNull(savedJobChat)
  }

  @Test
  fun `test find by ids`() {
    fail("not implemented")
  }

  @Test
  fun `test find by type and candidate`() {
    fail("not implemented")
  }

  @Test
  fun `test find by type and candidate and job`() {
    fail("not implemented")
  }

  @Test
  fun `test find by type and job and partner`() {
    fail("not implemented")
  }

  @Test
  fun `test find with posts since date`() {
    fail("not implemented")
  }
}
