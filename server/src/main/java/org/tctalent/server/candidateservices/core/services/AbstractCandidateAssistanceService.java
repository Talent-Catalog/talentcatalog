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

package org.tctalent.server.candidateservices.core.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.candidateservices.domain.mappers.ServiceAssignmentMapper;
import org.tctalent.server.candidateservices.domain.mappers.ServiceResourceMapper;
import org.tctalent.server.candidateservices.domain.model.AssignmentStatus;
import org.tctalent.server.candidateservices.domain.model.ResourceStatus;
import org.tctalent.server.candidateservices.domain.model.ServiceAssignment;
import org.tctalent.server.candidateservices.domain.model.ServiceCode;
import org.tctalent.server.candidateservices.domain.model.ServiceResource;
import org.tctalent.server.candidateservices.domain.persistence.ServiceAssignmentEntity;
import org.tctalent.server.candidateservices.domain.persistence.ServiceAssignmentRepository;
import org.tctalent.server.candidateservices.domain.persistence.ServiceResourceRepository;
import org.tctalent.server.candidateservices.core.allocators.ResourceAllocator;
import org.tctalent.server.candidateservices.core.importers.FileInventoryImporter;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.ImportFailedException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.User;
import org.tctalent.server.service.db.SavedListService;

@RequiredArgsConstructor
public abstract class AbstractCandidateAssistanceService implements CandidateAssistanceService {

  protected final ServiceAssignmentRepository assignmentRepository;
  protected final ServiceResourceRepository resourceRepository;
  protected final AssignmentEngine assignmentEngine;
  protected final SavedListService savedListService;

  // provider-specific hooks
  protected abstract String provider();                        // e.g. "DUOLINGO"
  protected abstract ServiceCode serviceCode();                // e.g. "DUOLINGO_TEST_PROCTORED"
  protected abstract ResourceAllocator allocator(); // TODO -- SM -- have a default allocator that assigns a single resource to multiple candidates?
  protected FileInventoryImporter importer() { return null; }  // Optional

  // default implementations
  @Override
  public String providerKey() {
    return provider().trim().toUpperCase(Locale.ROOT) + "::"
        + serviceCode().name().trim().toUpperCase(Locale.ROOT);
  }

  @Override
  @Transactional
  public void importInventory(MultipartFile file) throws ImportFailedException {
    var importer = importer();
    if (importer == null) {
      throw new ImportFailedException("Import not supported for " + provider());
    }
    importer.importFile(file, serviceCode().name());
  }

  @Override
  @Transactional
  public ServiceAssignment assignToCandidate(Long candidateId, User user) {
    var assignments = getAssignmentsForCandidate(candidateId);

    for (ServiceAssignment a : assignments) {
      if (a.getStatus().equals(AssignmentStatus.ASSIGNED)) {
        throw new EntityExistsException(serviceCode() + " resources", "for this candidate");
      }
    }

    return assignmentEngine.assign(allocator(), candidateId, user);
  }

  @Override
  @Transactional
  public ServiceAssignment reassignForCandidate(String candidateNumber, User user)
      throws NoSuchObjectException {

    return assignmentEngine.reassign(allocator(), candidateNumber, user);
  }

  @Override
  @Transactional
  public List<ServiceAssignment> assignToList(Long listId, User user) {
    var savedList = savedListService.get(listId);
    var candidates = savedList.getCandidates();

    // Confirm available resources
    List<ServiceResource> availableResources = getAvailableResources();

    if (availableResources.isEmpty() || candidates.size() > availableResources.size()) {
      throw new NoSuchObjectException(
          "There are not enough available " + serviceCode() + " resources to assign to all candidates "
              + "in the list. Please import more from the settings page.");
    }

    List<ServiceAssignment> done = new ArrayList<>();
    for (Candidate candidate : candidates) {
      boolean hasSentCoupon = assignmentRepository
          .findByCandidateAndProviderAndService(candidate.getId(), provider(), serviceCode().name())
          .stream()
          .anyMatch(assignment -> assignment.getStatus() == AssignmentStatus.ASSIGNED);

      if (!hasSentCoupon) {
        done.add(assignToCandidate(candidate.getId(), user));
      }
    }
    return done;
  }

  @Override
  @Transactional(readOnly = true)
  public List<ServiceAssignment> getAssignmentsForCandidate(Long candidateId) {
    return assignmentRepository
        .findByCandidateAndProviderAndService(candidateId, provider(), serviceCode().name())
        .stream()
        .map(ServiceAssignmentMapper::toModel)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<ServiceResource> getResourcesForCandidate(Long candidateId) {
    var assignments = getAssignmentsForCandidate(candidateId);
    return assignments.stream()
        .map(ServiceAssignment::getResource)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<ServiceResource> getAvailableResources() {
    return resourceRepository
        .findByProviderAndServiceCodeAndStatus(provider(), serviceCode(), ResourceStatus.AVAILABLE)
        .stream()
        .map(ServiceResourceMapper::toModel)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public ServiceResource getResourceForResourceCode(String resourceCode) throws NoSuchObjectException {
    return resourceRepository
        .findByProviderAndResourceCode(provider(), resourceCode)
        .map(ServiceResourceMapper::toModel)
        .orElseThrow(() -> new NoSuchObjectException("Coupon with code " + resourceCode + " not found"));
  }

  @Override
  public Candidate getCandidateForResourceCode(String resourceCode) throws NoSuchObjectException {
    var resource = resourceRepository
        .findByProviderAndResourceCode(provider(), resourceCode)
        .map(ServiceResourceMapper::toModel)
        .orElseThrow(() -> new NoSuchObjectException("Coupon with code " + resourceCode + " not found"));

    return assignmentRepository
        .findTopByProviderAndServiceAndResource(provider(), serviceCode().name(), resource.getId())
        .map(ServiceAssignmentEntity::getCandidate)
        .orElse(null);
  }

  @Override
  @Transactional
  public void updateResourceStatus(String resourceCode, ResourceStatus status) {
    resourceRepository
        .findByProviderAndResourceCode(provider(), resourceCode)
        .ifPresent(resource -> {
          resource.setStatus(status);
          resourceRepository.save(resource);
        });
  }

  @Override
  public long countAvailableForProvider() {
    return resourceRepository.countAvailableByProvider(provider());
  }

  @Override
  public long countAvailableForProviderAndService() {
    return resourceRepository.countAvailableByProviderAndService(provider(), serviceCode());
  }
}
