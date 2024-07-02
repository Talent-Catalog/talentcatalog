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

import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest

/** Appears unused class in main. No such table. Will fail when running. */
class SavedListLinkRepositoryIntTest : BaseDBIntegrationTest() {
  //  @Autowired lateinit var repo: SavedListLinkRepository
  //  @Autowired lateinit var savedListRepository: SavedListRepository
  //  private lateinit var savedListLink: SavedListLink
  //
  //  @BeforeTest
  //  fun setup() {
  //    assertTrue { isContainerInitialized() }
  //    savedListLink = getSavedSavedListLink(repo)
  //  }
  //
  //  @Test
  //  fun `test find by link ignore case`() {
  //    val result = repo.findByLinkIgnoreCase(savedListLink.link)
  //    assertNotNull(result)
  //    assertEquals(savedListLink.id, result.id)
  //  }
  //
  //  @Test
  //  fun `test find by link ignore case fail`() {
  //    val result = repo.findByLinkIgnoreCase("")
  //    assertNull(result)
  //  }
  //
  //  @Test
  //  fun `test find by saved list`() {
  //    val testSavedList = getSavedSavedList(savedListRepository)
  //    repo.save(savedListLink.apply { savedList = testSavedList })
  //    val result = repo.findBySavedList(testSavedList.id)
  //    assertNotNull(result)
  //    assertEquals(savedListLink.id, result.id)
  //  }
  //
  //  @Test
  //  fun `test find by saved list fail`() {
  //    val testSavedList = getSavedSavedList(savedListRepository)
  //    repo.save(savedListLink.apply { savedList = testSavedList })
  //    val result = repo.findBySavedList(0L)
  //    assertNull(result)
  //  }
}
