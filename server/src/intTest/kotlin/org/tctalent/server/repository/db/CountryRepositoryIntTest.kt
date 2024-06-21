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

import java.util.*
import kotlin.test.*
import org.springframework.beans.factory.annotation.Autowired
import org.tctalent.server.model.db.Country
import org.tctalent.server.model.db.Status
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest
import org.tctalent.server.repository.db.integrationhelp.getCountry
import org.tctalent.server.repository.db.integrationhelp.getSavedCountry

class CountryRepositoryIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repo: CountryRepository
  private lateinit var country: Country

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    country = getSavedCountry(repo)
  }

  @Test
  fun `test find by status`() {
    val c = repo.findByStatus(Status.active)
    assertNotNull(c)
    assertTrue { c.isNotEmpty() }
    val names = c.map { it.name }
    assertTrue { names.contains(country.name) }
  }

  @Test
  fun `test find by status fail`() {
    val newCountry = getCountry().apply { status = Status.inactive }
    repo.save(newCountry)
    assertTrue { newCountry.id > 0 }
    val c = repo.findByStatus(Status.active)
    assertNotNull(c)
    assertTrue { c.isNotEmpty() }
    val ids = c.map { it.id }
    assertFalse { ids.contains(newCountry.id) }
  }

  @Test
  fun `test find by name ignore case`() {
    val name = country.name.uppercase(Locale.getDefault())
    val c = repo.findByNameIgnoreCase(name)
    assertNotNull(c)
    assertEquals(country.isoCode, c.isoCode)
  }

  @Test
  fun `test find by name deleted`() {
    // The function actually has an extra clause in it, so checking that one here.
    val newCountry = getCountry().apply { status = Status.deleted }
    repo.save(newCountry)
    assertTrue { newCountry.id > 0 }
    val c = repo.findByNameIgnoreCase(newCountry.name)
    assertNotNull(c)
    assertEquals(country.id, c.id)
  }

  @Test
  fun `test get names for ids`() {
    val newCountry = getCountry().apply { status = Status.deleted }
    repo.save(newCountry)
    assertTrue { newCountry.id > 0 }
    val countries = repo.getNamesForIds(listOf(newCountry.id, country.id))
    assertNotNull(countries)
    assertEquals(2, countries.size)
  }

  @Test
  fun `test get names for ids fail`() {
    val newCountry = getCountry().apply { status = Status.deleted }
    repo.save(newCountry)
    assertTrue { newCountry.id > 0 }
    val countries = repo.getNamesForIds(listOf(newCountry.id))
    assertNotNull(countries)
    assertEquals(1, countries.size)
  }

  @Test
  fun `test find by status and source countries`() {
    val newCountry = getCountry().apply { status = Status.deleted }
    repo.save(newCountry)
    assertTrue { newCountry.id > 0 }
    val countries = repo.findByStatusAndSourceCountries(Status.deleted, setOf(country, newCountry))
    assertNotNull(countries)
    assertEquals(1, countries.size)
    val ids = countries.map { it.id }
    assertTrue { ids.contains(newCountry.id) }
  }
}
