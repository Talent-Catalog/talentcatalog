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
import org.tctalent.server.model.db.HelpLink;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;
import org.tctalent.server.request.helplink.SearchHelpLinkRequest;

public class HelpLinkSettingsSpecificationIntTest extends BaseDBIntegrationTest {

  @Autowired
  private HelpLinkRepository repo;
  @Autowired
  private CountryRepository countryRepo;
  private HelpLink helpLink;
  private Specification<HelpLink> spec;
  private SearchHelpLinkRequest request;

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
  public void testKeyword() {
    request.setKeyword(helpLink.getLabel());
    spec = HelpLinkSettingsSpecification.buildSearchQuery(request);
    List<HelpLink> results = repo.findAll(spec);
    assertNotNull(results);
    assertFalse(results.isEmpty());
    assertEquals(1, results.size());
    assertEquals(helpLink.getId(), results.getFirst().getId());
  }

  @Test
  public void testKeywordWithLink() {
    request.setKeyword(helpLink.getLink());
    spec = HelpLinkSettingsSpecification.buildSearchQuery(request);
    List<HelpLink> results = repo.findAll(spec);
    assertNotNull(results);
    assertFalse(results.isEmpty());
    assertEquals(1, results.size());
    assertEquals(helpLink.getId(), results.getFirst().getId());
  }

  @Test
  public void testKeywordFail() {
    request.setKeyword("NOTHING");
    spec = HelpLinkSettingsSpecification.buildSearchQuery(request);
    List<HelpLink> results = repo.findAll(spec);
    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  @Test
  public void testWithCountry() {
    assert helpLink.getCountry() != null;
    request.setCountryId(helpLink.getCountry().getId());
    spec = HelpLinkSettingsSpecification.buildSearchQuery(request);
    List<HelpLink> results = repo.findAll(spec);
    assertNotNull(results);
    assertFalse(results.isEmpty());
    List<Long> ids = results.stream().map(HelpLink::getId).toList();
    assertTrue(ids.contains(helpLink.getId()));
  }

  @Test
  public void testNoCountry() {
    request.setCountryId(0L);
    spec = HelpLinkSettingsSpecification.buildSearchQuery(request);
    List<HelpLink> results = repo.findAll(spec);
    assertNotNull(results);
    assertTrue(results.isEmpty());
  }
}
