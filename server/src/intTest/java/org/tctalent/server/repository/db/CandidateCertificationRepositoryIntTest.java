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
import org.tctalent.server.model.db.CandidateCertification;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getCandidateCert;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedCandidate;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedUser;

public class CandidateCertificationRepositoryIntTest extends BaseDBIntegrationTest {

  @Autowired
  private CandidateCertificationRepository repository;
  @Autowired
  private CandidateRepository candidateRepository;
  @Autowired
  private UserRepository userRepository;
  private CandidateCertification cert;
  private Candidate testCandidate;

  @BeforeEach
  void setup() {
    assertTrue(isContainerInitialised());

    User savedUser = getSavedUser(userRepository);
    testCandidate = getSavedCandidate(candidateRepository, savedUser);
    cert = getCandidateCert();
    cert.setCandidate(testCandidate);
    CandidateCertification cert2 = getCandidateCert();
    cert2.setCandidate(testCandidate);
    repository.save(cert);
    assertTrue(cert.getId() > 0);
    repository.save(cert2);
    assertTrue(cert2.getId() > 0);
  }

  @Test
  void testFindByIdAndLoadCandidate() {
    var savedCert = repository.findByIdLoadCandidate(cert.getId()).orElse(null);
    assertNotNull(savedCert);
    assertTrue(savedCert.getCandidate().getPhone().startsWith("999999999"));
  }

  @Test
  void testFindByIdAndLoadCandidateFails() {
    var savedCert = repository.findByIdLoadCandidate(99999999999L).orElse(null);
    assertNull(savedCert);
  }

  @Test
  void testFindByCandidateId() {
    var savedCert = repository.findByCandidateId(testCandidate.getId());
    assertNotNull(savedCert);
    assertFalse(savedCert.isEmpty());
    assertEquals(2, savedCert.size());
    var names = savedCert.stream().map(CandidateCertification::getName).toList();
    assertTrue(names.contains("GREAT CERT"));
  }

  @Test
  void testFindByCandidateIdFails() {
    var savedCert = repository.findByCandidateId(999999999L);
    assertTrue(savedCert.isEmpty());
  }
}
