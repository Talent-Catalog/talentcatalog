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

package org.tctalent.server.casi.core.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Collections;
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
import org.tctalent.server.casi.domain.events.ServiceExpiredEvent;
import org.tctalent.server.casi.domain.model.AssignmentStatus;
import org.tctalent.server.casi.domain.model.ResourceStatus;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.casi.domain.persistence.ServiceAssignmentEntity;
import org.tctalent.server.casi.domain.persistence.ServiceAssignmentRepository;
import org.tctalent.server.casi.domain.persistence.ServiceResourceEntity;
import org.tctalent.server.casi.domain.persistence.ServiceResourceRepository;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.User;

@ExtendWith(MockitoExtension.class)
class ResourceExpirySchedulerTest {

  private static final Long RESOURCE_ID_1 = 1L;
  private static final Long RESOURCE_ID_2 = 2L;
  private static final Long ASSIGNMENT_ID = 10L;
  private static final Long CANDIDATE_ID = 123L;
  private static final Long ACTOR_ID = 456L;

  @Mock
  private ServiceResourceRepository resourceRepository;
  @Mock
  private ServiceAssignmentRepository assignmentRepository;
  @Mock
  private ApplicationEventPublisher eventPublisher;

  @InjectMocks
  private ResourceExpiryScheduler scheduler;

  private ServiceResourceEntity expirableResource1;
  private ServiceResourceEntity expirableResource2;
  private ServiceAssignmentEntity assignmentEntity;
  private OffsetDateTime now;

  @BeforeEach
  void setUp() {
    now = OffsetDateTime.now();

    expirableResource1 = new ServiceResourceEntity();
    expirableResource1.setId(RESOURCE_ID_1);
    expirableResource1.setProvider(ServiceProvider.DUOLINGO);
    expirableResource1.setServiceCode(ServiceCode.TEST_PROCTORED);
    expirableResource1.setResourceCode("COUPON123");
    expirableResource1.setStatus(ResourceStatus.AVAILABLE);
    expirableResource1.setExpiresAt(now.minusDays(1)); // Expired yesterday

    expirableResource2 = new ServiceResourceEntity();
    expirableResource2.setId(RESOURCE_ID_2);
    expirableResource2.setProvider(ServiceProvider.DUOLINGO);
    expirableResource2.setServiceCode(ServiceCode.TEST_NON_PROCTORED);
    expirableResource2.setResourceCode("COUPON456");
    expirableResource2.setStatus(ResourceStatus.SENT);
    expirableResource2.setExpiresAt(now.minusHours(12)); // Expired 12 hours ago

    Candidate candidate = new Candidate();
    candidate.setId(CANDIDATE_ID);

    User actor = new User();
    actor.setId(ACTOR_ID);

    assignmentEntity = new ServiceAssignmentEntity();
    assignmentEntity.setId(ASSIGNMENT_ID);
    assignmentEntity.setProvider(ServiceProvider.DUOLINGO);
    assignmentEntity.setServiceCode(ServiceCode.TEST_PROCTORED);
    assignmentEntity.setResource(expirableResource1);
    assignmentEntity.setCandidate(candidate);
    assignmentEntity.setActor(actor);
    assignmentEntity.setStatus(AssignmentStatus.ASSIGNED);
    assignmentEntity.setAssignedAt(now.minusDays(2));
  }

  @Test
  @DisplayName("mark resources as expired succeeds")
  void markResourcesAsExpiredSucceeds() {
    // Arrange
    when(resourceRepository.findExpirable(any(OffsetDateTime.class), anyList()))
        .thenReturn(List.of(expirableResource1, expirableResource2));
    when(assignmentRepository.findTopByProviderAndServiceAndResource(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED, RESOURCE_ID_1))
        .thenReturn(Optional.of(assignmentEntity));
    when(assignmentRepository.findTopByProviderAndServiceAndResource(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_NON_PROCTORED, RESOURCE_ID_2))
        .thenReturn(Optional.empty());

    // Act
    scheduler.markResourcesAsExpired();

    // Assert
    assertThat(expirableResource1.getStatus()).isEqualTo(ResourceStatus.EXPIRED);
    assertThat(expirableResource2.getStatus()).isEqualTo(ResourceStatus.EXPIRED);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<ServiceResourceEntity>> saveAllCaptor =
        ArgumentCaptor.forClass(List.class);
    verify(resourceRepository).saveAll(saveAllCaptor.capture());
    List<ServiceResourceEntity> savedResources = saveAllCaptor.getValue();
    assertThat(savedResources).hasSize(2);
    assertThat(savedResources).containsExactlyInAnyOrder(expirableResource1, expirableResource2);

    // Verify event was published for resource1 (which has an assignment)
    ArgumentCaptor<ServiceExpiredEvent> eventCaptor =
        ArgumentCaptor.forClass(ServiceExpiredEvent.class);
    verify(eventPublisher).publishEvent(eventCaptor.capture());
    ServiceExpiredEvent publishedEvent = eventCaptor.getValue();
    assertThat(publishedEvent.assignment()).isNotNull();
    assertThat(publishedEvent.assignment().getCandidateId()).isEqualTo(CANDIDATE_ID);
    assertThat(publishedEvent.assignment().getResource().getId()).isEqualTo(RESOURCE_ID_1);
  }

