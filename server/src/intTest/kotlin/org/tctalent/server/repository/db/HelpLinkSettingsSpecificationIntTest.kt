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
import org.tctalent.server.model.db.HelpLink
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest
import org.tctalent.server.repository.db.integrationhelp.getHelpLink
import org.tctalent.server.repository.db.integrationhelp.getSavedCountry
import org.tctalent.server.request.helplink.SearchHelpLinkRequest

class HelpLinkSettingsSpecificationIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repo: HelpLinkRepository
  @Autowired lateinit var countryRepo: CountryRepository
  lateinit var helpLink: HelpLink

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    helpLink = getHelpLink().apply { country = getSavedCountry(countryRepo) }
    repo.save(helpLink)
    assertTrue { helpLink.id > 0 }
  }

  @Test
  fun `test keyword`() {
    val request = SearchHelpLinkRequest().apply { keyword = helpLink.label }
    val spec = HelpLinkSettingsSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(helpLink.id, result.first().id)
  }

  @Test
  fun `test keyword with link`() {
    val request = SearchHelpLinkRequest().apply { keyword = helpLink.link }
    val spec = HelpLinkSettingsSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(helpLink.id, result.first().id)
  }

  @Test
  fun `test keyword fail`() {
    val request = SearchHelpLinkRequest().apply { keyword = "NOTHING" }
    val spec = HelpLinkSettingsSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isEmpty() }
  }

  @Test
  fun `test with country`() {
    val request = SearchHelpLinkRequest().apply { countryId = helpLink.country?.id ?: fail() }
    val spec = HelpLinkSettingsSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isNotEmpty())
    val ids = result.map { it.id }
    assertTrue { ids.contains(helpLink.id) }
  }

  @Test
  fun `test no country`() {
    val request = SearchHelpLinkRequest().apply { countryId = 0 }
    val spec = HelpLinkSettingsSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isEmpty())
  }
}
