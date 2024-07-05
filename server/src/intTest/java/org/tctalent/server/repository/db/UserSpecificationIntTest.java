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

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;
import org.tctalent.server.repository.db.integrationhelp.DomainHelpers;

public class UserSpecificationIntTest extends BaseDBIntegrationTest {

  @Autowired
  private UserRepository repo;
  private User testUser;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());
    testUser = DomainHelpers.getUser();
    testUser.setRole(Role.admin);
    repo.save(testUser);
    assertTrue(testUser.getId() > 0);
  }


}
