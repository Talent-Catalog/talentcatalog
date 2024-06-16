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

import java.time.OffsetDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.tctalent.server.model.db.TaskImpl
import org.tctalent.server.model.db.User

open class TaskRepositoryIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repo: TaskRepository

  @Test
  fun `test find by name`() {
    assertTrue { isContainerInitialized() }
    val name = "BigTask"
    val user = User()
    user.id = 4
    user.username = "andrew.todd"
    val task = TaskImpl()
    assertNull(task.id)
    task.setName(name)
    task.setCreatedBy(user)
    task.setCreatedDate(OffsetDateTime.now())
    repo.save(task)
    assertNotNull(task.id)
    assertTrue { task.id > 0 }
    assertEquals(name, task.name)

    val savedTask = repo.findByName(name)
    assertNotNull(savedTask)
    assertEquals(1, savedTask.size)
  }
}
