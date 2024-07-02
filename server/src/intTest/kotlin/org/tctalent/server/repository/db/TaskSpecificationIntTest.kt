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

import kotlin.test.*
import org.springframework.beans.factory.annotation.Autowired
import org.tctalent.server.model.db.TaskImpl
import org.tctalent.server.model.db.task.Task
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest
import org.tctalent.server.repository.db.integrationhelp.getTask
import org.tctalent.server.request.task.SearchTaskRequest

class TaskSpecificationIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repo: TaskRepository
  private lateinit var task: Task

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    // Delete everything first.
    repo.deleteAll()
    task = getTask()
    repo.save(task as TaskImpl)
    assertTrue { task.id > 0 }
    assertEquals("DEFAULT", task.name)
  }

  @Test
  fun `test build search query with keyword`() {
    val request = SearchTaskRequest().apply { keyword = task.name }
    val spec = TaskSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isNotEmpty())
    assertEquals(1, result.size)
    assertEquals(task.id, result.first().id)
  }

  @Test
  fun `test build search query with non-matching keyword`() {
    val request = SearchTaskRequest().apply { keyword = "NOTHING" }
    val spec = TaskSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isEmpty())
  }

  @Test
  fun `test build search query with empty keyword`() {
    val request = SearchTaskRequest().apply { keyword = "" }
    val spec = TaskSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isNotEmpty())
    assertEquals(1, result.size)
    assertEquals(task.id, result.first().id)
  }
}
