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
import org.tctalent.server.model.db.PartnerImpl
import org.tctalent.server.model.db.Status
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest
import org.tctalent.server.repository.db.integrationhelp.getSavedPartner
import org.tctalent.server.request.partner.SearchPartnerRequest

class PartnerSpecificationIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repo: PartnerRepository
  private lateinit var partner: PartnerImpl

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    partner = getSavedPartner(repo)
  }

  @Test
  fun `test build search query with status`() {
    val request = SearchPartnerRequest().apply { status = partner.status }
    val spec = PartnerSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isNotEmpty())
    val ids = result.map { it.id }
    assertTrue(ids.contains(partner.id))
  }

  @Test
  fun `test build search query with status fail`() {
    val request = SearchPartnerRequest().apply { status = Status.deleted }
    val spec = PartnerSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isEmpty())
  }

  @Test
  fun `test keyword`() {
    val request = SearchPartnerRequest().apply { keyword = partner.name }
    val spec = PartnerSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isNotEmpty())
    assertEquals(1, result.size)
    assertEquals(partner.id, result.first().id)
  }

  @Test
  fun `test non matching keyword`() {
    val request = SearchPartnerRequest().apply { keyword = "NonMatching" }
    val spec = PartnerSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isEmpty())
  }

  @Test
  fun `test matching keyword abbreviation`() {
    val request = SearchPartnerRequest().apply { keyword = partner.abbreviation }
    val spec = PartnerSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isNotEmpty())
    assertEquals(1, result.size)
    assertEquals(partner.id, result.first().id)
  }

  @Test
  fun `test job creator fail`() {
    val request = SearchPartnerRequest().apply { jobCreator = !partner.isJobCreator }
    val spec = PartnerSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isNotEmpty())
    val ids = result.map { it.id }

    // Note, do not expect the result to be in this list.
    assertFalse(ids.contains(partner.id))
  }

  @Test
  fun `test source partner`() {
    val request = SearchPartnerRequest().apply { sourcePartner = partner.isSourcePartner }
    val spec = PartnerSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isNotEmpty())
    val ids = result.map { it.id }
    assertTrue(ids.contains(partner.id))
  }
}
