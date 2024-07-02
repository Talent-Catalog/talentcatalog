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

import kotlin.jvm.optionals.getOrNull
import kotlin.test.*
import org.springframework.beans.factory.annotation.Autowired
import org.tctalent.server.model.db.PartnerImpl
import org.tctalent.server.model.db.Status
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest
import org.tctalent.server.repository.db.integrationhelp.getPartner
import org.tctalent.server.repository.db.integrationhelp.getSavedCountry
import org.tctalent.server.repository.db.integrationhelp.getSavedPartner

class PartnerRepositoryIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repo: PartnerRepository
  @Autowired lateinit var countryRepository: CountryRepository
  private lateinit var partner: PartnerImpl

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    partner = getSavedPartner(repo)
  }

  @Test
  fun `test get names for ids`() {
    val newPartner = getPartner().apply { name = "TEST PARTNER" }
    repo.save(newPartner)
    val names = repo.getNamesForIds(listOf(newPartner.id, partner.id))
    assertNotNull(names)
    assertTrue { names.isNotEmpty() }
    assertTrue { names.all { it.contains("TEST") } }
    // also test ordering as it needs to be asc
    assertEquals(newPartner.name, "TEST PARTNER")
  }

  @Test
  fun `test find by status order by name`() {
    repo.save(
      partner.apply {
        name = "ZIGGY"
        status = Status.deleted
      }
    )
    val newPartner =
      getPartner().apply {
        name = "AZZY"
        status = Status.deleted
      }
    repo.save(newPartner)

    val results = repo.findByStatusOrderByName(Status.deleted)
    assertNotNull(results)
    assertTrue(results.isNotEmpty())
    assertEquals(2, results.count())
    assertEquals(newPartner, results[0])
  }

  @Test
  fun `test find by abbreviation`() {
    val result = repo.findByAbbreviation(partner.abbreviation).getOrNull()
    assertNotNull(result)
    assertEquals(partner.id, result.id)
  }

  @Test
  fun `test find by abbreviation none`() {
    val result = repo.findByAbbreviation("NONE").getOrNull()
    assertNull(result)
  }

  @Test
  fun `test find by default source partner`() {
    val result = repo.findByDefaultSourcePartner(true).getOrNull()
    assertNotNull(result)
    assertNotEquals(partner.id, result.id)
  }

  @Test
  fun `test find source partner by auto assignable country`() {
    val country = getSavedCountry(countryRepository)
    repo.save(partner.apply { sourceCountries = mutableSetOf(country) })

    val result = repo.findSourcePartnerByAutoassignableCountry(country)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(1, result.first().sourceCountries.size)
    assertEquals(country.id, result.first().sourceCountries.first().id)
  }
}
