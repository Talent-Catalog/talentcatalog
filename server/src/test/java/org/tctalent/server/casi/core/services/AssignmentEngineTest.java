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

package org.tctalent.server.casi.core.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.tctalent.server.casi.core.allocators.ResourceAllocator;
import org.tctalent.server.casi.domain.events.ServiceAssignedEvent;
import org.tctalent.server.casi.domain.events.ServiceReassignedEvent;
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
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidateRepository;

@ExtendWith(MockitoExtension.class)
class AssignmentEngineTest {

  private static final Long CANDIDATE_ID = 123L;
  private static final String CANDIDATE_NUMBER = "C12345";
  private static final String RESOURCE_CODE = "COUPON123";

  @Mock private CandidateRepository candidateRepository;
  @Mock private ServiceAssignmentRepository assignmentRepository;
  @Mock private ServiceResourceRepository resourceRepository;
  @Mock private ApplicationEventPublisher eventPublisher;
  @Mock private ResourceAllocator allocator;

  @InjectMocks
  private AssignmentEngine assignmentEngine;

  private Candidate candidate;
  private User actor;
  private ServiceResource resource;
  private ServiceResourceEntity resourceEntity;
  private ServiceAssignmentEntity existingAssignment;

  @BeforeEach
  void setUp() {
    actor = new User();
    actor.setId(1L);
    actor.setUsername("testuser");

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

    existingAssignment = new ServiceAssignmentEntity();
    existingAssignment.setId(100L);
    existingAssignment.setCandidate(candidate);
    existingAssignment.setResource(resourceEntity);
    existingAssignment.setActor(actor);
    existingAssignment.setStatus(AssignmentStatus.ASSIGNED);
  }

  @Test
  @DisplayName("assign succeeds and publishes event")
  void assignSucceeds() {
    // Arrange
    when(allocator.getProvider()).thenReturn(ServiceProvider.DUOLINGO);
    when(allocator.getServiceCode()).thenReturn(ServiceCode.TEST_PROCTORED);
    when(candidateRepository.findById(CANDIDATE_ID))
        .thenReturn(Optional.of(candidate));
    when(allocator.allocateFor(candidate)).thenReturn(resource);
    when(resourceRepository.getReferenceById(1L)).thenReturn(resourceEntity);
    when(assignmentRepository.save(any(ServiceAssignmentEntity.class)))
        .thenAnswer(invocation -> {
          ServiceAssignmentEntity entity = invocation.getArgument(0);
          entity.setId(1L);
          return entity;
        });

    // Act
    ServiceAssignment result = assignmentEngine.assign(allocator, CANDIDATE_ID, actor);

    // Assert
    // Verify ServiceAssigment result
    assertThat(result).isNotNull();
    assertThat(result.getCandidateId()).isEqualTo(CANDIDATE_ID);
    assertThat(result.getActorId()).isEqualTo(actor.getId());
    assertThat(result.getStatus()).isEqualTo(AssignmentStatus.ASSIGNED);
    assertThat(result.getProvider()).isEqualTo(ServiceProvider.DUOLINGO);
    assertThat(result.getServiceCode()).isEqualTo(ServiceCode.TEST_PROCTORED);
    assertThat(result.getResource()).isNotNull();
    assertThat(result.getResource().getResourceCode()).isEqualTo(RESOURCE_CODE);

    // Capture and verify ServiceAssignmentEntity
    ArgumentCaptor<ServiceAssignmentEntity> entityCaptor =
        ArgumentCaptor.forClass(ServiceAssignmentEntity.class);
    verify(assignmentRepository).save(entityCaptor.capture());
    ServiceAssignmentEntity savedEntity = entityCaptor.getValue();

    assertThat(savedEntity.getCandidate().getId()).isEqualTo(CANDIDATE_ID);
    assertThat(savedEntity.getActor().getId()).isEqualTo(actor.getId());
    assertThat(savedEntity.getStatus()).isEqualTo(AssignmentStatus.ASSIGNED);
    assertThat(savedEntity.getProvider()).isEqualTo(ServiceProvider.DUOLINGO);
    assertThat(savedEntity.getServiceCode()).isEqualTo(ServiceCode.TEST_PROCTORED);
    assertThat(savedEntity.getAssignedAt()).isNotNull();

    // Capture and verify ServiceAssignedEvent
    ArgumentCaptor<ServiceAssignedEvent> eventCaptor =
        ArgumentCaptor.forClass(ServiceAssignedEvent.class);
    verify(eventPublisher).publishEvent(eventCaptor.capture());
    ServiceAssignedEvent event = eventCaptor.getValue();

    assertThat(event.assignment()).isNotNull();
    assertThat(event.assignment().getCandidateId()).isEqualTo(CANDIDATE_ID);
    assertThat(event.assignment().getProvider()).isEqualTo(ServiceProvider.DUOLINGO);
    assertThat(event.assignment().getServiceCode()).isEqualTo(ServiceCode.TEST_PROCTORED);
  }

