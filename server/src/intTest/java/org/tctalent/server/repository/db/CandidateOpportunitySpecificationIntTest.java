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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getCandidateOpportunity;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getChatPost;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getJobChat;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getPartner;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSalesforceJobOpp;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedCandidate;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedCandidateOpportunity;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedJobChatUser;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedPartner;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedSfJobOpp;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedUser;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.model.db.CandidateOpportunityStage;
import org.tctalent.server.model.db.ChatPost;
import org.tctalent.server.model.db.JobChat;
import org.tctalent.server.model.db.JobChatType;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.PartnerJobRelation;
import org.tctalent.server.model.db.PartnerJobRelationKey;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;
import org.tctalent.server.request.candidate.opportunity.SearchCandidateOpportunityRequest;
import org.tctalent.server.request.opportunity.OpportunityOwnershipType;

public class CandidateOpportunitySpecificationIntTest extends BaseDBIntegrationTest {

  @Autowired
  private CandidateOpportunityRepository repo;
  @Autowired
  private CandidateRepository candidateRepo;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private JobChatRepository jobChatRepository;
  @Autowired
  private JobChatUserRepository jcuRepository;
  @Autowired
  private SalesforceJobOppRepository sfJobOpportunityRepository;
  @Autowired
  private ChatPostRepository chatPostRepository;
  @Autowired
  private PartnerRepository partnerRepository;
  @Autowired
  private PartnerJobRelationRepository partnerJobRelationRepository;
  private CandidateOpportunity candidateOpportunity;
  private SearchCandidateOpportunityRequest request;
  private Specification<CandidateOpportunity> spec;

  @BeforeEach
  void setup() {
    assertTrue(isContainerInitialised());
    request = new SearchCandidateOpportunityRequest();
  }

  @Test
  void testKeyword() {
    candidateOpportunity = getSavedCandidateOpportunity(repo);

    request.setKeyword(candidateOpportunity.getName());
    spec = CandidateOpportunitySpecification.buildSearchQuery(
        request, null);
    List<CandidateOpportunity> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(candidateOpportunity.getId(), result.getFirst().getId());
  }

