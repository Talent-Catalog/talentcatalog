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

package org.tctalent.server.repository.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getChatPost;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getReaction;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedJobChat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tctalent.server.model.db.ChatPost;
import org.tctalent.server.model.db.JobChat;
import org.tctalent.server.model.db.Reaction;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;

public class ReactionRepositoryIntTest extends BaseDBIntegrationTest {

  @Autowired
  private ReactionRepository repository;
  @Autowired
  private ChatPostRepository chatPostRepository;
  @Autowired
  private JobChatRepository jobChatRepository;
  private ChatPost testChatPost;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());

    JobChat testJobChat = getSavedJobChat(jobChatRepository);
    testChatPost = getChatPost();
    testChatPost.setJobChat(testJobChat);
    chatPostRepository.save(testChatPost);
    assertTrue(testChatPost.getId() > 0);

    Reaction reaction = getReaction();
    reaction.setChatPost(testChatPost);
    repository.save(reaction);
    assertNotNull(reaction.getId());
    assertTrue(reaction.getId() > 0);
  }

  @Test
  public void testFindByChatPostId() {
    List<Reaction> savedReaction = repository.findBychatPostId(testChatPost.getId()).orElse(null);
    assertNotNull(savedReaction);
    assertFalse(savedReaction.isEmpty(), "Empty list.");
    List<Long> ids = savedReaction.stream().map(reaction -> reaction.getChatPost().getId())
        .toList();
    assertTrue(ids.contains(testChatPost.getId()), "Id not in the list");
  }

  @Test
  public void testFindByChatPostIdFail() {
    List<Reaction> savedReaction = repository.findBychatPostId(0L).orElse(null);
    assertNotNull(savedReaction);
    assertTrue(savedReaction.isEmpty());
  }

  @Test
  public void testFindByEmojiAndChatPostId() {
    Reaction savedReaction = repository.findByEmojiAndChatPostId("Smile", testChatPost.getId())
        .orElse(null);
    assertNotNull(savedReaction);
    assertEquals("Smile", savedReaction.getEmoji());
  }

  @Test
  public void testFindByEmojiAndChatPostIdFail() {
    Reaction savedReaction = repository.findByEmojiAndChatPostId("", 0L).orElse(null);
    assertNull(savedReaction);
  }
}
