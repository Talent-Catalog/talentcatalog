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

import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;

/**
 * Appears the class is unused. No table in the DB. Will fail when running.
 */
public class SavedListLinkRepositoryIntTest extends BaseDBIntegrationTest {

//  @Autowired
//  private SavedListLinkRepository repo;
//  @Autowired
//  private SavedListRepository savedListRepository;
//  private SavedListLink savedListLink;
//
//  @BeforeEach
//  public void setup() {
//    assertTrue(isContainerInitialised());
//    savedListLink = getSavedSavedListLink(repo);
//  }
//
//  @Test
//  public void testFindByLinkIgnoreCase() {
//    SavedListLink result = repo.findByLinkIgnoreCase(savedListLink.getLink());
//    assertNotNull(result);
//    assertEquals(savedListLink.getId(), result.getId());
//  }
//
//  @Test
//  public void testFindByLinkIgnoreCaseFail() {
//    SavedListLink result = repo.findByLinkIgnoreCase("");
//    assertNull(result);
//  }
//
//  @Test
//  public void testFindBySavedList() {
//    SavedList testSavedList = getSavedSavedList(savedListRepository);
//    savedListLink.setSavedList(testSavedList);
//    repo.save(savedListLink);
//    SavedListLink result = repo.findBySavedList(testSavedList.getId());
//    assertNotNull(result);
//    assertEquals(savedListLink.getId(), result.getId());
//  }
//
//  @Test
//  public void testFindBySavedListFail() {
//    SavedList testSavedList = getSavedSavedList(savedListRepository);
//    savedListLink.setSavedList(testSavedList);
//    repo.save(savedListLink);
//    SavedListLink result = repo.findBySavedList(0L);
//    assertNull(result);
//  }
}
