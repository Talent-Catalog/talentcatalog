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

package org.tctalent.server.casi.application.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.casi.domain.persistence.ServiceResourceRepository;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.service.db.CandidateService;

@ExtendWith(MockitoExtension.class)
class UnhcrEligibilityPolicyTest {

  @Mock
  private CandidateService candidateService;

  @Mock
  private ServiceResourceRepository resourceRepository;

  private UnhcrEligibilityPolicy policy;

  @BeforeEach
  void setUp() {
    policy = new UnhcrEligibilityPolicy(candidateService, resourceRepository);
  }

  @Test
  @DisplayName("provider returns UNHCR")
  void providerReturnsUnhcr() {
    assertThat(policy.provider()).isEqualTo(ServiceProvider.UNHCR);
  }

  @Test
  @DisplayName("isEligible returns false when candidateId is null")
  void isEligibleReturnsFalseWhenCandidateIdNull() {
    assertThat(policy.isEligible(null)).isFalse();
  }

  @Test
  @DisplayName("isEligible returns false when candidate does not exist")
  void isEligibleReturnsFalseWhenCandidateNotFound() {
    when(candidateService.getCandidate(99L))
        .thenThrow(new NoSuchObjectException("Candidate not found"));

    assertThat(policy.isEligible(99L)).isFalse();
  }

  @Test
  @DisplayName("isEligible returns false when candidate country is missing")
  void isEligibleReturnsFalseWhenCountryMissing() {
    Candidate candidate = new Candidate();
    candidate.setCountry(null);
    when(candidateService.getCandidate(1L)).thenReturn(candidate);

    assertThat(policy.isEligible(1L)).isFalse();
  }

  @Test
  @DisplayName("isEligible returns false when country isoCode is blank")
  void isEligibleReturnsFalseWhenCountryIsoBlank() {
    Candidate candidate = new Candidate();
    Country country = new Country();
    country.setIsoCode("   ");
    candidate.setCountry(country);
    when(candidateService.getCandidate(1L)).thenReturn(candidate);

    assertThat(policy.isEligible(1L)).isFalse();
  }

  @Test
  @DisplayName("isEligible returns true when country resources exist")
  void isEligibleReturnsTrueWhenCountryResourcesExist() {
    Candidate candidate = candidateWithIso("PK");
    when(candidateService.getCandidate(1L)).thenReturn(candidate);
    when(resourceRepository.countAvailableByProviderServiceAndCountry(
        ServiceProvider.UNHCR, ServiceCode.HELP_SITE_LINK, "PK"))
        .thenReturn(1L);

    assertThat(policy.isEligible(1L)).isTrue();
  }

  @Test
  @DisplayName("isEligible returns false when country resources do not exist")
  void isEligibleReturnsFalseWhenCountryResourcesDoNotExist() {
    Candidate candidate = candidateWithIso("PK");
    when(candidateService.getCandidate(1L)).thenReturn(candidate);
    when(resourceRepository.countAvailableByProviderServiceAndCountry(
        ServiceProvider.UNHCR, ServiceCode.HELP_SITE_LINK, "PK"))
        .thenReturn(0L);

    assertThat(policy.isEligible(1L)).isFalse();
  }

  @Test
  @DisplayName("isEligible uppercases isoCode before repository lookup")
  void isEligibleUppercasesIsoCodeBeforeLookup() {
    Candidate candidate = candidateWithIso("pk");
    when(candidateService.getCandidate(1L)).thenReturn(candidate);
    when(resourceRepository.countAvailableByProviderServiceAndCountry(
        ServiceProvider.UNHCR, ServiceCode.HELP_SITE_LINK, "PK"))
        .thenReturn(1L);

    assertThat(policy.isEligible(1L)).isTrue();
    verify(resourceRepository).countAvailableByProviderServiceAndCountry(
        ServiceProvider.UNHCR, ServiceCode.HELP_SITE_LINK, "PK");
  }

  private Candidate candidateWithIso(String isoCode) {
    Candidate candidate = new Candidate();
    Country country = new Country();
    country.setIsoCode(isoCode);
    candidate.setCountry(country);
    return candidate;
  }
}
