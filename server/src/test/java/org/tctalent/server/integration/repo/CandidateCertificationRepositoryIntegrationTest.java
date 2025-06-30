/*
 * Copyright (c) 2025 Talent Catalog.
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

package org.tctalent.server.integration.repo;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tctalent.server.integration.helper.BaseDBIntegrationTest;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateCertification;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidateCertificationRepository;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.tctalent.server.integration.helper.TestDataFactory.createAndSaveCandidate;
import static org.tctalent.server.integration.helper.TestDataFactory.createAndSaveUser;
import static org.tctalent.server.integration.helper.TestDataFactory.createCandidateCertification;

/**
 * Integration tests for CandidateCertificationRepository, verifying certification retrieval and association with candidates.
 */
public class CandidateCertificationRepositoryIntegrationTest extends BaseDBIntegrationTest {

  @Autowired
  private CandidateCertificationRepository certificationRepository;
  @Autowired
  private CandidateRepository candidateRepository;
  @Autowired
  private UserRepository userRepository;

  private CandidateCertification testCertification;
  private Candidate testCandidate;

  /**
   * Sets up test data by creating a user, candidate, and two certifications associated with the candidate.
   */
  @BeforeEach
  void setUp() {
    assertTrue(isContainerInitialised(), "Database container should be initialized");

    User savedUser = createAndSaveUser(userRepository);
    testCandidate = createAndSaveCandidate(candidateRepository, savedUser);
    testCertification = createCandidateCertification();
    testCertification.setCandidate(testCandidate);
    CandidateCertification secondCertification = createCandidateCertification();
    secondCertification.setCandidate(testCandidate);
    certificationRepository.save(testCertification);
    assertTrue(testCertification.getId() > 0, "First certification should have a valid ID");
    certificationRepository.save(secondCertification);
    assertTrue(secondCertification.getId() > 0, "Second certification should have a valid ID");
  }

  /**
   * Tests finding a certification by ID with its associated candidate, verifying the candidate's phone number.
   */
  @Test
  void shouldFindCertificationByIdWithCandidate() {
    CandidateCertification foundCertification = certificationRepository
        .findByIdLoadCandidate(testCertification.getId())
        .orElse(null);
    assertNotNull(foundCertification, "Certification should be found");
    assertTrue(foundCertification.getCandidate().getPhone().startsWith("999999999"),
        "Candidate phone number should match expected pattern");
  }

  /**
   * Tests that finding a certification by a non-existent ID returns null.
   */
  @Test
  void shouldReturnNullForNonExistentCertificationId() {
    CandidateCertification foundCertification = certificationRepository
        .findByIdLoadCandidate(99999999999L)
        .orElse(null);
    assertNull(foundCertification, "Non-existent certification should not be found");
  }

  /**
   * Tests finding all certifications for a candidate, verifying the number and content of certifications.
   */
  @Test
  void shouldFindCertificationsByCandidateId() {
    List<CandidateCertification> foundCertifications = certificationRepository
        .findByCandidateId(testCandidate.getId());
    assertNotNull(foundCertifications, "Certifications list should not be null");
    assertFalse(foundCertifications.isEmpty(), "Certifications list should not be empty");
    assertEquals(2, foundCertifications.size(), "Should find exactly two certifications");
    List<String> certificationNames = foundCertifications.stream()
        .map(CandidateCertification::getName)
        .toList();
    assertTrue(certificationNames.contains("GREAT CERT"), "Certifications should include expected name");
  }

  /**
   * Tests that finding certifications for a non-existent candidate ID returns an empty list.
   */
  @Test
  void shouldReturnEmptyListForNonExistentCandidateId() {
    List<CandidateCertification> foundCertifications = certificationRepository
        .findByCandidateId(999999999L);
    assertTrue(foundCertifications.isEmpty(), "Certifications list should be empty for non-existent candidate");
  }
}