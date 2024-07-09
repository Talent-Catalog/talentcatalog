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
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getCandidateSavedList;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedCandidate;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedSavedList;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedUser;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateSavedList;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;

public class GetCandidateSavedListsQueryIntTest extends BaseDBIntegrationTest {

  @Autowired
  private CandidateSavedListRepository repo;
  @Autowired
  private SavedListRepository savedListRepository;
  @Autowired
  private CandidateRepository candidateRepository;
  @Autowired
  private UserRepository userRepository;
  private SavedList testSavedList;
  private Candidate testCandidate;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());
    testCandidate = getSavedCandidate(candidateRepository, getSavedUser(userRepository));
    testSavedList = getSavedSavedList(savedListRepository);

    CandidateSavedList candidateSavedList = getCandidateSavedList(testCandidate, testSavedList);
    repo.save(candidateSavedList);
    assertNotNull(candidateSavedList.getId());
  }

  @Test
  public void testCandidateSavedListsQuery() {
    GetCandidateSavedListsQuery spec = new GetCandidateSavedListsQuery(testCandidate.getId());
    List<SavedList> result = savedListRepository.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(testSavedList.getId(), result.getFirst().getId());
  }

  @Test
  public void testCandidateSavedListsQueryWithNoMatchingCandidate() {
    GetCandidateSavedListsQuery spec = new GetCandidateSavedListsQuery(-1L);
    List<SavedList> result = savedListRepository.findAll(spec);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }
}
