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

package org.tctalent.server.integration.helper;

import java.io.IOException;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Abstract base class for integration tests that interact with the `tctalent` database.
 * It initializes a shared PostgreSQL container to be reused across all test classes,
 * ensuring consistent database state and improved performance.
 * To use, simply extend this class—no additional setup is required in your test classes.
 */
@Testcontainers
@ActiveProfiles("test")
public abstract class BaseDBIntegrationTest {


  /**
   * Initializes the PostgreSQL container before executing any test cases.
   */
  @BeforeAll
  public static void startDBContainer() throws IOException, InterruptedException {
    PostgresTestContainer.startContainer();
  }

  /**
   * Supplies dynamic configuration properties from the container to the Spring context.
   *
   * @param registry The property registry used to inject container-specific database properties.
   */
  @DynamicPropertySource
  public static void registerDBContainer(DynamicPropertyRegistry registry) {
    PostgresTestContainer.injectContainerProperties(registry);
  }

  /**
   * Verifies whether the database container has been successfully started.
   * Typically useful to assert the container state prior to running test logic.
   *
   * @return true if the container is active; false otherwise.
   */
  public boolean isContainerInitialised() {
    return PostgresTestContainer.container.isRunning();
  }
}