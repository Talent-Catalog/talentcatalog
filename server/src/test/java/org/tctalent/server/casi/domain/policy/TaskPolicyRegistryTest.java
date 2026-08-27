/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.casi.domain.policy;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.tctalent.server.casi.application.policy.DuolingoTaskPolicy;
import org.tctalent.server.casi.application.policy.LinkedInTaskPolicy;
import org.tctalent.server.casi.application.policy.ReferenceTaskPolicy;
import org.tctalent.server.casi.application.policy.TaskPolicyRegistry;
import org.tctalent.server.casi.domain.model.ServiceProvider;


@SpringJUnitConfig(classes = TaskPolicyRegistryTest.TestConfig.class) // load minimal context
class TaskPolicyRegistryTest {

  /**
   * Scan only TaskPolicyRegistry and concrete TaskPolicy beans. Avoid scanning
   * EligibilityPolicy components (e.g. PifiEligibilityPolicy), which pull in
   * unrelated dependencies, and skip NoOpTaskPolicy (not a Spring bean).
   */
  @Configuration
  @ComponentScan(
      basePackageClasses = TaskPolicyRegistry.class,
      useDefaultFilters = false,
      includeFilters = @ComponentScan.Filter(
          type = FilterType.ASSIGNABLE_TYPE,
          classes = {
              TaskPolicyRegistry.class,
              DuolingoTaskPolicy.class,
              LinkedInTaskPolicy.class,
              ReferenceTaskPolicy.class
          }
      )
  )
  static class TestConfig {
  }

  @Autowired
  TaskPolicyRegistry registry;

  @Test
  void loadsDuolingo() {
    assertNotNull(registry.forProvider(ServiceProvider.DUOLINGO));
  }

  @Test
  void returnsNoOpForUnregisteredProvider() {
    var policy = registry.forProvider(ServiceProvider.UNHCR);
    assertNotNull(policy);
    assertTrue(policy.tasksOnAssigned(null).isEmpty());
    assertTrue(policy.tasksOnRedeemed(null).isEmpty());
    assertTrue(policy.tasksOnReassigned(null).isEmpty());
    assertTrue(policy.tasksOnExpired(null).isEmpty());
  }
}
