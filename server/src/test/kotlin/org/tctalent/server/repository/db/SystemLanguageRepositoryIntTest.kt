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
import org.tctalent.server.model.db.Status
import org.tctalent.server.model.db.SystemLanguage
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest
import org.tctalent.server.repository.db.integrationhelp.getSavedSystemLanguage
import org.testcontainers.shaded.org.bouncycastle.asn1.x500.style.RFC4519Style.name
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SystemLanguageRepositoryIntTest: BaseDBIntegrationTest() {
    @Autowired
    private lateinit var repo: SystemLanguageRepository

    @Test
    fun `find by status`() {
        assertTrue { isContainerInitialized() }

        val sl = getSavedSystemLanguage(repo)
        assertNotNull(sl.id)
        assertTrue { sl.id > 0 }

        val savedLang = repo.findByStatus(Status.active)
        assertNotNull(savedLang)
        assertTrue { savedLang.isNotEmpty() }
    }

    @Test
    fun `find by status fails`() {
        assertTrue { isContainerInitialized() }

        val sl = getSavedSystemLanguage(repo)
        assertNotNull(sl.id)
        assertTrue { sl.id > 0 }

        val savedLang = repo.findByStatus(Status.deleted)
        assertNotNull(savedLang)
        assertTrue { savedLang.isEmpty() }
    }
}
