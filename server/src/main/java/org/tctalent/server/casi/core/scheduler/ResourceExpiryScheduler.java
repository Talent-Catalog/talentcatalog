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

package org.tctalent.server.casi.core.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.casi.domain.events.ServiceExpiredEvent;
import org.tctalent.server.casi.domain.mappers.ServiceAssignmentMapper;
import org.tctalent.server.casi.domain.model.ResourceStatus;
import org.tctalent.server.casi.domain.persistence.ServiceAssignmentRepository;
import org.tctalent.server.casi.domain.persistence.ServiceResourceEntity;
import org.tctalent.server.casi.domain.persistence.ServiceResourceRepository;


/**
 * Scheduler that marks service resources as EXPIRED when their expiration date is reached.
 * It runs daily at midnight GMT and processes resources that are not already in EXPIRED, REDEEMED,
 * or DISABLED status.
 * When a resource is marked as EXPIRED, an event is published to notify other parts of the system.
 * <p>
 * Note: The scheduler uses ShedLock so that only one instance runs in a distributed environment.
 *
 * @author sadatmalik
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ResourceExpiryScheduler {

  private final ServiceResourceRepository resources;
  private final ServiceAssignmentRepository assignments;
  private final ApplicationEventPublisher events;

  // Exclude EXPIRED, REDEEMED and DISABLED statuses
  private static final List<ResourceStatus> EXCLUDE =
      List.of(ResourceStatus.EXPIRED, ResourceStatus.REDEEMED, ResourceStatus.DISABLED);

  @Transactional
  @Scheduled(cron = "0 0 0 * * ?", zone = "GMT")
  @SchedulerLock(name = "ServiceResource_Expire", lockAtLeastFor = "PT23H", lockAtMostFor = "PT23H")
  public void markResourcesAsExpired() {
    List<ServiceResourceEntity> expirable = resources
        .findExpirable(
            LocalDateTime.now(), EXCLUDE
        );

    if (expirable.isEmpty()) {
      return;
    }

    expirable.forEach(resource -> {
      resource.setStatus(ResourceStatus.EXPIRED);

      assignments
          .findTopByProviderAndServiceAndResource(
              resource.getProvider(),
              resource.getServiceCode(),
              resource.getId())
          .ifPresent(assignment ->
              events.publishEvent(new ServiceExpiredEvent(ServiceAssignmentMapper.toModel(assignment))));

    });
    resources.saveAll(expirable);
  }

}
