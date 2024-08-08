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
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedEducationMajor;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.tctalent.server.model.db.EducationMajor;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;
import org.tctalent.server.request.education.major.SearchEducationMajorRequest;

public class EducationMajorSpecificationIntTest extends BaseDBIntegrationTest {

  @Autowired
  private EducationMajorRepository repo;
  private EducationMajor educationMajor;
  SearchEducationMajorRequest request;
  Specification<EducationMajor> spec;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());
    educationMajor = getSavedEducationMajor(repo);
    request = new SearchEducationMajorRequest();
  }

  @Test
  public void testKeyword() {
    request.setKeyword(educationMajor.getName());
    spec = EducationMajorSpecification.buildSearchQuery(request);
    List<EducationMajor> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(educationMajor.getId(), result.getFirst().getId());
  }

  @Test
  public void testKeywordFail() {
    request.setKeyword("NOTHING");
    spec = EducationMajorSpecification.buildSearchQuery(request);
    List<EducationMajor> result = repo.findAll(spec);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testStatus() {
    request.setStatus(Status.active);
    spec = EducationMajorSpecification.buildSearchQuery(request);
    List<EducationMajor> result = repo.findAll(spec);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    List<Long> ids = result.stream().map(EducationMajor::getId).toList();
    assertTrue(ids.contains(educationMajor.getId()));
  }

  @Test
  public void testStatusFail() {
    request.setStatus(Status.deleted);
    spec = EducationMajorSpecification.buildSearchQuery(request);
    List<EducationMajor> result = repo.findAll(spec);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }
}
