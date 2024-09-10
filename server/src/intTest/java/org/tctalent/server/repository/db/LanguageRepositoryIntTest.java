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
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getLanguage;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedLanguage;

import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tctalent.server.model.db.Language;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;

public class LanguageRepositoryIntTest extends BaseDBIntegrationTest {

  @Autowired
  private LanguageRepository repo;
  private Language language;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());
    language = getSavedLanguage(repo);
  }

  @Test
  public void testFindByStatus() {
    List<Language> lang = repo.findByStatus(Status.active);
    assertNotNull(lang);
    assertFalse(lang.isEmpty());
    List<String> names = lang.stream().map(Language::getName).toList();
    assertTrue(names.contains(language.getName()));
  }

  @Test
  public void testFindByStatusFail() {
    Language newLang = getLanguage();
    newLang.setStatus(Status.inactive);
    repo.save(newLang);
    assertTrue(newLang.getId() > 0);
    List<Language> savedIndustry = repo.findByStatus(Status.active);
    assertNotNull(savedIndustry);
    assertFalse(savedIndustry.isEmpty());
    List<Long> ids = savedIndustry.stream().map(Language::getId).toList();
    assertFalse(ids.contains(newLang.getId()));
  }

  @Test
  public void testFindByNameIgnoreCase() {
    String name = language.getName().toUpperCase(Locale.getDefault());
    Language i = repo.findByNameIgnoreCase(name);
    assertNotNull(i);
    assertEquals(language.getName(), i.getName());
  }
}
