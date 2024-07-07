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
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getCandidateReviewStatusItem;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedCandidate;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedSavedSearch;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedUser;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateReviewStatusItem;
import org.tctalent.server.model.db.ReviewStatus;
import org.tctalent.server.model.db.SavedSearch;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;

public class CandidateReviewStatusRepositoryIntTest extends BaseDBIntegrationTest {

  @Autowired
  private CandidateReviewStatusRepository repo;

  @Autowired
  private SavedSearchRepository savedSearchRepository;

  @Autowired
  private CandidateRepository candidateRepo;

  @Autowired
  private UserRepository userRepo;

  private SavedSearch testSavedSearch;
  private CandidateReviewStatusItem candidateReviewStatusItem;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());

    testSavedSearch = getSavedSavedSearch(savedSearchRepository);
    savedSearchRepository.save(testSavedSearch);
    assertTrue(testSavedSearch.getId() > 0);

    Candidate testCandidate = getSavedCandidate(candidateRepo, getSavedUser(userRepo));

    candidateReviewStatusItem = getCandidateReviewStatusItem();
    candidateReviewStatusItem.setSavedSearch(testSavedSearch);
    candidateReviewStatusItem.setCandidate(testCandidate);
    repo.save(candidateReviewStatusItem);
    assertTrue(candidateReviewStatusItem.getId() > 0);
  }

  @Test
  public void findReviewedCandidatesForSearch() {
    Set<Candidate> item = repo.findReviewedCandidatesForSearch(testSavedSearch.getId(),
        List.of(
            ReviewStatus.verified));
    assertNotNull(item);
    assertFalse(item.isEmpty());
    assertEquals(1, item.size());
    assertEquals(candidateReviewStatusItem.getCandidate().getId(), item.iterator().next().getId());
  }

  @Test
  public void findReviewedCandidatesForSearchFail() {
    Set<Candidate> item = repo.findReviewedCandidatesForSearch(testSavedSearch.getId(),
        List.of(ReviewStatus.unverified));
    assertNotNull(item);
    assertTrue(item.isEmpty());
  }
}
