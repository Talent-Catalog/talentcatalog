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
import org.tctalent.server.model.db.Country
import org.tctalent.server.model.db.Status
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest
import org.tctalent.server.repository.db.integrationhelp.getSavedCountry
import org.tctalent.server.request.country.SearchCountryRequest

class CountrySpecificationIntTest : BaseDBIntegrationTest() {
  @Autowired private lateinit var repo: CountryRepository
  private lateinit var country: Country

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    country = getSavedCountry(repo)
  }

  @Test
  fun `test keyword`() {
    val request = SearchCountryRequest().apply { keyword = country.name }
    val spec = CountrySpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(country.id, result.first().id)
  }

  @Test
  fun `test keyword fail`() {
    val request = SearchCountryRequest().apply { keyword = "NOTHING" }
    val spec = CountrySpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isEmpty() }
  }

  @Test
  fun `test  status`() {
    val request = SearchCountryRequest().apply { status = Status.active }
    val spec = CountrySpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isNotEmpty())
    val ids = result.map { it.id }
    assertTrue { ids.contains(country.id) }
  }

  @Test
  fun `test status fail`() {
    val request = SearchCountryRequest().apply { status = Status.deleted }
    val spec = CountrySpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isEmpty())
  }
}
