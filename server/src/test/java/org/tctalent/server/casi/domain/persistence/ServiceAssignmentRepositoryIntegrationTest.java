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

package org.tctalent.server.casi.domain.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.tctalent.server.integration.helper.TestDataFactory.createAndSaveCandidate;
import static org.tctalent.server.integration.helper.TestDataFactory.createAndSaveUser;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tctalent.server.casi.domain.model.AssignmentStatus;
import org.tctalent.server.casi.domain.model.ResourceStatus;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.integration.helper.BaseJpaIntegrationTest;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.UserRepository;

/**
 * Integration tests for {@link ServiceAssignmentRepository} running against a real PostgreSQL
 * instance via Testcontainers. Covers all custom finder queries including filtering, ordering,
 * join-fetch, and edge cases.
 */
class ServiceAssignmentRepositoryIntegrationTest extends BaseJpaIntegrationTest {

  @Autowired
  private ServiceAssignmentRepository assignmentRepo;

  @Autowired
  private ServiceResourceRepository resourceRepo;

  @Autowired
  private CandidateRepository candidateRepo;

  @Autowired
  private UserRepository userRepo;

  private Candidate candidateA;
  private Candidate candidateB;
  private User actor;

  private ServiceResourceEntity resourceProc1;
  private ServiceResourceEntity resourceProc2;
  private ServiceResourceEntity resourceNonProc;

  private ServiceAssignmentEntity assignedProc;
  private ServiceAssignmentEntity redeemedProc;
  private ServiceAssignmentEntity assignedNonProc;
  private ServiceAssignmentEntity candidateBAssignment;

  @BeforeEach
  void setUp() {
    actor = createAndSaveUser(userRepo);
    candidateA = createAndSaveCandidate(candidateRepo, createAndSaveUser(userRepo));
    candidateB = createAndSaveCandidate(candidateRepo, createAndSaveUser(userRepo));

    resourceProc1 = saveResource("PROC-ASN-001", ServiceCode.TEST_PROCTORED, ResourceStatus.SENT);
    resourceProc2 = saveResource("PROC-ASN-002", ServiceCode.TEST_PROCTORED, ResourceStatus.SENT);
    resourceNonProc = saveResource("NONPROC-ASN-001", ServiceCode.TEST_NON_PROCTORED, ResourceStatus.SENT);

    // CandidateA: ASSIGNED proctored (oldest)
    assignedProc = saveAssignment(
        candidateA, resourceProc1, ServiceCode.TEST_PROCTORED,
        AssignmentStatus.ASSIGNED, OffsetDateTime.now().minusDays(5));

    // CandidateA: REDEEMED proctored (middle)
    redeemedProc = saveAssignment(
        candidateA, resourceProc2, ServiceCode.TEST_PROCTORED,
        AssignmentStatus.REDEEMED, OffsetDateTime.now().minusDays(2));

    // CandidateA: ASSIGNED non-proctored (newest)
    assignedNonProc = saveAssignment(
        candidateA, resourceNonProc, ServiceCode.TEST_NON_PROCTORED,
        AssignmentStatus.ASSIGNED, OffsetDateTime.now().minusDays(1));

    // CandidateB: one assignment (to verify candidate isolation)
    ServiceResourceEntity resourceForB = saveResource(
        "PROC-ASN-B01", ServiceCode.TEST_PROCTORED, ResourceStatus.SENT);
    candidateBAssignment = saveAssignment(
        candidateB, resourceForB, ServiceCode.TEST_PROCTORED,
        AssignmentStatus.ASSIGNED, OffsetDateTime.now());
  }

  // ── findByCandidateIdOrderByAssignedAtDesc ─────────────────────────────

  @Test
  @DisplayName("findByCandidateIdOrderByAssignedAtDesc returns all assignments ordered newest first")
  void findByCandidateId_returnsAllOrderedDesc() {
    List<ServiceAssignmentEntity> result =
        assignmentRepo.findByCandidateIdOrderByAssignedAtDesc(candidateA.getId());

    assertThat(result).hasSize(3);
    assertThat(result.get(0).getId()).isEqualTo(assignedNonProc.getId());
    assertThat(result.get(1).getId()).isEqualTo(redeemedProc.getId());
    assertThat(result.get(2).getId()).isEqualTo(assignedProc.getId());
  }

  @Test
  @DisplayName("findByCandidateIdOrderByAssignedAtDesc does not return other candidates' assignments")
  void findByCandidateId_isolatesByCandidate() {
    List<ServiceAssignmentEntity> result =
        assignmentRepo.findByCandidateIdOrderByAssignedAtDesc(candidateA.getId());

    List<Long> ids = result.stream().map(ServiceAssignmentEntity::getId).toList();
    assertThat(ids).doesNotContain(candidateBAssignment.getId());
  }

