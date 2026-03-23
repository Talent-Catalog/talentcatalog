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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.tctalent.server.casi.domain.model.ServiceProvider;

class EligibilityPolicyRegistryTest {

  @Test
  @DisplayName("applies configured policy for a provider")
  void appliesConfiguredPolicyForProvider() {
    EligibilityPolicy linkedInPolicy = new EligibilityPolicy() {
      @Override
      public ServiceProvider provider() {
        return ServiceProvider.LINKEDIN;
      }

      @Override
      public boolean isEligible(Long candidateId) {
        return candidateId != null && candidateId == 123L;
      }
    };

    EligibilityPolicyRegistry registry = new EligibilityPolicyRegistry(List.of(linkedInPolicy));

    assertThat(registry.isEligible(ServiceProvider.LINKEDIN, 123L)).isTrue();
    assertThat(registry.isEligible(ServiceProvider.LINKEDIN, 999L)).isFalse();
  }

  @Test
  @DisplayName("defaults to eligible when no policy is configured")
  void defaultsToEligibleWhenNoPolicyConfigured() {
    EligibilityPolicyRegistry registry = new EligibilityPolicyRegistry(List.of());
    assertThat(registry.isEligible(ServiceProvider.DUOLINGO, 123L)).isTrue();
  }

  @Test
  @DisplayName("applies always-eligible policy implementation")
  void appliesAlwaysEligiblePolicyImplementation() {
    EligibilityPolicy duolingoPolicy = new AlwaysEligiblePolicy(ServiceProvider.DUOLINGO);
    EligibilityPolicyRegistry registry = new EligibilityPolicyRegistry(List.of(duolingoPolicy));

    assertThat(registry.isEligible(ServiceProvider.DUOLINGO, 1L)).isTrue();
  }

  @Test
  @DisplayName("rejects duplicate policies for the same provider")
  void rejectsDuplicatePoliciesForSameProvider() {
    EligibilityPolicy p1 = new AlwaysEligiblePolicy(ServiceProvider.DUOLINGO);
    EligibilityPolicy p2 = new AlwaysEligiblePolicy(ServiceProvider.DUOLINGO);

    assertThatThrownBy(() -> new EligibilityPolicyRegistry(List.of(p1, p2)))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Duplicate EligibilityPolicy");
  }
}
