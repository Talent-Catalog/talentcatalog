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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.tctalent.server.casi.domain.model.ServiceProvider;

/**
 * Registry of EligibilityPolicy implementations keyed by provider.
 * Providers without an explicit policy default to eligible.
 *
 * @author sadatmalik
 */
@Component
public class EligibilityPolicyRegistry {

  private final Map<ServiceProvider, EligibilityPolicy> policies;

  public EligibilityPolicyRegistry(List<EligibilityPolicy> policies) {
    Map<ServiceProvider, EligibilityPolicy> map = new HashMap<>();
    for (EligibilityPolicy p : policies) {
      ServiceProvider key = p.provider();
      if (map.putIfAbsent(key, p) != null) {
        throw new IllegalStateException("Duplicate EligibilityPolicy for provider=" + key);
      }
    }
    this.policies = Map.copyOf(map);
  }

  public boolean isEligible(ServiceProvider provider, Long candidateId) {
    EligibilityPolicy policy = policies.get(provider);
    if (policy == null) {
      return true;
    }
    return policy.isEligible(candidateId);
  }
}
