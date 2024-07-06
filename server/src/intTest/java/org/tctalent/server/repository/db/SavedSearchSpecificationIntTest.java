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
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedSearch;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedUser;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.tctalent.server.model.db.SavedSearch;
import org.tctalent.server.model.db.SavedSearchType;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;
import org.tctalent.server.request.search.SearchSavedSearchRequest;

public class SavedSearchSpecificationIntTest extends BaseDBIntegrationTest {

  @Autowired
  private SavedSearchRepository repo;

  @Autowired
  private UserRepository userRepository;

  private SavedSearch savedSearch;
  private User loggedInUser;
  private SearchSavedSearchRequest request;
  private Specification<SavedSearch> spec;


  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());
    savedSearch = repo.save(getSavedSearch());
    savedSearch.setDefaultSearch(false);
    loggedInUser = getSavedUser(userRepository);

    request = new SearchSavedSearchRequest();
  }

  @Test
  public void testKeyword() {
    request.setKeyword("Test");
    spec = SavedSearchSpecification.buildSearchQuery(request,
        loggedInUser);
    List<SavedSearch> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(savedSearch.getId(), result.getFirst().getId());
  }

  @Test
  public void testStatusFail() {
    request.setKeyword("NOTHING");
    spec = SavedSearchSpecification.buildSearchQuery(request,
        loggedInUser);
    List<SavedSearch> result = repo.findAll(spec);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testDefaultFail() {
    savedSearch.setDefaultSearch(true);
    repo.save(savedSearch);
    request.setKeyword("Test");
    spec = SavedSearchSpecification.buildSearchQuery(request,
        loggedInUser);
    List<SavedSearch> result = repo.findAll(spec);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testSearchType() {
    savedSearch.setType(SavedSearchType.job.name());
    repo.save(savedSearch);
    request.setSavedSearchType(
        SavedSearchType.job);
    spec = SavedSearchSpecification.buildSearchQuery(request,
        loggedInUser);
    List<SavedSearch> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(savedSearch.getId(), result.getFirst().getId());
  }

  @Test
  public void testSearchTypeFail() {
    savedSearch.setType(SavedSearchType.job.name());
    repo.save(savedSearch);
    request.setSavedSearchType(
        SavedSearchType.profession);
    spec = SavedSearchSpecification.buildSearchQuery(request,
        loggedInUser);
    List<SavedSearch> result = repo.findAll(spec);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testFixed() {
    savedSearch.setFixed(true);
    repo.save(savedSearch);
    request.setFixed(true);
    spec = SavedSearchSpecification.buildSearchQuery(request,
        loggedInUser);
    List<SavedSearch> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(savedSearch.getId(), result.getFirst().getId());
  }

  @Test
  public void testFixedFalse() {
    savedSearch.setDefaultSearch(true);
    repo.save(savedSearch);
    request.setFixed(false);
    spec = SavedSearchSpecification.buildSearchQuery(request,
        loggedInUser);
    List<SavedSearch> result = repo.findAll(spec);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testGlobal() {
    savedSearch.setGlobal(true);
    repo.save(savedSearch);
    request.setGlobal(true);
    spec = SavedSearchSpecification.buildSearchQuery(request,
        loggedInUser);
    List<SavedSearch> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(savedSearch.getId(), result.getFirst().getId());
  }

  @Test
  public void testGlobalFalse() {
    request.setKeyword("NOTHING");
    request.setGlobal(false);
    spec = SavedSearchSpecification.buildSearchQuery(request,
        loggedInUser);
    List<SavedSearch> result = repo.findAll(spec);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testOwned() {
    savedSearch.setCreatedBy(loggedInUser);
    repo.save(savedSearch);
    request.setOwned(true);
    spec = SavedSearchSpecification.buildSearchQuery(request,
        loggedInUser);
    List<SavedSearch> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(savedSearch.getId(), result.getFirst().getId());
  }

  @Test
  public void testOwnedFail() {
    savedSearch.setDefaultSearch(true);
    repo.save(savedSearch);
    request.setOwned(false);
    spec = SavedSearchSpecification.buildSearchQuery(request,
        loggedInUser);
    List<SavedSearch> result = repo.findAll(spec);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testOwnedNoLoggedInUser() {
    savedSearch.setDefaultSearch(true);
    repo.save(savedSearch);
    request.setOwned(false);
    spec = SavedSearchSpecification.buildSearchQuery(request, null);
    List<SavedSearch> result = repo.findAll(spec);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testShared() {
    loggedInUser.setSharedSearches(Set.of(savedSearch));
    userRepository.save(loggedInUser);
    request.setShared(true);
    spec = SavedSearchSpecification.buildSearchQuery(request,
        loggedInUser);
    List<SavedSearch> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(savedSearch.getId(), result.getFirst().getId());
  }

  @Test
  public void testSharedEmpty() {
    loggedInUser.setSharedSearches(Set.of());
    userRepository.save(loggedInUser);
    request.setShared(true);
    spec = SavedSearchSpecification.buildSearchQuery(request,
        loggedInUser);
    List<SavedSearch> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
  }

  @Test
  public void testSharedFalse() {
    savedSearch.setDefaultSearch(true);
    repo.save(savedSearch);
    request.setShared(false);
    spec = SavedSearchSpecification.buildSearchQuery(request,
        loggedInUser);
    List<SavedSearch> result = repo.findAll(spec);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }
}
