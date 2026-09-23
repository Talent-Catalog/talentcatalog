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

package org.tctalent.server.casi.application.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.casi.application.support.RelevantCountryResolver;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.casi.domain.persistence.ServiceResourceRepository;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.service.db.impl.TcInstanceService;

@ExtendWith(MockitoExtension.class)
class PifiEligibilityPolicyTest {

  @Mock
  private TcInstanceService tcInstanceService;
  @Mock
  private RelevantCountryResolver countryResolver;
  @Mock
  private ServiceResourceRepository resourceRepository;

  private PifiEligibilityPolicy policy;

  @BeforeEach
  void setUp() {
    policy = new PifiEligibilityPolicy(tcInstanceService, countryResolver, resourceRepository);
  }

  @Test
  @DisplayName("provider returns PIFI")
  void providerReturnsPifi() {
    assertThat(policy.provider()).isEqualTo(ServiceProvider.PIFI);
  }

  @Test
  @DisplayName("ineligible when not GRN")
  void ineligibleWhenNotGrn() {
    when(tcInstanceService.isGRN()).thenReturn(false);

    assertThat(policy.isEligible(1L)).isFalse();
  }

  @Test
  @DisplayName("eligible when any relevant country has resources")
  void eligibleWhenAnyRelevantCountryHasResources() {
    when(tcInstanceService.isGRN()).thenReturn(true);
    when(countryResolver.resolveCountryIsoCodes(1L)).thenReturn(List.of("AU", "PK"));
    when(resourceRepository.countAvailableByProviderServiceAndCountry(
        ServiceProvider.PIFI, ServiceCode.HELP_SITE_LINK, "AU")).thenReturn(1L);

    assertThat(policy.isEligible(1L)).isTrue();
  }

  @Test
  @DisplayName("ineligible when no relevant country has resources")
  void ineligibleWhenNoRelevantCountryHasResources() {
    when(tcInstanceService.isGRN()).thenReturn(true);
    when(countryResolver.resolveCountryIsoCodes(1L)).thenReturn(List.of("AU", "PK"));
    when(resourceRepository.countAvailableByProviderServiceAndCountry(
        ServiceProvider.PIFI, ServiceCode.HELP_SITE_LINK, "AU")).thenReturn(0L);
    when(resourceRepository.countAvailableByProviderServiceAndCountry(
        ServiceProvider.PIFI, ServiceCode.HELP_SITE_LINK, "PK")).thenReturn(0L);

    assertThat(policy.isEligible(1L)).isFalse();
  }

  @Test
  @DisplayName("ineligible when candidate lookup fails")
  void ineligibleWhenCandidateLookupFails() {
    when(tcInstanceService.isGRN()).thenReturn(true);
    when(countryResolver.resolveCountryIsoCodes(1L)).thenThrow(new NoSuchObjectException("missing"));

    assertThat(policy.isEligible(1L)).isFalse();
  }
}
