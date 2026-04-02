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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.tctalent.server.casi.domain.model.ServiceProvider;

class ReferenceTaskPolicyTest {

  private final ReferenceTaskPolicy policy = new ReferenceTaskPolicy();

  @Test
  @DisplayName("returns REFERENCE provider")
  void providerReturnsReference() {
    assertEquals(ServiceProvider.REFERENCE, policy.provider());
  }

  @Test
  @DisplayName("returns deterministic task on assignment")
  void tasksOnAssignedReturnsReferenceTask() {
    assertEquals(List.of("referenceVoucherAssigned"), policy.tasksOnAssigned(null));
  }
}
