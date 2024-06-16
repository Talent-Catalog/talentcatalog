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

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.tctalent.server.model.db.TaskImpl
import java.time.OffsetDateTime
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

open class TaskRepositoryIntTest: BaseDBIntegrationTest() {
  @Autowired lateinit var repo: TaskRepository

  @Test
  fun `test find by name`() {
    assertTrue { isContainerInitialized() }

    val task = TaskImpl()
    assertNull(task.id)
    task.setName("Sample Simple Task")
    task.setCreatedDate(OffsetDateTime.now())
    repo.save(task)
    assertNotNull(task.id)
    assertTrue { task.id > 0 }
  }
}
