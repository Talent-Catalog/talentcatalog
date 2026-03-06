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

package org.tctalent.server.casi.application.providers.duolingo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.casi.core.allocators.ResourceAllocator;
import org.tctalent.server.casi.core.importers.FileInventoryImporter;
import org.tctalent.server.casi.core.services.AbstractCandidateAssistanceService;
import org.tctalent.server.casi.core.services.AssignmentEngine;
import org.tctalent.server.casi.domain.model.AssignmentStatus;
import org.tctalent.server.casi.domain.model.ResourceStatus;
import org.tctalent.server.casi.domain.model.ServiceAssignment;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.casi.domain.model.ServiceResource;
import org.tctalent.server.casi.domain.persistence.ServiceAssignmentEntity;
import org.tctalent.server.casi.domain.persistence.ServiceAssignmentRepository;
import org.tctalent.server.casi.domain.persistence.ServiceResourceEntity;
import org.tctalent.server.casi.domain.persistence.ServiceResourceRepository;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.ImportFailedException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.User;
import org.tctalent.server.service.db.SavedListService;

@ExtendWith(MockitoExtension.class)
class DuolingoServiceTest {

  private static final Long CANDIDATE_ID = 123L;
  private static final String CANDIDATE_NUMBER = "C12345";
  private static final Long LIST_ID = 456L;
  private static final String RESOURCE_CODE = "COUPON123";

  @Mock private ServiceAssignmentRepository assignmentRepository;
  @Mock private ServiceResourceRepository resourceRepository;
  @Mock private AssignmentEngine assignmentEngine;
  @Mock private SavedListService savedListService;
  @Mock private FileInventoryImporter duolingoImporter;
  @Mock private ResourceAllocator duolingoAllocator;

  @InjectMocks private DuolingoService duolingoService;

  private User user;
  private Candidate candidate;
  private ServiceResource resource;
  private ServiceResourceEntity resourceEntity;
  private ServiceAssignment assignment;
  private ServiceAssignmentEntity assignmentEntity;
  private SavedList savedList;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(1L);
    user.setUsername("testuser");

    candidate = new Candidate();
    candidate.setId(CANDIDATE_ID);
    candidate.setCandidateNumber(CANDIDATE_NUMBER);

    resource = ServiceResource.builder()
        .id(1L)
        .provider(ServiceProvider.DUOLINGO)
        .serviceCode(ServiceCode.TEST_PROCTORED)
        .resourceCode(RESOURCE_CODE)
        .status(ResourceStatus.AVAILABLE)
        .build();

    resourceEntity = new ServiceResourceEntity();
    resourceEntity.setId(1L);
    resourceEntity.setProvider(ServiceProvider.DUOLINGO);
    resourceEntity.setServiceCode(ServiceCode.TEST_PROCTORED);
    resourceEntity.setResourceCode(RESOURCE_CODE);
    resourceEntity.setStatus(ResourceStatus.AVAILABLE);

    assignment = ServiceAssignment.builder()
        .id(1L)
        .provider(ServiceProvider.DUOLINGO)
        .serviceCode(ServiceCode.TEST_PROCTORED)
        .resource(resource)
        .candidateId(CANDIDATE_ID)
        .actorId(user.getId())
        .status(AssignmentStatus.ASSIGNED)
        .build();

    assignmentEntity = new ServiceAssignmentEntity();
    assignmentEntity.setId(1L);
    assignmentEntity.setProvider(ServiceProvider.DUOLINGO);
    assignmentEntity.setServiceCode(ServiceCode.TEST_PROCTORED);
    assignmentEntity.setResource(resourceEntity);
    assignmentEntity.setCandidate(candidate);
    assignmentEntity.setStatus(AssignmentStatus.ASSIGNED);

