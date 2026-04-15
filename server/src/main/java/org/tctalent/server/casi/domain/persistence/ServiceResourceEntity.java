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

package org.tctalent.server.casi.domain.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;
import org.tctalent.server.casi.domain.model.ResourceStatus;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.casi.domain.model.ResourceType;
import org.tctalent.server.model.db.AbstractDomainObject;

/**
 * Entity representing a service resource.
 *
 * @author sadatmalik
 */
@Entity
@Table(name = "service_resource")
@SequenceGenerator(name = "seq_gen", sequenceName = "service_resource_id_seq", allocationSize = 1)
@Getter
@Setter
public class ServiceResourceEntity extends AbstractDomainObject<Long> {

  @Enumerated(EnumType.STRING)
  @Column(nullable=false)
  private ServiceProvider provider; // e.g. "DUOLINGO"

  @Enumerated(EnumType.STRING)
  @Column(nullable=false)
  private ServiceCode serviceCode; // e.g. "TEST_PROCTORED"

  private String resourceCode; // coupon code

  @Column(name = "country_iso_code")
  private String countryIsoCode; // ISO 3166-1 alpha-2 code (e.g. "PK")

  @Enumerated(EnumType.STRING)
  @Column(name = "resource_type", nullable = false)
  private ResourceType resourceType = ResourceType.UNIQUE;

  @Enumerated(EnumType.STRING)
  @Column(nullable=false)
  private ResourceStatus status; // e.g. AVAILABLE/ASSIGNED/REDEEMED/EXPIRED

  private OffsetDateTime expiresAt;

  private OffsetDateTime sentAt;

  @Column(nullable = false, updatable = false)
  private OffsetDateTime createdAt = OffsetDateTime.now();
}

