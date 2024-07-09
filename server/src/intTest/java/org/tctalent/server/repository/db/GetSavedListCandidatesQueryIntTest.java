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
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getCandidateSavedList;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedCandidate;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedList;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedSfJobOpp;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedUser;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.model.db.CandidateSavedList;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;
import org.tctalent.server.request.candidate.SavedListGetRequest;

public class GetSavedListCandidatesQueryIntTest extends BaseDBIntegrationTest {

  @Autowired
  private CandidateSavedListRepository repo;
  @Autowired
  private SavedListRepository savedListRepository;
  @Autowired
  private CandidateRepository candidateRepository;
  @Autowired
  private SalesforceJobOppRepository salesforceJobOppRepository;
  @Autowired
  private UserRepository userRepository;
  private SavedList testSavedList;
  private Candidate testCandidate;
  private SavedListGetRequest request;
  private GetSavedListCandidatesQuery spec;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());
    SalesforceJobOpp testSfJobOpp = getSavedSfJobOpp(salesforceJobOppRepository);
    testCandidate = getSavedCandidate(candidateRepository, getSavedUser(userRepository));

    testSavedList = getSavedList();
    testSavedList.setSfJobOpp(testSfJobOpp);
    savedListRepository.save(testSavedList);

    CandidateOpportunity co = getCandidateOpportunity();
    co.setJobOpp(testSfJobOpp);
    co.setCandidate(testCandidate);

    List<CandidateOpportunity> candidateOpportunities = new ArrayList<>();
    candidateOpportunities.add(co);
    testCandidate.setCandidateOpportunities(candidateOpportunities);
    testCandidate.addSavedList(testSavedList);
    candidateRepository.save(testCandidate);

    CandidateSavedList candidateSavedList = getCandidateSavedList(testCandidate, testSavedList);
    repo.save(candidateSavedList);

    assertNotNull(candidateSavedList.getId());

    request = new SavedListGetRequest();
  }

  @Test
  public void testCandidateSavedListsQuery() {
    spec = new GetSavedListCandidatesQuery(testSavedList, new SavedListGetRequest());
    List<Candidate> result = candidateRepository.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(testCandidate.getId(), result.getFirst().getId());
  }

  @Test
  public void testCandidateSavedListsQueryWithKeyword() {

    request.setKeyword(testCandidate.getCandidateNumber());
    spec = new GetSavedListCandidatesQuery(testSavedList, request);
    List<Candidate> result = candidateRepository.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(testCandidate.getId(), result.getFirst().getId());
  }

  @Test
  public void testCandidateSavedListsQueryWithNoMatchingKeyword() {
    request.setKeyword("NOTHING");
    spec = new GetSavedListCandidatesQuery(testSavedList, request);
    List<Candidate> result = candidateRepository.findAll(spec);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testCandidateSavedListsQueryWithJobOpp() {
    spec = new GetSavedListCandidatesQuery(testSavedList, new SavedListGetRequest());
    List<Candidate> result = candidateRepository.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(testCandidate.getId(), result.getFirst().getId());
  }

  @Test
  public void testCandidateSavedListsQueryWithClosedOpps() {
    request.setShowClosedOpps(false);
    spec = new GetSavedListCandidatesQuery(testSavedList, request);
    List<Candidate> result = candidateRepository.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(testCandidate.getId(), result.getFirst().getId());
  }

  @Test
  public void testCandidateSavedListsQueryWithNoMatchingCandidate() {
    SavedList nonExistentSavedList = getSavedList();
    spec = new GetSavedListCandidatesQuery(nonExistentSavedList, new SavedListGetRequest());
    List<Candidate> result = candidateRepository.findAll(spec);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }
}
