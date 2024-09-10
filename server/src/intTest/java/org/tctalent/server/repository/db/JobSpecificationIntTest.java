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
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getChatPost;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getJobChat;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSalesforceJobOpp;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedCandidate;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedCountry;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedJobChatUser;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedPartner;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedSavedList;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedUser;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.ChatPost;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.JobChat;
import org.tctalent.server.model.db.JobChatType;
import org.tctalent.server.model.db.JobOpportunityStage;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;
import org.tctalent.server.request.job.SearchJobRequest;

public class JobSpecificationIntTest extends BaseDBIntegrationTest {

  @Autowired
  private SalesforceJobOppRepository repo;
  @Autowired
  private SavedListRepository savedListRepository;
  @Autowired
  private CountryRepository countryRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private PartnerRepository partnerRepository;
  @Autowired
  private JobChatRepository jobChatRepository;
  @Autowired
  private JobChatUserRepository jcuRepository;
  @Autowired
  private ChatPostRepository chatPostRepository;
  @Autowired
  private CandidateRepository candidateRepo;
  private SalesforceJobOpp job;
  private SavedList savedList;
  private Country savedCountry;
  private User loggedInUser;
  private SearchJobRequest request;
  private Specification<SalesforceJobOpp> spec;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());

    PartnerImpl savedPartner = getSavedPartner(partnerRepository);
    loggedInUser = getSavedUser(userRepository);
    loggedInUser.setPartner(savedPartner);
    userRepository.save(loggedInUser);
    savedList = getSavedSavedList(savedListRepository);
    savedCountry = getSavedCountry(countryRepository);

    request = new SearchJobRequest();
  }

  @Test
  public void testKeywordWithNothingName() {
    job = getSalesforceJobOpp();
    repo.save(job);
    request.setKeyword("NOTHING");
    spec = JobSpecification.buildSearchQuery(request, null);
    List<SalesforceJobOpp> results = repo.findAll(spec);
    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  @Test
  public void testKeywordCaseInsensitive() {
    job = getSalesforceJobOpp();
    job.setSubmissionList(savedList);
    repo.save(job);

    request.setKeyword(job.getName().toUpperCase());
    spec = JobSpecification.buildSearchQuery(request, null);
    List<SalesforceJobOpp> results = repo.findAll(spec);
    assertNotNull(results);
    assertFalse(results.isEmpty());
    assertEquals(1, results.size());
    assertEquals(job.getId(), results.getFirst().getId());
  }

  @Test
  public void testStages() {
    job = getSalesforceJobOpp();
    job.setSubmissionList(savedList);
    job.setStage(JobOpportunityStage.cvPreparation);
    repo.save(job);

    request.setStages(Collections.singletonList(JobOpportunityStage.cvPreparation));
    spec = JobSpecification.buildSearchQuery(request, null);
    List<SalesforceJobOpp> results = repo.findAll(spec);
    assertNotNull(results);
    assertFalse(results.isEmpty());
    assertEquals(1, results.size());
    assertEquals(job.getId(), results.getFirst().getId());
  }

  @Test
  public void testStagesFail() {
    job = getSalesforceJobOpp();
    job.setSubmissionList(savedList);
    repo.save(job);

    request.setStages(List.of(JobOpportunityStage.noJobOffer));
    spec = JobSpecification.buildSearchQuery(request, null);
    List<SalesforceJobOpp> results = repo.findAll(spec);
    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  @Test
  public void testDestinations() {
    job = getSalesforceJobOpp();
    job.setSubmissionList(savedList);
    job.setCountry(savedCountry);
    repo.save(job);

    Long countryId = savedCountry.getId();
    request.setDestinationIds(Collections.singletonList(countryId));
    spec = JobSpecification.buildSearchQuery(request, null);
    List<SalesforceJobOpp> results = repo.findAll(spec);
    assertNotNull(results);
    assertFalse(results.isEmpty());
    assertEquals(1, results.size());
    assertEquals(job.getId(), results.getFirst().getId());
  }

  @Test
  public void testClosed() {
    job = getSalesforceJobOpp();
    job.setSubmissionList(savedList);
    job.setClosed(true);
    repo.save(job);

    SearchJobRequest request = new SearchJobRequest();
    request.setSfOppClosed(true);

    spec = JobSpecification.buildSearchQuery(request, null);
    List<SalesforceJobOpp> results = repo.findAll(spec);
    assertNotNull(results);
    assertFalse(results.isEmpty());
    assertEquals(1, results.size());
    assertEquals(job.getId(), results.getFirst().getId());
  }

  @Test
  public void testGetStarred() {
    job = getSalesforceJobOpp();
    job.setSubmissionList(savedList);
    job.setStarringUsers(Set.of(loggedInUser));
    repo.save(job);

    request.setStarred(true);

    spec = JobSpecification.buildSearchQuery(request, loggedInUser);
    List<SalesforceJobOpp> results = repo.findAll(spec);
    assertNotNull(results);
    assertFalse(results.isEmpty());
    assertEquals(1, results.size());
    assertEquals(job.getId(), results.getFirst().getId());
  }

  @Test
  public void testGetStarredLoggedInUserNull() {
    job = getSalesforceJobOpp();
    job.setStarringUsers(Set.of(loggedInUser));
    repo.save(job);

    request.setStarred(true);
    spec = JobSpecification.buildSearchQuery(request, null);
    List<SalesforceJobOpp> results = repo.findAll(spec);
    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  @Test
  public void testGetOwnedByMyPartner() {
    job = getSalesforceJobOpp();
    job.setSubmissionList(savedList);
    job.setCreatedBy(loggedInUser);
    repo.save(job);

    request.setSortFields(new String[]{"id"});
    request.setSortDirection(Sort.Direction.ASC);
    request.setOwnedByMyPartner(true);

    spec = JobSpecification.buildSearchQuery(request, loggedInUser);
    List<SalesforceJobOpp> results = repo.findAll(spec);
    assertNotNull(results);
    assertFalse(results.isEmpty());
    assertEquals(1, results.size());
    assertEquals(job.getId(), results.getFirst().getId());
  }

  @Test
  public void testGetOwnedByMeAndOrderByList() {
    job = getSalesforceJobOpp();
    job.setSubmissionList(savedList);
    job.setCreatedBy(loggedInUser);
    job.setContactUser(loggedInUser);
    repo.save(job);

    request.setSortFields(new String[]{"submissionList.id"});
    request.setSortDirection(Sort.Direction.ASC);
    request.setOwnedByMe(true);

    spec = JobSpecification.buildSearchQuery(request, loggedInUser);
    List<SalesforceJobOpp> results = repo.findAll(spec);
    assertNotNull(results);
    assertFalse(results.isEmpty());
    assertEquals(1, results.size());
    assertEquals(job.getId(), results.getFirst().getId());
  }

  @Test
  public void testGetOwnedByMeNoLoggedInUser() {
    job = getSalesforceJobOpp();
    job.setContactUser(loggedInUser);
    job.setCreatedBy(loggedInUser);
    repo.save(job);

    request.setOwnedByMe(true);
    spec = JobSpecification.buildSearchQuery(request, null);
    List<SalesforceJobOpp> results = repo.findAll(spec);
    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  @Test
  public void testUnreadMessages() {
    job = getSalesforceJobOpp();
    job.setSubmissionList(savedList);
    job.setCreatedBy(loggedInUser);
    job.setContactUser(loggedInUser);
    repo.save(job);

    User loggedInUser = getSavedUser(userRepository);
    Candidate c = getSavedCandidate(candidateRepo, loggedInUser);

    JobChat jc = getJobChat();
    jc.setCandidate(c);
    jc.setType(JobChatType.JobCreatorSourcePartner);
    jc.setJobOpp(job);
    jobChatRepository.save(jc);

    getSavedJobChatUser(jcuRepository, c.getUser(), jc);
    ChatPost cp = getChatPost();
    cp.setJobChat(jc);
    chatPostRepository.save(cp);

    request.setWithUnreadMessages(true);
    spec = JobSpecification.buildSearchQuery(request, loggedInUser);
    List<SalesforceJobOpp> results = repo.findAll(spec);
    assertNotNull(results);
    assertFalse(results.isEmpty());
    assertEquals(1, results.size());
    assertEquals(job.getId(), results.getFirst().getId());
  }
}