  @Test
  @DisplayName("findByCandidateIdOrderByAssignedAtDesc returns empty for unknown candidate")
  void findByCandidateId_emptyForUnknown() {
    List<ServiceAssignmentEntity> result =
        assignmentRepo.findByCandidateIdOrderByAssignedAtDesc(999_999_999L);

    assertThat(result).isEmpty();
  }

  // ── findByCandidateIdAndStatus ─────────────────────────────────────────

  @Test
  @DisplayName("findByCandidateIdAndStatus filters by status")
  void findByCandidateIdAndStatus_filtersByStatus() {
    List<ServiceAssignmentEntity> result =
        assignmentRepo.findByCandidateIdAndStatus(candidateA.getId(), AssignmentStatus.ASSIGNED);

    assertThat(result).hasSize(2);
    assertThat(result).allMatch(a -> a.getStatus() == AssignmentStatus.ASSIGNED);

    List<Long> ids = result.stream().map(ServiceAssignmentEntity::getId).toList();
    assertThat(ids).containsExactlyInAnyOrder(assignedProc.getId(), assignedNonProc.getId());
  }

  @Test
  @DisplayName("findByCandidateIdAndStatus returns REDEEMED only")
  void findByCandidateIdAndStatus_redeemedOnly() {
    List<ServiceAssignmentEntity> result =
        assignmentRepo.findByCandidateIdAndStatus(candidateA.getId(), AssignmentStatus.REDEEMED);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(redeemedProc.getId());
  }

  @Test
  @DisplayName("findByCandidateIdAndStatus returns empty for non-matching status")
  void findByCandidateIdAndStatus_emptyForNonMatchingStatus() {
    List<ServiceAssignmentEntity> result =
        assignmentRepo.findByCandidateIdAndStatus(candidateA.getId(), AssignmentStatus.EXPIRED);

    assertThat(result).isEmpty();
  }

  // ── findByCandidateAndProviderAndService ───────────────────────────────

  @Test
  @DisplayName("findByCandidateAndProviderAndService returns matching, ordered by assignedAt desc")
  void findByCandidateAndProviderAndService_returnsMatchingOrdered() {
    List<ServiceAssignmentEntity> result =
        assignmentRepo.findByCandidateAndProviderAndService(
            candidateA.getId(), ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED);

    assertThat(result).hasSize(2);
    // newest first
    assertThat(result.get(0).getId()).isEqualTo(redeemedProc.getId());
    assertThat(result.get(1).getId()).isEqualTo(assignedProc.getId());
  }

  @Test
  @DisplayName("findByCandidateAndProviderAndService filters by serviceCode")
  void findByCandidateAndProviderAndService_filtersByServiceCode() {
    List<ServiceAssignmentEntity> result =
        assignmentRepo.findByCandidateAndProviderAndService(
            candidateA.getId(), ServiceProvider.DUOLINGO, ServiceCode.TEST_NON_PROCTORED);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(assignedNonProc.getId());
  }

  @Test
  @DisplayName("findByCandidateAndProviderAndService returns empty for non-matching candidate")
  void findByCandidateAndProviderAndService_emptyForNonMatchingCandidate() {
    List<ServiceAssignmentEntity> result =
        assignmentRepo.findByCandidateAndProviderAndService(
            candidateB.getId(), ServiceProvider.DUOLINGO, ServiceCode.TEST_NON_PROCTORED);

    assertThat(result).isEmpty();
  }

  // ── findByCandidateAndProviderServiceAndStatus ─────────────────────────

  @Test
  @DisplayName("findByCandidateAndProviderServiceAndStatus returns matching ordered by assignedAt desc")
  void findByCandidateAndProviderServiceAndStatus_returnsMatching() {
    List<ServiceAssignmentEntity> result =
        assignmentRepo.findByCandidateAndProviderServiceAndStatus(
            candidateA.getId(), ServiceProvider.DUOLINGO,
            ServiceCode.TEST_PROCTORED, AssignmentStatus.ASSIGNED);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(assignedProc.getId());
  }

  @Test
  @DisplayName("findByCandidateAndProviderServiceAndStatus returns empty for non-matching combination")
  void findByCandidateAndProviderServiceAndStatus_emptyForNonMatching() {
    List<ServiceAssignmentEntity> result =
        assignmentRepo.findByCandidateAndProviderServiceAndStatus(
            candidateA.getId(), ServiceProvider.DUOLINGO,
            ServiceCode.TEST_NON_PROCTORED, AssignmentStatus.REDEEMED);

    assertThat(result).isEmpty();
  }

