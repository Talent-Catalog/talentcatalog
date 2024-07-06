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

package org.tctalent.server.repository.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tctalent.server.model.db.AuditLog;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;
import org.tctalent.server.repository.db.integrationhelp.DomainHelpers;
import org.tctalent.server.service.db.audit.AuditType;

public class AuditLogRepositoryIntTest extends BaseDBIntegrationTest {

  @Autowired
  private AuditLogRepository repo;
  private final String objRef = "TEST_AUDIT";
  private AuditLog auditLog;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());
    auditLog = DomainHelpers.getAuditLog(objRef);
    repo.save(auditLog);
    assertTrue(auditLog.getId() > 0);
  }

  @Test
  public void testFindByTypeAndObject() {
    AuditLog savedAuditLog = repo.findByTypeAndObjectRef(AuditType.CANDIDATE_OCCUPATION, objRef);
    assertNotNull(savedAuditLog);
    assertEquals(objRef, savedAuditLog.getObjectRef());
  }

  @Test
  public void testFindByTypeAndObjectRefFail() {
    AuditLog savedAuditLog = repo.findByTypeAndObjectRef(AuditType.CANDIDATE_OCCUPATION,
        objRef + "OO");
    assertNull(savedAuditLog);
  }
}
