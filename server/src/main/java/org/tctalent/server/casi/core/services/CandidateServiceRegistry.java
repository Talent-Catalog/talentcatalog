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

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;


/**
 * Registry for CandidateAssistanceService implementations.
 * It maps service providers and their service codes to the corresponding
 * CandidateAssistanceService instances.
 * <p>
 * This allows for dynamic retrieval of services based on provider and service code.
 * <p>
 * Example usage:
 * CandidateAssistanceService service = registry.forProviderAndServiceCode("ProviderA", "ServiceX");
 * <p>
 * The provider and service code are normalised for consistent lookups.
 *
 * @author sadatmalik
 */
@Component
public class CandidateServiceRegistry {

  private static final String SEP = "::";
  private final Map<String, CandidateAssistanceService> services; // provider -> service

  /**
   * Constructs the registry with a list of CandidateAssistanceService beans.
   * It builds a map of services keyed by a combination of provider and service code.
   *
   * @param serviceBeans the list of CandidateAssistanceService implementations
   */
  public CandidateServiceRegistry(List<CandidateAssistanceService> serviceBeans) {
    Map<String, CandidateAssistanceService> map = new HashMap<>();
    for (CandidateAssistanceService s : serviceBeans) {
      String key = normalise(s.providerKey());
      if (map.putIfAbsent(key, s) != null) {
        throw new IllegalStateException("Duplicate CandidateAssistanceService for provider=" + key);
      }
    }
    this.services = Map.copyOf(map);
  }

  /**
   * Retrieves the CandidateAssistanceService for the given provider and service code.
   * The provider and service code are normalised for consistent lookups.
   *
   * @param provider the service provider (e.g., "DUOLINGO")
   * @param serviceCode the specific service code (e.g., "TEST_PROCTORED")
   * @return the corresponding CandidateAssistanceService
   * @throws IllegalStateException if no service is found for the given provider and service code
   */
  public CandidateAssistanceService forProviderAndServiceCode(String provider, String serviceCode) {
    String key = (normalise(provider) + SEP + normalise(serviceCode));
    CandidateAssistanceService svc = services.get(key);
    if (svc == null) {
      throw new IllegalStateException("No service for " + key);
    }
    return svc;
  }

  private static String normalise(String v) {
    return v == null ? null : v.trim().toUpperCase(Locale.ROOT);
  }
}
