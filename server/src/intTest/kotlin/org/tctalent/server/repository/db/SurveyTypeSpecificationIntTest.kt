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

import kotlin.test.*
import org.springframework.beans.factory.annotation.Autowired
import org.tctalent.server.model.db.SurveyType
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest
import org.tctalent.server.repository.db.integrationhelp.getSavedSurveyType
import org.tctalent.server.request.survey.SearchSurveyTypeRequest

class SurveyTypeSpecificationIntTest : BaseDBIntegrationTest() {
  @Autowired private lateinit var repo: SurveyTypeRepository
  private lateinit var st: SurveyType

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }

    st = getSavedSurveyType(repo)
    assertNotNull(st.id)
    assertTrue { st.id > 0 }
  }

  @Test
  fun `test get status`() {
    val request = SearchSurveyTypeRequest().apply { status = st.status }
    val spec = SurveyTypeSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    val resultIds = result.map { it.id }
    assertTrue { resultIds.contains(st.id) }
  }

  @Test
  fun `test keyword`() {
    val request = SearchSurveyTypeRequest().apply { keyword = st.name }
    val spec = SurveyTypeSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(st.id, result.first().id)
  }
}
