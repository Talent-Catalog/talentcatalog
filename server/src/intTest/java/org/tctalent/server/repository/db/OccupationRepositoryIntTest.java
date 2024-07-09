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
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getOccupation;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedOccupation;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tctalent.server.model.db.Occupation;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;

public class OccupationRepositoryIntTest extends BaseDBIntegrationTest {

  @Autowired
  private OccupationRepository repo;
  private Occupation occupation;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());
    occupation = getSavedOccupation(repo);
  }

  @Test
  public void testFindByStatus() {
    List<Occupation> levels = repo.findByStatus(Status.active);
    assertNotNull(levels);
    assertTrue(levels.size() > 0);
    List<String> names = levels.stream().map(Occupation::getName).collect(Collectors.toList());
    assertTrue(names.contains(occupation.getName()));
  }

  @Test
  public void testFindByStatusFail() {
    Occupation newOcc = getOccupation();
    newOcc.setStatus(Status.inactive);
    repo.save(newOcc);
    assertTrue(newOcc.getId() > 0);
    List<Occupation> savedIndustry = repo.findByStatus(Status.active);
    assertNotNull(savedIndustry);
    assertTrue(savedIndustry.size() > 0);
    List<Long> ids = savedIndustry.stream().map(Occupation::getId).collect(Collectors.toList());
    assertFalse(ids.contains(newOcc.getId()));
  }

  @Test
  public void testFindByNameIgnoreCase() {
    String name = occupation.getName().toUpperCase(Locale.getDefault());
    Occupation i = repo.findByNameIgnoreCase(name);
    assertNotNull(i);
    assertEquals(occupation.getName(), i.getName());
  }

  @Test
  public void testGetNamesForIds() {
    Occupation occ1 = getOccupation();
    repo.save(occ1);
    Occupation occ2 = getOccupation();
    repo.save(occ2);
    List<Long> ids = List.of(occ1.getId(), occupation.getId(), occ2.getId());
    List<String> names = repo.getNamesForIds(ids);
    assertEquals(3, names.size());
    assertTrue(names.stream().allMatch(name -> name.startsWith("TEST_OCCUPATION")));
  }
}
