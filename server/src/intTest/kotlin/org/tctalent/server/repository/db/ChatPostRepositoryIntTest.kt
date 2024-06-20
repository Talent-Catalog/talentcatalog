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
import org.tctalent.server.model.db.ChatPost
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest
import org.tctalent.server.repository.db.integrationhelp.getChatPost

class ChatPostRepositoryIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repo: ChatPostRepository
  @Autowired lateinit var jobChatRepository: JobChatRepository
  private lateinit var chatPost: ChatPost

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    chatPost = getChatPost()
    repo.save(chatPost)
    assertTrue { chatPost.id > 0 }
  }

  @Test
  fun `test find last chat post`() {
    val savedPostId = repo.findLastChatPost(chatPost.jobChat.id)
    assertNotNull(savedPostId)
    assertEquals(chatPost.id, savedPostId)
  }

  @Test
  fun `test find last chat post fail no id`() {
    val savedPostId = repo.findLastChatPost(0)
    assertNull(savedPostId)
  }

  @Test
  fun `test delete by job chat id`() {
    repo.deleteByJobChatId(chatPost.jobChat.id)
    val saved = repo.findById(chatPost.jobChat.id).getOrNull()
    assertNull(saved)
  }

  @Test
  fun `test find by job chat id`() {
    val found = repo.findByJobChatId(chatPost.jobChat.id).getOrNull()
    assertNotNull(found)
    val idList = found.map { it.id }
    assertTrue { idList.contains(chatPost.id) }
  }

  @Test
  fun `test find by job chat id fail`() {
    val found = repo.findByJobChatId(99999999L).getOrNull()
    assertNotNull(found)
  }
}
