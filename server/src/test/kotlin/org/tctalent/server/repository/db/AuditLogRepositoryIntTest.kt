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
import org.tctalent.server.model.db.AuditLog
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest
import org.tctalent.server.service.db.audit.AuditType

open class AuditLogRepositoryIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repository: AuditLogRepository

  @Test
  fun `test find by type and object`() {
    assertTrue(isContainerInitialized())
    val auditLog = AuditLog()
    auditLog.type = AuditType.CANDIDATE_OCCUPATION
    auditLog.userId = 9L

    assertNull(auditLog.id, "Expect Id to be null")
    repository.save(auditLog)
    assertNotEquals(0L, auditLog.id)
  }
}
