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
import org.tctalent.server.repository.db.integrationhelp.getReaction
import org.tctalent.server.repository.db.integrationhelp.getSavedChatPost

class ReactionRepositoryIntTest : BaseDBIntegrationTest() {
  @Autowired private lateinit var repository: ReactionRepository
  @Autowired private lateinit var chatPostRepository: ChatPostRepository
  private lateinit var testChatPost: ChatPost

  @BeforeTest
  fun setup() {
    testChatPost = getSavedChatPost(chatPostRepository)
    val reaction = getReaction()
    reaction.apply { chatPost = testChatPost }
    repository.save(reaction)
    assertNotNull(reaction.id)
    assertTrue { reaction.id > 0 }
  }

  @Test
  fun `test find by chat post id`() {
    assertTrue { isContainerInitialized() }

    val savedReaction = repository.findBychatPostId(testChatPost.id).getOrNull()
    assertNotNull(savedReaction)
    assertTrue(savedReaction.isNotEmpty(), "Empty list.")
    val ids = savedReaction.map { it.chatPost.id }
    assertTrue(ids.contains(testChatPost.id), "Id not in the list")
  }

  @Test
  fun `test find by chat post id fail`() {
    assertTrue { isContainerInitialized() }

    val savedReaction = repository.findBychatPostId(0L).getOrNull()
    assertNotNull(savedReaction)
    assertTrue { savedReaction.isEmpty() }
  }

  @Test
  fun `test find by emoji and chat post id`() {
    assertTrue { isContainerInitialized() }

    val savedReaction = repository.findByEmojiAndChatPostId("Smile", testChatPost.id).getOrNull()
    assertNotNull(savedReaction)
    assertEquals("Smile", savedReaction.emoji)
  }

  @Test
  fun `test find by emoji and chat post id fail`() {
    assertTrue { isContainerInitialized() }

    val savedReaction = repository.findByEmojiAndChatPostId("", 0L).getOrNull()
    assertNull(savedReaction)
  }
}
