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

import org.springframework.beans.factory.annotation.Autowired
import org.tctalent.server.model.db.Industry
import org.tctalent.server.model.db.Status
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest
import org.tctalent.server.repository.db.integrationhelp.getIndustry
import org.tctalent.server.repository.db.integrationhelp.getSavedIndustry
import java.util.*
import kotlin.test.*

class IndustryRepositoryIntTest: BaseDBIntegrationTest() {
    @Autowired private lateinit var repo: IndustryRepository
    private lateinit var industry: Industry

    @BeforeTest
    fun setup() {
        assertTrue { isContainerInitialized() }
        industry = getSavedIndustry(repo)
    }

    @Test
    fun `test find by status`() {
        val industries = repo.findByStatus(Status.active)
        assertNotNull(industries)
        assertTrue { industries.isNotEmpty() }
        val names = industries.map { it.name }
        assertTrue { names.contains(industry.name) }
    }

    @Test
    fun `test find by status fail`() {
        val newIndustry = getIndustry().apply { status = Status.inactive }
        repo.save(newIndustry)
        assertTrue { newIndustry.id > 0 }
        val savedIndustry = repo.findByStatus(Status.active)
        assertNotNull(savedIndustry)
        assertTrue { savedIndustry.isNotEmpty() }
        val ids = savedIndustry.map { it.id }
        assertFalse { ids.contains(newIndustry.id) }
    }

    @Test
    fun `test find by name ignore case`() {
        val name = industry.name.uppercase(Locale.getDefault())
        val i = repo.findByNameIgnoreCase(name)
        assertNotNull(i)
        assertEquals(industry.name, i.name)
    }
}
