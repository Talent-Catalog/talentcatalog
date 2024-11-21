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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getPartner;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedCountry;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedPartner;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;

public class PartnerRepositoryIntTest extends BaseDBIntegrationTest {

  @Autowired
  private PartnerRepository repo;
  @Autowired
  private CountryRepository countryRepository;
  private PartnerImpl partner;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());
    partner = getSavedPartner(repo);
  }

  @Test
  public void testGetNamesForIds() {
    PartnerImpl newPartner = getPartner();
    newPartner.setName("TEST PARTNER");
    repo.save(newPartner);
    List<String> names = repo.getNamesForIds(List.of(newPartner.getId(), partner.getId()));
    assertNotNull(names);
    assertFalse(names.isEmpty());
    assertTrue(names.stream().allMatch(name -> name.contains("TEST")));
    // also test ordering as it needs to be asc
    assertEquals(newPartner.getName(), "TEST PARTNER");
  }

  @Test
  public void testFindByStatusOrderByName() {
    partner.setName("ZIGGY");
    partner.setStatus(Status.deleted);
    repo.save(partner);

    PartnerImpl newPartner = getPartner();
    newPartner.setName("AZZY");
    newPartner.setStatus(Status.deleted);
    repo.save(newPartner);

    List<PartnerImpl> results = repo.findByStatusOrderByName(Status.deleted);
    assertNotNull(results);
    assertFalse(results.isEmpty());
    assertEquals(2, results.size());
    assertEquals(newPartner, results.getFirst());
  }

  @Test
  public void testFindByAbbreviation() {
    Optional<PartnerImpl> result = repo.findByAbbreviation(partner.getAbbreviation());
    assertTrue(result.isPresent());
    assertEquals(partner.getId(), result.get().getId());
  }

  @Test
  public void testFindByAbbreviationNone() {
    Optional<PartnerImpl> result = repo.findByAbbreviation("NONE");
    assertFalse(result.isPresent());
  }

  @Test
  public void testFindByDefaultSourcePartner() {
    Optional<PartnerImpl> result = repo.findByDefaultSourcePartner(true);
    assertTrue(result.isPresent());
    assertNotEquals(partner.getId(), result.get().getId());
  }

  @Test
  public void testFindSourcePartnerByAutoassignableCountry() {
    Country country = getSavedCountry(countryRepository);
    partner.setSourceCountries(new HashSet<>(Collections.singletonList(country)));
    repo.save(partner);

    List<PartnerImpl> result = repo.findSourcePartnerByAutoassignableCountry(country);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(1, result.getFirst().getSourceCountries().size());
    assertEquals(country.getId(), result.getFirst().getSourceCountries().iterator().next().getId());
  }
}
