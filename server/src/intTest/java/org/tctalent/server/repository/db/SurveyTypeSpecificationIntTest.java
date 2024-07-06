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
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedSurveyType;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.tctalent.server.model.db.SurveyType;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;
import org.tctalent.server.request.survey.SearchSurveyTypeRequest;

public class SurveyTypeSpecificationIntTest extends BaseDBIntegrationTest {

  @Autowired
  private SurveyTypeRepository repo;
  private SurveyType st;
  private SearchSurveyTypeRequest request;
  private Specification<SurveyType> spec;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());
    st = getSavedSurveyType(repo);
    assertTrue(st.getId() > 0);

    request = new SearchSurveyTypeRequest();
  }

  @Test
  public void testStatus() {
    request.setStatus(st.getStatus());
    spec = SurveyTypeSpecification.buildSearchQuery(request);
    List<SurveyType> results = repo.findAll(spec);
    assertNotNull(results);
    assertFalse(results.isEmpty());
    List<Long> ids = results.stream().map(SurveyType::getId).toList();
    assertTrue(ids.contains(st.getId()));
  }

  @Test
  public void testKeyword() {
    request.setKeyword(st.getName());
    spec = SurveyTypeSpecification.buildSearchQuery(request);
    List<SurveyType> results = repo.findAll(spec);
    assertNotNull(results);
    assertFalse(results.isEmpty());
    assertEquals(1, results.size());
    assertEquals(st.getId(), results.getFirst().getId());
  }

  @Test
  public void testKeywordFail() {
    request.setKeyword("NOTHING");
    spec = SurveyTypeSpecification.buildSearchQuery(request);
    List<SurveyType> results = repo.findAll(spec);
    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

}