    savedList = mock(SavedList.class);
    // Use lenient() for stubbings that are not used by all tests.
    // Without lenient(), Mockito would throw UnnecessaryStubbingException
    // for tests that don't use savedList (e.g., providerKeyReturnsCorrectFormat).
    lenient().when(savedList.getId()).thenReturn(LIST_ID);
    lenient().when(savedList.getCandidates()).thenReturn(Set.of(candidate));
  }

  // Provider Key Generation Tests

  @Test
  @DisplayName("providerKey returns correct format")
  void providerKeyReturnsCorrectFormat() {
    // Act
    String providerKey = duolingoService.providerKey();

    // Assert
    assertThat(providerKey).isEqualTo("DUOLINGO::TEST_PROCTORED");
  }

  // Import Inventory Tests

  @Test
  @DisplayName("import inventory succeeds with importer")
  void importInventorySucceedsWithImporter() throws Exception {
    // Arrange
    MultipartFile file = mock(MultipartFile.class);

    // Act
    duolingoService.importInventory(file);

    // Assert
    verify(duolingoImporter).importFile(file, ServiceCode.TEST_PROCTORED);
  }

  @Test
  @DisplayName("import inventory fails when importer is null")
  void importInventoryFailsWhenImporterIsNull() {
    // Arrange
    MultipartFile file = mock(MultipartFile.class);
    // Create a service without importer by using a different constructor approach
    // Since DuolingoService always has an importer, I'm testing the abstract class behavior
    // by creating a test subclass without importer
    AbstractCandidateAssistanceServiceWithoutImporter serviceWithoutImporter =
        new AbstractCandidateAssistanceServiceWithoutImporter(
            assignmentRepository, resourceRepository, assignmentEngine, savedListService);

    // Act & Assert
    assertThatThrownBy(() -> serviceWithoutImporter.importInventory(file))
        .isInstanceOf(ImportFailedException.class)
        .hasMessageContaining("Import not supported");
  }

  @Test
  @DisplayName("import inventory fails when importer throws exception")
  void importInventoryFailsWhenImporterThrowsException() throws Exception {
    // Arrange
    MultipartFile file = mock(MultipartFile.class);
    doThrow(new ImportFailedException("Invalid file format"))
        .when(duolingoImporter).importFile(file, ServiceCode.TEST_PROCTORED);

    // Act & Assert
    assertThatThrownBy(() -> duolingoService.importInventory(file))
        .isInstanceOf(ImportFailedException.class)
        .hasMessageContaining("Invalid file format");

    verify(duolingoImporter).importFile(file, ServiceCode.TEST_PROCTORED);
  }

  // Helper class for testing without importer
  private static class AbstractCandidateAssistanceServiceWithoutImporter extends AbstractCandidateAssistanceService {
    public AbstractCandidateAssistanceServiceWithoutImporter(
        ServiceAssignmentRepository assignmentRepo,
        ServiceResourceRepository resourceRepo,
        AssignmentEngine assignmentEngine,
        SavedListService savedListService) {
      super(assignmentRepo, resourceRepo, assignmentEngine, savedListService);
    }

    @Override
    protected ServiceProvider provider() {
      return ServiceProvider.DUOLINGO;
    }

    @Override
    protected ServiceCode serviceCode() {
      return ServiceCode.TEST_PROCTORED;
    }

    @Override
    protected ResourceAllocator allocator() {
      return null;
    }
  }

  // Assign to Candidate Tests

  @Test
  @DisplayName("assign to candidate succeeds")
  void assignToCandidateSucceeds() {
    // Arrange
    when(assignmentRepository.findByCandidateAndProviderAndService(
        CANDIDATE_ID, ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED))
        .thenReturn(Collections.emptyList());
    when(assignmentEngine.assign(duolingoAllocator, CANDIDATE_ID, user))
        .thenReturn(assignment);

    // Act
    ServiceAssignment result = duolingoService.assignToCandidate(CANDIDATE_ID, user);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getCandidateId()).isEqualTo(CANDIDATE_ID);
    assertThat(result.getStatus()).isEqualTo(AssignmentStatus.ASSIGNED);
    verify(assignmentEngine).assign(duolingoAllocator, CANDIDATE_ID, user);
  }

  @Test
  @DisplayName("assign to candidate fails when duplicate assignment exists")
  void assignToCandidateFailsWhenDuplicateExists() {
    // Arrange
    when(assignmentRepository.findByCandidateAndProviderAndService(
        CANDIDATE_ID, ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED))
        .thenReturn(List.of(assignmentEntity));

    // Act & Assert
    assertThatThrownBy(() -> duolingoService.assignToCandidate(CANDIDATE_ID, user))
        .isInstanceOf(EntityExistsException.class)
        .hasMessageContaining("ASSIGNED TEST_PROCTORED resource")
        .hasMessageContaining("already exists: for this candidate");

    verify(assignmentEngine, never()).assign(any(), any(), any());
  }

  @Test
  @DisplayName("assign to candidate succeeds when candidate has non-ASSIGNED assignments")
  void assignToCandidateSucceedsWhenCandidateHasNonAssignedAssignments() {
    // Arrange
    ServiceAssignmentEntity reassignedEntity = new ServiceAssignmentEntity();
    reassignedEntity.setId(2L);
    reassignedEntity.setProvider(ServiceProvider.DUOLINGO);
    reassignedEntity.setServiceCode(ServiceCode.TEST_PROCTORED);
    reassignedEntity.setStatus(AssignmentStatus.REASSIGNED);
    reassignedEntity.setCandidate(candidate);

    when(assignmentRepository.findByCandidateAndProviderAndService(
        CANDIDATE_ID, ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED))
        .thenReturn(List.of(reassignedEntity)); // Only REASSIGNED, no ASSIGNED
    when(assignmentEngine.assign(duolingoAllocator, CANDIDATE_ID, user))
        .thenReturn(assignment);

    // Act
    ServiceAssignment result = duolingoService.assignToCandidate(CANDIDATE_ID, user);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getCandidateId()).isEqualTo(CANDIDATE_ID);
    verify(assignmentEngine).assign(duolingoAllocator, CANDIDATE_ID, user);
  }

  @Test
  @DisplayName("assign to candidate fails when candidate not found")
  void assignToCandidateFailsWhenCandidateNotFound() {
    // Arrange
    when(assignmentRepository.findByCandidateAndProviderAndService(
        CANDIDATE_ID, ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED))
        .thenReturn(Collections.emptyList());
    when(assignmentEngine.assign(duolingoAllocator, CANDIDATE_ID, user))
        .thenThrow(new NoSuchObjectException("Candidate with ID " + CANDIDATE_ID + " not found"));

    // Act & Assert
    assertThatThrownBy(() -> duolingoService.assignToCandidate(CANDIDATE_ID, user))
        .isInstanceOf(NoSuchObjectException.class)
        .hasMessageContaining("Candidate with ID " + CANDIDATE_ID + " not found");
  }

  @Test
  @DisplayName("assign to candidate fails when no resources available")
  void assignToCandidateFailsWhenNoResourcesAvailable() {
    // Arrange
    when(assignmentRepository.findByCandidateAndProviderAndService(
        CANDIDATE_ID, ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED))
        .thenReturn(Collections.emptyList());
    when(assignmentEngine.assign(duolingoAllocator, CANDIDATE_ID, user))
        .thenThrow(new NoSuchObjectException("There are no available TEST_PROCTORED coupons to assign"));

    // Act & Assert
    assertThatThrownBy(() -> duolingoService.assignToCandidate(CANDIDATE_ID, user))
        .isInstanceOf(NoSuchObjectException.class)
        .hasMessageContaining("There are no available");
  }

  // Assign to List Tests

  @Test
  @DisplayName("assign to list succeeds")
  void assignToListSucceeds() {
    // Arrange
    Candidate candidate2 = new Candidate();
    candidate2.setId(789L);
    when(savedList.getCandidates()).thenReturn(Set.of(candidate, candidate2));

    ServiceAssignment assignment2 = ServiceAssignment.builder()
        .id(2L)
        .provider(ServiceProvider.DUOLINGO)
        .serviceCode(ServiceCode.TEST_PROCTORED)
        .resource(resource)
        .candidateId(789L)
        .actorId(user.getId())
        .status(AssignmentStatus.ASSIGNED)
        .build();

    when(savedListService.get(LIST_ID)).thenReturn(savedList);
    when(resourceRepository.countAvailableByProviderAndService(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED))
        .thenReturn(2L);
    when(assignmentRepository.findByCandidateAndProviderAndService(
        eq(CANDIDATE_ID), eq(ServiceProvider.DUOLINGO), eq(ServiceCode.TEST_PROCTORED)))
        .thenReturn(Collections.emptyList());
    when(assignmentRepository.findByCandidateAndProviderAndService(
        eq(789L), eq(ServiceProvider.DUOLINGO), eq(ServiceCode.TEST_PROCTORED)))
        .thenReturn(Collections.emptyList());
    when(assignmentEngine.assign(eq(duolingoAllocator), eq(CANDIDATE_ID), eq(user)))
        .thenReturn(assignment);
    when(assignmentEngine.assign(eq(duolingoAllocator), eq(789L), eq(user)))
        .thenReturn(assignment2);

    // Act
    List<ServiceAssignment> result = duolingoService.assignToList(LIST_ID, user);

    // Assert
    assertThat(result).hasSize(2);
    assertThat(result).containsExactlyInAnyOrder(assignment, assignment2);
    verify(assignmentEngine).assign(duolingoAllocator, CANDIDATE_ID, user);
    verify(assignmentEngine).assign(duolingoAllocator, 789L, user);
  }

  @Test
  @DisplayName("assign to list fails when insufficient resources")
  void assignToListFailsWhenInsufficientResources() {
    // Arrange
    Candidate candidate2 = new Candidate();
    candidate2.setId(789L);
    when(savedList.getCandidates()).thenReturn(Set.of(candidate, candidate2));

    when(savedListService.get(LIST_ID)).thenReturn(savedList);
    when(resourceRepository.countAvailableByProviderAndService(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED))
        .thenReturn(1L); // Only 1 resource for 2 candidates

    // Act & Assert
    assertThatThrownBy(() -> duolingoService.assignToList(LIST_ID, user))
        .isInstanceOf(NoSuchObjectException.class)
        .hasMessageContaining("not enough available")
        .hasMessageContaining("TEST_PROCTORED resources");

    verify(assignmentEngine, never()).assign(any(), any(), any());
  }

  @Test
  @DisplayName("assign to list skips candidates with existing assignments")
  void assignToListSkipsExistingAssignments() {
    // Arrange
    Candidate candidate2 = new Candidate();
    candidate2.setId(789L);
    when(savedList.getCandidates()).thenReturn(Set.of(candidate, candidate2));

    when(savedListService.get(LIST_ID)).thenReturn(savedList);
    when(resourceRepository.countAvailableByProviderAndService(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED))
        .thenReturn(2L);
    when(assignmentRepository.findByCandidateAndProviderAndService(
        eq(CANDIDATE_ID), eq(ServiceProvider.DUOLINGO), eq(ServiceCode.TEST_PROCTORED)))
        .thenReturn(List.of(assignmentEntity)); // Candidate 1 already has assignment
    when(assignmentRepository.findByCandidateAndProviderAndService(
        eq(789L), eq(ServiceProvider.DUOLINGO), eq(ServiceCode.TEST_PROCTORED)))
        .thenReturn(Collections.emptyList());
    when(assignmentEngine.assign(eq(duolingoAllocator), eq(789L), eq(user)))
        .thenReturn(assignment);

    // Act
    List<ServiceAssignment> result = duolingoService.assignToList(LIST_ID, user);

    // Assert
    assertThat(result).hasSize(1);
    verify(assignmentEngine, never()).assign(eq(duolingoAllocator), eq(CANDIDATE_ID), any());
    verify(assignmentEngine).assign(duolingoAllocator, 789L, user);
  }

  @Test
  @DisplayName("assign to list fails when list not found")
  void assignToListFailsWhenListNotFound() {
    // Arrange
    when(savedListService.get(LIST_ID))
        .thenThrow(new NoSuchObjectException(SavedList.class, LIST_ID));

    // Act & Assert
    assertThatThrownBy(() -> duolingoService.assignToList(LIST_ID, user))
        .isInstanceOf(NoSuchObjectException.class)
        .hasMessageContaining("SavedList");

    verify(assignmentEngine, never()).assign(any(), any(), any());
  }

  @Test
  @DisplayName("assign to list succeeds with empty list")
  void assignToListSucceedsWithEmptyList() {
    // Arrange
    when(savedListService.get(LIST_ID)).thenReturn(savedList);
    when(savedList.getCandidates()).thenReturn(Set.of());
    when(resourceRepository.countAvailableByProviderAndService(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED))
        .thenReturn(1L); // Has resources but no candidates

    // Act
    List<ServiceAssignment> result = duolingoService.assignToList(LIST_ID, user);

    // Assert
    assertThat(result).isEmpty();
    verify(assignmentEngine, never()).assign(any(), any(), any());
  }

  @Test
  @DisplayName("assign to list succeeds when exactly enough resources")
  void assignToListSucceedsWhenExactlyEnoughResources() {
    // Arrange
    Candidate candidate2 = new Candidate();
    candidate2.setId(789L);
    when(savedList.getCandidates()).thenReturn(Set.of(candidate, candidate2));

    when(savedListService.get(LIST_ID)).thenReturn(savedList);
    when(resourceRepository.countAvailableByProviderAndService(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED))
        .thenReturn(2L); // Exactly 2 resources for 2 candidates
    when(assignmentRepository.findByCandidateAndProviderAndService(
        eq(CANDIDATE_ID), eq(ServiceProvider.DUOLINGO), eq(ServiceCode.TEST_PROCTORED)))
        .thenReturn(Collections.emptyList());
    when(assignmentRepository.findByCandidateAndProviderAndService(
        eq(789L), eq(ServiceProvider.DUOLINGO), eq(ServiceCode.TEST_PROCTORED)))
        .thenReturn(Collections.emptyList());
    when(assignmentEngine.assign(eq(duolingoAllocator), eq(CANDIDATE_ID), eq(user)))
        .thenReturn(assignment);
    when(assignmentEngine.assign(eq(duolingoAllocator), eq(789L), eq(user)))
        .thenReturn(assignment);

    // Act
    List<ServiceAssignment> result = duolingoService.assignToList(LIST_ID, user);

    // Assert
    assertThat(result).hasSize(2);
    verify(assignmentEngine).assign(duolingoAllocator, CANDIDATE_ID, user);
    verify(assignmentEngine).assign(duolingoAllocator, 789L, user);
  }

  @Test
  @DisplayName("assign to list fails when all assignments fail")
  void assignToListFailsWhenAllAssignmentsFail() {
    // Arrange
    when(savedListService.get(LIST_ID)).thenReturn(savedList);
    when(resourceRepository.countAvailableByProviderAndService(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED))
        .thenReturn(1L);
    when(assignmentRepository.findByCandidateAndProviderAndService(
        eq(CANDIDATE_ID), eq(ServiceProvider.DUOLINGO), eq(ServiceCode.TEST_PROCTORED)))
        .thenReturn(Collections.emptyList());
    when(assignmentEngine.assign(eq(duolingoAllocator), eq(CANDIDATE_ID), eq(user)))
        .thenThrow(new NoSuchObjectException("Candidate with ID " + CANDIDATE_ID + " not found"));

    // Act & Assert
    assertThatThrownBy(() -> duolingoService.assignToList(LIST_ID, user))
        .isInstanceOf(NoSuchObjectException.class)
        .hasMessageContaining("Failed to assign")
        .hasMessageContaining("to any candidates")
        .hasMessageContaining("1 assignment(s) failed");
  }

  @Test
  @DisplayName("assign to list returns partial success when some assignments fail")
  void assignToListReturnsPartialSuccessWhenSomeAssignmentsFail() {
    // Arrange
    Candidate candidate2 = new Candidate();
    candidate2.setId(789L);
    candidate2.setCandidateNumber("C789");
    when(savedList.getCandidates()).thenReturn(Set.of(candidate, candidate2));

    ServiceAssignment assignment2 = ServiceAssignment.builder()
        .id(2L)
        .provider(ServiceProvider.DUOLINGO)
        .serviceCode(ServiceCode.TEST_PROCTORED)
        .resource(resource)
        .candidateId(789L)
        .actorId(user.getId())
        .status(AssignmentStatus.ASSIGNED)
        .build();

    when(savedListService.get(LIST_ID)).thenReturn(savedList);
    when(resourceRepository.countAvailableByProviderAndService(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED))
        .thenReturn(2L);
    when(assignmentRepository.findByCandidateAndProviderAndService(
        eq(CANDIDATE_ID), eq(ServiceProvider.DUOLINGO), eq(ServiceCode.TEST_PROCTORED)))
        .thenReturn(Collections.emptyList());
    when(assignmentRepository.findByCandidateAndProviderAndService(
        eq(789L), eq(ServiceProvider.DUOLINGO), eq(ServiceCode.TEST_PROCTORED)))
        .thenReturn(Collections.emptyList());
    // First candidate assignment fails
    when(assignmentEngine.assign(eq(duolingoAllocator), eq(CANDIDATE_ID), eq(user)))
        .thenThrow(new NoSuchObjectException("Candidate with ID " + CANDIDATE_ID + " not found"));
    // Second candidate assignment succeeds
    when(assignmentEngine.assign(eq(duolingoAllocator), eq(789L), eq(user)))
        .thenReturn(assignment2);

    // Act
    List<ServiceAssignment> result = duolingoService.assignToList(LIST_ID, user);

    // Assert
    assertThat(result).hasSize(1);
    assertThat(result).containsExactly(assignment2);
    verify(assignmentEngine).assign(duolingoAllocator, CANDIDATE_ID, user);
    verify(assignmentEngine).assign(duolingoAllocator, 789L, user);
  }

  @Test
  @DisplayName("assign to list fails when all multiple assignments fail")
  void assignToListFailsWhenAllMultipleAssignmentsFail() {
    // Arrange
    Candidate candidate2 = new Candidate();
    candidate2.setId(789L);
    candidate2.setCandidateNumber("C789");
    when(savedList.getCandidates()).thenReturn(Set.of(candidate, candidate2));

    when(savedListService.get(LIST_ID)).thenReturn(savedList);
    when(resourceRepository.countAvailableByProviderAndService(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED))
        .thenReturn(2L);
    when(assignmentRepository.findByCandidateAndProviderAndService(
        eq(CANDIDATE_ID), eq(ServiceProvider.DUOLINGO), eq(ServiceCode.TEST_PROCTORED)))
        .thenReturn(Collections.emptyList());
    when(assignmentRepository.findByCandidateAndProviderAndService(
        eq(789L), eq(ServiceProvider.DUOLINGO), eq(ServiceCode.TEST_PROCTORED)))
        .thenReturn(Collections.emptyList());
    // Both assignments fail
    when(assignmentEngine.assign(eq(duolingoAllocator), eq(CANDIDATE_ID), eq(user)))
        .thenThrow(new NoSuchObjectException("Candidate with ID " + CANDIDATE_ID + " not found"));
    when(assignmentEngine.assign(eq(duolingoAllocator), eq(789L), eq(user)))
        .thenThrow(new NoSuchObjectException("There are no available TEST_PROCTORED coupons to assign"));

    // Act & Assert
    assertThatThrownBy(() -> duolingoService.assignToList(LIST_ID, user))
        .isInstanceOf(NoSuchObjectException.class)
        .hasMessageContaining("Failed to assign")
        .hasMessageContaining("to any candidates")
        .hasMessageContaining("2 assignment(s) failed");

    verify(assignmentEngine).assign(duolingoAllocator, CANDIDATE_ID, user);
    verify(assignmentEngine).assign(duolingoAllocator, 789L, user);
  }

  @Test
  @DisplayName("assign to list: resource exhaustion mid-loop yields partial success and continues the loop")
  void assignToList_resourceExhaustionMidLoopYieldsPartialSuccessAndContinuesLoop() {
    // Arrange – count check passes (3 resources for 3 candidates), but two allocations fail
    // mid-loop because another thread consumed the resources between the count and the assign.
    Candidate candidate2 = new Candidate();
    candidate2.setId(789L);
    candidate2.setCandidateNumber("C789");
    Candidate candidate3 = new Candidate();
    candidate3.setId(999L);
    candidate3.setCandidateNumber("C999");
    when(savedList.getCandidates()).thenReturn(Set.of(candidate, candidate2, candidate3));

    when(savedListService.get(LIST_ID)).thenReturn(savedList);
    when(resourceRepository.countAvailableByProviderAndService(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED))
        .thenReturn(3L);

    when(assignmentRepository.findByCandidateAndProviderAndService(
        eq(CANDIDATE_ID), eq(ServiceProvider.DUOLINGO), eq(ServiceCode.TEST_PROCTORED)))
        .thenReturn(Collections.emptyList());
    when(assignmentRepository.findByCandidateAndProviderAndService(
        eq(789L), eq(ServiceProvider.DUOLINGO), eq(ServiceCode.TEST_PROCTORED)))
        .thenReturn(Collections.emptyList());
    when(assignmentRepository.findByCandidateAndProviderAndService(
        eq(999L), eq(ServiceProvider.DUOLINGO), eq(ServiceCode.TEST_PROCTORED)))
        .thenReturn(Collections.emptyList());

    // Candidate 1 succeeds; candidates 2 and 3 fail as the allocator finds no remaining resource
    NoSuchObjectException exhausted =
        new NoSuchObjectException("No available TEST_PROCTORED resources");
    when(assignmentEngine.assign(eq(duolingoAllocator), eq(CANDIDATE_ID), eq(user)))
        .thenReturn(assignment);
    when(assignmentEngine.assign(eq(duolingoAllocator), eq(789L), eq(user)))
        .thenThrow(exhausted);
    when(assignmentEngine.assign(eq(duolingoAllocator), eq(999L), eq(user)))
        .thenThrow(exhausted);

    // Act – at least one success, so no final exception is thrown
    List<ServiceAssignment> result = duolingoService.assignToList(LIST_ID, user);

    // Assert – partial result returned, loop continued past failures and attempted all candidates
    assertThat(result).hasSize(1).containsExactly(assignment);
    verify(assignmentEngine).assign(duolingoAllocator, CANDIDATE_ID, user);
    verify(assignmentEngine).assign(duolingoAllocator, 789L, user);
    verify(assignmentEngine).assign(duolingoAllocator, 999L, user);
  }

  @Test
  @DisplayName("assign to list logs an ERROR for each individual assignment failure")
  void assignToList_logsErrorForEachFailedIndividualAssignment() {
    // Capture log output emitted by AbstractCandidateAssistanceService via LogBuilder
    Logger serviceLogger =
        (Logger) LoggerFactory.getLogger(AbstractCandidateAssistanceService.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    serviceLogger.addAppender(listAppender);

    try {
      // Arrange – one candidate fails mid-loop, the other succeeds
      Candidate candidate2 = new Candidate();
      candidate2.setId(789L);
      candidate2.setCandidateNumber("C789");
      when(savedList.getCandidates()).thenReturn(Set.of(candidate, candidate2));

      when(savedListService.get(LIST_ID)).thenReturn(savedList);
      when(resourceRepository.countAvailableByProviderAndService(
          ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED))
          .thenReturn(2L);
      when(assignmentRepository.findByCandidateAndProviderAndService(
          eq(CANDIDATE_ID), eq(ServiceProvider.DUOLINGO), eq(ServiceCode.TEST_PROCTORED)))
          .thenReturn(Collections.emptyList());
      when(assignmentRepository.findByCandidateAndProviderAndService(
          eq(789L), eq(ServiceProvider.DUOLINGO), eq(ServiceCode.TEST_PROCTORED)))
          .thenReturn(Collections.emptyList());
      when(assignmentEngine.assign(eq(duolingoAllocator), eq(CANDIDATE_ID), eq(user)))
          .thenThrow(new NoSuchObjectException("No available TEST_PROCTORED resources"));
      when(assignmentEngine.assign(eq(duolingoAllocator), eq(789L), eq(user)))
          .thenReturn(assignment);

      // Act
      duolingoService.assignToList(LIST_ID, user);

      // Assert – exactly one ERROR log for the failing candidate
      List<ILoggingEvent> errors = listAppender.list.stream()
          .filter(e -> e.getLevel() == Level.ERROR)
          .toList();

      assertThat(errors).hasSize(1);
      String loggedMessage = errors.get(0).getFormattedMessage();
      assertThat(loggedMessage)
          .contains("action: assignToList")
          .contains("Failed to assign")
          .contains(CANDIDATE_ID.toString())
          .contains(CANDIDATE_NUMBER)
          .contains("No available TEST_PROCTORED resources");

    } finally {
      serviceLogger.detachAppender(listAppender);
    }
  }

  // Reassign Tests

  @Test
  @DisplayName("reassign for candidate succeeds")
  void reassignForCandidateSucceeds() {
    // Arrange
    when(assignmentEngine.reassign(duolingoAllocator, CANDIDATE_NUMBER, user))
        .thenReturn(assignment);

    // Act
    ServiceAssignment result = duolingoService.reassignForCandidate(CANDIDATE_NUMBER, user);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getCandidateId()).isEqualTo(CANDIDATE_ID);
    verify(assignmentEngine).reassign(duolingoAllocator, CANDIDATE_NUMBER, user);
  }

  @Test
  @DisplayName("reassign for candidate fails when candidate not found")
  void reassignForCandidateFailsWhenCandidateNotFound() {
    // Arrange
    when(assignmentEngine.reassign(duolingoAllocator, CANDIDATE_NUMBER, user))
        .thenThrow(new NoSuchObjectException("Candidate with Number " + CANDIDATE_NUMBER + " not found"));

    // Act & Assert
    assertThatThrownBy(() -> duolingoService.reassignForCandidate(CANDIDATE_NUMBER, user))
        .isInstanceOf(NoSuchObjectException.class)
        .hasMessageContaining("Candidate with Number " + CANDIDATE_NUMBER + " not found");
  }

  // Get Assignments for Candidate Tests

  @Test
  @DisplayName("get assignments for candidate returns assignments")
  void getAssignmentsForCandidateReturnsAssignments() {
    // Arrange
    when(assignmentRepository.findByCandidateAndProviderAndService(
        CANDIDATE_ID, ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED))
        .thenReturn(List.of(assignmentEntity));

    // Act
    List<ServiceAssignment> result = duolingoService.getAssignmentsForCandidate(CANDIDATE_ID);

    // Assert
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getCandidateId()).isEqualTo(CANDIDATE_ID);
  }

  @Test
  @DisplayName("get assignments for candidate returns empty list when no assignments")
  void getAssignmentsForCandidateReturnsEmptyList() {
    // Arrange
    when(assignmentRepository.findByCandidateAndProviderAndService(
        CANDIDATE_ID, ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED))
        .thenReturn(Collections.emptyList());

    // Act
    List<ServiceAssignment> result = duolingoService.getAssignmentsForCandidate(CANDIDATE_ID);

    // Assert
    assertThat(result).isEmpty();
  }

  // Get Resources for Candidate Tests

  @Test
  @DisplayName("get resources for candidate returns resources")
  void getResourcesForCandidateReturnsResources() {
    // Arrange
    when(assignmentRepository.findByCandidateAndProviderAndService(
        CANDIDATE_ID, ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED))
        .thenReturn(List.of(assignmentEntity));

    // Act
    List<ServiceResource> result = duolingoService.getResourcesForCandidate(CANDIDATE_ID);

    // Assert
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getResourceCode()).isEqualTo(RESOURCE_CODE);
  }

  @Test
  @DisplayName("get resources for candidate returns empty list when no assignments")
  void getResourcesForCandidateReturnsEmptyList() {
    // Arrange
    when(assignmentRepository.findByCandidateAndProviderAndService(
        CANDIDATE_ID, ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED))
        .thenReturn(Collections.emptyList());

    // Act
    List<ServiceResource> result = duolingoService.getResourcesForCandidate(CANDIDATE_ID);

    // Assert
    assertThat(result).isEmpty();
  }

  // Get Available Resources Tests

  @Test
  @DisplayName("get available resources returns resources")
  void getAvailableResourcesReturnsResources() {
    // Arrange
    when(resourceRepository.findByProviderAndServiceCodeAndStatus(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED, ResourceStatus.AVAILABLE))
        .thenReturn(List.of(resourceEntity));

    // Act
    List<ServiceResource> result = duolingoService.getAvailableResources();

    // Assert
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getResourceCode()).isEqualTo(RESOURCE_CODE);
    assertThat(result.get(0).getStatus()).isEqualTo(ResourceStatus.AVAILABLE);
  }

  @Test
  @DisplayName("get available resources returns empty list when no resources")
  void getAvailableResourcesReturnsEmptyList() {
    // Arrange
    when(resourceRepository.findByProviderAndServiceCodeAndStatus(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED, ResourceStatus.AVAILABLE))
        .thenReturn(Collections.emptyList());

    // Act
    List<ServiceResource> result = duolingoService.getAvailableResources();

    // Assert
    assertThat(result).isEmpty();
  }

  // Get Resource by Code Tests

  @Test
  @DisplayName("get resource by code returns resource when found")
  void getResourceByCodeReturnsResourceWhenFound() {
    // Arrange
    when(resourceRepository.findByProviderAndResourceCode(
        ServiceProvider.DUOLINGO, RESOURCE_CODE))
        .thenReturn(Optional.of(resourceEntity));

    // Act
    ServiceResource result = duolingoService.getResourceForResourceCode(RESOURCE_CODE);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getResourceCode()).isEqualTo(RESOURCE_CODE);
  }

  @Test
  @DisplayName("get resource by code throws exception when not found")
  void getResourceByCodeThrowsExceptionWhenNotFound() {
    // Arrange
    when(resourceRepository.findByProviderAndResourceCode(
        ServiceProvider.DUOLINGO, RESOURCE_CODE))
        .thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> duolingoService.getResourceForResourceCode(RESOURCE_CODE))
        .isInstanceOf(NoSuchObjectException.class)
        .hasMessageContaining("Coupon with code " + RESOURCE_CODE + " not found");
  }

  // Get Candidate for Resource Code Tests

  @Test
  @DisplayName("get candidate for resource code returns candidate when assigned")
  void getCandidateForResourceCodeReturnsCandidateWhenAssigned() {
    // Arrange
    when(resourceRepository.findByProviderAndResourceCode(
        ServiceProvider.DUOLINGO, RESOURCE_CODE))
        .thenReturn(Optional.of(resourceEntity));
    when(assignmentRepository.findTopByProviderAndServiceAndResource(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED, 1L))
        .thenReturn(Optional.of(assignmentEntity));

    // Act
    Candidate result = duolingoService.getCandidateForResourceCode(RESOURCE_CODE);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(CANDIDATE_ID);
  }

  @Test
  @DisplayName("get candidate for resource code returns null when not assigned")
  void getCandidateForResourceCodeReturnsNullWhenNotAssigned() {
    // Arrange
    when(resourceRepository.findByProviderAndResourceCode(
        ServiceProvider.DUOLINGO, RESOURCE_CODE))
        .thenReturn(Optional.of(resourceEntity));
    when(assignmentRepository.findTopByProviderAndServiceAndResource(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED, 1L))
        .thenReturn(Optional.empty());

    // Act
    Candidate result = duolingoService.getCandidateForResourceCode(RESOURCE_CODE);

    // Assert
    assertThat(result).isNull();
  }

  @Test
  @DisplayName("get candidate for resource code throws exception when resource not found")
  void getCandidateForResourceCodeThrowsExceptionWhenResourceNotFound() {
    // Arrange
    when(resourceRepository.findByProviderAndResourceCode(
        ServiceProvider.DUOLINGO, RESOURCE_CODE))
        .thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> duolingoService.getCandidateForResourceCode(RESOURCE_CODE))
        .isInstanceOf(NoSuchObjectException.class)
        .hasMessageContaining("Coupon with code " + RESOURCE_CODE + " not found");
  }

  // Update Resource Status Tests

  @Test
  @DisplayName("update resource status succeeds")
  void updateResourceStatusSucceeds() {
    // Arrange
    when(resourceRepository.findByProviderAndResourceCode(
        ServiceProvider.DUOLINGO, RESOURCE_CODE))
        .thenReturn(Optional.of(resourceEntity));
    when(resourceRepository.save(resourceEntity)).thenReturn(resourceEntity);

    // Act
    duolingoService.updateResourceStatus(RESOURCE_CODE, ResourceStatus.DISABLED);

    // Assert
    assertThat(resourceEntity.getStatus()).isEqualTo(ResourceStatus.DISABLED);
    verify(resourceRepository).save(resourceEntity);
  }

  @Test
  @DisplayName("update resource status throws exception when resource not found")
  void updateResourceStatusThrowsExceptionWhenResourceNotFound() {
    // Arrange
    when(resourceRepository.findByProviderAndResourceCode(
        ServiceProvider.DUOLINGO, RESOURCE_CODE))
        .thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> duolingoService.updateResourceStatus(RESOURCE_CODE, ResourceStatus.DISABLED))
        .isInstanceOf(NoSuchObjectException.class)
        .hasMessageContaining("Resource with code " + RESOURCE_CODE + " not found");

    verify(resourceRepository, never()).save(any());
  }

  // State Transition Tests

  @ParameterizedTest(name = "EXPIRED → {0} is rejected")
  @EnumSource(ResourceStatus.class)
  @DisplayName("updateResourceStatus rejects any transition from EXPIRED (terminal state)")
  void updateResourceStatus_rejectsAnyTransitionFromExpired(ResourceStatus target) {
    resourceEntity.setStatus(ResourceStatus.EXPIRED);
    when(resourceRepository.findByProviderAndResourceCode(ServiceProvider.DUOLINGO, RESOURCE_CODE))
        .thenReturn(Optional.of(resourceEntity));

    assertThatThrownBy(() -> duolingoService.updateResourceStatus(RESOURCE_CODE, target))
        .isInstanceOf(InvalidRequestException.class)
        .hasMessageContaining("EXPIRED");

    verify(resourceRepository, never()).save(any());
  }

  @ParameterizedTest(name = "REDEEMED → {0} is rejected")
  @EnumSource(ResourceStatus.class)
  @DisplayName("updateResourceStatus rejects any transition from REDEEMED (terminal state)")
  void updateResourceStatus_rejectsAnyTransitionFromRedeemed(ResourceStatus target) {
    resourceEntity.setStatus(ResourceStatus.REDEEMED);
    when(resourceRepository.findByProviderAndResourceCode(ServiceProvider.DUOLINGO, RESOURCE_CODE))
        .thenReturn(Optional.of(resourceEntity));

    assertThatThrownBy(() -> duolingoService.updateResourceStatus(RESOURCE_CODE, target))
        .isInstanceOf(InvalidRequestException.class)
        .hasMessageContaining("REDEEMED");

    verify(resourceRepository, never()).save(any());
  }

  @Test
  @DisplayName("updateResourceStatus allows AVAILABLE → DISABLED")
  void updateResourceStatus_allowsAvailableToDisabled() {
    resourceEntity.setStatus(ResourceStatus.AVAILABLE);
    when(resourceRepository.findByProviderAndResourceCode(ServiceProvider.DUOLINGO, RESOURCE_CODE))
        .thenReturn(Optional.of(resourceEntity));
    when(resourceRepository.save(resourceEntity)).thenReturn(resourceEntity);

    duolingoService.updateResourceStatus(RESOURCE_CODE, ResourceStatus.DISABLED);

    assertThat(resourceEntity.getStatus()).isEqualTo(ResourceStatus.DISABLED);
    verify(resourceRepository).save(resourceEntity);
  }

  @Test
  @DisplayName("updateResourceStatus allows DISABLED → AVAILABLE (re-enable)")
  void updateResourceStatus_allowsDisabledToAvailable() {
    resourceEntity.setStatus(ResourceStatus.DISABLED);
    when(resourceRepository.findByProviderAndResourceCode(ServiceProvider.DUOLINGO, RESOURCE_CODE))
        .thenReturn(Optional.of(resourceEntity));
    when(resourceRepository.save(resourceEntity)).thenReturn(resourceEntity);

    duolingoService.updateResourceStatus(RESOURCE_CODE, ResourceStatus.AVAILABLE);

    assertThat(resourceEntity.getStatus()).isEqualTo(ResourceStatus.AVAILABLE);
    verify(resourceRepository).save(resourceEntity);
  }

  @Test
  @DisplayName("updateResourceStatus allows SENT → DISABLED")
  void updateResourceStatus_allowsSentToDisabled() {
    resourceEntity.setStatus(ResourceStatus.SENT);
    when(resourceRepository.findByProviderAndResourceCode(ServiceProvider.DUOLINGO, RESOURCE_CODE))
        .thenReturn(Optional.of(resourceEntity));
    when(resourceRepository.save(resourceEntity)).thenReturn(resourceEntity);

    duolingoService.updateResourceStatus(RESOURCE_CODE, ResourceStatus.DISABLED);

    assertThat(resourceEntity.getStatus()).isEqualTo(ResourceStatus.DISABLED);
    verify(resourceRepository).save(resourceEntity);
  }

  @Test
  @DisplayName("updateResourceStatus allows RESERVED → AVAILABLE")
  void updateResourceStatus_allowsReservedToAvailable() {
    resourceEntity.setStatus(ResourceStatus.RESERVED);
    when(resourceRepository.findByProviderAndResourceCode(ServiceProvider.DUOLINGO, RESOURCE_CODE))
        .thenReturn(Optional.of(resourceEntity));
    when(resourceRepository.save(resourceEntity)).thenReturn(resourceEntity);

    duolingoService.updateResourceStatus(RESOURCE_CODE, ResourceStatus.AVAILABLE);

    assertThat(resourceEntity.getStatus()).isEqualTo(ResourceStatus.AVAILABLE);
    verify(resourceRepository).save(resourceEntity);
  }

  // Count Operations Tests

  @Test
  @DisplayName("count available for provider and service returns count")
  void countAvailableForProviderAndServiceReturnsCount() {
    // Arrange
    when(resourceRepository.countAvailableByProviderAndService(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED))
        .thenReturn(5L);

    // Act
    long result = duolingoService.countAvailableForProviderAndService();

    // Assert
    assertThat(result).isEqualTo(5L);
  }

  @Test
  @DisplayName("count available for provider returns count")
  void countAvailableForProviderReturnsCount() {
    // Arrange
    when(resourceRepository.countAvailableByProvider(ServiceProvider.DUOLINGO))
        .thenReturn(10L);

    // Act
    long result = duolingoService.countAvailableForProvider();

    // Assert
    assertThat(result).isEqualTo(10L);
  }

  @Test
  @DisplayName("count available for provider and service returns zero when no resources")
  void countAvailableForProviderAndServiceReturnsZero() {
    // Arrange
    when(resourceRepository.countAvailableByProviderAndService(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED))
        .thenReturn(0L);

    // Act
    long result = duolingoService.countAvailableForProviderAndService();

    // Assert
    assertThat(result).isEqualTo(0L);
  }

  @Test
  @DisplayName("count available for provider returns zero when no resources")
  void countAvailableForProviderReturnsZero() {
    // Arrange
    when(resourceRepository.countAvailableByProvider(ServiceProvider.DUOLINGO))
        .thenReturn(0L);

    // Act
    long result = duolingoService.countAvailableForProvider();

    // Assert
    assertThat(result).isEqualTo(0L);
  }
}