  // ── findTopByProviderAndServiceAndResource ─────────────────────────────

  @Test
  @DisplayName("findTopByProviderAndServiceAndResource returns assignment for a given resource")
  void findTopByProviderAndServiceAndResource_returnsAssignment() {
    Optional<ServiceAssignmentEntity> result =
        assignmentRepo.findTopByProviderAndServiceAndResource(
            ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED, resourceProc1.getId());

    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(assignedProc.getId());
  }

  @Test
  @DisplayName("findTopByProviderAndServiceAndResource returns the most recent when multiple exist")
  void findTopByProviderAndServiceAndResource_returnsMostRecent() {
    // Mirror the real reassignment workflow: the original ASSIGNED row must become REASSIGNED
    // before a new ASSIGNED row can be inserted (enforced by the partial unique index
    // sa_assigned_per_resource_uq_idx on resource_id WHERE status = 'ASSIGNED').
    assignedProc.setStatus(AssignmentStatus.REASSIGNED);
    assignmentRepo.saveAndFlush(assignedProc);

    ServiceAssignmentEntity newer = saveAssignment(
        candidateB, resourceProc1, ServiceCode.TEST_PROCTORED,
        AssignmentStatus.ASSIGNED, OffsetDateTime.now());

    Optional<ServiceAssignmentEntity> result =
        assignmentRepo.findTopByProviderAndServiceAndResource(
            ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED, resourceProc1.getId());

    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(newer.getId());
  }

  @Test
  @DisplayName("findTopByProviderAndServiceAndResource returns empty for non-existing resource")
  void findTopByProviderAndServiceAndResource_emptyForNonExisting() {
    Optional<ServiceAssignmentEntity> result =
        assignmentRepo.findTopByProviderAndServiceAndResource(
            ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED, 999_999_999L);

    assertThat(result).isEmpty();
  }

  // ── findAllForCandidateWithResource ────────────────────────────────────

  @Test
  @DisplayName("findAllForCandidateWithResource returns all assignments with resource eagerly fetched")
  void findAllForCandidateWithResource_returnsWithResource() {
    List<ServiceAssignmentEntity> result =
        assignmentRepo.findAllForCandidateWithResource(candidateA.getId());

    assertThat(result).hasSize(3);

    // Verify resources are loaded (not lazy proxies that would fail outside a session)
    for (ServiceAssignmentEntity a : result) {
      assertThat(a.getResource()).isNotNull();
      assertThat(a.getResource().getResourceCode()).isNotBlank();
    }
  }

  @Test
  @DisplayName("findAllForCandidateWithResource is ordered by assignedAt desc")
  void findAllForCandidateWithResource_orderedDesc() {
    List<ServiceAssignmentEntity> result =
        assignmentRepo.findAllForCandidateWithResource(candidateA.getId());

    assertThat(result).hasSize(3);
    assertThat(result.get(0).getId()).isEqualTo(assignedNonProc.getId());
    assertThat(result.get(1).getId()).isEqualTo(redeemedProc.getId());
    assertThat(result.get(2).getId()).isEqualTo(assignedProc.getId());
  }

  @Test
  @DisplayName("findAllForCandidateWithResource returns empty for unknown candidate")
  void findAllForCandidateWithResource_emptyForUnknown() {
    List<ServiceAssignmentEntity> result =
        assignmentRepo.findAllForCandidateWithResource(999_999_999L);

    assertThat(result).isEmpty();
  }

  // ── helpers ────────────────────────────────────────────────────────────

  private ServiceResourceEntity saveResource(
      String resourceCode, ServiceCode serviceCode, ResourceStatus status) {
    ServiceResourceEntity e = new ServiceResourceEntity();
    e.setProvider(ServiceProvider.DUOLINGO);
    e.setServiceCode(serviceCode);
    e.setResourceCode(resourceCode);
    e.setStatus(status);
    e.setExpiresAt(OffsetDateTime.now().plusDays(30));
    return resourceRepo.saveAndFlush(e);
  }

  private ServiceAssignmentEntity saveAssignment(
      Candidate candidate, ServiceResourceEntity resource,
      ServiceCode serviceCode, AssignmentStatus status,
      OffsetDateTime assignedAt) {
    ServiceAssignmentEntity a = new ServiceAssignmentEntity();
    a.setProvider(ServiceProvider.DUOLINGO);
    a.setServiceCode(serviceCode);
    a.setResource(resource);
    a.setCandidate(candidate);
    a.setActor(actor);
    a.setStatus(status);
    a.setAssignedAt(assignedAt);
    return assignmentRepo.saveAndFlush(a);
  }
}
