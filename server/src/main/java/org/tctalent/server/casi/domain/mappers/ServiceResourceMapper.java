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
import org.tctalent.server.casi.api.dto.ServiceResourceDto;
import org.tctalent.server.casi.domain.model.ServiceResource;
import org.tctalent.server.casi.domain.persistence.ServiceResourceEntity;


/**
 * Mapper for converting between ServiceResource, ServiceResourceEntity, and ServiceResourceDto.
 *
 * @author sadatmalik
 */
@Component
public class ServiceResourceMapper {
  public static ServiceResource toModel(ServiceResourceEntity e) {
    if (e == null) {
      return null;
    }

    return ServiceResource.builder()
        .id(e.getId())
        .provider(e.getProvider())
        .serviceCode(e.getServiceCode())
        .resourceCode(e.getResourceCode())
        .status(e.getStatus())
        .sentAt(e.getSentAt())
        .expiresAt(e.getExpiresAt())
        .build();
  }

  public static ServiceResource toModel(ServiceResourceDto dto) {
    if (dto == null) {
      return null;
    }

    return ServiceResource.builder()
        .id(dto.getId())
        .provider(dto.getProvider())
        .serviceCode(dto.getServiceCode())
        .resourceCode(dto.getResourceCode())
        .status(dto.getStatus())
        .sentAt(dto.getSentAt())
        .expiresAt(dto.getExpiresAt())
        .build();
  }

  public static ServiceResourceDto toDto(ServiceResource model) {
    if (model == null) {
      return null;
    }

    return ServiceResourceDto.builder()
        .id(model.getId())
        .provider(model.getProvider())
        .serviceCode(model.getServiceCode())
        .resourceCode(model.getResourceCode())
        .status(model.getStatus())
        .sentAt(model.getSentAt())
        .expiresAt(model.getExpiresAt())
        .build();
  }

}
