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

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.candidateservices.application.alloc.ResourceAllocator;
import org.tctalent.server.candidateservices.domain.model.AssignmentStatus;
import org.tctalent.server.candidateservices.domain.model.ServiceAssignment;
import org.tctalent.server.candidateservices.domain.model.ServiceResource;
import org.tctalent.server.candidateservices.infrastructure.importers.FileInventoryImporter;
import org.tctalent.server.candidateservices.infrastructure.persistence.assignment.ServiceAssignmentEntity;
import org.tctalent.server.candidateservices.infrastructure.persistence.assignment.ServiceAssignmentRepository;
import org.tctalent.server.candidateservices.infrastructure.persistence.resource.ServiceResourceRepository;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.ImportFailedException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
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
  @Transactional
  public ServiceAssignment assignToCandidate(Long candidateId, User user, String serviceCode) {
    List<ServiceAssignment> assignments = getAssignmentsForCandidate(candidateId, serviceCode);

    for (ServiceAssignment a : assignments) {
      if (a.getStatus().equals(AssignmentStatus.ASSIGNED)) {
        throw new EntityExistsException("coupon", "for this candidate");
      }
    }

    var allocator = allocator(serviceCode);
    if (allocator == null) {
      throw new IllegalArgumentException("No allocator for service code " + serviceCode);
    }
    return assignmentEngine.assign(allocator, candidateId, user);
  }

  @Override
  @Transactional
  public List<ServiceAssignment> assignToList(Long listId, String serviceCode, User user) {
    var savedList = savedListService.get(listId);
    var candidates = savedList.getCandidates();

    // Confirm available resources
    List<ServiceResource> availableResources = getAvailableResources();

    if (availableResources.isEmpty() || candidates.size() > availableResources.size()) {
      throw new NoSuchObjectException(
          "There are not enough available coupons to assign to all candidates in the list. Please import more coupons from the settings page.");
    }

    List<ServiceAssignment> done = new ArrayList<>();
    for (Candidate candidate : candidates) {
      boolean hasSentCoupon = assignmentRepository
          .findByCandidateAndProviderAndService(candidate.getId(), provider(), serviceCode)
          .stream()
          .anyMatch(assignment -> assignment.getStatus() == AssignmentStatus.ASSIGNED);

      if (!hasSentCoupon) {
        done.add(assignToCandidate(candidate.getId(), user, serviceCode));
      }
    }
    return done;
  }

  @Override
  @Transactional(readOnly = true)
  public List<ServiceAssignment> getAssignmentsForCandidate(Long candidateId, String serviceCode) {
    List<ServiceAssignmentEntity> assignments = assignmentRepository
        .findByCandidateAndProviderAndService(candidateId, provider(), serviceCode);
    List<ServiceAssignment> models = new ArrayList<>();
    for (ServiceAssignmentEntity a : assignments) {
      models.add(ServiceAssignment.from(a));
    }
    return models;
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
