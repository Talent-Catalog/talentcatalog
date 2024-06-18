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

import kotlin.test.BeforeTest
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.tctalent.server.model.db.Candidate
import org.tctalent.server.model.db.SavedList
import org.tctalent.server.model.db.TaskAssignmentImpl
import org.tctalent.server.model.db.User
import org.tctalent.server.repository.db.integrationhelp.*

class TaskAssignmentRepositoryIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repo: TaskAssignmentRepository
  @Autowired lateinit var taskRepository: TaskRepository
  @Autowired lateinit var candidateRepository: CandidateRepository
  @Autowired lateinit var userRepository: UserRepository
  @Autowired lateinit var savedListRepository: SavedListRepository
  private lateinit var user: User
  private lateinit var testCandidate: Candidate
  private lateinit var ta: TaskAssignmentImpl
  private lateinit var savedList: SavedList

  @BeforeTest
  fun setup() {
    user = getSavedUser(userRepository)
    testCandidate = getSavedCandidate(candidateRepository, user)

    val testTask = getSavedTask(taskRepository)
    savedList = getSavedList(savedListRepository)

    ta = getTaskAssignment(user)
    ta.apply {
      task = testTask
      candidate = testCandidate
      relatedList = savedList
    }
    assertNull(ta.id)
    repo.save(ta)
    assertNotNull(ta.id)
    assertTrue { ta.id > 0 }
  }

  @Test
  fun `test find by task and list`() {
    assertTrue { isContainerInitialized() }

    val taskId = ta.task.id

    val savedAssignment = repo.findByTaskAndList(taskId, savedList.id)
    assertNotNull(savedAssignment)
    assertTrue { savedAssignment.isNotEmpty() }

    val resultIds = savedAssignment.map { it.id }
    assertTrue { resultIds.contains(ta.id) }
  }

  /**
   * This is identical to above, except with the 1 added to the savedList id so that it does not
   * return results.
   */
  @Test
  fun `test find by task and list fails`() {
    assertTrue { isContainerInitialized() }

    val taskId = ta.task.id

    val savedAssignment = repo.findByTaskAndList(taskId, savedList.id + 1)
    assertNotNull(savedAssignment)
    assertTrue { savedAssignment.isEmpty() }
  }
}
