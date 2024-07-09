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
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getLanguageLevel;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedLanguageLevel;

import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tctalent.server.model.db.LanguageLevel;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;

public class LanguageLevelRepositoryIntTest extends BaseDBIntegrationTest {

  @Autowired
  private LanguageLevelRepository repo;
  private LanguageLevel languageLevel;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());
    languageLevel = getSavedLanguageLevel(repo);
  }

  @Test
  public void testFindByStatus() {
    List<LanguageLevel> levels = repo.findByStatus(Status.active);
    assertNotNull(levels);
    assertFalse(levels.isEmpty());
    List<String> names = levels.stream().map(LanguageLevel::getName).toList();
    assertTrue(names.contains(languageLevel.getName()));
  }

  @Test
  public void testFindByStatusFail() {
    LanguageLevel newLevel = getLanguageLevel();
    newLevel.setStatus(Status.inactive);
    repo.save(newLevel);
    assertTrue(newLevel.getId() > 0);
    List<LanguageLevel> savedIndustry = repo.findByStatus(Status.active);
    assertNotNull(savedIndustry);
    assertFalse(savedIndustry.isEmpty());
    List<Long> ids = savedIndustry.stream().map(LanguageLevel::getId).toList();
    assertFalse(ids.contains(newLevel.getId()));
  }

  @Test
  public void testFindByNameIgnoreCase() {
    String name = languageLevel.getName().toUpperCase(Locale.getDefault());
    LanguageLevel i = repo.findByNameIgnoreCase(name);
    assertNotNull(i);
    assertEquals(languageLevel.getName(), i.getName());
  }

  // TODO (this test case tests wrong code - i.e. name and code don't match)
  @Test
  public void findByLevelIgnoreCase() {
    // The code actually finds by level and not deleted.
    LanguageLevel level = repo.findByLevelIgnoreCase(1);
    assertNotNull(level);
  }

  // TODO (this test case tests wrong code - i.e. name and code don't match)
  @Test
  public void findByLevelIgnoreCaseFail() {
    // The code actually finds by level and not deleted. Testing the deleted bit.
    languageLevel.setStatus(Status.deleted);
    repo.save(languageLevel);
    LanguageLevel level = repo.findByLevelIgnoreCase(1);
    assertNull(level);
  }

  @Test
  public void findAllActive() {
    // add a second inactive.
    LanguageLevel l = getLanguageLevel();
    l.setStatus(Status.inactive);
    repo.save(l);
    List<LanguageLevel> levels = repo.findAllActive();
    assertNotNull(levels);
    assertFalse(levels.isEmpty());
    List<Long> ids = levels.stream().map(LanguageLevel::getId).toList();
    assertTrue(ids.contains(languageLevel.getId()));
  }
}
