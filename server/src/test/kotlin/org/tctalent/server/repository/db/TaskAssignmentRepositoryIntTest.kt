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
import org.tctalent.server.repository.db.integrationhelp.*

class TaskAssignmentRepositoryIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repo: TaskAssignmentRepository
  @Autowired lateinit var taskRepository: TaskRepository
  @Autowired lateinit var candidateRepository: CandidateRepository
  @Autowired lateinit var userRepository: UserRepository
  @Autowired lateinit var savedListRepository: SavedListRepository

  @Test
  fun `test find by task and list`() {
    assertTrue { isContainerInitialized() }

    val testTask = getSavedTask(taskRepository)
    val user = getSavedUser(userRepository)
    val testCandidate = getSavedCandidate(candidateRepository, user)
    val savedList = getSavedList(savedListRepository)

    val ta = getTaskAssignment(user)
    ta.apply {
      task = testTask
      candidate = testCandidate
      relatedList = savedList
    }
    assertNull(ta.id)
    repo.save(ta)
    assertNotNull(ta.id)
    assertTrue { ta.id > 0 }
    val taskId = ta.task.id

    val savedAssignment = repo.findByTaskAndList(taskId, savedList.id)
    assertNotNull(savedAssignment)
    assertTrue { savedAssignment.size > 0 }
  }

  /**
   * This is identical to above, except with the 1 added to the savedList id so that it does not
   * return results.
   */
  @Test
  fun `test find by task and list fails`() {
    assertTrue { isContainerInitialized() }

    val testTask = getSavedTask(taskRepository)
    val user = getSavedUser(userRepository)
    val testCandidate = getSavedCandidate(candidateRepository, user)
    val savedList = getSavedList(savedListRepository)

    val ta = getTaskAssignment(user)
    ta.apply {
      task = testTask
      candidate = testCandidate
      relatedList = savedList
    }
    assertNull(ta.id)
    repo.save(ta)
    assertNotNull(ta.id)
    assertTrue { ta.id > 0 }
    val taskId = ta.task.id

    val savedAssignment = repo.findByTaskAndList(taskId, savedList.id + 1)
    assertNotNull(savedAssignment)
    assertTrue { savedAssignment.isEmpty() }
  }
}
