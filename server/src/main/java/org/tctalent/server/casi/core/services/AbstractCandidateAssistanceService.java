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

package org.tctalent.server.casi.core.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.casi.domain.mappers.ServiceAssignmentMapper;
import org.tctalent.server.casi.domain.mappers.ServiceResourceMapper;
import org.tctalent.server.casi.domain.model.AssignmentStatus;
import org.tctalent.server.casi.domain.model.ResourceStatus;
import org.tctalent.server.casi.domain.model.ServiceAssignment;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.casi.domain.model.ServiceResource;
import org.tctalent.server.casi.domain.persistence.ServiceAssignmentEntity;
import org.tctalent.server.casi.domain.persistence.ServiceAssignmentRepository;
import org.tctalent.server.casi.domain.persistence.ServiceResourceEntity;
import org.tctalent.server.casi.domain.persistence.ServiceResourceRepository;
import org.tctalent.server.casi.core.allocators.ResourceAllocator;
import org.tctalent.server.casi.core.importers.FileInventoryImporter;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.ImportFailedException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.User;
import org.tctalent.server.service.db.SavedListService;


/**
 * Base class for candidate assistance services (e.g., Duolingo, PathwayClub, etc.)
 * that manage service assignments and resources.
 *
 * @author sadatmalik
 */
@RequiredArgsConstructor
public abstract class AbstractCandidateAssistanceService implements CandidateAssistanceService {

  protected final ServiceAssignmentRepository assignmentRepository;
  protected final ServiceResourceRepository resourceRepository;
  protected final AssignmentEngine assignmentEngine;
  protected final SavedListService savedListService;

  // provider-specific hooks
  protected abstract ServiceProvider provider();               // e.g. "DUOLINGO"
  protected abstract ServiceCode serviceCode();                // e.g. "TEST_PROCTORED"
  protected abstract ResourceAllocator allocator(); // TODO -- SM -- have a default allocator that assigns a single resource to multiple candidates?
  protected FileInventoryImporter importer() { return null; }  // Optional

  // default implementations
  @Override
  public String providerKey() {
    return provider().name().trim().toUpperCase(Locale.ROOT) + "::"
        + serviceCode().name().trim().toUpperCase(Locale.ROOT);
  }

  // Import inventory from file
  @Override
  @Transactional
  public void importInventory(MultipartFile file) throws ImportFailedException {
    var importer = importer();
    if (importer == null) {
      throw new ImportFailedException("Import not supported for " + provider());
    }
    importer.importFile(file, serviceCode().name());
  }

  // Assignments
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

  // Reassignments
  @Override
  @Transactional
  public ServiceAssignment reassignForCandidate(String candidateNumber, User user)
      throws NoSuchObjectException {

    return assignmentEngine.reassign(allocator(), candidateNumber, user);
  }

  // Assign all candidates in a saved list
  // Skip candidates who already have an active assignment
  // Fail if not enough resources to assign to all candidates
  // Return list of assignments done
  // SM -- TODO -- add pagination for large lists
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
          .findByCandidateAndProviderAndService(candidate.getId(), provider(), serviceCode())
          .stream()
          .anyMatch(assignment -> assignment.getStatus() == AssignmentStatus.ASSIGNED);

      if (!hasSentCoupon) {
        done.add(assignToCandidate(candidate.getId(), user));
      }
    }
    return done;
  }

  // Queries
  @Override
  @Transactional(readOnly = true)
  public List<ServiceAssignment> getAssignmentsForCandidate(Long candidateId) {
    return assignmentRepository
        .findByCandidateAndProviderAndService(candidateId, provider(), serviceCode())
        .stream()
        .map(ServiceAssignmentMapper::toModel)
        .toList();
  }

  // Resources
  @Override
  @Transactional(readOnly = true)
  public List<ServiceResource> getResourcesForCandidate(Long candidateId) {
    var assignments = getAssignmentsForCandidate(candidateId);
    return assignments.stream()
        .map(ServiceAssignment::getResource)
        .toList();
  }

  // Get all available resources for this provider and service
  // (e.g., all available Duolingo TEST_PROCTORED coupons)
  @Override
  @Transactional(readOnly = true)
  public List<ServiceResource> getAvailableResources() {
    return resourceRepository
        .findByProviderAndServiceCodeAndStatus(provider(), serviceCode(), ResourceStatus.AVAILABLE)
        .stream()
        .map(ServiceResourceMapper::toModel)
        .toList();
  }

  // Get resource by resource code (e.g., get coupon by coupon code)
  // Throws NoSuchObjectException if not found
  @Override
  @Transactional(readOnly = true)
  public ServiceResource getResourceForResourceCode(String resourceCode) throws NoSuchObjectException {
    return resourceRepository
        .findByProviderAndResourceCode(provider(), resourceCode)
        .map(ServiceResourceMapper::toModel)
        .orElseThrow(() -> new NoSuchObjectException("Coupon with code " + resourceCode + " not found"));
  }

  // Get candidate assigned to a resource by resource code (e.g., get candidate assigned to a coupon by coupon code)
  // Returns null if not assigned
  // Throws NoSuchObjectException if resource not found
  @Override
  public Candidate getCandidateForResourceCode(String resourceCode) throws NoSuchObjectException {
    var resource = resourceRepository
        .findByProviderAndResourceCode(provider(), resourceCode)
        .map(ServiceResourceMapper::toModel)
        .orElseThrow(() -> new NoSuchObjectException("Coupon with code " + resourceCode + " not found"));

    return assignmentRepository
        .findTopByProviderAndServiceAndResource(provider(), serviceCode(), resource.getId())
        .map(ServiceAssignmentEntity::getCandidate)
        .orElse(null);
  }

  // Update resource status (e.g., mark a coupon as USED or DISABLED)
  // Throws NoSuchObjectException if resource not found
  @Override
  @Transactional
  public void updateResourceStatus(String resourceCode, ResourceStatus status) throws NoSuchObjectException {
    ServiceResourceEntity resource = resourceRepository
        .findByProviderAndResourceCode(provider(), resourceCode)
        .orElseThrow(() -> new NoSuchObjectException("Resource with code " + resourceCode + " not found"));
    
    resource.setStatus(status);
    resourceRepository.save(resource);
  }

  // Count available resources for this provider (e.g., count all available Duolingo coupons
  @Override
  public long countAvailableForProvider() {
    return resourceRepository.countAvailableByProvider(provider());
  }

  // Count available resources for this provider and service (e.g., count available Duolingo TEST_PROCTORED coupons)
  @Override
  public long countAvailableForProviderAndService() {
    return resourceRepository.countAvailableByProviderAndService(provider(), serviceCode());
  }
}
