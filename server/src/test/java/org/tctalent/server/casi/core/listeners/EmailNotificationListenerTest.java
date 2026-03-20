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

package org.tctalent.server.casi.core.listeners;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.casi.domain.events.ServiceAssignedEvent;
import org.tctalent.server.casi.domain.model.AssignmentStatus;
import org.tctalent.server.casi.domain.model.ResourceStatus;
import org.tctalent.server.casi.domain.model.ServiceAssignment;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.casi.domain.model.ServiceResource;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.service.db.email.EmailHelper;

@ExtendWith(MockitoExtension.class)
class EmailNotificationListenerTest {

  private static final Long CANDIDATE_ID = 123L;

  @Mock
  private CandidateRepository candidateRepository;
  @Mock
  private EmailHelper emailHelper;

  @InjectMocks
  private EmailNotificationListener listener;

  private ServiceAssignment assignment;
  private Candidate candidate;
  private User candidateUser;

  @BeforeEach
  void setUp() {
    ServiceResource resource = ServiceResource.builder()
        .id(1L)
        .provider(ServiceProvider.DUOLINGO)
        .serviceCode(ServiceCode.TEST_PROCTORED)
        .resourceCode("COUPON123")
        .status(ResourceStatus.AVAILABLE)
        .build();

    assignment = ServiceAssignment.builder()
        .id(1L)
        .provider(ServiceProvider.DUOLINGO)
        .serviceCode(ServiceCode.TEST_PROCTORED)
        .resource(resource)
        .candidateId(CANDIDATE_ID)
        .actorId(1L)
        .status(AssignmentStatus.ASSIGNED)
        .assignedAt(OffsetDateTime.now())
        .build();

    candidateUser = new User();
    candidateUser.setId(456L);
    candidateUser.setEmail("candidate@example.com");

    candidate = new Candidate();
    candidate.setId(CANDIDATE_ID);
    candidate.setUser(candidateUser);
  }

  @Test
  @DisplayName("onAssigned sends email for Duolingo service")
  void onAssignedSendsEmailForDuolingo() {
    // Arrange
    when(candidateRepository.findById(CANDIDATE_ID))
        .thenReturn(Optional.of(candidate));

    ServiceAssignedEvent event = new ServiceAssignedEvent(assignment);

    // Act
    listener.onAssigned(event);

    // Assert
    verify(emailHelper).sendDuolingoCouponEmail(candidateUser);
  }

  @Test
  @DisplayName("onAssigned sends email for Duolingo service with different service code")
  void onAssignedSendsEmailForDuolingoWithDifferentServiceCode() {
    // Arrange
    ServiceResource otherResource = ServiceResource.builder()
        .id(2L)
        .provider(ServiceProvider.DUOLINGO)
        .serviceCode(ServiceCode.TEST_NON_PROCTORED)
        .resourceCode("OTHER123")
        .status(ResourceStatus.AVAILABLE)
        .build();

    ServiceAssignment otherAssignment = ServiceAssignment.builder()
        .id(2L)
        .provider(ServiceProvider.DUOLINGO)
        .serviceCode(ServiceCode.TEST_NON_PROCTORED)
        .resource(otherResource)
        .candidateId(CANDIDATE_ID)
        .actorId(1L)
        .status(AssignmentStatus.ASSIGNED)
        .assignedAt(OffsetDateTime.now())
        .build();

    when(candidateRepository.findById(CANDIDATE_ID))
        .thenReturn(Optional.of(candidate));

    ServiceAssignedEvent event = new ServiceAssignedEvent(otherAssignment);

    // Act
    listener.onAssigned(event);

    // Assert
    // Current implementation sends emails for all Duolingo services regardless of service code
    verify(emailHelper).sendDuolingoCouponEmail(candidateUser);
  }

  // TODO: Add test for "onAssigned does not send email for non-Duolingo service"
  // when ServiceProvider enum includes providers other than DUOLINGO

  @Test
  @DisplayName("onAssigned handles missing candidate ID gracefully")
  void onAssignedHandlesMissingCandidateId() {
    // Arrange
    ServiceAssignment assignmentWithoutCandidate = ServiceAssignment.builder()
        .id(1L)
        .provider(ServiceProvider.DUOLINGO)
        .serviceCode(ServiceCode.TEST_PROCTORED)
        .resource(assignment.getResource())
        .candidateId(null) // Missing candidate ID
        .actorId(1L)
        .status(AssignmentStatus.ASSIGNED)
        .assignedAt(OffsetDateTime.now())
        .build();

    ServiceAssignedEvent event = new ServiceAssignedEvent(assignmentWithoutCandidate);

    // Act
    listener.onAssigned(event);

    // Assert
    verify(candidateRepository, never()).findById(any());
    verify(emailHelper, never()).sendDuolingoCouponEmail(any());
  }

  @Test
  @DisplayName("onAssigned handles candidate not found gracefully")
  void onAssignedHandlesCandidateNotFound() {
    // Arrange
    when(candidateRepository.findById(CANDIDATE_ID))
        .thenReturn(Optional.empty());

    ServiceAssignedEvent event = new ServiceAssignedEvent(assignment);

    // Act
    listener.onAssigned(event);

    // Assert
    verify(emailHelper, never()).sendDuolingoCouponEmail(any());
  }

  @Test
  @DisplayName("onAssigned handles email sending failure gracefully")
  void onAssignedHandlesEmailSendingFailure() {
    // Arrange
    when(candidateRepository.findById(CANDIDATE_ID))
        .thenReturn(Optional.of(candidate));
    doThrow(new RuntimeException("Email service unavailable"))
        .when(emailHelper).sendDuolingoCouponEmail(candidateUser);

    ServiceAssignedEvent event = new ServiceAssignedEvent(assignment);

    // Act - Should not throw exception
    listener.onAssigned(event);

    // Assert
    verify(emailHelper).sendDuolingoCouponEmail(candidateUser);
    // Exception should be caught and logged, not propagated
  }
}

