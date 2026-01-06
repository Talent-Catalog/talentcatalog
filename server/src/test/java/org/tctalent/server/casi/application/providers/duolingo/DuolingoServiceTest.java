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
    when(resourceRepository.findByProviderAndServiceCodeAndStatus(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED, ResourceStatus.AVAILABLE))
        .thenReturn(List.of(resourceEntity, resourceEntity));
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
    when(resourceRepository.findByProviderAndServiceCodeAndStatus(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED, ResourceStatus.AVAILABLE))
        .thenReturn(List.of(resourceEntity)); // Only 1 resource for 2 candidates

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
    when(resourceRepository.findByProviderAndServiceCodeAndStatus(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED, ResourceStatus.AVAILABLE))
        .thenReturn(List.of(resourceEntity, resourceEntity));
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
    when(resourceRepository.findByProviderAndServiceCodeAndStatus(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED, ResourceStatus.AVAILABLE))
        .thenReturn(List.of(resourceEntity)); // Has resources but no candidates

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
    when(resourceRepository.findByProviderAndServiceCodeAndStatus(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED, ResourceStatus.AVAILABLE))
        .thenReturn(List.of(resourceEntity, resourceEntity)); // Exactly 2 resources for 2 candidates
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
    when(resourceRepository.findByProviderAndServiceCodeAndStatus(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED, ResourceStatus.AVAILABLE))
        .thenReturn(List.of(resourceEntity));
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
    when(resourceRepository.findByProviderAndServiceCodeAndStatus(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED, ResourceStatus.AVAILABLE))
        .thenReturn(List.of(resourceEntity, resourceEntity));
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
    when(resourceRepository.findByProviderAndServiceCodeAndStatus(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED, ResourceStatus.AVAILABLE))
        .thenReturn(List.of(resourceEntity, resourceEntity));
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


}

