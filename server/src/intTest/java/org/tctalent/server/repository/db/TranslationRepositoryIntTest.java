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
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedTranslation;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tctalent.server.model.db.Translation;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;

public class TranslationRepositoryIntTest extends BaseDBIntegrationTest {

  @Autowired
  private TranslationRepository repo;
  private Translation translation;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());
    translation = getSavedTranslation(repo);
  }

  @Test
  public void findByTypeLanguage() {
    List<Translation> t = repo.findByTypeLanguage(translation.getObjectType(),
        translation.getLanguage());
    assertNotNull(t);
    assertFalse(t.isEmpty());
    assertEquals(1, t.size());
    List<Long> ids = t.stream().map(Translation::getId).toList();
    assertEquals(translation.getId(), ids.getFirst());
  }

  @Test
  public void findByTypeLanguageFail() {
    List<Translation> t = repo.findByTypeLanguage(translation.getObjectType(), null);
    assertNotNull(t);
    assertTrue(t.isEmpty());
  }

  @Test
  public void findByIdsTypeLanguage() {
    List<Translation> t = repo.findByIdsTypeLanguage(List.of(1L), translation.getObjectType(),
        translation.getLanguage());
    assertNotNull(t);
    assertFalse(t.isEmpty());
    assertEquals(1, t.size());
    List<Long> ids = t.stream().map(Translation::getId).toList();
    assertEquals(translation.getId(), ids.getFirst());
  }

  @Test
  public void findByIdsTypeLanguageFail() {
    List<Translation> t = repo.findByIdsTypeLanguage(List.of(1L), null, translation.getLanguage());
    assertNotNull(t);
    assertTrue(t.isEmpty());
  }

  @Test
  public void findByObjectIdTypeLang() {
    Optional<Translation> t = repo.findByObjectIdTypeLang(translation.getObjectId(),
        translation.getObjectType(), translation.getLanguage());
    assertTrue(t.isPresent());
    assertEquals(translation.getId(), t.get().getId());
  }

  @Test
  public void findByObjectIdTypeLangFail() {
    Optional<Translation> t = repo.findByObjectIdTypeLang(translation.getObjectId(), null,
        translation.getLanguage());
    assertTrue(t.isEmpty());
  }

  @Test
  public void testDeleteTranslations() {
    repo.delete(translation);
    List<Translation> t = repo.findAll();
    assertNotNull(t);
    assertTrue(t.isEmpty());
  }
}
