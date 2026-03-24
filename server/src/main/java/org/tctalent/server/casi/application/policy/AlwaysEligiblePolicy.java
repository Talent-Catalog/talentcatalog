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
import org.tctalent.server.casi.domain.model.ServiceProvider;

/**
 * Simple policy that makes all candidates eligible for a provider.
 * Used for testing and as a default policy when no other policies are defined for a provider.
 *
 * @author sadatmalik
 */
@RequiredArgsConstructor
public class AlwaysEligiblePolicy implements EligibilityPolicy {

  private final ServiceProvider provider;

  @Override
  public ServiceProvider provider() {
    return provider;
  }

  @Override
  public boolean isEligible(Long candidateId) {
    return true;
  }
}
