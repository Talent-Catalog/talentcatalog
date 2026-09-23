/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.casi.application.providers.pifi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.casi.application.support.RelevantCountryResolver;
import org.tctalent.server.casi.core.allocators.ResourceAllocator;
import org.tctalent.server.casi.core.services.AssignmentEngine;
import org.tctalent.server.casi.domain.model.AssignmentStatus;
import org.tctalent.server.casi.domain.model.ResourceStatus;
import org.tctalent.server.casi.domain.model.ResourceType;
import org.tctalent.server.casi.domain.model.ServiceAssignment;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.casi.domain.persistence.ServiceAssignmentEntity;
import org.tctalent.server.casi.domain.persistence.ServiceAssignmentRepository;
import org.tctalent.server.casi.domain.persistence.ServiceResourceEntity;
import org.tctalent.server.casi.domain.persistence.ServiceResourceRepository;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.User;
import org.tctalent.server.service.db.SavedListService;

@ExtendWith(MockitoExtension.class)
class PifiServiceTest {

  @Mock private ServiceAssignmentRepository assignmentRepository;
  @Mock private ServiceResourceRepository resourceRepository;
  @Mock private AssignmentEngine assignmentEngine;
  @Mock private SavedListService savedListService;
  @Mock private RelevantCountryResolver countryResolver;
  @Mock private ResourceAllocator allocator;

  private PifiService service;

  @BeforeEach
  void setUp() {
    service = new PifiService(
        assignmentRepository,
        resourceRepository,
        assignmentEngine,
        savedListService,
        countryResolver,
        allocator
    );
  }

  @Test
  @DisplayName("provider key is PIFI::HELP_SITE_LINK")
  void providerKeyIsPifiHelpSiteLink() {
    assertThat(service.providerKey()).isEqualTo("PIFI::HELP_SITE_LINK");
  }

  @Test
  @DisplayName("getCurrentAssignment returns first matching country")
  void getCurrentAssignmentReturnsFirstMatchingCountry() {
    when(countryResolver.resolveCountryIsoCodes(100L)).thenReturn(List.of("AU", "PK"));
    when(resourceRepository.countAvailableByProviderServiceAndCountry(
        ServiceProvider.PIFI, ServiceCode.HELP_SITE_LINK, "AU")).thenReturn(1L);

    ServiceAssignmentEntity pk = assignmentWithResource(21L, "PK");
    ServiceAssignmentEntity au = assignmentWithResource(22L, "AU");
    when(assignmentRepository.findByCandidateAndProviderAndService(
        100L, ServiceProvider.PIFI, ServiceCode.HELP_SITE_LINK)).thenReturn(List.of(pk, au));

    ServiceAssignment current = service.getCurrentAssignment(100L);
    assertThat(current).isNotNull();
    assertThat(current.getResource().getCountryIsoCode()).isEqualTo("AU");
  }

  @Test
  @DisplayName("getCurrentAssignment matches assignment country ISO case-insensitively")
  void getCurrentAssignmentMatchesCountryIsoCaseInsensitively() {
    when(countryResolver.resolveCountryIsoCodes(100L)).thenReturn(List.of("AU"));
    when(resourceRepository.countAvailableByProviderServiceAndCountry(
        ServiceProvider.PIFI, ServiceCode.HELP_SITE_LINK, "AU")).thenReturn(1L);
    when(assignmentRepository.findByCandidateAndProviderAndService(
        100L, ServiceProvider.PIFI, ServiceCode.HELP_SITE_LINK))
        .thenReturn(List.of(assignmentWithResource(22L, "au")));

    ServiceAssignment current = service.getCurrentAssignment(100L);
    assertThat(current).isNotNull();
    assertThat(current.getResource().getCountryIsoCode()).isEqualTo("au");
  }

  @Test
  @DisplayName("assignToCandidate throws when same-country ASSIGNED exists")
  void assignToCandidateThrowsWhenSameCountryAlreadyAssigned() {
    when(countryResolver.resolveCountryIsoCodes(100L)).thenReturn(List.of("AU"));
    when(resourceRepository.countAvailableByProviderServiceAndCountry(
        ServiceProvider.PIFI, ServiceCode.HELP_SITE_LINK, "AU")).thenReturn(1L);
    when(assignmentRepository.findByCandidateAndProviderAndService(
        100L, ServiceProvider.PIFI, ServiceCode.HELP_SITE_LINK))
        .thenReturn(List.of(assignmentWithResource(31L, "AU")));

    assertThatThrownBy(() -> service.assignToCandidate(100L, new User()))
        .isInstanceOf(EntityExistsException.class);
    verifyNoInteractions(assignmentEngine);
  }

  @Test
  @DisplayName("assignToCandidate throws when same-country ASSIGNED exists with different ISO case")
  void assignToCandidateThrowsWhenSameCountryAlreadyAssignedDifferentCase() {
    when(countryResolver.resolveCountryIsoCodes(100L)).thenReturn(List.of("AU"));
    when(resourceRepository.countAvailableByProviderServiceAndCountry(
        ServiceProvider.PIFI, ServiceCode.HELP_SITE_LINK, "AU")).thenReturn(1L);
    when(assignmentRepository.findByCandidateAndProviderAndService(
        100L, ServiceProvider.PIFI, ServiceCode.HELP_SITE_LINK))
        .thenReturn(List.of(assignmentWithResource(31L, "au")));

    assertThatThrownBy(() -> service.assignToCandidate(100L, new User()))
        .isInstanceOf(EntityExistsException.class);
    verifyNoInteractions(assignmentEngine);
  }

