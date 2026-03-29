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

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.casi.domain.model.ResourceStatus;
import org.tctalent.server.casi.domain.model.ServiceAssignment;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.casi.domain.model.ServiceResource;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.User;

class CandidateServiceRegistryTest {

  private CandidateAssistanceService service1;
  private CandidateAssistanceService service2;
  private CandidateAssistanceService service3;
  private CandidateServiceRegistry registry;

  @BeforeEach
  void setUp() {
    service1 = createMockService(ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED);
    service2 = createMockService(ServiceProvider.DUOLINGO, ServiceCode.TEST_NON_PROCTORED);
    service3 = createMockService(ServiceProvider.REFERENCE, ServiceCode.VOUCHER);
  }

  private CandidateAssistanceService createMockService(
      ServiceProvider provider, ServiceCode serviceCode) {
    return new CandidateAssistanceService() {
      @Override
      public String providerKey() {
        return provider.name() + "::" + serviceCode.name();
      }

      @Override
      public void importInventory(MultipartFile file) {
      }

      @Override
      public ServiceAssignment assignToCandidate(
          Long candidateId, User actor) {
        return null;
      }

      @Override
      public List<ServiceAssignment> assignToList(
          Long listId, User actor) {
        return null;
      }

      @Override
      public ServiceAssignment reassignForCandidate(
          String candidateNumber, User actor) {
        return null;
      }

      @Override
      public List<ServiceAssignment>
          getAssignmentsForCandidate(Long candidateId) {
        return null;
      }

      @Override
      public ServiceAssignment getCurrentAssignment(Long candidateId) {
        return null;
      }

      @Override
      public List<ServiceResource>
          getAvailableResources() {
        return null;
      }

      @Override
      public List<ServiceResource>
          getResourcesForCandidate(Long candidateId) {
        return null;
      }

      @Override
      public ServiceResource getResourceForResourceCode(
          String resourceCode) {
        return null;
      }

      @Override
      public Candidate getCandidateForResourceCode(
          String resourceCode) {
        return null;
      }

      @Override
      public long countAvailableForProvider() {
        return 0;
      }

      @Override
      public long countAvailableForProviderAndService() {
        return 0;
      }

      @Override
      public void updateResourceStatus(
          String resourceCode,
          ResourceStatus status) {
      }
    };
  }

  @Test
  @DisplayName("registry successfully registers and retrieves services")
  void registryRegistersAndRetrievesServices() {
    // Arrange & Act
    registry = new CandidateServiceRegistry(List.of(service1, service2));

    // Assert
    CandidateAssistanceService retrieved1 = registry.forProviderAndServiceCode(
        "DUOLINGO", "TEST_PROCTORED");
    CandidateAssistanceService retrieved2 = registry.forProviderAndServiceCode(
        "DUOLINGO", "TEST_NON_PROCTORED");

    assertThat(retrieved1).isNotNull();
    assertThat(retrieved2).isNotNull();
    assertThat(retrieved1.providerKey()).isEqualTo("DUOLINGO::TEST_PROCTORED");
    assertThat(retrieved2.providerKey()).isEqualTo("DUOLINGO::TEST_NON_PROCTORED");
  }

  @Test
  @DisplayName("registry lookup is case insensitive")
  void registryLookupIsCaseInsensitive() {
    // Arrange
    registry = new CandidateServiceRegistry(List.of(service1));

    // Act
    CandidateAssistanceService retrieved1 = registry.forProviderAndServiceCode(
        "duolingo", "test_proctored");
    CandidateAssistanceService retrieved2 = registry.forProviderAndServiceCode(
        "Duolingo", "Test_Proctored");
    CandidateAssistanceService retrieved3 = registry.forProviderAndServiceCode(
        "  DUOLINGO  ", "  TEST_PROCTORED  ");

    // Assert
    assertThat(retrieved1).isNotNull();
    assertThat(retrieved2).isNotNull();
    assertThat(retrieved3).isNotNull();
    assertThat(retrieved1.providerKey()).isEqualTo("DUOLINGO::TEST_PROCTORED");
  }

  @Test
  @DisplayName("registry throws exception for duplicate services")
  void registryThrowsExceptionForDuplicateServices() {
    // Arrange
    CandidateAssistanceService duplicate = createMockService(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED);

    // Act & Assert
    assertThatThrownBy(() -> new CandidateServiceRegistry(List.of(service1, duplicate)))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Duplicate CandidateAssistanceService");
  }

  @Test
  @DisplayName("registry throws exception when service not found")
  void registryThrowsExceptionWhenServiceNotFound() {
    // Arrange
    registry = new CandidateServiceRegistry(List.of(service1));

    // Act & Assert
    assertThatThrownBy(() -> registry.forProviderAndServiceCode("UNKNOWN", "UNKNOWN"))
        .isInstanceOf(NoSuchObjectException.class)
        .hasMessageContaining("Unknown candidate service");
  }

  @Test
  @DisplayName("registry supports reference provider service lookup")
  void registrySupportsReferenceProviderServiceLookup() {
    registry = new CandidateServiceRegistry(List.of(service1, service3));

    CandidateAssistanceService retrieved = registry.forProviderAndServiceCode(
        "REFERENCE", "VOUCHER");

    assertThat(retrieved).isNotNull();
    assertThat(retrieved.providerKey()).isEqualTo("REFERENCE::VOUCHER");
  }

  @Test
  @DisplayName("registry handles empty service list")
  void registryHandlesEmptyServiceList() {
    // Arrange
    registry = new CandidateServiceRegistry(List.of());

    // Act & Assert
    assertThatThrownBy(() -> registry.forProviderAndServiceCode("DUOLINGO", "TEST_PROCTORED"))
        .isInstanceOf(NoSuchObjectException.class)
        .hasMessageContaining("Unknown candidate service for provider: DUOLINGO, serviceCode: TEST_PROCTORED");
  }

  @Test
  @DisplayName("registry normalizes whitespace in provider and service codes")
  void registryNormalizesWhitespace() {
    // Arrange
    registry = new CandidateServiceRegistry(List.of(service1));

    // Act
    CandidateAssistanceService retrieved = registry.forProviderAndServiceCode(
        "  DUOLINGO  ", "  TEST_PROCTORED  ");

    // Assert
    assertThat(retrieved).isNotNull();
    assertThat(retrieved.providerKey()).isEqualTo("DUOLINGO::TEST_PROCTORED");
  }
}

