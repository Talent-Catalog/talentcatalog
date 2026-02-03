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

package org.tctalent.server.casi.application.policy;

import java.util.List;
import org.tctalent.server.casi.domain.events.ServiceAssignedEvent;
import org.tctalent.server.casi.domain.events.ServiceExpiredEvent;
import org.tctalent.server.casi.domain.events.ServiceReassignedEvent;
import org.tctalent.server.casi.domain.events.ServiceRedeemedEvent;
import org.tctalent.server.casi.domain.model.ServiceProvider;

/**
 * Task policy interface defining tasks to be executed on various service events.
 * Implementations should specify the provider they handle and the tasks for each event.
 *
 * @author sadatmalik
 */
public interface TaskPolicy {

  // Unique provider identifier
  ServiceProvider provider();

  // Tasks to execute when a service is assigned
  List<String> tasksOnAssigned(ServiceAssignedEvent e);

  // Tasks to execute when a service is redeemed
  default List<String> tasksOnRedeemed(ServiceRedeemedEvent e) { return List.of(); }

  // Tasks to close when a service is reassigned
  default List<String> tasksOnReassigned(ServiceReassignedEvent e) { return List.of(); }

  // Tasks to close when a service expires
  default List<String> tasksOnExpired(ServiceExpiredEvent e) { return List.of(); }

}
