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

package org.tctalent.server.repository.db

import kotlin.jvm.optionals.getOrNull
import kotlin.test.*
import org.springframework.beans.factory.annotation.Autowired
import org.tctalent.server.model.db.ExportColumn
import org.tctalent.server.model.db.SavedList
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest
import org.tctalent.server.repository.db.integrationhelp.getSavedList

class ExportColumnRepositoryIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repo: ExportColumnRepository
  @Autowired lateinit var savedListRepository: SavedListRepository
  private lateinit var ec: ExportColumn
  private lateinit var testSavedList: SavedList

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }

    testSavedList = getSavedList(savedListRepository)
    ec = ExportColumn().apply { savedList = testSavedList }
    repo.save(ec)
    assertTrue { ec.id > 0 }
  }

  @Test
  fun `delete by saved list`() {

    val savedExportColumn = repo.findById(ec.id).getOrNull()
    assertNotNull(savedExportColumn)
    assertEquals(testSavedList.id, savedExportColumn.savedList.id)
    repo.deleteBySavedList(testSavedList)
    val secondSavedExportColumn = repo.findById(ec.id).getOrNull()
    assertNull(secondSavedExportColumn)
  }
}
