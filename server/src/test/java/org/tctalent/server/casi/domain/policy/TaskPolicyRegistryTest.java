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

package org.tctalent.server.casi.domain.policy;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.tctalent.server.casi.application.policy.DuolingoTaskPolicy;
import org.tctalent.server.casi.application.policy.TaskPolicyRegistry;
import org.tctalent.server.casi.domain.model.ServiceProvider;


@SpringJUnitConfig(classes = TaskPolicyRegistryTest.TestConfig.class) // load minimal context
class TaskPolicyRegistryTest {

  @Configuration
  @ComponentScan(basePackageClasses = {
      TaskPolicyRegistry.class,    // the registry @Component
      DuolingoTaskPolicy.class     // the policy @Component (add others as needed)
  })
  static class TestConfig {
    // no datasource, no flyway, or the test will fail in the GitHub CI action
  }

  @Autowired
  TaskPolicyRegistry registry;

  @Test
  void loadsDuolingo() {
    assertNotNull(registry.forProvider(ServiceProvider.DUOLINGO));
  }
}
