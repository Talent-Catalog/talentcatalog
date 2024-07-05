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
import lombok.extern.slf4j.Slf4j;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.MountableFile;
import org.testcontainers.utility.TestcontainersConfiguration;

/**
 * Singleton that creates a Testcontainer instance with the tctalent Postgres database dump file
 * loaded. It must be initialized statically and run beforeAll to ensure the Flyway scripts execute
 * against it when Spring launches. This setup occurs before Spring takes over, ensuring the
 * database is created and users are set up correctly so the dump loads as expected. ##
 * Requirements: - Refer to the <a
 * href="./resources/testcontainers.properties">testcontainers.properties</a> file. - Set the system
 * environment variable *TCTALENT_DB_HOME* to specify the location of the dump file. - Ensure
 * `testcontainers.properties` is in the classpath with the required variables set.
 * <p>
 * The first parameter is the name of the dump file (must start with a slash). The second parameter
 * is the required mount point in the Docker image, with a default value provided. Configuration
 * functions are separated, so the container setup is streamlined and focused solely on the
 * container configuration.
 */
@Slf4j
public class DBContainer {

  private static final String DB_NAME = "tctalent";
  private static final String DB_USER = "tctalent";
  private static final String DB_PWD = "tctalent";
  public static final String DEFAULT_DUMP = "/integration-test-dump.sql";
  public static final String TEST_CONTAINERS_DUMP = "testcontainers.dump.location";
  public static final String TEST_CONTAINERS_MOUNT = "testcontainers.container.mount";

  @Container
  public static final PostgreSQLContainer<?> db = new PostgreSQLContainer<>(pgContainerImage())
      .withDatabaseName(DB_NAME)
      .withUsername(DB_USER)
      .withPassword(DB_PWD)
      .withReuse(true);

  static {
    log.info("Creating the postgres container.");
    log.info("Dump file path: " + dumpFilePath());
    log.info("Container mount path: " + containerMountPath());
  }

  /**
   * Starts the database container and initializes the database.
   */
  public static void startDBContainer() {
    db.start();
    createDbObjects();
    loadDb();
    importDumpFileToDatabase();
  }

  /**
   * Imports the database dump file to the database container.
   */
  private static void importDumpFileToDatabase() {
    log.info("Importing the database dump.");
    try {
      db.execInContainer(psqlCommand());
    } catch (IOException | InterruptedException e) {
      log.error("Could not import the database dump file.{}", e.getMessage());
    }
    log.info("Done importing the database dump. Connection is: {}", db.getJdbcUrl());
    log.info("Ready to use: {}", db.isRunning());
  }

  /**
   * Creates database objects and users in the database container.
   */
  private static void createDbObjects() {
    log.info("Creating database objects");
    try {
      db.execInContainer(createDbCommands());
      db.execInContainer(createUserCommands());
      db.execInContainer(superUserCommand());
    } catch (InterruptedException | IOException e) {
      log.error("Error creating database objects", e);
    }
    log.info("Database container started. DB and user created.");
  }

  /**
   * Copies the database dump file to the database container.
   */
  private static void loadDb() {
    log.info("Copying dump file to the container.");
    db.copyFileToContainer(MountableFile.forHostPath(dumpFilePath()), containerMountPath());
    log.info("Dump file copied to the database");
  }

  /**
   * Registers the database container properties with the Spring context.
   *
   * @param registry The DynamicPropertyRegistry to register the database container with.
   */
  public static void registerDBContainer(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", db::getJdbcUrl);
    registry.add("spring.datasource.username", db::getUsername);
    registry.add("spring.datasource.password", db::getPassword);
  }

  /**
   * Retrieves the PostgreSQL container image.
   *
   * @return The PostgreSQL container image.
   */
  private static String pgContainerImage() {
    return "postgres:14";
  }

  /**
   * Retrieves the commands to create the database.
   *
   * @return An array of commands to create the database.
   */
  private static String[] createDbCommands() {
    return new String[]{"psql", "-U", "postgres", "-c", "CREATE DATABASE tctalent;"};
  }

  /**
   * Retrieves the commands to create the database user.
   *
   * @return An array of commands to create the database user.
   */
  private static String[] createUserCommands() {
    return new String[]{"psql", "-U", "postgres", "-c",
        "CREATE USER tctalent WITH PASSWORD 'tctalent';"};
  }

  /**
   * Retrieves the command to grant superuser privileges to the database user.
   *
   * @return An array of commands to grant superuser privileges to the database user.
   */
  private static String[] superUserCommand() {
    return new String[]{"psql", "-U", "postgres", "-c", "ALTER USER tctalent WITH SUPERUSER;"};
  }

  /**
   * Retrieves the path to the dump file.
   *
   * @return The path to the dump file.
   */
  private static String dumpFilePath() {
    TestcontainersConfiguration tcContainerConfig = TestcontainersConfiguration.getInstance();
    return tcContainerConfig.getEnvVarOrProperty(TEST_CONTAINERS_DUMP, DEFAULT_DUMP);
  }

  /**
   * Retrieves the mount path in the container.
   *
   * @return The mount path in the container.
   */
  private static String containerMountPath() {
    TestcontainersConfiguration tcContainerConfig = TestcontainersConfiguration.getInstance();
    return tcContainerConfig.getEnvVarOrProperty(TEST_CONTAINERS_MOUNT,
        "ERROR: Container mount point is not set.");
  }

  /**
   * Retrieves the command to import the database dump file.
   *
   * @return An array of commands to import the database dump file.
   */
  private static String[] psqlCommand() {
    return new String[]{"psql", "-d", "tctalent", "-U", "tctalent", "-f", containerMountPath()};
  }
}
