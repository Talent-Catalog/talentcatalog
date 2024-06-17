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

import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class TaskAssignmentRepositoryIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repo: TaskAssignmentRepository
  @Autowired lateinit var taskRepository: TaskRepository
  @Autowired lateinit var candidateRepository: CandidateRepository
  @Autowired lateinit var userRepository: UserRepository

  @Test
  fun `test find by task and list`() {
    assertTrue { isContainerInitialized() }

    val testTask = getSavedTask(taskRepository)
    val user = getSavedUser(userRepository)
    val testCandidate = getSavedCandidate(candidateRepository, user)

    val ta = getTaskAssignment(user)
    ta.apply {
      task = testTask
      candidate = testCandidate
    }
    assertNull(ta.id)
    repo.save(ta)
    assertNotNull(ta.id)
    assertTrue { ta.id > 0 }
    val taskId = ta.task.id

    val savedAssignment = repo.findByTaskAndList(taskId, null)
    assertNotNull(savedAssignment)
    assertTrue { savedAssignment.size > 0 }
  }
}
