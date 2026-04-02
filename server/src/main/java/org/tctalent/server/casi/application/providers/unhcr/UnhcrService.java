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

package org.tctalent.server.casi.application.providers.unhcr;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.casi.core.allocators.ResourceAllocator;
import org.tctalent.server.casi.core.services.AbstractCandidateAssistanceService;
import org.tctalent.server.casi.core.services.AssignmentEngine;
import org.tctalent.server.casi.domain.model.ResourceStatus;
import org.tctalent.server.casi.domain.model.ServiceAssignment;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.casi.domain.persistence.ServiceAssignmentRepository;
import org.tctalent.server.casi.domain.persistence.ServiceResourceRepository;
import org.tctalent.server.service.db.SavedListService;

/**
 * CASI service for country-specific UNHCR help site links.
 *
 * @author sadatmalik
 */
@Service
public class UnhcrService extends AbstractCandidateAssistanceService {

  private final ResourceAllocator unhcrAllocator;

  public UnhcrService(
      ServiceAssignmentRepository assignmentRepo,
      ServiceResourceRepository resourceRepo,
      AssignmentEngine assignmentEngine,
      SavedListService savedListService,
      @Qualifier("unhcrHelpSiteLinkAllocator") ResourceAllocator allocator) {
    super(assignmentRepo, resourceRepo, assignmentEngine, savedListService);
    this.unhcrAllocator = allocator;
  }

  @Override
  protected ServiceProvider provider() {
    return ServiceProvider.UNHCR;
  }

  @Override
  protected ServiceCode serviceCode() {
    return ServiceCode.HELP_SITE_LINK;
  }

  @Override
  protected ResourceAllocator allocator() {
    return unhcrAllocator;
  }

  // UNHCR help site link assignments do not expire, so we can simply return the first available assignment
  @Override
  @Transactional(readOnly = true)
  public ServiceAssignment getCurrentAssignment(Long candidateId) {
    return getAssignmentsForCandidate(candidateId).stream()
        .filter(a -> a.getResource() != null && a.getResource().getStatus() == ResourceStatus.AVAILABLE)
        .findFirst()
        .orElse(null);
  }
}
