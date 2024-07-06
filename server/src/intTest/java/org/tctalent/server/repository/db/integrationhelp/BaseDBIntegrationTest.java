/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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

package org.tctalent.server.repository.db.integrationhelp;

import java.io.IOException;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for integration testing when using the tctalent database. It provides a singleton
 * instance of the database to each subclass to execute test cases against. Subclasses do not need
 * to call anything, simply extend from this class.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public abstract class BaseDBIntegrationTest {


  /**
   * Starts the database container before all tests.
   */
  @BeforeAll
  public static void startDBContainer() throws IOException, InterruptedException {
    DBContainer.startDBContainer();
  }

  /**
   * Registers the database container with the DynamicPropertyRegistry.
   *
   * @param registry The DynamicPropertyRegistry to register the database container with.
   */
  @DynamicPropertySource
  public static void registerDBContainer(DynamicPropertyRegistry registry) {
    DBContainer.registerDBContainer(registry);
  }

  /**
   * Checks if the database container is running. Useful to call before each test.
   *
   * @return true if the database container is running, false otherwise.
   */
  public boolean isContainerInitialised() {
    return DBContainer.db.isRunning();
  }
}
