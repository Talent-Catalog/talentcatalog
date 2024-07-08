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
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedEducationLevel;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tctalent.server.model.db.EducationLevel;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;

public class EducationLevelRepositoryIntTest extends BaseDBIntegrationTest {

  @Autowired
  private EducationLevelRepository repo;
  private EducationLevel educationLevel;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());
    educationLevel = getSavedEducationLevel(repo);
  }

  @Test
  public void testFindByStatus() {
    List<EducationLevel> result = repo.findByStatus(Status.active);
    assertNotNull(result);
    assertFalse(result.isEmpty());
  }

  @Test
  public void testFindByStatusSingle() {
    List<EducationLevel> result = repo.findByStatus(Status.deleted);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    List<Long> ids = result.stream().map(EducationLevel::getId).toList();
    assertEquals(educationLevel.getId(), ids.getFirst());
  }

  @Test
  public void testFindByStatusSingleFail() {
    educationLevel.setStatus(Status.active);
    repo.save(educationLevel);
    List<EducationLevel> result = repo.findByStatus(Status.deleted);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testFindByNameIgnoreCaseFailStatus() {
    EducationLevel result = repo.findByNameIgnoreCase(educationLevel.getName());
    assertNull(result);
  }

  @Test
  public void testFindByNameIgnoreCase() {
    educationLevel.setStatus(Status.active);
    repo.save(educationLevel);
    EducationLevel result = repo.findByNameIgnoreCase(educationLevel.getName());
    assertNotNull(result);
    assertEquals(educationLevel.getId(), result.getId());
  }

  @Test
  public void testFindByNameIgnoreCaseFail() {
    EducationLevel result = repo.findByNameIgnoreCase("NONE TO FIND");
    assertNull(result);
  }

  @Test
  public void testFindByLevelIgnoreCaseFailLevel() {
    EducationLevel result = repo.findByLevelIgnoreCase(9);
    assertNull(result);
  }

  @Test
  public void testFindByLevelIgnoreCaseFailStatus() {
    EducationLevel result = repo.findByLevelIgnoreCase(7);
    assertNull(result);
  }

  @Test
  public void testFindByLevelIgnoreCase() {
    educationLevel.setStatus(Status.active);
    repo.save(educationLevel);
    EducationLevel result = repo.findByLevelIgnoreCase(9);
    assertNotNull(result);
    assertEquals(educationLevel.getId(), result.getId());
  }

  @Test
  public void testFindAllActive() {
    List<EducationLevel> results = repo.findAllActive();
    assertNotNull(results);
    assertFalse(results.isEmpty());
  }
}
