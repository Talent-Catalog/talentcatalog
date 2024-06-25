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

import kotlin.test.*
import org.springframework.beans.factory.annotation.Autowired
import org.tctalent.server.model.db.SavedListLink
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest
import org.tctalent.server.repository.db.integrationhelp.getSavedSavedListLink

/** Does not appear to be used, similar to the repo. */
class SavedListLinkSpecificationIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repo: SavedListLinkRepository
  @Autowired lateinit var savedListRepository: SavedListRepository
  private lateinit var savedListLink: SavedListLink

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    savedListLink = getSavedSavedListLink(repo)
  }

  @Test
  fun `test keyword`() {
    assertTrue { true }
  }

  @Test
  fun `test keyword fail`() {
    assertTrue { true }
  }
}
