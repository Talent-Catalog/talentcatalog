/*
 * Copyright (c) 2026 Talent Catalog.
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

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.tctalent.server.casi.domain.model.ListAction;
import org.tctalent.server.casi.domain.model.ListRole;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.model.db.AbstractDomainObject;
import org.tctalent.server.model.db.SavedList;

/**
 * Links a {@link SavedList} to a candidate assistance service, declaring its role within that
 * service and the admin actions permitted on its candidates.
 * <p>
 * Records are created at application startup by {@link org.tctalent.server.casi.core.services.ServiceListSetupService}
 * based on each service's {@code serviceListSpecs()} declaration.
 */
@Entity
@Table(name = "service_list")
@SequenceGenerator(name = "seq_gen", sequenceName = "service_list_id_seq", allocationSize = 1)
@Getter
@Setter
public class ServiceListEntity extends AbstractDomainObject<Long> {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "saved_list_id", nullable = false, unique = true)
  private SavedList savedList;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ServiceProvider provider;

  @Enumerated(EnumType.STRING)
  @Column(name = "service_code", nullable = false)
  private ServiceCode serviceCode;

  @Enumerated(EnumType.STRING)
  @Column(name = "list_role", nullable = false)
  private ListRole listRole;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
      name = "service_list_permitted_actions",
      joinColumns = @JoinColumn(name = "service_list_id")
  )
  @Column(name = "action")
  @Enumerated(EnumType.STRING)
  private Set<ListAction> permittedActions = new HashSet<>();

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();
}