  @Test
  void testKeywordFail() {
    candidateOpportunity = getSavedCandidateOpportunity(repo);

    request.setKeyword("NOTHING");
    spec = CandidateOpportunitySpecification.buildSearchQuery(
        request, null);
    List<CandidateOpportunity> result = repo.findAll(spec);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void testStages() {
    candidateOpportunity = getSavedCandidateOpportunity(repo);

    request.setStages(List.of(CandidateOpportunityStage.cvPreparation));
    spec = CandidateOpportunitySpecification.buildSearchQuery(
        request, null);
    List<CandidateOpportunity> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(candidateOpportunity.getId(), result.getFirst().getId());
  }

  @Test
  void testStagesFilterActiveDontShow() {
    candidateOpportunity = getSavedCandidateOpportunity(repo);

    request.setActiveStages(null);
    request.setStages(List.of());
    spec = CandidateOpportunitySpecification.buildSearchQuery(
        request, null);
    List<CandidateOpportunity> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(candidateOpportunity.getId(), result.getFirst().getId());
  }

  @Test
  void testStagesFilterActiveShowNotClosed() {
    candidateOpportunity = getSavedCandidateOpportunity(repo);

    request.setActiveStages(true);
    request.setSfOppClosed(false);
    request.setStages(List.of());
    spec = CandidateOpportunitySpecification.buildSearchQuery(
        request, null);
    List<CandidateOpportunity> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(candidateOpportunity.getId(), result.getFirst().getId());
  }

  @Test
  void testStagesFilterActiveStageFalse() {
    candidateOpportunity = getSavedCandidateOpportunity(repo);

    request.setActiveStages(false);
    request.setStages(List.of());
    spec = CandidateOpportunitySpecification.buildSearchQuery(
        request, null);
    List<CandidateOpportunity> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(candidateOpportunity.getId(), result.getFirst().getId());
  }

  @Test
  void testStagesFilterActiveShowClosed() {
    candidateOpportunity = getSavedCandidateOpportunity(repo);

    candidateOpportunity.setClosed(true);
    repo.save(candidateOpportunity);

    request.setActiveStages(true);
    request.setSfOppClosed(true);
    request.setStages(List.of());
    spec = CandidateOpportunitySpecification.buildSearchQuery(
        request, null);
    List<CandidateOpportunity> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(candidateOpportunity.getId(), result.getFirst().getId());
  }

  @Test
  void testStagesFilterActiveAndShowClosedNotNull() {
    candidateOpportunity = getSavedCandidateOpportunity(repo);

    candidateOpportunity.setClosed(true);
    repo.save(candidateOpportunity);

    request.setActiveStages(true);
    request.setSfOppClosed(true);
    request.setStages(List.of());
    spec = CandidateOpportunitySpecification.buildSearchQuery(
        request, null);
    List<CandidateOpportunity> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(candidateOpportunity.getId(), result.getFirst().getId());
  }

  @Test
  void testOverdue() {
    candidateOpportunity = getSavedCandidateOpportunity(repo);

    candidateOpportunity.setNextStepDueDate(LocalDate.now().minusDays(10));
    repo.save(candidateOpportunity);

    request.setOverdue(true);
    spec = CandidateOpportunitySpecification.buildSearchQuery(
        request, null);
    List<CandidateOpportunity> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(candidateOpportunity.getId(), result.getFirst().getId());
  }

  @Test
  void testOverdueFalse() {

    request.setOverdue(false);
    spec = CandidateOpportunitySpecification.buildSearchQuery(
        request, null);
    List<CandidateOpportunity> result = repo.findAll(spec);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void testOverdueNull() {

    request.setOverdue(null);
    spec = CandidateOpportunitySpecification.buildSearchQuery(
        request, null);
    List<CandidateOpportunity> result = repo.findAll(spec);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void testUnreadMessagesIsNull() {

    request.setWithUnreadMessages(null);
    spec = CandidateOpportunitySpecification.buildSearchQuery(
        request, null);
    List<CandidateOpportunity> result = repo.findAll(spec);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void testUnreadMessages() {
    SalesforceJobOpp sfJobOpp = getSavedSfJobOpp(sfJobOpportunityRepository);
    User loggedInUser = getSavedUser(userRepository);
    Candidate c = getSavedCandidate(candidateRepo, loggedInUser);

    JobChat tmpJobChat = getJobChat();
    tmpJobChat.setCandidate(c);
    tmpJobChat.setType(JobChatType.CandidateRecruiting);
    tmpJobChat.setJobOpp(sfJobOpp);
    JobChat jc = jobChatRepository.save(tmpJobChat);

    getSavedJobChatUser(jcuRepository, c.getUser(), jc);
    ChatPost cp = getChatPost();
    cp.setJobChat(jc);
    chatPostRepository.save(cp);

    CandidateOpportunity co = getCandidateOpportunity();
    co.setCandidate(c);
    candidateOpportunity = repo.save(co);

    request.setWithUnreadMessages(true);
    spec = CandidateOpportunitySpecification.buildSearchQuery(
        request, loggedInUser);
    List<CandidateOpportunity> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(candidateOpportunity.getId(), result.getFirst().getId());
  }

  @Test
  void testNullUnreadMessages() {

    request.setWithUnreadMessages(null);
    spec = CandidateOpportunitySpecification.buildSearchQuery(
        request, null);
    List<CandidateOpportunity> result = repo.findAll(spec);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void testOwnershipTypeLoggedinUserNull() {

    request.setOwnershipType(OpportunityOwnershipType.AS_JOB_CREATOR);
    spec = CandidateOpportunitySpecification.buildSearchQuery(
        request, null);
    List<CandidateOpportunity> result = repo.findAll(spec);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void testOwnershipTypeLoggedinUserPartnerNull() {
    User u = getSavedUser(userRepository);
    u.setPartner(null);
    User loggedInUser = userRepository.save(u);

    request.setOwnershipType(OpportunityOwnershipType.AS_JOB_CREATOR);
    spec = CandidateOpportunitySpecification.buildSearchQuery(
        request, loggedInUser);
    List<CandidateOpportunity> result = repo.findAll(spec);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void testNullOwnershipType() {

    request.setOwnershipType(null);
    spec = CandidateOpportunitySpecification.buildSearchQuery(
        request, null);
    List<CandidateOpportunity> result = repo.findAll(spec);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void testOwnershipTypeJobOwnedByCreator() {
    PartnerImpl savedPartner = getSavedPartner(partnerRepository);
    SalesforceJobOpp sfjo = getSavedSfJobOpp(sfJobOpportunityRepository);
    sfjo.setJobCreator(savedPartner);
    SalesforceJobOpp sfJobOpp = sfJobOpportunityRepository.save(sfjo);

    User u = getSavedUser(userRepository);
    u.setPartner(savedPartner);
    User loggedInUser = userRepository.save(u);

    request.setOwnershipType(OpportunityOwnershipType.AS_JOB_CREATOR);
    request.setOwnedByMyPartner(true);

    CandidateOpportunity co = getCandidateOpportunity();
    co.setJobOpp(sfJobOpp);
    candidateOpportunity = repo.save(co);

    spec = CandidateOpportunitySpecification.buildSearchQuery(
        request, loggedInUser);
    List<CandidateOpportunity> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(candidateOpportunity.getId(), result.getFirst().getId());
  }

  @Test
  void testOwnershipTypeJobOwnedByMe() {
    User loggedInUser = getSavedUser(userRepository);

    SalesforceJobOpp sfJobOpp = getSalesforceJobOpp();
    sfJobOpp.setCreatedBy(loggedInUser);
    sfJobOpp.setContactUser(loggedInUser);
    sfJobOpp = sfJobOpportunityRepository.save(sfJobOpp);

    SearchCandidateOpportunityRequest request = new SearchCandidateOpportunityRequest();
    request.setOwnershipType(OpportunityOwnershipType.AS_JOB_CREATOR);
    request.setOwnedByMe(true);

    CandidateOpportunity candidateOpportunity = getCandidateOpportunity();
    candidateOpportunity.setJobOpp(sfJobOpp);
    candidateOpportunity = repo.save(candidateOpportunity);

    Specification<CandidateOpportunity> spec = CandidateOpportunitySpecification.buildSearchQuery(
        request, loggedInUser);
    List<CandidateOpportunity> result = repo.findAll(spec);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(candidateOpportunity.getId(), result.getFirst().getId());
  }

  @Test
  void testOwnershipTypeSourcePartnerOwnedByCreator() {
    PartnerImpl savedPartner = getPartner();
    savedPartner.setSourcePartner(true);
    savedPartner = partnerRepository.save(savedPartner);

    SalesforceJobOpp sfJobOpp = getSalesforceJobOpp();
    sfJobOpp.setJobCreator(savedPartner);
    sfJobOpp = sfJobOpportunityRepository.save(sfJobOpp);

    User loggedInUser = getSavedUser(userRepository);
    loggedInUser.setPartner(savedPartner);
    loggedInUser = userRepository.save(loggedInUser);
    Candidate c = getSavedCandidate(candidateRepo, loggedInUser);

    SearchCandidateOpportunityRequest request = new SearchCandidateOpportunityRequest();
    request.setOwnershipType(OpportunityOwnershipType.AS_SOURCE_PARTNER);
    request.setOwnedByMyPartner(true);

    CandidateOpportunity candidateOpportunity = getCandidateOpportunity();
    candidateOpportunity.setCandidate(c);
    candidateOpportunity.setJobOpp(sfJobOpp);

    candidateOpportunity = repo.save(
        candidateOpportunity);

    Specification<CandidateOpportunity> spec = CandidateOpportunitySpecification.buildSearchQuery(
        request, loggedInUser);
    List<CandidateOpportunity> result = repo.findAll(spec);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(candidateOpportunity.getId(), result.getFirst().getId());
  }


  @Test
  void testOwnershipTypeSourcePartnerOwnedByMe() {
    PartnerImpl savedPartner = getPartner();
    savedPartner.setSourcePartner(true);
    savedPartner = partnerRepository.save(savedPartner);

    User loggedInUser = getSavedUser(userRepository);
    loggedInUser.setPartner(savedPartner);
    loggedInUser = userRepository.save(loggedInUser);

    savedPartner.setDefaultContact(loggedInUser);
    savedPartner.setSourcePartner(true);
    savedPartner = partnerRepository.save(savedPartner);

    Candidate c = getSavedCandidate(candidateRepo, loggedInUser);
    SalesforceJobOpp sfJobOpp = getSalesforceJobOpp();
    sfJobOpp.setCreatedBy(loggedInUser);
    sfJobOpp.setContactUser(loggedInUser);
    sfJobOpp = sfJobOpportunityRepository.save(sfJobOpp);
    candidateOpportunity = getCandidateOpportunity();
    candidateOpportunity.setCandidate(c);
    candidateOpportunity.setJobOpp(sfJobOpp);
    candidateOpportunity = repo.save(candidateOpportunity);

    PartnerJobRelationKey pjrKey = new PartnerJobRelationKey();
    pjrKey.setTcJobId(sfJobOpp.getId());
    pjrKey.setPartnerId(((PartnerImpl) savedPartner).getId());

    PartnerJobRelation pjr = new PartnerJobRelation();
    pjr.setPartner(savedPartner);
    pjr.setJob(sfJobOpp);
    pjr.setContact(loggedInUser);
    pjr.setId(pjrKey);

    pjr = partnerJobRelationRepository.save(pjr);

    request.setOwnershipType(OpportunityOwnershipType.AS_SOURCE_PARTNER);
    request.setOwnedByMe(true);

    spec = CandidateOpportunitySpecification.buildSearchQuery(
        request, loggedInUser);
    List<CandidateOpportunity> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(candidateOpportunity.getId(), result.getFirst().getId());
  }
}
