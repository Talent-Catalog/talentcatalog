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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedJobChat;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedJobChatUser;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedUser;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tctalent.server.model.db.JobChat;
import org.tctalent.server.model.db.JobChatUser;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;

public class JobChatUserRepositoryIntTest extends BaseDBIntegrationTest {

  @Autowired
  private JobChatUserRepository repo;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private JobChatRepository jobChatRepository;
  private JobChat jobChat;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());

    User user = getSavedUser(userRepository);
    jobChat = getSavedJobChat(jobChatRepository);
    getSavedJobChatUser(repo, user, jobChat);
  }

  @Test
  public void testDeleteByJobChatId() {
    repo.deleteByJobChatId(jobChat.getId());
    List<JobChatUser> users = repo.findAll();
    assertNotNull(users);
    assertTrue(users.isEmpty());
  }
}
