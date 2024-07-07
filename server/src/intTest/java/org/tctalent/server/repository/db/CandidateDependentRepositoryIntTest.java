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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getCandidateDependent;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedCandidate;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedUser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateDependant;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;

public class CandidateDependentRepositoryIntTest extends BaseDBIntegrationTest {

  @Autowired
  CandidateDependantRepository repo;
  @Autowired
  UserRepository userRepo;
  @Autowired
  CandidateRepository candidateRepo;
  private Candidate testCandidate;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());
    testCandidate = getSavedCandidate(candidateRepo, getSavedUser(userRepo));

    CandidateDependant cd1 = getCandidateDependent();
    cd1.setCandidate(testCandidate);
    repo.save(cd1);
    assertTrue(cd1.getId() > 0);

    CandidateDependant cd2 = getCandidateDependent();
    cd2.setCandidate(testCandidate);
    repo.save(cd2);
    assertTrue(cd2.getId() > 0);
  }

  @Test
  public void testCountByCandidateId() {
    Long count = repo.countByCandidateId(testCandidate.getId());
    assertEquals(2, count);
  }
}
