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

package org.tctalent.server.casi.domain.mappers;

import org.springframework.stereotype.Component;
import org.tctalent.server.casi.api.dto.ServiceAssignmentDto;
import org.tctalent.server.casi.api.dto.ServiceResourceDto;
import org.tctalent.server.casi.domain.model.ServiceAssignment;
import org.tctalent.server.casi.domain.model.ServiceResource;
import org.tctalent.server.casi.domain.persistence.ServiceAssignmentEntity;
import org.tctalent.server.casi.domain.persistence.ServiceResourceEntity;


@Component
public class ServiceAssignmentMapper {
  public static ServiceAssignment toModel(ServiceAssignmentEntity e) {
    if (e == null) {
      return null;
    }

    ServiceResourceEntity re = e.getResource();

    return ServiceAssignment.builder()
        .id(e.getId())
        .provider(e.getProvider())
        .serviceCode(e.getServiceCode())
        .resource(ServiceResourceMapper.toModel(re))
        .candidateId(e.getCandidate().getId())
        .actorId(e.getActor().getId())
        .status(e.getStatus())
        .assignedAt(e.getAssignedAt())
        .build();
  }

  public static ServiceAssignment toModel(ServiceAssignmentDto dto) {
    if (dto == null) {
      return null;
    }

    ServiceResourceDto rd = dto.getResource();

    return ServiceAssignment.builder()
        .id(dto.getId())
        .provider(dto.getProvider())
        .serviceCode(dto.getServiceCode())
        .resource(ServiceResourceMapper.toModel(rd))
        .candidateId(dto.getCandidateId())
        .actorId(dto.getActorId())
        .status(dto.getStatus())
        .assignedAt(dto.getAssignedAt())
        .build();
  }

  public static ServiceAssignmentDto toDto(ServiceAssignment model) {
    if (model == null) {
      return null;
    }

    ServiceResource r = model.getResource();

    return ServiceAssignmentDto.builder()
        .id(model.getId())
        .provider(model.getProvider())
        .serviceCode(model.getServiceCode())
        .resource(ServiceResourceMapper.toDto(r))
        .candidateId(model.getCandidateId())
        .actorId(model.getActorId())
        .status(model.getStatus())
        .assignedAt(model.getAssignedAt())
        .build();
  }

}
