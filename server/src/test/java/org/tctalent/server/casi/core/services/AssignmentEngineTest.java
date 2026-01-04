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
    assertThat(savedEntity.getAssignedAt()).isNotNull();

    // Capture and verify ServiceAssignedEvent
    ArgumentCaptor<ServiceAssignedEvent> eventCaptor =
        ArgumentCaptor.forClass(ServiceAssignedEvent.class);
    verify(eventPublisher).publishEvent(eventCaptor.capture());
    ServiceAssignedEvent event = eventCaptor.getValue();

    assertThat(event.assignment()).isNotNull();
    assertThat(event.assignment().getCandidateId()).isEqualTo(CANDIDATE_ID);
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

}

