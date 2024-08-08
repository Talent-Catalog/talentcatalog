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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getTask;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.tctalent.server.model.db.TaskImpl;
import org.tctalent.server.model.db.task.Task;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;
import org.tctalent.server.request.task.SearchTaskRequest;

public class TaskSpecificationIntTest extends BaseDBIntegrationTest {

  @Autowired
  TaskRepository repo;
  private Task task;
  Specification<TaskImpl> spec;
  SearchTaskRequest request;

  @BeforeEach
  public void setUp() {
    assertTrue(isContainerInitialised());
    repo.deleteAll();
    task = getTask(null, null);
    repo.save((TaskImpl) task);
    assertTrue(task.getId() > 0);
    assertEquals("DEFAULT", task.getName());
     request = new SearchTaskRequest();
  }

  @Test
  public void testWithKeyword() {
     request.setKeyword(task.getName());
     spec = TaskSpecification.buildSearchQuery(request);
    List<TaskImpl> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(task.getName(), result.getFirst().getName());
  }

  @Test
  public void testWithNoMatchKeyword() {
    request.setKeyword("NOTHING");
    spec = TaskSpecification.buildSearchQuery(request);
    List<TaskImpl> result = repo.findAll(spec);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }
  @Test
  public void testWithEmptyKeyword() {
    request.setKeyword(null);
    spec = TaskSpecification.buildSearchQuery(request);
    List<TaskImpl> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(task.getName(), result.getFirst().getName());
  }
}
