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

package org.tctalent.server.candidateservices.application.policy;

import java.util.List;
import org.springframework.stereotype.Component;
import org.tctalent.server.candidateservices.domain.events.ServiceAssignedEvent;
import org.tctalent.server.candidateservices.domain.events.ServiceReassignedEvent;
import org.tctalent.server.candidateservices.domain.events.ServiceRedeemedEvent;

@Component
public class DuolingoTaskPolicy implements TaskPolicy {

  @Override
  public String provider() {
    return "DUOLINGO"; // TODO -- SM -- use enum or constant
  }

  @Override
  public List<String> tasksOnAssigned(ServiceAssignedEvent e) {
    return List.of("claimCouponButton");
  }

  @Override
  public List<String> tasksOnRedeemed(ServiceRedeemedEvent e) {
    return List.of("duolingoTest");
  }

  @Override
  public List<String> tasksOnReassigned(ServiceReassignedEvent e) {
    return List.of("duolingoTest");
  }

}
