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
import org.tctalent.server.model.db.Employer
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest

class EmployerRepositoryIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repository: EmployerRepository
  private val testDescription = "The description"
  private val salesforceId = "salesforceId"

  @BeforeTest
  fun setup() {
    val employer =
      Employer().apply {
        description = testDescription
        sfId = salesforceId
      }
    repository.save(employer)
    assertTrue { employer.id > 0 }
  }

  @Test
  fun `find first by sf id`() {
    assertTrue { isContainerInitialized() }

    val savedEmployee = repository.findFirstBySfId(salesforceId).getOrNull()
    assertNotNull(savedEmployee)
    assertTrue { savedEmployee.description == testDescription }
  }

  @Test
  fun `find by sf id fail`() {
    assertTrue { isContainerInitialized() }

    val savedEmployee = repository.findFirstBySfId(salesforceId + "00").getOrNull()
    assertNull(savedEmployee)
  }
}
