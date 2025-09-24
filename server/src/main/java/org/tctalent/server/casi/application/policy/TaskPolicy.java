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

public interface TaskPolicy {

  String provider();

  List<String> tasksOnAssigned(ServiceAssignedEvent e);

  default List<String> tasksOnRedeemed(ServiceRedeemedEvent e) { return List.of(); }

  default List<String> tasksOnReassigned(ServiceReassignedEvent e) { return List.of(); }

  default void handleOnExpired(ServiceExpiredEvent e) {}

}
