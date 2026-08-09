/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.integration.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.tctalent.server.integration.helper.TestDataFactory.createAndSaveCountry;
import static org.tctalent.server.integration.helper.TestDataFactory.createCandidate;
import static org.tctalent.server.integration.helper.TestDataFactory.createUser;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.tctalent.server.integration.helper.BaseJpaIntegrationTest;
import org.tctalent.server.integration.helper.PostgresTestContainer;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateStatus;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.CountryRepository;
import org.tctalent.server.repository.db.UserRepository;

/**
 * Integration tests for CandidateRepository, in particular verifying that name searches
 * correctly exclude deleted candidates.
 * <p/>
 * Regression coverage for a bug (TC-1396) where the `AND c.status &lt;&gt; 'deleted'`
 * clause was appended after a chain of un-parenthesized `OR` name conditions, so it only
 * bound to the last `OR` branch and deleted candidates could still be matched by first
 * name, last name, or normal full name.
 */
public class CandidateRepositoryIntegrationTest extends BaseJpaIntegrationTest {

  @Autowired
  private CandidateRepository candidateRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private CountryRepository countryRepository;

  private Set<Country> sourceCountries;
  private Candidate activeCandidate;
  private Candidate deletedCandidate;

  @BeforeAll
  public static void setup() throws IOException, InterruptedException {
    PostgresTestContainer.startContainer();
  }

  /**
   * Creates an active and a deleted candidate sharing the same first/last name and source
   * country, so that name searches can be checked for correct exclusion of the deleted one.
   */
  @BeforeEach
  void setUp() {
    assertTrue(isContainerInitialised(), "Database container should be initialized");

    Country country = createAndSaveCountry(countryRepository);
    sourceCountries = Set.of(country);

    activeCandidate = saveNamedCandidate("active-user", CandidateStatus.active, country);
    deletedCandidate = saveNamedCandidate("deleted-user", CandidateStatus.deleted, country);
  }

  private Candidate saveNamedCandidate(String userSuffix, CandidateStatus status, Country country) {
    User user = createUser(null);
    user.setUsername(userSuffix);
    user.setEmail(userSuffix + "@email.com");
    user.setFirstName("Maria");
    user.setLastName("Santos");
    user = userRepository.save(user);

    Candidate candidate = createCandidate();
    candidate.setUser(user);
    candidate.setCountry(country);
    candidate.setStatus(status);
    candidate.setCreatedBy(user);
    return candidateRepository.save(candidate);
  }

  @Test
  void shouldExcludeDeletedCandidateWhenSearchingByFullName() {
    Page<Candidate> results = candidateRepository.searchCandidateName(
        "%maria santos%", sourceCountries, PageRequest.of(0, 10));

    assertMatchesOnlyActiveCandidate(results);
  }

  @Test
  void shouldExcludeDeletedCandidateWhenSearchingByFirstNameOnly() {
    Page<Candidate> results = candidateRepository.searchCandidateName(
        "%maria%", sourceCountries, PageRequest.of(0, 10));

    assertMatchesOnlyActiveCandidate(results);
  }

  @Test
  void shouldExcludeDeletedCandidateWhenSearchingByLastNameOnly() {
    Page<Candidate> results = candidateRepository.searchCandidateName(
        "%santos%", sourceCountries, PageRequest.of(0, 10));

    assertMatchesOnlyActiveCandidate(results);
  }

  @Test
  void shouldExcludeDeletedCandidateWhenSearchingByReversedFullName() {
    Page<Candidate> results = candidateRepository.searchCandidateName(
        "%santos maria%", sourceCountries, PageRequest.of(0, 10));

    assertMatchesOnlyActiveCandidate(results);
  }

  private void assertMatchesOnlyActiveCandidate(Page<Candidate> results) {
    List<Long> ids = results.getContent().stream().map(Candidate::getId).toList();
    assertEquals(1, ids.size(), "Should find exactly one (non-deleted) candidate");
    assertTrue(ids.contains(activeCandidate.getId()), "Active candidate should be found");
    assertTrue(!ids.contains(deletedCandidate.getId()), "Deleted candidate should not be found");
  }
}
