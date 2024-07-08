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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tctalent.server.model.db.Employer;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;

public class EmployerRepositoryIntTest extends BaseDBIntegrationTest {

  @Autowired
  private EmployerRepository employerRepository;
  private final String testDescription = "The description";
  private final String salesforceId = "salesforceId";

  @BeforeEach
  public void setUp() {
    assertTrue(isContainerInitialised());
    Employer employer = new Employer();
    employer.setDescription(testDescription);
    employer.setSfId(salesforceId);
    employerRepository.save(employer);
    assertTrue(employer.getId() > 0);
  }

  @Test
  public void testFindFirstBySfId() {
    Optional<Employer> savedEmployer = employerRepository.findFirstBySfId(salesforceId);
    assertNotNull(savedEmployer);
    assertTrue(savedEmployer.isPresent());
    assertEquals(savedEmployer.get().getDescription(), testDescription);
  }

  @Test
  public void testFindBySfIdFail() {
    Optional<Employer> savedEmployer = employerRepository.findFirstBySfId(salesforceId + "00");
    assertNotNull(savedEmployer);
    assertFalse(savedEmployer.isPresent());
  }
}
