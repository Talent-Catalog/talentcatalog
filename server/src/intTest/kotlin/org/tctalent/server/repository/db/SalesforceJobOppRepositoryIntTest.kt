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
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.tctalent.server.model.db.SalesforceJobOpp
import org.tctalent.server.model.db.SavedList
import org.tctalent.server.model.db.User
import org.tctalent.server.repository.db.integrationhelp.*

class SalesforceJobOppRepositoryIntTest : BaseDBIntegrationTest() {
  @Autowired private lateinit var repo: SalesforceJobOppRepository
  @Autowired private lateinit var savedListRepository: SavedListRepository
  @Autowired private lateinit var jobChatUserRepository: JobChatUserRepository
  @Autowired private lateinit var userRepository: UserRepository
  @Autowired private lateinit var jobChatRepository: JobChatRepository
  @Autowired private lateinit var chatPostRepository: ChatPostRepository
  private lateinit var sfJobOpp: SalesforceJobOpp
  private lateinit var savedList: SavedList
  private lateinit var savedUser: User

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }

    savedUser = getSavedUser(userRepository)
    val savedJobChat = getSavedJobChat(jobChatRepository)
    val savedChatPost = getSavedChatPost(chatPostRepository)
    val jobChatUser = getSavedJobChatUser(jobChatUserRepository, savedUser, savedJobChat)
    jobChatUser.apply { lastReadPost = savedChatPost }
    jobChatUserRepository.save(jobChatUser)

    assertTrue { isContainerInitialized() }
    savedList = getSavedList(savedListRepository)
    sfJobOpp = getSalesforceJobOpp().apply { submissionList = savedList }
    repo.save(sfJobOpp)
    assertTrue { sfJobOpp.id > 0 }
  }

  @Test
  fun `test find by sf id`() {
    val savedOpp = repo.findBySfId(sfJobOpp.sfId).getOrNull()
    assertNotNull(savedOpp)
    assertEquals(savedOpp.description, sfJobOpp.description)
  }

  @Test
  fun `test find by sf id fail`() {
    val savedOpp = repo.findBySfId(sfJobOpp.sfId + "00").getOrNull()
    assertNull(savedOpp)
  }

  @Test
  fun `test get job by submission list`() {
    val savedJobOpp = repo.getJobBySubmissionList(savedList)
    assertNotNull(savedJobOpp)
    assertEquals(savedJobOpp.description, sfJobOpp.description)
  }

  @Test
  fun `test get job by submission list fail`() {
    val newSL = getSavedList(savedListRepository)
    val savedJobOpp = repo.getJobBySubmissionList(newSL)
    assertNull(savedJobOpp)
  }

  /*
   * Create a second job opp here to know it works properly with multiple items in a list. It should return 1 using the max(id) in the qry.
   */
  @Test
  fun `test find unread chats in opps`() {
    // Different user here.
    val newUser = getSavedUser(userRepository)
    val sfJobOpp2 = getSalesforceJobOpp().apply { submissionList = savedList }
    repo.save(sfJobOpp2)
    val sfJobOpp3 = getSalesforceJobOpp().apply { submissionList = savedList }
    repo.save(sfJobOpp3)

    // Create some saved chats
    val savedJobChat1 = getSavedJobChat(jobChatRepository).apply { jobOpp = sfJobOpp2 }
    jobChatRepository.save(savedJobChat1)
    val savedJobChat2 = getSavedJobChat(jobChatRepository).apply { jobOpp = sfJobOpp3 }
    jobChatRepository.save(savedJobChat2)

    // Create job chat user links
    getSavedJobChatUser(jobChatUserRepository, newUser, savedJobChat1)
    getSavedJobChatUser(jobChatUserRepository, newUser, savedJobChat2)

    val savedOpp =
      repo.findUnreadChatsInOpps(newUser.id, listOf(sfJobOpp.id, sfJobOpp2.id, sfJobOpp3.id))

    assertNotNull(savedOpp)
    assertTrue { savedOpp.isNotEmpty() }
    assertEquals(1, savedOpp.size)
  }

  @Test
  fun `test find unread chats in opps fail`() {
    var savedOpp = repo.findUnreadChatsInOpps(999999L, emptySet())
    assertNotNull(savedOpp)
    assertTrue("EmptySet and wrong user id should be empty.") { savedOpp.isEmpty() }
    savedOpp = repo.findUnreadChatsInOpps(999999L, listOf(sfJobOpp.id))
    assertTrue("Wrong user Id with OK id.") { savedOpp.isEmpty() }
    savedOpp = repo.findUnreadChatsInOpps(savedUser.id, emptySet())
    assertTrue("Good user but wrong id should be empty.") { savedOpp.isEmpty() }
  }
}
