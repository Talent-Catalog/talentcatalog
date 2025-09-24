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

package org.tctalent.server.casi.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.persistence.ServiceResourceRepository;
import org.tctalent.server.casi.core.allocators.InventoryAllocator;
import org.tctalent.server.casi.core.allocators.ResourceAllocator;


@Configuration
public class AllocatorsConfig {

  @Bean("duolingoNonProctoredAllocator")
  public ResourceAllocator duolingoNonProctoredAllocator(ServiceResourceRepository repo) {
    return new InventoryAllocator(repo, "DUOLINGO", ServiceCode.TEST_NON_PROCTORED.name()); // TODO -- SM -- pass ServiceCode enum type
  }

  @Bean("duolingoProctoredAllocator")
  public ResourceAllocator duolingoProctoredAllocator(ServiceResourceRepository repo) {
    return new InventoryAllocator(repo, "DUOLINGO", ServiceCode.TEST_PROCTORED.name()); // TODO -- SM -- pass ServiceCode enum type
  }

  // Add more providers by instantiating InventoryAllocator or other implementation of ResourceAllocator
}
