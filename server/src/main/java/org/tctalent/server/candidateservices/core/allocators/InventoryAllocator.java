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

package org.tctalent.server.candidateservices.core.allocators;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.candidateservices.domain.model.ResourceStatus;
import org.tctalent.server.candidateservices.domain.model.ServiceResource;
import org.tctalent.server.candidateservices.domain.persistence.ServiceResourceEntity;
import org.tctalent.server.candidateservices.domain.persistence.ServiceResourceRepository;
import org.tctalent.server.candidateservices.domain.mappers.ServiceResourceMapper;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;

@RequiredArgsConstructor
public class InventoryAllocator implements ResourceAllocator {

  private final ServiceResourceRepository resources;
  private final String provider;
  private final String serviceCode;

  @Override
  @Transactional
  public ServiceResource allocateFor(Candidate c) {
    // get and reserve a service resource e.g. a coupon
    ServiceResourceEntity e = resources.lockNextAvailable(provider, serviceCode);
    if (e == null) {
      throw new NoSuchObjectException("There are no available " + serviceCode + " coupons to assign to the candidate. "
          + "Please import more coupons from the settings page.");
    }

    e.setStatus(ResourceStatus.RESERVED);
    resources.save(e);

    return ServiceResourceMapper.toModel(e);
  }

  @Override
  public String getProvider() {
    return provider;
  }

  @Override
  public String getServiceCode() {
    return serviceCode;
  }
}
