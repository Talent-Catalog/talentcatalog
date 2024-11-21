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
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedSavedSearch;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedSfJobOpp;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedUser;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.SavedSearch;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;

public class SavedListRepositoryIntTest extends BaseDBIntegrationTest {

  @Autowired
  private SavedListRepository repo;
  @Autowired
  private SalesforceJobOppRepository sfJobOppRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private SavedSearchRepository savedSearchRepository;
  private SavedList savedList;
  private SalesforceJobOpp testSFJobOpp;
  private User testUser;
  private SavedSearch testSavedSearch;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());

    savedList = getSavedSavedList(repo);
    testSFJobOpp = getSavedSfJobOpp(sfJobOppRepository);
    testUser = getSavedUser(userRepository);
  }

  @Test
  public void testFindByJobIds() {
    savedList.setSfJobOpp(testSFJobOpp);
    repo.save(savedList);
    List<SavedList> result = repo.findByJobIds(testSFJobOpp.getId());
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(savedList.getId(), result.getFirst().getId());
  }

  @Test
  public void testFindByJobIdsFail() {
    List<SavedList> result = repo.findByJobIds(testSFJobOpp.getId());
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testFindByNameIgnoreCase() {
    repo.save(savedList);
    SavedList results = repo.findByNameIgnoreCase(savedList.getName(),
        savedList.getCreatedBy().getId()).orElse(null);
    assertNotNull(results);
    assertEquals(savedList.getId(), results.getId());
  }

  @Test
  public void testFindByNameIgnoreCaseFail() {
    repo.save(savedList);
    SavedList results = repo.findByNameIgnoreCase("NONE", testUser.getId()).orElse(null);
    assertNull(results);
  }

  @Test
  public void testFindByIdLoadUsers() {
    SavedList result = repo.findByIdLoadUsers(savedList.getId()).orElse(null);
    assertNotNull(result);
    assertEquals(savedList.getId(), result.getId());
  }

  @Test
  public void testFindByIdLoadUsersFailNoId() {
    SavedList result = repo.findByIdLoadUsers(0L).orElse(null);
    assertNull(result);
  }

  @Test
  public void testFindByIdLoadCandidates() {
    SavedList result = repo.findByIdLoadCandidates(savedList.getId()).orElse(null);
    assertNotNull(result);
    assertEquals(savedList.getId(), result.getId());
  }

  @Test
  public void testFindByIdLoadCandidatesFailNoId() {
    SavedList result = repo.findByIdLoadCandidates(0L).orElse(null);
    assertNull(result);
  }

  @Test
  public void testFindSelectionList() {
    testSavedSearch = getSavedSavedSearch(savedSearchRepository);
    savedList.setSavedSearch(testSavedSearch);
    repo.save(savedList);
    SavedList result = repo.findSelectionList(testSavedSearch.getId(),
        savedList.getCreatedBy().getId()).orElse(null);
    assertNotNull(result);
    assertEquals(savedList.getId(), result.getId());
  }

  @Test
  public void testFindSelectionListFailNoSearch() {
    testSavedSearch = getSavedSavedSearch(savedSearchRepository);
    SavedList result = repo.findSelectionList(testSavedSearch.getId(),
        savedList.getCreatedBy().getId()).orElse(null);
    assertNull(result);
  }

  @Test
  public void testFindSelectionListFailUser() {
    testSavedSearch = getSavedSavedSearch(savedSearchRepository);
    SavedList result = repo.findSelectionList(testSavedSearch.getId(), null).orElse(null);
    assertNull(result);
  }

  @Test
  public void testFindRegisteredJobList() {
    savedList.setSfJobOpp(testSFJobOpp);
    repo.save(savedList);
    assert savedList.getSfJobOpp() != null;
    SavedList result = repo.findRegisteredJobList(savedList.getSfJobOpp().getSfId()).orElse(null);
    assertNotNull(result);
    assertEquals(savedList.getId(), result.getId());
  }

  @Test
  public void testFindRegisteredJobListFailNotRegistered() {
    repo.save(savedList);
    savedList.setRegisteredJob(false);
    SavedList result = repo.findRegisteredJobList(String.valueOf(testSFJobOpp.getId()))
        .orElse(null);
    assertNull(result);
  }

  @Test
  public void testFindRegisteredJobListFailId() {
    repo.save(savedList);
    // This is unusual to have a string for the sfJobLink
    SavedList result = repo.findRegisteredJobList("").orElse(null);
    assertNull(result);
  }

  @Test
  public void testFindByShortNameIgnoreCase() {
    SavedList result = repo.findByShortNameIgnoreCase(savedList.getTbbShortName()).orElse(null);
    assertNotNull(result);
    assertEquals(savedList.getId(), result.getId());
  }

  @Test
  public void testFindByShortNameIgnoreCaseFail() {
    SavedList result = repo.findByShortNameIgnoreCase("BOB").orElse(null);
    assertNull(result);
  }

  @Test
  public void testFindListsWithJobs() {
    savedList.setSfJobOpp(testSFJobOpp);
    repo.save(savedList);
    List<SavedList> result = repo.findListsWithJobs();
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(savedList.getId(), result.getFirst().getId());
  }

  @Test
  public void testFindListsWithJobsFail() {
    savedList.setStatus(Status.deleted);
    repo.save(savedList);
    List<SavedList> result = repo.findListsWithJobs();
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testFindListsWithJobsFailJobOppNull() {
    List<SavedList> result = repo.findListsWithJobs();
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }
}
