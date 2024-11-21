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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedSystemLanguage;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.SystemLanguage;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;

public class SystemLanguageRepositoryIntTest extends BaseDBIntegrationTest {

  @Autowired
  private SystemLanguageRepository repo;
  private SystemLanguage systemLanguage;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());
    systemLanguage = getSavedSystemLanguage(repo);
    assertTrue(systemLanguage.getId() > 0);
  }

  @Test
  public void testFindByStatus() {
    List<SystemLanguage> savedLang = repo.findByStatus(Status.active);
    assertNotNull(savedLang);
    assertFalse(savedLang.isEmpty());
    List<Long> resultIds = savedLang.stream().map(SystemLanguage::getId).toList();
    assertTrue(resultIds.contains(systemLanguage.getId()));
  }

  @Test
  public void testFindByStatusFAils() {
    List<SystemLanguage> savedLang = repo.findByStatus(Status.deleted);
    assertNotNull(savedLang);
    assertTrue(savedLang.isEmpty());
  }
}
