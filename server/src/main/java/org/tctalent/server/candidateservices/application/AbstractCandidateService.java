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

package org.tctalent.server.candidateservices.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.candidateservices.application.alloc.ResourceAllocator;
import org.tctalent.server.candidateservices.domain.model.ServiceAssignment;
import org.tctalent.server.candidateservices.domain.model.ServiceResource;
import org.tctalent.server.candidateservices.infrastructure.importers.FileInventoryImporter;
import org.tctalent.server.candidateservices.infrastructure.persistence.assignment.ServiceAssignmentRepository;
import org.tctalent.server.candidateservices.infrastructure.persistence.resource.ServiceResourceRepository;
import org.tctalent.server.exception.ImportFailedException;
import org.tctalent.server.model.db.User;
import org.tctalent.server.service.db.SavedListService;

@RequiredArgsConstructor
public abstract class AbstractCandidateService implements CandidateService {

  protected final ServiceAssignmentRepository assignmentRepository;
  protected final ServiceResourceRepository resourceRepository;
  protected final AssignmentEngine assignmentEngine;
  protected final SavedListService savedListService;

  // provider-specific hooks
  protected abstract String provider();                        // e.g. "DUOLINGO"
  protected abstract ResourceAllocator allocator(String serviceCode);
  protected FileInventoryImporter importer() { return null; }  // Optional

  // default implementations
  @Override
  @Transactional
  public void importInventory(MultipartFile file, String serviceCode) throws ImportFailedException {
    var importer = importer();
    if (importer == null) {
      throw new ImportFailedException("Import not supported for " + provider());
    }
    importer.importFile(file, serviceCode);
  }

  @Override
  public ServiceAssignment assignToCandidate(Long candidateId, User actor, String serviceCode) {
    return null;
  }

  @Override
  public List<ServiceAssignment> assignToList(Long listId, String serviceCode, User actor) {
    return List.of();
  }

  @Override
  public List<ServiceAssignment> getAssignmentsForCandidate(Long candidateId, String serviceCode) {
    return List.of();
  }

  @Override
  public List<ServiceResource> getAvailableResources() {
    return List.of();
  }


  @Override
  public long countAvailableForProvider() {
    return 0;
  }
}
