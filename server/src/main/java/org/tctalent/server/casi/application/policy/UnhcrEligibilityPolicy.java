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

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.casi.domain.persistence.ServiceResourceRepository;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.service.db.CandidateService;

/**
 * Eligibility policy for UNHCR help site links.
 * A candidate is eligible only when resources exist for their current country.
 *
 * @author sadatmalik
 */
@Component
@RequiredArgsConstructor
public class UnhcrEligibilityPolicy implements EligibilityPolicy {

  private final CandidateService candidateService;
  private final ServiceResourceRepository resourceRepository;

  @Override
  public ServiceProvider provider() {
    return ServiceProvider.UNHCR;
  }

  @Override
  public boolean isEligible(Long candidateId) {
    if (candidateId == null) {
      return false;
    }
    try {
      Candidate candidate = candidateService.getCandidate(candidateId);
      String isoCode = candidate.getCountry() == null ? null : candidate.getCountry().getIsoCode();
      if (isoCode == null || isoCode.isBlank()) {
        return false;
      }
      return resourceRepository.countAvailableByProviderServiceAndCountry(
              ServiceProvider.UNHCR,
              ServiceCode.HELP_SITE_LINK,
              isoCode.trim().toUpperCase(Locale.ROOT))
          > 0;
    } catch (NoSuchObjectException e) {
      return false;
    }
  }
}
