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
import static org.junit.jupiter.api.Assertions.fail;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedSavedList;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedSavedSearch;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedSfJobOpp;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.systemUser;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.SavedSearch;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;
import org.tctalent.server.request.list.SearchSavedListRequest;

public class GetSavedListsQueryIntTest extends BaseDBIntegrationTest {

  @Autowired
  private SavedListRepository repo;
  @Autowired
  private SalesforceJobOppRepository sfJobOppRepository;
  @Autowired
  private SavedSearchRepository savedSearchRepository;
  private SavedList savedList;
  private SearchSavedListRequest request;
  private GetSavedListsQuery spec;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());
    savedList = getSavedSavedList(repo);
    request = new SearchSavedListRequest();
  }

  @Test
  public void testKeyword() {
    request.setKeyword(savedList.getName());
    spec = new GetSavedListsQuery(request, null);
    List<SavedList> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(savedList.getId(), result.getFirst().getId());
  }

  @Test
  public void testKeywordFail() {
    SavedSearch ss = getSavedSavedSearch(savedSearchRepository);
    savedList.setSavedSearch(ss);
    repo.save(savedList);
    request.setKeyword("NOTHING");
    spec = new GetSavedListsQuery(request, null);
    List<SavedList> result = repo.findAll(spec);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testFixed() {
    request.setFixed(true);
    spec = new GetSavedListsQuery(request, null);
    List<SavedList> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(savedList.getId(), result.getFirst().getId());
  }

  @Test
  public void testFixedFalse() {
    SavedSearch ss = getSavedSavedSearch(savedSearchRepository);
    savedList.setSavedSearch(ss);
    repo.save(savedList);
    request.setFixed(false);
    spec = new GetSavedListsQuery(request, null);
    List<SavedList> result = repo.findAll(spec);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testRegisteredJob() {
    request.setRegisteredJob(true);
    spec = new GetSavedListsQuery(request, null);
    List<SavedList> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(savedList.getId(), result.getFirst().getId());
  }

  // TODO (the query is broken as it uses a method call as an attribute)
  // Requires confirmation and fixing.
  @Test
  public void testSfOppClosed() {
    SalesforceJobOpp testSFJobOpp = getSavedSfJobOpp(sfJobOppRepository);
    testSFJobOpp.setClosed(true);
    savedList.setSfJobOpp(testSFJobOpp);
    repo.save(savedList);

    SearchSavedListRequest request = new SearchSavedListRequest();
    request.setSfOppClosed(true);
    repo.save(savedList);
    spec = new GetSavedListsQuery(request, null);
    fail(
        "Expect to fail - query uses a method in place of an attribute so can't work? Should be fixed.");
    // List<SavedList> result = repo.findAll(spec);
    // assertNotNull(result);
    // assertTrue(!result.isEmpty());
    // assertEquals(1, result.size());
    // assertEquals(savedList.getId(), result.get(0).getId());
  }

  @Test
  public void testShortName() {
    request.setShortName(true);
    spec = new GetSavedListsQuery(request, null);
    List<SavedList> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(savedList.getId(), result.getFirst().getId());
  }

  @Test
  public void testShortNameFalse() {
    SavedSearch ss = getSavedSavedSearch(savedSearchRepository);
    savedList.setSavedSearch(ss);
    repo.save(savedList);
    request.setShortName(false);
    spec = new GetSavedListsQuery(request, null);
    List<SavedList> result = repo.findAll(spec);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testGlobal() {
    request = new SearchSavedListRequest();
    request.setGlobal(true);
    spec = new GetSavedListsQuery(request, null);
    List<SavedList> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(savedList.getId(), result.getFirst().getId());
  }

  @Test
  public void testGlobalFalse() {
    request.setGlobal(false);
    spec = new GetSavedListsQuery(request, null);
    List<SavedList> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
  }

  @Test
  public void testShared() {
    User loggedInUser = systemUser();
    loggedInUser.setSharedLists(Set.of(savedList));
    request.setShared(true);
    spec = new GetSavedListsQuery(request, loggedInUser);
    List<SavedList> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(savedList.getId(), result.getFirst().getId());
  }

  @Test
  public void testSharedFalse() {
    SavedSearch ss = getSavedSavedSearch(savedSearchRepository);
    savedList.setSavedSearch(ss);
    repo.save(savedList);
    User loggedInUser = systemUser();
    loggedInUser.setSharedLists(Set.of(savedList));
    request.setShared(false);
    spec = new GetSavedListsQuery(request, loggedInUser);
    List<SavedList> result = repo.findAll(spec);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testOwned() {
    User loggedInUser = systemUser();
    loggedInUser.setSharedLists(Set.of(savedList));
    savedList.setCreatedBy(loggedInUser);
    repo.save(savedList);
    request.setOwned(true);
    spec = new GetSavedListsQuery(request, loggedInUser);
    List<SavedList> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(savedList.getId(), result.getFirst().getId());
  }

  @Test
  public void testOwnedFalse() {
    SavedSearch ss = getSavedSavedSearch(savedSearchRepository);
    savedList.setSavedSearch(ss);
    repo.save(savedList);
    request.setOwned(false);
    spec = new GetSavedListsQuery(request, null);
    List<SavedList> result = repo.findAll(spec);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testOwnedNoLoggedInUser() {
    SavedSearch ss = getSavedSavedSearch(savedSearchRepository);
    savedList.setSavedSearch(ss);
    repo.save(savedList);
    request.setOwned(false);
    spec = new GetSavedListsQuery(request, null);
    List<SavedList> result = repo.findAll(spec);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }
}
