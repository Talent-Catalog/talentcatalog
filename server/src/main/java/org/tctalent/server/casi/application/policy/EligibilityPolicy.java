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

import org.tctalent.server.casi.domain.model.ServiceProvider;

/**
 * Eligibility strategy for a CASI provider.
 * Implementations decide whether a candidate can access a provider's service.
 *
 * @author sadatmalik
 */
public interface EligibilityPolicy {

  ServiceProvider provider();

  boolean isEligible(Long candidateId);
}
