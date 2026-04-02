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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.casi.application.policy.TaskPolicy;
import org.tctalent.server.casi.application.policy.TaskPolicyRegistry;
import org.tctalent.server.casi.domain.events.ServiceAssignedEvent;
import org.tctalent.server.casi.domain.events.ServiceExpiredEvent;
import org.tctalent.server.casi.domain.events.ServiceReassignedEvent;
import org.tctalent.server.casi.domain.events.ServiceRedeemedEvent;
import org.tctalent.server.casi.domain.model.AssignmentStatus;
import org.tctalent.server.casi.domain.model.ResourceStatus;
import org.tctalent.server.casi.domain.model.ServiceAssignment;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.casi.domain.model.ServiceResource;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.TaskAssignmentImpl;
import org.tctalent.server.model.db.TaskImpl;
import org.tctalent.server.model.db.User;
import org.tctalent.server.service.db.TaskAssignmentService;
import org.tctalent.server.service.db.TaskService;

@ExtendWith(MockitoExtension.class)
class ServiceTaskOrchestratorTest {

  private static final Long CANDIDATE_ID = 123L;
  private static final Long ACTOR_ID = 1L;
  private static final String TASK_NAME_1 = "claimCouponButton";
  private static final String TASK_NAME_2 = "duolingoTest";

  @Mock private TaskService taskService;
  @Mock private TaskAssignmentService taskAssignmentService;
  @Mock private TaskPolicyRegistry policyRegistry;
  @Mock private TaskPolicy taskPolicy;

  @InjectMocks
  private ServiceTaskOrchestrator orchestrator;

  private ServiceAssignment assignment;
  private TaskImpl task1;
  private TaskImpl task2;
  private TaskAssignmentImpl taskAssignment;

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
        .actorId(ACTOR_ID)
        .status(AssignmentStatus.ASSIGNED)
        .assignedAt(OffsetDateTime.now())
        .build();

    task1 = new TaskImpl();
    task1.setId(1L);
    task1.setName(TASK_NAME_1);

    task2 = new TaskImpl();
    task2.setId(2L);
    task2.setName(TASK_NAME_2);

    Candidate candidate = new Candidate();
    candidate.setId(CANDIDATE_ID);

    taskAssignment = new TaskAssignmentImpl();
    taskAssignment.setId(100L);
    taskAssignment.setStatus(Status.active);
    taskAssignment.setCandidate(candidate);

