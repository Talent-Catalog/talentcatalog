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

object DBContainer {
  private val tcContainerConfig = TestcontainersConfiguration.getInstance()
  private val createDb = arrayOf("psql", "-U", "postgres", "-c", "CREATE DATABASE tctalent;")
  private val createUserCommand =
    arrayOf("psql", "-U", "postgres", "-c", "CREATE USER tctalent WITH PASSWORD 'tctalent';")

  private val superUserCommand =
    arrayOf("psql", "-U", "postgres", "-c", "ALTER USER tctalent WITH SUPERUSER;")

  private val dumpFilePath =
    tcContainerConfig.environment["DB_HOME"] +
      tcContainerConfig.getEnvVarOrProperty(
        "testcontainers.dump.location",
        "ERROR: Dump file location is not set.",
      )

  private val containerMountPath =
    tcContainerConfig.getEnvVarOrProperty(
      "testcontainers.container.mount",
      "ERROR: Container mount point is not set.",
    )

  private val psqlCommand =
    arrayOf("psql", "-d", "tctalent", "-U", "tctalent", "-f", containerMountPath)

  @Container
  val db: PostgreSQLContainer<*> =
    PostgreSQLContainer("postgres:14")
      .also {
        logger.info { "Creating the postgres container." }
        logger.info { "Dump file path: $dumpFilePath" }
        logger.info { "Container mount path: $containerMountPath" }
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
    db.apply { execInContainer(*psqlCommand) }
    logger.info { "Done importing the database dump." }
  }

  private fun createDbObjects() {
    logger.info { "Creating database objects" }
    db.apply {
      execInContainer(*createDb)
      execInContainer(*createUserCommand)
      execInContainer(*superUserCommand)
    }
    logger.info { "Database container started. DB and user created." }
  }

  private fun loadDb() {
    logger.info { "Copying dump file to the container." }
    db.apply { copyFileToContainer(MountableFile.forHostPath(dumpFilePath), containerMountPath) }
    logger.info { "Dump file copied to the database" }
  }

  fun registerDBContainer(registry: DynamicPropertyRegistry) {
    registry.add("spring.datasource.url", db::getJdbcUrl)
    registry.add("spring.datasource.username", db::getUsername)
    registry.add("spring.datasource.password", db::getPassword)
  }
}
