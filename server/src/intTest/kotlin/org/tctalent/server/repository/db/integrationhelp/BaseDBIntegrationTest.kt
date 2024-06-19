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

import org.junit.jupiter.api.BeforeAll
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Testcontainers

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
abstract class BaseDBIntegrationTest {

  companion object {
    private val dbContainer = DBContainer

    @BeforeAll
    @JvmStatic
    fun startDBContainer() {
      dbContainer.startDBContainer()
    }

    @DynamicPropertySource
    @JvmStatic
    fun registerDBContainer(registry: DynamicPropertyRegistry) {
      dbContainer.registerDBContainer(registry)
    }
  }

  fun isContainerInitialized(): Boolean = dbContainer.db.isRunning()
}
