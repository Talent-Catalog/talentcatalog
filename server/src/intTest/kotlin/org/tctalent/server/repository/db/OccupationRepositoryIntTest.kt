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
import kotlin.test.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.tctalent.server.model.db.Occupation
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest

class OccupationRepositoryIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repo: OccupationRepository
  private lateinit var occupation: Occupation

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
  }

  @Test fun `test get names for ids`() {}
}
