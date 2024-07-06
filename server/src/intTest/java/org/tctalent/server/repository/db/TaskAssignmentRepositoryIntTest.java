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

package org.tctalent.server.repository.db;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedCandidate;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedSavedList;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedTask;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedUser;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getTaskAssignment;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.TaskAssignmentImpl;
import org.tctalent.server.model.db.TaskImpl;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;

public class TaskAssignmentRepositoryIntTest extends BaseDBIntegrationTest {

  @Autowired
  private TaskAssignmentRepository repo;
  @Autowired
  private TaskRepository taskRepository;
  @Autowired
  private CandidateRepository candidateRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private SavedListRepository savedListRepository;

  private TaskAssignmentImpl ta;
  private SavedList savedList;

  @BeforeEach
  void setup() {
    assertTrue(isContainerInitialised());

    User user = getSavedUser(userRepository);
    Candidate testCandidate = getSavedCandidate(candidateRepository, user);

    TaskImpl testTask = getSavedTask(taskRepository);
    savedList = getSavedSavedList(savedListRepository);

    ta = getTaskAssignment(user);
    ta.setTask(testTask);
    ta.setCandidate(testCandidate);
    ta.setRelatedList(savedList);

    assertNull(ta.getId());
    repo.save(ta);
    assertNotNull(ta.getId());
    assertTrue(ta.getId() > 0);
  }

  @Test
  void testFindByTaskAndList() {
    Long taskId = ta.getTask().getId();
    List<TaskAssignmentImpl> savedAssignment = repo.findByTaskAndList(taskId, savedList.getId());

    assertNotNull(savedAssignment);
    assertFalse(savedAssignment.isEmpty());

    List<Long> resultIds = savedAssignment.stream().map(TaskAssignmentImpl::getId).toList();
    assertTrue(resultIds.contains(ta.getId()));
  }

  /**
   * This is identical to above, except with the 1 added to the savedList id so that it does not
   * return results.
   */
  @Test
  void testFindByTaskAndListFails() {
    Long taskId = ta.getTask().getId();
    List<TaskAssignmentImpl> savedAssignment = repo.findByTaskAndList(taskId,
        savedList.getId() + 1);

    assertNotNull(savedAssignment);
    assertTrue(savedAssignment.isEmpty());
  }
}
