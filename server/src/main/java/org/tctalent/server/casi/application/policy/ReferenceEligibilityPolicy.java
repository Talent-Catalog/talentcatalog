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

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.service.db.CandidateService;

/**
 * Reference implementation of {@link EligibilityPolicy} for the REFERENCE CASI provider.
 * Demonstrates the eligibility infrastructure: a candidate is eligible only when they exist
 * and have a non-inactive {@link org.tctalent.server.model.db.CandidateStatus} (e.g. active,
 * pending, incomplete, unreachable). Candidates who are deleted, employed, withdrawn, etc. are
 * not eligible.
 *
 * @author sadatmalik
 */
@Component
@RequiredArgsConstructor
public class ReferenceEligibilityPolicy implements EligibilityPolicy {

  private final CandidateService candidateService;

  @Override
  public ServiceProvider provider() {
    return ServiceProvider.REFERENCE;
  }

  @Override
  public boolean isEligible(Long candidateId) {
    if (candidateId == null) {
      return false;
    }
    try {
      Candidate candidate = candidateService.getCandidate(candidateId);
      return !candidate.getStatus().isInactive();
    } catch (NoSuchObjectException e) {
      return false;
    }
  }
}