  @Test
  @DisplayName("assignToCandidate allows reassignment after disabled link is replaced")
  void assignToCandidateAllowsReassignmentAfterDisabledLinkReplaced() {
    when(countryResolver.resolveCountryIsoCodes(100L)).thenReturn(List.of("AU"));
    when(resourceRepository.countAvailableByProviderServiceAndCountry(
        ServiceProvider.PIFI, ServiceCode.HELP_SITE_LINK, "AU")).thenReturn(1L);

    ServiceAssignmentEntity disabledAu = assignmentWithResource(31L, "AU");
    disabledAu.getResource().setStatus(ResourceStatus.DISABLED);
    when(assignmentRepository.findByCandidateAndProviderAndService(
        100L, ServiceProvider.PIFI, ServiceCode.HELP_SITE_LINK))
        .thenReturn(List.of(disabledAu));

    assertThat(service.getCurrentAssignment(100L)).isNull();

    ServiceAssignment replacement = ServiceAssignment.builder().id(99L).build();
    when(assignmentEngine.assign(eq(allocator), eq(100L), any(User.class)))
        .thenReturn(replacement);

    ServiceAssignment result = service.assignToCandidate(100L, new User());
    assertThat(result.getId()).isEqualTo(99L);
    verify(assignmentEngine).assign(eq(allocator), eq(100L), any(User.class));
  }

  @Test
  @DisplayName("assignToCandidate assigns when matching country exists")
  void assignToCandidateAssignsWhenMatchingCountryExists() {
    when(countryResolver.resolveCountryIsoCodes(100L)).thenReturn(List.of("AU"));
    when(resourceRepository.countAvailableByProviderServiceAndCountry(
        ServiceProvider.PIFI, ServiceCode.HELP_SITE_LINK, "AU")).thenReturn(1L);
    when(assignmentRepository.findByCandidateAndProviderAndService(
        100L, ServiceProvider.PIFI, ServiceCode.HELP_SITE_LINK)).thenReturn(List.of());

    ServiceAssignment assigned = ServiceAssignment.builder().id(42L).build();
    when(assignmentEngine.assign(eq(allocator), eq(100L), any(User.class))).thenReturn(assigned);

    ServiceAssignment result = service.assignToCandidate(100L, new User());
    assertThat(result.getId()).isEqualTo(42L);
  }

  @Test
  @DisplayName("assignToCandidate throws when no country configured")
  void assignToCandidateThrowsWhenNoCountryConfigured() {
    when(countryResolver.resolveCountryIsoCodes(100L)).thenReturn(List.of("AU"));
    when(resourceRepository.countAvailableByProviderServiceAndCountry(
        ServiceProvider.PIFI, ServiceCode.HELP_SITE_LINK, "AU")).thenReturn(0L);

    assertThatThrownBy(() -> service.assignToCandidate(100L, new User()))
        .isInstanceOf(NoSuchObjectException.class);
  }

  @Test
  @DisplayName("disableSharedResource disables related ASSIGNED assignments")
  void disableSharedResourceDisablesRelatedAssignedAssignments() {
    ServiceResourceEntity sharedResource = new ServiceResourceEntity();
    sharedResource.setId(77L);
    sharedResource.setProvider(ServiceProvider.PIFI);
    sharedResource.setServiceCode(ServiceCode.HELP_SITE_LINK);
    sharedResource.setResourceType(ResourceType.SHARED);
    sharedResource.setStatus(ResourceStatus.AVAILABLE);

    ServiceAssignmentEntity assigned = assignmentWithResource(51L, "AU");
    assigned.setStatus(AssignmentStatus.ASSIGNED);
    ServiceAssignmentEntity redeemed = assignmentWithResource(52L, "AU");
    redeemed.setStatus(AssignmentStatus.REDEEMED);

    when(resourceRepository.findByIdAndProviderAndServiceCodeAndResourceType(
        77L, ServiceProvider.PIFI, ServiceCode.HELP_SITE_LINK, ResourceType.SHARED))
        .thenReturn(Optional.of(sharedResource));
    when(assignmentRepository.findByProviderAndServiceAndResource(
        ServiceProvider.PIFI, ServiceCode.HELP_SITE_LINK, 77L))
        .thenReturn(List.of(assigned, redeemed));

    service.disableSharedResource(77L);

    assertThat(sharedResource.getStatus()).isEqualTo(ResourceStatus.DISABLED);
    assertThat(assigned.getStatus()).isEqualTo(AssignmentStatus.DISABLED);
    assertThat(redeemed.getStatus()).isEqualTo(AssignmentStatus.REDEEMED);
    verify(resourceRepository).save(sharedResource);
    verify(assignmentRepository).save(assigned);
    verify(assignmentRepository, never()).save(redeemed);
  }

  private ServiceAssignmentEntity assignmentWithResource(Long assignmentId, String countryIsoCode) {
    ServiceResourceEntity resource = new ServiceResourceEntity();
    resource.setId(assignmentId);
    resource.setProvider(ServiceProvider.PIFI);
    resource.setServiceCode(ServiceCode.HELP_SITE_LINK);
    resource.setResourceCode("https://pifiproperty.com/" + countryIsoCode.toLowerCase());
    resource.setCountryIsoCode(countryIsoCode);
    resource.setResourceType(ResourceType.SHARED);
    resource.setStatus(ResourceStatus.AVAILABLE);

    Candidate candidate = new Candidate();
    candidate.setId(100L);

    ServiceAssignmentEntity assignment = new ServiceAssignmentEntity();
    assignment.setId(assignmentId);
    assignment.setProvider(ServiceProvider.PIFI);
    assignment.setServiceCode(ServiceCode.HELP_SITE_LINK);
    assignment.setResource(resource);
    assignment.setCandidate(candidate);
    assignment.setStatus(AssignmentStatus.ASSIGNED);
    assignment.setAssignedAt(OffsetDateTime.now());
    return assignment;
  }
}
