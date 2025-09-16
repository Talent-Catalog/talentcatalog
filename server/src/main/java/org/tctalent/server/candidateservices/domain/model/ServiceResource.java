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
import org.tctalent.server.candidateservices.infrastructure.persistence.resource.ServiceResourceEntity;

@Value
@Builder
public class ServiceResource {
  Long id;
  String provider;
  ServiceCode serviceCode;
  String resourceCode;
  ResourceStatus status;
  LocalDateTime sentAt;
  LocalDateTime expiresAt;

  public static ServiceResource from(ServiceResourceEntity re) {
    return ServiceResource.builder()
        .id(re.getId())
        .provider(re.getProvider())
        .serviceCode(re.getServiceCode())
        .resourceCode(re.getResourceCode())
        .status(re.getStatus())
        .sentAt(re.getSentAt())
        .expiresAt(re.getExpiresAt())
        .build();
  }
}