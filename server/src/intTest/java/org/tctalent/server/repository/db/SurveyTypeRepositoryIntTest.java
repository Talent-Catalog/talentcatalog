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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedSurveyType;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.SurveyType;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;

public class SurveyTypeRepositoryIntTest extends BaseDBIntegrationTest {

  @Autowired
  private SurveyTypeRepository surveyTypeRepository;
  private SurveyType st;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());

    st = getSavedSurveyType(surveyTypeRepository);
    assertNotNull(st.getId());
    assertTrue(st.getId() > 0);
  }

  @Test
  public void testFindByStatus() {
    List<SurveyType> savedSurveyType = surveyTypeRepository.findByStatus(Status.active);
    assertNotNull(savedSurveyType);
    assertFalse(savedSurveyType.isEmpty());
    List<Long> resultIds = savedSurveyType.stream().map(SurveyType::getId).toList();
    assertTrue(resultIds.contains(st.getId()));
  }

  @Test
  public void testFindByStatusFail() {
    List<SurveyType> savedSurveyType = surveyTypeRepository.findByStatus(Status.deleted);
    assertNotNull(savedSurveyType);
    assertTrue(savedSurveyType.isEmpty());
  }

  @Test
  public void testGetNamesForIds() {
    List<Long> ids = Collections.singletonList(st.getId());
    List<String> savedNames = surveyTypeRepository.getNamesForIds(ids);
    assertNotNull(savedNames);
    assertFalse(savedNames.isEmpty());
    assertTrue(savedNames.contains(st.getName()));
  }

}