  @Test
  @DisplayName("skip already expired resources")
  void skipAlreadyExpiredResources() {
    // Arrange
    ServiceResourceEntity expiredResource = new ServiceResourceEntity();
    expiredResource.setId(3L);
    expiredResource.setProvider(ServiceProvider.DUOLINGO);
    expiredResource.setServiceCode(ServiceCode.TEST_PROCTORED);
    expiredResource.setResourceCode("COUPON789");
    expiredResource.setStatus(ResourceStatus.EXPIRED); // Already expired
    expiredResource.setExpiresAt(now.minusDays(5));

    // The query excludes EXPIRED status, so this should not be returned
    when(resourceRepository.findExpirable(any(OffsetDateTime.class), anyList()))
        .thenReturn(Collections.emptyList());

    // Act
    scheduler.markResourcesAsExpired();

    // Assert
    verify(resourceRepository, never()).saveAll(any());
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  @DisplayName("skip already redeemed resources")
  void skipAlreadyRedeemedResources() {
    // Arrange
    ServiceResourceEntity redeemedResource = new ServiceResourceEntity();
    redeemedResource.setId(4L);
    redeemedResource.setProvider(ServiceProvider.DUOLINGO);
    redeemedResource.setServiceCode(ServiceCode.TEST_PROCTORED);
    redeemedResource.setResourceCode("COUPON999");
    redeemedResource.setStatus(ResourceStatus.REDEEMED); // Already redeemed
    redeemedResource.setExpiresAt(now.minusDays(3));

    // The query excludes REDEEMED status, so this should not be returned
    when(resourceRepository.findExpirable(any(OffsetDateTime.class), anyList()))
        .thenReturn(Collections.emptyList());

    // Act
    scheduler.markResourcesAsExpired();

    // Assert
    verify(resourceRepository, never()).saveAll(any());
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  @DisplayName("skip disabled resources")
  void skipDisabledResources() {
    // Arrange
    ServiceResourceEntity disabledResource = new ServiceResourceEntity();
    disabledResource.setId(5L);
    disabledResource.setProvider(ServiceProvider.DUOLINGO);
    disabledResource.setServiceCode(ServiceCode.TEST_PROCTORED);
    disabledResource.setResourceCode("COUPON111");
    disabledResource.setStatus(ResourceStatus.DISABLED); // Disabled
    disabledResource.setExpiresAt(now.minusDays(2));

    // The query excludes DISABLED status, so this should not be returned
    when(resourceRepository.findExpirable(any(OffsetDateTime.class), anyList()))
        .thenReturn(Collections.emptyList());

    // Act
    scheduler.markResourcesAsExpired();

    // Assert
    verify(resourceRepository, never()).saveAll(any());
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  @DisplayName("event publication on expiration")
  void eventPublicationOnExpiration() {
    // Arrange
    when(resourceRepository.findExpirable(any(OffsetDateTime.class), anyList()))
        .thenReturn(List.of(expirableResource1));
    when(assignmentRepository.findTopByProviderAndServiceAndResource(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED, RESOURCE_ID_1))
        .thenReturn(Optional.of(assignmentEntity));

    // Act
    scheduler.markResourcesAsExpired();

    // Assert
    ArgumentCaptor<ServiceExpiredEvent> eventCaptor =
        ArgumentCaptor.forClass(ServiceExpiredEvent.class);
    verify(eventPublisher).publishEvent(eventCaptor.capture());
    ServiceExpiredEvent event = eventCaptor.getValue();
    assertThat(event).isNotNull();
    assertThat(event.assignment()).isNotNull();
    assertThat(event.assignment().getId()).isEqualTo(ASSIGNMENT_ID);
    assertThat(event.assignment().getCandidateId()).isEqualTo(CANDIDATE_ID);
    assertThat(event.assignment().getResource().getId()).isEqualTo(RESOURCE_ID_1);
  }

  @Test
  @DisplayName("no event published when assignment not found")
  void noEventPublishedWhenAssignmentNotFound() {
    // Arrange
    when(resourceRepository.findExpirable(any(OffsetDateTime.class), anyList()))
        .thenReturn(List.of(expirableResource1));
    when(assignmentRepository.findTopByProviderAndServiceAndResource(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED, RESOURCE_ID_1))
        .thenReturn(Optional.empty());

    // Act
    scheduler.markResourcesAsExpired();

    // Assert
    assertThat(expirableResource1.getStatus()).isEqualTo(ResourceStatus.EXPIRED);
    verify(resourceRepository).saveAll(anyList());
    // No event should be published when no assignment is found
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  @DisplayName("no expirable resources")
  void noExpirableResources() {
    // Arrange
    when(resourceRepository.findExpirable(any(OffsetDateTime.class), anyList()))
        .thenReturn(Collections.emptyList());

    // Act
    scheduler.markResourcesAsExpired();

    // Assert
    verify(resourceRepository, never()).saveAll(any());
    verify(assignmentRepository, never()).findTopByProviderAndServiceAndResource(any(), any(), any());
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  @DisplayName("resources with null expiresAt are skipped")
  void resourcesWithNullExpiresAtAreSkipped() {
    // Arrange
    ServiceResourceEntity resourceWithNullExpiresAt = new ServiceResourceEntity();
    resourceWithNullExpiresAt.setId(6L);
    resourceWithNullExpiresAt.setProvider(ServiceProvider.DUOLINGO);
    resourceWithNullExpiresAt.setServiceCode(ServiceCode.TEST_PROCTORED);
    resourceWithNullExpiresAt.setResourceCode("COUPON222");
    resourceWithNullExpiresAt.setStatus(ResourceStatus.AVAILABLE);
    resourceWithNullExpiresAt.setExpiresAt(null); // Null expiration date

    // The query filters out null expiresAt, so this should not be returned
    when(resourceRepository.findExpirable(any(OffsetDateTime.class), anyList()))
        .thenReturn(Collections.emptyList());

    // Act
    scheduler.markResourcesAsExpired();

    // Assert
    verify(resourceRepository, never()).saveAll(any());
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  @DisplayName("multiple resources with assignments publish multiple events")
  void multipleResourcesWithAssignmentsPublishMultipleEvents() {
    // Arrange
    ServiceAssignmentEntity assignmentEntity2 = new ServiceAssignmentEntity();
    assignmentEntity2.setId(20L);
    assignmentEntity2.setProvider(ServiceProvider.DUOLINGO);
    assignmentEntity2.setServiceCode(ServiceCode.TEST_NON_PROCTORED);
    assignmentEntity2.setResource(expirableResource2);
    Candidate candidate2 = new Candidate();
    candidate2.setId(789L);
    assignmentEntity2.setCandidate(candidate2);
    User actor2 = new User();
    actor2.setId(999L);
    assignmentEntity2.setActor(actor2);
    assignmentEntity2.setStatus(AssignmentStatus.ASSIGNED);
    assignmentEntity2.setAssignedAt(now.minusDays(1));

    when(resourceRepository.findExpirable(any(OffsetDateTime.class), anyList()))
        .thenReturn(List.of(expirableResource1, expirableResource2));
    when(assignmentRepository.findTopByProviderAndServiceAndResource(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED, RESOURCE_ID_1))
        .thenReturn(Optional.of(assignmentEntity));
    when(assignmentRepository.findTopByProviderAndServiceAndResource(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_NON_PROCTORED, RESOURCE_ID_2))
        .thenReturn(Optional.of(assignmentEntity2));

    // Act
    scheduler.markResourcesAsExpired();

    // Assert
    assertThat(expirableResource1.getStatus()).isEqualTo(ResourceStatus.EXPIRED);
    assertThat(expirableResource2.getStatus()).isEqualTo(ResourceStatus.EXPIRED);

    ArgumentCaptor<ServiceExpiredEvent> eventCaptor =
        ArgumentCaptor.forClass(ServiceExpiredEvent.class);
    verify(eventPublisher, times(2)).publishEvent(eventCaptor.capture());
    List<ServiceExpiredEvent> publishedEvents = eventCaptor.getAllValues();
    assertThat(publishedEvents).hasSize(2);
    assertThat(publishedEvents.get(0).assignment().getResource().getId()).isEqualTo(RESOURCE_ID_1);
    assertThat(publishedEvents.get(1).assignment().getResource().getId()).isEqualTo(RESOURCE_ID_2);
  }
}

