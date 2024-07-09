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
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedIndustry;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.tctalent.server.model.db.Industry;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;
import org.tctalent.server.request.industry.SearchIndustryRequest;

public class IndustrySpecificationIntTest extends BaseDBIntegrationTest {

  @Autowired
  private IndustryRepository repo;
  private Industry industry;
  private Specification<Industry> spec;
  private SearchIndustryRequest request;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());
    industry = getSavedIndustry(repo);

    request = new SearchIndustryRequest();
  }

  @Test
  public void testKeyword() {
    request.setKeyword(industry.getName());
    spec = IndustrySpecification.buildSearchQuery(request);
    List<Industry> results = repo.findAll(spec);
    assertNotNull(results);
    assertFalse(results.isEmpty());
    assertEquals(1, results.size());
    assertEquals(industry.getId(), results.getFirst().getId());
  }


  @Test
  public void testKeywordFail() {
    request.setKeyword("NOTHING");
    spec = IndustrySpecification.buildSearchQuery(request);
    List<Industry> results = repo.findAll(spec);
    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  @Test
  public void testWithStatus() {
    request.setStatus(Status.active);
    spec = IndustrySpecification.buildSearchQuery(request);
    List<Industry> results = repo.findAll(spec);
    assertNotNull(results);
    assertFalse(results.isEmpty());
    List<Long> ids = results.stream().map(Industry::getId).toList();
    assertTrue(ids.contains(industry.getId()));
  }

  @Test
  public void testWithStatusFail() {
    request.setStatus(Status.deleted);
    spec = IndustrySpecification.buildSearchQuery(request);
    List<Industry> results = repo.findAll(spec);
    assertNotNull(results);
    assertTrue(results.isEmpty());
  }
}
