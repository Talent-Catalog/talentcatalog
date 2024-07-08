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
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getIndustry;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedIndustry;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tctalent.server.model.db.Industry;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;

public class IndustryRepositoryIntTest extends BaseDBIntegrationTest {

  @Autowired
  private IndustryRepository repo;
  private Industry industry;

  @BeforeEach
  public void setUp() {
    assertTrue(isContainerInitialised());
    industry = getSavedIndustry(repo);
  }

  @Test
  public void testFindByStatus() {
    List<Industry> industries = repo.findByStatus(Status.active);
    assertNotNull(industries);
    assertFalse(industries.isEmpty());
    List<String> names = industries.stream().map(Industry::getName).toList();
    assertTrue(names.contains(industry.getName()));
  }

  @Test
  public void testFindByStatusFail() {
    Industry newIndustry = getIndustry();
    newIndustry.setStatus(Status.inactive);
    repo.save(newIndustry);
    assertTrue(newIndustry.getId() > 0);
    List<Industry> industries = repo.findByStatus(Status.active);
    assertNotNull(industries);
    assertFalse(industries.isEmpty());
    List<Long> ids = industries.stream().map(Industry::getId).toList();
    assertFalse(ids.contains(newIndustry.getId()));
  }

  @Test
  public void testFindByNameIgnoreCase() {
    String name = industry.getName().toUpperCase();
    Industry i = repo.findByNameIgnoreCase(name);
    assertNotNull(i);
    assertEquals(industry.getName(), i.getName());
  }
}
