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

package org.tctalent.server.repository.db

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.tctalent.server.model.db.Status
import org.tctalent.server.model.db.SurveyType
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest
import org.tctalent.server.repository.db.integrationhelp.getSavedSurveyType

class SurveyTypeRepositoryIntTest : BaseDBIntegrationTest() {
  @Autowired private lateinit var surveyTypeRepository: SurveyTypeRepository
  private lateinit var st: SurveyType

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }

    st = getSavedSurveyType(surveyTypeRepository)
    assertNotNull(st.id)
    assertTrue { st.id > 0 }
  }

  @Test
  fun `test find by status`() {

    val savedSurveyType = surveyTypeRepository.findByStatus(Status.active)
    assertNotNull(savedSurveyType)
    assertTrue { savedSurveyType.isNotEmpty() }
    val resultIds = savedSurveyType.map { it.id }
    assertTrue { resultIds.contains(st.id) }
  }

  @Test
  fun `test find by status fail`() {

    val savedSurveyType = surveyTypeRepository.findByStatus(Status.deleted)
    assertNotNull(savedSurveyType)
    assertTrue { savedSurveyType.isEmpty() }
  }

  @Test
  fun `test get names for ids`() {

    val ids = listOf(st.id)
    val savedNames = surveyTypeRepository.getNamesForIds(ids)
    assertNotNull(savedNames)
    assertTrue { savedNames.isNotEmpty() }
    assertTrue { savedNames.contains(st.name) }
  }
}
