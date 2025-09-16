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

package org.tctalent.server.candidateservices.infrastructure.persistence.assignment;

import org.springframework.stereotype.Component;
import org.tctalent.server.candidateservices.domain.model.ServiceAssignment;
import org.tctalent.server.candidateservices.infrastructure.persistence.resource.ServiceResourceMapper;

@Component
public class ServiceAssignmentMapper {
  public static ServiceAssignment toDomain(ServiceAssignmentEntity e) {
    if (e == null) {
      return null;
    }

    return ServiceAssignment.builder()
        .resource(ServiceResourceMapper.toDomain(e.getResource()))
        .candidateId(e.getCandidate().getId())
        .actorId(e.getActor().getId())
        .status(e.getStatus())
        .assignedAt(e.getAssignedAt())
        .build();
  }
}
