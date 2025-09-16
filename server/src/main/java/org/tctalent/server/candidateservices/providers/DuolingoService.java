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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.candidateservices.application.AssignmentEngine;
import org.tctalent.server.candidateservices.application.CandidateService;
import org.tctalent.server.candidateservices.application.ProviderDescriptor;
import org.tctalent.server.candidateservices.application.alloc.ResourceAllocator;
import org.tctalent.server.candidateservices.domain.model.AssignmentStatus;
import org.tctalent.server.candidateservices.domain.model.ResourceStatus;
import org.tctalent.server.candidateservices.domain.model.ServiceAssignment;
import org.tctalent.server.candidateservices.domain.model.ServiceCode;
import org.tctalent.server.candidateservices.domain.model.ServiceResource;
import org.tctalent.server.candidateservices.infrastructure.importers.DuolingoCouponImporter;
import org.tctalent.server.candidateservices.infrastructure.persistence.assignment.ServiceAssignmentEntity;
import org.tctalent.server.candidateservices.infrastructure.persistence.assignment.ServiceAssignmentRepository;
import org.tctalent.server.candidateservices.infrastructure.persistence.resource.ServiceResourceEntity;
import org.tctalent.server.candidateservices.infrastructure.persistence.resource.ServiceResourceRepository;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.ImportFailedException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.User;
import org.tctalent.server.service.db.SavedListService;

@Service
@Slf4j
@RequiredArgsConstructor
public class DuolingoService implements CandidateService {

  private final ServiceAssignmentRepository serviceRepository;
  private final ServiceResourceRepository resourceRepository;
  private final AssignmentEngine assignmentEngine;
  private final ResourceAllocator duolingoAllocator;
  private final DuolingoCouponImporter importer;
  private final SavedListService savedListService;

  private static final ProviderDescriptor PD =
      new ProviderDescriptor("DUOLINGO", ServiceCode.DUOLINGO_TEST_PROCTORED);

  @Override
  @Transactional
  public void importInventory(MultipartFile file, String serviceCode) throws ImportFailedException {
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

    return assignmentEngine.assign(duolingoAllocator, candidateId, user);
  }

  @Override
  @Transactional
  public List<ServiceAssignment> assignToList(Long listId, String serviceCode, User user) {
    SavedList savedList = savedListService.get(listId);
    Set<Candidate> candidates = savedList.getCandidates();

    // Find available resources
    List<ServiceResource> availableResources = getAvailableResources();

    if (availableResources.isEmpty() || candidates.size() > availableResources.size()) {
      throw new NoSuchObjectException(
          "There are not enough available coupons to assign to all candidates in the list. Please import more coupons from the settings page.");
    }

    List<ServiceAssignment> done = new ArrayList<>();
    for (Candidate candidate : candidates) {
      boolean hasSentCoupon = serviceRepository
          .findByCandidateAndProviderAndService(candidate.getId(), PD.provider(), serviceCode)
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
    List<ServiceAssignmentEntity> assignments = serviceRepository
        .findByCandidateAndProviderAndService(candidateId, PD.provider(), serviceCode);
    List<ServiceAssignment> models = new ArrayList<>();
    for (ServiceAssignmentEntity a : assignments) {
      models.add(ServiceAssignment.from(a));
    }
    return models;
  }

  @Override
  @Transactional(readOnly = true)
  public List<ServiceResource> getAvailableResources() {
    return resourceRepository
        .findByProviderAndServiceCodeAndStatus(PD.provider(), PD.serviceCode(), ResourceStatus.AVAILABLE)
        .stream()
        .map(ServiceResource::from)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public ServiceResource getResourceForResourceCode(String resourceCode) throws NoSuchObjectException {
    return resourceRepository
        .findByProviderAndResourceCode(PD.provider(), resourceCode)
        .map(ServiceResource::from)
        .orElseThrow(() -> new NoSuchObjectException("Coupon with code " + resourceCode + " not found"));
  }

  @Override
  public Candidate getCandidateForResourceCode(String resourceCode) throws NoSuchObjectException{
    return serviceRepository
        .findTopByProviderAndServiceAndResource(PD.provider(), PD.serviceCode().name(), resourceCode)
        .map(ServiceAssignmentEntity::getCandidate)
        .orElse(null);
  }

  @Override
  @Transactional
  public void updateAResourceStatus(String resourceCode, ResourceStatus status) {
    resourceRepository
        .findByProviderAndResourceCode(PD.provider(), resourceCode)
        .ifPresent(resource -> {
          resource.setStatus(status);
          resourceRepository.save(resource);
        });
  }

  @Override
  public long countAvailableForProvider() {
    return resourceRepository.countAvailableByProvider(PD.provider());
  }

  @Override
  public long countAvailableForProviderAndService() {
    return resourceRepository.countAvailableByProviderAndService(PD.provider(), PD.serviceCode());
  }

  //  @Override // TODO -- SM -- put in base class or resource expiry scheduler class?
  @Transactional
  @Scheduled(cron = "0 0 0 * * ?", zone = "GMT")
  @SchedulerLock(name = "ResourceSchedulerTask_markResourcesAsExpired", lockAtLeastFor = "PT23H", lockAtMostFor = "PT23H")
  public void markResourcesAsExpired() {
    // Exclude EXPIRED, REDEEMED and DISABLED statuses
    List<ServiceResourceEntity> expiredCoupons = resourceRepository
        .findExpirable(
            LocalDateTime.now(),
            List.of(ResourceStatus.EXPIRED, ResourceStatus.REDEEMED, ResourceStatus.DISABLED)
    );

    if (!expiredCoupons.isEmpty()) {
      expiredCoupons.forEach(resource -> resource.setStatus(ResourceStatus.EXPIRED));
      resourceRepository.saveAll(expiredCoupons);
    }
  }

}
