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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
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
import org.tctalent.server.casi.domain.model.ResourceType;
import org.tctalent.server.casi.domain.persistence.ServiceAssignmentEntity;
import org.tctalent.server.casi.domain.persistence.ServiceAssignmentRepository;
import org.tctalent.server.casi.domain.persistence.ServiceResourceEntity;
import org.tctalent.server.casi.domain.persistence.ServiceResourceRepository;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.User;
import org.tctalent.server.service.db.CandidateService;
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
  private CandidateService candidateService;
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
        candidateService,
        allocator
    );
  }

  @Test
  @DisplayName("provider key is UNHCR::HELP_SITE_LINK")
  void providerKeyIsUnhcrHelpSiteLink() {
    assertThat(service.providerKey()).isEqualTo("UNHCR::HELP_SITE_LINK");
  }

  @Test
  @DisplayName("getCurrentAssignment returns AVAILABLE assignment for candidate current country")
  void getCurrentAssignmentReturnsAvailableForCurrentCountry() {
    when(candidateService.getCandidate(100L)).thenReturn(candidateWithIso("PK"));

    ServiceAssignmentEntity availableJo =
        assignmentWithResource(11L, ResourceStatus.AVAILABLE, AssignmentStatus.ASSIGNED, "UNHCR-JO", "JO");
    ServiceAssignmentEntity availablePk =
        assignmentWithResource(12L, ResourceStatus.AVAILABLE, AssignmentStatus.ASSIGNED, "UNHCR-PK", "PK");

    when(assignmentRepository.findByCandidateAndProviderAndService(
        100L, ServiceProvider.UNHCR, ServiceCode.HELP_SITE_LINK))
        .thenReturn(List.of(availableJo, availablePk));

    ServiceAssignment current = service.getCurrentAssignment(100L);

    assertThat(current).isNotNull();
    assertThat(current.getId()).isEqualTo(12L);
    assertThat(current.getResource().getStatus()).isEqualTo(ResourceStatus.AVAILABLE);
    assertThat(current.getResource().getResourceCode()).isEqualTo("UNHCR-PK");
    assertThat(current.getResource().getCountryIsoCode()).isEqualTo("PK");
  }

  @Test
  @DisplayName("getCurrentAssignment returns null when available assignment is for different country")
  void getCurrentAssignmentReturnsNullWhenDifferentCountry() {
    when(candidateService.getCandidate(100L)).thenReturn(candidateWithIso("JO"));

    ServiceAssignmentEntity availablePk =
        assignmentWithResource(21L, ResourceStatus.AVAILABLE, AssignmentStatus.ASSIGNED, "UNHCR-PK", "PK");

    when(assignmentRepository.findByCandidateAndProviderAndService(
        100L, ServiceProvider.UNHCR, ServiceCode.HELP_SITE_LINK))
        .thenReturn(List.of(availablePk));

    ServiceAssignment current = service.getCurrentAssignment(100L);

    assertThat(current).isNull();
  }

  @Test
  @DisplayName("assignToCandidate throws when same-country ASSIGNED exists")
  void assignToCandidateThrowsWhenSameCountryAssignedExists() {
    when(candidateService.getCandidate(100L)).thenReturn(candidateWithIso("JO"));

    ServiceAssignmentEntity existingJo =
        assignmentWithResource(31L, ResourceStatus.AVAILABLE, AssignmentStatus.ASSIGNED, "UNHCR-JO", "JO");
    when(assignmentRepository.findByCandidateAndProviderAndService(
        100L, ServiceProvider.UNHCR, ServiceCode.HELP_SITE_LINK))
        .thenReturn(List.of(existingJo));

    assertThatThrownBy(() -> service.assignToCandidate(100L, new User()))
        .isInstanceOf(EntityExistsException.class)
        .hasMessageContaining("ASSIGNED HELP_SITE_LINK resource");

    verifyNoInteractions(assignmentEngine);
  }

  @Test
  @DisplayName("assignToCandidate allows assignment when only different-country ASSIGNED exists")
  void assignToCandidateAllowsWhenCountryChanged() {
    when(candidateService.getCandidate(100L)).thenReturn(candidateWithIso("JO"));

    ServiceAssignmentEntity existingPk =
        assignmentWithResource(41L, ResourceStatus.AVAILABLE, AssignmentStatus.ASSIGNED, "UNHCR-PK", "PK");
    when(assignmentRepository.findByCandidateAndProviderAndService(
        100L, ServiceProvider.UNHCR, ServiceCode.HELP_SITE_LINK))
        .thenReturn(List.of(existingPk));

    ServiceAssignment assigned = ServiceAssignment.builder()
        .id(42L)
        .provider(ServiceProvider.UNHCR)
        .serviceCode(ServiceCode.HELP_SITE_LINK)
        .build();
    when(assignmentEngine.assign(eq(allocator), eq(100L), any(User.class))).thenReturn(assigned);

    ServiceAssignment result = service.assignToCandidate(100L, new User());
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(42L);
  }

  private ServiceAssignmentEntity assignmentWithResource(
      Long assignmentId,
      ResourceStatus resourceStatus,
      AssignmentStatus assignmentStatus,
      String resourceCode,
      String countryIsoCode) {
    ServiceResourceEntity resource = new ServiceResourceEntity();
    resource.setId(assignmentId);
    resource.setProvider(ServiceProvider.UNHCR);
    resource.setServiceCode(ServiceCode.HELP_SITE_LINK);
    resource.setResourceCode(resourceCode);
    resource.setCountryIsoCode(countryIsoCode);
    resource.setResourceType(ResourceType.SHARED);
    resource.setStatus(resourceStatus);

    Candidate candidate = new Candidate();
    candidate.setId(100L);

    ServiceAssignmentEntity assignment = new ServiceAssignmentEntity();
    assignment.setId(assignmentId);
    assignment.setProvider(ServiceProvider.UNHCR);
    assignment.setServiceCode(ServiceCode.HELP_SITE_LINK);
    assignment.setResource(resource);
    assignment.setCandidate(candidate);
    assignment.setStatus(assignmentStatus);
    assignment.setAssignedAt(OffsetDateTime.now());
    return assignment;
  }

  private Candidate candidateWithIso(String isoCode) {
    Country country = new Country();
    country.setIsoCode(isoCode);
    Candidate candidate = new Candidate();
    candidate.setCountry(country);
    return candidate;
  }
}
