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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getTask;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tctalent.server.model.db.TaskImpl;
import org.tctalent.server.model.db.task.Task;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;

public class TaskRepositoryIntTest extends BaseDBIntegrationTest {

  @Autowired
  private TaskRepository repo;
  private TaskImpl task;

  @BeforeEach
  public void setUp() {
    assertTrue(isContainerInitialised());
    task = getTask(null, null);
    repo.save(task);
    assertTrue(task.getId() > 0);
    assertEquals("DEFAULT", task.getName());
  }

  @Test
  public void testFindByName() {
    List<TaskImpl> savedTask = repo.findByName("DEFAULT");
    assertNotNull(savedTask);
    List<Long> resultIds = savedTask.stream().map(TaskImpl::getId).toList();
    assertTrue(resultIds.contains(task.getId()));
    assertEquals(1, savedTask.size());
  }

  @Test
  public void testFindByLowerName() {
    Optional<TaskImpl> savedTask = repo.findByLowerName("Default");
    assertNotNull(savedTask);
    assertTrue(savedTask.isPresent());
    assertEquals(task.getId(), savedTask.get().getId());
    assertEquals("DEFAULT", savedTask.get().getName());
  }

  @Test
  public void testFindByLowerNameFail() {
    Optional<TaskImpl> savedTask = repo.findByLowerName("NothingToFind");
    assertNotNull(savedTask);
    assertFalse(savedTask.isPresent());
  }

  @Test
  public void testFindByLowerDisplayName() {
    TaskImpl savedTask = repo.findByLowerDisplayName("Default Display");
    assertNotNull(savedTask);
    assertEquals(task.getId(), savedTask.getId());
    assertEquals("DEFAULT", savedTask.getName());
  }

  @Test
  public void testFindByLowerDisplayNameFail() {
    Task savedTask = repo.findByLowerDisplayName("NothingToFind");
    assertNull(savedTask);
  }
}
