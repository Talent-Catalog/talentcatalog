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
import static org.junit.jupiter.api.Assertions.fail;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getChatPost;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getJobChat;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedCandidate;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedJobChat;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedPartner;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedSfJobOpp;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedUser;

import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.ChatPost;
import org.tctalent.server.model.db.JobChat;
import org.tctalent.server.model.db.JobChatType;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;

public class JobChatRepositoryIntTest extends BaseDBIntegrationTest {

  @Autowired
  private JobChatRepository repo;
  @Autowired
  private CandidateRepository candidateRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private SalesforceJobOppRepository sfJobOppRepository;
  @Autowired
  private PartnerRepository partnerRepository;
  @Autowired
  private ChatPostRepository chatPostRepository;
  private Candidate testCandidate;
  private JobChat testJobChat;
  private SalesforceJobOpp sfJobOpp;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());

    testCandidate = getSavedCandidate(candidateRepository, getSavedUser(userRepository));
    sfJobOpp = getSavedSfJobOpp(sfJobOppRepository);
    PartnerImpl partner = getSavedPartner(partnerRepository);

    testJobChat = getJobChat();
    testJobChat.setJobOpp(sfJobOpp);
    testJobChat.setCandidate(testCandidate);
    testJobChat.setSourcePartner(partner);

    repo.save(testJobChat);
    assertTrue(testJobChat.getId() > 0);
  }

  @Test
  public void testFindByJobOppId() {
    List<JobChat> savedJobChat = repo.findByIds(List.of(testJobChat.getId()));
    assertNotNull(savedJobChat);
    assertFalse(savedJobChat.isEmpty());
    List<Long> ids = savedJobChat.stream().map(JobChat::getId).toList();
    assertTrue(ids.contains(testJobChat.getId()));
  }

  @Test
  public void testFindByJobOppIdFail() {
    List<JobChat> savedJobChat = repo.findByIds(List.of(testJobChat.getId() + 9009999));
    assertNotNull(savedJobChat);
    assertTrue(savedJobChat.isEmpty());
  }

  @Test
  public void testFindByTypeAndJob() {
    JobChat savedJobChat = repo.findByTypeAndJob(JobChatType.JobCreatorAllSourcePartners,
        sfJobOpp.getId());
    assertNotNull(savedJobChat);
    assertEquals(testCandidate.getWorkAbroadNotes(),
        savedJobChat.getCandidate() != null ? savedJobChat.getCandidate().getWorkAbroadNotes()
            : fail("Candidate is wrong"));
  }

  @Test
  public void testFindByTypeAndJobFail() {
    JobChat savedJobChat = repo.findByTypeAndJob(JobChatType.JobCreatorAllSourcePartners,
        sfJobOpp.getId() + 1);
    assertNull(savedJobChat);

    savedJobChat = repo.findByTypeAndJob(JobChatType.CandidateProspect, sfJobOpp.getId());
    assertNull(savedJobChat);
  }

  @Test
  public void testFindByIds() {
    JobChat jc1 = getSavedJobChat(repo);
    getSavedJobChat(repo); // This one won't be used in the search.
    JobChat jc3 = getSavedJobChat(repo);
    JobChat jc4 = getSavedJobChat(repo);
    List<JobChat> results = repo.findByIds(List.of(jc1.getId(), jc3.getId(), jc4.getId()));
    assertNotNull(results);
    assertFalse(results.isEmpty());
    assertEquals(3, results.size());
  }

  @Test
  public void testFindByTypeAndCandidate() {
    JobChat jobChatResult = repo.findByTypeAndCandidate(JobChatType.JobCreatorAllSourcePartners,
        testCandidate.getId());
    assertNotNull(jobChatResult);
    assertEquals(testJobChat.getId(), jobChatResult.getId());
  }

  @Test
  public void testFindByTypeAndCandidateFail() {
    JobChat jobChatResult = repo.findByTypeAndCandidate(JobChatType.CandidateProspect,
        testCandidate.getId());
    assertNull(jobChatResult);
  }

  @Test
  public void testFindByTypeAndCandidateAndJob() {
    JobChat jobChatResult = repo.findByTypeAndCandidateAndJob(
        JobChatType.JobCreatorAllSourcePartners,
        testCandidate.getId(),
        testJobChat.getJobOpp().getId()
    );
    assertNotNull(jobChatResult);
    assertEquals(testJobChat.getId(), jobChatResult.getId());
  }

  @Test
  public void testFindByTypeAndCandidateAndJobFail() {
    JobChat jobChatResult = repo.findByTypeAndCandidateAndJob(
        JobChatType.CandidateRecruiting,
        testCandidate.getId(),
        testJobChat.getJobOpp().getId()
    );
    assertNull(jobChatResult);

    jobChatResult = repo.findByTypeAndCandidateAndJob(
        JobChatType.JobCreatorAllSourcePartners,
        testCandidate.getId(),
        null
    );
    assertNull(jobChatResult);

    jobChatResult = repo.findByTypeAndCandidateAndJob(
        JobChatType.JobCreatorAllSourcePartners,
        null,
        testJobChat.getId()
    );
    assertNull(jobChatResult);
  }

  @Test
  public void testFindByTypeAndJobAndPartner() {
    assert testJobChat.getSourcePartner() != null;
    JobChat jobChatResult = repo.findByTypeAndJobAndPartner(
        JobChatType.JobCreatorAllSourcePartners,
        testJobChat.getJobOpp().getId(),
        testJobChat.getSourcePartner().getId()
    );
    assertNotNull(jobChatResult);
    assertEquals(testJobChat.getId(), jobChatResult.getId());
  }

  @Test
  public void testFindByTypeAndJobAndPartnerFail() {
    JobChat jobChatResult = repo.findByTypeAndJobAndPartner(
        JobChatType.JobCreatorAllSourcePartners,
        testJobChat.getJobOpp().getId(),
        null
    );
    assertNull(jobChatResult);
  }

  @Test
  public void testFindWithPostsSinceDate() {
    ChatPost cp = getChatPost();
    cp.setJobChat(testJobChat);
    chatPostRepository.save(cp);
    assertTrue(cp.getId() > 0);

    OffsetDateTime yesterday = OffsetDateTime.now().minusDays(1);
    List<Long> ids = repo.myFindChatsWithPostsSinceDate(yesterday);
    assertNotNull(ids);
    assertFalse(ids.isEmpty());
    assertEquals(1, ids.size());
  }

  @Test
  public void testFindWithPostsSinceDateFail() {
    OffsetDateTime yesterday = OffsetDateTime.now();
    List<Long> ids = repo.myFindChatsWithPostsSinceDate(yesterday);
    assertNotNull(ids);
    assertTrue(ids.isEmpty());
  }
}
