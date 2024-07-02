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

import kotlin.jvm.optionals.getOrNull
import kotlin.test.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.tctalent.server.model.db.TaskImpl
import org.tctalent.server.model.db.task.Task
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest
import org.tctalent.server.repository.db.integrationhelp.getTask

/**
 * Integration test class for task repository to ensure JPA working as expected. User comes from
 * creation function in base class.
 */
open class TaskRepositoryIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repo: TaskRepository
  private lateinit var task: Task

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }

    task = getTask()
    repo.save(task as TaskImpl)
    assertTrue { task.id > 0 }
    assertEquals("DEFAULT", task.name)
  }

  @Test
  fun `test find by name`() {

    val savedTask = repo.findByName("DEFAULT")
    assertNotNull(savedTask)

    val resultIds = savedTask.map { it.id }
    assertTrue { resultIds.contains(task.id) }
    assertEquals(1, savedTask.size)
  }

  @Test
  fun `test find by lower name`() {

    val savedTask = repo.findByLowerName("Default").orElseThrow { fail("Did not find.") }
    assertTrue { savedTask.id > 0 }
    assertEquals(task.id, savedTask.id)
    assertEquals("DEFAULT", savedTask.name)
  }

  @Test
  fun `test find by lower name fail`() {

    val savedTask = repo.findByLowerName("NothingToFind").getOrNull()
    assertNull(savedTask)
  }

  @Test
  fun `test find by lower display name`() {

    val savedTask = repo.findByLowerDisplayName("Default Display")
    assertTrue { savedTask.id > 0 }
    assertEquals(task.id, savedTask.id)
    assertEquals("DEFAULT", savedTask.name)
  }

  @Test
  fun `test find by lower display name fail`() {

    val savedTask = repo.findByLowerDisplayName("NothingToFind")
    assertNull(savedTask)
  }
}
