/*
 * Copyright (c) 2026 Talent Catalog.
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

package org.tctalent.server.casi.application.providers.unhcr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.casi.core.allocators.ResourceAllocator;
import org.tctalent.server.casi.core.services.AssignmentEngine;
import org.tctalent.server.casi.domain.model.AssignmentStatus;
import org.tctalent.server.casi.domain.model.ResourceStatus;
import org.tctalent.server.casi.domain.model.ServiceAssignment;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.casi.domain.persistence.ServiceAssignmentEntity;
import org.tctalent.server.casi.domain.persistence.ServiceAssignmentRepository;
import org.tctalent.server.casi.domain.persistence.ServiceResourceEntity;
import org.tctalent.server.casi.domain.persistence.ServiceResourceRepository;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.service.db.SavedListService;

@ExtendWith(MockitoExtension.class)
class UnhcrServiceTest {

  @Mock
  private ServiceAssignmentRepository assignmentRepository;
  @Mock
  private ServiceResourceRepository resourceRepository;
  @Mock
  private AssignmentEngine assignmentEngine;
  @Mock
  private SavedListService savedListService;
  @Mock
  private ResourceAllocator allocator;

  private UnhcrService service;

  @BeforeEach
  void setUp() {
    service = new UnhcrService(
        assignmentRepository,
        resourceRepository,
        assignmentEngine,
        savedListService,
        allocator
    );
  }

  @Test
  @DisplayName("provider key is UNHCR::HELP_SITE_LINK")
  void providerKeyIsUnhcrHelpSiteLink() {
    assertThat(service.providerKey()).isEqualTo("UNHCR::HELP_SITE_LINK");
  }

  @Test
  @DisplayName("getCurrentAssignment returns first AVAILABLE assignment")
  void getCurrentAssignmentReturnsFirstAvailableAssignment() {
    ServiceAssignmentEntity reserved = assignmentWithResourceStatus(11L, ResourceStatus.RESERVED, "UNHCR-RES");
    ServiceAssignmentEntity available = assignmentWithResourceStatus(12L, ResourceStatus.AVAILABLE, "UNHCR-PK");

    when(assignmentRepository.findByCandidateAndProviderAndService(
        100L, ServiceProvider.UNHCR, ServiceCode.HELP_SITE_LINK))
        .thenReturn(List.of(reserved, available));

    ServiceAssignment current = service.getCurrentAssignment(100L);

    assertThat(current).isNotNull();
    assertThat(current.getId()).isEqualTo(12L);
    assertThat(current.getResource().getStatus()).isEqualTo(ResourceStatus.AVAILABLE);
    assertThat(current.getResource().getResourceCode()).isEqualTo("UNHCR-PK");
  }

  @Test
  @DisplayName("getCurrentAssignment returns null when no AVAILABLE assignment exists")
  void getCurrentAssignmentReturnsNullWhenNoAvailableExists() {
    ServiceAssignmentEntity redeemed = assignmentWithResourceStatus(21L, ResourceStatus.REDEEMED, "UNHCR-OLD");
    ServiceAssignmentEntity reserved = assignmentWithResourceStatus(22L, ResourceStatus.RESERVED, "UNHCR-RES");

    when(assignmentRepository.findByCandidateAndProviderAndService(
        100L, ServiceProvider.UNHCR, ServiceCode.HELP_SITE_LINK))
        .thenReturn(List.of(redeemed, reserved));

    ServiceAssignment current = service.getCurrentAssignment(100L);

    assertThat(current).isNull();
  }

  private ServiceAssignmentEntity assignmentWithResourceStatus(
      Long assignmentId, ResourceStatus resourceStatus, String resourceCode) {
    ServiceResourceEntity resource = new ServiceResourceEntity();
    resource.setId(assignmentId);
    resource.setProvider(ServiceProvider.UNHCR);
    resource.setServiceCode(ServiceCode.HELP_SITE_LINK);
    resource.setResourceCode(resourceCode);
    resource.setStatus(resourceStatus);

    Candidate candidate = new Candidate();
    candidate.setId(100L);

    ServiceAssignmentEntity assignment = new ServiceAssignmentEntity();
    assignment.setId(assignmentId);
    assignment.setProvider(ServiceProvider.UNHCR);
    assignment.setServiceCode(ServiceCode.HELP_SITE_LINK);
    assignment.setResource(resource);
    assignment.setCandidate(candidate);
    assignment.setStatus(AssignmentStatus.ASSIGNED);
    assignment.setAssignedAt(OffsetDateTime.now());
    return assignment;
  }
}
