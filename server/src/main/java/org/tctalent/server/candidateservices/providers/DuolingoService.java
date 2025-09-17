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

package org.tctalent.server.candidateservices.providers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.tctalent.server.candidateservices.application.AbstractCandidateService;
import org.tctalent.server.candidateservices.application.AssignmentEngine;
import org.tctalent.server.candidateservices.application.alloc.ResourceAllocator;
import org.tctalent.server.candidateservices.domain.model.ServiceCode;
import org.tctalent.server.candidateservices.infrastructure.importers.FileInventoryImporter;
import org.tctalent.server.candidateservices.infrastructure.persistence.assignment.ServiceAssignmentRepository;
import org.tctalent.server.candidateservices.infrastructure.persistence.resource.ServiceResourceRepository;
import org.tctalent.server.service.db.SavedListService;


@Service
@Slf4j
public class DuolingoService extends AbstractCandidateService {

  private final FileInventoryImporter duolingoImporter;
  private final ResourceAllocator duolingoAllocator;

  public DuolingoService(ServiceAssignmentRepository assignmentRepo,
      ServiceResourceRepository resourceRepo,
      AssignmentEngine assignmentEngine,
      SavedListService savedListService,
      FileInventoryImporter duolingoCouponImporter,
      @Qualifier("duolingoProctoredAllocator") ResourceAllocator duolingoProctoredAllocator) {
    super(assignmentRepo, resourceRepo, assignmentEngine, savedListService);
    this.duolingoImporter = duolingoCouponImporter;
    this.duolingoAllocator = duolingoProctoredAllocator;
  }

  @Override
  protected String provider() {
    return "DUOLINGO";
  }

  @Override
  protected ServiceCode serviceCode() {
    return ServiceCode.DUOLINGO_TEST_PROCTORED;
  }

  @Override
  protected ResourceAllocator allocator() {
    return duolingoAllocator;
  }

  @Override
  protected FileInventoryImporter importer() {
    return duolingoImporter;
  }

}
