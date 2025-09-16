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

package org.tctalent.server.candidateservices.domain.model;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;
import org.tctalent.server.candidateservices.infrastructure.persistence.assignment.ServiceAssignmentEntity;
import org.tctalent.server.candidateservices.infrastructure.persistence.resource.ServiceResourceEntity;

@Value
@Builder
public class ServiceAssignment {
  String provider;
  String serviceCode;
  ServiceResource resource; // e.g., coupon code
  Long candidateId;
  Long actorId; // who assigned it
  AssignmentStatus status; // ASSIGNED, REDEEMED, EXPIRED, REASSIGNED
  LocalDateTime assignedAt;

  public static ServiceAssignment from(ServiceAssignmentEntity e) {
    ServiceResourceEntity r = e.getResource();
    return ServiceAssignment.builder()
        .provider(e.getProvider())
        .serviceCode(e.getServiceCode().name())
        .resource(ServiceResource.from(r))
        .candidateId(e.getCandidate().getId())
        .actorId(e.getActor().getId())
        .status(e.getStatus())
        .assignedAt(e.getAssignedAt())
        .build();
  }

}
