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

package org.tctalent.server.casi.core.allocators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.casi.domain.model.ResourceStatus;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.casi.domain.model.ServiceResource;
import org.tctalent.server.casi.domain.model.ResourceType;
import org.tctalent.server.casi.domain.persistence.ServiceResourceEntity;
import org.tctalent.server.casi.domain.persistence.ServiceResourceRepository;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.Country;

@ExtendWith(MockitoExtension.class)
class SharedCountryResourceAllocatorTest {

  @Mock
  private ServiceResourceRepository resourceRepository;

  private SharedCountryResourceAllocator allocator;
  private Candidate candidate;

  @BeforeEach
  void setUp() {
    allocator = new SharedCountryResourceAllocator(
        resourceRepository,
        ServiceProvider.UNHCR,
        ServiceCode.HELP_SITE_LINK);

    Country country = new Country();
    country.setIsoCode("pk");
    candidate = new Candidate();
    candidate.setCountry(country);
  }

  @Test
  @DisplayName("allocate returns first available resource for candidate country")
  void allocateReturnsFirstAvailableResource() {
    ServiceResourceEntity first = new ServiceResourceEntity();
    first.setId(1L);
    first.setProvider(ServiceProvider.UNHCR);
    first.setServiceCode(ServiceCode.HELP_SITE_LINK);
    first.setResourceCode("https://help.unhcr.org/pakistan/");
    first.setResourceType(ResourceType.SHARED);
    first.setStatus(ResourceStatus.AVAILABLE);

    ServiceResourceEntity second = new ServiceResourceEntity();
    second.setId(2L);
    second.setProvider(ServiceProvider.UNHCR);
    second.setServiceCode(ServiceCode.HELP_SITE_LINK);
    second.setResourceCode("https://help.unhcr.org/pakistan/contact/");
    second.setResourceType(ResourceType.SHARED);
    second.setStatus(ResourceStatus.AVAILABLE);

    when(resourceRepository.findAvailableByProviderServiceAndCountry(
        ServiceProvider.UNHCR, ServiceCode.HELP_SITE_LINK, "PK"))
        .thenReturn(List.of(first, second));

    ServiceResource result = allocator.allocateFor(candidate);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getResourceCode()).isEqualTo("https://help.unhcr.org/pakistan/");
    verify(resourceRepository).findAvailableByProviderServiceAndCountry(
        ServiceProvider.UNHCR, ServiceCode.HELP_SITE_LINK, "PK");
  }

  @Test
  @DisplayName("allocate uppercases ISO code before lookup")
  void allocateUppercasesIsoCodeBeforeLookup() {
    ServiceResourceEntity entity = new ServiceResourceEntity();
    entity.setId(99L);
    entity.setProvider(ServiceProvider.UNHCR);
    entity.setServiceCode(ServiceCode.HELP_SITE_LINK);
    entity.setResourceCode("https://help.unhcr.org/pakistan/");
    entity.setResourceType(ResourceType.SHARED);
    entity.setStatus(ResourceStatus.AVAILABLE);

    when(resourceRepository.findAvailableByProviderServiceAndCountry(
        ServiceProvider.UNHCR, ServiceCode.HELP_SITE_LINK, "PK"))
        .thenReturn(List.of(entity));

    allocator.allocateFor(candidate);

    verify(resourceRepository).findAvailableByProviderServiceAndCountry(
        ServiceProvider.UNHCR, ServiceCode.HELP_SITE_LINK, "PK");
  }

  @Test
  @DisplayName("allocate throws when candidate country is missing")
  void allocateThrowsWhenCountryMissing() {
    candidate.setCountry(null);

    assertThatThrownBy(() -> allocator.allocateFor(candidate))
        .isInstanceOf(NoSuchObjectException.class)
        .hasMessageContaining("No country-specific")
        .hasMessageContaining(ServiceCode.HELP_SITE_LINK.name());
  }

  @Test
  @DisplayName("allocate throws when candidate country ISO code is blank")
  void allocateThrowsWhenIsoBlank() {
    candidate.getCountry().setIsoCode("   ");

    assertThatThrownBy(() -> allocator.allocateFor(candidate))
        .isInstanceOf(NoSuchObjectException.class)
        .hasMessageContaining("No country-specific")
        .hasMessageContaining(ServiceCode.HELP_SITE_LINK.name());
  }

  @Test
  @DisplayName("allocate throws when no country-specific resources are found")
  void allocateThrowsWhenNoCountryResourcesFound() {
    when(resourceRepository.findAvailableByProviderServiceAndCountry(
        ServiceProvider.UNHCR, ServiceCode.HELP_SITE_LINK, "PK"))
        .thenReturn(List.of());

    assertThatThrownBy(() -> allocator.allocateFor(candidate))
        .isInstanceOf(NoSuchObjectException.class)
        .hasMessageContaining("No HELP_SITE_LINK resources are configured for country");
  }

  @Test
  @DisplayName("getProvider returns configured provider")
  void getProviderReturnsConfiguredProvider() {
    assertThat(allocator.getProvider()).isEqualTo(ServiceProvider.UNHCR);
  }

  @Test
  @DisplayName("getServiceCode returns configured service code")
  void getServiceCodeReturnsConfiguredServiceCode() {
    assertThat(allocator.getServiceCode()).isEqualTo(ServiceCode.HELP_SITE_LINK);
  }
}