  @Test
  @DisplayName("assign fails when candidate not found")
  void assignFailsWhenCandidateNotFound() {
    // Arrange
    when(candidateRepository.findById(CANDIDATE_ID))
        .thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> assignmentEngine.assign(allocator, CANDIDATE_ID, actor))
        .isInstanceOf(NoSuchObjectException.class)
        .hasMessageContaining("Candidate with ID " + CANDIDATE_ID + " not found");

    verify(allocator, never()).allocateFor(any());
    verify(assignmentRepository, never()).save(any());
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  @DisplayName("assign fails when no resources available")
  void assignFailsWhenNoResourcesAvailable() {
    // Arrange
    when(candidateRepository.findById(CANDIDATE_ID))
        .thenReturn(Optional.of(candidate));
    when(allocator.allocateFor(candidate))
        .thenThrow(new NoSuchObjectException("No available resources"));

    // Act & Assert
    assertThatThrownBy(() -> assignmentEngine.assign(allocator, CANDIDATE_ID, actor))
        .isInstanceOf(NoSuchObjectException.class)
        .hasMessageContaining("No available resources");

    verify(assignmentRepository, never()).save(any());
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  @DisplayName("reassign succeeds and marks previous assignments")
  void reassignSucceeds() {
    // Arrange
    when(allocator.getProvider()).thenReturn(ServiceProvider.DUOLINGO);
    when(allocator.getServiceCode()).thenReturn(ServiceCode.TEST_PROCTORED);
    when(candidateRepository.findByCandidateNumber(CANDIDATE_NUMBER))
        .thenReturn(candidate);
    when(assignmentRepository.findByCandidateAndProviderAndService(
        CANDIDATE_ID, ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED))
        .thenReturn(List.of(existingAssignment));
    when(candidateRepository.findById(CANDIDATE_ID))
        .thenReturn(Optional.of(candidate));
    when(allocator.allocateFor(candidate)).thenReturn(resource);
    when(resourceRepository.getReferenceById(1L)).thenReturn(resourceEntity);
    when(assignmentRepository.save(any(ServiceAssignmentEntity.class)))
        .thenAnswer(invocation -> {
          ServiceAssignmentEntity entity = invocation.getArgument(0);
          if (entity.getId() == null) {
            entity.setId(2L);
          }
          return entity;
        });

    // Act
    ServiceAssignment result = assignmentEngine.reassign(allocator, CANDIDATE_NUMBER, actor);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getCandidateId()).isEqualTo(CANDIDATE_ID);

    // Verify previous assignment was marked as REASSIGNED
    verify(assignmentRepository).save(existingAssignment);
    assertThat(existingAssignment.getStatus()).isEqualTo(AssignmentStatus.REASSIGNED);

    // Verify resource was disabled
    verify(resourceRepository).save(resourceEntity);
    assertThat(resourceEntity.getStatus()).isEqualTo(ResourceStatus.DISABLED);

    // Verify reassigned event was published
    ArgumentCaptor<ServiceReassignedEvent> eventCaptor =
        ArgumentCaptor.forClass(ServiceReassignedEvent.class);
    verify(eventPublisher).publishEvent(eventCaptor.capture());
    assertThat(eventCaptor.getValue().assignment()).isNotNull();

    // Verify new assignment event was published
    ArgumentCaptor<ServiceAssignedEvent> assignedEventCaptor =
        ArgumentCaptor.forClass(ServiceAssignedEvent.class);
    verify(eventPublisher, org.mockito.Mockito.atLeastOnce())
        .publishEvent(assignedEventCaptor.capture());
  }

  @Test
  @DisplayName("reassign fails when candidate not found")
  void reassignFailsWhenCandidateNotFound() {
    // Arrange
    when(candidateRepository.findByCandidateNumber(CANDIDATE_NUMBER))
        .thenReturn(null);

    // Act & Assert
    assertThatThrownBy(() -> assignmentEngine.reassign(allocator, CANDIDATE_NUMBER, actor))
        .isInstanceOf(NoSuchObjectException.class)
        .hasMessageContaining("Candidate with Number " + CANDIDATE_NUMBER + " not found");

    verify(allocator, never()).allocateFor(any());
    verify(assignmentRepository, never()).save(any());
  }

