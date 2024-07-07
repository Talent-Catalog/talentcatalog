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
import org.tctalent.server.model.db.CandidateAttachment;
import org.tctalent.server.model.db.task.UploadType;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getCandidateAttachment;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedCandidate;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedUser;

public class CandidateAttachmentRepositoryIntTest extends BaseDBIntegrationTest {

  @Autowired
  private CandidateAttachmentRepository repo;
  @Autowired
  private CandidateRepository candidateRepository;
  @Autowired
  private UserRepository userRepository;
  private CandidateAttachment candidateAttachment;
  private Candidate testCandidate;

  @BeforeEach
  void setup() {
    assertTrue(isContainerInitialised());

    testCandidate = getSavedCandidate(candidateRepository, getSavedUser(userRepository));
    candidateAttachment = getCandidateAttachment();
    candidateAttachment.setCandidate(testCandidate);
    repo.save(candidateAttachment);
    assertTrue(candidateAttachment.getId() > 0);
  }

  @Test
  void testFindByCandidateIdLoadAudit() {
    // create a second one so we can check order.
    CandidateAttachment newCA = getCandidateAttachment();
    newCA.setCandidate(testCandidate);
    repo.save(newCA);
    assertTrue(newCA.getId() > 0);
    List<CandidateAttachment> results = repo.findByCandidateIdLoadAudit(testCandidate.getId());
    assertNotNull(results);
    assertFalse(results.isEmpty());
    System.out.println(candidateAttachment.getCreatedDate());
    System.out.println(newCA.getCreatedDate());
    assertEquals(newCA.getId(), results.getFirst().getId());
  }

  @Test
  void testFindByCandidateIdLoadAuditFail() {
    // create a second one so we can check order.
    CandidateAttachment newCA = getCandidateAttachment();
    newCA.setCandidate(testCandidate);
    repo.save(newCA);
    assertTrue(newCA.getId() > 0);
    List<CandidateAttachment> results = repo.findByCandidateIdLoadAudit(0L);
    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  @Test
  void testFindByCandidateIdAndType() {
    List<CandidateAttachment> results = repo.findByCandidateIdAndType(testCandidate.getId(),
        UploadType.idCard);
    assertNotNull(results);
    assertFalse(results.isEmpty());
    assertEquals(1, results.size());
    assertTrue(results.stream().allMatch(it -> it.getId().equals(candidateAttachment.getId())));
  }

  @Test
  void testFindByCandidateIdAndTypeFailId() {
    List<CandidateAttachment> results = repo.findByCandidateIdAndType(null, UploadType.idCard);
    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  @Test
  void testFindByCandidateIdAndTypeFailType() {
    List<CandidateAttachment> results = repo.findByCandidateIdAndType(testCandidate.getId(),
        UploadType.cv);
    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  @Test
  void testFindByCandidateIdPageable() {
    var results = repo.findByCandidateId(testCandidate.getId(), Pageable.unpaged());
    assertNotNull(results);
    assertFalse(results.getContent().isEmpty());
    assertEquals(1, results.getContent().size());
    assertTrue(
        results.getContent().stream().allMatch(i -> i.getId().equals(candidateAttachment.getId())));
  }

  @Test
  void testFindByCandidateIdPageableFailId() {
    var results = repo.findByCandidateId(null, Pageable.unpaged());
    assertNotNull(results);
    assertTrue(results.getContent().isEmpty());
  }

  @Test
  void testFindByCandidateId() {
    List<CandidateAttachment> results = repo.findByCandidateId(testCandidate.getId());
    assertNotNull(results);
    assertFalse(results.isEmpty());
    assertEquals(1, results.size());
    assertTrue(results.stream().allMatch(it -> it.getId().equals(candidateAttachment.getId())));
  }

  @Test
  void testFindByCandidateIdFail() {
    List<CandidateAttachment> results = repo.findByCandidateId(9999L);
    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  @Test
  void testFindByCandidateIdAndCvFalse() {
    List<CandidateAttachment> results = repo.findByCandidateIdAndCv(testCandidate.getId(), false);
    assertNotNull(results);
    assertFalse(results.isEmpty());
    assertEquals(1, results.size());
    assertTrue(results.stream().allMatch(it -> it.getId().equals(candidateAttachment.getId())));
  }

  @Test
  void testFindByCandidateIdAndCvTrue() {
    candidateAttachment.setUploadType(UploadType.cv);
    candidateAttachment.setCv(true);
    repo.save(candidateAttachment);
    List<CandidateAttachment> results = repo.findByCandidateIdAndCv(testCandidate.getId(), true);
    assertNotNull(results);
    assertFalse(results.isEmpty());
    assertEquals(1, results.size());
    assertTrue(results.stream().allMatch(it -> it.getId().equals(candidateAttachment.getId())));
  }

  @Test
  void testFindByCandidateIdAndCvFail() {
    candidateAttachment.setUploadType(UploadType.cv);
    repo.save(candidateAttachment);
    List<CandidateAttachment> results = repo.findByCandidateIdAndCv(999999L, true);
    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  @Test
  void testFindByIdLoadCandidate() {
    var results = repo.findByIdLoadCandidate(candidateAttachment.getId()).orElse(null);
    assertNotNull(results);
    assertEquals(candidateAttachment.getId(), results.getId());
  }

  @Test
  void testFindByIdLoadCandidateFail() {
    var results = repo.findByIdLoadCandidate(null).orElse(null);
    assertNull(results);
  }

  @Test
  void testFindByFileType() {
    List<CandidateAttachment> results = repo.findByFileType("pdf");
    assertNotNull(results);
    assertFalse(results.isEmpty());
    assertEquals(1, results.size());
    assertEquals(candidateAttachment.getId(), results.getFirst().getId());
  }

  @Test
  void testFindByFileTypeFail() {
    List<CandidateAttachment> results = repo.findByFileType("PNG");
    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  @Test
  void testFindByFileTypesAndMigrated() {
    List<CandidateAttachment> results = repo.findByFileTypesAndMigrated(List.of("pdf"), true);
    assertNotNull(results);
    assertFalse(results.isEmpty());
    assertEquals(1, results.size());
    assertEquals(candidateAttachment.getId(), results.getFirst().getId());
  }

  @Test
  void testFindByFileTypesAndMigratedFailed() {
    List<CandidateAttachment> results = repo.findByFileTypesAndMigrated(null, true);
    assertNotNull(results);
    assertTrue(results.isEmpty());
  }
}
