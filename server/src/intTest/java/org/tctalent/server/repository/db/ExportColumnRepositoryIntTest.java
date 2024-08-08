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
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedSavedList;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tctalent.server.model.db.ExportColumn;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;

public class ExportColumnRepositoryIntTest extends BaseDBIntegrationTest {

  @Autowired
  private ExportColumnRepository repo;
  @Autowired
  SavedListRepository savedListRepo;
  private ExportColumn ec;
  private SavedList testSavedList;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());

    testSavedList = getSavedSavedList(savedListRepo);
    ec = new ExportColumn();
    ec.setSavedList(testSavedList);
    repo.save(ec);
    assertTrue(ec.getId() > 0);
  }

  @Test
  public void testDeletedBySavedList() {
    Optional<ExportColumn> savedExportColumn = repo.findById(ec.getId());
    assertNotNull(savedExportColumn);
    assertTrue(savedExportColumn.isPresent());

    assertEquals(testSavedList.getId(), savedExportColumn.get().getSavedList().getId());
    repo.deleteBySavedList(testSavedList);
    Optional<ExportColumn> secondSavedExportColumn = repo.findById(ec.getId());
    assertNotNull(secondSavedExportColumn);
    assertFalse(secondSavedExportColumn.isPresent());
  }
}
