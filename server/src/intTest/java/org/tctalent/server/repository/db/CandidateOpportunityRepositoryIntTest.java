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

import static org.junit.jupiter.api.Assertions.*;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getCandidateOpportunity;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getChatPost;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getPartner;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSalesforceJobOpp;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedCandidate;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedJobChat;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedJobChatUser;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getUser;

import java.util.List;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.model.db.ChatPost;
import org.tctalent.server.model.db.JobChat;
import org.tctalent.server.model.db.JobChatType;
import org.tctalent.server.model.db.JobChatUser;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class CandidateOpportunityRepositoryIntTest extends BaseDBIntegrationTest {

  @Autowired
  private CandidateOpportunityRepository repo;
  @Autowired
  private UserRepository userRepo;
  @Autowired
  private CandidateRepository candidateRepo;
  @Autowired
  private SalesforceJobOppRepository sfJobOppRepository;
  @Autowired
  private PartnerRepository partnerRepository;
  @Autowired
  private JobChatRepository jobChatRepository;
  @Autowired
  private ChatPostRepository chatPostRepository;
  @Autowired
  private JobChatUserRepository jobChatUserRepository;
  private JobChatUser testjobChatUser;
  private Candidate testCandidate;
  private CandidateOpportunity candidateOpportunity;
  private SalesforceJobOpp sfJobOpp;
  private User user;

  @BeforeEach
  void setup() {
    assertTrue(isContainerInitialised());
    PartnerImpl savedPartner = getPartner();
    savedPartner.setId(999999L);
    partnerRepository.saveAndFlush(savedPartner);
    assertTrue(savedPartner.getId() > 0);

    PartnerImpl newP = partnerRepository.findAll().stream()
        .filter(p -> "GTP".equals(p.getAbbreviation()))
        .findFirst()
        .orElse(null);

    user = getUser();
    user.setPartner(newP);
    userRepo.save(user);

    testCandidate = getSavedCandidate(candidateRepo, user);
    sfJobOpp = getSalesforceJobOpp();
    sfJobOpp.setCreatedBy(user);
    sfJobOppRepository.save(sfJobOpp);

    JobChat savedJobChat = getSavedJobChat(jobChatRepository);
    ChatPost chatPost = getChatPost();
    chatPost.setJobChat(savedJobChat);
    chatPostRepository.save(chatPost);
    assertTrue(chatPost.getId() > 0);

    testjobChatUser = getSavedJobChatUser(jobChatUserRepository, user, savedJobChat);
    testjobChatUser.setLastReadPost(chatPost);
    jobChatUserRepository.save(testjobChatUser);

    candidateOpportunity = getCandidateOpportunity();
    candidateOpportunity.setJobOpp(sfJobOpp);
    candidateOpportunity.setCandidate(testCandidate);
    candidateOpportunity.setSfId(sfJobOpp.getSfId());
    repo.save(candidateOpportunity);
    assertTrue(candidateOpportunity.getId() > 0);
  }

  @Test
  void findBySfId() {
    CandidateOpportunity candidateOpp = repo.findBySfId(sfJobOpp.getSfId()).orElse(null);
    assertNotNull(candidateOpp);
    assertEquals(candidateOpportunity.getId(), candidateOpp.getId());
  }

  @Test
  void findBySfIdFail() {
    CandidateOpportunity candidateOpp = repo.findBySfId(null).orElse(null);
    assertNull(candidateOpp);
  }

  @Test
  void findByCandidateIdAndJobId() {
    CandidateOpportunity opp = repo.findByCandidateIdAndJobId(testCandidate.getId(),
        sfJobOpp.getId());
    assertNotNull(opp);
    assertEquals(candidateOpportunity.getId(), opp.getId());
  }

  @Test
  void findByCandidateIdAndJobIdFail() {
    CandidateOpportunity opp = repo.findByCandidateIdAndJobId(testCandidate.getId(), 999999999L);
    assertNull(opp);
  }

  @Test
  void findPartnerOpps() {
    var opp = repo.findPartnerOpps(sfJobOpp.getCreatedBy().getPartner().getId());
    assertNotNull(opp);
    assertFalse(opp.isEmpty());
    assertEquals(1, opp.size());
    assertEquals(candidateOpportunity.getId(), opp.getFirst().getId());
  }

  @Test
  void findPartnerOppsFail() {
    var opp = repo.findPartnerOpps(999999999L);
    assertNotNull(opp);
    assertTrue(opp.isEmpty());
  }

  @Test
  void testFindUnreadChatsInOpps() {
    JobChat testJobChat = getSavedJobChat(jobChatRepository);
    testJobChat.setJobOpp(sfJobOpp);
    testJobChat.setType(JobChatType.CandidateRecruiting);
    testJobChat.setCandidate(testCandidate);

    ChatPost testChatPost = getChatPost();
    testChatPost.setJobChat(testJobChat);
    chatPostRepository.save(testChatPost);
    assertTrue(testChatPost.getId() > 0);

    getSavedJobChatUser(jobChatUserRepository, user, testJobChat);
    repo.save(candidateOpportunity);

    var ids = repo.findUnreadChatsInOpps(testjobChatUser.getUser().getId(),
        List.of(candidateOpportunity.getId()));
    assertNotNull(ids);
    assertFalse(ids.isEmpty());
  }
}
