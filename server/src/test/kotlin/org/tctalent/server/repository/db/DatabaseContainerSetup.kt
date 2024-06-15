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

import org.junit.ClassRule
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.containers.wait.strategy.WaitAllStrategy
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
object DatabaseContainerSetup {
    private val createDb = "postgres psql -U postgres -c \"CREATE DATABASE tctalent;\""
    private val createUser = "postgres psql -U postgres -c \"CREATE USER tctalent with PASSWORD 'tctalent';\""
    private val superUser = "postgres psql -U postgres -c \"ALTER USER tctalent WITH SUPERUSER;\""

    @JvmField
    @ClassRule
    val db = PostgreSQLContainer("postgres:14")
        .withDatabaseName("tctalent")
        .withUsername("tctalent")
        .withPassword("tctalent")

    fun startDbContainer() {
        db.start()
    }

    fun stopDbContainer() {
        db.stop()
    }

    fun registerDbContainer(registry: DynamicPropertyRegistry) {
        registry.add("spring.datasource.url", db::getJdbcUrl)
        registry.add("spring.datasource.username", db::getUsername)
        registry.add("spring.datasource.password", db::getPassword)
    }

    fun isDbRunning(): Boolean = db.isRunning
}