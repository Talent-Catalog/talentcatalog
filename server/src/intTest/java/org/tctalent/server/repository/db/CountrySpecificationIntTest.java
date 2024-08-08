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
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedCountry;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;
import org.tctalent.server.request.country.SearchCountryRequest;

public class CountrySpecificationIntTest extends BaseDBIntegrationTest {

  @Autowired
  private CountryRepository repo;
  private Country country;
  SearchCountryRequest request;
  Specification<Country> spec;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());
    country = getSavedCountry(repo);
    request = new SearchCountryRequest();
  }

  @Test
  public void testKeyword() {
    SearchCountryRequest request = new SearchCountryRequest();
    request.setKeyword(country.getName());
    spec = CountrySpecification.buildSearchQuery(request);
    List<Country> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(country.getId(), result.getFirst().getId());
  }

  @Test
  public void testKeywordFail() {
    request.setKeyword("NOTHING");
    spec = CountrySpecification.buildSearchQuery(request);
    List<Country> result = repo.findAll(spec);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testStatus() {
    request.setStatus(Status.active);
    spec = CountrySpecification.buildSearchQuery(request);
    List<Country> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    List<Long> ids = result.stream().map(Country::getId).toList();
    assertTrue(ids.contains(country.getId()));
  }

  @Test
  public void testStatusFail() {
    request.setStatus(Status.deleted);
    spec = CountrySpecification.buildSearchQuery(request);
    List<Country> result = repo.findAll(spec);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }
}
