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

package org.tctalent.server.casi.core.allocators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.casi.domain.model.ResourceStatus;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.casi.domain.model.ServiceResource;
import org.tctalent.server.casi.domain.persistence.ServiceResourceEntity;
import org.tctalent.server.casi.domain.persistence.ServiceResourceRepository;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;

@ExtendWith(MockitoExtension.class)
class InventoryAllocatorTest {

  private static final String RESOURCE_CODE = "COUPON123";

  @Mock
  private ServiceResourceRepository resourceRepository;

  @InjectMocks
  private InventoryAllocator allocator;

  private ServiceResourceEntity availableResource;
  private Candidate candidate;

  @BeforeEach
  void setUp() {
    allocator = new InventoryAllocator(
        resourceRepository,
        ServiceProvider.DUOLINGO,
        ServiceCode.TEST_PROCTORED
    );

    availableResource = new ServiceResourceEntity();
    availableResource.setId(1L);
    availableResource.setProvider(ServiceProvider.DUOLINGO);
    availableResource.setServiceCode(ServiceCode.TEST_PROCTORED);
    availableResource.setResourceCode(RESOURCE_CODE);
    availableResource.setStatus(ResourceStatus.AVAILABLE);

    candidate = new Candidate();
    candidate.setId(123L);
  }

  @Test
  @DisplayName("allocate succeeds and reserves resource")
  void allocateSucceeds() {
    // Arrange
    when(resourceRepository.lockNextAvailable(
        ServiceProvider.DUOLINGO.name(), ServiceCode.TEST_PROCTORED.name()))
        .thenReturn(availableResource);
    when(resourceRepository.save(availableResource))
        .thenReturn(availableResource);

    // Act
    ServiceResource result = allocator.allocateFor(candidate);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getResourceCode()).isEqualTo(RESOURCE_CODE);
    assertThat(result.getProvider()).isEqualTo(ServiceProvider.DUOLINGO);
    assertThat(result.getServiceCode()).isEqualTo(ServiceCode.TEST_PROCTORED);

    // Verify resource status was updated to RESERVED
    verify(resourceRepository).save(availableResource);
    assertThat(availableResource.getStatus()).isEqualTo(ResourceStatus.RESERVED);
  }

  @Test
  @DisplayName("allocate fails when no resources available")
  void allocateFailsWhenNoResourcesAvailable() {
    // Arrange
    when(resourceRepository.lockNextAvailable(
        ServiceProvider.DUOLINGO.name(), ServiceCode.TEST_PROCTORED.name()))
        .thenReturn(null);

    // Act & Assert
    assertThatThrownBy(() -> allocator.allocateFor(candidate))
        .isInstanceOf(NoSuchObjectException.class)
        .hasMessageContaining("There are no available")
        .hasMessageContaining("coupons to assign");

    verify(resourceRepository, org.mockito.Mockito.never()).save(any());
  }

  @Test
  @DisplayName("getProvider returns correct provider")
  void getProviderReturnsCorrectProvider() {
    // Act
    ServiceProvider provider = allocator.getProvider();

    // Assert
    assertThat(provider).isEqualTo(ServiceProvider.DUOLINGO);
  }

  @Test
  @DisplayName("getServiceCode returns correct service code")
  void getServiceCodeReturnsCorrectServiceCode() {
    // Act
    ServiceCode serviceCode = allocator.getServiceCode();

    // Assert
    assertThat(serviceCode).isEqualTo(ServiceCode.TEST_PROCTORED);
  }

  @Test
  @DisplayName("allocate uses pessimistic locking")
  void allocateUsesPessimisticLocking() {
    // Arrange
    when(resourceRepository.lockNextAvailable(
        ServiceProvider.DUOLINGO.name(), ServiceCode.TEST_PROCTORED.name()))
        .thenReturn(availableResource);
    when(resourceRepository.save(availableResource))
        .thenReturn(availableResource);

    // Act
    allocator.allocateFor(candidate);

    // Assert
    // Verify that lockNextAvailable was called, which uses FOR UPDATE SKIP LOCKED
    verify(resourceRepository).lockNextAvailable(
        ServiceProvider.DUOLINGO.name(), ServiceCode.TEST_PROCTORED.name());
  }
}