    when(policyRegistry.forProvider(ServiceProvider.DUOLINGO))
        .thenReturn(taskPolicy);
  }

  @Test
  @DisplayName("onAssigned assigns tasks from policy")
  void onAssignedAssignsTasks() {
    // Arrange
    when(taskPolicy.tasksOnAssigned(any(ServiceAssignedEvent.class)))
        .thenReturn(List.of(TASK_NAME_1, TASK_NAME_2));
    when(taskService.getByName(TASK_NAME_1)).thenReturn(task1);
    when(taskService.getByName(TASK_NAME_2)).thenReturn(task2);

    ServiceAssignedEvent event = new ServiceAssignedEvent(assignment);

    // Act
    orchestrator.onAssigned(event);

    // Assert
    verify(taskService).getByName(TASK_NAME_1);
    verify(taskService).getByName(TASK_NAME_2);
    verify(taskAssignmentService, times(2)).assignTaskToCandidate(
        any(User.class), any(TaskImpl.class), any(Candidate.class), isNull(), isNull());
  }

  @Test
  @DisplayName("onAssigned handles empty task list")
  void onAssignedHandlesEmptyTaskList() {
    // Arrange
    when(taskPolicy.tasksOnAssigned(any(ServiceAssignedEvent.class)))
        .thenReturn(List.of());

    ServiceAssignedEvent event = new ServiceAssignedEvent(assignment);

    // Act
    orchestrator.onAssigned(event);

    // Assert
    verify(taskService, never()).getByName(anyString());
    verify(taskAssignmentService, never()).assignTaskToCandidate(
        any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("onAssigned is silent when provider has no task policy")
  void onAssignedIsNoOpWhenPolicyNotFound() {
    // Arrange
    when(policyRegistry.forProvider(ServiceProvider.DUOLINGO))
        .thenReturn(noOpPolicy());

    ServiceAssignedEvent event = new ServiceAssignedEvent(assignment);

    // Act
    orchestrator.onAssigned(event);

    // Assert
    verify(taskService, never()).getByName(anyString());
    verify(taskAssignmentService, never()).assignTaskToCandidate(
        any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("onRedeemed assigns tasks from policy")
  void onRedeemedAssignsTasks() {
    // Arrange
    when(taskPolicy.tasksOnRedeemed(any(ServiceRedeemedEvent.class)))
        .thenReturn(List.of(TASK_NAME_2));
    when(taskService.getByName(TASK_NAME_2)).thenReturn(task2);

    ServiceRedeemedEvent event = new ServiceRedeemedEvent(assignment);

    // Act
    orchestrator.onRedeemed(event);

    // Assert
    verify(taskService).getByName(TASK_NAME_2);
    verify(taskAssignmentService).assignTaskToCandidate(
        any(User.class), eq(task2), any(Candidate.class), isNull(), isNull());
  }

  @Test
  @DisplayName("onRedeemed is silent when provider has no task policy")
  void onRedeemedIsNoOpWhenPolicyNotFound() {
    // Arrange
    when(policyRegistry.forProvider(ServiceProvider.DUOLINGO))
        .thenReturn(noOpPolicy());

    ServiceRedeemedEvent event = new ServiceRedeemedEvent(assignment);

    // Act
    orchestrator.onRedeemed(event);

    // Assert
    verify(taskService, never()).getByName(anyString());
    verify(taskAssignmentService, never()).assignTaskToCandidate(
        any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("onReassigned deactivates existing task assignments")
  void onReassignedDeactivatesTasks() {
    // Arrange
    when(taskPolicy.tasksOnReassigned(any(ServiceReassignedEvent.class)))
        .thenReturn(List.of(TASK_NAME_2));
    when(taskService.getByName(TASK_NAME_2)).thenReturn(task2);
    when(taskAssignmentService.findByTaskIdAndCandidateIdAndStatus(
        task2.getId(), CANDIDATE_ID, Status.active))
        .thenReturn(List.of(taskAssignment));

    ServiceReassignedEvent event = new ServiceReassignedEvent(assignment);

    // Act
    orchestrator.onReassigned(event);

    // Assert
    verify(taskService).getByName(TASK_NAME_2);
    verify(taskAssignmentService).findByTaskIdAndCandidateIdAndStatus(
        task2.getId(), CANDIDATE_ID, Status.active);
    verify(taskAssignmentService).update(
        eq(taskAssignment), isNull(), eq(true),
        eq("Marked inactive due to reassignment"), isNull());
    assertThat(taskAssignment.getStatus()).isEqualTo(Status.inactive);
  }

  @Test
  @DisplayName("onReassigned handles no existing task assignments")
  void onReassignedHandlesNoExistingTasks() {
    // Arrange
    when(taskPolicy.tasksOnReassigned(any(ServiceReassignedEvent.class)))
        .thenReturn(List.of(TASK_NAME_2));
    when(taskService.getByName(TASK_NAME_2)).thenReturn(task2);
    when(taskAssignmentService.findByTaskIdAndCandidateIdAndStatus(
        task2.getId(), CANDIDATE_ID, Status.active))
        .thenReturn(List.of());

    ServiceReassignedEvent event = new ServiceReassignedEvent(assignment);

    // Act
    orchestrator.onReassigned(event);

    // Assert
    verify(taskAssignmentService, never()).update(any(), any(), anyBoolean(), any(), any());
  }

  @Test
  @DisplayName("onReassigned is silent when provider has no task policy")
  void onReassignedIsNoOpWhenPolicyNotFound() {
    // Arrange
    when(policyRegistry.forProvider(ServiceProvider.DUOLINGO))
        .thenReturn(noOpPolicy());

    ServiceReassignedEvent event = new ServiceReassignedEvent(assignment);

    // Act
    orchestrator.onReassigned(event);

    // Assert
    verify(taskService, never()).getByName(anyString());
    verify(taskAssignmentService, never()).findByTaskIdAndCandidateIdAndStatus(
        any(), any(), any());
    verify(taskAssignmentService, never()).update(any(), any(), anyBoolean(), any(), any());
  }

  @Test
  @DisplayName("onReassigned handles multiple existing task assignments")
  void onReassignedHandlesMultipleExistingTasks() {
    // Arrange
    Candidate candidate = new Candidate();
    candidate.setId(CANDIDATE_ID);

    TaskAssignmentImpl taskAssignment2 = new TaskAssignmentImpl();
    taskAssignment2.setId(101L);
    taskAssignment2.setStatus(Status.active);
    taskAssignment2.setCandidate(candidate);

    when(taskPolicy.tasksOnReassigned(any(ServiceReassignedEvent.class)))
        .thenReturn(List.of(TASK_NAME_2));
    when(taskService.getByName(TASK_NAME_2)).thenReturn(task2);
    when(taskAssignmentService.findByTaskIdAndCandidateIdAndStatus(
        task2.getId(), CANDIDATE_ID, Status.active))
        .thenReturn(List.of(taskAssignment, taskAssignment2));

    ServiceReassignedEvent event = new ServiceReassignedEvent(assignment);

    // Act
    orchestrator.onReassigned(event);

    // Assert
    verify(taskAssignmentService, times(2)).update(
        any(TaskAssignmentImpl.class), isNull(), eq(true),
        eq("Marked inactive due to reassignment"), isNull());
    assertThat(taskAssignment.getStatus()).isEqualTo(Status.inactive);
    assertThat(taskAssignment2.getStatus()).isEqualTo(Status.inactive);
  }

  @Test
  @DisplayName("onExpired deactivates existing task assignments")
  void onExpiredDeactivatesTasks() {
    // Arrange
    when(taskPolicy.tasksOnExpired(any(ServiceExpiredEvent.class)))
        .thenReturn(List.of(TASK_NAME_2));
    when(taskService.getByName(TASK_NAME_2)).thenReturn(task2);
    when(taskAssignmentService.findByTaskIdAndCandidateIdAndStatus(
        task2.getId(), CANDIDATE_ID, Status.active))
        .thenReturn(List.of(taskAssignment));

    ServiceExpiredEvent event = new ServiceExpiredEvent(assignment);

    // Act
    orchestrator.onExpired(event);

    // Assert
    verify(taskAssignmentService).update(
        eq(taskAssignment), isNull(), eq(true),
        eq("Marked inactive due to expiration"), isNull());
    assertThat(taskAssignment.getStatus()).isEqualTo(Status.inactive);
  }

  @Test
  @DisplayName("onExpired handles no existing task assignments")
  void onExpiredHandlesNoExistingTasks() {
    // Arrange
    when(taskPolicy.tasksOnExpired(any(ServiceExpiredEvent.class)))
        .thenReturn(List.of(TASK_NAME_2));
    when(taskService.getByName(TASK_NAME_2)).thenReturn(task2);
    when(taskAssignmentService.findByTaskIdAndCandidateIdAndStatus(
        task2.getId(), CANDIDATE_ID, Status.active))
        .thenReturn(List.of());

    ServiceExpiredEvent event = new ServiceExpiredEvent(assignment);

    // Act
    orchestrator.onExpired(event);

    // Assert
    verify(taskAssignmentService, never()).update(any(), any(), anyBoolean(), any(), any());
  }

  @Test
  @DisplayName("onExpired is silent when provider has no task policy")
  void onExpiredIsNoOpWhenPolicyNotFound() {
    // Arrange
    when(policyRegistry.forProvider(ServiceProvider.DUOLINGO))
        .thenReturn(noOpPolicy());

    ServiceExpiredEvent event = new ServiceExpiredEvent(assignment);

    // Act
    orchestrator.onExpired(event);

    // Assert
    verify(taskService, never()).getByName(anyString());
    verify(taskAssignmentService, never()).findByTaskIdAndCandidateIdAndStatus(
        any(), any(), any());
    verify(taskAssignmentService, never()).update(any(), any(), anyBoolean(), any(), any());
  }

  private TaskPolicy noOpPolicy() {
    return new TaskPolicy() {
      @Override
      public ServiceProvider provider() {
        return ServiceProvider.DUOLINGO;
      }

      @Override
      public List<String> tasksOnAssigned(ServiceAssignedEvent e) {
        return List.of();
      }
    };
  }
}

