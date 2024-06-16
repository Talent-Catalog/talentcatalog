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

package org.tctalent.server.repository.db

import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.PostgreSQLContainer

private val logger = KotlinLogging.logger {}

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ExtendWith(SpringExtension::class)
@TestConfiguration
@ActiveProfiles("test")
abstract class BaseDBIntegrationTest {

  companion object {
    private val dumpFilePath: String =
      System.getenv("TC_DB_DUMP_FILE_PATH") ?: "FAIL: ENV VARIABLE not set"
    private val containerMountPath: String =
      System.getenv("TC_CONTAINER_MOUNT_PATH") ?: "FAIL: ENV VARIABLE not set"

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
        }

    @BeforeAll
    @JvmStatic
    fun startDBContainer() {
      db.start()
    }

    @AfterAll
    @JvmStatic
    fun stopDBContainer() {
      db.stop()
    }

    @DynamicPropertySource
    @JvmStatic
    fun registerDBContainer(registry: DynamicPropertyRegistry) {
      registry.add("spring.datasource.url", db::getJdbcUrl)
      registry.add("spring.datasource.username", db::getUsername)
      registry.add("spring.datasource.password", db::getPassword)
    }
  }

  fun isContainerInitialized(): Boolean = db.isRunning()
}
