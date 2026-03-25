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

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.tctalent.server.casi.domain.mappers.ServiceResourceMapper;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.casi.domain.model.ServiceResource;
import org.tctalent.server.casi.domain.persistence.ServiceResourceRepository;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;

/**
 * Allocates a shared resource scoped by candidate country.
 * Selected resources remain AVAILABLE so they can be reused by multiple candidates.
 *
 * @author sadatmalik
 */
@RequiredArgsConstructor
public class SharedCountryResourceAllocator implements ResourceAllocator {

  private final ServiceResourceRepository resources;
  private final ServiceProvider provider;
  private final ServiceCode serviceCode;

  @Override
  public ServiceResource allocateFor(Candidate candidate) {
    String isoCode = candidate.getCountry() == null ? null : candidate.getCountry().getIsoCode();
    if (isoCode == null || isoCode.isBlank()) {
      throw new NoSuchObjectException("No country-specific " + serviceCode
          + " resources are configured for this candidate.");
    }

    return resources.findAvailableByProviderServiceAndCountry(
            provider,
            serviceCode,
            isoCode.trim().toUpperCase(Locale.ROOT))
        .stream()
        .findFirst()
        .map(ServiceResourceMapper::toModel)
        .orElseThrow(() -> new NoSuchObjectException(
            "No " + serviceCode + " resources are configured for country " + isoCode + "."));
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
