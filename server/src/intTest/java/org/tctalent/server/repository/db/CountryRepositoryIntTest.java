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
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getCountry;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedCountry;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;

public class CountryRepositoryIntTest extends BaseDBIntegrationTest {

  @Autowired
  private CountryRepository repo;
  private Country country;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());
    country = getSavedCountry(repo);
  }

  @Test
  public void testFindByStatus() {
    List<Country> c = repo.findByStatus(Status.active);
    assertNotNull(c);
    assertFalse(c.isEmpty());
    List<String> names = c.stream().map(Country::getName).toList();
    assertTrue(names.contains(country.getName()));
  }

  @Test
  public void testFindByStatusFail() {
    Country newCountry = getCountry();
    newCountry.setStatus(Status.inactive);
    repo.save(newCountry);
    assertTrue(newCountry.getId() > 0);
    List<Country> c = repo.findByStatus(Status.active);
    assertNotNull(c);
    assertFalse(c.isEmpty());
    List<Long> ids = c.stream().map(Country::getId).toList();
    assertFalse(ids.contains(newCountry.getId()));
  }

  @Test
  public void testFindByNameIgnoreCase() {
    String name = country.getName().toUpperCase(Locale.getDefault());
    Country c = repo.findByNameIgnoreCase(name);
    assertNotNull(c);
    assertEquals(country.getIsoCode(), c.getIsoCode());
  }

  @Test
  public void testFindByNameDeleted() {
    // The function actually has an extra clause in it, so checking that one here.
    Country newCountry = getCountry();
    newCountry.setStatus(Status.deleted);
    repo.save(newCountry);
    assertTrue(newCountry.getId() > 0);
    Country c = repo.findByNameIgnoreCase(newCountry.getName());
    assertNotNull(c);
    assertEquals(country.getId(), c.getId());
  }

  @Test
  public void testGetNamesForIds() {
    Country newCountry = getCountry();
    newCountry.setStatus(Status.deleted);
    repo.save(newCountry);
    assertTrue(newCountry.getId() > 0);
    List<String> countries = repo.getNamesForIds(List.of(newCountry.getId(), country.getId()));
    assertNotNull(countries);
    assertEquals(2, countries.size());
  }

  @Test
  public void testGetNamesForIdsFail() {
    Country newCountry = getCountry();
    newCountry.setStatus(Status.deleted);
    repo.save(newCountry);
    assertTrue(newCountry.getId() > 0);
    List<String> countries = repo.getNamesForIds(List.of(newCountry.getId()));
    assertNotNull(countries);
    assertEquals(1, countries.size());
  }

  @Test
  public void testFindByStatusAndSourceCountries() {
    Country newCountry = getCountry();
    newCountry.setStatus(Status.deleted);
    repo.save(newCountry);
    assertTrue(newCountry.getId() > 0);
    List<Country> countries = repo.findByStatusAndSourceCountries(Status.deleted,
        Set.of(country, newCountry));
    assertNotNull(countries);
    assertEquals(1, countries.size());
    List<Long> ids = countries.stream().map(Country::getId).toList();
    assertTrue(ids.contains(newCountry.getId()));
  }
}
