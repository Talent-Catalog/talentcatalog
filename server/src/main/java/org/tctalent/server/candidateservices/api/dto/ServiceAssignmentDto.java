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

package org.tctalent.server.candidateservices.api.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import org.tctalent.server.candidateservices.domain.model.AssignmentStatus;
import org.tctalent.server.candidateservices.domain.model.ServiceAssignment;
import org.tctalent.server.candidateservices.domain.model.ServiceResource;

@Getter
@Builder
public class ServiceAssignmentDto {
  private final ServiceResourceDto resource; // e.g., coupon code
  private final Long candidateId;
  private final Long actorId; // who assigned it
  private final AssignmentStatus status; // ASSIGNED, REDEEMED, EXPIRED, REASSIGNED
  private final LocalDateTime assignedAt;

  public static ServiceAssignmentDto from(ServiceAssignment a) {
    ServiceResource r = a.getResource();
    return ServiceAssignmentDto.builder()
        .resource(ServiceResourceDto.from(r))
        .candidateId(a.getCandidateId())
        .actorId(a.getActorId())
        .status(a.getStatus())
        .assignedAt(a.getAssignedAt())
        .build();
  }
}