  @Test
  @DisplayName("reassign handles candidate with no previous assignments")
  void reassignHandlesNoPreviousAssignments() {
    // Arrange
    when(allocator.getProvider()).thenReturn(ServiceProvider.DUOLINGO);
    when(allocator.getServiceCode()).thenReturn(ServiceCode.TEST_PROCTORED);
    when(candidateRepository.findByCandidateNumber(CANDIDATE_NUMBER))
        .thenReturn(candidate);
    when(assignmentRepository.findByCandidateAndProviderAndService(
        CANDIDATE_ID, ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED))
        .thenReturn(List.of());
    when(candidateRepository.findById(CANDIDATE_ID))
        .thenReturn(Optional.of(candidate));
    when(allocator.allocateFor(candidate)).thenReturn(resource);
    when(resourceRepository.getReferenceById(1L)).thenReturn(resourceEntity);
    when(assignmentRepository.save(any(ServiceAssignmentEntity.class)))
        .thenAnswer(invocation -> {
          ServiceAssignmentEntity entity = invocation.getArgument(0);
          entity.setId(1L);
          return entity;
        });

    // Act
    ServiceAssignment result = assignmentEngine.reassign(allocator, CANDIDATE_NUMBER, actor);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getCandidateId()).isEqualTo(CANDIDATE_ID);

    // Verify no previous assignments were updated
    verify(assignmentRepository, never()).save(existingAssignment);

    // Verify no reassigned event was published (no previous assignments)
    verify(eventPublisher, never()).publishEvent(any(ServiceReassignedEvent.class));

    // Verify one assigned event was published (from the new assignment)
    ArgumentCaptor<ServiceAssignedEvent> assignedEventCaptor =
        ArgumentCaptor.forClass(ServiceAssignedEvent.class);
    verify(eventPublisher, org.mockito.Mockito.times(1))
        .publishEvent(assignedEventCaptor.capture());
    assertThat(assignedEventCaptor.getValue().assignment()).isNotNull();
    assertThat(assignedEventCaptor.getValue().assignment().getCandidateId()).isEqualTo(CANDIDATE_ID);
  }

  @Test
  @DisplayName("reassign handles multiple previous assignments")
  void reassignHandlesMultiplePreviousAssignments() {
    // Arrange
    ServiceAssignmentEntity assignment2 = new ServiceAssignmentEntity();
    assignment2.setId(101L);
    assignment2.setCandidate(candidate);
    assignment2.setResource(resourceEntity);
    assignment2.setActor(actor);
    assignment2.setStatus(AssignmentStatus.ASSIGNED);

    when(allocator.getProvider()).thenReturn(ServiceProvider.DUOLINGO);
    when(allocator.getServiceCode()).thenReturn(ServiceCode.TEST_PROCTORED);
    when(candidateRepository.findByCandidateNumber(CANDIDATE_NUMBER))
        .thenReturn(candidate);
    when(assignmentRepository.findByCandidateAndProviderAndService(
        CANDIDATE_ID, ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED))
        .thenReturn(List.of(existingAssignment, assignment2));
    when(candidateRepository.findById(CANDIDATE_ID))
        .thenReturn(Optional.of(candidate));
    when(allocator.allocateFor(candidate)).thenReturn(resource);
    when(resourceRepository.getReferenceById(1L)).thenReturn(resourceEntity);
    when(assignmentRepository.save(any(ServiceAssignmentEntity.class)))
        .thenAnswer(invocation -> {
          ServiceAssignmentEntity entity = invocation.getArgument(0);
          if (entity.getId() == null) {
            entity.setId(2L);
          }
          return entity;
        });

    // Act
    ServiceAssignment result = assignmentEngine.reassign(allocator, CANDIDATE_NUMBER, actor);

    // Assert
    assertThat(result).isNotNull();

    // Verify both previous assignments were marked as REASSIGNED
    verify(assignmentRepository).save(existingAssignment);
    verify(assignmentRepository).save(assignment2);
    assertThat(existingAssignment.getStatus()).isEqualTo(AssignmentStatus.REASSIGNED);
    assertThat(assignment2.getStatus()).isEqualTo(AssignmentStatus.REASSIGNED);

    // Verify 2 reassigned events were published (one for each previous assignment)
    ArgumentCaptor<ServiceReassignedEvent> reassignedEventCaptor =
        ArgumentCaptor.forClass(ServiceReassignedEvent.class);
    verify(eventPublisher, org.mockito.Mockito.times(2))
        .publishEvent(reassignedEventCaptor.capture());
    List<ServiceReassignedEvent> reassignedEvents = reassignedEventCaptor.getAllValues();
    assertThat(reassignedEvents).hasSize(2);
    assertThat(reassignedEvents.get(0).assignment()).isNotNull();
    assertThat(reassignedEvents.get(1).assignment()).isNotNull();

    // Verify 1 assigned event was published (for the new assignment)
    ArgumentCaptor<ServiceAssignedEvent> assignedEventCaptor =
        ArgumentCaptor.forClass(ServiceAssignedEvent.class);
    verify(eventPublisher, org.mockito.Mockito.times(1))
        .publishEvent(assignedEventCaptor.capture());
    assertThat(assignedEventCaptor.getValue().assignment()).isNotNull();
    assertThat(assignedEventCaptor.getValue().assignment().getCandidateId()).isEqualTo(CANDIDATE_ID);
  }
}

