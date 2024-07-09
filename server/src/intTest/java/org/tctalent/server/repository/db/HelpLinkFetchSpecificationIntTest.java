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
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getHelpLink;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedCountry;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.tctalent.server.model.db.CandidateOpportunityStage;
import org.tctalent.server.model.db.HelpFocus;
import org.tctalent.server.model.db.HelpLink;
import org.tctalent.server.model.db.JobOpportunityStage;
import org.tctalent.server.model.db.NextStepInfo;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;
import org.tctalent.server.request.helplink.SearchHelpLinkRequest;

public class HelpLinkFetchSpecificationIntTest extends BaseDBIntegrationTest {

  @Autowired
  private HelpLinkRepository repo;
  @Autowired
  private CountryRepository countryRepo;
  private HelpLink helpLink;
  private SearchHelpLinkRequest request;
  private Specification<HelpLink> spec;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());
    helpLink = getHelpLink();
    helpLink.setCountry(getSavedCountry(countryRepo));
    repo.save(helpLink);
    assertTrue(helpLink.getId() > 0);

    request = new SearchHelpLinkRequest();
  }

  @Test
  public void testAddCountry() {
    helpLink.setCountry(getSavedCountry(countryRepo));
    repo.save(helpLink);

    assert helpLink.getCountry() != null;
    request.setCountryId(helpLink.getCountry().getId());
    spec = HelpLinkFetchSpecification.buildSearchQuery(request);
    List<HelpLink> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(helpLink.getId(), result.getFirst().getId());
  }

  @Test
  public void testAddJobStage() {
    helpLink.setJobStage(JobOpportunityStage.jobOffer);
    repo.save(helpLink);

    request.setJobStage(helpLink.getJobStage());
    spec = HelpLinkFetchSpecification.buildSearchQuery(request);
    List<HelpLink> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(helpLink.getId(), result.getFirst().getId());
  }

  @Test
  public void testAddJobStageFail() {
    helpLink.setJobStage(JobOpportunityStage.jobOffer);
    repo.save(helpLink);

    request.setJobStage(JobOpportunityStage.noJobOffer);
    spec = HelpLinkFetchSpecification.buildSearchQuery(request);
    List<HelpLink> result = repo.findAll(spec);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testAddCaseStage() {
    helpLink.setCaseStage(CandidateOpportunityStage.testing);
    repo.save(helpLink);

    request.setCaseStage(helpLink.getCaseStage());
    spec = HelpLinkFetchSpecification.buildSearchQuery(request);
    List<HelpLink> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(helpLink.getId(), result.getFirst().getId());
  }

  @Test
  public void testAddCaseStageFail() {
    helpLink.setCaseStage(CandidateOpportunityStage.testing);
    repo.save(helpLink);

    request.setCaseStage(CandidateOpportunityStage.cvPreparation);
    spec = HelpLinkFetchSpecification.buildSearchQuery(request);
    List<HelpLink> result = repo.findAll(spec);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testAddFocus() {
    helpLink.setFocus(HelpFocus.updateStage);
    repo.save(helpLink);

    request.setFocus(helpLink.getFocus());
    spec = HelpLinkFetchSpecification.buildSearchQuery(request);
    List<HelpLink> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(helpLink.getId(), result.getFirst().getId());
  }

  @Test
  public void testAddFocusFail() {
    helpLink.setFocus(HelpFocus.updateStage);
    repo.save(helpLink);

    request.setFocus(HelpFocus.updateNextStep);
    spec = HelpLinkFetchSpecification.buildSearchQuery(request);
    List<HelpLink> result = repo.findAll(spec);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testAddNextStepName() {
    NextStepInfo nextStepInfo = new NextStepInfo();
    nextStepInfo.setNextStepName("STOP");
    helpLink.setNextStepInfo(nextStepInfo);
    repo.save(helpLink);

    assert helpLink.getNextStepInfo() != null;
    request.setNextStepName(helpLink.getNextStepInfo().getNextStepName());
    spec = HelpLinkFetchSpecification.buildSearchQuery(request);
    List<HelpLink> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(helpLink.getId(), result.getFirst().getId());
  }

  @Test
  public void testAddNextStepNameFail() {
    NextStepInfo nextStepInfo = new NextStepInfo();
    nextStepInfo.setNextStepName("STOP");
    helpLink.setNextStepInfo(nextStepInfo);
    repo.save(helpLink);

    request.setNextStepName("GO");
    spec = HelpLinkFetchSpecification.buildSearchQuery(request);
    List<HelpLink> result = repo.findAll(spec);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }
}
