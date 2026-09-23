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

package org.tctalent.server.casi.core.allocators;

import lombok.RequiredArgsConstructor;
import org.tctalent.server.casi.application.support.RelevantCountryResolver;
import org.tctalent.server.casi.domain.mappers.ServiceResourceMapper;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.casi.domain.model.ServiceResource;
import org.tctalent.server.casi.domain.persistence.ServiceResourceRepository;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;

/**
 * Allocates the first country-matching shared resource from a candidate's relevant countries.
 *
 * @author sadatmalik
 */
@RequiredArgsConstructor
public class FirstRelevantCountrySharedResourceAllocator implements ResourceAllocator {

  private final ServiceResourceRepository resources;
  private final RelevantCountryResolver countryResolver;
  private final ServiceProvider provider;
  private final ServiceCode serviceCode;

  @Override
  public ServiceResource allocateFor(Candidate candidate) {
    for (String isoCode : countryResolver.resolveCountryIsoCodes(candidate)) {
      ServiceResource resource = resources.findAvailableByProviderServiceAndCountry(
              provider,
              serviceCode,
              isoCode)
          .stream()
          .findFirst()
          .map(ServiceResourceMapper::toModel)
          .orElse(null);
      if (resource != null) {
        return resource;
      }
    }

    throw new NoSuchObjectException("No " + serviceCode + " resources are configured for candidate "
        + candidate.getId() + ".");
  }

  @Override
  public ServiceProvider getProvider() {
    return provider;
  }

  @Override
  public ServiceCode getServiceCode() {
    return serviceCode;
  }
}
