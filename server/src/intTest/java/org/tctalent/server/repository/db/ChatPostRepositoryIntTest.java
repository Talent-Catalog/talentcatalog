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
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedJobChat;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tctalent.server.model.db.ChatPost;
import org.tctalent.server.model.db.JobChat;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;

public class ChatPostRepositoryIntTest extends BaseDBIntegrationTest {

  @Autowired
  private ChatPostRepository repo;
  @Autowired
  private JobChatRepository jobChatRepository;
  private ChatPost chatPost;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());

    JobChat testJobChat = getSavedJobChat(jobChatRepository);
    chatPost = getChatPost();
    chatPost.setJobChat(testJobChat);
    repo.save(chatPost);
    assertTrue(chatPost.getId() > 0);
  }

  @Test
  public void testFindLastChatPost() {
    Long savedPostId = repo.findLastChatPost(chatPost.getJobChat().getId());
    assertNotNull(savedPostId);
    assertEquals(chatPost.getId(), savedPostId);
  }

  @Test
  public void testFindLastChatPostFailNoId() {
    Long savedPostId = repo.findLastChatPost(0L);
    assertNull(savedPostId);
  }

  @Test
  public void testDeleteByJobChatId() {
    repo.deleteByJobChatId(chatPost.getJobChat().getId());
    Optional<ChatPost> saved = repo.findById(chatPost.getJobChat().getId());
    assertFalse(saved.isPresent());
  }

  @Test
  public void testFindByJobChatId() {
    Optional<List<ChatPost>> found = repo.findByJobChatId(chatPost.getJobChat().getId());
    assertTrue(found.isPresent());
    List<Long> idList = found.get().stream().map(ChatPost::getId).toList();
    assertTrue(idList.contains(chatPost.getId()));
  }

  @Test
  public void testFindByJobChatIdFail() {
    Optional<List<ChatPost>> found = repo.findByJobChatId(99999999L);
    assertTrue(found.isPresent());
    assertTrue(found.get().isEmpty());
  }
}
