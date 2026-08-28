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

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.tctalent.server.casi.application.support.RelevantCountryResolver;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.casi.domain.persistence.ServiceResourceRepository;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.service.db.impl.TcInstanceService;

/**
 * Eligibility policy for PiFi signposting links.
 *
 * @author sadatmalik
 */
@Component
@RequiredArgsConstructor
public class PifiEligibilityPolicy implements EligibilityPolicy {

  private final TcInstanceService tcInstanceService;
  private final RelevantCountryResolver countryResolver;
  private final ServiceResourceRepository resourceRepository;

  @Override
  public ServiceProvider provider() {
    return ServiceProvider.PIFI;
  }

  @Override
  public boolean isEligible(Long candidateId) {
    if (candidateId == null || !tcInstanceService.isGRN()) {
      return false;
    }

    try {
      return countryResolver.resolveCountryIsoCodes(candidateId).stream()
          .anyMatch(isoCode -> resourceRepository.countAvailableByProviderServiceAndCountry(
              ServiceProvider.PIFI,
              ServiceCode.HELP_SITE_LINK,
              isoCode) > 0);
    } catch (NoSuchObjectException e) {
      return false;
    }
  }
}
