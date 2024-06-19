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
 * Singleton that creates a Testcontainer instance with the tctalent postgres database dump file
 * loaded. It must be done statically and run beforeAll so the flyway scripts will execute against
 * it when spring launches, so all this happens before spring takes over. It also ensures the
 * database is created and users created so the dump loads correctly.
 *
 * ## Requirements:
 * - Must have a system environment variable *TCTALENT_DB_HOME* specifying the location of the dump
 *   file.
 * - Must have testcontainers.properties in the classpath with the two variables set.
 *
 * The first param is the name of the dump file (lead with a slash). The second is the required
 * mount point in the docker image.A default for the second item is provided.
 *
 * Configuration functions are separated so the container is simply the container.
 */
object DBContainer {

  @Container
  val db: PostgreSQLContainer<*> =
    PostgreSQLContainer(pgContainerImage())
      .also {
        logger.info { "Creating the postgres container." }
        logger.info { "Dump file path: ${dumpFilePath()}" }
        logger.info { "Container mount path: ${containerMountPath()}" }
      }
      .apply {
        withDatabaseName("tctalent")
        withUsername("tctalent")
        withPassword("tctalent")
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
    logger.info { "Done importing the database dump." }
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

// This looks a bit funky. It's getting a path to the dump file based on
// an environment variable and the test container classpath variable.
private fun dumpFilePath() = buildString {
  val tcContainerConfig = TestcontainersConfiguration.getInstance()

  val home = tcContainerConfig.environment["TCTALENT_DB_HOME"] ?: "ERROR DB_HOME not set"
  val dumpLocation =
    tcContainerConfig.getEnvVarOrProperty(
      "testcontainers.dump.location",
      "/docker-entrypoint-initdb.d/dump.sql",
    )
  append(home)
  append(dumpLocation)
}

private fun containerMountPath() = buildString {
  val tcContainerConfig = TestcontainersConfiguration.getInstance()

  val mountPoint =
    tcContainerConfig.getEnvVarOrProperty(
      "testcontainers.container.mount",
      "ERROR: Container mount point is not set.",
    )
  append(mountPoint)
}

private fun psqlCommand() =
  arrayOf("psql", "-d", "tctalent", "-U", "tctalent", "-f", containerMountPath())
