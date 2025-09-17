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
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.candidateservices.application.AbstractCandidateService;
import org.tctalent.server.candidateservices.application.AssignmentEngine;
import org.tctalent.server.candidateservices.application.ProviderDescriptor;
import org.tctalent.server.candidateservices.application.alloc.ResourceAllocator;
import org.tctalent.server.candidateservices.domain.model.ResourceStatus;
import org.tctalent.server.candidateservices.domain.model.ServiceCode;
import org.tctalent.server.candidateservices.domain.model.ServiceResource;
import org.tctalent.server.candidateservices.infrastructure.importers.FileInventoryImporter;
import org.tctalent.server.candidateservices.infrastructure.persistence.assignment.ServiceAssignmentEntity;
import org.tctalent.server.candidateservices.infrastructure.persistence.assignment.ServiceAssignmentRepository;
import org.tctalent.server.candidateservices.infrastructure.persistence.resource.ServiceResourceEntity;
import org.tctalent.server.candidateservices.infrastructure.persistence.resource.ServiceResourceRepository;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.service.db.SavedListService;


@Service
@Slf4j
public class DuolingoService extends AbstractCandidateService {

  private final FileInventoryImporter duolingoImporter;
  private final ResourceAllocator duolingoAllocator;

  private static final ProviderDescriptor PD =
      new ProviderDescriptor("DUOLINGO", ServiceCode.DUOLINGO_TEST_PROCTORED); // TODO -- SM -- maybe don't need this

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
    return ""; // TODO
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
