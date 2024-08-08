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
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedPartner;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;
import org.tctalent.server.request.partner.SearchPartnerRequest;

public class PartnerSpecificationIntTest extends BaseDBIntegrationTest {

  @Autowired
  private PartnerRepository repo;
  private PartnerImpl partner;
  private SearchPartnerRequest request;
  private Specification<PartnerImpl> spec;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());
    partner = getSavedPartner(repo);

    request = new SearchPartnerRequest();
  }

  @Test
  public void testBuildSearchQueryWithStatus() {
    request.setStatus(partner.getStatus());
    spec = PartnerSpecification.buildSearchQuery(request);
    List<PartnerImpl> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    List<Long> ids = result.stream().map(PartnerImpl::getId).toList();
    assertTrue(ids.contains(partner.getId()));
  }

  @Test
  public void testBuildSearchQueryWithStatusFail() {
    request.setStatus(Status.deleted);
    spec = PartnerSpecification.buildSearchQuery(request);
    List<PartnerImpl> result = repo.findAll(spec);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testKeyword() {
    request.setKeyword(partner.getName());
    spec = PartnerSpecification.buildSearchQuery(request);
    List<PartnerImpl> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(partner.getId(), result.getFirst().getId());
  }

  @Test
  public void testNonMatchingKeyword() {
    request.setKeyword("NonMatching");
    spec = PartnerSpecification.buildSearchQuery(request);
    List<PartnerImpl> result = repo.findAll(spec);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testMatchingKeywordAbbreviation() {
    request.setKeyword(partner.getAbbreviation());
    spec = PartnerSpecification.buildSearchQuery(request);
    List<PartnerImpl> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(partner.getId(), result.getFirst().getId());
  }

  @Test
  public void testJobCreatorFail() {
    request.setJobCreator(!partner.isJobCreator());
    spec = PartnerSpecification.buildSearchQuery(request);
    List<PartnerImpl> result = repo.findAll(spec);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testSourcePartner() {
    request.setSourcePartner(partner.isSourcePartner());
    spec = PartnerSpecification.buildSearchQuery(request);
    List<PartnerImpl> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    List<Long> ids = result.stream().map(PartnerImpl::getId).toList();
    assertTrue(ids.contains(partner.getId()));
  }
}
