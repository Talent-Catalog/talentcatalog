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

package org.tctalent.server.casi.application.policy;

import java.util.List;
import org.springframework.stereotype.Component;
import org.tctalent.server.casi.domain.events.ServiceAssignedEvent;
import org.tctalent.server.casi.domain.model.ServiceProvider;

/**
 * Minimal task policy for the reference provider.
 * Uses a simple deterministic task to exercise lifecycle hooks.
 *
 * @author sadatmalik
 */
@Component
public class ReferenceTaskPolicy implements TaskPolicy {

  @Override
  public ServiceProvider provider() {
    return ServiceProvider.REFERENCE;
  }

  @Override
  public List<String> tasksOnAssigned(ServiceAssignedEvent e) {
    return List.of("referenceVoucherAssigned");
  }
}
