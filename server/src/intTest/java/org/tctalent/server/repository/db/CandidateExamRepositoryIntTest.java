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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getCandidateExam;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedCandidate;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedUser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateExam;
import org.tctalent.server.model.db.Exam;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;

public class CandidateExamRepositoryIntTest extends BaseDBIntegrationTest {

  @Autowired
  private CandidateExamRepository repo;
  @Autowired
  private CandidateRepository candidateRepository;
  @Autowired
  private UserRepository userRepository;
  private CandidateExam candidateExam;
  private Candidate testCandidate;

  @BeforeEach
  void setup() {
    assertTrue(isContainerInitialised());
    User savedUser = getSavedUser(userRepository);
    testCandidate = getSavedCandidate(candidateRepository, savedUser);
    candidateExam = getCandidateExam();
    candidateExam.setCandidate(testCandidate);
    repo.save(candidateExam);
    assertTrue(candidateExam.getId() > 0);
  }

  @Test
  void testFindByIdLoadCandidate() {
    var ce = repo.findByIdLoadCandidate(candidateExam.getId()).orElse(null);
    assertNotNull(ce);
    assertEquals(candidateExam.getId(), ce.getId());
  }

  @Test
  void testFindByIdLoadCandidateFail() {
    var ce = repo.findByIdLoadCandidate(99999999L).orElse(null);
    assertNull(ce);
  }

  @Test
  void testFindDuplicateByExamType() {
    var ce = repo.findDuplicateByExamType(Exam.OET, testCandidate.getId(), 9999999L).orElse(null);
    assertNotNull(ce);
    assertEquals(candidateExam.getId(), ce.getId());
  }

  @Test
  void testFindDuplicateByExamTypeFailExam() {
    var ce = repo.findDuplicateByExamType(Exam.IELTSGen, testCandidate.getId(),
        candidateExam.getId()).orElse(null);
    assertNull(ce);
  }

  @Test
  void testFindDuplicateByExamTypeFailCandidate() {
    var ce = repo.findDuplicateByExamType(Exam.OET, 99999L, candidateExam.getId()).orElse(null);
    assertNull(ce);
  }

  @Test
  void testFindDuplicateByExamTypeFailId() {
    var ce = repo.findDuplicateByExamType(Exam.OET, testCandidate.getId(), candidateExam.getId())
        .orElse(null);
    assertNull(ce);
  }
}
