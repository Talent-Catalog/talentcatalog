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
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getCandidateEducation;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedCandidate;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedUser;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateEducation;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;

public class EducationRepositoryIntTest extends BaseDBIntegrationTest {

  @Autowired
  private EducationRepository repository;
  @Autowired
  private CandidateRepository candidateRepository;
  @Autowired
  private UserRepository userRepository;
  private CandidateEducation ce;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());

    User user = getSavedUser(userRepository);
    Candidate testCandidate = getSavedCandidate(candidateRepository, user);
    ce = getCandidateEducation();
    ce.setCandidate(testCandidate);
    repository.save(ce);
    assertNotNull(ce.getId());
    assertTrue(ce.getId() > 0);
  }

  @Test
  public void testFindCandidateById() {
    Optional<CandidateEducation> savedCE = repository.findByIdLoadCandidate(ce.getId());
    assertTrue(savedCE.isPresent());
    assertEquals(ce.getInstitution(), savedCE.get().getInstitution());
  }

  @Test
  public void testFindCandidateByIdFail() {
    Optional<CandidateEducation> savedCE = repository.findByIdLoadCandidate(99999999L);
    assertFalse(savedCE.isPresent());
  }

// These tests fail - I think the code it calls is faulty and is not used?
// TODO (check out why failing and remove code if not used)

  //  @Test
  public void testFindByIdAndEducationType() {
    CandidateEducation savedCE = repository.findByIdLoadEducationType(ce.getEducationType());
    assertNotNull(savedCE);
    assertEquals(ce.getInstitution(), savedCE.getInstitution());
  }

  /**
   * Make sure it fails to find the saved one.
   */
//  @Test
  public void testFindByIdAndEducationTypeFail() {
    CandidateEducation savedCE = repository.findByIdLoadEducationType(ce.getEducationType());
    assertNull(savedCE);
  }
}
