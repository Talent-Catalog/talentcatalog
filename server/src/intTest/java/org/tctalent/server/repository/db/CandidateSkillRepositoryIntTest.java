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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getCandidateSkill;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedCandidate;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedUser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateSkill;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;

public class CandidateSkillRepositoryIntTest extends BaseDBIntegrationTest {

  @Autowired
  private CandidateSkillRepository repo;
  @Autowired
  private CandidateRepository candidateRepository;
  @Autowired
  private UserRepository userRepository;
  private CandidateSkill candidateSkill;
  private Candidate testCandidate;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());
    testCandidate = getSavedCandidate(candidateRepository, getSavedUser(userRepository));
    candidateSkill = getCandidateSkill();
    candidateSkill.setCandidate(testCandidate);
    repo.save(candidateSkill);
    assertTrue(candidateSkill.getId() > 0);
  }

  @Test
  public void testFindByCandidateId() {
    Page<CandidateSkill> candidateSkills = repo.findByCandidateId(testCandidate.getId(),
        Pageable.unpaged());
    assertNotNull(candidateSkills);
    assertEquals(1, candidateSkills.getContent().size());
    assertEquals(candidateSkill.getId(), candidateSkills.getContent().getFirst().getId());
  }
}
