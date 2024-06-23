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

package org.tctalent.server.repository.db.integrationhelp

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.test.context.DynamicPropertyRegistry
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.utility.MountableFile
import org.testcontainers.utility.TestcontainersConfiguration

private val logger = KotlinLogging.logger {}

/**
 * Singleton that creates a Testcontainer instance with the tctalent Postgres database dump file
 * loaded. It must be initialized statically and run beforeAll to ensure the Flyway scripts execute
 * against it when Spring launches. This setup occurs before Spring takes over, ensuring the
 * database is created and users are set up correctly so the dump loads as expected.
 *
 * ## Requirements:
 * - Refer to the <a href="./resources/testcontainers.properties">testcontainers.properties</a>
 *   file.
 * - Set the system environment variable *TCTALENT_DB_HOME* to specify the location of the dump
 *   file.
 * - Ensure `testcontainers.properties` is in the classpath with the required variables set.
 *
 * The first parameter is the name of the dump file (must start with a slash). The second parameter
 * is the required mount point in the Docker image, with a default value provided.
 *
 * Configuration functions are separated, so the container setup is streamlined and focused solely
 * on the container configuration.
 */
object DBContainer {
  private const val DB_NAME = "tctalent"
  private const val DB_USER = "tctalent"
  private const val DB_PWD = "tctalent"
  const val DEFAULT_DUMP = "/integration-test-dump.sql"
  const val TEST_CONTAINERS_DUMP = "testcontainers.dump.location"
  const val TEST_CONTAINERS_MOUNT = "testcontainers.container.mount"

  @Container
  val db: PostgreSQLContainer<*> =
    PostgreSQLContainer(pgContainerImage())
      .also {
        logger.info { "Creating the postgres container." }
        logger.info { "Dump file path: ${dumpFilePath()}" }
        logger.info { "Container mount path: ${containerMountPath()}" }
      }
      .apply {
        withDatabaseName(DB_NAME)
        withUsername(DB_USER)
        withPassword(DB_PWD)
        withReuse(true)
      }

  fun startDBContainer() {
    db.apply { start() }
    createDbObjects()
    loadDb()
    importDumpFileToDatabase()
  }

  private fun importDumpFileToDatabase() {
    logger.info { "Importing the database dump." }
    db.apply { execInContainer(*psqlCommand()) }
    logger.info { "Done importing the database dump. Connection is: ${db.jdbcUrl}" }
    logger.info { "Ready to use: ${db.isRunning()}" }
  }

  private fun createDbObjects() {
    logger.info { "Creating database objects" }
    db.apply {
      execInContainer(*createDbCommands())
      execInContainer(*createUserCommands())
      execInContainer(*superUserCommand())
    }
    logger.info { "Database container started. DB and user created." }
  }

  private fun loadDb() {
    logger.info { "Copying dump file to the container." }
    db.apply {
      copyFileToContainer(MountableFile.forHostPath(dumpFilePath()), containerMountPath())
    }
    logger.info { "Dump file copied to the database" }
  }

  fun registerDBContainer(registry: DynamicPropertyRegistry) {
    registry.add("spring.datasource.url", db::getJdbcUrl)
    registry.add("spring.datasource.username", db::getUsername)
    registry.add("spring.datasource.password", db::getPassword)
  }
}

private fun pgContainerImage() = "postgres:14"

private fun createDbCommands() =
  arrayOf("psql", "-U", "postgres", "-c", "CREATE DATABASE tctalent;")

private fun createUserCommands() =
  arrayOf("psql", "-U", "postgres", "-c", "CREATE USER tctalent WITH PASSWORD 'tctalent';")

private fun superUserCommand() =
  arrayOf("psql", "-U", "postgres", "-c", "ALTER USER tctalent WITH SUPERUSER;")

private fun dumpFilePath() = buildString {
  val tcContainerConfig = TestcontainersConfiguration.getInstance()
  val dumpLocation =
    tcContainerConfig.getEnvVarOrProperty(
      DBContainer.TEST_CONTAINERS_DUMP,
      DBContainer.DEFAULT_DUMP,
    )
  append(dumpLocation)
}

private fun containerMountPath() = buildString {
  val tcContainerConfig = TestcontainersConfiguration.getInstance()
  val mountPoint =
    tcContainerConfig.getEnvVarOrProperty(
      DBContainer.TEST_CONTAINERS_MOUNT,
      "ERROR: Container mount point is not set.",
    )
  append(mountPoint)
}

private fun psqlCommand() =
  arrayOf("psql", "-d", "tctalent", "-U", "tctalent", "-f", containerMountPath())
