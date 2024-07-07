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

import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateEducation;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getCandidateEducation;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedCandidate;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedUser;

public class CandidateEducationRepositoryIntTest extends BaseDBIntegrationTest {

  @Autowired
  private CandidateEducationRepository repo;
  @Autowired
  private CandidateRepository candidateRepository;
  @Autowired
  private UserRepository userRepository;
  private CandidateEducation candidateEducation;
  private Candidate testCandidate;

  @BeforeEach
  void setup() {
    assertTrue(isContainerInitialised());
    User savedUser = getSavedUser(userRepository);
    testCandidate = getSavedCandidate(candidateRepository, savedUser);
    candidateEducation = getCandidateEducation();
    candidateEducation.setCandidate(testCandidate);
    repo.save(candidateEducation);
    assertTrue(candidateEducation.getId() > 0);
  }

  @Test
  void testFindByIdLoadCandidate() {
    var result = repo.findByIdLoadCandidate(candidateEducation.getId()).orElse(null);
    assertNotNull(result);
    assertEquals(candidateEducation.getId(), result.getId());
  }

  @Test
  void testFindByIdLoadCandidateFail() {
    var result = repo.findByIdLoadCandidate(9999L).orElse(null);
    assertNull(result);
  }

  @Test
  void testFindByCandidateId() {
    var results = repo.findByCandidateId(testCandidate.getId());
    assertNotNull(results);
    assertFalse(results.isEmpty());
    assertEquals(candidateEducation.getId(), results.getFirst().getId());
  }

  @Test
  void testFindByCandidateIdFail() {
    var results = repo.findByCandidateId(99999L);
    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  @Test
  void testFindByIdAndCandidateId() {
    var results = repo.findByIdAndCandidateId(candidateEducation.getId(), testCandidate.getId());
    assertNotNull(results);
    assertEquals(candidateEducation.getId(), results.getId());
  }

  @Test
  void testFindByIdAndCandidateIdFailCandidate() {
    var results = repo.findByIdAndCandidateId(candidateEducation.getId(), 9999L);
    assertNull(results);
  }

  @Test
  void testFindByIdAndCandidateIdFailId() {
    var results = repo.findByIdAndCandidateId(9999L, testCandidate.getId());
    assertNull(results);
  }
}
