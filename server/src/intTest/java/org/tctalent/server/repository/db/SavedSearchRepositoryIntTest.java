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
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedSavedSearch;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedSfJobOpp;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedUser;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.SavedSearch;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;

public class SavedSearchRepositoryIntTest extends BaseDBIntegrationTest {

  @Autowired
  private SavedSearchRepository repo;
  @Autowired
  private SalesforceJobOppRepository sfJobOppRepository;
  @Autowired
  private UserRepository userRepository;
  private SavedSearch savedSearch;
  private SalesforceJobOpp testSFJobOpp;
  private User testUser;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());

    savedSearch = getSavedSavedSearch(repo);
    testUser = getSavedUser(userRepository);
    testSFJobOpp = getSavedSfJobOpp(sfJobOppRepository);
  }

  @Test
  public void testDelete() {
    savedSearch.setSfJobOpp(testSFJobOpp);
    repo.save(savedSearch);
    repo.deleteByJobId(testSFJobOpp.getId());
    List<SavedSearch> result = repo.findAll();
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testFindByNameIgnoreCase() {
    repo.save(savedSearch);
    SavedSearch results = repo.findByNameIgnoreCase(savedSearch.getName(),
        savedSearch.getCreatedBy().getId());
    assertNotNull(results);
    assertEquals(savedSearch.getId(), results.getId());
  }

  @Test
  public void testFindByNameIgnoreCaseFail() {
    repo.save(savedSearch);
    SavedSearch results = repo.findByNameIgnoreCase("NONE", testUser.getId());
    assertNull(results);
  }

  @Test
  public void testFindByIdLoadSearchJoins() {
    Optional<SavedSearch> result = repo.findByIdLoadSearchJoins(savedSearch.getId());
    assertTrue(result.isPresent());
    assertEquals(savedSearch.getId(), result.get().getId());
  }

  @Test
  public void testFindByIdLoadSearchJoinsFail() {
    Optional<SavedSearch> result = repo.findByIdLoadSearchJoins(0L);
    assertFalse(result.isPresent());
  }

  @Test
  public void testFindByIdLoadUsers() {
    Optional<SavedSearch> result = repo.findByIdLoadUsers(savedSearch.getId());
    assertTrue(result.isPresent());
    assertEquals(savedSearch.getId(), result.get().getId());
  }

  @Test
  public void testFindByIdLoadUsersFailDeleted() {
    savedSearch.setStatus(Status.deleted);
    repo.save(savedSearch);
    Optional<SavedSearch> result = repo.findByIdLoadUsers(savedSearch.getId());
    assertFalse(result.isPresent());
  }

  @Test
  public void testFindByIdLoadUsersFailNoId() {
    Optional<SavedSearch> result = repo.findByIdLoadUsers(0L);
    assertFalse(result.isPresent());
  }

  @Test
  public void testFindByIdLoadAudit() {
    Optional<SavedSearch> result = repo.findByIdLoadAudit(savedSearch.getId());
    assertTrue(result.isPresent());
    assertEquals(savedSearch.getId(), result.get().getId());
  }

  @Test
  public void testFindByIdLoadAuditFailNoId() {
    Optional<SavedSearch> result = repo.findByIdLoadAudit(0L);
    assertFalse(result.isPresent());
  }

  @Test
  public void testFindByWatcherIdsIsNotNullFailDeleted() {
    savedSearch.setStatus(Status.deleted);
    repo.save(savedSearch);
    Set<SavedSearch> results = repo.findByWatcherIdsIsNotNull();
    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  @Test
  public void testFindByWatcherIdsIsNotNullFailIsNull() {
    savedSearch.setWatcherIds(null);
    repo.save(savedSearch);
    Set<SavedSearch> results = repo.findByWatcherIdsIsNotNull();
    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  @Test
  public void testFindByWatcherIdsIsNotNull() {
    savedSearch.setWatcherIds(String.valueOf(testUser.getId()));
    repo.save(savedSearch);
    Set<SavedSearch> results = repo.findByWatcherIdsIsNotNull();
    assertNotNull(results);
    assertEquals(1, results.size());
    assertEquals(savedSearch.getId(), results.stream().toList().getFirst().getId());
  }

  @Test
  public void testFindUserWatchedSearches() {
    savedSearch.setWatcherIds(String.valueOf(testUser.getId()));
    repo.save(savedSearch);
    Set<SavedSearch> results = repo.findUserWatchedSearches(testUser.getId());
    assertNotNull(results);
    assertFalse(results.isEmpty());
    assertEquals(savedSearch.getId(), results.stream().toList().getFirst().getId());
  }

  @Test
  public void testFindUserWatchedSearchesFailDeleted() {
    savedSearch.setStatus(Status.deleted);
    repo.save(savedSearch);
    Set<SavedSearch> results = repo.findUserWatchedSearches(testUser.getId());
    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  @Test
  public void testFindUserWatchedSearchesFailId() {
    Set<SavedSearch> results = repo.findUserWatchedSearches(0L);
    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  @Test
  public void testFindDefaultSavedSearch() {
    Optional<SavedSearch> results = repo.findDefaultSavedSearch(savedSearch.getCreatedBy().getId());
    assertTrue(results.isPresent());
    assertEquals(savedSearch.getId(), results.get().getId());
  }

  @Test
  public void testFindDefaultSavedSearchFail() {
    Optional<SavedSearch> results = repo.findDefaultSavedSearch(testUser.getId());
    assertFalse(results.isPresent());
  }
}
