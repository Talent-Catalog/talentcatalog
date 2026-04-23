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

package org.tctalent.server.casi.api.dto;

import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import org.tctalent.server.casi.domain.model.ListAction;
import org.tctalent.server.casi.domain.model.ListRole;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.model.ServiceProvider;

/**
 * Data Transfer Object representing a service list association for a saved list.
 */
@Getter
@Builder
public class ServiceListDto {
  private Long id;
  private ServiceProvider provider;
  private ServiceCode serviceCode;
  private ListRole listRole;
  private Set<ListAction> permittedActions;
}
