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
import org.tctalent.server.model.db.JobChat
import org.tctalent.server.model.db.JobChatUser
import org.tctalent.server.model.db.User
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest
import org.tctalent.server.repository.db.integrationhelp.getSavedJobChat
import org.tctalent.server.repository.db.integrationhelp.getSavedJobChatUser
import org.tctalent.server.repository.db.integrationhelp.getSavedUser

class JobChatUserRepositoryIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repo: JobChatUserRepository
  @Autowired lateinit var userRepository: UserRepository
  @Autowired lateinit var jobChatRepository: JobChatRepository
  private lateinit var jobChatUser: JobChatUser
  private lateinit var user: User
  private lateinit var jobChat: JobChat

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }

    user = getSavedUser(userRepository)
    jobChat = getSavedJobChat(jobChatRepository)
    jobChatUser = getSavedJobChatUser(repo, user, jobChat)
  }

  @Test
  fun `test delete by job chat id`() {
    repo.deleteByJobChatId(jobChat.id)
    val result = repo.findAll()
    assertNotNull(result)
    assertTrue { result.isEmpty() }
  }
}
