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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getCandidateVisaCheck;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getCandidateVisaJobCheck;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedCandidate;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedCountry;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedSfJobOpp;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedUser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateVisaCheck;
import org.tctalent.server.model.db.CandidateVisaJobCheck;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;

public class CandidateVisaJobRepositoryIntTest extends BaseDBIntegrationTest {

  @Autowired
  private CandidateVisaJobRepository repo;
  @Autowired
  private UserRepository userRepo;
  @Autowired
  private CandidateRepository candidateRepo;
  @Autowired
  private CountryRepository countryRepo;
  @Autowired
  private CandidateVisaRepository candidateVisaJobCheckRepository;
  @Autowired
  private SalesforceJobOppRepository salesforceJobOppRepository;
  private Candidate testCandidate;
  private CandidateVisaJobCheck candidateVisaJobCheck;
  private SalesforceJobOpp sfJobOpp;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());
    testCandidate = getSavedCandidate(candidateRepo, getSavedUser(userRepo));
    CandidateVisaCheck testCandidateVisaCheck = getCandidateVisaCheck();
    testCandidateVisaCheck.setCandidate(testCandidate);
    testCandidateVisaCheck.setCountry(getSavedCountry(countryRepo));
    candidateVisaJobCheckRepository.save(testCandidateVisaCheck);
    assertTrue(testCandidateVisaCheck.getId() > 0);

    sfJobOpp = getSavedSfJobOpp(salesforceJobOppRepository);

    candidateVisaJobCheck = getCandidateVisaJobCheck();
    candidateVisaJobCheck.setCandidateVisaCheck(testCandidateVisaCheck);
    candidateVisaJobCheck.setJobOpp(sfJobOpp);
    repo.save(candidateVisaJobCheck);
    assertTrue(candidateVisaJobCheck.getId() > 0);
  }

  @Test
  public void findByCandidateIdAndJobOppId() {
    CandidateVisaJobCheck cvjc = repo.findByCandidateIdAndJobOppId(testCandidate.getId(),
        sfJobOpp.getId());
    assertNotNull(cvjc);
    assertEquals(cvjc.getName(), candidateVisaJobCheck.getName());
  }

  @Test
  public void findByCandidateIdAndJobOppIdFailCandidateId() {
    CandidateVisaJobCheck cvjc = repo.findByCandidateIdAndJobOppId(99999999999L, sfJobOpp.getId());
    assertNull(cvjc);
  }

  @Test
  public void findByCandidateIdAndJobOppIdFailJobOpp() {
    CandidateVisaJobCheck cvjc = repo.findByCandidateIdAndJobOppId(testCandidate.getId(),
        9999999999L);
    assertNull(cvjc);
  }
}
