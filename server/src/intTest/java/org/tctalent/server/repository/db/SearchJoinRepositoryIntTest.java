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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedSavedSearch;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSearchJoin;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tctalent.server.model.db.SavedSearch;
import org.tctalent.server.model.db.SearchJoin;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;

public class SearchJoinRepositoryIntTest extends BaseDBIntegrationTest {

  @Autowired
  private SearchJoinRepository repo;
  @Autowired
  private SavedSearchRepository searchSavedRepository;
  private SavedSearch testSavedSearch;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());
    testSavedSearch = getSavedSavedSearch(searchSavedRepository);
    SearchJoin searchJoin = getSearchJoin();
    searchJoin.setSavedSearch(testSavedSearch);
    searchJoin.setChildSavedSearch(testSavedSearch);
    repo.save(searchJoin);
    assertTrue(searchJoin.getId() > 0);
  }

  @Test
  public void testDeleteBySearchId() {
    SavedSearch newTSS = getSavedSavedSearch(searchSavedRepository);
    SearchJoin newSJ = getSearchJoin();
    newSJ.setSavedSearch(newTSS);
    newSJ.setChildSavedSearch(testSavedSearch);
    repo.save(newSJ);
    assertTrue(newSJ.getId() > 0);

    repo.deleteBySearchId(testSavedSearch.getId());
    List<SearchJoin> saved = repo.findAll();
    assertNotNull(saved);
    assertEquals(1, saved.size());
    assertEquals(newSJ.getId(), saved.getFirst().getId());
  }
}
