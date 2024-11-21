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
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSalesforceJobOpp;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedCandidate;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedJobChat;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedJobChatUser;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedSavedList;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedUser;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.ChatPost;
import org.tctalent.server.model.db.JobChat;
import org.tctalent.server.model.db.JobChatUser;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;

public class SalesforceJobOppRepositoryIntTest extends BaseDBIntegrationTest {

  @Autowired
  private SalesforceJobOppRepository repo;
  @Autowired
  private SavedListRepository savedListRepository;
  @Autowired
  private JobChatUserRepository jobChatUserRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private JobChatRepository jobChatRepository;
  @Autowired
  private ChatPostRepository chatPostRepository;
  @Autowired
  private CandidateRepository candidateRepository;
  private SalesforceJobOpp sfJobOpp;
  private SavedList savedList;
  private User savedUser;
  private Candidate testCandidate;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());

    savedUser = getSavedUser(userRepository);
    testCandidate = getSavedCandidate(candidateRepository, getSavedUser(userRepository));

    JobChat savedJobChat = getSavedJobChat(jobChatRepository);
    ChatPost chatPost = getChatPost();
    chatPost.setJobChat(savedJobChat);
    chatPostRepository.save(chatPost);
    assertTrue(chatPost.getId() > 0);

    JobChatUser jobChatUser = getSavedJobChatUser(jobChatUserRepository, savedUser, savedJobChat);
    jobChatUser.setLastReadPost(chatPost);
    jobChatUserRepository.save(jobChatUser);

    savedList = getSavedSavedList(savedListRepository);
    sfJobOpp = getSalesforceJobOpp();
    sfJobOpp.setSubmissionList(savedList);
    repo.save(sfJobOpp);
    assertTrue(sfJobOpp.getId() > 0);
  }

  @Test
  public void testFindBySfId() {
    SalesforceJobOpp savedOpp = repo.findBySfId(sfJobOpp.getSfId()).orElse(null);
    assertNotNull(savedOpp);
    assertEquals(savedOpp.getDescription(), sfJobOpp.getDescription());
  }

  @Test
  public void testFindBySfIdFail() {
    SalesforceJobOpp savedOpp = repo.findBySfId(sfJobOpp.getSfId() + "00").orElse(null);
    assertNull(savedOpp);
  }

  @Test
  public void testGetJobBySubmissionList() {
    SalesforceJobOpp savedJobOpp = repo.getJobBySubmissionList(savedList);
    assertNotNull(savedJobOpp);
    assertEquals(savedJobOpp.getDescription(), sfJobOpp.getDescription());
  }

  @Test
  public void testGetJobBySubmissionListFail() {
    SavedList newSL = getSavedSavedList(savedListRepository);
    SalesforceJobOpp savedJobOpp = repo.getJobBySubmissionList(newSL);
    assertNull(savedJobOpp);
  }

  @Test
  public void testFindUnreadChatsInOpps() {
    User newUser = getSavedUser(userRepository);

    SalesforceJobOpp sfJobOpp2 = getSalesforceJobOpp();
    sfJobOpp2.setSubmissionList(savedList);
    repo.save(sfJobOpp2);
    assertTrue(sfJobOpp2.getId() > 0);

    SalesforceJobOpp sfJobOpp3 = getSalesforceJobOpp();
    sfJobOpp3.setSubmissionList(savedList);
    repo.save(sfJobOpp3);
    assertTrue(sfJobOpp3.getId() > 0);

    JobChat savedJobChat1 = getSavedJobChat(jobChatRepository);
    savedJobChat1.setJobOpp(sfJobOpp2);
    savedJobChat1.setCandidate(testCandidate);
    jobChatRepository.save(savedJobChat1);
    assertTrue(savedJobChat1.getId() > 0);

    JobChat savedJobChat2 = getSavedJobChat(jobChatRepository);
    savedJobChat2.setJobOpp(sfJobOpp3);
    savedJobChat2.setCandidate(testCandidate);
    jobChatRepository.save(savedJobChat2);
    assertTrue(savedJobChat2.getId() > 0);

    ChatPost testChatPost = getChatPost();
    testChatPost.setJobChat(savedJobChat1);
    chatPostRepository.save(testChatPost);
    assertTrue(testChatPost.getId() > 0);

    ChatPost testChatPost2 = getChatPost();
    testChatPost2.setJobChat(savedJobChat2);
    chatPostRepository.save(testChatPost2);
    assertTrue(testChatPost2.getId() > 0);

    getSavedJobChatUser(jobChatUserRepository, newUser, savedJobChat1);
    getSavedJobChatUser(jobChatUserRepository, newUser, savedJobChat2);

    List<Long> savedOpp = repo.findUnreadChatsInOpps(newUser.getId(),
        List.of(sfJobOpp.getId(), sfJobOpp2.getId(), sfJobOpp3.getId()));
    assertNotNull(savedOpp);
    assertFalse(savedOpp.isEmpty());
    assertEquals(2, savedOpp.size());
  }

  @Test
  public void testFindUnreadChatsInOppsFail() {
    List<Long> savedOpp = repo.findUnreadChatsInOpps(999999L, Collections.emptySet());
    assertNotNull(savedOpp);
    assertTrue(savedOpp.isEmpty(), "EmptySet and wrong user id should be empty.");

    savedOpp = repo.findUnreadChatsInOpps(999999L, List.of(sfJobOpp.getId()));
    assertTrue(savedOpp.isEmpty(), "Wrong user Id with OK id.");

    savedOpp = repo.findUnreadChatsInOpps(savedUser.getId(), Collections.emptySet());
    assertTrue(savedOpp.isEmpty(), "Good user but wrong id should be empty.");
  }
}
