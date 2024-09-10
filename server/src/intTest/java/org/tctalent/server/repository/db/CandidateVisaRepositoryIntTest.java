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
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getCandidateVisaCheck;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedCandidate;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedCountry;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedUser;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateVisaCheck;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;

public class CandidateVisaRepositoryIntTest extends BaseDBIntegrationTest {

  @Autowired
  private CandidateVisaRepository repo;
  @Autowired
  private CandidateRepository candidateRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private CountryRepository countryRepository;
  private Candidate testCandidate;
  private CandidateVisaCheck candidateVisaCheck;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());
    testCandidate = getSavedCandidate(candidateRepository, getSavedUser(userRepository));
    candidateVisaCheck = getCandidateVisaCheck();
    candidateVisaCheck.setCandidate(testCandidate);
    candidateVisaCheck.setCountry(getSavedCountry(countryRepository));
    repo.save(candidateVisaCheck);
    assertTrue(candidateVisaCheck.getId() > 0);
  }

  @Test
  public void testFindByCandidateId() {
    List<CandidateVisaCheck> visaChecks = repo.findByCandidateId(testCandidate.getId());
    assertNotNull(visaChecks);
    assertFalse(visaChecks.isEmpty());
    assertEquals(1, visaChecks.size());
    List<Long> ids = visaChecks.stream().map(CandidateVisaCheck::getId).toList();
    assertTrue(ids.contains(candidateVisaCheck.getId()));
  }

  @Test
  public void testFindByCandidateIdAndCountryId() {
    Optional<CandidateVisaCheck> visaChecks = repo.findByCandidateIdCountryId(testCandidate.getId(),
        candidateVisaCheck.getCountry().getId());
    assertTrue(visaChecks.isPresent());
    assertEquals(candidateVisaCheck.getId(), visaChecks.get().getId());
  }

  @Test
  public void testFindByCandidateIdAndCountryIdFail() {
    Optional<CandidateVisaCheck> visaChecks = repo.findByCandidateIdCountryId(testCandidate.getId(),
        null);
    assertFalse(visaChecks.isPresent());
  }
}
