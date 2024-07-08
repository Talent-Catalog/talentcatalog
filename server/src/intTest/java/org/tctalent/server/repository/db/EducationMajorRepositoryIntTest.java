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
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getEducationMajor;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedEducationMajor;

import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tctalent.server.model.db.EducationMajor;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;

public class EducationMajorRepositoryIntTest extends BaseDBIntegrationTest {

  @Autowired
  private EducationMajorRepository repo;
  private EducationMajor educationMajor;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());
    educationMajor = getSavedEducationMajor(repo);
  }

  @Test
  public void testFindByStatus() {
    List<EducationMajor> levels = repo.findByStatus(Status.active);
    assertNotNull(levels);
    assertFalse(levels.isEmpty());
    List<String> names = levels.stream().map(EducationMajor::getName).toList();
    assertTrue(names.contains(educationMajor.getName()));
  }

  @Test
  public void testFindByStatusFail() {
    EducationMajor em = getEducationMajor();
    em.setStatus(Status.inactive);
    repo.save(em);
    assertTrue(em.getId() > 0);
    List<EducationMajor> savedIndustry = repo.findByStatus(Status.active);
    assertNotNull(savedIndustry);
    assertFalse(savedIndustry.isEmpty());
    List<Long> ids = savedIndustry.stream().map(EducationMajor::getId).toList();
    assertFalse(ids.contains(em.getId()));
  }

  @Test
  public void testFindByNameIgnoreCase() {
    String name = educationMajor.getName().toUpperCase(Locale.getDefault());
    EducationMajor i = repo.findByNameIgnoreCase(name);
    assertNotNull(i);
    assertEquals(educationMajor.getName(), i.getName());
  }

  @Test
  public void testGetNamesForIds() {
    EducationMajor em = getEducationMajor();
    repo.save(em);
    EducationMajor em2 = getEducationMajor();
    repo.save(em2);
    List<String> names = repo.getNamesForIds(
        List.of(em.getId(), educationMajor.getId(), em2.getId()));
    assertEquals(3, names.size());
    assertTrue(names.stream().allMatch(name -> name.startsWith("TEST_EDUCATION")));
  }
}
