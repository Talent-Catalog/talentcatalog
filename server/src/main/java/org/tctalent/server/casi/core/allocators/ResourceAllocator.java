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

package org.tctalent.server.casi.core.allocators;

import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.casi.domain.model.ServiceResource;
import org.tctalent.server.model.db.Candidate;


/**
 * Interface for allocating service resources to candidates.
 * Implementations of this interface should provide the logic to allocate
 * resources based on the candidate and the service provider's capabilities.
 *
 * @author sadatmalik
 */
public interface ResourceAllocator {

  // Allocate a service resource for the given candidate
  ServiceResource allocateFor(Candidate candidate);

  // Get the service provider associated with this allocator
  ServiceProvider getProvider();

  // Get the service code associated with this allocator
  ServiceCode getServiceCode();
}
