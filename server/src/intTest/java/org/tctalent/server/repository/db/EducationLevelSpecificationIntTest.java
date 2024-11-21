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
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedEducationLevel;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.tctalent.server.model.db.EducationLevel;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;
import org.tctalent.server.request.education.level.SearchEducationLevelRequest;

public class EducationLevelSpecificationIntTest extends BaseDBIntegrationTest {

  @Autowired
  private EducationLevelRepository repo;
  private EducationLevel educationLevel;
  SearchEducationLevelRequest request;
  Specification<EducationLevel> spec;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());
    educationLevel = getSavedEducationLevel(repo);
    request = new SearchEducationLevelRequest();
  }

  @Test
  public void testKeyword() {
    request.setKeyword(educationLevel.getName());
    spec = EducationLevelSpecification.buildSearchQuery(request);
    List<EducationLevel> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(educationLevel.getId(), result.getFirst().getId());
  }

  @Test
  public void testKeywordFail() {
    request.setKeyword("NOTHING");
    spec = EducationLevelSpecification.buildSearchQuery(request);
    List<EducationLevel> result = repo.findAll(spec);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testStatus() {
    request.setStatus(Status.deleted);
    spec = EducationLevelSpecification.buildSearchQuery(request);
    List<EducationLevel> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    List<Long> ids = result.stream().map(EducationLevel::getId).toList();
    assertTrue(ids.contains(educationLevel.getId()));
  }
}
